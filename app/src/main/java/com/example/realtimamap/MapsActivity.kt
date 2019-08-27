package com.example.realtimamap

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import com.google.maps.android.SphericalUtil


open class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    private lateinit var mMap: GoogleMap
    private var locationManager: LocationManager? = null
    lateinit var list: List<LatLng>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        checkPermissions()
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager?

        if (!checkPermissions()) {
            requestPermissions()
        } else {
            try {
                locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, locationListener);
            } catch (ex: SecurityException) {
                Log.d("myTag", "Security Exception, no location available\n $ex")
            }
        }
    }

    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            var here = LatLng(location.latitude, location.longitude)
            mMap.addMarker(
                MarkerOptions().position(here)
                    .title("${here.latitude},${here.longitude}")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
            )
            val line = findNearestPoint(here, list[0], list[1])
            Log.d("speed", "${location.speed * 18 / 5}km/h")
            mMap.addMarker(
                MarkerOptions().position(line)
                    .title("${line.latitude},${here.longitude}")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )
            val l = PolyUtil.distanceToLine(here, list[0], list[1])

            Log.d("lognti", "${location.latitude}, ${location.longitude} distance:$l")
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    private fun findNearestPoint(p: LatLng, start: LatLng, end: LatLng): LatLng {
        if (start == end) {
            return start
        }
        val s0lat = Math.toRadians(p.latitude)
        val s0lng = Math.toRadians(p.longitude)
        val s1lat = Math.toRadians(start.latitude)
        val s1lng = Math.toRadians(start.longitude)
        val s2lat = Math.toRadians(end.latitude)
        val s2lng = Math.toRadians(end.longitude)

        val s2s1lat = s2lat - s1lat
        val s2s1lng = s2lng - s1lng
        val u = ((s0lat - s1lat) * s2s1lat + (s0lng - s1lng) * s2s1lng) / (s2s1lat * s2s1lat + s2s1lng * s2s1lng)
        if (u <= 0) {
            return start
        }
        return if (u >= 1) {
            end
        } else LatLng(
            start.latitude + u * (end.latitude - start.latitude),
            start.longitude + u * (end.longitude - start.longitude)
        )
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setMinZoomPreference(16.0f)
        mMap.setMaxZoomPreference(20.0f)
        val here = LatLng(4.667426, -74.056624)
        mMap.addMarker(MarkerOptions().position(here).title("Mi Aguila"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(here))
        val distance = SphericalUtil.computeDistanceBetween(here, LatLng(4.672655, -74.056624))
        list = listOf(LatLng(4.667426, -74.056624), LatLng(4.672667, -74.054027))
        Toast.makeText(this, "distance: $distance", Toast.LENGTH_SHORT).show()
        val options = PolylineOptions().width(5f).color(Color.BLUE).geodesic(true)
        for (z in 0 until list.size) {
            val point = list.get(z)
            options.add(point)
        }
        val line = mMap.addPolyline(options)
        mMap.setInfoWindowAdapter(MyInfoWindowAdapter(applicationContext))

    }

    private fun checkPermissions(): Boolean {
        val permissionState = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        return permissionState == PackageManager.PERMISSION_GRANTED
    }

    private fun startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
            REQUEST_PERMISSIONS_REQUEST_CODE
        )
    }

    private fun showSnackbar(mainTextStringId: Int, actionStringId: Int, listener: View.OnClickListener) {
        Toast.makeText(this, getString(mainTextStringId), Toast.LENGTH_LONG).show()
    }

    private fun requestPermissions() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.")

            showSnackbar(R.string.permission_rationale, android.R.string.ok,
                View.OnClickListener {
                    // Request permission
                    startLocationPermissionRequest()
                })

        } else {
            Log.i(TAG, "Requesting permission")
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            startLocationPermissionRequest()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            when {
                grantResults.isEmpty() -> // If user interaction was interrupted, the permission request is cancelled and you
                    // receive empty arrays.
                    Log.i(TAG, "User interaction was cancelled.")
                grantResults[0] == PackageManager.PERMISSION_GRANTED -> // Permission granted.
                    Toast.makeText(this, "Permiso concedido", Toast.LENGTH_SHORT).show()
                else ->
                    showSnackbar(R.string.permission_denied_explanation, R.string.settings,
                        View.OnClickListener {
                            // Build intent that displays the App settings screen.
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri = Uri.fromParts(
                                "package",
                                BuildConfig.APPLICATION_ID, null
                            )
                            intent.data = uri
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        })
            }
        }
    }
}

