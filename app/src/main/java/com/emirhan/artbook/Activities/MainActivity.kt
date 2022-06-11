package com.emirhan.artbook.Activities

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.emirhan.artbook.Adapters.ArtAdapter
import com.emirhan.artbook.Models.Art
import com.emirhan.artbook.R
import com.emirhan.artbook.databinding.ActivityMainBinding
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var db: SQLiteDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var artArray = ArrayList<Art>()


        try {
            db = this.openOrCreateDatabase("Arts", MODE_PRIVATE,null)
            var cursor = db.rawQuery("SELECT * FROM artList",null)
            var nameIx = cursor.getColumnIndex("name")
            var imgIx = cursor.getColumnIndex("img")
            var producerIx = cursor.getColumnIndex("producer")
            var yearIx = cursor.getColumnIndex("year")

            while (cursor.moveToNext()) {
                var name = cursor.getString(nameIx)
                var img = cursor.getBlob(imgIx)
                var bitmap = BitmapFactory.decodeByteArray(img,0,img.size)
                var producer = cursor.getString(producerIx)
                var year = cursor.getString(yearIx)
                var artList = Art(name,bitmap,producer,year)
                artArray.add(artList)
            }
            binding.recyclerview.adapter = ArtAdapter(artArray)
            binding.recyclerview.layoutManager = LinearLayoutManager(this)
        } catch (e:Exception) {
            e.printStackTrace()
        }



    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.add_art,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
            Intent(this, DetailActivity::class.java).apply {
                putExtra("info",1)
                startActivity(this)
            }
        return super.onOptionsItemSelected(item)
    }
}