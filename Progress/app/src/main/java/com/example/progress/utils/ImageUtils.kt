// Kotlin
package com.example.progress.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object ImageUtils {

    fun encodeToBase64(
        context: Context,
        uri: Uri,
        maxSidePx: Int = 1024,
        quality: Int = 85
    ): String {
        val optsBounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input, null, optsBounds)
        }
        val srcW = optsBounds.outWidth.coerceAtLeast(1)
        val srcH = optsBounds.outHeight.coerceAtLeast(1)
        val maxSrcSide = max(srcW, srcH)
        var inSampleSize = 1
        while ((maxSrcSide / inSampleSize) > (maxSidePx * 2)) {
            inSampleSize *= 2
        }
        val optsDecode = BitmapFactory.Options().apply { this.inSampleSize = inSampleSize }
        val decoded = context.contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input, null, optsDecode)
        } ?: throw IllegalArgumentException("Unable to decode image")

        val scaled = run {
            val dW = decoded.width
            val dH = decoded.height
            val scale = min(1f, maxSidePx.toFloat() / max(dW, dH).toFloat())
            if (scale < 1f) {
                val newW = (dW * scale).roundToInt().coerceAtLeast(1)
                val newH = (dH * scale).roundToInt().coerceAtLeast(1)
                Bitmap.createScaledBitmap(decoded, newW, newH, true)
            } else decoded
        }
        if (scaled !== decoded) decoded.recycle()

        val bytes = ByteArrayOutputStream().use { os ->
            scaled.compress(Bitmap.CompressFormat.JPEG, quality.coerceIn(50, 100), os)
            os.toByteArray()
        }
        scaled.recycle()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
}
