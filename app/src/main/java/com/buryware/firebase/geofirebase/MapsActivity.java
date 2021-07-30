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
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.provider.Settings.Secure;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.ui.auth.AuthUI;

import com.firebase.ui.auth.BuildConfig;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
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

import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.security.ProviderInstaller;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private List<Marker> LocationMarkers;
    private Menu mMenu = null;

    private String mLocationProvider;
    private LocationManager mLocationManager;
    private Location mLastKnownLocation;
    private Location mCurrentLocation;

    // Firebase instance variables
    private FirebaseAuth mAuth = null;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseRecyclerAdapter<FriendlyMessage, MessageViewHolder> mFirebaseAdapter;
    private FirebaseAnalytics mFirebaseAnalytics;
    private GeoQuery geoQuery;

    private static final String MESSAGE_URL = "http://friendlychat.firebase.google.com/message/";

    private Button mSendButton;
    private Button mFindButton;
    private Button mCancelButton;
    private Button mMuteButton;
    private Button mFlipCameraButton;
    private ProgressBar mProgress;
    private RecyclerView mMessageRecyclerView;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private LinearLayoutManager mLinearLayoutManager;
    private ProgressBar mProgressBar;
    private EditText mMessageEditText;
    private ImageView mAddMessageImageView;

    private RtcEngine mRtcEngine;
    private FrameLayout mLocalContainer = null;
    private SurfaceView mLocalView = null;
    private FrameLayout mRemoteContainer = null;
    private SurfaceView mRemoteView = null;
    private VideoCanvas mLocalVideo;
    private VideoCanvas mRemoteVideo;
    private static String channelName;
    private static int channelProfile;

    // HelpFindMeAndSaveME user params
    private String mUid;
    private String this_device_id;
    private String mUsername;
    private String mPassword;
    private String mPhoneNumber;
    private Uri mPhotoUrl;
    private String mEmail;
    private String mMinutes = "5000";

    //   private GeoLocation mCurrentGeoLocation;
    private Location gps_loc, network_loc, final_loc;
    private double longitude;
    private double latitude;
    private LatLng debugLatLng = null;
    private Polyline mLastPolyline = null;

    private FriendlyMessage helpmsg = null;
    private FriendlyMessage findmsg = null;

    private boolean mEmailVerified = false;
    private boolean bHelpMe = false;
    private boolean bFindMe = false;
    private boolean bMuted = false;
    private boolean bCallEnd = false;
    private boolean bFrontCamera = true;
    private int mSelectedHelp;

    private static final int PERMISSION_REQ_ID = 22;
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

    // Ask for Android device permissions at runtime.
    private static final String[] REQUESTED_PERMISSIONS = {Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

     //   Intent intent = getIntent();
     //   channelName = intent.getStringExtra(MapsActivity.channelName);
    //    channelProfile = intent.getIntExtra(String.valueOf(MapsActivity.channelProfile), -1);

        this_device_id = Secure.getString(getApplicationContext().getContentResolver(), Secure.ANDROID_ID);

        LocationMarkers = new ArrayList<Marker>();
        mLocationProvider = LocationManager.GPS_PROVIDER;
        if (mLocationProvider.isEmpty()) {
            mLocationProvider = LocationManager.NETWORK_PROVIDER;
        }

        bFindMe = bHelpMe = false;
        mSelectedHelp = 0;  // sos BUG!  TODO

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

            List<AuthUI.IdpConfig> providers = Arrays.asList(new AuthUI.IdpConfig.EmailBuilder().build(), new AuthUI.IdpConfig.PhoneBuilder().build(), new AuthUI.IdpConfig.GoogleBuilder().build());

            // Create and launch sign-in intent
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers).setLogo(R.mipmap.burywareb).build(), RC_SIGN_IN);

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

        GoogleSignInOptions signinoptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build();
        GoogleSignInClient mSignInClient = GoogleSignIn.getClient(this, signinoptions);

        // Initialize Firebase Measurement.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Initialize Firebase Remote Config.
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        // Define Firebase Remote Config Settings.
        FirebaseRemoteConfigSettings firebaseRemoteConfigSettings = new FirebaseRemoteConfigSettings.Builder().build();

        // Define default config values. Defaults are used when fetched config values are not
        // available. Eg: if an error occurred fetching values from the server.
        Map<String, Object> defaultConfigMap = new HashMap<>();
        defaultConfigMap.put("friendly_msg_length", 120L);

        // Apply config settings and default values.
        mFirebaseRemoteConfig.setConfigSettingsAsync(firebaseRemoteConfigSettings);
        mFirebaseRemoteConfig.setDefaultsAsync(defaultConfigMap);

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
        FirebaseRecyclerOptions<FriendlyMessage> options = new FirebaseRecyclerOptions.Builder<FriendlyMessage>().setQuery(messagesRef, parser).build();
        mFirebaseAdapter = new FirebaseRecyclerAdapter<FriendlyMessage, MessageViewHolder>(options) {
            @Override
            public MessageViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                return new MessageViewHolder(inflater.inflate(R.layout.item_message, viewGroup, false));
            }

            @Override
            protected void onBindViewHolder(final MessageViewHolder viewHolder, int position, FriendlyMessage friendlyMessage) {
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                if (friendlyMessage.getmsgType() != null) {
                    viewHolder.messageTextView.setText(friendlyMessage.getmsgType());

                    if (friendlyMessage.getmsgType().equals(getResources().getString(R.string.findme_msg))) {
                        viewHolder.messageTextView.setTextColor(Color.BLUE);
                        for (int i = position - 1; i >= 0; i--) {

                            FriendlyMessage helpmsg = mFirebaseAdapter.getItem(i);
                            if (friendlyMessage.gethelpid().equals(helpmsg.gethelpid())) {

                                if (mLastPolyline != null) {  // If we have an old one, remove it
                                    for (int j = 0; j < LocationMarkers.size(); j++) {
                                        if (LocationMarkers.get(j).getTitle() == this_device_id) {
                                            LocationMarkers.remove(j);
                                        }
                                    }
                                    mLastPolyline.remove();
                                }

                                final int PATTERN_GAP_LENGTH_PX = 20;
                                final PatternItem DOT = new Dot();
                                final PatternItem GAP = new Gap(PATTERN_GAP_LENGTH_PX);
                                final List<PatternItem> PATTERN_POLYLINE_DOTTED = Arrays.asList(GAP, DOT);
                                Polyline polyline = mMap.addPolyline(new PolylineOptions().add(helpmsg.getLatLng()).add(friendlyMessage.getLatLng()).pattern(PATTERN_POLYLINE_DOTTED).color(Color.YELLOW).width(5f));
                                onMapAddHelpGPSSelected(helpmsg.getLatLng());
                                helpmsg.setFromTo(polyline.toString());
                                mLastPolyline = polyline;

                                break;
                            }
                        }
                        onMapAddAssistGPS(friendlyMessage.getLatLng());   // show close by possible finders
                    }
                    if (friendlyMessage.getmsgType().equals(getString(R.string.help_msg))) {
                        viewHolder.messageTextView.setTextColor(Color.RED);
                        onMapAddHelpGPS(friendlyMessage.getLatLng());
                        mSelectedHelp = position;
                    }
                    viewHolder.messageTextView.setVisibility(TextView.VISIBLE);
                    viewHolder.messageTextView.setGravity(Gravity.START);
                }
            }
        };

        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added message.
                if (lastVisiblePosition == -1 || (positionStart >= (friendlyMessageCount - 1) && lastVisiblePosition == (positionStart - 1))) {
                    mMessageRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        mMessageRecyclerView.setAdapter(mFirebaseAdapter);
        mProgress = findViewById(R.id.progressBar);

        // Ensure an updated security provider is installed into the system when a new one is
        // available via Google Play services.
        try {
            ProviderInstaller.installIfNeededAsync(getApplicationContext(), new ProviderInstaller.ProviderInstallListener() {
                @Override
                public void onProviderInstalled() {
                    //Toast("New security provider installed.");
                    installPlayServiceSecurityUpdates();
                }

                @Override
                public void onProviderInstallFailed(int errorCode, Intent intent) {
                    System.out.println("New security provider install failed.");
                    // No notification shown there is no user intervention needed.
                }
            });
        } catch (Exception ignorable) {
            System.out.println("Unknown issue trying to install a new security provider.");
        }

        mSendButton = (Button) findViewById(R.id.HelpButton);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (bHelpMe) {
                    Toast toast = Toast.makeText(getApplication().getBaseContext(), R.string.HelpStarted, Toast.LENGTH_SHORT);
                    toast.show();

                } else if (bFindMe) {
                    Toast toast = Toast.makeText(getApplication().getBaseContext(), R.string.FindStarted, Toast.LENGTH_SHORT);
                    toast.show();

                } else {
                    mProgress.setVisibility(View.VISIBLE);

                    String lat = String.valueOf(latitude);
                    String lng = String.valueOf(longitude);

                    if (BuildConfig.DEBUG) {
                        if (debugLatLng != null) {
                            lat = String.valueOf(debugLatLng.latitude);
                            lng = String.valueOf(debugLatLng.longitude);
                        }
                    }

                    setupVideoConfig();
                    setupLocalVideo();

                    mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
                    channelProfile = Constants.CLIENT_ROLE_BROADCASTER;
                    mRtcEngine.setClientRole(channelProfile);

                    joinChannel();
                    showButtons(true);

                    String timeStamp = new SimpleDateFormat(getString(R.string.TimeStampSeed)).format(new Date());
                    helpmsg = new FriendlyMessage("Steve", "1234", "sos@gmail.com", "(424) 679-6456", getString(R.string.help_msg), "500", lat, lng, timeStamp, "this_device_id", "0");
                    mFirebaseDatabaseReference.child(MESSAGES_CHILD).push().setValue(helpmsg);
                    mFirebaseAnalytics.logEvent(MESSAGE_SENT_EVENT, null);
                    bHelpMe = true;
                    debugLatLng = null;
                    mProgress.setVisibility(View.INVISIBLE);
                }
            }
        });

        mFindButton = (Button) findViewById(R.id.FindYouButton);
        mFindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bHelpMe) {
                    Toast toast = Toast.makeText(getApplication().getBaseContext(), R.string.HelpStarted, Toast.LENGTH_SHORT);
                    toast.show();

                } else if (bFindMe) {
                    Toast toast = Toast.makeText(getApplication().getBaseContext(), R.string.FindStarted, Toast.LENGTH_SHORT);
                    toast.show();

                } else {
                    mProgress.setVisibility(View.VISIBLE);

                    String lat = String.valueOf(latitude);
                    String lng = String.valueOf(longitude);

                    if (debugLatLng != null) {
                        lat = String.valueOf(debugLatLng.latitude);
                        lng = String.valueOf(debugLatLng.longitude);
                    }

                    if (mSelectedHelp > -1) {
                        helpmsg = mFirebaseAdapter.getItem(mSelectedHelp);

                        String timeStamp = new SimpleDateFormat(getString(R.string.TimeStampSeed)).format(new Date());
                        findmsg = new FriendlyMessage("Steve", "1234", "sos@gmail.com", "(424) 679-6456", getResources().getString(R.string.findme_msg), "500", lat, lng, timeStamp.toString(), helpmsg.gethelpid(), this_device_id);
                        mFirebaseDatabaseReference.child(MESSAGES_CHILD).push().setValue(findmsg);
                        mFirebaseAnalytics.logEvent(MESSAGE_SENT_EVENT, null);
                        bFindMe = true;
                        debugLatLng = null;
                    } else {
                        Toast toast = Toast.makeText(getApplication().getBaseContext(), "No selected people to find.", Toast.LENGTH_SHORT);
                        toast.show();
                    }

                    setupVideoConfig();
                    setupLocalVideo();

                    mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
                    channelProfile = Constants.CLIENT_ROLE_AUDIENCE;
                    mRtcEngine.setClientRole(channelProfile);

                    joinChannel();
                    showButtons(true);

                    mProgress.setVisibility(View.INVISIBLE);
                }
            }
        });

        mCancelButton = (Button) findViewById(R.id.CancelButton);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /*for (int i = mFirebaseAdapter.getItemCount()-1; i >=0; i--){  // todo delete the items or update
                    FriendlyMessage findmsg = mFirebaseAdapter.getItem(i);
                    if (findmsg.getmsgType().equals(getString(R.string.findme_msg))) {
                        findmsg.sethelpid("null");  // clear the helpid
                        mFirebaseDatabaseReference.child(MESSAGES_CHILD).push().removeValue();
                        mFirebaseAnalytics.logEvent(MESSAGE_SENT_EVENT, null);
                    }
                }
*/
                bHelpMe = bFindMe = false;
                findmsg = helpmsg = null;

                removeFromParent(mLocalVideo);
                mLocalVideo = null;
                removeFromParent(mRemoteVideo);
                mRemoteVideo = null;
                mRtcEngine.leaveChannel();

                mProgress.setVisibility(View.INVISIBLE);
                showButtons(false);

                Toast toast = Toast.makeText(getApplication().getBaseContext(), R.string.CancelSession, Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        mMuteButton = (Button) findViewById(R.id.MuteButton);
        mMuteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onLocalAudioMuteClicked(null);

                Drawable[] drawables = mMuteButton.getCompoundDrawables();
                Drawable leftCompoundDrawable = drawables[1];
                if (!bMuted) {
                    Drawable img = getApplicationContext().getResources().getDrawable(R.mipmap.mutedmic);
                    img.setBounds(leftCompoundDrawable.getBounds());
                    mMuteButton.setCompoundDrawables(null, img, null, null);

                    Toast toast = Toast.makeText(getApplication().getBaseContext(), R.string.MuteSound, Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    Drawable img = getApplicationContext().getResources().getDrawable(R.mipmap.nonmutedmic);
                    img.setBounds(leftCompoundDrawable.getBounds());
                    mMuteButton.setCompoundDrawables(null, img, null, null);

                    Toast toast = Toast.makeText(getApplication().getBaseContext(), R.string.UnMuteSound, Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

        mFlipCameraButton = (Button) findViewById(R.id.CameraflipButton);
        mFlipCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRtcEngine.switchCamera();
                bFrontCamera = !bFrontCamera;
            }
        });

        mLocalContainer = (FrameLayout) findViewById(R.id.local_video_view_container);
        mRemoteContainer = (FrameLayout) findViewById(R.id.remote_video_view_container);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // If all the permissions are granted, initialize the RtcEngine object and join a channel.
        if (checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) && checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID) && checkSelfPermission(REQUESTED_PERMISSIONS[2], PERMISSION_REQ_ID)) {
            initEngineAndJoinChannel();
        }
    }

    private void initEngineAndJoinChannel() {
        initializeEngine();
    }

    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        // Listen for the onJoinChannelSuccess callback.
        // This callback occurs when the local user successfully joins the channel.
        public void onJoinChannelSuccess(String channel, final int uid, int elapsed) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i("agora", "Join channel success, uid: " + (uid & 0xFFFFFFFFL));
                }
            });
        }

        @Override
        // Listen for the onFirstRemoteVideoDecoded callback.
        // This callback occurs when the first video frame of a remote user is received and decoded after the remote user successfully joins the channel.
        // You can call the setupRemoteVideo method in this callback to set up the remote video view.
        public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i("agora", "First remote video decoded, uid: " + (uid & 0xFFFFFFFFL));
                    setupRemoteVideo(uid);
                }
            });
        }

        @Override
        // Listen for the onUserOffline callback.
        // This callback occurs when the remote user leaves the channel or drops offline.
        public void onUserOffline(final int uid, int reason) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i("agora", "User offline, uid: " + (uid & 0xFFFFFFFFL));
                        onRemoteUserLeft(uid);
                }
            });
        }
    };

    // Listen for the onFirstRemoteVideoDecoded callback.
    // This callback occurs when the first video frame of a remote user is received and decoded after the remote user successfully joins the channel.
    // You can call the setupRemoteVideo method in this callback to set up the remote video view.
    public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i("agora", "First remote video decoded, uid: " + (uid & 0xFFFFFFFFL));
                setupRemoteVideo(uid);
            }
        });
    }

    private void onRemoteUserLeft(int uid) {
        if (mRemoteVideo != null && mRemoteVideo.uid == uid) {
            removeFromParent(mRemoteVideo);
            // Destroys remote view
            mRemoteVideo = null;
        }
    }

    private void setupRemoteVideo(int uid) {

        mRtcEngine.enableVideo();

        ViewGroup parent = mRemoteContainer;
        if (parent.indexOfChild(mLocalVideo.view) > -1) {
            parent = mLocalContainer;
        }

        // Only one remote video view is available for this
        // tutorial. Here we check if there exists a surface
        // view tagged as this uid.
        if (mRemoteVideo != null) {
            return;
        }

        /*
          Creates the video renderer view.
          CreateRendererView returns the SurfaceView type. The operation and layout of the view
          are managed by the app, and the Agora SDK renders the view provided by the app.
          The video display view must be created using this method instead of directly
          calling SurfaceView.
         */
        SurfaceView view = RtcEngine.CreateRendererView(getBaseContext());
        view.setZOrderMediaOverlay(parent == mLocalContainer);
        parent.addView(view);
        mRemoteVideo = new VideoCanvas(view, VideoCanvas.RENDER_MODE_HIDDEN, uid);
        // Initializes the video view of a remote user.
        mRtcEngine.setupRemoteVideo(mRemoteVideo);
    }

    // Initialize the RtcEngine object.
    private void initializeEngine() {
        try {
            mRtcEngine = RtcEngine.create(getBaseContext(), getString(R.string.agora_app_id), mRtcEventHandler);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
    }

    private void setupVideoConfig() {
        // In simple use cases, we only need to enable video capturing
        // and rendering once at the initialization step.
        // Note: audio recording and playing is enabled by default.

        // Please go to this page for detailed explanation
        // https://docs.agora.io/en/Video/API%20Reference/java/classio_1_1agora_1_1rtc_1_1_rtc_engine.html#af5f4de754e2c1f493096641c5c5c1d8f
        mRtcEngine.setVideoEncoderConfiguration(new VideoEncoderConfiguration(
                VideoEncoderConfiguration.VD_640x480,
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT));
    }

    private void setupLocalVideo() {
        // This is used to set a local preview.
        // The steps setting local and remote view are very similar.
        // But note that if the local user do not have a uid or do
        // not care what the uid is, he can set his uid as ZERO.
        // Our server will assign one and return the uid via the event
        // handler callback function (onJoinChannelSuccess) after
        // joining the channel successfully.

        // Create a SurfaceView object.
        mLocalView = RtcEngine.CreateRendererView(getBaseContext());
        mLocalView.setZOrderMediaOverlay(true);
        mLocalContainer.addView(mLocalView);
        // Set the local video view.
        VideoCanvas localVideoCanvas = new VideoCanvas(mLocalView, VideoCanvas.RENDER_MODE_HIDDEN, 0);
        mRtcEngine.setupLocalVideo(localVideoCanvas);
    }

    private void showButtons(boolean show) {
        int visibility = show ? View.VISIBLE : View.GONE;

        mMuteButton.setVisibility(visibility);
        mFlipCameraButton.setVisibility(visibility);
    }

  /*  private void startCall() {
        setupLocalVideo();
        joinChannel();
    }

    private void endCall() {
        removeFromParent(mLocalVideo);
        mLocalVideo = null;
        removeFromParent(mRemoteVideo);
        mRemoteVideo = null;
        leaveChannel();
    }*/

    private void joinChannel() {
        // Join a channel with a token.
        mRtcEngine.joinChannel(getString(R.string.agora_access_token), "HelpFindAndSaveME", "Extra Optional Data", 0);
    }

    public void onLocalAudioMuteClicked(View view) {
        bMuted = !bMuted;
        mRtcEngine.muteLocalAudioStream(bMuted);
    }

    private ViewGroup removeFromParent(VideoCanvas canvas) {
        if (canvas != null) {
            ViewParent parent = canvas.view.getParent();
            if (parent != null) {
                ViewGroup group = (ViewGroup) parent;
                group.removeView(canvas.view);
                return group;
            }
        }
        return null;
    }

    private void switchView(VideoCanvas canvas) {
        ViewGroup parent = removeFromParent(canvas);
        if (parent == mLocalContainer) {
            if (canvas.view instanceof SurfaceView) {
                ((SurfaceView) canvas.view).setZOrderMediaOverlay(false);
            }
            mRemoteContainer.addView(canvas.view);
        } else if (parent == mRemoteContainer) {
            if (canvas.view instanceof SurfaceView) {
                ((SurfaceView) canvas.view).setZOrderMediaOverlay(true);
            }
            mLocalContainer.addView(canvas.view);
        }
    }

    public void onLocalContainerClick(View view) {
        switchView(mLocalVideo);
        switchView(mRemoteVideo);
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView messageTextView;

        public MessageViewHolder(View v) {
            super(v);
            itemView.setOnClickListener(this);
            messageTextView = (TextView) itemView.findViewById(R.id.messageTextView);
        }

        @Override
        public void onClick(View v) {
            //Toast.makeText(v.getContext(), "position = " + getLayoutPosition(), Toast.LENGTH_SHORT).show();
            FriendlyMessage msg = mFirebaseAdapter.getItem(getLayoutPosition());

            if (msg.getmsgType().equals(getApplicationContext().getResources().getString(R.string.help_msg))) {

                if (mSelectedHelp != -1) {
                    FriendlyMessage msg2 = mFirebaseAdapter.getItem(mSelectedHelp);
                    onMapAddHelpGPS(msg2.getLatLng());
                }
                mSelectedHelp = getLayoutPosition();
                onMapAddHelpGPSSelected(msg.getLatLng());
            } else {
                Toast toast = Toast.makeText(getApplication().getBaseContext(), "Only HELP ME entries can be selectted.", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestForGPSPermission();
        }

        mLocationManager = (LocationManager) getApplication().getSystemService(Context.LOCATION_SERVICE);
        LocationListener mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(android.location.Location location) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                String msg = "New Latitude: " + latitude + "New Longitude: " + longitude;

                UpdateGPSPtsInMsgs(location);
                Toast.makeText(getBaseContext(),msg,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };

        mLocationManager.requestLocationUpdates(mLocationProvider, 10000, 3, mLocationListener);
        mLastKnownLocation = mLocationManager.getLastKnownLocation(mLocationProvider);

        gps_loc = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        network_loc = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        if (gps_loc != null) {
            final_loc = gps_loc;
            latitude = final_loc.getLatitude();
            longitude = final_loc.getLongitude();
        }
        else if (network_loc != null) {
            final_loc = network_loc;
            latitude = final_loc.getLatitude();
            longitude = final_loc.getLongitude();
        }
        else {
            // default to where are we?
            latitude = getLocationLatLong().latitude;;
            longitude = getLocationLatLong().longitude;
        }
    }

    private void UpdateGPSPtsInMsgs(Location location){

    //    mMap.clear();

        if (bHelpMe || bFindMe) {

            location.setLatitude(999999.99);
            location.setLongitude(999999.99);

            mFirebaseDatabaseReference.child("lat").setValue(location.getLatitude());
            mFirebaseDatabaseReference.child("longi").setValue(location.getLongitude());
            mFirebaseAnalytics.logEvent(MESSAGE_SENT_EVENT, null);

        } else {
            assert(true);
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

        if (!bCallEnd) {
            leaveChannel();
        }
        if (mRtcEngine != null) {
            RtcEngine.destroy();
        }
    }

    private void leaveChannel() {
        // Leave the current channel.
        if (mRtcEngine != null) {
            mRtcEngine.leaveChannel();
        }
    }

    /**
     *  installing latest security fixes throught play services, e.g. TLS1.2 support.
     *  But dont push anybody by annoying messages to upgrade Play Services, we dont rely on it.
     */
    private void installPlayServiceSecurityUpdates() {
        try {
            ProviderInstaller.installIfNeeded(getApplicationContext());

        } catch (Exception e) {
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

    @SuppressLint("MissingPermission")
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
                }
                break;

            case PERMISSION_REQUEST_AUDIO:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // TODO
                }
                break;

            case PERMISSION_REQ_ID:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED ||
                        grantResults[1] != PackageManager.PERMISSION_GRANTED ||
                        grantResults[2] != PackageManager.PERMISSION_GRANTED) {
                   /* showLongToast("Need permissions " + Manifest.permission.RECORD_AUDIO +
                            "/" + Manifest.permission.CAMERA + "/" + Manifest.permission.WRITE_EXTERNAL_STORAGE);*/
                    finish();
                    return;
                }

                // Here we continue only if all permissions are granted.
                // The permissions can also be granted in the system settings manually.
                initEngineAndJoinChannel();
                break;
        }
    }

    private boolean checkSelfPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, requestCode);
            return false;
        }
        return true;
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
    //    LatLng mLatLng = new LatLng(47.624690, -122.131130);
        LatLng mLatLng = new LatLng(latitude, longitude);

        if (!bHelpMe && !bFindMe) {
            mMap.addMarker(new MarkerOptions().position(mLatLng).title("Current location:"));
        }

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
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng location) {
                onMapChangeLocation(location);
            }
        });

    }

    public void onMapAddLocation(LatLng mNewLatLng) {

        //  BuildConfig.DEBUG  todo
        debugLatLng = mNewLatLng;
        onMapAddDebugGPS(debugLatLng);
    }

    public void onMapChangeLocation(LatLng mNewLatLng) {

        //  BuildConfig.DEBUG  todo
        debugLatLng = mNewLatLng;
        onMapChangeDebugGPS(debugLatLng);
    }

    public void onMapAddHelpGPSSelected(LatLng mLatLng) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(mLatLng);
        markerOptions.title(this_device_id);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.helpmapmarkersel2));
        LocationMarkers.add(mMap.addMarker(markerOptions));
    }

    public void onMapAddHelpGPS(LatLng mLatLng) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(mLatLng);
        markerOptions.title(this_device_id);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.helpmapmarker));
        LocationMarkers.add(mMap.addMarker(markerOptions));
    }

    public void onMapAddDebugGPS(LatLng mLatLng) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(mLatLng);
        markerOptions.title(this_device_id);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.debugmapmarker));
        LocationMarkers.add(mMap.addMarker(markerOptions));
    }

    public void onMapChangeDebugGPS(LatLng mLatLng) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(mLatLng);
        markerOptions.title(this_device_id);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.changemapmarker));
        LocationMarkers.add(mMap.addMarker(markerOptions));
    }

    public void onMapAddAssistGPS(LatLng mLatLng) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(mLatLng);
        markerOptions.title(this_device_id);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.findmapmarker));
        LocationMarkers.add(mMap.addMarker(markerOptions));
    }

    private LatLng getLocationLatLong() {
        LatLng mLatLong = getLocationLatLong();

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

              /*  // Choose authentication providers
                List<AuthUI.IdpConfig> providers = Arrays.asList(
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
      // if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {     // so todo agora
            cacheExpiration = 0;
       // }
        mFirebaseRemoteConfig.fetch(cacheExpiration).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // Make the fetched config available via FirebaseRemoteConfig get<type> calls.
                mFirebaseRemoteConfig.activate();
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
                Task<GoogleSignInAccount> task =
                        GoogleSignIn.getSignedInAccountFromIntent(data);
                if (task.isSuccessful()) {
                    // Sign in succeeded, proceed with account
                    GoogleSignInAccount acct = task.getResult();
                } else {
                    // Sign in failed, handle failure and update UI
                    // ...
                }

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