package com.example.robpercival.locationmapdemo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.PrintWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

public class MapsActivity extends FragmentActivity implements LocationListener, View.OnClickListener {

    private GoogleMap mMap;

    LocationManager locationManager;
    String provider;
    PolylineOptions pOptions = new PolylineOptions();

    private ImageButton buttonStart;
    private ImageButton buttonStop;
    private ImageButton buttonSave;
    private ImageButton buttonRetrieve;
    private ImageButton buttonDelete;
    private ImageButton buttonExit;

    private double longitude;
    private double latitude;

    private double startLatitude;
    private double startLongitude;
    private double stopLatitude;
    private double stopLongitude;


    private int points=0;

    boolean startTracking = false;
    boolean firstTime = true;

    private String SQLCommand;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    int mapValue=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        provider = locationManager.getBestProvider(new Criteria(), false);

        Location location = locationManager.getLastKnownLocation(provider);

        if (location != null) {

            onLocationChanged(location);

        }

        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("Start Point"));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 20));

        startLatitude = latitude;
        startLongitude = longitude;

        buttonStart = (ImageButton) findViewById(R.id.buttonStart);
        buttonStop = (ImageButton) findViewById(R.id.buttonStop);
        buttonSave = (ImageButton) findViewById(R.id.buttonSave);
        buttonRetrieve = (ImageButton) findViewById(R.id.buttonRetrieve);
        buttonDelete = (ImageButton) findViewById(R.id.buttonDelete);
        buttonExit = (ImageButton) findViewById(R.id.buttonExit);


        buttonStop.setOnClickListener(this);
        buttonStart.setOnClickListener(this);
        buttonSave.setOnClickListener(this);
        buttonRetrieve.setOnClickListener(this);
        buttonDelete.setOnClickListener(this);
        buttonExit.setOnClickListener(this);

        TextView txtResult = (TextView) findViewById(R.id.txtResult);
        txtResult.setText("Welcome to Show Me Where I Go App!");

        try{
            SQLiteDatabase DBTracks = this.openOrCreateDatabase("Tracks", MODE_PRIVATE, null);
            DBTracks.execSQL("CREATE TABLE IF NOT EXISTS LOCATION(DATE DATETIME, LAT DOUBLE, LNG DOUBLE)");
            DBTracks.execSQL("CREATE TABLE IF NOT EXISTS PLACES(DATE DATETIME, LAT DOUBLE, LNG DOUBLE)");
            DBTracks.execSQL("CREATE TABLE IF NOT EXISTS SCORE(INT POINTS)");

            Toast.makeText(this, "Database Tracks created or opened!", Toast.LENGTH_SHORT).show();
            DBTracks.close();
        }
        catch(Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        final Switch mapType=(Switch) findViewById(R.id.mapType);
        mapType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mapType.isChecked()){
                    mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                }
                else{
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                }

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();

        locationManager.requestLocationUpdates(provider, 400, 1, this);
    }


    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }


    private void setUpMap() {
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("Start Point"));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 20));
    }

    @Override
    public void onLocationChanged(Location location) {

        latitude = location.getLatitude();
        longitude = location.getLongitude();

        if(firstTime = true) {
            mMap.clear();
            firstTime = false;
        }
        mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("Start Point"));
        mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude)));

        if (startTracking==true) {
            pOptions.add(new LatLng(latitude, longitude)).width(5).color(Color.BLUE).geodesic(true);
            mMap.addPolyline(pOptions);
            try{
                SQLiteDatabase DBTracks = this.openOrCreateDatabase("Tracks", MODE_PRIVATE, null);
                Timestamp timeStamp = new Timestamp(System.currentTimeMillis());
                TimeUnit.SECONDS.sleep(1);
                SQLCommand = "INSERT INTO LOCATION VALUES('" + sdf.format(timeStamp) + "', " + Double.toString(latitude) + ", " + Double.toString(longitude) + ")";
                DBTracks.execSQL(SQLCommand);

                Location loc1 = new Location("");
                Location loc2 = new Location("");
                loc1.setLatitude(startLatitude);
                loc1.setLongitude(startLongitude);
                loc2.setLatitude(latitude);
                loc2.setLongitude(longitude);
                float distanceInMeters = loc1.distanceTo(loc2);


                Toast.makeText(this, "You have walked " + Float.toString(distanceInMeters) + "!", Toast.LENGTH_SHORT).show();

                if(distanceInMeters>=1000){
                    Toast.makeText(this, "You have walked 1km. You deserve a point!!!", Toast.LENGTH_SHORT).show();
                    points++;
                    PrintWriter writer = new PrintWriter("points.txt", "UTF-8");
                    writer.println(Integer.toString(points));
                    writer.close();

                    Toast.makeText(this, "Text file Points has been Updated!", Toast.LENGTH_SHORT).show();
                }



                Toast.makeText(this, "Record has been added to table LOCATION!", Toast.LENGTH_SHORT).show();
                DBTracks.close();
            }
            catch(Exception e){
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onClick(View v) {
        TextView txtResult = (TextView) findViewById(R.id.txtResult);
        if(v == buttonStart){
            startTracking=true;
            txtResult.setText("Tracking has started. Now you can walk!");
        }

        if(v == buttonStop){
            startTracking=false;
            mMap.stopAnimation();
            mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("Stop Point"));
            mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude)));
            stopLatitude = latitude;
            stopLongitude = longitude;

            Location loc1 = new Location("");
            Location loc2 = new Location("");
            loc1.setLatitude(startLatitude);
            loc1.setLongitude(startLongitude);
            loc2.setLatitude(stopLatitude);
            loc2.setLongitude(stopLongitude);
            float distanceInMeters = loc1.distanceTo(loc2);
            Toast.makeText(this, "Distance in meters = " + Float.toString(distanceInMeters), Toast.LENGTH_SHORT).show();

            txtResult.setText("Tracking has stopped!");
        }

        if(v == buttonSave){
            try{
                SQLiteDatabase DBTracks = this.openOrCreateDatabase("Tracks", MODE_PRIVATE, null);
                Timestamp timeStamp = new Timestamp(System.currentTimeMillis());

                SQLCommand = "INSERT INTO PLACES VALUES('" + sdf.format(timeStamp) + "', " + Double.toString(latitude) + ", " + Double.toString(longitude) + ")";
                DBTracks.execSQL(SQLCommand);

                Toast.makeText(this, "Record has been added to table PLACES!", Toast.LENGTH_SHORT).show();
                DBTracks.close();
            }
            catch(Exception e){
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        if(v == buttonDelete){
            try{
                SQLiteDatabase DBTracks = this.openOrCreateDatabase("Tracks", MODE_PRIVATE, null);
                DBTracks.execSQL("DELETE FROM LOCATION");
                DBTracks.execSQL("DELETE FROM PLACES");
                Toast.makeText(this, "All data from database has been deleted!", Toast.LENGTH_SHORT).show();
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("Start Point"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 20));
                DBTracks.close();
            }
            catch(Exception e){
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        if(v == buttonExit){
            locationManager.removeUpdates(this);
            locationManager = null;
            finishAndRemoveTask();
        }
    }
}
