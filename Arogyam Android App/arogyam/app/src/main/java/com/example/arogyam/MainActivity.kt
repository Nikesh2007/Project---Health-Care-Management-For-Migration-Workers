package com.example.arogyam

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arogyam.ui.theme.ArogyamTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class UserData(
    val name: String = "",
    val age: String = "",
    val mobile: String = "",
    val email: String = "",
    val occupation: String = "",
    val pincode: String = "",
    val state: String = "",
    val blood: String = "",
    val sex: String = "",
    val altMobile: String = "",
    val dob: String = "",
    val aadhar: String = ""
)

data class PatientReport(
    val id: String = "",
    val name: String = "",
    val doctorName: String = "",
    val department: String = "",
    val blood: String = "",
    val allergies: String = "",
    val bloodTestURL: String = "",
    val covidCertURL: String = "",
    val hospitalId: String = "",
    val hospitalized: Boolean = false,
    val insuranceIssued: Boolean = false,
    val insuranceURL: String = "",
    val lifethreateningDisease: String = "",
    val medicineDetails: String = "",
    val otherDocsURL: String = "",
    val surgeries: String = "",
    val fromDate: String = "",
    val endDate: String = "",
    val aadhar: String = ""
)

data class S3File(
    val fileName: String = "",
    val size: Long = 0L,
    val lastModified: String = "",
    val url: String = "",
    val key: String = ""
)

data class NavigationItem(
    val title: String,
    val icon: ImageVector,
    val route: String,
    val isSpecial: Boolean = false
)

sealed class Screen(val route: String) {
    object Profile : Screen("profile")
    object HealthReports : Screen("health_reports")
    object AboutUs : Screen("about_us")
    object SOS : Screen("sos")
}

object AWSConfig {
    const val ACCESS_KEY_ID = "AKIAZM5MREM2YKXSDLDU"
    const val SECRET_ACCESS_KEY = "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"
    const val REGION = "ap-southeast-2"
    const val BUCKET_NAME = "patient-report-10"
    const val BASE_URL = "https://$BUCKET_NAME.s3.$REGION.amazonaws.com"
}

suspend fun generateS3Files(aadharNumber: String): List<S3File> {
    return withContext(Dispatchers.IO) {
        listOf(
            S3File(
                fileName = "bloodtest_report.pdf",
                size = 1024 * 245,
                lastModified = "2025-09-15",
                url = "${AWSConfig.BASE_URL}/$aadharNumber/bloodtest_report.pdf",
                key = "$aadharNumber/bloodtest_report.pdf"
            ),
            S3File(
                fileName = "xray_chest.pdf",
                size = 1024 * 180,
                lastModified = "2025-09-14",
                url = "${AWSConfig.BASE_URL}/$aadharNumber/xray_chest.pdf",
                key = "$aadharNumber/xray_chest.pdf"
            ),
            S3File(
                fileName = "medical_certificate.pdf",
                size = 1024 * 95,
                lastModified = "2025-09-13",
                url = "${AWSConfig.BASE_URL}/$aadharNumber/medical_certificate.pdf",
                key = "$aadharNumber/medical_certificate.pdf"
            ),
            S3File(
                fileName = "prescription.pdf",
                size = 1024 * 67,
                lastModified = "2025-09-12",
                url = "${AWSConfig.BASE_URL}/$aadharNumber/prescription.pdf",
                key = "$aadharNumber/prescription.pdf"
            )
        )
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()
        setContent {
            ArogyamTheme {
                ArogyamApp()
            }
        }
    }
}

@Composable
fun ArogyamApp() {
    var isLoggedIn by remember { mutableStateOf(false) }
    var userData by remember { mutableStateOf(UserData()) }

    if (isLoggedIn) {
        MainScreenWithDrawer(userData = userData) {
            isLoggedIn = false
        }
    } else {
        LoginScreen { user ->
            userData = user
            isLoggedIn = true
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenWithDrawer(userData: UserData, onLogout: () -> Unit) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedScreen by remember { mutableStateOf(Screen.Profile.route) }

    val navigationItems = listOf(
        NavigationItem("Profile", Icons.Default.Person, Screen.Profile.route),
        NavigationItem("Health Reports", Icons.Default.List, Screen.HealthReports.route),
        NavigationItem("About Us", Icons.Default.Info, Screen.AboutUs.route),
        NavigationItem("🆘 SOS Emergency", Icons.Default.Warning, Screen.SOS.route, isSpecial = true)
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(280.dp)
            ) {
                DrawerContent(
                    userData = userData,
                    navigationItems = navigationItems,
                    selectedScreen = selectedScreen,
                    onItemClick = { screen ->
                        selectedScreen = screen
                        scope.launch {
                            drawerState.close()
                        }
                    },
                    onLogout = {
                        scope.launch {
                            drawerState.close()
                        }
                        onLogout()
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = when (selectedScreen) {
                                Screen.Profile.route -> "Profile"
                                Screen.HealthReports.route -> "Health Reports"
                                Screen.AboutUs.route -> "About Us"
                                Screen.SOS.route -> "🆘 Emergency SOS"
                                else -> "Arogyam"
                            },
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    if (drawerState.isClosed) {
                                        drawerState.open()
                                    } else {
                                        drawerState.close()
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = Color(0xFF2196F3)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White,
                        titleContentColor = if (selectedScreen == Screen.SOS.route) Color(0xFFE53935) else Color(0xFF2196F3)
                    )
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF5F5F5))
            ) {
                when (selectedScreen) {
                    Screen.Profile.route -> ProfileContent(userData)
                    Screen.HealthReports.route -> HealthReportsContent(userData)
                    Screen.AboutUs.route -> AboutUsContent()
                    Screen.SOS.route -> SOSContent(userData)
                }
            }
        }
    }
}

@Composable
fun DrawerContent(
    userData: UserData,
    navigationItems: List<NavigationItem>,
    selectedScreen: String,
    onItemClick: (String) -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF2196F3)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🏥",
                        fontSize = 24.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = userData.name.ifEmpty { "User" },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Arogyam Patient",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        navigationItems.forEach { item ->
            NavigationDrawerItem(
                label = {
                    Text(
                        text = item.title,
                        fontSize = 16.sp,
                        fontWeight = if (item.isSpecial) FontWeight.Bold else FontWeight.Medium
                    )
                },
                selected = selectedScreen == item.route,
                onClick = { onItemClick(item.route) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = when {
                            item.isSpecial -> Color(0xFFE53935)
                            selectedScreen == item.route -> Color(0xFF2196F3)
                            else -> Color.Gray
                        }
                    )
                },
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = if (item.isSpecial)
                        Color(0xFFE53935).copy(alpha = 0.1f)
                    else Color(0xFF2196F3).copy(alpha = 0.1f),
                    selectedTextColor = if (item.isSpecial) Color(0xFFE53935) else Color(0xFF2196F3),
                    selectedIconColor = if (item.isSpecial) Color(0xFFE53935) else Color(0xFF2196F3),
                    unselectedTextColor = if (item.isSpecial) Color(0xFFE53935) else Color.Black,
                    unselectedIconColor = if (item.isSpecial) Color(0xFFE53935) else Color.Gray
                ),
                modifier = Modifier.padding(
                    NavigationDrawerItemDefaults.ItemPadding
                )
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = Color.LightGray
        )

        NavigationDrawerItem(
            label = {
                Text(
                    text = "Logout",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            },
            selected = false,
            onClick = onLogout,
            icon = {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Logout",
                    tint = Color(0xFFE57373)
                )
            },
            colors = NavigationDrawerItemDefaults.colors(
                unselectedTextColor = Color(0xFFE57373),
                unselectedIconColor = Color(0xFFE57373)
            ),
            modifier = Modifier.padding(
                NavigationDrawerItemDefaults.ItemPadding
            )
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun SOSContent(userData: UserData) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE53935))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🆘",
                        fontSize = 60.sp
                    )
                    Text(
                        text = "EMERGENCY SOS",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Get immediate help in medical emergencies",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        item {
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_CALL).apply {
                        data = Uri.parse("tel:108")
                    }
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "📞 Call Ambulance (108)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        item {
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_CALL).apply {
                        data = Uri.parse("tel:102")
                    }
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "🚑 Emergency Services (102)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        item {
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_CALL).apply {
                        data = Uri.parse("tel:100")
                    }
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "🚓 Police Emergency (100)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Your Medical Information",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF333333)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Name: ${userData.name}", fontSize = 14.sp)
                    Text("Blood Group: ${userData.blood}", fontSize = 14.sp)
                    Text("Mobile: ${userData.mobile}", fontSize = 14.sp)
                    Text("Age: ${userData.age}", fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun ProfileContent(userData: UserData) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ProfileSection(title = "Personal Information") {
                ProfileItem(Icons.Default.Person, "Name", userData.name.ifEmpty { "Not provided" })
                ProfileItem(Icons.Default.DateRange, "Date of Birth", userData.dob.ifEmpty { "Not provided" })
                ProfileItem(Icons.Default.AccountCircle, "Gender", userData.sex.ifEmpty { "Not provided" })
                ProfileItem(Icons.Default.AccountCircle, "Age", userData.age.ifEmpty { "Not provided" })
            }
        }

        item {
            ProfileSection(title = "Contact Information") {
                ProfileItem(Icons.Default.Phone, "Mobile", userData.mobile.ifEmpty { "Not provided" })
                ProfileItem(Icons.Default.Phone, "Alt Mobile", userData.altMobile.ifEmpty { "Not provided" })
                ProfileItem(Icons.Default.Email, "Email", userData.email.ifEmpty { "Not provided" })
            }
        }

        item {
            ProfileSection(title = "Location Information") {
                ProfileItem(Icons.Default.LocationOn, "State", userData.state.ifEmpty { "Not provided" })
                ProfileItem(Icons.Default.Home, "Pincode", userData.pincode.ifEmpty { "Not provided" })
            }
        }

        item {
            ProfileSection(title = "Medical Information") {
                ProfileItem(Icons.Default.Favorite, "Blood Group", userData.blood.ifEmpty { "Not provided" })
                ProfileItem(Icons.Default.Build, "Occupation", userData.occupation.ifEmpty { "Not provided" })
            }
        }
    }
}

@Composable
fun HealthReportsContent(userData: UserData) {
    var reports by remember { mutableStateOf<List<PatientReport>>(emptyList()) }
    var s3Files by remember { mutableStateOf<List<S3File>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isLoadingS3 by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(userData.aadhar) {
        if (userData.aadhar.isNotEmpty()) {
            try {
                val db = Firebase.firestore
                println("DEBUG: Fetching reports for aadhar: ${userData.aadhar}")

                val snapshot = db.collection("patientReports")
                    .whereEqualTo("aadhar", userData.aadhar)
                    .get()
                    .await()

                println("DEBUG: Found ${snapshot.documents.size} documents")

                val reportsList = snapshot.documents.mapIndexed { index, doc ->
                    println("DEBUG: Document $index - ID: ${doc.id}")
                    println("DEBUG: Document $index data: ${doc.data}")

                    PatientReport(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        doctorName = doc.getString("doctorName") ?: "",
                        department = doc.getString("department") ?: "",
                        blood = doc.getString("blood") ?: "",
                        allergies = doc.getString("allergies") ?: "",
                        bloodTestURL = doc.getString("bloodTestURL") ?: "",
                        covidCertURL = doc.getString("covidCertURL") ?: "",
                        hospitalId = doc.getString("hospitalId") ?: "",
                        hospitalized = doc.getBoolean("hospitalized") ?: false,
                        insuranceIssued = doc.getBoolean("insuranceIssued") ?: false,
                        insuranceURL = doc.getString("insuranceURL") ?: "",
                        lifethreateningDisease = doc.getString("lifethreateningDisease") ?: "",
                        medicineDetails = doc.getString("medicineDetails") ?: "",
                        otherDocsURL = doc.getString("otherDocsURL") ?: "",
                        surgeries = doc.getString("surgeries") ?: "",
                        fromDate = doc.getString("fromDate") ?: "",
                        endDate = doc.getString("endDate") ?: "",
                        aadhar = doc.getString("aadhar") ?: ""
                    )
                }
                reports = reportsList
                println("DEBUG: Processed ${reportsList.size} reports")
                isLoading = false
            } catch (e: Exception) {
                println("DEBUG: Error loading reports: ${e.message}")
                errorMessage = "Failed to load reports: ${e.message}"
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2196F3)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = userData.name.ifEmpty { "Patient" },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Total Reports: ${reports.size}",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    if (userData.aadhar.isNotEmpty()) {
                        Text(
                            text = "ID: ${userData.aadhar}",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "Medical Reports",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )
        }

        if (isLoading) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF2196F3),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Loading reports...",
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        if (errorMessage.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Text(
                        text = errorMessage,
                        fontSize = 14.sp,
                        color = Color(0xFFD32F2F),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        if (reports.isEmpty() && !isLoading && errorMessage.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No reports found",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        )
                        Text(
                            text = "Your medical reports will appear here",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        items(reports) { report ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (report.department.isNotEmpty()) {
                                "${report.department} Report"
                            } else "Medical Report",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2196F3),
                            modifier = Modifier.weight(1f)
                        )

                        if (report.bloodTestURL.isNotEmpty()) {
                            Text(
                                text = "📄 PDF",
                                fontSize = 12.sp,
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .background(
                                        Color(0xFF4CAF50).copy(alpha = 0.1f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (report.name.isNotEmpty()) {
                        DetailRow("👤", "Patient Name", report.name, Color(0xFF333333))
                    }

                    if (report.doctorName.isNotEmpty()) {
                        DetailRow("👨‍⚕️", "Doctor", "Dr. ${report.doctorName}", Color(0xFF2196F3))
                    }

                    if (report.hospitalId.isNotEmpty()) {
                        DetailRow("🏥", "Hospital ID", report.hospitalId, Color(0xFF666666))
                    }

                    if (report.department.isNotEmpty()) {
                        DetailRow("🏢", "Department", report.department, Color(0xFF666666))
                    }

                    if (report.blood.isNotEmpty()) {
                        DetailRow("🩸", "Blood Group", report.blood, Color(0xFFE53935))
                    }

                    if (report.allergies.isNotEmpty() && report.allergies.lowercase() != "none") {
                        DetailRow("⚠️", "Allergies", report.allergies, Color(0xFFFF5722))
                    }

                    if (report.lifethreateningDisease.isNotEmpty() && report.lifethreateningDisease.lowercase() != "none") {
                        DetailRow("🚨", "Critical Condition", report.lifethreateningDisease, Color(0xFFD32F2F))
                    }

                    if (report.surgeries.isNotEmpty() && report.surgeries.lowercase() != "no" && report.surgeries.lowercase() != "none") {
                        DetailRow("🔪", "Surgeries", report.surgeries, Color(0xFF795548))
                    }

                    if (report.medicineDetails.isNotEmpty() && report.medicineDetails.lowercase() != "nil" && report.medicineDetails.lowercase() != "none") {
                        DetailRow("💊", "Medicines", report.medicineDetails, Color(0xFF9C27B0))
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (report.hospitalized) {
                            StatusBadge("🏥 Hospitalized", Color(0xFFFF9800))
                        }

                        if (report.insuranceIssued) {
                            StatusBadge("📋 Insurance", Color(0xFF4CAF50))
                        }
                    }

                    if (report.bloodTestURL.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(report.bloodTestURL))
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2196F3)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("📄 View Report", fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Cloud Storage Files",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AWS S3 Storage",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF333333)
                        )

                        Button(
                            onClick = {
                                isLoadingS3 = true
                                scope.launch {
                                    try {
                                        val files = generateS3Files(userData.aadhar)
                                        s3Files = files
                                    } catch (e: Exception) {
                                        println("S3 Load Error: ${e.message}")
                                    } finally {
                                        isLoadingS3 = false
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2196F3)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            enabled = !isLoadingS3
                        ) {
                            if (isLoadingS3) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Text("🔄 Load Files")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Files path: ${userData.aadhar}/",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )

                    if (s3Files.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Available Files (${s3Files.size})",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF333333)
                        )

                        s3Files.forEach { file ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(file.url))
                                        context.startActivity(intent)
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFF8F9FA)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("📄", fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = file.fileName,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF333333)
                                        )
                                        Text(
                                            text = "${file.size / 1024} KB • ${file.lastModified}",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "VIEW",
                                            fontSize = 12.sp,
                                            color = Color(0xFF2196F3),
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "DOWNLOAD",
                                            fontSize = 10.sp,
                                            color = Color(0xFF4CAF50),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    } else if (!isLoadingS3) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Click 'Load Files' to view documents stored in S3 bucket",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "📁 Your files are stored in: ${userData.aadhar}/ folder",
                            fontSize = 12.sp,
                            color = Color(0xFF666666),
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(emoji: String, label: String, value: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = emoji,
            fontSize = 16.sp,
            modifier = Modifier.width(24.dp)
        )
        Text(
            text = "$label:",
            fontSize = 14.sp,
            color = Color(0xFF666666),
            modifier = Modifier.width(120.dp)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = color,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatusBadge(text: String, color: Color) {
    Text(
        text = text,
        fontSize = 12.sp,
        color = color,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .background(
                color.copy(alpha = 0.1f),
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

@Composable
fun AboutUsContent() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF4FC3F7),
                                        Color(0xFF29B6F6),
                                        Color(0xFF03A9F4)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🏥",
                            fontSize = 40.sp,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "About Arogyam",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2196F3),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Your Health, Our Priority",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Our Mission",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF333333),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Text(
                        text = "Arogyam is your trusted healthcare companion, designed to digitize and secure your medical records. We provide a comprehensive platform for storing, accessing, and managing your health information safely.",
                        fontSize = 16.sp,
                        color = Color(0xFF555555),
                        lineHeight = 24.sp
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Features",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF333333),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    FeatureItem("🔐", "Secure Storage", "All medical records encrypted and stored safely")
                    FeatureItem("📱", "Easy Access", "Access your reports anytime, anywhere")
                    FeatureItem("📋", "Digital Reports", "Upload and organize all your medical documents")
                    FeatureItem("👤", "Personal Profile", "Comprehensive patient information management")
                    FeatureItem("🆘", "Emergency SOS", "Quick access to emergency services")
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Contact Information",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF333333),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    ContactItem(Icons.Default.Email, "support@arogyam.com")
                    ContactItem(Icons.Default.Phone, "+91 9876543210")
                    ContactItem(Icons.Default.LocationOn, "Healthcare Technology Solutions")
                }
            }
        }

        item {
            Text(
                text = "Version 1.0.0\n© 2025 Arogyam Healthcare Solutions",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    }
}

@Composable
fun FeatureItem(icon: String, title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = icon,
            fontSize = 20.sp,
            modifier = Modifier.padding(end = 12.dp, top = 2.dp)
        )
        Column {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333)
            )
            Text(
                text = description,
                fontSize = 14.sp,
                color = Color.Gray,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun ContactItem(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF2196F3),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 16.sp,
            color = Color(0xFF555555)
        )
    }
}

fun convertDateFormat(ddmmyyyy: String): String {
    if (ddmmyyyy.length != 8) return ""
    val dd = ddmmyyyy.substring(0, 2)
    val mm = ddmmyyyy.substring(2, 4)
    val yyyy = ddmmyyyy.substring(4, 8)
    return "$yyyy-$mm-$dd"
}

fun verifyCredentials(aadhar: String, dob: String, onResult: (Boolean, String, UserData?) -> Unit) {
    val db = Firebase.firestore
    val convertedDob = convertDateFormat(dob)

    db.collection("patients")
        .document(aadhar)
        .get()
        .addOnSuccessListener { document ->
            if (document.exists()) {
                val dbDob = document.getString("dob")
                if (dbDob == convertedDob) {
                    val userData = UserData(
                        name = document.getString("name") ?: "",
                        age = document.getString("age") ?: "",
                        mobile = document.getString("mobile") ?: "",
                        email = document.getString("email") ?: "",
                        occupation = document.getString("occupation") ?: "",
                        pincode = document.getString("pincode") ?: "",
                        state = document.getString("state") ?: "",
                        blood = document.getString("blood") ?: "",
                        sex = document.getString("sex") ?: "",
                        altMobile = document.getString("altMobile") ?: "",
                        dob = document.getString("dob") ?: "",
                        aadhar = aadhar
                    )
                    onResult(true, "Login successful! ✅", userData)
                } else {
                    onResult(false, "Invalid Date of Birth ❌", null)
                }
            } else {
                onResult(false, "Aadhaar number not found ❌", null)
            }
        }
        .addOnFailureListener {
            onResult(false, "Connection error. Try again ❌", null)
        }
}

@Composable
fun LoginScreen(onLoginSuccess: (UserData) -> Unit) {
    var aadhar by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF4FC3F7),
                            Color(0xFF29B6F6),
                            Color(0xFF03A9F4)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🏥",
                fontSize = 50.sp,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Arogyam",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2196F3),
            textAlign = TextAlign.Center
        )
        Text(
            text = "Your Health, Our Priority",
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 40.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Welcome Back",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF333333),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Please sign in to continue",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                OutlinedTextField(
                    value = aadhar,
                    onValueChange = {
                        if (it.length <= 12 && it.all { ch -> ch.isDigit() }) {
                            aadhar = it
                        }
                    },
                    label = { Text("Aadhaar Number", color = Color.Gray) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = TextStyle(color = Color.Black),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2196F3),
                        unfocusedBorderColor = Color.LightGray,
                        cursorColor = Color.Black,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = dob,
                    onValueChange = {
                        if (it.length <= 8 && it.all { ch -> ch.isDigit() }) {
                            dob = it
                        }
                    },
                    label = { Text("Date of Birth (DDMMYYYY)", color = Color.Gray) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = TextStyle(color = Color.Black),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2196F3),
                        unfocusedBorderColor = Color.LightGray,
                        cursorColor = Color.Black,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (aadhar.length == 12 && dob.length == 8) {
                            isLoading = true
                            message = "Verifying..."
                            verifyCredentials(aadhar, dob) { success, msg, userData ->
                                isLoading = false
                                message = msg
                                if (success && userData != null) {
                                    onLoginSuccess(userData)
                                }
                            }
                        } else {
                            message = "Please enter valid Aadhaar (12 digits) and DOB (8 digits) ❌"
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = "Sign In",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (message.isNotEmpty()) {
                    Text(
                        text = message,
                        fontSize = 14.sp,
                        color = if (message.contains("successful")) Color(0xFF4CAF50) else Color(0xFFE57373),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Secure • Reliable • Trusted",
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ProfileSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF333333),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

@Composable
fun ProfileItem(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color(0xFF2196F3),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                fontSize = 16.sp,
                color = Color(0xFF333333),
                fontWeight = FontWeight.Normal
            )
        }
    }
}
