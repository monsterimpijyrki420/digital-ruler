/**

package com.example.testi123

import android.graphics.ImageFormat
import android.media.Image
import android.util.Log
import androidx.camera.core.ImageProxy
import org.opencv.android.OpenCVLoader
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

@androidx.camera.core.ExperimentalGetImage
class DetectionStuff {

    val note_length = 15.6
    val note_height = 6.63

    fun testi(imageProxy: ImageProxy?) {
        if (imageProxy != null) {
            try {
                imageProxy.image?.let {
                    // ImageProxy uses an ImageReader under the hood:
                    // https://developer.android.com/reference/androidx/camera/core/ImageProxy.html
                    // That has a default format of YUV_420_888 if not changed that's the default
                    // Android camera format.
                    // https://developer.android.com/reference/android/graphics/ImageFormat.html#YUV_420_888
                    // https://developer.android.com/reference/android/media/ImageReader.html

                    // Sanity check
                    if (it.format == ImageFormat.YUV_420_888
                        && it.planes.size == 3
                    ) {
                        //val rgbaMat = it.toMat()
                        if (OpenCVLoader.initDebug()) {
                            Log.d("asd", "onnistui")
                        }
                        Log.d("asd", "mit vit")
                    } else {
                        Log.d("asd", "ei onnistunu")
                    }
                }
            } catch (ise: IllegalStateException) {
                ise.printStackTrace()
            }
        }
    }

    // Ported from opencv private class JavaCamera2Frame
    fun Image.yuvToRgba(): Mat {
        val rgbaMat = Mat()

        if (format == ImageFormat.YUV_420_888
            && planes.size == 3) {

            val chromaPixelStride = planes[1].pixelStride

            if (chromaPixelStride == 2) { // Chroma channels are interleaved
                assert(planes[0].pixelStride == 1)
                assert(planes[2].pixelStride == 2)
                val yPlane = planes[0].buffer
                val uvPlane1 = planes[1].buffer
                val uvPlane2 = planes[2].buffer
                val yMat = Mat(height, width, CvType.CV_8UC1, yPlane)
                val uvMat1 = Mat(height / 2, width / 2, CvType.CV_8UC2, uvPlane1)
                val uvMat2 = Mat(height / 2, width / 2, CvType.CV_8UC2, uvPlane2)
                val addrDiff = uvMat2.dataAddr() - uvMat1.dataAddr()
                if (addrDiff > 0) {
                    assert(addrDiff == 1L)
                    Imgproc.cvtColorTwoPlane(yMat, uvMat1, rgbaMat, Imgproc.COLOR_YUV2RGBA_NV12)
                } else {
                    assert(addrDiff == -1L)
                    Imgproc.cvtColorTwoPlane(yMat, uvMat2, rgbaMat, Imgproc.COLOR_YUV2RGBA_NV21)
                }
            } else { // Chroma channels are not interleaved
                val yuvBytes = ByteArray(width * (height + height / 2))
                val yPlane = planes[0].buffer
                val uPlane = planes[1].buffer
                val vPlane = planes[2].buffer

                yPlane.get(yuvBytes, 0, width * height)

                val chromaRowStride = planes[1].rowStride
                val chromaRowPadding = chromaRowStride - width / 2

                var offset = width * height
                if (chromaRowPadding == 0) {
                    // When the row stride of the chroma channels equals their width, we can copy
                    // the entire channels in one go
                    uPlane.get(yuvBytes, offset, width * height / 4)
                    offset += width * height / 4
                    vPlane.get(yuvBytes, offset, width * height / 4)
                } else {
                    // When not equal, we need to copy the channels row by row
                    for (i in 0 until height / 2) {
                        uPlane.get(yuvBytes, offset, width / 2)
                        offset += width / 2
                        if (i < height / 2 - 1) {
                            uPlane.position(uPlane.position() + chromaRowPadding)
                        }
                    }
                    for (i in 0 until height / 2) {
                        vPlane.get(yuvBytes, offset, width / 2)
                        offset += width / 2
                        if (i < height / 2 - 1) {
                            vPlane.position(vPlane.position() + chromaRowPadding)
                        }
                    }
                }

                val yuvMat = Mat(height + height / 2, width, CvType.CV_8UC1)
                yuvMat.put(0, 0, yuvBytes)
                Imgproc.cvtColor(yuvMat, rgbaMat, Imgproc.COLOR_YUV2RGBA_I420, 4)
            }
        }

        return rgbaMat
    }

    private fun Image.toMat(): Mat {
        // Get the image data as a byte buffer
        val buffer = planes[0].buffer
        val imageData = ByteArray(buffer.remaining())
        buffer.get(imageData)

        // Create a Mat from the byte data
        val mat = Mat(height, width, CvType.CV_8UC3)
        mat.put(0, 0, imageData)

        // Convert the color format from YUV to RGB
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_YUV2RGB_NV21)

        // Rotate the image 90 degrees clockwise
        val rotated = Mat()
        Core.rotate(mat, rotated, Core.ROTATE_90_CLOCKWISE)

        return rotated
    }


}
 **/