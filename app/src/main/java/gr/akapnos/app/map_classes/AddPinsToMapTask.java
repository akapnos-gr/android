package gr.akapnos.app.map_classes;

import android.app.Activity;
import android.location.Location;
import android.os.AsyncTask;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;

import gr.akapnos.app.objects.Store;
import gr.akapnos.app.utilities.LocatorGoogle;
import trikita.log.Log;

public class AddPinsToMapTask extends AsyncTask<Void, Void, Void> {
    private ArrayList<Store> entries;
    private Activity activity;
    private GoogleMap theMap;
    private ClusterManager<MapClusterItem> cluster_manager;

    private boolean use_distance_limit;
    private int store_category_to_show;
    public AddPinsToMapTask(ArrayList<Store> entries, Activity ctx, GoogleMap map, boolean _use_distance_limit, int _store_category_to_show, ClusterManager.OnClusterItemClickListener<MapClusterItem> listener) {
        this.entries = entries;
        this.activity = ctx;
        this.theMap = map;
        this.store_category_to_show = _store_category_to_show;

        if(theMap != null) {
            theMap.clear();
            use_distance_limit = _use_distance_limit;

            getCluster_manager().setOnClusterItemClickListener(listener);

//deprecated        this.theMap.setOnCameraChangeListener(cluster_manager);
            final CameraPosition[] mPreviousCameraPosition = {null};
            this.theMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                @Override
                public void onCameraIdle() {
                    CameraPosition position = theMap.getCameraPosition();
                    if (mPreviousCameraPosition[0] == null || mPreviousCameraPosition[0].zoom != position.zoom) {
                        mPreviousCameraPosition[0] = theMap.getCameraPosition();
                        getCluster_manager().cluster();
                    }
                }
            });
            this.theMap.setOnMarkerClickListener(cluster_manager);
        }

        doInBackground();
    }

    public ClusterManager<MapClusterItem> getCluster_manager() {
        if(cluster_manager == null) {
            cluster_manager = new ClusterManager<>(this.activity, this.theMap);
        }
        return cluster_manager;
    }

    @Override
    protected Void doInBackground(Void... params) {
        if(theMap == null) {
            return null;
        }

        final ArrayList<Marker> mMarkerArray = new ArrayList<>();
        final Location user_location = LocatorGoogle.getInstance().getUserLocation();

        final int finalDistance_limit = 1000;
        this.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(theMap == null) {
                    return;
                }

                for (final Store entry : entries) {
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(entry.getLatLng());
                    markerOptions.title(entry.getTitle());
                    markerOptions.snippet(entry.getAddress());

                    Marker marker = theMap.addMarker(markerOptions);
                    marker.setVisible(false);
                    MapClusterItem item = new MapClusterItem(marker);
                    item.setStore(entry);
                    getCluster_manager().addItem(item);

                    if((store_category_to_show == 0 || store_category_to_show != 0) &&
                            (mMarkerArray.size() == 0 //first item in array is the nearest
                                    || !use_distance_limit //add all
                                    || (user_location == null ||
                                    (entry.distance_to_user() > -1 && entry.distance_to_user() <= finalDistance_limit))
                            )
                            ) {
                        mMarkerArray.add(marker);
                    }
                }
            }
        });

        Log.v("MARKERS=" + mMarkerArray.size());
//        if(Helper.isDebug() && mMarkerArray.size() < 100) {
//            for(Marker marker : mMarkerArray) {
//                Log.v("MARKER=" + marker.getTitle());
//            }
//        }

        try {
            if (mMarkerArray.size() > 0) {
                int padding = 80; // offset from edges of the map in pixels
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (Marker marker : mMarkerArray) {
                    builder.include(marker.getPosition());
                }
                LatLngBounds bounds = builder.build();

                if (user_location != null) {
                    LatLng currentLoc = LocatorGoogle.getInstance().getUserLatLng();

                    double lat = currentLoc == null ? 0 : currentLoc.latitude;
                    double lon = currentLoc == null ? 0 : currentLoc.longitude;


                    //make sure that centre location doesn't change
                    double deltaLat = Math.max(Math.abs(bounds.southwest.latitude - lat), Math.abs(bounds.northeast.latitude - lat));
                    double deltaLon = Math.max(Math.abs(bounds.southwest.longitude - lon), Math.abs(bounds.northeast.longitude - lon));

                    LatLngBounds.Builder displayBuilder = new LatLngBounds.Builder();
                    if(currentLoc != null) { displayBuilder.include(currentLoc); } //not necessary but hey
                    displayBuilder.include(new LatLng(lat + deltaLat, lon + deltaLon));
                    displayBuilder.include(new LatLng(lat - deltaLat, lon - deltaLon));

                    bounds = displayBuilder.build();
                }

                this.theMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
            }
        }
        catch (Exception e) {
            Log.e("PinsException: " + e);
        }
        this.theMap.setInfoWindowAdapter(getCluster_manager().getMarkerManager());
        getCluster_manager().getMarkerCollection().setOnInfoWindowAdapter(new MapMarkerPopupAdapter(this.activity.getLayoutInflater(), this.activity));
        getCluster_manager().setRenderer(new OwnIconRendered(this.activity, this.theMap, getCluster_manager()));

        return null;
    }
}