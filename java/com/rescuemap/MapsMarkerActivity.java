package com.rescuemap;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static android.Manifest.permission.GET_ACCOUNTS;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.rescuemap.R.id.map;
import static com.rescuemap.R.string.date;
import static java.lang.System.currentTimeMillis;

/**
 * An activity that displays a Google map with a marker (pin) to indicate a particular location.
 */
public class MapsMarkerActivity extends AppCompatActivity
        implements OnMapReadyCallback, OnMapLongClickListener {

    private static final int MAX_ZOOM_LEVEL = 21;
    private static final long SECOND2MS = 1000;
    private static final float DEFAULT_ZOOM_LEVEL = 5.0f;
    private static GoogleMap myMap;
    private static boolean isFirstTimeOfAccountPermissionError = true;
    private TextView mTapTextView;
    private LatLng currentLatLng = new LatLng(39.933333, 32.866667); //Ankara
    private MapLoad mapLoad = new MapLoad();
    private int currentZoomLevel;
    private int iZoom;
    private List<Date> prevTimeList_ms = new ArrayList<>();
    private List<Double> prevRadiusList_m = new ArrayList<>();
    private List<Circle> circleList = new ArrayList<>();
    private static List<Marker> markerList = new ArrayList<>();
    private boolean isFirst = true;
    private boolean isZooming = false;
    private LocationUtil locationUtil;
    private static MapsMarkerActivity mapsMarkerActivity;
    private static int markerCount = 0;

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
        locationUtil = new LocationUtil(this);
        mapsMarkerActivity = this;
        MySettings.init(this);
    }

    private void updateCircles() {
        for (int i = 0; i < circleList.size(); i++) {
            LatLng center = circleList.get(i).getCenter();
            LatLng latLng = new LatLng(center.latitude, center.longitude);
            circleList.get(i).remove(); //clears previous circle
            long elapsedTime_s = (currentTimeMillis() - prevTimeList_ms.get(i).getTime()) / SECOND2MS;
            prevTimeList_ms.set(i, new Date(currentTimeMillis()));
            double speed_mps = ((VictimProperties) markerList.get(i).getTag()).getSpeed_mps();
            double deltaRadius_m = elapsedTime_s * speed_mps;
            double newRadius_m = prevRadiusList_m.get(i) + deltaRadius_m;
            prevRadiusList_m.set(i, newRadius_m);
            updateCirclesList(i, latLng, newRadius_m);
        }
    }

    private void updateCirclesList(int i, LatLng latLng, double radius_m) {
        circleList.set(i, addCircleToMap(latLng, radius_m));
    }

    @NonNull
    private Circle addCircleToMap(LatLng latLng, double radius_m) {
        return myMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(radius_m)
                .strokeColor(Color.RED));
    }

    private void addCircleToList(LatLng latLng, Date date, double radius_m) {
        circleList.add(addCircleToMap(latLng, radius_m));
        prevRadiusList_m.add(0D);
        prevTimeList_ms.add(date);
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
        myMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                displayInputsView(marker.getTitle(), marker.getPosition(), false, marker);

            }
        });
    }

    private void displayInputsView(final String markerTitle, final LatLng clickedLatLng, boolean showButtons,
                                   Marker marker) {
        View inputsView = getLayoutInflater().inflate(R.layout.inputs, null);
        EditText etTitle = (EditText) inputsView.findViewById(R.id.etTitle);
        etTitle.setEnabled(false);
        etTitle.setText(markerTitle);

        final EditText etLat = (EditText) inputsView.findViewById(R.id.etLat);
        etLat.setEnabled(showButtons);
        etLat.setText(String.format(Locale.US, "%1.6f", clickedLatLng.latitude));

        final EditText etLon = (EditText) inputsView.findViewById(R.id.etLon);
        etLon.setEnabled(showButtons);
        etLon.setText(String.format(Locale.US, "0%1.6f", clickedLatLng.longitude));

        Date currentDate = new Date();
        final EditText etDate = (EditText) inputsView.findViewById(R.id.etDate);
        etDate.setEnabled(showButtons);
        String dateFormat = "dd.MM.yyyy";
        SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);
        etDate.setText(dateFormatter.format(currentDate));

        final EditText etTime = (EditText) inputsView.findViewById(R.id.etTime);
        etTime.setEnabled(showButtons);
        String timeFormat = "HH:mm:ss";
        SimpleDateFormat timeFormatter = new SimpleDateFormat(timeFormat);
        etTime.setText(timeFormatter.format(currentDate));

        final Spinner spinner = (Spinner) inputsView.findViewById(R.id.sVictimCategory);
        spinner.setEnabled(showButtons);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.victim_category_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        if (markerTitle.equals(getUserName())) { //user marker was clicked
            spinner.setVisibility(View.INVISIBLE);
        } else {
            if (marker != null) {//an existing victim marker was clicked --> show its info
                spinner.setSelection(((VictimProperties) marker.getTag()).getCategoryIndex());
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(MapsMarkerActivity.this);
        if (showButtons) {
            builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //leave empty, will be defined below
                }
            });
            builder.setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    dialogInterface.dismiss();
                }
            });
        }
        builder.setView(inputsView);
        final AlertDialog dialog = builder.create();
        dialog.show();
        if (showButtons) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    double lat_deg = Double.parseDouble(etLat.getText().toString());
                    double lon_deg = Double.parseDouble(etLon.getText().toString());
                    LatLng latLng = new LatLng(lat_deg, lon_deg);

                    String dateTimeStr = etDate.getText().toString() + " / " + etTime.getText().toString();
                    String dateTimeFormat = "dd.MM.yyyy / HH:mm:ss";
                    SimpleDateFormat dateTimeFormatter = new SimpleDateFormat(dateTimeFormat);
                    Date date = dateTimeFormatter.parse(dateTimeStr, new ParsePosition(0));

                    onMapMarkerOKClick(dialog, latLng, date, markerTitle, spinner.getSelectedItemPosition());
                }
            });
        }
    }

    private void onMapMarkerOKClick(AlertDialog dialog, LatLng latLng, Date date, String markerTitle,
                                    int victimCategoryIndex) {
        dialog.dismiss();
        if (isFirst) {
            //if this is the first time a marker was added, start periodic marker update job
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
        currentLatLng = latLng;
        Marker marker = myMap.addMarker(new MarkerOptions().position(currentLatLng).title(markerTitle));
        markerCount++;
        marker.setTag(new VictimProperties(markerCount, victimCategoryIndex));
        markerList.add(marker);
        myMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng));
        CameraPosition currentPos = myMap.getCameraPosition();
        currentZoomLevel = (int) currentPos.zoom;
        addCircleToList(latLng, date, 0);
        new ZoomMap().execute();
    }

    /**
     * http://stackoverflow.com/a/29689855/51358
     */
    private static String getUserName() {
        AccountManager manager = AccountManager.get(mapsMarkerActivity);
        if (ActivityCompat.checkSelfPermission(mapsMarkerActivity, GET_ACCOUNTS) != PERMISSION_GRANTED) {
            if (isFirstTimeOfAccountPermissionError) {
                showAlertDialog(mapsMarkerActivity.getString(R.string.accountPermissionError), mapsMarkerActivity);
            }
            isFirstTimeOfAccountPermissionError = false;
            return "";
        } else {
            Account[] accounts = manager.getAccountsByType("com.google");
            //http://stackoverflow.com/a/29689845/51358
            String email = accounts[0].name;
            String[] parts = email.split("@");
            String nickname = parts[0];
            return nickname;
        }
    }

    public static void addUserMarkerToMap(double lat_deg, double lon_deg) {
        myMap.addMarker(new MarkerOptions().position(new LatLng(lat_deg, lon_deg)).title(getUserName()).
                icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
    }

    @Override
    public void onMapLongClick(final LatLng clickedLatLng) {
        if (isOnline()) {
            if (isZooming) {
                showMessage(R.string.zoomingInProgress, Toast.LENGTH_SHORT);
            } else {
                final String markerTitle = String.format("victim %d", circleList.size() + 1);
                displayInputsView(markerTitle, clickedLatLng, true, null);
            }
        } else {
            showMessage(R.string.label_internet_error, Toast.LENGTH_SHORT);
        }
    }

    private void setMapZoom(int iZoom) {
        mapLoad.setIsMapLoaded(false);
        myMap.setOnMapLoadedCallback(mapLoad);
        CameraUpdate locZoom = CameraUpdateFactory.newLatLngZoom(currentLatLng, iZoom);
        myMap.animateCamera(locZoom);
    }

    /**
     * http://stackoverflow.com/a/2115770/51358
     *
     * @param msgID
     */
    public static void showAlertDialog(int msgID, Context context) {
        showAlertDialog(context.getResources().getText(msgID), context);
    }

    public static void showAlertDialog(CharSequence msg, Context context) {
        new AlertDialog.Builder(context)
                .setMessage(msg)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void showMessage(int msgID, int duration) {
        Toast.makeText(MapsMarkerActivity.this, getResources().getText(msgID), duration).show();
    }

    public void btnShowSettingsOnClick(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }


    private class ZoomMap extends AsyncTask<String, Void, Void> {
        /**
         * The system calls this to perform work in a worker thread and
         * delivers it the parameters given to AsyncTask.execute()
         */
        @Override
        protected Void doInBackground(String... urls) {
            isZooming = true;
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
            isZooming = false;
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
                    (int) myMap.getCameraPosition().zoom + " / " + MAX_ZOOM_LEVEL);
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

    protected void onStart() {
        locationUtil.connect();
        super.onStart();
    }

    protected void onStop() {
        locationUtil.disconnect();
        super.onStop();
    }

}
