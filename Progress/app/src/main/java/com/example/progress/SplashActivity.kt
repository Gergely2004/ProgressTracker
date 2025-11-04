package com.example.progress

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.progress.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Log.d(TAG, "onCreate: SplashActivity created.")

        val binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.goToMain.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("textView", binding.editTextText.text.toString())
            startActivity(intent)
            finish()
        }

    }
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: SplashActivity resumed.")
    }
    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: SplashActivity paused.")
    }
    override fun onRestart() {
        super.onRestart()
        Log.d(TAG, "onRestart: SplashActivity restarted.")
    }
    override fun onDestroy() {
        super.onDestroy()
    }
}