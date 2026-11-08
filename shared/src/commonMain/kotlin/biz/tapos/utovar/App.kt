package biz.tapos.utovar

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import biz.tapos.utovar.api.ApiClient
import biz.tapos.utovar.api.Truck
import biz.tapos.utovar.api.TruckStatusResponse
import biz.tapos.utovar.ui.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import taposutovar.shared.generated.resources.Res
import taposutovar.shared.generated.resources.splash_bg

enum class Screen {
    SPLASH,
    LOGIN,
    TRUCK_SELECT,
    MAIN_WORKFLOW
}

@Composable
fun App() {
    var currentScreen by remember { mutableStateOf(Screen.SPLASH) }
    var selectedTruck by remember { mutableStateOf<Truck?>(null) }
    var activeTab by remember { mutableStateOf(0) }
    var truckStatus by remember { mutableStateOf<TruckStatusResponse?>(null) }
    val scope = rememberCoroutineScope()

    fun refreshStatus() {
        val truckId = selectedTruck?.id ?: return
        scope.launch {
            try {
                truckStatus = ApiClient.getTruckStatus(truckId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // SPLASH TIMER (2 sekunde)
    LaunchedEffect(Unit) {
        delay(2000)
        currentScreen = if (ApiClient.token != null) Screen.TRUCK_SELECT else Screen.LOGIN
    }

    MaterialTheme(colorScheme = darkColorScheme(background = Color(0xFF0F172A))) {
        when (currentScreen) {
            Screen.SPLASH -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0F172A))
                ) {
                    Image(
                        painter = painterResource(Res.drawable.splash_bg),
                        contentDescription = "Loading",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    CircularProgressIndicator(
                        color = Color(0xFFF39C12),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 60.dp)
                            .size(40.dp)
                    )
                }
            }

            Screen.LOGIN -> {
                LoginScreen(onLoginSuccess = {
                    currentScreen = Screen.TRUCK_SELECT
                })
            }

            Screen.TRUCK_SELECT -> {
                TruckSelectScreen(
                    onTruckSelected = { truck ->
                        selectedTruck = truck
                        refreshStatus()
                        currentScreen = Screen.MAIN_WORKFLOW
                    },
                    onLogout = {
                        ApiClient.logout()
                        currentScreen = Screen.LOGIN
                    }
                )
            }

            Screen.MAIN_WORKFLOW -> {
                Scaffold(
                    topBar = {
                        // POPRAVLJENA GORNJA TRAKA (Dugme 'Promeni' je sada fiksirano i uvek horizontalno)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0F172A))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedTruck?.truckName ?: "Kamion",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f) // Zauzima sav prostor do dugmeta
                            )
                            Spacer(Modifier.width(8.dp))
                            Button(
                                onClick = { currentScreen = Screen.TRUCK_SELECT },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Promeni", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = Color(0xFF0F172A),
                            tonalElevation = 8.dp
                        ) {
                            NavigationBarItem(
                                selected = activeTab == 0,
                                onClick = { activeTab = 0 },
                                label = { Text("Skener", fontWeight = FontWeight.Bold) },
                                icon = { Text("📷", fontSize = 18.sp) }
                            )
                            NavigationBarItem(
                                selected = activeTab == 1,
                                onClick = {
                                    refreshStatus()
                                    activeTab = 1
                                },
                                label = { Text("Pregled", fontWeight = FontWeight.Bold) },
                                icon = { Text("📋", fontSize = 18.sp) }
                            )
                        }
                    }
                ) { padding ->
                    Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                        if (activeTab == 0) {
                            ScannerScreen(
                                truckId = selectedTruck?.id ?: 0,
                                status = truckStatus,
                                onRefreshStatus = { refreshStatus() }
                            )
                        } else {
                            OverviewScreen(status = truckStatus)
                        }
                    }
                }
            }
        }
    }
}