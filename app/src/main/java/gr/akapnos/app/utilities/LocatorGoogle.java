package gr.akapnos.app.utilities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import gr.akapnos.app.Helper;
import gr.akapnos.app.R;
import trikita.log.Log;

import ru.alexbykov.nopermission.PermissionHelper;

//Uses: compile 'ru.alexbykov:nopermission:1.1.2'

public class LocatorGoogle implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private static LocatorGoogle _instance;

    public static LocatorGoogle getInstance() {
        if (_instance == null) { _instance = new LocatorGoogle(Helper.appCtx()); }
        return _instance;
    }

    private Location user_location;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Context context;
    Runnable _block, _external_block;
    PermissionHelper permissionHelper;

    private LocatorGoogle(Context context) {
        super();
        this.context = context;
    }

    public PermissionHelper init(Activity activity) {
        return init(activity, null);
    }
    public PermissionHelper init(Activity activity, final Runnable external_block) {
        _external_block = external_block;
        return LocatorGoogle.getInstance().getTheLocation(activity, new Runnable() {
            @Override
            public void run() {
                LocatorGoogle.getInstance().getLocation();
            }
        });
    }

    private synchronized void buildGoogleApiClient() {
        logNow("Building GoogleApiClient"); //NON-NLS
        mGoogleApiClient = new GoogleApiClient.Builder(this.context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    private void getLocation() {
        logNow("Getting location..."); //NON-NLS
        if (mGoogleApiClient == null) {
            buildGoogleApiClient();
            logNow("Connect GoogleAPIClient now"); //NON-NLS
            mGoogleApiClient.connect();
        }
        else if(_external_block != null) {
            _external_block.run();
        }
    }

    private void createLocationRequest() {
        try {
            //remove location updates so that it resets
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this); //Import should not be **android.Location.LocationListener**
            //import should be **import com.google.android.gms.location.LocationListener**;

            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(10000);
            mLocationRequest.setFastestInterval(5000);
            mLocationRequest.setSmallestDisplacement(10);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            //restart location updates with the new interval
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } catch (SecurityException e) {
            logNow("Location services denied 0"); //NON-NLS
        }
    }

    public void gotPermission() {
        logNow("Got permission"); //NON-NLS
        if(_block != null) {
            _block.run();
        }
        if(_external_block != null) {
            _external_block.run();
        }
    }

    private PermissionHelper getTheLocation(final Activity activity, final Runnable block) {
        if(activity == null || activity.isFinishing()) {
            logNow("Null activity or finishing"); //NON-NLS
            if(block != null) { block.run(); }
            return null;
        }
        permissionHelper = new PermissionHelper(activity); //prepei na dimiourgei kainourgio gia na allazei to activity

        logNow("Asking user..."); //NON-NLS
        permissionHelper.check(Manifest.permission.ACCESS_FINE_LOCATION)
                .withDialogBeforeRun(R.string.LOCATION_BEFORE_TRY_TITLE, R.string.LOCATION_BEFORE_TRY_MESSAGE, R.string.OK)
                .setDialogPositiveButtonColor(R.color.keyColor)
                .onSuccess(new Runnable() {
                    @Override
                    public void run() {
                        logNow("Has Location permissions"); //NON-NLS
                        if(block != null) { block.run(); }
                    }
                })
                .onDenied(new Runnable() {
                    @Override
                    public void run() {
                        logNow("Location permissions DENIED"); //NON-NLS
                        Helper.createAlertDialog(activity,
                                activity.getResources().getString(R.string.PERMISSION_DENIED),
                                activity.getResources().getString(R.string.LOCATION_PERMISSION_DENIED_MSG),
                                activity.getResources().getString(R.string.OK),
                                null,null,null);
                        if(block != null) { block.run(); }
                    }
                })
                .onNeverAskAgain(new Runnable() {
                    @Override
                    public void run() {
                        Helper.showToast(activity.getResources().getString(R.string.LOCATION_PERMISSIONS_DENIED), Toast.LENGTH_LONG);
                        if(block != null) { block.run(); }
                    }
                })
                .run();
        return permissionHelper;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onConnected(Bundle connectionHint) {
        try {
            createLocationRequest();
            user_location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);  //may return **null** because, I can't guarantee location has been changed immmediately
            gotPermission();
            logNow("onConnected: UserLocation: " + user_location + " - isConnected: " + mGoogleApiClient.isConnected()); //NON-NLS NON-NLS
        } catch (Exception e) {
            logNow("Location services denied - " + e.getLocalizedMessage()); //NON-NLS
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        logNow("Connection suspended"); //NON-NLS
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        logNow("Connection failed"); //NON-NLS
    }

    @Override
    public void onLocationChanged(Location location) {
        user_location = location;
        if (user_location != null) {
            logNow("LocationChanged: " + String.valueOf(user_location.getLatitude()) + ", " + String.valueOf(user_location.getLongitude())); //NON-NLS
        }
    }

    private void logNow(String str) {
        boolean log = Helper.isDebug();
        if(log) {
            Log.i(str);
        }
    }



    public Location getUserLocation() {
        return getUserLocation(false);
    }
    public Location getUserLocation(boolean required) {
        if(user_location == null && required) {
            logNow("ERROR: REQUIRED LOCATION BUT IS NULL"); //NON-NLS
        }
        return user_location;
    }

    public LatLng getUserLatLng() {
        if(getUserLocation() != null) {
            return new LatLng(getUserLocation().getLatitude(), getUserLocation().getLongitude());
        }
        return new LatLng(0,0);
    }
}