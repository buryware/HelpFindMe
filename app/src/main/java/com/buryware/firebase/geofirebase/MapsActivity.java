/**
 * Copyright Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * Help Find Me! is written by Steve Stansbury, for the Buryware Company
 * Created September 2, 2020 by Buryware.
 * All rights reservered.
 *
 *
 */
package com.buryware.firebase.geofirebase;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.geofire.LocationCallback;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.android.gms.security.ProviderInstaller;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.BuildConfig;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener {

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;
        ImageView messageImageView;
        TextView messengerTextView;
      //  CircleImageView messengerImageView;

        public MessageViewHolder(View v) {
            super(v);
            messageTextView = (TextView) itemView.findViewById(R.id.messageTextView);
        }
    }

    private GoogleMap mMap;
    private List<Marker> LocationMarkers;
    private Menu mMenu = null;

    private String mLocationProvider;
    private LocationManager mLocationManager;
    private Location mLastKnownLocation;
    private Location mCurrentLocation;
    private LatLng debugLatLng = null;
    private LocationListener mLocationListener;

    // Firebase instance variables
    private FirebaseAuth mAuth = null;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseRecyclerAdapter<FriendlyMessage, MessageViewHolder> mFirebaseAdapter;
    private FirebaseAnalytics mFirebaseAnalytics;
    private GeoQuery geoQuery;

    private GoogleApiClient mGoogleApiClient;
    private static final String MESSAGE_URL = "http://friendlychat.firebase.google.com/message/";

    private Button mSendButton;
    private Button mFindButton;
    private ProgressBar mProgress;
    private RecyclerView mMessageRecyclerView;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private LinearLayoutManager mLinearLayoutManager;
    private ProgressBar mProgressBar;
    private EditText mMessageEditText;
    private ImageView mAddMessageImageView;

    // HelpFindMeAndSaveME user params
    private String mUid;
    private String mUsername;
    private String mPassword;
    private String mPhoneNumber;
    private Uri mPhotoUrl;
    private String mEmail;
    private String mMinutes;
    private GeoLocation mCurrentGeoLocation;
    private boolean mEmailVerified;
    private boolean bHelpMe = false;
    private boolean bFindMe = false;

    private static final int TWENTY_MINUTES = 1000 * 60 * 20;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final int PERMISSION_REQUEST_GPS = 2;
    private static final int PERMISSION_REQUEST_CAMERA = 3;
    private static final int PERMISSION_REQUEST_AUDIO = 4;

    private static final int REQUEST_INVITE = 1;
    public static final String ANONYMOUS = "anonymous";
    private static final String MESSAGE_SENT_EVENT = "message_sent";
    private static final String TAG = "MapsActivity";
    private static final int RC_SIGN_IN = 007;
    public static final String MESSAGES_CHILD = "messages";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LocationMarkers = new ArrayList<Marker>();
        mLocationProvider = LocationManager.GPS_PROVIDER;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            if (mLocationProvider.isEmpty()) {
                mLocationProvider = LocationManager.NETWORK_PROVIDER;
            }
        }

        FirebaseCrashlytics.getInstance().setUserId("12345");

        Button crashButton = new Button(this);
        crashButton.setText("Crash!");
        crashButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                throw new RuntimeException("Test Crash"); // Force a crash
            }
        });

        addContentView(crashButton, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mAuth.getCurrentUser();

        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            mUsername = ANONYMOUS;
            // Choose authentication providers
         /*   List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.EmailBuilder().build(),
                    new AuthUI.IdpConfig.PhoneBuilder().build(),
                    new AuthUI.IdpConfig.GoogleBuilder().build(),
                    new AuthUI.IdpConfig.FacebookBuilder().build(),
                    new AuthUI.IdpConfig.TwitterBuilder().build());*/

            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.EmailBuilder().build(),
                    new AuthUI.IdpConfig.PhoneBuilder().build(),
                    new AuthUI.IdpConfig.GoogleBuilder().build());

            // Create and launch sign-in intent
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .setLogo(R.mipmap.burywareb)
                            .build(),
                    RC_SIGN_IN);

            finish();
            return;

        } else {

            mUsername = mFirebaseUser.getDisplayName();
            mEmail = mFirebaseUser.getEmail();
            mPhoneNumber = mFirebaseUser.getPhoneNumber();
            mPhotoUrl = mFirebaseUser.getPhotoUrl();
            mUid = mFirebaseUser.getUid();
            mEmailVerified = mFirebaseUser.isEmailVerified();
        }

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("message/users");

        // myRef.setValue("Hello, World!");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                Log.d(TAG, "Value is: " + value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        // Initialize Firebase Measurement.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Initialize Firebase Remote Config.
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        // Define Firebase Remote Config Settings.
        FirebaseRemoteConfigSettings firebaseRemoteConfigSettings = new FirebaseRemoteConfigSettings.Builder().setDeveloperModeEnabled(true).build();

        // Define default config values. Defaults are used when fetched config values are not
        // available. Eg: if an error occurred fetching values from the server.
        Map<String, Object> defaultConfigMap = new HashMap<>();
        defaultConfigMap.put("friendly_msg_length", 120L);

        // Apply config settings and default values.
        mFirebaseRemoteConfig.setConfigSettings(firebaseRemoteConfigSettings);
        mFirebaseRemoteConfig.setDefaults(defaultConfigMap);

        // Fetch remote config.
        fetchConfig();

        // Initialize ProgressBar and RecyclerView.
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mMessageRecyclerView = (RecyclerView) findViewById(R.id.messageRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        SnapshotParser<FriendlyMessage> parser = new SnapshotParser<FriendlyMessage>() {
            @Override
            public FriendlyMessage parseSnapshot(DataSnapshot dataSnapshot) {
                FriendlyMessage friendlyMessage = dataSnapshot.getValue(FriendlyMessage.class);
                if (friendlyMessage != null) {
                    friendlyMessage.setId(dataSnapshot.getKey());
                }
                return friendlyMessage;
            }
        };

        DatabaseReference messagesRef = mFirebaseDatabaseReference.child(MESSAGES_CHILD);
        FirebaseRecyclerOptions<FriendlyMessage> options =
                new FirebaseRecyclerOptions.Builder<FriendlyMessage>()
                        .setQuery(messagesRef, parser)
                        .build();
        mFirebaseAdapter = new FirebaseRecyclerAdapter<FriendlyMessage, MessageViewHolder>(options) {
            @Override
            public MessageViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                return new MessageViewHolder(inflater.inflate(R.layout.item_message, viewGroup, false));
            }

            @Override
            protected void onBindViewHolder(final MessageViewHolder viewHolder,
                                            int position,
                                            FriendlyMessage friendlyMessage) {
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                if (friendlyMessage.getText() != null) {
                    viewHolder.messageTextView.setText(friendlyMessage.getText());
                    viewHolder.messageTextView.setVisibility(TextView.VISIBLE);
                    viewHolder.messageImageView.setVisibility(ImageView.GONE);
                }
            }
        };

        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition =
                        mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    mMessageRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        mMessageRecyclerView.setAdapter(mFirebaseAdapter);
        mProgress = findViewById(R.id.progressBar);

        mLocationProvider = LocationManager.GPS_PROVIDER;
        if (mLocationProvider.isEmpty()) {
            mLocationProvider = LocationManager.NETWORK_PROVIDER;
        }

        mLocationManager = (LocationManager) getApplication().getSystemService(Context.LOCATION_SERVICE);

        DatabaseReference georef = FirebaseDatabase.getInstance().getReference("messages/geofire");
        final GeoFire geoFire = new GeoFire(georef);

        geoFire.setLocation("firebase-hq", new GeoLocation(47.624690, -122.131130));  // todo set to current location

        geoFire.getLocation("firebase-hq", new LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation location) {
                if (location != null) {
                    System.out.println(String.format("The location for key %s is [%f,%f]", key, location.latitude, location.longitude));

                    geoQuery = geoFire.queryAtLocation(new GeoLocation(location.latitude, location.longitude), 0.8);
                    geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                        @Override
                        public void onKeyEntered(String key, GeoLocation location) {
                            System.out.println(String.format("Key %s entered the search area at [%f,%f]", key, location.latitude, location.longitude));
                        }

                        @Override
                        public void onKeyExited(String key) {
                            System.out.println(String.format("Key %s is no longer in the search area", key));
                        }

                        @Override
                        public void onKeyMoved(String key, GeoLocation location) {
                            System.out.println(String.format("Key %s moved within the search area to [%f,%f]", key, location.latitude, location.longitude));
                        }

                        @Override
                        public void onGeoQueryReady() {
                            System.out.println("All initial data has been loaded and events have been fired!");
                        }

                        @Override
                        public void onGeoQueryError(DatabaseError error) {
                            System.err.println("There was an error with this query: " + error);
                        }
                    });

                    mCurrentGeoLocation = location;

                } else {
                    System.out.println(String.format("There is no location for key %s in GeoFire", key));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // Ensure an updated security provider is installed into the system when a new one is
        // available via Google Play services.
        try {
            ProviderInstaller.installIfNeededAsync(getApplicationContext(),
                    new ProviderInstaller.ProviderInstallListener() {
                        @Override
                        public void onProviderInstalled() {
                            //Toast("New security provider installed.");
                            installPlayServiceSecurityUpdates();
                        }

                        @Override
                        public void onProviderInstallFailed(int errorCode, Intent intent) {
                            //    ConsoleMessage.MessageLevel.LOG(TAG, "New security provider install failed.");
                            // No notification shown there is no user intervention needed.
                        }
                    });
        } catch (Exception ignorable) {
            // ConsoleMessage.MessageLevel.LOG(TAG, "Unknown issue trying to install a new security provider.", ignorable);
        }

        mSendButton = (Button) findViewById(R.id.HelpButton);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String timeStamp = new SimpleDateFormat( getString(R.string.TimeStampSeed)).format(new Date());
                FriendlyMessage friendlyMessage = new FriendlyMessage(mUid, mUsername, mPassword, mCurrentGeoLocation.toString(), mMinutes, "HELPME", timeStamp);
                mFirebaseDatabaseReference.child(MESSAGES_CHILD).push().setValue(friendlyMessage);
                mFirebaseAnalytics.logEvent(MESSAGE_SENT_EVENT, null);
            }
        });

        mFindButton = (Button) findViewById(R.id.FindYouButton);
        mFindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String timeStamp = new SimpleDateFormat( getString(R.string.TimeStampSeed)).format(new Date());
                FriendlyMessage friendlyMessage = new FriendlyMessage(mUid, mUsername, mPassword, mCurrentGeoLocation.toString(), mMinutes, "FINDYOU", timeStamp);
                mFirebaseDatabaseReference.child(MESSAGES_CHILD).push().setValue(friendlyMessage);
                mFirebaseAnalytics.logEvent(MESSAGE_SENT_EVENT, null);
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestForGPSPermission();
        }
    }

    @Override
    public void onPause() {
        mFirebaseAdapter.stopListening();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mFirebaseAdapter.startListening();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /**
     *  installing latest security fixes throught play services, e.g. TLS1.2 support.
     *  But dont push anybody by annoying messages to upgrade Play Services, we dont rely on it.
     */
    private void installPlayServiceSecurityUpdates() {
        try {

            ProviderInstaller.installIfNeeded(getApplicationContext());

        } catch (Exception e ) {
            Log.e(TAG, "Error installing play service features", e);
        }
    }

    public void requestForGPSPermission() {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_GPS);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_GPS);
    }

    public void requestForCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
    }

    public void requestForAudioPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST_AUDIO);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Uri callUri = Uri.parse("tel://<911>");
                    Intent callIntent = new Intent(Intent.ACTION_CALL, callUri);
                    callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_USER_ACTION);
                }
                break;

            case PERMISSION_REQUEST_GPS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        mLocationManager.requestLocationUpdates(mLocationProvider, 0, 0, mLocationListener);
                        mLastKnownLocation = mLocationManager.getLastKnownLocation(mLocationProvider);
                        onMapReady(mMap);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;

            case PERMISSION_REQUEST_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                        requestForAudioPermission();
                    }
                    // doStartVideoStreaming();  // sos todo agora videostreming
                }
                break;

            case PERMISSION_REQUEST_AUDIO:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // doStartVideoStreaming();  // sos todo agora videostreming
                }
                break;
        }
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

    //    LatLng mLatLng = new LatLng(mCurrentGeoLocation.latitude, mCurrentGeoLocation.longitude);
        LatLng mLatLng = new LatLng(47.624690, -122.131130);
        mMap.addMarker(new MarkerOptions().position(mLatLng).title("Current location:"));

        final float GEOFENCE_RADIUS = 1000.0f;
        CircleOptions circleOptions = new CircleOptions().center(new LatLng(mLatLng.latitude, mLatLng.longitude))
                                                        .radius(GEOFENCE_RADIUS).fillColor(R.color.colorGeoFence)
                                                        .strokeColor(Color.LTGRAY).strokeWidth(2);
        mMap.addCircle(circleOptions);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(mLatLng));
        UiSettings mUiSettings = mMap.getUiSettings();
        mUiSettings.setCompassEnabled(true);
        mUiSettings.setZoomControlsEnabled(true);
        mUiSettings.setMapToolbarEnabled(true);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng location) {
                onMapAddLocation(location);
            }
        });
    }

    public void onMapAddLocation(LatLng mNewLatLng) {
        if (BuildConfig.DEBUG) debugLatLng = mNewLatLng;
    }

    public void onMapAddHelpGPSSelected(LatLng mLatLng) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(mLatLng);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.helpmapmarkersel));

        LocationMarkers.add(mMap.addMarker(markerOptions));
    }

    public void onMapAddHelpGPS(LatLng mLatLng) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(mLatLng);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.helpmapmarker));

        LocationMarkers.add(mMap.addMarker(markerOptions));
    }

    public void onMapAddassistgpsSelected(LatLng mLatLng) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(mLatLng);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.findmapmarkersel));

        LocationMarkers.add(mMap.addMarker(markerOptions));
    }

    public void onMapAddassistgps(LatLng mLatLng) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(mLatLng);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.findmapmarker));

        LocationMarkers.add(mMap.addMarker(markerOptions));
    }

    private LatLng getLocationLatLong() {
        LatLng mLatLong = null;


        // geo fire  //  sos todo

        return mLatLong;
    }

    /**
     * Checks whether two providers are the same
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        mMenu = menu;

        mMenu.getItem(4).setEnabled(false);   // Camera front
        mMenu.getItem(5).setEnabled(false);   // Camera back
        mMenu.getItem(6).setEnabled(false);   // Flash on
        mMenu.getItem(7).setEnabled(false);   // Flash off

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.invite_menu:
                sendInvitation();
                return true;

            case R.id.sign_out_menu:
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                // ...
                            }
                        });
                mFirebaseUser = null;
                mUsername = ANONYMOUS;

                // Choose authentication providers
                List<AuthUI.IdpConfig> providers = Arrays.asList(
                        new AuthUI.IdpConfig.EmailBuilder().build(),
                        new AuthUI.IdpConfig.PhoneBuilder().build(),
                        new AuthUI.IdpConfig.GoogleBuilder().build(),
                        new AuthUI.IdpConfig.FacebookBuilder().build(),
                        new AuthUI.IdpConfig.TwitterBuilder().build());

                // Create and launch sign-in intent
                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setAvailableProviders(providers)
                                .build(),
                        RC_SIGN_IN);

                finish();
                return true;

            case R.id.fresh_config_menu:
                fetchConfig();
                return true;

            case R.id.camara_front:
                try {
                   //  mCamera.switchToPosition(FKCamera.Position.FRONT);  // so todo agora
                    mMenu.getItem(4).setEnabled(false);   // Front off
                    mMenu.getItem(5).setEnabled(true);    // Back on
                    mMenu.getItem(6).setEnabled(false);   // Flash on
                    mMenu.getItem(7).setEnabled(false);   // Flash off

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;

            case R.id.camara_back:
                try {
                    // mCamera.switchToPosition(FKCamera.Position.BACK);    // so todo agora
                    mMenu.getItem(4).setEnabled(true);   // Front on
                    mMenu.getItem(5).setEnabled(false);  // Back off
                /*    if (mCamera.isFlashAvailable()) {                     // so todo agora
                        mMenu.getItem(6).setEnabled(true);   // Flash on
                        mMenu.getItem(7).setEnabled(true);   // Flash off
                    }*/

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;

            case R.id.camara_flashon:
                try {
                    // mCamera.setFlashEnabled(true);                         // so todo agora

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;

            case R.id.camara_flashoff:
                try {
                    //mCamera.setFlashEnabled(false);                          // so todo agora

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;

            case R.id.useraccount:
            {
                Intent about = new Intent(this, AboutActivity.class);
                about.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_USER_ACTION);
                startActivity(about);
            }

            case R.id.about:
            {
                Intent about = new Intent(this, AboutActivity.class);
                about.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_USER_ACTION);
                startActivity(about);
            }

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void causeCrash() {
        throw new NullPointerException("Fake null pointer exception");
    }

    private void sendInvitation() {
        Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title)).setMessage(getString(R.string.invitation_message)).setCallToActionText(getString(R.string.invitation_cta)).build();
        startActivityForResult(intent, REQUEST_INVITE);
    }

    // Fetch the config to determine the allowed length of messages.
    public void fetchConfig() {
        long cacheExpiration = 3600; // 1 hour in seconds
        // If developer mode is enabled reduce cacheExpiration to 0 so that each fetch goes to the
        // server. This should not be used in release builds.
        if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {     // so todo agora
            cacheExpiration = 0;
        }
        mFirebaseRemoteConfig.fetch(cacheExpiration).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // Make the fetched config available via FirebaseRemoteConfig get<type> calls.
                mFirebaseRemoteConfig.activateFetched();
                applyRetrievedLengthLimit();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // There has been an error fetching the config
                Log.w(TAG, "Error fetching config", e);
                applyRetrievedLengthLimit();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);  // so todo agora

        if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                // Use Firebase Measurement to log that invitation was sent.
                Bundle payload = new Bundle();
                payload.putString(FirebaseAnalytics.Param.VALUE, "inv_sent");

                // Check how many invitations were sent and log.
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                Log.d(TAG, "Invitations sent: " + ids.length);
            } else {
                // Use Firebase Measurement to log that invitation was not sent
                Bundle payload = new Bundle();
                payload.putString(FirebaseAnalytics.Param.VALUE, "inv_not_sent");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SHARE, payload);

                // Sending failed or it was canceled, show failure message to the user
                Log.d(TAG, "Failed to send invitation.");
            }
        } else if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                mUsername = mFirebaseUser.getDisplayName();
                mEmail = mFirebaseUser.getEmail();
                mPhoneNumber = mFirebaseUser.getPhoneNumber();
                mPhotoUrl = mFirebaseUser.getPhotoUrl();
                mUid = mFirebaseUser.getUid();
                mEmailVerified = mFirebaseUser.isEmailVerified();
                // ...
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
                int i = 0;
            }
        }
    }

    /**
     * Apply retrieved length limit to edit text field. This result may be fresh from the server or it may be from
     * cached values.
     */
    private void applyRetrievedLengthLimit() {
        Long friendly_msg_length = mFirebaseRemoteConfig.getLong("friendly_msg_length");
        //  mMessageText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(friendly_msg_length.intValue())});
        //  mMessageText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(friendly_msg_length.intValue())});
        Log.d(TAG, "FML is: " + friendly_msg_length);
    }
}