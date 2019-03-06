package gr.akapnos.app.utilities;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;

//Image Download Wrapper
public class ImageDL {
    private static ImageDL _instance;
    public static ImageDL get() {
        if (_instance == null) { _instance = new ImageDL(); }
        return _instance;
    }
    public interface ImageDLCompletion {
        void completed(String image_url, boolean success);
    }

    //from File
    public void setImage(@NonNull File file, ImageView imageView) {
        setImage(file, imageView, null);
    }
    public void setImage(@NonNull File file, ImageView imageView, ImageDLCompletion block) {
        Uri uri = Uri.fromFile(file);
        String uri_string = uri.toString();
        setImage(uri_string, imageView, block);
    }

    //from url string
    public void setImage(String url, ImageView imageView) {
        setImage(url, imageView, null);
    }
    public void setImage(String url, ImageView imageView, final ImageDLCompletion block) {
//        if(block != null) { block.completed(url, false); } //debug only, try without loading images
        if(url != null && url.length() > 0) {
            cancelRequest(imageView);
            if(block == null) {
                Picasso.get().load(url).into(imageView);
            }
            else {
                final String image_url = url;
                Picasso.get().load(image_url).into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        block.completed(image_url, true);
                    }

                    @Override
                    public void onError(Exception e) {
                        block.completed(image_url, false);
                    }
                });
            }
        }
    }

    public void cancelRequest(ImageView imageView) {
        Picasso.get().cancelRequest(imageView);
    }
}