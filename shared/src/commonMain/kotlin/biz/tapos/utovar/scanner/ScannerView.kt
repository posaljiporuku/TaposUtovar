package biz.tapos.utovar.scanner

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun ScannerView(
    onCodeScanned: (String) -> Unit,
    modifier: Modifier = Modifier
)