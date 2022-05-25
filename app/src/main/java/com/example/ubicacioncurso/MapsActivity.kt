package com.example.ubicacioncurso

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.ubicacioncurso.databinding.ActivityMapsBinding
import com.example.ubicacioncurso.db.LocationDatabase
import com.example.ubicacioncurso.entity.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private val SOLICITA_GPS:Int = 1
    private lateinit var mLocationClient: FusedLocationProviderClient
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var mLocationCallback: LocationCallback
    //instancia de BD
    private lateinit var locationDatabase: LocationDatabase


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Inicializacion del database
        locationDatabase = LocationDatabase.getInstance(this)

        mLocationCallback = object : LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult) {

                if(mMap==null && locationResult.equals(null)){
                    return
                }
                //Dibujar en el mapa los puntos
                for(location in locationResult.locations){
                    val currentLocation = LatLng( location.latitude, location.longitude)
                    mMap.addMarker(MarkerOptions().position(currentLocation).title("Marker"))
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation))
                    locationDatabase.locationDao
                        .insert(Location(null,location.latitude,location.longitude))
                }

            }
        }
        mLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mLocationRequest = LocationRequest()
        mLocationRequest.interval =1000
        mLocationRequest.fastestInterval =500
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest)




        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        this.getLocation()
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        var locations = locationDatabase.locationDao.query()

        for(location in locations){
            val currentLocation = LatLng( location.latitude, location.longitude)
            mMap.addMarker(MarkerOptions().position(currentLocation).title("Marker"))
            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation))
        }

    }

    fun getLocation(){
        //Tengo permisos?
        if(ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
                {
                //No tengo permisos
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    SOLICITA_GPS
                )
        }else {
            mLocationClient.requestLocationUpdates(
                mLocationRequest,
                mLocationCallback, null
            )
        }
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            SOLICITA_GPS -> {
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //El usuario dio permisos
                    mLocationClient.requestLocationUpdates(
                        mLocationRequest,
                        mLocationCallback, null
                    )

                }else {//El usuario no dio permiso
                    System.exit(1)
                }
            }
        }
    }
}