package biz.tapos.utovar.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import biz.tapos.utovar.api.ApiClient
import biz.tapos.utovar.api.ScanRequest
import biz.tapos.utovar.api.TruckStatusResponse
import biz.tapos.utovar.audio.rememberSoundManager
import biz.tapos.utovar.scanner.ScannerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ScannerScreen(
    truckId: Int,
    status: TruckStatusResponse?,
    onRefreshStatus: () -> Unit
) {
    var scanMode by remember { mutableStateOf("load") } // "load" ili "unload"
    var manualCode by remember { mutableStateOf("") }

    // Dijalog / Popup stanje
    var lastScannedCode by remember { mutableStateOf("") }
    var alertMessage by remember { mutableStateOf<String?>(null) }
    var isSuccessAlert by remember { mutableStateOf(true) }
    var isProcessing by remember { mutableStateOf(false) }

    val soundManager = rememberSoundManager()
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    fun processBarcode(code: String) {
        if (code.isBlank() || isProcessing) return
        isProcessing = true
        lastScannedCode = code.trim()

        scope.launch {
            try {
                val res = ApiClient.postScan(ScanRequest(truckId, lastScannedCode, scanMode))
                if (res.success) {
                    soundManager.playSuccess()
                    isSuccessAlert = true
                    alertMessage = res.message.ifBlank { "Uspešno evidentirano!" }
                    onRefreshStatus()
                } else {
                    soundManager.playError()
                    isSuccessAlert = false
                    alertMessage = res.message.ifBlank { "Greška pri obradi koda." }
                }
            } catch (e: Exception) {
                soundManager.playError()
                isSuccessAlert = false
                alertMessage = "Mrežna greška: ${e.message}"
            } finally {
                // Auto-sakrij poruku posle 2.5 sekunde kako ne bi smetala sledećem skenu
                delay(3500)
                alertMessage = null
                isProcessing = false
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A))
                .verticalScroll(scrollState)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. KARTICA AKTIVNOG GRADA
            val activeCity = status?.activeCity ?: "ČEKANJE RUTE / NEMA"
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (status?.routeIsSet == true) Color(0xFFF39C12) else Color(0xFFDC2626)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "SADA SE TOVARI:",
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        activeCity,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // 2. KONTRASTNA DUGMAD: UTOVAR / ISTOVAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Dugme UTOVAR
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clickable { scanMode = "load" },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (scanMode == "load") Color(0xFF10B981) else Color(0xFF1E293B)
                    ),
                    border = if (scanMode == "load") null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "📦 UTOVAR",
                            color = if (scanMode == "load") Color.White else Color(0xFF94A3B8),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

                // Dugme ISTOVAR
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clickable { scanMode = "unload" },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (scanMode == "unload") Color(0xFFEF4444) else Color(0xFF1E293B)
                    ),
                    border = if (scanMode == "unload") null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "↩️ ISTOVAR",
                            color = if (scanMode == "unload") Color.White else Color(0xFF94A3B8),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            // 3. KOMPAKTAN OKVIR KAMERE (220.dp - ne zaklanja ostatak ekrana)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = 2.dp,
                        color = if (scanMode == "load") Color(0xFF10B981) else Color(0xFFEF4444),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .background(Color.Black)
            ) {
                ScannerView(
                    onCodeScanned = { code -> processBarcode(code) },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 4. RUČNI UNOS KODA
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = manualCode,
                    onValueChange = { manualCode = it },
                    placeholder = { Text("Očitani kod...", color = Color(0xFF64748B), fontSize = 14.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF1E293B),
                        unfocusedContainerColor = Color(0xFF1E293B),
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF334155)
                    )
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (manualCode.isNotBlank()) {
                            processBarcode(manualCode)
                            manualCode = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(52.dp)
                ) {
                    Text("OK", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 5. VELIKI ON-SCREEN STATUS (POPUPOVAN PREKO SVEGA)
        AnimatedVisibility(
            visible = alertMessage != null,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSuccessAlert) Color(0xFF065F46) else Color(0xFF991B1B)
                ),
                elevation = CardDefaults.cardElevation(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isSuccessAlert) "✅ USPEŠNO" else "❌ GREŠKA",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Kod: $lastScannedCode",
                        color = Color(0xFFE2E8F0),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = alertMessage ?: "",
                        color = Color.White,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}