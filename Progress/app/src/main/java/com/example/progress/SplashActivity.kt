package com.example.progress

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.example.progress.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val splashDuration = 3000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        startAnimations()
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToMainActivity()
        }, splashDuration)
    }

    private fun startAnimations() {
        val logoFadeIn = ObjectAnimator.ofFloat(binding.imgLogo, View.ALPHA, 0f, 1f).apply {
            duration = 1000
            interpolator = DecelerateInterpolator()
        }
        val logoScaleX = ObjectAnimator.ofFloat(binding.imgLogo, View.SCALE_X, 0.8f, 1.1f, 1f).apply {
            duration = 1200
            interpolator = OvershootInterpolator()
        }
        val logoScaleY = ObjectAnimator.ofFloat(binding.imgLogo, View.SCALE_Y, 0.8f, 1.1f, 1f).apply {
            duration = 1200
            interpolator = OvershootInterpolator()
        }
        val welcomeTranslateY = ObjectAnimator.ofFloat(binding.tvWelcome, View.TRANSLATION_Y, -100f, 0f).apply {
            duration = 800
            interpolator = DecelerateInterpolator()
        }
        val welcomeFadeIn = ObjectAnimator.ofFloat(binding.tvWelcome, View.ALPHA, 0f, 1f).apply {
            duration = 800
            interpolator = DecelerateInterpolator()
        }
        val appNameTranslateY = ObjectAnimator.ofFloat(binding.tvAppName, View.TRANSLATION_Y, 100f, 0f).apply {
            duration = 1000
            startDelay = 300
            interpolator = DecelerateInterpolator()
        }

        val appNameFadeIn = ObjectAnimator.ofFloat(binding.tvAppName, View.ALPHA, 0f, 1f).apply {
            duration = 1000
            startDelay = 300
            interpolator = DecelerateInterpolator()
        }
        val appNamePulse = ObjectAnimator.ofFloat(binding.tvAppName, View.SCALE_X, 1f, 1.05f, 1f).apply {
            duration = 1500
            startDelay = 1300
            interpolator = AccelerateDecelerateInterpolator()
        }
        val appNamePulseY = ObjectAnimator.ofFloat(binding.tvAppName, View.SCALE_Y, 1f, 1.05f, 1f).apply {
            duration = 1500
            startDelay = 1300
            interpolator = AccelerateDecelerateInterpolator()
        }
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(
            logoFadeIn,
            logoScaleX,
            logoScaleY,
            welcomeTranslateY,
            welcomeFadeIn,
            appNameTranslateY,
            appNameFadeIn,
            appNamePulse,
            appNamePulseY
        )
        animatorSet.start()
    }

    private fun navigateToMainActivity() {
        val fadeOut = ObjectAnimator.ofFloat(binding.root, View.ALPHA, 1f, 0f).apply {
            duration = 500
            interpolator = AccelerateDecelerateInterpolator()
        }

        fadeOut.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                val intent = Intent(this@SplashActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        })

        fadeOut.start()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}