package com.burdgis.displaymap

import android.Manifest
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.view.LocationDisplay.*
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    //private val ACCESS_LOCATION_REQUEST = 101
    //private val ACCESS_STORAGE_REQUEST = 123
    private val ACCESS_MULTI_REQUESTS = 101


    private val TAG = "MainActivity"

    // lateinnnit vars as suggested by ESRI
    lateinit var map: ArcGISMap


    /**
     * ****BASEMAP SWITCHING*******
     *
    // basemap switching menu items
    lateinit var mStreetsMenuItem: MenuItem
    lateinit var mTopoMenuItem: MenuItem
    lateinit var mGrayMenuItem: MenuItem
    lateinit var mOceansMenuItem: MenuItem
     */

    /**
     * ****END BASEMAP SWITCHING*******
     */


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // license with license key
        ArcGISRuntimeEnvironment.setLicense("runtimelite,1000,rud8431095210,none,A3E60RFLTJJSA1GJH161")

        setUpPermissions()

        // use online map
        map = ArcGISMap("https://www.arcgis.com/home/webmap/viewer.html?webmap=9f65c604ad514295838b94d42911ae2a")

        // create a map with the BasemapType topographic
        // map = ArcGISMap(Basemap.Type.TOPOGRAPHIC, 35.932145, 14.380042, 10)
        // set the map to be displayed in the layout's MapView

        mapView.map = map


    }

    /**
     * ****BASEMAP SWITCHING*******
     * method to create options menu and load vars
    override fun onCreateOptionsMenu(menu: Menu): Boolean {

    // Inflate the menu; this adds item to the action bar if it is present
    menuInflater.inflate(R.menu.menu_main, menu)
    // Get the basemap switching menu items
    mStreetsMenuItem = menu.getItem(0)
    mTopoMenuItem = menu.getItem(1)
    mGrayMenuItem = menu.getItem(2)
    mOceansMenuItem = menu.getItem(3)
    // set the topo menu item checked by default
    mTopoMenuItem.isChecked = true

    return super.onCreateOptionsMenu(menu)
    }




     * method to fire basemap change when menu item selected
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
    R.id.World_Street_Map -> consume {
    // create a map with Streets Basemap
    map.basemap = Basemap.createStreets()
    mStreetsMenuItem.isChecked = true
    }
    R.id.World_Topo -> consume {
    // create a map with Topographic Basemap
    map.basemap = Basemap.createTopographic()
    mTopoMenuItem.isChecked = true
    }
    R.id.Gray -> consume{
    // create a map with Gray Basemap
    map.basemap = Basemap.createLightGrayCanvas()
    mGrayMenuItem.isChecked = true
    }
    R.id.Ocean_Basemap -> consume{
    // create a map with Oceans Basemap
    map.basemap = Basemap.createOceans()
    mOceansMenuItem.isChecked = true
    }
    else -> super.onOptionsItemSelected(item)
    }



     * inline method that I DO NOT UNDERSTAND

    inline fun consume(f: () -> Unit): Boolean{
    f()
    return true
    }
     */

    /**
     * ****END BASEMAP SWITCHING*******
     */

    override fun onPause() {
        super.onPause()
        mapView.pause()
    }

    override fun onResume() {
        super.onResume()
        mapView.resume()
    }
    /**
     * ***** START LOCATION *************
     */

    private fun startLocation(){

        val ls = mapView.locationDisplay
        ls.autoPanMode = AutoPanMode.RECENTER

        if (!ls.isStarted) {
            ls.startAsync()
        }

        Log.d(TAG, "Location DISPLAY AUTOPAN = " + ls.autoPanMode)
        Log.d(TAG, "Location DISPLAY IS STARTED = " + ls.isStarted)
        Log.d(TAG, "Location data source = " + ls.locationDataSource)

    }
    /**
     * ***** END LOCATION *************
     */


    /**
     * ***** START PERMISSIONS TESTING AND REQUESTS ****
     */

    // Could definitely make this better by putting the permissions themselves in an array, checking with for loop
    // requesting within the same for loop. I think. But fuck it I'm going home.
    private fun setUpPermissions() {
        val permission01 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        val permission02 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val permission03 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val permission04 = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)

        val ALL_PERMISSIONS = arrayOf(permission01, permission02, permission03, permission04)

        for (permission in ALL_PERMISSIONS) {
            if (permission != PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Permission " + permission + "NOT GRANTED")
                makeRequest()
            }

        }
        startLocation()

    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),
                ACCESS_MULTI_REQUESTS)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            ACCESS_MULTI_REQUESTS -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Permission has been denied by user")

                } else {
                    Log.i(TAG, "Permission has been granted by user")
                    startLocation()
                }
            }

        }

    }
    /**
     * ***** END PERMISSIONS TESTING AND REQUESTS ****
     */
}