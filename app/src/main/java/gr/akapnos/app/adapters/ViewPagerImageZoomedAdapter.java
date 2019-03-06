package gr.akapnos.app.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import gr.akapnos.app.Helper;
import gr.akapnos.app.R;
import gr.akapnos.app.Statics;
import gr.akapnos.app.layout.TouchImageView;
import gr.akapnos.app.utilities.ImageDL;

public class ViewPagerImageZoomedAdapter extends PagerAdapter {

    private ArrayList<Object> itemObjs;
    private Context context;
    private LayoutInflater inflater;


    public ViewPagerImageZoomedAdapter(ArrayList<Object> itemObjs, Context context) {
        this.itemObjs = itemObjs;
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public int getCount() {
        return itemObjs == null ? 0 : itemObjs.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }


    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        final View v = inflater.inflate(R.layout.viewpager_img_item_zoomed, null);
        String image = Statics.imageFrom(itemObjs.get(position));

        final TouchImageView imageview = v.findViewById(R.id.image);
        final ProgressBar progressBar = v.findViewById(R.id.loadingProgress);
        try {
            ImageDL.get().setImage(image, imageview, new ImageDL.ImageDLCompletion() {
                @Override
                public void completed(String image_url, boolean success) {
                    Helper.setVisibilityTo(progressBar, false);
                    Helper.setVisibilityTo(imageview, success);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        container.addView(v);
        return v;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((RelativeLayout) object);
    }
}
