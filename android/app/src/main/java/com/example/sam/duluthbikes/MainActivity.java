package com.example.sam.duluthbikes;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

/**
 * Main Activity Class
 * Displays a map with your current location and allows to start tracking
 */

public class MainActivity extends FragmentActivity
        implements OnMapReadyCallback,ModelViewPresenterComponents.View {

    //variable declaration area
    private Presenter mPresenter;
    public static String userName = null;
    private Location mLastLocation;
    private GoogleMap mMap;
    private ArrayList<LatLng> points;
    private PolylineOptions polylineOptions;
    private LocationData locationData;
    private boolean animate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        points = new ArrayList<>();

        polylineOptions = new PolylineOptions()
                .width(15)
                .color(Color.BLUE);

        mPresenter = new Presenter(this.getApplicationContext(),this,this);
        mPresenter.clickStart();

        animate = true;

        //Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
    }

    /**
     * Method for pausing the current ride, will start a new activity.
     * @param view
     */
    public void pauseRide(View view) {
        mPresenter.pauseRideButton();
        Intent pauseIntent = new Intent(this, PauseActivity.class);
        startActivity(pauseIntent);
    }

    public void endRide(View view) {
        mPresenter.finishRideButton();
        Intent endIntent = new Intent(this.getApplicationContext(),EndRideActivity.class);
        endIntent.putExtra("dis",LocationData.getOurInstance(this.getBaseContext()).getDistance());
        startActivity(endIntent);

        //TODO: Calls resetData() in Model that Removes polylines from map when ride is complete.
        //locationData.getOurInstance((this.getBaseContext()).resetData());
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.connectApi();
    }

     /**
     * Required by OnMapReadyCallback interface
     * Called when the map is ready to be used.
     * https://developers.google.com/android/reference/com/google/android/gms/maps/OnMapReadyCallback
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    //called by get coordinates button
    public void displayLocation(View view) {
        setLastLocation(mLastLocation);
    }

    //get location and set location methods
    public Location getLastLocation(){ return mLastLocation; }
    public void setLastLocation(Location curr) { mLastLocation = curr; }

    @Override
    public void locationChanged(Location location) {
        setLastLocation(location);
        LatLng latLng =
                new LatLng(getLastLocation().getLatitude(),getLastLocation().getLongitude());
        points.add(latLng);

        if(mMap.isMyLocationEnabled()==false){
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED){
                mMap.setMyLocationEnabled(true);
                mMap.setMaxZoomPreference(18);
            }
        }

        locationData.getOurInstance(this.getBaseContext()).addPoint(latLng,location);
        polylineOptions=locationData.getOurInstance(this.getBaseContext()).getPoints();
        LatLngBounds.Builder bounds = LocationData.getOurInstance(this.getBaseContext()).getBuilder();
        CameraUpdate cu =  CameraUpdateFactory.newLatLngBounds(bounds.build(),100);

        if(animate)
        {
            animate = false;
            mMap.animateCamera(cu);
        }
        else mMap.moveCamera(cu);
        Polyline p = mMap.addPolyline(polylineOptions);
    }



}