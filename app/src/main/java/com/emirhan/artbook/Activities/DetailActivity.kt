package com.emirhan.artbook.Activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.SpannableStringBuilder
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.emirhan.artbook.Models.ArtData
import com.emirhan.artbook.R
import com.emirhan.artbook.databinding.ActivityDetailBinding
import com.google.android.material.snackbar.Snackbar
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.Exception

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var db:SQLiteDatabase
    private var selectedBitmap: Bitmap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        registerLauncher()
        selectImage()
        save()
        var info = intent.getIntExtra("info",1)
        if (info == 1) {
            binding.imageView.setColorFilter(R.color.black)
            binding.name.setText("")
            binding.producer.setText("")
            binding.year.setText("")
            binding.save.visibility = View.VISIBLE

        } else if (info == 0) {
            try {
                db = this.openOrCreateDatabase("Arts", MODE_PRIVATE,null)
                val id = ArtData.data
                val cursor = db.rawQuery("SELECT * FROM artList WHERE name LIKE '$id'",null)
                val nameIx = cursor.getColumnIndex("name")
                val imgIx = cursor.getColumnIndex("img")
                val producerIx = cursor.getColumnIndex("producer")
                val yearIx = cursor.getColumnIndex("year")

                while (cursor.moveToNext()) {
                    var name = cursor.getString(nameIx)
                    val img = cursor.getBlob(imgIx)
                    var bitmap = BitmapFactory.decodeByteArray(img,0,img.size)
                    var producer = cursor.getString(producerIx)
                    var year = cursor.getString(yearIx)
                    binding.imageView.setImageBitmap(bitmap)
                    binding.name.text = SpannableStringBuilder(name)
                    binding.producer.text = SpannableStringBuilder(producer)
                    binding.year.text = SpannableStringBuilder(year)
                    binding.save.visibility = View.INVISIBLE
                }
                cursor.close()
            } catch (e:Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun selectImage() {
        binding.imageView.setOnClickListener {
                view ->
            if (ContextCompat.checkSelfPermission(this@DetailActivity,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Snackbar.make(view,"Permission needed for gallery",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission") {
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }.show()
                } else {
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            } else {
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        }
    }

    private fun makeSmallerImage(img:Bitmap,maxSize:Int) : Bitmap {
        var width = img.width
        var height = img.height
        var ratio = width.toFloat() / height.toFloat()
        if (ratio > 1) {
            width = maxSize
            height = width / ratio.toInt()
        } else {
            height = maxSize
            width = height * ratio.toInt()
        }
        return Bitmap.createScaledBitmap(img,width,height,true)
    }

    private fun save() {
        binding.save.setOnClickListener {
            val name = binding.name.text
            val producer = binding.producer.text
            val year = binding.year.text
            if (name.equals("") || producer.equals("") || year.equals("") || selectedBitmap != null) {
                var smallImage = makeSmallerImage(selectedBitmap!!,300)
                var outputStream = ByteArrayOutputStream()
                smallImage.compress(Bitmap.CompressFormat.PNG,50,outputStream)
                var byteArray = outputStream.toByteArray()

                try {
                    db = this.openOrCreateDatabase("Arts", MODE_PRIVATE,null)
                    db.execSQL("CREATE TABLE IF NOT EXISTS artList (id INTEGER PRIMARY KEY,img BLOB, name VARCHAR,producer VARCHAR,year VARCHAR)")
                    val sqlString = "INSERT INTO artList (name,img, producer, year) VALUES (?,?,?,?)"
                    val sqLiteStatement = db.compileStatement(sqlString)
                    sqLiteStatement.bindString(1,name.toString())
                    sqLiteStatement.bindBlob(2,byteArray)
                    sqLiteStatement.bindString(3,producer.toString())
                    sqLiteStatement.bindString(4,year.toString())
                    sqLiteStatement.execute()
                    Intent(this, MainActivity::class.java).apply {
                        startActivity(this)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            } else {
                Toast.makeText(this,"Please fill all in the blanks",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun registerLauncher() {
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result ->
            if (result.resultCode == RESULT_OK) {
                val intentForResult = result.data
                if (intentForResult != null) {
                    val imageData = intentForResult.data
                    try {
                        if (Build.VERSION.SDK_INT >= 28) {
                          val source = ImageDecoder.createSource(this@DetailActivity.contentResolver,imageData!!)
                           selectedBitmap = ImageDecoder.decodeBitmap(source)
                            binding.imageView.setImageBitmap(selectedBitmap)
                        } else {
                            selectedBitmap = MediaStore.Images.Media.getBitmap(this@DetailActivity.contentResolver,imageData!!)
                            binding.imageView.setImageBitmap(selectedBitmap)
                        }
                    }catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            result ->
            if (result) {
                // permission granted
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
             } else {
                 // permission denied
                 Toast.makeText(this@DetailActivity,"Permission needed",Toast.LENGTH_LONG).show()

            }
        }
    }
}