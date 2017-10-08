package com.drake.crimemap;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnPolylineClickListener, GoogleMap.OnMarkerDragListener {

    private final int MY_LOCATION_PERMISSION = 1;

    private EditText editText;
    private Button myLocationButton;

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;

    private Location myLocation;
    private LatLng myLatLng;
    private Marker myMarker;
    private LatLng draggedLatLng;

    private Polyline currentRoute;

    private AddressResultReceiver resultReceiver;
    private String addressOutput;
    private String typedAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

//        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
//                .findFragmentById(R.id.map);
//        mapFragment.getMapAsync(this);

        SupportMapFragment mapFragment = new SupportMapFragment();
        mapFragment.getMapAsync(this);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, mapFragment)
                .commit();

        editText = (EditText) findViewById(R.id.destination_box);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    typedAddress = editText.getText().toString();
                    startIntentService(Constants.USE_ADDRESS_NAME);
                    handled = true;
                }
                return handled;
            }
        });
        myLocationButton = (Button) findViewById(R.id.my_location_button);
        myLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocation();
            }
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        resultReceiver = new AddressResultReceiver(null);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setOnPolylineClickListener(this);
        mMap.setOnMarkerDragListener(this);

        // Add a marker in Berkeley and move the camera
        LatLng berkeley = new LatLng(37.87, -122.27);
        myMarker = mMap.addMarker(new MarkerOptions()
                .position(berkeley)
                .title("Marker in Berkeley")
                .draggable(true));

//        Polyline polyline = mMap.addPolyline(new PolylineOptions().clickable(false)
//                .add(
//                        berkeley,
//                        new LatLng(37.8, -122.27),
//                        new LatLng(37.8, -122.2)
//                ));
//
//        Utils.stylePolyline(polyline);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(berkeley, 12));

        myLatLng = berkeley;
//        getLocation();
    }


    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_LOCATION_PERMISSION);
            return;
        }
        fusedLocationClient.getLastLocation() //call this on a background thread with IntentService
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            myLocation = location;
                            myLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                            if (!Geocoder.isPresent()) {
                                Toast.makeText(MapActivity.this, "No Geocoder Available", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            startIntentService(Constants.USE_LAT_LONG);

                            goToMyLocation(); //for testing

                        } else {
                            //location is null, maybe GPS is turned off,
                            //could show dialog asking to turn on GPS
                            Log.d("MapActivity", "location is null");
                            return;
                        }
                    }
                });
    }

    private void goToMyLocation() {
        if (myLocation == null) {
            return;
        }
        myMarker.remove();
        LatLng myLatLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
        myMarker = mMap.addMarker(new MarkerOptions()
                .position(myLatLng)
                .title("You are here")
                .draggable(true));
        myMarker.showInfoWindow();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 12));
    }



    //send the destination (and current location) to our backend
    private void submitAddress(final LatLng latLng) {
        if (currentRoute != null) {
            currentRoute.remove();
        }
        ArrayList<LatLng> waypoints = new ArrayList<>();
        waypoints.add(myLatLng);
        waypoints.add(latLng);

        currentRoute = mMap.addPolyline(Utils.constructPolyline(waypoints));
        Utils.stylePolyline(currentRoute);
        goToMyLocation();
    }





    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_LOCATION_PERMISSION: {
                if ((grantResults.length > 0) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                } else {
                    //permission denied, show dialog explaining need permission
                    AlertDialog dialog = new AlertDialog.Builder(this)
                            .setMessage("This version of the app needs permission to access your location.")
                            .setTitle("Permission needed")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    MapActivity.this.finish();
                                }
                            })
                            .show();
                }
                break;
            }
            default: {
                break;
            }
        }
    }

    @Override
    public void onPolylineClick(Polyline polyline) {}

    @Override
    public void onMarkerDragStart(Marker marker) {
        marker.hideInfoWindow();
    }

    @Override
    public void onMarkerDrag(Marker marker) {}

    @Override
    public void onMarkerDragEnd(Marker marker) {
        final LatLng draggedLatLng = marker.getPosition();
        Toast.makeText(this, "lat: " + draggedLatLng.latitude + "\nlng: " + draggedLatLng.longitude, Toast.LENGTH_SHORT).show();

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage("Is this where you want to go?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
//                        Toast.makeText(MapActivity.this, "sending coordinates... (not really)", Toast.LENGTH_SHORT).show();
                        submitAddress(draggedLatLng);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        goToMyLocation();
                    }
                })
                .show();
    }

    private void startIntentService(int lookupType) {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, resultReceiver);

        if (lookupType == Constants.USE_LAT_LONG) { //if select destination via drag-n-drop
            intent.putExtra(Constants.LOCATION_DATA_EXTRA, myLocation);
            intent.putExtra(Constants.FETCH_TYPE_EXTRA, Constants.USE_LAT_LONG);
        } else if (lookupType == Constants.USE_ADDRESS_NAME) { //if select destination by typing
            intent.putExtra(Constants.LOCATION_NAME_EXTRA, typedAddress);
            intent.putExtra(Constants.FETCH_TYPE_EXTRA, Constants.USE_ADDRESS_NAME);
        }
        startService(intent);
    }

    private class AddressResultReceiver extends android.os.ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
//            addressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            if (resultCode == Constants.SUCCESS_RESULT) {
                final Address address = resultData.getParcelable(Constants.RESULT_ADDRESS);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        submitAddress(new LatLng(address.getLatitude(), address.getLongitude()));
                    }
                });
            }
        }
    }
}
