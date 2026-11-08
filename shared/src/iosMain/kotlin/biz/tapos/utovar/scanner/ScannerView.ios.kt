package biz.tapos.utovar.scanner

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import platform.AVFoundation.*
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970
import platform.QuartzCore.CATransaction
import platform.UIKit.UIColor
import platform.UIKit.UIView
import platform.darwin.NSObject
import platform.darwin.dispatch_get_main_queue

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun ScannerView(
    onCodeScanned: (String) -> Unit,
    modifier: Modifier
) {
    var lastCode by remember { mutableStateOf("") }
    var lastTime by remember { mutableStateOf(0.0) }

    val captureSession = remember { AVCaptureSession() }
    val previewLayer = remember {
        AVCaptureVideoPreviewLayer(session = captureSession).apply {
            videoGravity = AVLayerVideoGravityResizeAspectFill
        }
    }

    val delegate = remember {
        object : NSObject(), AVCaptureMetadataOutputObjectsDelegateProtocol {
            override fun captureOutput(
                output: AVCaptureOutput,
                didOutputMetadataObjects: List<*>,
                fromConnection: AVCaptureConnection
            ) {
                val metadataObj = didOutputMetadataObjects.firstOrNull() as? AVMetadataMachineReadableCodeObject
                val code = metadataObj?.stringValue ?: return

                val now = NSDate().timeIntervalSince1970 * 1000.0
                if (code != lastCode || (now - lastTime) > 3500) {
                    lastCode = code
                    lastTime = now
                    onCodeScanned(code)
                }
            }
        }
    }

    DisposableEffect(Unit) {
        val device = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
        if (device != null) {
            val input = AVCaptureDeviceInput.deviceInputWithDevice(device, null)
            if (input != null && captureSession.canAddInput(input)) {
                captureSession.addInput(input)
            }

            val output = AVCaptureMetadataOutput()
            if (captureSession.canAddOutput(output)) {
                captureSession.addOutput(output)
                output.setMetadataObjectsDelegate(delegate, dispatch_get_main_queue())
                output.metadataObjectTypes = listOf(
                    AVMetadataObjectTypeQRCode,
                    AVMetadataObjectTypeEAN13Code,
                    AVMetadataObjectTypeEAN8Code,
                    AVMetadataObjectTypeCode128Code
                )
            }
            captureSession.startRunning()
        }

        onDispose {
            captureSession.stopRunning()
        }
    }

    UIKitView(
        factory = {
            object : UIView(frame = CGRectZero.readValue()) {
                override fun layoutSubviews() {
                    super.layoutSubviews()
                    CATransaction.begin()
                    CATransaction.setDisableActions(true)
                    previewLayer.frame = bounds
                    CATransaction.commit()
                }
            }.apply {
                backgroundColor = UIColor.blackColor
                layer.addSublayer(previewLayer)
            }
        },
        modifier = modifier
    )
}
