package com.example.mapwithmarker;

import android.graphics.Bitmap;
import android.graphics.Color;
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
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;

import static com.example.mapwithmarker.R.id.map;
import static com.google.android.gms.maps.CameraUpdateFactory.zoomIn;

/**
 * An activity that displays a Google map with a marker (pin) to indicate a particular location.
 */
public class MapsMarkerActivity extends AppCompatActivity
        implements OnMapReadyCallback, OnMapClickListener, OnMapLongClickListener, AdapterView.OnItemSelectedListener {

    private static final int MAX_ZOOM_LEVEL = 21;
    private GoogleMap myMap;
    private TextView mTapTextView;
    private LatLng currentLatLng;
    private MapLoad mapLoad = new MapLoad();

    private void setUpMap() {
        myMap.setMapType(GoogleMap.MAP_TYPE_NONE);

        myMap.addTileOverlay(new TileOverlayOptions().tileProvider(new CustomMapTileProvider(getResources().getAssets())));

        CameraUpdate upd = CameraUpdateFactory.newLatLngZoom(new LatLng(39.933333, 32.866667), 0.5f); //Ankara
        myMap.moveCamera(upd);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_maps);
        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);
        Spinner spinner = (Spinner) findViewById(R.id.planets_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.planets_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        mTapTextView = (TextView) findViewById(R.id.tap_text);
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        if (myMap != null) {
            double radius_m = 2e3 * (pos + 1);
            // Add a circle in Sydney
            myMap.clear();
            Circle circle = myMap.addCircle(new CircleOptions()
                    .center(new LatLng(39.933333, 32.866667))
                    .radius(radius_m)
                    .strokeColor(Color.RED));
            //.fillColor(Color.BLUE));
            mTapTextView.setText("pos: " + pos);
        }
    }

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
        // Add a marker in Sydney, Australia,
        // and move the map's camera to the same location.
        LatLng ankara = new LatLng(39.933333, 32.866667);
        googleMap.addMarker(new MarkerOptions().position(ankara).title("Marker in Ankara"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ankara, 10.0f));
        myMap = googleMap;
        myMap.setOnMapClickListener(this);
        myMap.setOnMapLongClickListener(this);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        mTapTextView.setText("tapped, point=" + latLng);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        currentLatLng = latLng;
        mTapTextView.setText("long pressed, point: " + latLng);
        CameraPosition currentPos = myMap.getCameraPosition();

        new ZoomMap().execute();

        //myMap.animateCamera(CameraUpdateFactory.newCameraPosition(currentPos));
    }

    private synchronized GoogleMap getMyMap() {
        return myMap;
    }

    private class ZoomMap extends AsyncTask<String, Void, Void> {
        /**
         * The system calls this to perform work in a worker thread and
         * delivers it the parameters given to AsyncTask.execute()
         */
        protected Void doInBackground(String... urls) {
            for (int iZoom = 1; iZoom < MAX_ZOOM_LEVEL; iZoom++) {
                mapLoad.setIsMapLoaded(false);
                getMyMap().setOnMapLoadedCallback(mapLoad);
                CameraUpdate locZoom = CameraUpdateFactory.newLatLngZoom(currentLatLng, iZoom);
                myMap.animateCamera(locZoom);
                while (!mapLoad.isMapLoaded()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                    }
                }
            }
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
            mTapTextView.setText("Map finished loading zoom level: " + myMap.getCameraPosition().zoom);
            isMapLoaded = true;
        }
    }

}
