package com.example.testi123

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.testi123.databinding.ActivityImageViewBinding
import com.example.testi123.databinding.ActivityMainBinding

class ImageView : AppCompatActivity() {
    private lateinit var binding: ActivityImageViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageViewBinding.inflate(layoutInflater, null, false)
        setContentView(binding.root)

        val bitmap = intent.getParcelableExtra<Bitmap>("bitmap")
        binding.imgView.setImageBitmap(bitmap)
    }
}