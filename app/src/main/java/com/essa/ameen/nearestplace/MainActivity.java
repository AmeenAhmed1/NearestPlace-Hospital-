package com.essa.ameen.nearestplace;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {


    private static final String TAG = "MainActivity";

    private GoogleMap mGoogleMap;

    private FusedLocationProviderClient mFusedLocation;
    private Location mLastKnownLocation;
    private boolean mLocationGranted = false;
    private final int mPermissionRequestCode = 12;

    //Variables to check if the services is enabled or not
    private boolean GPS_enabled = false, NETWORK_enable = false;

    final float mMapZoom = 17.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Handle map with callback
        SupportMapFragment mMap = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapView);
        mMap.getMapAsync(this);

        mFusedLocation = LocationServices.getFusedLocationProviderClient(this);
    }


    //When the map is ready to use
    @Override
    public void onMapReady(GoogleMap googleMap) {

        Log.i(TAG, "onMapReady: ");

        mGoogleMap = googleMap;

        //check for the internet and gps
        checkForGpsAndInternet();

        //Getting permissions
        getLocationPermission();

        //update the UI
        updateInitMap();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }


    //to get map ready on the layout
    private void updateInitMap() {

        //This if map is not ready
        if (mGoogleMap == null) {
            Log.i(TAG, "updateInitMap: Null");
            return;
        }

        try{

            mGoogleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json));

            Log.i(TAG, "updateInitMap: ");

            if(mLocationGranted){
                Log.i(TAG, "updateInitMap: Location Permission is Granted");

                mGoogleMap.setMyLocationEnabled(true);
                mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);

                getCurrentLocation();
            }else{
                Log.i(TAG, "updateInitMap: Location Permission is not granted");

                mGoogleMap.setMyLocationEnabled(false);
                mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission(); //To get permission
            }

        }catch (SecurityException ex){
            Log.e("Exception: ", ex.getMessage());
        }
    }

    //getting current location of the device
    private void getCurrentLocation(){

        try{
            if(mLocationGranted && GPS_enabled && NETWORK_enable){
                Log.i(TAG, "getCurrentLocation: ");

                final Task<Location> mLocationResult = mFusedLocation.getLastLocation();
                mLocationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Log.i(TAG, "onComplete: ");

                        if(task.isSuccessful()){
                            Log.i(TAG, "onComplete: in getCurrent Location ");

                            mLastKnownLocation = task.getResult();

                            //Position Variables
                            double mLatitude = mLastKnownLocation.getLatitude();
                            double mLongitude = mLastKnownLocation.getLongitude();

                            Log.i(TAG, "onComplete: Latitude = " + mLatitude);
                            Log.i(TAG, "onComplete: Longitude = " + mLongitude);

                            //Zoom on current position
                            /*mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLatitude, mLongitude), mMapZoom));*/

                            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLatitude, mLongitude), mMapZoom));

                            //To add circle around the current place
                            Circle mCircle = mGoogleMap.addCircle(new CircleOptions()
                            .center(new LatLng(mLatitude, mLongitude))
                            .radius(5000) //Specified in meters = 5 K.M
                            .strokeColor(Color.WHITE)
                            .fillColor(Color.argb(100, 135, 206, 250)));

                            // 	rgb(135,206,250)
                            Log.i(TAG, "onComplete: circle");

                            //Get nearest place
                            new NearestHospital().execute();

                        }else{
                            Log.i(TAG, "onComplete: Else -->");
                            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            } else{
                Log.i(TAG, "getCurrentLocation: GPS Or Network not enabled");
                mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
                Toast.makeText(this, "Enable Gps and Network", Toast.LENGTH_SHORT).show();
            }

        }catch (SecurityException ex){
            Log.e("Exception: ", ex.getMessage());
        }
    }


    //Check for internet and gps
    private void checkForGpsAndInternet(){

        //Get Location Enabled or not
        LocationManager mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //Get Network is enabled or not
        ConnectivityManager mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetwork = mConnectivityManager.getActiveNetworkInfo();

        try{
            NETWORK_enable = (mNetwork != null) && (mNetwork.isConnectedOrConnecting());
            GPS_enabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);


            Log.i(TAG, "checkForGpsAndInternet: GPS = " + GPS_enabled);
            Log.i(TAG, "checkForGpsAndInternet: Net = " + NETWORK_enable);

        }catch (Exception ex){
            Log.i(TAG, "checkForGpsAndInternet: " + ex.getMessage());
        }

        //For network check
        if(!NETWORK_enable){

            //Notify user
            AlertDialog.Builder mDialog = new AlertDialog.Builder(this);
            mDialog.setMessage("Network not enabled");
            mDialog.setPositiveButton("Open settings", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //to open the GPS settings
                    Intent mIntent = new Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS);
                    startActivity(mIntent);
                }
            });
            mDialog.show();
        }

        //for GPS check
        if(!GPS_enabled){

            //Notify user
            AlertDialog.Builder mDialog = new AlertDialog.Builder(this);
            mDialog.setMessage("Gps not enabled");
            mDialog.setPositiveButton("Open settings", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //to open the GPS settings
                    Intent mIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(mIntent);
                }
            });
            mDialog.show();
        }
    }

    /*
    ** Start Permission request
     */

    //Getting location permission to access GPS
    private void getLocationPermission(){

        Log.i(TAG, "getLocationPermission: ");

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            //Permission Granted
            mLocationGranted = true;
            getCurrentLocation();
        }else{
            // Permission not granted
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, mPermissionRequestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        Log.i(TAG, "onRequestPermissionsResult: ");

        mLocationGranted = false;
        switch (requestCode){
            case mPermissionRequestCode:{
                //Here to ask again for permissions
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    mLocationGranted = true;
            }
        }
        //Update the ui with map
        updateInitMap();
    }

    /*
    **  End Permission request
     */
}