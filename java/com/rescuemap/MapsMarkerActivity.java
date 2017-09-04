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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import static java.lang.System.currentTimeMillis;

/**
 * An activity that displays a Google map with a marker (pin) to indicate a particular location.
 */
public class MapsMarkerActivity extends AppCompatActivity
        implements OnMapReadyCallback, OnMapLongClickListener {

    private AlertDialog alertDialog = null;
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
    private static List<MyMarker> myMarkerList = new ArrayList<>();
    private boolean isFirstTimeAMarkerIsAddedToMap = true;
    private boolean isZooming = false;
    private LocationUtil locationUtil;
    private static MapsMarkerActivity mapsMarkerActivity;
    private static int markerCount = 0;

    private EditText etTitle;
    private EditText etLat;
    private EditText etLon;
    private EditText etDate;
    private EditText etTime;
    private Button btnDelete;
    private Marker selectedMarker;

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

    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.settingsbutton) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateMarkers() {
        for (MyMarker myMarker: myMarkerList) {
            LatLng center = myMarker.getCircle().getCenter();
            myMarker.getCircle().remove(); //deletes circle from map
            LatLng latLng = new LatLng(center.latitude, center.longitude);
            long elapsedTime_s = (currentTimeMillis() - myMarker.getPrevTime_ms().getTime()) / SECOND2MS;
            myMarker.setPrevTime_ms(new Date(currentTimeMillis()));
            double speed_mps = ((VictimProperties) myMarker.getTag()).getSpeed_mps();
            double deltaRadius_m = elapsedTime_s * speed_mps;
            double newRadius_m = myMarker.getPrevRadius_m() + deltaRadius_m;
            myMarker.setPrevRadius_m(newRadius_m);
            myMarker.setGoogleMapCircle(addCircleToMap(latLng, newRadius_m));
        }
    }

    @NonNull
    private Circle addCircleToMap(LatLng latLng, double radius_m) {
        return addCircleToMap(latLng, radius_m, Color.RED);
    }

    private Circle addCircleToMap(LatLng latLng, double radius_m, int color) {
        return myMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(radius_m)
                .strokeColor(color));
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

    private void displayInputsView(final String markerTitle, final LatLng clickedLatLng, boolean newlyCreated,
                                   Marker marker) {
        selectedMarker = marker;
        View inputsView = getLayoutInflater().inflate(R.layout.inputs, null);
        final Spinner spinner = (Spinner) inputsView.findViewById(R.id.sVictimCategory);
        spinner.setEnabled(newlyCreated);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.victim_category_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        boolean isUserMarkerClicked = markerTitle.equals(getUserName());
        VictimProperties victim;
        if (isUserMarkerClicked) { //user marker was clicked
            spinner.setVisibility(View.INVISIBLE);
        } else {
            if (marker != null) {//an existing victim marker was clicked --> show its info
                victim = (VictimProperties) marker.getTag();
                spinner.setSelection(victim.getCategoryIndex());
                showVictimData(inputsView, isUserMarkerClicked, newlyCreated, victim.getName(), victim.getLatLng(), victim.getDate());
            } else {
                showVictimData(inputsView, isUserMarkerClicked, newlyCreated, markerTitle, clickedLatLng, new Date());
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(MapsMarkerActivity.this);
        if (newlyCreated) {
            btnDelete.setVisibility(View.INVISIBLE);
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
        } else {
            btnDelete.setVisibility(View.VISIBLE);
        }
        builder.setView(inputsView);
        alertDialog = builder.create();
        alertDialog.show();
        if (newlyCreated) {
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    double lat_deg = Double.parseDouble(etLat.getText().toString());
                    double lon_deg = Double.parseDouble(etLon.getText().toString());
                    LatLng latLng = new LatLng(lat_deg, lon_deg);

                    String dateTimeStr = etDate.getText().toString() + " / " + etTime.getText().toString();
                    String dateTimeFormat = "dd.MM.yyyy / HH:mm:ss";
                    SimpleDateFormat dateTimeFormatter = new SimpleDateFormat(dateTimeFormat);
                    Date date = dateTimeFormatter.parse(dateTimeStr, new ParsePosition(0));

                    onMapMarkerOKClick(alertDialog, latLng, date, String.valueOf(etTitle.getText()), spinner.getSelectedItemPosition());
                }
            });
        }
    }

    private void showVictimData(View inputsView, boolean isUserMarkerClicked, boolean showButtons, String name, LatLng latLng, Date date) {
        etTitle = (EditText) inputsView.findViewById(R.id.etTitle);
        etTitle.setEnabled(isUserMarkerClicked || !showButtons ? false:true);
        etTitle.setText(name);

        etLat = (EditText) inputsView.findViewById(R.id.etLat);
        etLat.setEnabled(showButtons);
        etLat.setText(String.format(Locale.US, "%1.6f", latLng.latitude));

        etLon = (EditText) inputsView.findViewById(R.id.etLon);
        etLon.setEnabled(showButtons);
        etLon.setText(String.format(Locale.US, "0%1.6f", latLng.longitude));

        etDate = (EditText) inputsView.findViewById(R.id.etDate);
        etDate.setEnabled(showButtons);
        String dateFormat = "dd.MM.yyyy";
        SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);
        etDate.setText(dateFormatter.format(date));

        etTime = (EditText) inputsView.findViewById(R.id.etTime);
        etTime.setEnabled(showButtons);
        String timeFormat = "HH:mm:ss";
        SimpleDateFormat timeFormatter = new SimpleDateFormat(timeFormat);
        etTime.setText(timeFormatter.format(date));

        btnDelete = (Button)inputsView.findViewById(R.id.btDelete);
    }

    private void onMapMarkerOKClick(AlertDialog dialog, LatLng latLng, Date date, String markerTitle,
                                    int victimCategoryIndex) {
        dialog.dismiss();
        if (isFirstTimeAMarkerIsAddedToMap) {
            //if this is the first time a marker was added, start periodic marker update job
            isFirstTimeAMarkerIsAddedToMap = false;
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() { //update circle drawings every second
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateMarkers();
                        }
                    });
                }
            }, 0, SECOND2MS);
        }
        currentLatLng = latLng;
        Marker marker = myMap.addMarker(new MarkerOptions().position(currentLatLng).title(markerTitle));
        markerCount++;
        VictimProperties victimProperties = new VictimProperties(markerTitle, markerCount, victimCategoryIndex, latLng, date);
        marker.setTag(victimProperties);
        myMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng));
        CameraPosition currentPos = myMap.getCameraPosition();
        currentZoomLevel = (int) currentPos.zoom;

        Circle circle = addCircleToMap(latLng, 0);
        Circle constCircle = addCircleToMap(latLng, victimProperties.getConstRadius_m(), Color.BLACK);
        MyMarker myMarker = new MyMarker(marker, circle, constCircle, date);

        myMarkerList.add(myMarker);
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
                String markerTitle = String.format("victim %d", myMarkerList.size() + 1);
                for (MyMarker myMarker: myMarkerList) {
                    if (myMarker.getMarker().getTitle().equals(markerTitle)) {
                        markerTitle = markerTitle + "0"; //prevent markers with same title. Can happen when a marker was deleted and a new one was added.
                    }
                }
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

    public void btnDeleteClick(View view) {
        for (MyMarker myMarker: myMarkerList) {
            if (myMarker.getMarker().getPosition().equals(selectedMarker.getPosition())) {
                myMarkerList.remove(myMarkerList.indexOf(myMarker));
                myMarker.remove();
                alertDialog.dismiss();
                break;
            }
        }
    }

}
