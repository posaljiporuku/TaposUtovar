package biz.tapos.utovar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import biz.tapos.utovar.api.ApiClient
import biz.tapos.utovar.api.Truck
import kotlinx.coroutines.launch

@Composable
fun TruckSelectScreen(
    onTruckSelected: (Truck) -> Unit,
    onLogout: () -> Unit
) {
    var trucks by remember { mutableStateOf<List<Truck>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val res = ApiClient.getTrucks()
                if (res.success) trucks = res.trucks
                else errorMsg = res.message
            } catch (e: Exception) {
                errorMsg = e.message
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().background(Color(0xFF0F172A)).padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Izaberite Kamion", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                TextButton(onClick = onLogout) {
                    Text("Odjava", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = Color(0xFF1E293B)
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = Color(0xFF1ABC9C))
                errorMsg != null -> Text("Greška: $errorMsg", color = Color.Red, modifier = Modifier.align(Alignment.Center))
                trucks.isEmpty() -> Text("Nema aktivnih kamiona.", color = Color.White, modifier = Modifier.align(Alignment.Center))
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        items(trucks) { truck ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .clickable { onTruckSelected(truck) },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF334155)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(20.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        truck.truckName,
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}