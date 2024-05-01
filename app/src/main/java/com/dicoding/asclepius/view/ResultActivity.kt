package com.dicoding.asclepius.view

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.dicoding.asclepius.databinding.ActivityResultBinding
import com.dicoding.asclepius.helper.ImageClassifierHelper
import com.dicoding.asclepius.helper.ImageClassifierHelper.ClassifierListener
import org.tensorflow.lite.task.vision.classifier.Classifications

class ResultActivity : AppCompatActivity(), ClassifierListener {
    private lateinit var binding: ActivityResultBinding
    private lateinit var imageClassifierHelper: ImageClassifierHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imageClassifierHelper = ImageClassifierHelper(context = this, classifierListener = this)

        val imageUri = Uri.parse(intent.getStringExtra(EXTRA_IMAGE_URI))
        imageUri?.let {
            Log.d("Image URI", "showImage: $it")
            binding.resultImage.setImageURI(it)
            imageClassifierHelper.classifyStaticImage(it) // Analisis gambar saat gambar ditampilkan
        }
    }

    override fun onResults(results: List<Classifications>?, inferenceTime: Long) {
        // Tampilkan hasil analisis di sini
        results?.let {
            val topResult = it.firstOrNull()
            val resultText = topResult?.categories?.joinToString("\n") { "${it.label}: ${it.score}" }
            binding.resultText.text = resultText
            binding.resultText.visibility = View.VISIBLE

            // Menampilkan nama kanker dan presentasinya jika hasil analisis tidak kosong

            val cancerResult = results.find { classifications ->
                classifications.categories.any { category ->
                    category.label.contains("kanker", ignoreCase = true)
                }
            }

            cancerResult?.categories?.forEach { classification ->
                if (classification.label.contains("kanker", ignoreCase = true)) {
                    binding.resultText.text = "${classification.label}: ${classification.score * 100}%"
                    binding.resultText.visibility = View.VISIBLE
                    return@forEach // Keluar dari loop karena sudah menemukan hasil analisis kanker
                }
            }
        }
    }



    override fun onError(error: String) {
        showToast(error)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val EXTRA_IMAGE_URI = "extra_image_uri"
        const val EXTRA_RESULT = "extra_result"
    }
}
