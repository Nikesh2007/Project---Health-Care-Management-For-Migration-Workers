// server.js
const express = require('express');
const multer = require('multer');
const AWS = require('aws-sdk');
const cors = require('cors');

const app = express();
const port = process.env.PORT || 3000;

// Enable CORS for all origins
app.use(cors());

// Use multer to parse multipart form data and store files in memory
const upload = multer({ storage: multer.memoryStorage() });

// Configure AWS SDK with your credentials and region
const s3 = new AWS.S3({
  accessKeyId: 'AKIAZM5MREM2YKXSDLDU',            // Replace with your actual access key ID
  secretAccessKey: 'Ae40jtv8LoMwFyRieHFGL3tJ4WfcxCyFm1Y9zOLw', // Replace with your secret access key
  region: 'ap-southeast-2'                         // Your bucket region
});
const bucketName = 'patient-report-10';

// POST endpoint for uploading a PDF file
app.post('/upload-pdf', upload.single('pdfFile'), (req, res) => {
  if (!req.file) {
    return res.status(400).json({ error: 'No file uploaded.' });
  }
  const aadhar = req.body.aadhar;
  if (!aadhar) {
    return res.status(400).json({ error: 'Aadhar number is required.' });
  }

  // Set S3 upload parameters - put files inside folder named after Aadhar number
  const params = {
    Bucket: bucketName,
    Key: `${aadhar}/${Date.now()}_${req.file.originalname}`, // Folder by Aadhar number
    Body: req.file.buffer,
    ContentType: req.file.mimetype
    // No explicit ACL - use bucket default
  };

  // Upload file to S3
  s3.upload(params, (err, data) => {
    if (err) {
      console.error('S3 upload error:', err);
      return res.status(500).json({ error: 'Error uploading file to S3.' });
    }
    // Success - return the file URL
    res.json({ url: data.Location });
  });
});

// Start the server
app.listen(port, () => {
  console.log(`Server running on port ${port}`);
});
