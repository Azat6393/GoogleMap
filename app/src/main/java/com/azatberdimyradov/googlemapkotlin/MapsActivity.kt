package com.azatberdimyradov.googlemapkotlin

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.lang.Exception
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapLongClickListener(myListener)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        locationListener = object : LocationListener{
            override fun onLocationChanged(location: Location?) {
                if (location != null){
                    val sharedPreferences = this@MapsActivity.getSharedPreferences("com.azatberdimyradov.googlemapkotlin",Context.MODE_PRIVATE)
                    var firstTimeCheck = sharedPreferences.getBoolean("check",false)
                    if (!firstTimeCheck){
                        mMap.clear()
                        var newUserLocation = LatLng(location.latitude,location.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newUserLocation,15f))
                        sharedPreferences.edit().putBoolean("check",true).apply()
                    }
                }
            }
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String?) {}
            override fun onProviderDisabled(provider: String?) {}
        }

        if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),1)
        }else{
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,2,2f,locationListener)

            val intent = intent
            var info = intent.getStringExtra("info")
            if (info.equals("new")){
                var lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (lastLocation != null){
                    var lastLocationLatLng = LatLng(lastLocation.latitude,lastLocation.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLocationLatLng,15f))
                }
            }else {
                val address = intent.getStringExtra("address")
                val latitude = intent.getDoubleExtra("latitude",0.0)
                val longitude = intent.getDoubleExtra("longitude",0.0)
                var latLng = LatLng(latitude,longitude)
                mMap.clear()
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15f))
                mMap.addMarker(MarkerOptions().position(latLng).title(address))
            }
        }
    }

    val myListener = object : GoogleMap.OnMapLongClickListener{
        override fun onMapLongClick(p0: LatLng?) {

            var geoCode = Geocoder(this@MapsActivity, Locale.getDefault())
            var adress = ""
            if (p0 != null){
                try {
                    val adressList = geoCode.getFromLocation(p0.latitude,p0.longitude,1)
                    if (adress != null && adressList.size > 0){
                        if (adressList[0].thoroughfare != null){
                            adress += adressList[0].thoroughfare
                            if (adressList[0].subThoroughfare != null){
                                adress += adressList[0].subThoroughfare
                            }
                        }
                    }else{
                        adress = "No Adress"
                    }
                }catch (e: Exception){
                }
                mMap.addMarker(MarkerOptions().position(p0).title(adress))
                val newPlace = Place(adress,p0.latitude,p0.longitude)
                var dialog = AlertDialog.Builder(this@MapsActivity)
                dialog.setCancelable(false)
                dialog.setTitle("Are you sure?")
                dialog.setMessage(newPlace.adress)
                dialog.setPositiveButton("Yes"){dialog, which ->
                    try {
                        val database = openOrCreateDatabase("Places",Context.MODE_PRIVATE,null)
                        database.execSQL("CREATE TABLE IF NOT EXISTS places(address TEXT, lat DOUBLE, lng DOUBLE)")
                        val toCompile = "INSERT INTO places(address, lat, lng) VALUES (?,?,?)"
                        val sqLiteStatement = database.compileStatement(toCompile)
                        sqLiteStatement.bindString(1,newPlace.adress)
                        sqLiteStatement.bindDouble(2,newPlace.lat!!)
                        sqLiteStatement.bindDouble(3,newPlace.lng!!)
                        sqLiteStatement.execute()
                        val intent = Intent(this@MapsActivity,MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }catch (e: Exception){ }
                }.setNegativeButton("No"){dialog, which ->
                    mMap.clear()
                    Toast.makeText(this@MapsActivity,"Canceled",Toast.LENGTH_SHORT).show()
                }.show()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this@MapsActivity,MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1){
            if (grantResults.size > 0){
                if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,2,2f,locationListener)
                }
            }
        }
    }
}