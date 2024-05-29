package com.example.ble

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.example.ble.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private val splashScreenDuration = 3000L
    private val handler = Handler(Looper.getMainLooper())
    private val splashRunnable = Runnable {
        startActivity(Intent(this, ScanActivity::class.java))
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        handler.postDelayed(splashRunnable, splashScreenDuration)

    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(splashRunnable)
    }
}
