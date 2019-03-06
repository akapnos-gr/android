package gr.akapnos.app.map_classes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;

import gr.akapnos.app.Helper;
import gr.akapnos.app.R;
import gr.akapnos.app.objects.Store;
import gr.akapnos.app.utilities.ImageDL;

public class MapMarkerPopupAdapter implements InfoWindowAdapter {
    private View popup = null;
    private LayoutInflater inflater;
    private Context context;

    public MapMarkerPopupAdapter(LayoutInflater inflater, Context ctx) {
        this.inflater=inflater;
        this.context = ctx;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        Helper.currentlyClickedMarker = marker;
        return null;
    }

    //@SuppressLint("InflateParams")
    @Override
    public View getInfoContents(Marker marker) {
        if (popup == null) { popup = inflater.inflate(R.layout.map_popup, null); }

        final ImageView img= popup.findViewById(R.id.image);
        TextView ttl = popup.findViewById(R.id.title);
        TextView subttl = popup.findViewById(R.id.subtitle);

        setParamWidthToView(img, ViewGroup.LayoutParams.WRAP_CONTENT);

        Store obj = Helper.currentlyClickedClusterItem != null ? Helper.currentlyClickedClusterItem.getStore() : null;

        if(obj != null) {
            String image_url = null;//obj.getImage();
            //Log.v("MyLog", image_url);
            if (image_url != null) {
                if (image_url.length() > 0) {
                    ImageDL.get().setImage(image_url, img, new ImageDL.ImageDLCompletion() {
                        @Override
                        public void completed(String image_url, boolean success) {
                            if(success) {
                                setParamWidthToView(img, 100);

                                if (Helper.currentlyClickedMarker != null && Helper.currentlyClickedMarker.isInfoWindowShown()) {
                                    Helper.currentlyClickedMarker.hideInfoWindow();
                                    Helper.currentlyClickedMarker.showInfoWindow();
                                }
                            }
                        }
                    });
                }
            }
        }
        ttl.setText(marker.getTitle());

        subttl.setText(/*Helper.currentlyClickedClusterItem != null ? Helper.currentlyClickedClusterItem.getTheSnippet() : */marker.getSnippet());

        return popup;
    }

    private void setParamWidthToView(View v, int width) {
        android.view.ViewGroup.LayoutParams params = v.getLayoutParams();
        params.width = width;
        params.height = params.width;
        v.setLayoutParams(params);
    }
}