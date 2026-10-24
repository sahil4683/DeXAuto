package com.example.dexauto

import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.SurfaceHolder
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.example.dexauto.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val clockHandler = Handler(Looper.getMainLooper())
    private val clockFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    private val captureConsentLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            launchCaptureService(result.resultCode, result.data!!)
        } else {
            binding.startButton.isEnabled = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.startButton.setOnClickListener { requestCapturePermission() }
        binding.stopButton.setOnClickListener { stopMirroring() }

        binding.mirrorSurface.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                ScreenCaptureService.targetSurface = holder.surface
            }

            override fun surfaceChanged(
                holder: SurfaceHolder, format: Int, width: Int, height: Int
            ) {
                ScreenCaptureService.targetSurface = holder.surface
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                ScreenCaptureService.targetSurface = null
            }
        })

        startClock()
    }

    private fun requestCapturePermission() {
        binding.startButton.isEnabled = false
        val projectionManager =
            getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        // This triggers Android's mandatory system consent dialog.
        // There is no way for an app to skip or pre-approve this step.
        captureConsentLauncher.launch(projectionManager.createScreenCaptureIntent())
    }

    private fun launchCaptureService(resultCode: Int, data: Intent) {
        val serviceIntent = Intent(this, ScreenCaptureService::class.java).apply {
            putExtra(ScreenCaptureService.EXTRA_RESULT_CODE, resultCode)
            putExtra(ScreenCaptureService.EXTRA_RESULT_DATA, data)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
        binding.startButton.visibility = android.view.View.GONE
        binding.stopButton.visibility = android.view.View.VISIBLE
    }

    private fun stopMirroring() {
        stopService(Intent(this, ScreenCaptureService::class.java))
        binding.startButton.visibility = android.view.View.VISIBLE
        binding.startButton.isEnabled = true
        binding.stopButton.visibility = android.view.View.GONE
    }

    private fun startClock() {
        val tick = object : Runnable {
            override fun run() {
                binding.clockText.text = clockFormat.format(Date())
                clockHandler.postDelayed(this, 1000)
            }
        }
        clockHandler.post(tick)
    }

    override fun onDestroy() {
        clockHandler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}
