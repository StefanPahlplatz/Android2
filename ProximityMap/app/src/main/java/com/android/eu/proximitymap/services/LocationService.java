package com.android.eu.proximitymap.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.android.eu.proximitymap.R;
import com.android.eu.proximitymap.Utils.DistanceCalculator;
import com.android.eu.proximitymap.Utils.LocationHelper;
import com.android.eu.proximitymap.activities.LoginActivity;
import com.android.eu.proximitymap.models.SimpleLocation;
import com.android.eu.proximitymap.models.UserLocation;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class LocationService extends Service implements LocationHelper.LocationListener {
    private static final String TAG = LocationService.class.getSimpleName();

    public static boolean RUNNING;
    private LatLng lastLocation;
    private ArrayList<String> usersInVicinity;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.v(TAG, "onStartCommand");
        RUNNING = true;

        // Get last position from intent.
        double lat = intent.getExtras().getDouble("lat");
        double lng = intent.getExtras().getDouble("lng");
        lastLocation = new LatLng(lat, lng);
        usersInVicinity = new ArrayList<>();

        // Create calculator
        final DistanceCalculator calc = new DistanceCalculator();

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            stopSelf();
        } else {
            LocationHelper locationHelper = new LocationHelper(this, user.getUid(), user.getDisplayName());
            locationHelper.buildGoogleApiClient();

            DatabaseReference database = FirebaseDatabase.getInstance().getReference("locations");
            database.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    // Convert dataSnapShot to a location.
                    SimpleLocation simpleLoc = dataSnapshot.getValue(SimpleLocation.class);
                    UserLocation loc = new UserLocation(simpleLoc, dataSnapshot.getKey());

                    // If it's not the own user location that changed.
                    if (!loc.uid.equals(user.getUid())) {
                        // If the other user is closer than x meters.
                        if (calc.getDistance(lastLocation, loc.getLatLng()) < 50) { //TODO: Make setting.
                            if (!usersInVicinity.contains(loc.uid)) {
                                usersInVicinity.add(loc.uid);
                                vibrate();
                                addNotification(loc.name);
                            }
                        } else {
                            if (usersInVicinity.contains(loc.uid)) {
                                usersInVicinity.remove(loc.uid);
                            }
                        }
                        Log.v(TAG + ".onDataChange", loc.toString());
                    }
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
        return START_STICKY;
    }

    @Override
    public void updateLocation(LatLng latLng) {
        lastLocation = latLng;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "onDestroy");
        RUNNING = false;
    }

    private void addNotification(String user) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_directions_walk_black_24dp)
                        .setContentTitle("A User Entered Your Vicinity!")
                        .setContentText(user + " just entered your neighbourhood.");

        Intent notificationIntent = new Intent(this, LoginActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }

    /**
     * Vibrates the phone for 400 ms.
     */
    private void vibrate() {
        // Get instance of Vibrator from current Context
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        Log.v(TAG + ".vibrate", "VIBRATING");
        if (v.hasVibrator()) {
            v.vibrate(400);
        }
    }
}
