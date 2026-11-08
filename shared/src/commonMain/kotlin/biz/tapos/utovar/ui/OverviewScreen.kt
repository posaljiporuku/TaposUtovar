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
import biz.tapos.utovar.api.TruckStatusResponse

@Composable
fun OverviewScreen(status: TruckStatusResponse?) {
    if (status == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF1ABC9C))
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF1E293B)).padding(16.dp)) {
        // Ukupan napredak
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF334155)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${status.totalScanned} / ${status.totalExpected} koleta",
                    color = if (status.isComplete) Color(0xFF22C55E) else Color(0xFF38BDF8),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { (status.progressPercent / 100.0).toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(16.dp),
                    color = Color(0xFF1ABC9C),
                    trackColor = Color(0xFF0F172A)
                )
                Spacer(Modifier.height(4.dp))
                Text("${status.progressPercent}%", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (!status.routeIsSet) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF59E0B))
            ) {
                Text(
                    "Menadžer još nije odredio redosled utovara za ovaj kamion na računaru!",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // Spisak Gradova (Harmonika)
        val cities = if (status.routeCities.isNotEmpty()) status.routeCities else status.citiesData.keys.toList()

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(cities) { city ->
                val cityData = status.citiesData[city]
                var expanded by remember { mutableStateOf(city == status.activeCity) }
                val isDone = (cityData?.scan ?: 0) >= (cityData?.exp ?: 0) && (cityData?.exp ?: 0) > 0
                val isActive = city == status.activeCity

                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isActive) Color(0xFF1ABC9C) else Color(0xFF334155))
                                .clickable { expanded = !expanded }
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(city, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("(${cityData?.scan ?: 0} / ${cityData?.exp ?: 0})", color = Color.LightGray, fontSize = 12.sp)
                            }
                            Text(
                                text = when {
                                    isDone -> "Završeno"
                                    isActive -> "Trenutno"
                                    else -> "Na čekanju"
                                },
                                color = if (isDone) Color(0xFF22C55E) else if (isActive) Color(0xFFFACC15) else Color.Gray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        if (expanded && cityData != null) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                if (cityData.preostalo.isNotEmpty()) {
                                    Text("Preostalo za utovar:", color = Color(0xFFFACC15), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    cityData.preostalo.forEach { item ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(item.naziv, color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                            Text("${item.kol}", color = Color(0xFFFACC15), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }
                                    }
                                    Spacer(Modifier.height(8.dp))
                                }

                                if (cityData.utovareno.isNotEmpty()) {
                                    Text("Utovareno:", color = Color(0xFF22C55E), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    cityData.utovareno.forEach { item ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(item.naziv, color = Color.LightGray, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                            Text("${item.kol}", color = Color(0xFF22C55E), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}