package com.example.testi123

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.example.testi123.databinding.ActivityMainBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var imageAnalyzer: ImageAnalysis
    private lateinit var imageExecutor: ExecutorService
    private lateinit var dl: DetectionLogics
    private lateinit var gestureDetector: GestureDetector
    private lateinit var previewView: PreviewView
    private lateinit var gl: GestureListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater, null, false)
        setContentView(binding.root)

        dl = DetectionLogics()
        gl = GestureListener()
        //testis
        previewView = binding.kamera
        gestureDetector = GestureDetector(this, gl)


        //gets screen size
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        var flag = false

        imageExecutor = Executors.newSingleThreadExecutor()
        imageAnalyzer = ImageAnalysis.Builder()
            .setTargetResolution(Size(256, 256))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        imageAnalyzer.setAnalyzer(imageExecutor, { imageProxy ->
            // Convert the image to a bitmap
            val bitmap = imageProxy.toBitmap()


            if (flag) {

                flag = false
                val pointList = gl.getPoints()
                if (pointList.isEmpty()){
                    //Log.d("testitesti", "big W")
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(
                            this,
                            "Measures not given properly",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    val distance = dl.doEverything(bitmap, pointList)
                    val str_distance = String.format("%.1f", distance)
                    Handler(Looper.getMainLooper()).post {
                        if (distance.isFinite()) {
                            Toast.makeText(
                                this,
                                "Distance is " + str_distance + " cm",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this,
                                "No USD 50 bill found",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    /**
                    val intent = Intent(this, ImageView::class.java)
                    intent.putExtra("bitmap", bm2)
                    startActivity(intent)
                    **/
                }
            }

            imageProxy.close()
        })

        //Log.d("as", pixels.toString())
        requestPermission()

        binding.btnMeasure.setOnClickListener {

            Log.d("nabs", "painettu")
            flag = true
        }

        previewView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }
    }

    private fun requestPermission() {
        requestCameraPermissionIfMissing { hasPermission ->
            if (hasPermission)
                startCamera()
        }
    }

    private fun requestCameraPermissionIfMissing(onResult: ((Boolean) -> Unit)) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            onResult(true)
        } else {
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                onResult(it)
            }.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        val processCameraProvider = ProcessCameraProvider.getInstance(this)
        processCameraProvider.addListener({
            val cameraProvider = processCameraProvider.get()
            val previewUseCase = Preview.Builder().build()
            previewUseCase.setSurfaceProvider(binding.kamera.surfaceProvider)
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, previewUseCase)
            cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, imageAnalyzer)

        }, ContextCompat.getMainExecutor(this))
    }


    private class GestureListener () : GestureDetector.SimpleOnGestureListener() {

        var x1 = 0.0.toFloat()
        var y1 = 0.0.toFloat()
        var x2 = 0.0.toFloat()
        var y2 = 0.0.toFloat()
        var flag = 1

        override fun onSingleTapUp(event: MotionEvent): Boolean {

            //Log.d("singletap", "onnistui")
            // Get the x and y coordinates of the tap event
            if (flag == 1) {
                x1 = event?.x ?: return false
                y1 = event?.y ?: return false
                flag = 2
            } else if (flag == 2){
                x2 = event?.x ?: return false
                y2 = event?.y ?: return false
                flag = 3
            } else {

                //resets everything
                x1 = 0.0.toFloat()
                y1 = 0.0.toFloat()
                x2 = 0.0.toFloat()
                y2 = 0.0.toFloat()
                flag = 1
            }
            /**
            Log.d("gesture", x1.toString())
            Log.d("gesture", y1.toString())
            Log.d("gesture", x2.toString())
            Log.d("gesture", y2.toString())
            **/

            return super.onSingleTapUp(event)
        }

        fun getPoints(): List<Float> {
            val pointList = mutableListOf<Float>()
            if (flag == 3) {
                pointList.add(x1)
                pointList.add(y1)
                pointList.add(x2)
                pointList.add(y2)

            }
            flag =1
            return pointList
        }

    }

}
