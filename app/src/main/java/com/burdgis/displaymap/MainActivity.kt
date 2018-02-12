package com.burdgis.displaymap

import android.Manifest

import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.support.v7.app.AppCompatActivity
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.view.LocationDisplay.*
import kotlinx.android.synthetic.main.activity_main.*
import android.os.Environment.getExternalStorageDirectory
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.burdgis.displaymap.R.id.mapView
import com.esri.arcgisruntime.data.TileCache
import com.esri.arcgisruntime.layers.ArcGISTiledLayer
import com.esri.arcgisruntime.mapping.Basemap
import java.io.*
import java.net.URL
import java.net.URLConnection

import java.net.HttpURLConnection
import java.net.MalformedURLException


class MainActivity : AppCompatActivity() {

    //private val ACCESS_LOCATION_REQUEST = 101
    //private val ACCESS_STORAGE_REQUEST = 123
    private val ACCESS_MULTI_REQUESTS = 101


    private val TAG = "MainActivity"

    // lateinnnit vars as suggested by ESRI
    lateinit var map: ArcGISMap


    var yeah_we_did_it: String? = "cusucoTile"

    /*
     * ****BASEMAP SWITCHING*******
     */
    // basemap switching menu items
    //lateinit var mStreetsMenuItem: MenuItem
    //lateinit var mTopoMenuItem: MenuItem
    //lateinit var mGrayMenuItem: MenuItem
    //lateinit var mOceansMenuItem: MenuItem


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
        // map = ArcGISMap("https://www.arcgis.com/home/webmap/viewer.html?webmap=9f65c604ad514295838b94d42911ae2a")

        // create a map with the BasemapType topographic
        map = ArcGISMap(Basemap.Type.IMAGERY, 15.509196, -88.233646, 10)
        // set the map to be displayed in the layout's MapView

        mapView.map = map


    }

    /**
     * ****BASEMAP SWITCHING*******
     * method to create options menu and load vars
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {

    // Inflate the menu; this adds item to the action bar if it is present
    menuInflater.inflate(R.menu.menu_main, menu)
    // Get the basemap switching menu items
    //mStreetsMenuItem = menu.getItem(0)
    //mTopoMenuItem = menu.getItem(1)
    //mGrayMenuItem = menu.getItem(2)
    //mOceansMenuItem = menu.getItem(3)
    // set the topo menu item checked by default
    //mTopoMenuItem.isChecked = true

    return super.onCreateOptionsMenu(menu)
    }




    // method to fire basemap change when menu item selected
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {




            R.id.downloadMap -> consume {
                Toast.makeText(this, "Offline map downloading", Toast.LENGTH_LONG).show()
                DownloadFile().execute()
            }

            R.id.goOffline -> consume {
                /*
                var filename = yeah_we_did_it!! + ".tpk"
                var mediaStorageDir = File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/displayMap/basemaps/")

                var tileString = "$mediaStorageDir" + "/" + "$filename"
                var cusucoTiles = TileCache(tileString)
                var tiledLayer = ArcGISTiledLayer(cusucoTiles)
                map.basemap = Basemap(tiledLayer)
                */
                val filename = yeah_we_did_it!! + ".tpk"
                val mediaStorageDir = File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/displayMap/basemaps/")
                val basemap = "$mediaStorageDir" + "/" + "$filename"
                val cusucoTiles = TileCache(basemap)
                Log.i("Tile cache object:", " Tile cache object " + cusucoTiles)
                val cusucoTiledLayer = ArcGISTiledLayer(cusucoTiles)
                //map = ArcGISMap(Basemap.Type.TOPOGRAPHIC, 35.932145, 14.380042, 10)
                Log.i("Tile layer location:", " Location of tiles " + cusucoTiledLayer)

                map.basemap = Basemap(cusucoTiledLayer)

                Toast.makeText(this, "Now in offline mode", Toast.LENGTH_LONG).show()

            }
/*
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
            */
            else -> super.onOptionsItemSelected(item)

    }



    // inline method that I DO NOT UNDERSTAND

    inline fun consume(f: () -> Unit): Boolean{
    f()
    return true
    }


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


    /*
     * This code does not work and cause a STRICT mode blocking. It should be placed in it's own
     * activity and called from there.
     * Original code take from
     * http://www.bsttutorial.com/save-image-into-sd-card-in-kotlin-android-example/
     */

    private inner class DownloadFile() : AsyncTask<String, Int, Long>() {



        override fun doInBackground(vararg aurl: String): Long? {
            var filepath: String? = null
            val strFolderName = "/displayMap/basemaps/"



            try {
                //set the download URL, a url that points to a file on the internet
                //this is the file to be downloaded
                val url = URL("https://s3-eu-west-1.amazonaws.com/burdgis.site/images/hondoApp_tiles2.tpk")
                //create the new connection
                val urlConnection = url.openConnection() as HttpURLConnection

                //set up some things on the connection
                urlConnection.requestMethod = "GET"
                urlConnection.doOutput = false
                //and connect!
                urlConnection.connect()
                //set the path where we want to save the file
                //in this case, going to save it on the root directory of the
                //sd card.
                val mediaStorageDir = File(Environment.getExternalStorageDirectory().getAbsolutePath() + strFolderName)

                // Create the storage directory if it does not exist
                if (!mediaStorageDir.exists()) {
                    mediaStorageDir.mkdirs()
                }

                //create a new file, specifying the path, and the filename
                //which we want to save the file as.
                val filename = yeah_we_did_it!! + ".tpk"   // you can download to any type of file ex:.jpeg (image) ,.txt(text file),.mp3 (audio file)
                Log.i("Local filename:", "" + filename)

                val file = File(mediaStorageDir, filename)
                Log.i("File filename:", " File HAS been created " + file)
                if (file.createNewFile()) {
                    file.createNewFile()
                    Log.i("File filename:", " File HAS been created " + filename)
                }

                //this will be used to write the downloaded data into the file we created
                val fileOutput = FileOutputStream(file)

                //this will be used in reading the data from the internet
                val inputStream = urlConnection.inputStream

                //this is the total size of the file
                val totalSize = urlConnection.contentLength

                //variable to store total downloaded bytes
                var downloadedSize = 0

                //create a buffer...
                val buffer = ByteArray(1024)

                //now, read through the input buffer and write the contents to the file
                // OJB there's a problem with this code (cannot set a variable in a while statement)
                // and the original code was:
                // while ((count /*= inputStream.read(buffer)*/) > 0) {
                var count = inputStream.read(buffer)
                while (count > 0) {
                    //add up the size so we know how much is downloaded
                    downloadedSize += count


                    //add the data in the buffer to the file in the file output stream (the file on the sd card
                    fileOutput.write(buffer, 0, count)

                    //this is where you would do something to report the prgress, like this maybe
                    Log.i("Progress:", "downloadedSize:" + downloadedSize + "totalSize:" + totalSize)
                    count = inputStream.read(buffer)
                }


                //fileOutput.write(buffer)
                //close the output stream when done
                fileOutput.close()

                if (downloadedSize == totalSize) {
                    filepath = file.path

                }

                // By using this line you can able to see saved images in the gallery view.
                sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)))

                //catch some possible errors...
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            } catch (e: IOException) {
                filepath = null
                e.printStackTrace()
            }

            Log.i("filepath:", " " + filepath!!)

            return null
        }







    }



    private fun createDir(){
        val path = File(Environment.getExternalStorageDirectory().getAbsolutePath(), "displayMap/basemaps/")




        if (!path.exists()) {
            path.mkdirs()
            Log.d("TAG", "Path created at: " + path)

        } else {
            Log.d("TAG", "Path " + path + " already exists")

        }
    }




    private fun startLocation(){
        createDir()
        val ls = mapView.locationDisplay
        ls.autoPanMode = AutoPanMode.OFF

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
        val permission05 = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)

        val ALL_PERMISSIONS = arrayOf(permission01, permission02, permission03, permission04, permission05)

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
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.INTERNET),
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