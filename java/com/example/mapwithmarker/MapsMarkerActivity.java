package com.example.mapwithmarker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import static com.example.mapwithmarker.R.id.map;

/**
 * An activity that displays a Google map with a marker (pin) to indicate a particular location.
 */
public class MapsMarkerActivity extends AppCompatActivity
        implements OnMapReadyCallback, OnMapClickListener, OnMapLongClickListener, AdapterView.OnItemSelectedListener {

    private static final int MAX_ZOOM_LEVEL = 21;
    private GoogleMap myMap;
    private TextView mTapTextView;
    private LatLng currentLatLng = new LatLng(39.933333, 32.866667); //Ankara
    private MapLoad mapLoad = new MapLoad();
    private int currentZoomLevel;
    private int iZoom;
    private double currentRadius_m;
    private Spinner spinner;
    Circle mapCircle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_maps);
        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(map);
        mapFragment.getMapAsync(this);
        spinner = (Spinner) findViewById(R.id.planets_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.victim_category_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        mTapTextView = (TextView) findViewById(R.id.tap_text);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        if (myMap != null && currentLatLng != null) {
            removePrevCircleAtCurrentLatLng();
            myMap.addMarker(new MarkerOptions().position(currentLatLng).title("initial victim location"));
            currentRadius_m = (pos + 1) * 1e4;
            addCircle(currentRadius_m);
            mTapTextView.setText("pos: " + pos);
        }
    }

    private void addCircle(double radius_m) {
        mapCircle = myMap.addCircle(new CircleOptions()
                .center(currentLatLng)
                .radius(radius_m)
                .strokeColor(Color.RED));
    }

    /**
     * http://stackoverflow.com/a/15481266/51358
     */
    private void removePrevCircleAtCurrentLatLng() {
        if (mapCircle != null) {
            mapCircle.remove();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    /**
     * Manipulates the map when it's available.
     * The API invokes this callback when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user receives a prompt to install
     * Play services inside the SupportMapFragment. The API invokes this method after the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        myMap = googleMap;
        myMap.setOnMapClickListener(this);
        myMap.setOnMapLongClickListener(this);
        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 5.0f));
        addCircle((spinner.getSelectedItemPosition() + 1) * 1e4);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        mTapTextView.setText("tapped, point=" + latLng);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        if (isOnline()) {
            mTapTextView.setText("long pressed, point: " + latLng);
            currentLatLng = latLng;
            myMap.addMarker(new MarkerOptions().position(currentLatLng).title("initial victim location"));
            addCircle(currentRadius_m);
            myMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng));
            CameraPosition currentPos = myMap.getCameraPosition();
            currentZoomLevel = (int) currentPos.zoom;
            new ZoomMap().execute();
        } else {
            mTapTextView.setText(R.string.label_internet_error);
        }
    }

    private void setMapZoom(int iZoom) {
        mapLoad.setIsMapLoaded(false);
        myMap.setOnMapLoadedCallback(mapLoad);
        CameraUpdate locZoom = CameraUpdateFactory.newLatLngZoom(currentLatLng, iZoom);
        myMap.animateCamera(locZoom);
    }

    private class ZoomMap extends AsyncTask<String, Void, Void> {
        /**
         * The system calls this to perform work in a worker thread and
         * delivers it the parameters given to AsyncTask.execute()
         */
        @Override
        protected Void doInBackground(String... urls) {
            for (iZoom = currentZoomLevel; iZoom <= MAX_ZOOM_LEVEL; iZoom++) {
                mapLoad.setIsMapLoaded(false);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setMapZoom(iZoom);
                    }
                });
                while (!mapLoad.isMapLoaded()) { //wait until map loading at iZoom level is finished.
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                    }
                }
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setMapZoom(currentZoomLevel);
                }
            });
            return null;
        }

        /**
         * The system calls this to perform work in the UI thread and delivers
         * the result from doInBackground()
         */
        protected void onPostExecute(Bitmap result) {
        }
    }

    private class MapLoad implements OnMapLoadedCallback {

        private boolean isMapLoaded = false;

        public boolean isMapLoaded() {
            return isMapLoaded;
        }

        public void setIsMapLoaded(boolean isMapLoaded) {
            this.isMapLoaded = isMapLoaded;
        }

        @Override
        public void onMapLoaded() {
            mTapTextView.setText(getResources().getText(R.string.label_finished_zoom) + ": " +
                    myMap.getCameraPosition().zoom);
            isMapLoaded = true;
        }
    }

    /**
     * http://stackoverflow.com/a/4009133/51358
     */
    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

}
