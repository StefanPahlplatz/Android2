package com.android.eu.proximitymap.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.eu.proximitymap.R;
import com.android.eu.proximitymap.models.SimpleLocation;
import com.android.eu.proximitymap.models.User;
import com.android.eu.proximitymap.models.UserLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

/**
 * Map activity, default activity after logging in.
 * <p>
 * Navigation bar documentation: https://github.com/ittianyu/BottomNavigationViewEx
 */
public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        ChildEventListener,
        BottomNavigationView.OnNavigationItemSelectedListener {

    private static final int NAV_HEIGHT = 190;
    private static final int INITIAL_ZOOM = 16;
    private static final int MARKER_ICON_SIZE = 200;
    private static final int MARKER_ICON_BORDER_SIZE = 28;
    private static final float NAV_ICON_SIZE = 32f;
    private static final float MARKER_COLOUR = BitmapDescriptorFactory.HUE_BLUE;
    private static final int MARKER_ICON_BORDER_COLOUR = Color.WHITE;


    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private FirebaseUser mUser;
    private HashMap<UserLocation, Marker> mMarkers;
    private boolean firstTimeZoom = false;

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Navigation settings.
        BottomNavigationViewEx bnve = (BottomNavigationViewEx) findViewById(R.id.bottom_navigation);
        bnve.enableAnimation(true);
        bnve.enableItemShiftingMode(true);
        bnve.setTextVisibility(false);
        bnve.setIconSize(NAV_ICON_SIZE, NAV_ICON_SIZE);
        bnve.setItemHeight(NAV_HEIGHT);
        bnve.setOnNavigationItemSelectedListener(this);

        // Request location permission.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mUser == null) {
            Log.wtf("ToPkEk", "CaN't HaPpEn! ImPoSsIbLe StAte!");
            throw new NullPointerException("User not logged in???");
        }
        mMarkers = new HashMap<>();

        // Firebase database position reference.
        mDatabase = FirebaseDatabase.getInstance().getReference("locations");

        // Get existing markers.
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        addMarker(snapshot);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // Assign a listener for position changes.
        mDatabase.addChildEventListener(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // TODO: Clickable markers.
        // http://stackoverflow.com/questions/14226453/google-maps-api-v2-how-to-make-markers-clickable
        // mMap.setOnMarkerClickListener(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the mUser will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the mUser has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
    }

    /**
     * Handles the actions for the bottom navigation bar.
     *
     * @param item that is clicked.
     * @return whether the action is handled.
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_map:
                Log.v("NAV", "MAP");
                return true;

            case R.id.action_user:
                Log.v("NAV", "USER");
                return true;

            case R.id.action_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
        }
        return false;
    }

    /**
     * Called when the location has changed.
     * <p>
     * There are no restrictions on the use of the supplied Location object.
     *
     * @param location The new location, as a Location object.
     */
    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        //move map camera
        if (!firstTimeZoom) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(INITIAL_ZOOM));
            firstTimeZoom = true;
        }

        // Update location in the database.
        updateLocation(mUser.getUid(), latLng, mUser.getDisplayName());
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        } else {
            Log.e("PERMISSIONS", "USER DENIED LOCATION PERMISSION");
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            finish();
                            break;
                    }
                }
            };
            // No picture selected, sure you want to continue?
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("You can't use this application without GPS permissions")
                    .setPositiveButton("I understand", dialogClickListener).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) { // Do nothing
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Couldn't connect to the servers.", Toast.LENGTH_SHORT).show();
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    /**
     * Dispatch onStop() to all fragments.  Ensure all loaders are stopped.
     */
    @Override
    protected void onStop() {
        super.onStop();
        //stop location updates
        if (mGoogleApiClient != null) {
            Log.e("onStop", "Removing location updates.");
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }

        // TODO: Decide on this functionality.
        // Remove your own location entry.
        // mDatabase.getRoot().child("locations").child(mUser.getUid()).removeValue();
    }

    /**
     * This method is triggered when the data at a child location has changed.
     *
     * @param dataSnapshot An immutable snapshot of the data at the new data at the child location
     * @param s            The key name of sibling location ordered before the child.
     *                     This will be null for the first child node of a location.
     */
    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        addMarker(dataSnapshot);
    }

    /**
     * This method is triggered when the data at a child location has changed.
     *
     * @param dataSnapshot An immutable snapshot of the data at the new data at the child location
     * @param s            The key name of sibling location ordered before the child.
     *                     This will be null for the first child node of a location.
     */
    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        addMarker(dataSnapshot);
    }

    /**
     * This method is triggered when a child is removed from the
     * location to which this listener was added. Remove the corresponding marker on the map.
     *
     * @param dataSnapshot An immutable snapshot of the data at the child that was removed.
     */
    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        String uid = dataSnapshot.getKey();
        // Check and remove any duplicates.
        for (UserLocation loc : mMarkers.keySet()) {
            if (loc.uid.equals(uid)) {
                mMarkers.remove(loc).remove();
            }
        }
    }

    /**
     * This method is triggered when a child location's priority changes.
     *
     * @param dataSnapshot An immutable snapshot of the data at the location that moved.
     * @param s            The key name of the sibling location ordered before the child location.
     *                     This will be null if this location is ordered first.
     */
    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        Log.v("DATABASE", "onChildMoved");
    }

    /**
     * This method will be triggered in the event that this listener either failed at the server,
     * or is removed as a result of the security and Firebase rules.
     *
     * @param databaseError A description of the error that occurred
     */
    @Override
    public void onCancelled(DatabaseError databaseError) {
        Log.d("DATABASE", databaseError.getMessage() + databaseError.getDetails());
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking mUser if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the mUser *asynchronously* -- don't block
                // this thread waiting for the mUser's response! After the mUser
                // sees the explanation, try again to request the permission.

                //Prompt the mUser once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    /**
     * Updates the location of the current mUser in the database.
     *
     * @param userId the userId of the current mUser.
     * @param latLng current coordinates.
     */
    private void updateLocation(String userId, LatLng latLng, String name) {
        SimpleLocation loc = new SimpleLocation(latLng.latitude, latLng.longitude, name);
        mDatabase.child(userId).setValue(loc);
    }

    /**
     * Adds a marker to the map.
     *
     * @param dataSnapshot of the marker.
     */
    private void addMarker(DataSnapshot dataSnapshot) {
        SimpleLocation simpleLocation = dataSnapshot.getValue(SimpleLocation.class);
        final String uid = dataSnapshot.getKey();

        // Don't place markers for yourself.
        if (uid.equals(mUser.getUid())) {
            return;
        }

        // Check and remove any duplicates.
        for (UserLocation loc : mMarkers.keySet()) {
            if (loc.uid.equals(uid)) {
                mMarkers.remove(loc).remove();
            }
        }

        UserLocation userLocation = new UserLocation(simpleLocation, uid);
        MarkerOptions newMarker = userLocation.getMarkerOptions(MARKER_COLOUR);
        if (newMarker != null) {
            Log.d("ICON", "Added default marker");
            mMarkers.put(userLocation, mMap.addMarker(newMarker));
            mMarkers.get(userLocation).setVisible(false);
        }

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users")
                .child(uid);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("ICON", "onDataChange called");
                User user = dataSnapshot.getValue(User.class);
                new MarkerIconTask(uid).execute(user.picture);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("ICON", "onCancelled called");
            }
        });
    }

    /**
     * Load a user's profile picture and set it as their marker icon.
     */
    private class MarkerIconTask extends AsyncTask<String, Void, Bitmap> {

        /**
         * Uid of the user that belongs to the icon that is being loaded.
         */
        private String uid;

        MarkerIconTask(String uid) {
            this.uid = uid;
        }

        /**
         * @param params at index 0 is the download link for the bitmap.
         * @return bitmap or null.
         */
        @Override
        protected Bitmap doInBackground(String... params) {
            Log.d("ICON", "Loading the bitmap from " + params[0]);
            try {
                URL url = new URL(params[0]);

                Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                // TODO: Crop out the center, different case for hor and vert.
                // Create a square bitmap.
                bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getHeight(), bmp.getHeight());
                // Crop the bitmap to a 200 by 200 image.
                bmp = Bitmap.createScaledBitmap(bmp, MARKER_ICON_SIZE, MARKER_ICON_SIZE, true);
                // Get the bitmap as a circle.
                bmp = getCircleBitmap(bmp, MARKER_ICON_BORDER_SIZE);
                return bmp;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * Set the bitmap as marker image for the corresponding user.
         * @param bitmap user image.
         */
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            Log.d("ICON", "Loaded");
            for (UserLocation loc : mMarkers.keySet()) {
                if (loc.uid.equals(uid)) {
                    if (!mMarkers.get(loc).isVisible()) {
                        mMarkers.get(loc).setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
                        mMarkers.get(loc).setVisible(true);
                        Log.d("ICON", "Assigned");
                    }
                    return;
                }
            }
            Log.d("ICON", "Couldn't find corresponding marker");
        }

        /**
         * Returns the passed bitmap as a circle with a border.
         * @param bitmap to crop.
         * @param borderSizePx size of the border.
         * @return new cropped bitmap with a border.
         */
        private Bitmap getCircleBitmap(Bitmap bitmap, int borderSizePx) {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            // Create bitmap that has an alpha value.
            Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);

            final int color = 0xff424242;
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, width, height);

            // Prepare canvas.
            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            canvas.drawCircle(width / 2, height / 2, width / 2, paint);

            // Draw bitmap.
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, rect, rect, paint);

            // Draw border.
            paint.setColor(MARKER_ICON_BORDER_COLOUR);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth((float) borderSizePx);
            canvas.drawCircle(width / 2, height / 2, width / 2, paint);

            return output;
        }
    }
}
