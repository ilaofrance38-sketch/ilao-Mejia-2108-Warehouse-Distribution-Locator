package com.example.nasugbuwarehouselocator

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

// ==========================================
// 1. BLUE COLOR PALETTE (THEME)
// ==========================================
val DeepBlue = Color(0xFF0D47A1)
val PrimaryBlue = Color(0xFF1565C0)
val BrightBlue = Color(0xFF1E88E5)
val LightBlueBg = Color(0xFFE3F2FD)
val SurfaceWhite = Color(0xFFFFFFFF)
val FilterAccent = Color(0xFFFF8F00)

val CapacityWarning = Color(0xFFD32F2F)
val CapacitySafe = Color(0xFF388E3C)
val CapacityMedium = Color(0xFFF57C00)

private val AppBlueColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = LightBlueBg,
    onPrimaryContainer = DeepBlue,
    secondary = BrightBlue,
    background = LightBlueBg,
    surface = SurfaceWhite
)

// ==========================================
// 2. DATA MODELS & NASUGBU WAREHOUSE DATA
// ==========================================
data class UserAccount(
    val fullName: String,
    val email: String,
    val password: String
)

data class InventoryZone(
    val code: String,       // e.g. "Zone A"
    val name: String,       // e.g. "Dry Goods & FMCG"
    val loadPercent: Int    // e.g. 78
)

data class DeliveryDestination(
    val barangay: String,   // e.g. "Brgy. Wawa"
    val latitude: Double,
    val longitude: Double,
    val distanceKm: Double,
    val status: String      // "Active Route", "Daily Delivery", "Scheduled"
)

data class Warehouse(
    val id: String,
    val name: String,
    val category: String,
    val barangay: String,
    val latitude: Double,
    val longitude: Double,
    val capacity: String,
    val status: String,
    val contact: String,
    val goodsType: String,
    val percentFull: Int,
    val inventoryZones: List<InventoryZone>,
    val deliveryDestinations: List<DeliveryDestination>
)

val nasugbuWarehouses = listOf(
    Warehouse(
        id = "WH-01",
        name = "Nasugbu Central Warehouse",
        category = "Central Warehouse",
        barangay = "Brgy. Poblacion, Nasugbu",
        latitude = 14.0695,
        longitude = 120.6322,
        capacity = "85% Full (12,000 pallets)",
        percentFull = 85,
        status = "Operational",
        contact = "Logistics Coordinator: 0917-555-0101",
        goodsType = "FMCG & Retail Goods",
        inventoryZones = listOf(
            InventoryZone("Zone A", "Dry Goods & FMCG", 78),
            InventoryZone("Zone B", "Beverages & Canned Goods", 92),
            InventoryZone("Zone C", "General Supplies", 85)
        ),
        deliveryDestinations = listOf(
            DeliveryDestination("Brgy. Poblacion", 14.0695, 120.6322, 0.5, "Daily Active"),
            DeliveryDestination("Brgy. Wawa", 14.0842, 120.6208, 2.3, "Active Route"),
            DeliveryDestination("Brgy. Bucana", 14.0520, 120.6285, 3.1, "Scheduled")
        )
    ),
    Warehouse(
        id = "WH-02",
        name = "Wawa Cold Storage Hub",
        category = "Cold Storage",
        barangay = "Brgy. Wawa, Nasugbu",
        latitude = 14.0842,
        longitude = 120.6208,
        capacity = "60% Full (8,500 pallets)",
        percentFull = 60,
        status = "Operational",
        contact = "Warehouse Manager: 0917-555-0102",
        goodsType = "Cold Storage & Seafood",
        inventoryZones = listOf(
            InventoryZone("Zone A", "Frozen Seafood & Fish", 55),
            InventoryZone("Zone B", "Poultry & Meat Cold Room", 68),
            InventoryZone("Zone C", "Dairy & Frozen Desserts", 57)
        ),
        deliveryDestinations = listOf(
            DeliveryDestination("Brgy. Wawa", 14.0842, 120.6208, 0.3, "Daily Active"),
            DeliveryDestination("Brgy. Natipuan", 14.1295, 120.6255, 5.2, "Active Route"),
            DeliveryDestination("Brgy. Pantalan", 14.0720, 120.6275, 1.8, "Daily Active")
        )
    ),
    Warehouse(
        id = "WH-03",
        name = "Lumbangan Fulfillment Center",
        category = "Fulfillment Center",
        barangay = "Brgy. Lumbangan, Nasugbu",
        latitude = 14.0588,
        longitude = 120.6725,
        capacity = "95% Full (15,000 pallets)",
        percentFull = 95,
        status = "At Full Capacity",
        contact = "Logistics Coordinator: 0917-555-0103",
        goodsType = "Sugar, Rice & Agricultural Supplies",
        inventoryZones = listOf(
            InventoryZone("Zone A", "Raw Sugar Bulk Silos", 98),
            InventoryZone("Zone B", "Sacked Rice Storage", 94),
            InventoryZone("Zone C", "Agri Fertilizers", 93)
        ),
        deliveryDestinations = listOf(
            DeliveryDestination("Brgy. Lumbangan", 14.0588, 120.6725, 0.4, "High Volume Route"),
            DeliveryDestination("Brgy. Aga", 14.0890, 120.7350, 7.5, "Active Route"),
            DeliveryDestination("Brgy. Kaylaway", 14.0825, 120.7930, 13.8, "Scheduled")
        )
    ),
    Warehouse(
        id = "WH-04",
        name = "Aga Highway Transit Warehouse",
        category = "Fulfillment Center",
        barangay = "Brgy. Aga, Nasugbu",
        latitude = 14.0890,
        longitude = 120.7350,
        capacity = "45% Full (10,000 pallets)",
        percentFull = 45,
        status = "Operational",
        contact = "Warehouse Manager: 0917-555-0104",
        goodsType = "Construction & Hardware",
        inventoryZones = listOf(
            InventoryZone("Zone A", "Cement & Construction Materials", 40),
            InventoryZone("Zone B", "Hardware & Electrical Supplies", 52),
            InventoryZone("Zone C", "Steel & Pipes Yard", 43)
        ),
        deliveryDestinations = listOf(
            DeliveryDestination("Brgy. Aga", 14.0890, 120.7350, 0.5, "Daily Active"),
            DeliveryDestination("Brgy. Kaylaway", 14.0825, 120.7930, 6.2, "Active Route"),
            DeliveryDestination("Brgy. Lumbangan", 14.0588, 120.6725, 7.5, "Active Route")
        )
    ),
    Warehouse(
        id = "WH-05",
        name = "Kaylaway Highland Cold Chain",
        category = "Cold Storage",
        barangay = "Brgy. Kaylaway, Nasugbu",
        latitude = 14.0825,
        longitude = 120.7930,
        capacity = "90% Full (6,000 pallets)",
        percentFull = 90,
        status = "High Volume",
        contact = "Logistics Coordinator: 0917-555-0105",
        goodsType = "Fresh Produce & Dairy",
        inventoryZones = listOf(
            InventoryZone("Zone A", "Highland Fresh Vegetables", 92),
            InventoryZone("Zone B", "Fruits & Chilled Produce", 88),
            InventoryZone("Zone C", "Milk & Dairy Cold Storage", 90)
        ),
        deliveryDestinations = listOf(
            DeliveryDestination("Brgy. Kaylaway", 14.0825, 120.7930, 0.4, "High Volume Route"),
            DeliveryDestination("Brgy. Aga", 14.0890, 120.7350, 6.2, "Daily Active"),
            DeliveryDestination("Brgy. Poblacion", 14.0695, 120.6322, 17.5, "Scheduled")
        )
    )
)

// ==========================================
// 3. MAIN ACTIVITY
// ==========================================
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().userAgentValue = "com.example.nasugbuwarehouselocator/1.1"
        Configuration.getInstance().load(
            applicationContext,
            applicationContext.getSharedPreferences("nasugbu_prefs", MODE_PRIVATE)
        )

        setContent {
            MaterialTheme(colorScheme = AppBlueColorScheme) {
                WarehouseLocatorApp()
            }
        }
    }
}

// ==========================================
// 4. NAVIGATION & AUTH STATE CONTROLLER
// ==========================================
@Composable
fun WarehouseLocatorApp() {
    val context = LocalContext.current
    val registeredUsers = remember {
        mutableStateListOf(UserAccount("Admin", "admin@nasugbu.ph", "123456"))
    }
    var currentUser by remember { mutableStateOf<UserAccount?>(null) }
    var isSignUpScreen by remember { mutableStateOf(false) }

    if (currentUser == null) {
        if (isSignUpScreen) {
            SignUpScreen(
                onSignUpSuccess = { newUser ->
                    registeredUsers.add(newUser)
                    currentUser = newUser
                    Toast.makeText(context, "Account created! Welcome, ${newUser.fullName}", Toast.LENGTH_SHORT).show()
                },
                onNavigateToLogin = { isSignUpScreen = false }
            )
        } else {
            LoginScreen(
                registeredUsers = registeredUsers,
                onLoginSuccess = { user ->
                    currentUser = user
                    Toast.makeText(context, "Welcome back, ${user.fullName}!", Toast.LENGTH_SHORT).show()
                },
                onNavigateToSignUp = { isSignUpScreen = true }
            )
        }
    } else {
        MapDashboardScreen(
            user = currentUser!!,
            onLogout = {
                currentUser = null
                isSignUpScreen = false
                Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

// ==========================================
// 5 & 6. LOGIN at SIGN-UP
// ==========================================
@Composable
fun LoginScreen(
    registeredUsers: List<UserAccount>,
    onLoginSuccess: (UserAccount) -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    var email by remember { mutableStateOf("admin@nasugbu.ph") }
    var password by remember { mutableStateOf("123456") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DeepBlue, PrimaryBlue, BrightBlue)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = LightBlueBg,
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warehouse,
                        contentDescription = "Warehouse Logo",
                        tint = PrimaryBlue,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Nasugbu Logistics",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepBlue
                )
                Text(
                    text = "Warehouse Distribution Locator",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; errorMessage = "" },
                    label = { Text("Email Address") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = PrimaryBlue) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; errorMessage = "" },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = PrimaryBlue) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Password"
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorMessage, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val matchedUser = registeredUsers.find {
                            it.email.equals(email.trim(), ignoreCase = true) && it.password == password
                        }
                        if (matchedUser != null) {
                            onLoginSuccess(matchedUser)
                        } else {
                            errorMessage = "Invalid email or password."
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("LOGIN", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Wala pang account? ", fontSize = 14.sp, color = Color.Gray)
                    Text(
                        text = "Sign Up",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue,
                        modifier = Modifier.clickable { onNavigateToSignUp() }
                    )
                }
            }
        }
    }
}

@Composable
fun SignUpScreen(
    onSignUpSuccess: (UserAccount) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DeepBlue, PrimaryBlue, BrightBlue)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Create Account",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepBlue
                )
                Text(
                    text = "Mag-register para ma-access ang Nasugbu Map",
                    fontSize = 13.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it; errorMessage = "" },
                    label = { Text("Full Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryBlue) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; errorMessage = "" },
                    label = { Text("Email Address") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = PrimaryBlue) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; errorMessage = "" },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = PrimaryBlue) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; errorMessage = "" },
                    label = { Text("Confirm Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = PrimaryBlue) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorMessage, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        when {
                            fullName.isBlank() || email.isBlank() || password.isBlank() -> {
                                errorMessage = "Pakipunan ang lahat ng fields."
                            }
                            password.length < 6 -> {
                                errorMessage = "Dapat hindi bababa sa 6 characters ang password."
                            }
                            password != confirmPassword -> {
                                errorMessage = "Hindi magkatugma ang password."
                            }
                            else -> {
                                onSignUpSuccess(UserAccount(fullName.trim(), email.trim(), password))
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("SIGN UP", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("May account na? ", fontSize = 14.sp, color = Color.Gray)
                    Text(
                        text = "Login",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue,
                        modifier = Modifier.clickable { onNavigateToLogin() }
                    )
                }
            }
        }
    }
}

// ==========================================
// 7. MAP DASHBOARD SCREEN WITH ZONES & DESTINATIONS
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapDashboardScreen(
    user: UserAccount,
    onLogout: () -> Unit
) {
    val categories = listOf("Central Warehouse", "Cold Storage", "Fulfillment Center")
    var selectedCategory by remember { mutableStateOf(categories.first()) }

    val filteredWarehouses = nasugbuWarehouses.filter { it.category == selectedCategory }

    var selectedWarehouse by remember { mutableStateOf(filteredWarehouses.firstOrNull()) }
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }

    // Toggle Tab inside Card: 0 = General Info & Inventory Zones, 1 = Delivery Destinations
    var cardDetailTab by remember { mutableIntStateOf(0) }

    val lifecycleOwner = LocalLifecycleOwner.current
    val nasugbuCenter = remember { GeoPoint(14.0750, 120.6650) }

    LaunchedEffect(selectedCategory) {
        if (filteredWarehouses.isNotEmpty()) {
            selectedWarehouse = filteredWarehouses.first()
            val point = GeoPoint(selectedWarehouse!!.latitude, selectedWarehouse!!.longitude)
            mapViewRef?.controller?.animateTo(point)
            mapViewRef?.controller?.setZoom(14.5)
        } else {
            selectedWarehouse = null
        }
    }

    DisposableEffect(lifecycleOwner, mapViewRef) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapViewRef?.onResume()
                Lifecycle.Event.ON_PAUSE -> mapViewRef?.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Nasugbu Warehouse Map",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Text(
                            text = "User: ${user.fullName}",
                            fontSize = 12.sp,
                            color = LightBlueBg
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepBlue
                ),
                actions = {
                    IconButton(onClick = {
                        mapViewRef?.controller?.animateTo(nasugbuCenter)
                        mapViewRef?.controller?.setZoom(14.5)
                    }) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Center Nasugbu",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Logout",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.OpenTopo)
                        setMultiTouchControls(true)
                        isTilesScaledToDpi = true
                        setUseDataConnection(true)

                        minZoomLevel = 10.0
                        maxZoomLevel = 20.0
                        controller.setZoom(14.5)
                        controller.setCenter(nasugbuCenter)

                        mapViewRef = this
                    }
                },
                update = { map ->
                    map.overlays.clear()

                    // 1. Draw Warehouses Markers
                    filteredWarehouses.forEach { warehouse ->
                        val marker = Marker(map).apply {
                            position = GeoPoint(warehouse.latitude, warehouse.longitude)
                            title = warehouse.name
                            snippet = "${warehouse.barangay} • ${warehouse.goodsType}"
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            setOnMarkerClickListener { _, _ ->
                                selectedWarehouse = warehouse
                                map.controller.animateTo(position)
                                map.controller.setZoom(15.5)
                                showInfoWindow()
                                true
                            }
                        }
                        map.overlays.add(marker)
                    }

                    // 2. Draw Delivery Destinations Markers when a warehouse is selected
                    selectedWarehouse?.let { warehouse ->
                        warehouse.deliveryDestinations.forEach { dest ->
                            val destMarker = Marker(map).apply {
                                position = GeoPoint(dest.latitude, dest.longitude)
                                title = "Destination: ${dest.barangay}"
                                snippet = "Served by ${warehouse.name} (${dest.distanceKm} km • ${dest.status})"
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                            }
                            map.overlays.add(destMarker)
                        }
                    }

                    map.invalidate()
                }
            )

            // Category & Barangay Filters
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
            ) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, start = 8.dp, end = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        val isSelected = selectedCategory == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = category },
                            label = {
                                Text(
                                    text = category,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color.White,
                                labelColor = DeepBlue,
                                selectedContainerColor = FilterAccent,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, start = 8.dp, end = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredWarehouses) { warehouse ->
                        val isSelected = selectedWarehouse?.id == warehouse.id
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedWarehouse = warehouse
                                val point = GeoPoint(warehouse.latitude, warehouse.longitude)
                                mapViewRef?.controller?.animateTo(point)
                                mapViewRef?.controller?.setZoom(15.5)
                            },
                            label = {
                                Text(
                                    text = warehouse.barangay.removePrefix("Brgy. "),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color.White,
                                labelColor = DeepBlue,
                                selectedContainerColor = PrimaryBlue,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            )
                        )
                    }
                }
            }

            // Bottom Selected Warehouse Details Card with Inventory Zones & Destinations
            selectedWarehouse?.let { warehouse ->
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Header Row: Warehouse ID & Status Badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = LightBlueBg,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = warehouse.id,
                                    color = DeepBlue,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            val statusColor = when {
                                warehouse.percentFull >= 90 -> CapacityWarning
                                warehouse.percentFull >= 75 -> CapacityMedium
                                else -> CapacitySafe
                            }
                            val statusBg = statusColor.copy(alpha = 0.1f)

                            Surface(
                                color = statusBg,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = warehouse.status,
                                    color = statusColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = warehouse.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepBlue
                        )
                        Text(
                            text = warehouse.barangay,
                            fontSize = 13.sp,
                            color = Color.DarkGray
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Toggle Chips between Inventory Zones & Delivery Destinations
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = cardDetailTab == 0,
                                onClick = { cardDetailTab = 0 },
                                label = { Text("📦 Inventory Zones", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryBlue,
                                    selectedLabelColor = Color.White
                                )
                            )
                            FilterChip(
                                selected = cardDetailTab == 1,
                                onClick = { cardDetailTab = 1 },
                                label = { Text("🚚 Delivery Destinations", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryBlue,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        if (cardDetailTab == 0) {
                            // Section: Inventory Zones Load Percentages
                            Column {
                                Text("Storage Zones Capacity:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                                Spacer(modifier = Modifier.height(6.dp))
                                warehouse.inventoryZones.forEach { zone ->
                                    val zoneColor = when {
                                        zone.loadPercent >= 90 -> CapacityWarning
                                        zone.loadPercent >= 75 -> CapacityMedium
                                        else -> CapacitySafe
                                    }
                                    Column(modifier = Modifier.padding(vertical = 3.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("${zone.code} (${zone.name})", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.SemiBold)
                                            Text("${zone.loadPercent}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = zoneColor)
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        LinearProgressIndicator(
                                            progress = { zone.loadPercent / 100f },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp)),
                                            color = zoneColor,
                                            trackColor = LightBlueBg
                                        )
                                    }
                                }
                            }
                        } else {
                            // Section: Barangay Delivery Destinations
                            Column {
                                Text("Served Delivery Destinations (Nasugbu):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeepBlue)
                                Spacer(modifier = Modifier.height(6.dp))
                                warehouse.deliveryDestinations.forEach { dest ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.LocalShipping, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(dest.barangay, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                                        }
                                        Text("${dest.distanceKm} km • ${dest.status}", fontSize = 11.sp, color = PrimaryBlue, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Contact: ${warehouse.contact}",
                                fontSize = 12.sp,
                                color = Color.DarkGray
                            )
                        }
                    }
                }
            }
        }
    }
}
