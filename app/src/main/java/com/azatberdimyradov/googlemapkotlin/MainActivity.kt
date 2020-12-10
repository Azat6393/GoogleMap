package com.azatberdimyradov.googlemapkotlin

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.ListView
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    lateinit var listView: ListView
    var placeArrays = arrayListOf<String>()
    var latitude = arrayListOf<Double>()
    var longitude = arrayListOf<Double>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listView = findViewById(R.id.listView)
        try {
            val database = openOrCreateDatabase("Places", Context.MODE_PRIVATE,null)
            var cursor = database.rawQuery("SELECT * FROM places",null,null)
            if (cursor != null){
                while (cursor.moveToNext()){
                    placeArrays.add(cursor.getString(cursor.getColumnIndex("address")))
                    latitude.add(cursor.getDouble(cursor.getColumnIndex("lat")))
                    longitude.add(cursor.getDouble(cursor.getColumnIndex("lng")))
                }
            }
        }catch (e: Exception){ }
        var arrayAdapter = ArrayAdapter(this,android.R.layout.simple_list_item_1,placeArrays)
        listView.adapter = arrayAdapter
        listView.setOnItemClickListener { parent, view, position, id ->
            val intent = Intent(this,MapsActivity::class.java)
            intent.putExtra("info","old")
            intent.putExtra("address",placeArrays[position])
            intent.putExtra("latitude",latitude[position])
            intent.putExtra("longitude",longitude[position])
            startActivity(intent)
            finish()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.add_map){
            var intent = Intent(this,MapsActivity::class.java)
            intent.putExtra("info","new")
            startActivity(intent)
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}