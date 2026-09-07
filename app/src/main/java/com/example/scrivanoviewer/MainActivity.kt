package com.example.scrivanoviewer

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.InputStream
import java.util.zip.ZipInputStream

class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { openScrivanoFile(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Basic full-screen container programmatically initialized
        imageView = ImageView(this).apply {
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        setContentView(imageView)

        // Launch file picker immediately upon startup
        filePickerLauncher.launch("*/*")
    }

    private fun openScrivanoFile(uri: Uri) {
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val zipInputStream = ZipInputStream(inputStream)

            var entry = zipInputStream.nextEntry
            var thumbnailFound = false

            // Iterate ZIP entries inside .scrivano archive
            while (entry != null) {
                if (entry.name == "thumbnail.png") {
                    val bitmap = BitmapFactory.decodeStream(zipInputStream)
                    imageView.setImageBitmap(bitmap)
                    thumbnailFound = true
                    break
                }
                zipInputStream.closeEntry()
                entry = zipInputStream.nextEntry
            }

            zipInputStream.close()

            if (!thumbnailFound) {
                Toast.makeText(this, "No thumbnail preview found in .scrivano file", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error parsing file: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}