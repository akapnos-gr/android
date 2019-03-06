package gr.akapnos.app.map_classes;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.ClusterItem;

import gr.akapnos.app.objects.Store;

public class MapClusterItem implements ClusterItem {
    private LatLng mPosition;
    private Marker marker;
    private String mTitle;
    private String mSnippet;
    private Store store;

    public MapClusterItem(Store store) {
        this.store = store;
    }
    public MapClusterItem(Marker m){
        marker = m;
        mPosition = marker.getPosition();
        mTitle = marker.getTitle();
        mSnippet = marker.getSnippet();
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    public LatLng getmPosition() {
        return mPosition;
    }

    public Marker getMarker() {
        return marker;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getSnippet() {
        return mSnippet;
    }
    public String getTheSnippet() {
        if(getMarker() != null) {
            return getMarker().getSnippet();
        }
        else if(getStore() != null) {
            return getStore().getAddress();
        }
        return "";
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }
}
