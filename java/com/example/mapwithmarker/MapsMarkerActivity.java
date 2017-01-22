package com.example.mapwithmarker;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.mapwithmarker.R.id.etDateTime;
import static com.example.mapwithmarker.R.id.map;
import static com.google.android.gms.analytics.internal.zzy.et;

/**
 * An activity that displays a Google map with a marker (pin) to indicate a particular location.
 */
public class MapsMarkerActivity extends AppCompatActivity
        implements OnMapReadyCallback, OnMapLongClickListener {

    private static final int MAX_ZOOM_LEVEL = 21;
    private static final long SECOND2MS = 1000;
    private static final float DEFAULT_ZOOM_LEVEL = 5.0f;
    private GoogleMap myMap;
    private TextView mTapTextView;
    private LatLng currentLatLng = new LatLng(39.933333, 32.866667); //Ankara
    private MapLoad mapLoad = new MapLoad();
    private int currentZoomLevel;
    private int iZoom;
    private Spinner spinner;
    List<Long> prevTimeList_ms = new ArrayList<>();
    List<Double> speedList_mps = new ArrayList<>();
    List<Double> prevRadiusList_m = new ArrayList<>();
    List<Circle> circleList = new ArrayList<>();
    private boolean isFirst = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_maps);
        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(map);
        mapFragment.getMapAsync(this);
        mTapTextView = (TextView) findViewById(R.id.tap_text);
    }

    private void updateCircles() {
        for (int i = 0; i < circleList.size(); i++) {
            LatLng center = circleList.get(i).getCenter();
            LatLng latLng = new LatLng(center.latitude, center.longitude);
            circleList.get(i).remove(); //clears previous circle
            long elapsedTime_s = (SystemClock.elapsedRealtime() - prevTimeList_ms.get(i)) / SECOND2MS;
            prevTimeList_ms.set(i, SystemClock.elapsedRealtime());
            double deltaRadius_m = elapsedTime_s * speedList_mps.get(i);
            double newRadius_m = prevRadiusList_m.get(i) + deltaRadius_m;
            prevRadiusList_m.set(i, newRadius_m);
            updateCirclesList(i, latLng, newRadius_m);
        }
    }

    private void updateCirclesList(int i, LatLng latLng, double radius_m) {
        circleList.set(i, addCircleToMap(latLng, radius_m));
    }

    private Circle addCircleToMap(LatLng latLng, double radius_m) {
        Circle mapCircle = myMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(radius_m)
                .strokeColor(Color.RED));
        return mapCircle;
    }

    private void addCircleToList(LatLng latLng, double radius_m) {
        circleList.add(addCircleToMap(latLng, radius_m));
        double speed_ms = (spinner.getSelectedItemPosition() + 1) * 100;
        speedList_mps.add(speed_ms);
        prevRadiusList_m.add(0D);
        prevTimeList_ms.add(SystemClock.elapsedRealtime());
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
        myMap.setOnMapLongClickListener(this);
        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, DEFAULT_ZOOM_LEVEL));
    }

    @Override
    public void onMapLongClick(final LatLng clickedLatLng) {
        if (isOnline()) {
            View inputsView = getLayoutInflater().inflate(R.layout.inputs, null);
            EditText etLocation = (EditText) inputsView.findViewById(R.id.etLocation);
            etLocation.setEnabled(false);
            etLocation.setText(String.format("%1.6f, 0%1.6f", clickedLatLng.latitude, clickedLatLng.longitude));
            EditText eDateTime = (EditText) inputsView.findViewById(R.id.etDateTime);
            eDateTime.setEnabled(false);
            eDateTime.setText(new SimpleDateFormat("dd.MM.yyyy / HH:mm:ss").format(new Date()));
            spinner = (Spinner) inputsView.findViewById(R.id.sVictimCategory);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                    R.array.victim_category_array, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);

            AlertDialog.Builder builder = new AlertDialog.Builder(MapsMarkerActivity.this);
            builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //leave empty, will be defined below
                }
            });
            builder.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    Toast.makeText(MapsMarkerActivity.this, "Pressed Cancel", Toast.LENGTH_SHORT).show();
                    dialogInterface.dismiss();
                }
            });
            builder.setView(inputsView);
            final AlertDialog dialog = builder.create();
            dialog.show();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(MapsMarkerActivity.this, "Pressed OK", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    if (isFirst) {
                        isFirst = false;
                        Timer timer = new Timer();
                        timer.scheduleAtFixedRate(new TimerTask() { //update circle drawings every second
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        updateCircles();
                                    }
                                });
                            }
                        }, 0, SECOND2MS);
                    }
                    currentLatLng = clickedLatLng;
                    myMap.addMarker(new MarkerOptions().position(currentLatLng).title(
                            String.format("victim %d", circleList.size()+1)));
                    myMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng));
                    CameraPosition currentPos = myMap.getCameraPosition();
                    currentZoomLevel = (int) currentPos.zoom;
                    addCircleToList(clickedLatLng, 0);
                    new ZoomMap().execute();
                }
            });
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
                    (int)myMap.getCameraPosition().zoom + " / " + MAX_ZOOM_LEVEL);
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
