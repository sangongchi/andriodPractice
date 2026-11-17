package com.jy.flsh.scan

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.jy.flsh.databinding.ActivityScanBinding
import org.json.JSONObject
import java.util.concurrent.Executors

/**
 * 扫码界面：CameraX 相机预览 + ML Kit 条码识别
 */
class ScanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScanBinding
    private val executor = Executors.newSingleThreadExecutor()
    private var cameraProvider: ProcessCameraProvider? = null
    private var resolved = false

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                startCamera()
            } else {
                Toast.makeText(this, "相机权限被拒绝", Toast.LENGTH_SHORT).show()
                finishWithError(2, "相机权限被拒绝")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnClose.setOnClickListener {
            finishWithError(1, "用户取消")
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        val providerFuture = ProcessCameraProvider.getInstance(this)
        providerFuture.addListener({
            try {
                cameraProvider = providerFuture.get()
                bindCameraUseCases()
            } catch (t: Throwable) {
                // 相机不可用/被占用时不能让 Activity 直接崩溃，回传明确错误给前端
                Toast.makeText(this, "相机启动失败：${t.message}", Toast.LENGTH_SHORT).show()
                finishWithError(3, "相机启动失败：${t.message ?: "未知错误"}")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindCameraUseCases() {
        val provider = cameraProvider ?: return

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(binding.previewView.surfaceProvider)
        }

        val analyzer = BarcodeAnalyzer { barcode ->
            if (!resolved) {
                resolved = true
                runOnUiThread {
                    val result = JSONObject().apply {
                        put("code", 0)
                        put("format", barcode.format.toString())
                        put("rawValue", barcode.rawValue ?: "")
                        put("timestamp", System.currentTimeMillis())
                    }
                    finishWithResult(result.toString())
                }
            }
        }

        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also { it.setAnalyzer(executor, analyzer) }

        provider.unbindAll()
        provider.bindToLifecycle(
            this,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            imageAnalysis
        )
    }

    /** 直接返回完整结果 JSON（成功时） */
    private fun finishWithResult(resultJson: String) {
        setResult(RESULT_OK, Intent().putExtra("result", resultJson))
        finish()
    }

    /** 返回错误结果（取消/权限拒绝等） */
    private fun finishWithError(code: Int, message: String) {
        val result = JSONObject().apply {
            put("code", code)
            put("message", message)
        }
        finishWithResult(result.toString())
    }

    override fun onDestroy() {
        super.onDestroy()
        executor.shutdown()
    }

    companion object {
        const val REQUEST_CODE = 1001
    }
}