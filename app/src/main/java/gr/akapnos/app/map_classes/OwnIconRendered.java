package gr.akapnos.app.map_classes;


import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import gr.akapnos.app.Helper;
import gr.akapnos.app.TheApplication;
import gr.akapnos.app.objects.Store;


class OwnIconRendered extends DefaultClusterRenderer<MapClusterItem> {
    private Context the_context;
    OwnIconRendered(Context context, GoogleMap map, ClusterManager<MapClusterItem> clusterManager) {
        super(context, map, clusterManager);
        the_context = context;
    }

    @Override
    protected void onBeforeClusterItemRendered(MapClusterItem item, final MarkerOptions markerOptions) {
        Store obj = item.getStore();

        if(Helper.MAP_ICONS_WITH_BADGES) {
            markerOptions.icon(TheApplication.getBitmapDescriptor(the_context, obj));
        }
        else {
            final int icon_id = Helper.iconForStoreType(obj.getStoreType(), true);
            if (icon_id == -1) {
                float color = BitmapDescriptorFactory.HUE_RED;
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(color));
            } else {
//            Drawable drawable = the_context.getResources().getDrawable(icon_id);
//            markerOptions.icon(getMarkerIconFromDrawable(drawable));
                markerOptions.icon(TheApplication.getBitmapDescriptor(icon_id));
            }
        }

        markerOptions.title(item.getTitle());
        markerOptions.snippet(item.getTheSnippet());

        super.onBeforeClusterItemRendered(item, markerOptions);
    }
}