package gr.akapnos.app.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import java.util.ArrayList;

import gr.akapnos.app.R;
import gr.akapnos.app.Statics;
import gr.akapnos.app.adapters.ViewPagerImageZoomedAdapter;

public class ImageViewDialogFragment extends DialogFragment {
    private static final String POSITION_KEY = "position";
    private ArrayList sourceList;
    private int position;
    public ImageViewDialogFragment() {}

    public static ImageViewDialogFragment newInstance(ArrayList source, int Position) {
        ImageViewDialogFragment fragment = new ImageViewDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(Statics.GET_IMAGE_SOURCE_DIALOG, source);
        args.putInt(POSITION_KEY, Position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, R.style.TransparentDialog);
        if (getArguments() != null) {
            this.sourceList = (ArrayList) getArguments().getSerializable(Statics.GET_IMAGE_SOURCE_DIALOG);
            this.position = getArguments().getInt(POSITION_KEY);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.gallery_view_fragment, container);

        ImageButton close_gallery = view.findViewById(R.id.close_gallery);
        close_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        if(getActivity() != null) {
            ViewPager viewPager = view.findViewById(R.id.view_pager);
            viewPager.setOffscreenPageLimit(2);
            ViewPagerImageZoomedAdapter adapter = new ViewPagerImageZoomedAdapter(sourceList, getActivity());
            viewPager.setAdapter(adapter);
            viewPager.setCurrentItem(position);
        }
        return view;
    }

    /*
    @Override
    protected float getDownScaleFactor() {
        // Allow to customize the down scale factor.
        return (float) 5.0;
    }

    @Override
    protected int getBlurRadius() {
        // Allow to customize the blur radius factor.
        return 4;
    }

    @Override
    protected boolean isActionBarBlurred() {
        // Enable or disable the blur effect on the action bar.
        // Disabled by default.
        return true;
    }

    @Override
    protected boolean isDimmingEnable() {
        // Enable or disable the dimming effect.
        // Disabled by default.
        return true;
    }
*/


//    @Override
//    public void onResume() {
//        super.onResume();
//        //set rotation to sensor dependent
//        if(getActivity() != null ) { getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR); }
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//        if(getActivity() != null) { getActivity().setRequestedOrientation(getResources().getConfiguration().orientation); }
//    }
}
