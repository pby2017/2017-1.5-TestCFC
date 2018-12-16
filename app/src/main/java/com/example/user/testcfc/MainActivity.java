package com.example.user.testcfc;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//        ft.add(R.id.fragment_main, new FuncFragment()).commit();

        Fragment fg = FunctionFragment.newInstance();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_main, fg).commit();
    }

    public static class FunctionFragment extends Fragment {

        private Button mBtnRegisterFunc, mBtnCheckFunc, mBtnMeasuresFunc;

        public static Fragment newInstance() {
            FunctionFragment mFunctionFragment = new FunctionFragment();
            return mFunctionFragment;
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

            View view = inflater.inflate(R.layout.fragment_func, null);

//            return super.onCreateView(inflater, container, savedInstanceState);

            initFindView(view);

            return view;
        }

        private void initFindView(View view) {

            mBtnRegisterFunc = (Button) view.findViewById(R.id.button_register_function);
            mBtnCheckFunc = (Button) view.findViewById(R.id.button_check_function);
            mBtnMeasuresFunc = (Button) view.findViewById(R.id.button_measures_fuction);

            mBtnRegisterFunc.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {

                    FragmentTransaction ft = getFragmentManager().beginTransaction();

                    if (getFragmentManager().findFragmentById(R.id.fragment_main) == null) {
                        ft.add(R.id.fragment_main, RegisterFragment.newInstance());
                    } else {
                        ft.replace(R.id.fragment_main, RegisterFragment.newInstance());
                    }

                    ft.addToBackStack(null);
                    ft.commit();
                }
            });
        }
    }

    public static class RegisterFragment extends Fragment {

        private Button mBtnFireReg, mBtnFloodReg, mBtnMeteorReg;

        public static Fragment newInstance() {
            RegisterFragment mRegisterFragment = new RegisterFragment();
            return mRegisterFragment;
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

            View view = inflater.inflate(R.layout.fragment_register, null);

            initFindView(view);

//            return super.onCreateView(inflater, container, savedInstanceState);
            return view;
        }

        private void initFindView(View view) {
            mBtnFireReg = (Button) view.findViewById(R.id.button_fire_register);
            mBtnFloodReg = (Button) view.findViewById(R.id.button_flood_register);
            mBtnMeteorReg = (Button) view.findViewById(R.id.button_meteor_register);

            mBtnFireReg.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    FragmentTransaction ft = getFragmentManager().beginTransaction();

                    if (getFragmentManager().findFragmentById(R.id.fragment_main) == null) {
                        ft.add(R.id.fragment_main, SendDataFragment.newInstance());
                    } else {
                        ft.replace(R.id.fragment_main, SendDataFragment.newInstance());
                    }

                    ft.addToBackStack(null);
                    ft.commit();
                }
            });
        }
    }

    public static class SendDataFragment extends Fragment {

        private static final String TAG = MainActivity.class.getSimpleName();

        private static final int REQUEST_PERMISSIONS_LOCATION_SETTINGS_REQUEST_CODE = 33;
        private static final int REQUEST_PERMISSIONS_LAST_LOCATION_REQUEST_CODE = 34;
        private static final int REQUEST_PERMISSIONS_CURRENT_LOCATION_REQUEST_CODE = 35;

        private FusedLocationProviderClient mFusedLocationProviderClient;
        private LocationRequest mLocationRequest;
        private Location mLastLocation;
        private Location lastLocation = null;
        private Location currentLocation = null;

        private long MIN_UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
        private long FASTEST_INTERVAL = 2000; /* 2 sec */

        private ImageView mIvValueImgReg;
        private TextView mTvValueLatitudeReg, mTvValueLongitudeReg;
        private Button mBtnSendActionReg,mBtnCancelActionReg;

        private Random mRandom = new Random();

        private boolean isLocation;
        // Lag = -180 ~ 180 , Lng = -90 ~ 90
        private String mLatitude, mLongitude;

        public static Fragment newInstance() {
            SendDataFragment mSendDataFragment = new SendDataFragment();
            return mSendDataFragment;
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

            View view = inflater.inflate(R.layout.fragment_send_data, null);

            initFindView(view);

            mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
            // Create the location request to start receiving updates
            checkForLocationRequest();
            checkForLocationSettings();

            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, 1234);

//            return super.onCreateView(inflater, container, savedInstanceState);
            return view;
        }

        private void initFindView(View view) {

            mIvValueImgReg = (ImageView)view.findViewById(R.id.imageView_value_image_register);
            mTvValueLatitudeReg = (TextView) view.findViewById(R.id.textView_value_latitude_register);
            mTvValueLongitudeReg = (TextView)view.findViewById(R.id.textView_value_longitude_register);
            mBtnSendActionReg = (Button)view.findViewById(R.id.button_send_action_register);
            mBtnCancelActionReg = (Button)view.findViewById(R.id.button_cancel_action_register);


            mBtnSendActionReg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            // All your networking logic
                            // should be here
                            URL githubEndpoint = null;
                            try {
                                // Create URL
                                // 클라우드 url에는 port를 안붙인다.
                                githubEndpoint = new URL("");

                                // Create connection HttpURLConnection or HttpsURLConnection
                                HttpURLConnection myConnection =
                                        (HttpURLConnection) githubEndpoint.openConnection();

                                myConnection.setRequestMethod("POST");

                                myConnection.setRequestProperty("User-Agent", "my-rest-app-v0.1");

                                // Create the data
                                String myData = "Latitude="+mLatitude+"&Longitude="+mLongitude;

                                // Enable writing
                                myConnection.setDoOutput(true);

                                // Write the data
                                myConnection.getOutputStream().write(myData.getBytes());

                                // down code is GET method
                                if (myConnection.getResponseCode() == 200) {
                                    // Success
                                    // Further processing here
                                } else {
                                    // Error handling code goes here
                                }

                                InputStream responseBody = myConnection.getInputStream();

                                InputStreamReader responseBodyReader =
                                        new InputStreamReader(responseBody, "UTF-8");

                                JsonReader jsonReader = new JsonReader(responseBodyReader);

                                jsonReader.close();

                                myConnection.disconnect();

                                getFragmentManager().popBackStack();
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });

            mBtnCancelActionReg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            // All your networking logic
                            // should be here
                            URL githubEndpoint = null;
                            try {
                                // Create URL
                                // 클라우드 url에는 port를 안붙인다.
                                githubEndpoint = new URL("");

                                // Create connection HttpURLConnection or HttpsURLConnection
                                HttpURLConnection myConnection =
                                        (HttpURLConnection) githubEndpoint.openConnection();

                                myConnection.setRequestMethod("POST");

                                myConnection.setRequestProperty("User-Agent", "my-rest-app-v0.1");

                                int[] optNum = {-1,1};

                                double randomLat = (double)mRandom.nextInt(179) * optNum[mRandom.nextInt(1)] + mRandom.nextFloat() * optNum[mRandom.nextInt(1)];
                                double randomLng = (double)mRandom.nextInt(84) * optNum[mRandom.nextInt(1)] + mRandom.nextFloat() * optNum[mRandom.nextInt(1)];

                                // Create the data
                                String myData = "Latitude="+randomLat+"&Longitude="+randomLng;

                                // Enable writing
                                myConnection.setDoOutput(true);

                                // Write the data
                                myConnection.getOutputStream().write(myData.getBytes());

                                // down code is GET method
                                if (myConnection.getResponseCode() == 200) {
                                    // Success
                                    // Further processing here
                                } else {
                                    // Error handling code goes here
                                }

                                InputStream responseBody = myConnection.getInputStream();

                                InputStreamReader responseBodyReader =
                                        new InputStreamReader(responseBody, "UTF-8");

                                JsonReader jsonReader = new JsonReader(responseBodyReader);

                                jsonReader.close();

                                myConnection.disconnect();

                                getFragmentManager().popBackStack();
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            Log.w(TAG,requestCode+", "+resultCode);
            if(requestCode == 1234 && resultCode != 0){
                Bitmap bmp = (Bitmap)data.getExtras().get("data");

                if(bmp == null){
                    Toast.makeText(getContext(), "bmp is null!", Toast.LENGTH_SHORT).show();
                }
                else{
                    mIvValueImgReg.setImageBitmap(bmp);
                    callLastKnownLocation();
                }
            }
        }

        private void startLocationPermissionRequest(int requestCode) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, requestCode);
        }

        public void requestPermissions(final int requestCode) {
            boolean shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION);

            // Provide an additional rationale to the user. This would happen if the user denied the
            // request previously, but didn't check the "Don't ask again" checkbox.
            if (shouldProvideRationale) {
                startLocationPermissionRequest(requestCode);
            }
            else{
                startLocationPermissionRequest(requestCode);
            }
        }

        private void checkForLocationRequest() {
            mLocationRequest = LocationRequest.create();
            mLocationRequest.setInterval(MIN_UPDATE_INTERVAL);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        }

        // Trigger new location updates at interval
        private void checkForLocationSettings() {

            try {
                // Create LocationSettingsRequest object using location request
                LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
                builder.addLocationRequest(mLocationRequest);

                // Check whether location settings are satisfied
                // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
                SettingsClient settingsClient = LocationServices.getSettingsClient(getContext());
                settingsClient.checkLocationSettings(builder.build())
                        .addOnSuccessListener(getActivity(), new OnSuccessListener<LocationSettingsResponse>() {
                            @Override
                            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                                //Setting is success...
                                Toast.makeText(getContext(), "Enabled the Location successfully.", Toast.LENGTH_SHORT).show();
                                try {
                                    if (
                                            ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                                                    ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                            ) {
                                        // TODO: Consider calling
                                        //    ActivityCompat#requestPermissions
                                        // here to request the missing permissions, and then overriding
                                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                        //                                          int[] grantResults)
                                        // to handle the case where the user grants the permission. See the documentation
                                        // for ActivityCompat#requestPermissions for more details.

                                        requestPermissions(REQUEST_PERMISSIONS_LAST_LOCATION_REQUEST_CODE);
                                        return;
                                    }
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        })
                        .addOnFailureListener(getActivity(), new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                int statusCode = ((ApiException) e).getStatusCode();
                                switch (statusCode) {
                                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                                        try {
                                            // Show the dialog by calling startResolutionForResult(), and check the
                                            // result in onActivityResult().
                                            ResolvableApiException rae = (ResolvableApiException) e;
                                            rae.startResolutionForResult(getActivity(), REQUEST_PERMISSIONS_LOCATION_SETTINGS_REQUEST_CODE);
                                        } catch (IntentSender.SendIntentException sie) {
                                            sie.printStackTrace();
                                        }
                                        break;
                                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                        Toast.makeText(getContext(), "Setting change is not available.Try in another device.", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            if (requestCode == REQUEST_PERMISSIONS_LAST_LOCATION_REQUEST_CODE) {
                if (grantResults.length <= 0) {
                    // If user interaction was interrupted, the permission request is cancelled and you
                    // receive empty arrays.
                    Log.i(TAG, "User interaction was cancelled.");
                } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted.
                    getLastLocation();
                }
            }

            if (requestCode == REQUEST_PERMISSIONS_CURRENT_LOCATION_REQUEST_CODE) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    callCurrentLocation(null);
                }
            }
        }

        public void callLastKnownLocation() {
            try {
                if (
                        ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                                ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.

                    requestPermissions(REQUEST_PERMISSIONS_LAST_LOCATION_REQUEST_CODE);
                    return;
                }
                getLastLocation();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        private void callCurrentLocation(View view) {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

                requestPermissions(REQUEST_PERMISSIONS_CURRENT_LOCATION_REQUEST_CODE);

                return;
            }
            mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    currentLocation = (Location) locationResult.getLastLocation();

                    mTvValueLatitudeReg.setText("" + currentLocation.getLatitude());
                    mTvValueLongitudeReg.setText("" + currentLocation.getLongitude());
                }
            }, Looper.myLooper());
        }

        private void getLastLocation() {

            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                requestPermissions(REQUEST_PERMISSIONS_LAST_LOCATION_REQUEST_CODE);
                return;
            }
            mFusedLocationProviderClient.getLastLocation()
                    .addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                mLastLocation = task.getResult();

                                mTvValueLatitudeReg.setText("" + mLastLocation.getLatitude());
                                mTvValueLongitudeReg.setText("" + mLastLocation.getLongitude());

                                isLocation = true;
                                mLatitude = ""+mLastLocation.getLatitude();
                                mLongitude = ""+mLastLocation.getLongitude();
                                Log.w(TAG,mLastLocation.getLatitude()+", "+mLastLocation.getLongitude());
                            } else {
                                Log.w(TAG,"getLastLocation:exception",task.getException());
                                mTvValueLatitudeReg.setText("No Last known location found. Try current location..!");
                                mTvValueLongitudeReg.setText("No Last known location found. Try current location..!");

                                isLocation = false;
                            }
                        }
                    });
        }
    }
}
