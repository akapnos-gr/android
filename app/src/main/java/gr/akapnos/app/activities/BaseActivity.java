package gr.akapnos.app.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.iconics.typeface.IIcon;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.ButterKnife;
import gr.akapnos.app.BuildConfig;
import gr.akapnos.app.Helper;
import gr.akapnos.app.LocaleManager;
import gr.akapnos.app.R;
import gr.akapnos.app.Statics;
import gr.akapnos.app.libraries.notification_center.NotificationCenter;

import static gr.akapnos.app.Statics.INDEX_ABOUT_US;
import static gr.akapnos.app.Statics.INDEX_FB_LINK;
import static gr.akapnos.app.Statics.INDEX_REPORT;
import static gr.akapnos.app.Statics.INDEX_SUGGEST;

public class BaseActivity extends AppCompatActivity {
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }
    private Toolbar _toolbar;
    private TextView _activityTitle;
    private boolean support_bar_set = false;
    Drawer drawerResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ButterKnife.bind(this);

        DrawerImageLoader.init(new AbstractDrawerImageLoader() {
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder) {
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                Picasso.get().load(uri).placeholder(placeholder).into(imageView);
            }
            @Override
            public void cancel(ImageView imageView) {
                Picasso.get().cancelRequest(imageView);
            }
        });
    }

    protected Toolbar toolbar() {
        if(_toolbar == null) {
            _toolbar = findViewById(R.id.toolbar);
        }
        return _toolbar;
    }
    protected TextView activityTitle() {
        if(_activityTitle == null) {
            _activityTitle = findViewById(R.id.title);
        }
        return _activityTitle;
    }
    protected void setTitle(String title) {
        if(activityTitle() != null) {
            activityTitle().setText(title);
        }
    }
    protected void setupActionBar() {
        if(getSupportActionBar() == null) {
            setSupportActionBar(this.toolbar());
        }
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
//    public void setBackButton() {
//        Toolbar toolbar = toolbar();
//        if (toolbar != null) {
//            setSupportActionBar(toolbar);
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//            getSupportActionBar().setDisplayShowHomeEnabled(true);
//            getSupportActionBar().setTitle("");
//        }
//    }


    private PrimaryDrawerItem newPrimaryItem(int title, int id, IIcon icon, Drawable drawable, int text_color) {
        return newPrimaryItem(title, id, icon, drawable, text_color, null);
    }
    private PrimaryDrawerItem newPrimaryItem(int title, int id, IIcon icon, Drawable drawable, int text_color, String title_string) {
        PrimaryDrawerItem item = new PrimaryDrawerItem().withIdentifier(id).withTextColor(text_color).withSelectable(false);
        if(title_string != null) { item.withName(title_string); } else { item.withName(title); }
        if (icon != null) { item.withIcon(icon).withIconColor(text_color).withTextColor(text_color); }
        else if (drawable != null) { item.withIcon(drawable).withTextColor(text_color); }
        return item;
    }

    protected void onCreateDrawer() {
        if(!(this instanceof SearchStoresActivity)) {
            return;
        }
        if(this.toolbar() != null && !support_bar_set) {
            setSupportActionBar(this.toolbar());
        }
        LayoutInflater inflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);



        int text_color = getResources().getColor(R.color.dark_gray);
        ArrayList<IDrawerItem> drawer_items = new ArrayList<>();

        drawer_items.add(newPrimaryItem(-1, INDEX_SUGGEST, null, getResources().getDrawable(R.drawable.menu_add_store), text_color,"Προτείνετε κατάστημα"));
        drawer_items.add(newPrimaryItem(-1, INDEX_REPORT, null, getResources().getDrawable(R.drawable.menu_report_store), text_color,"Αναφορά μη άκαπνου καταστήματος"));
        drawer_items.add(newPrimaryItem(-1, INDEX_ABOUT_US, null, getResources().getDrawable(R.drawable.menu_about_us), text_color,"Για εμάς"));
        drawer_items.add(newPrimaryItem(-1, INDEX_FB_LINK, null, getResources().getDrawable(R.drawable.menu_facebook_link), text_color,"Γίνετε φίλοι μας στο Facebook"));


//        drawer_items.add(new CustomDrawerDividerItem());
//
//        drawer_items.add(new SecondaryDrawerItem().withName(R.string.INFORMATION).withIdentifier(INDEX_INFORMATION).withSelectable(false).withTextColor(text_color));

        ViewGroup drawerFooter = null;
        if(inflater != null) {
            drawerFooter = (ViewGroup)inflater.inflate(R.layout.drawer_footer, null);
            TextView footerText = drawerFooter.findViewById(R.id.text);
            footerText.setTextColor(getResources().getColor(R.color.gray));
            footerText.setText(String.format("Version: %s", BuildConfig.VERSION_NAME));
        }

        drawerResult = new DrawerBuilder(this)
                .withActivity(this)
                .withToolbar(this.toolbar())
                .withDisplayBelowStatusBar(true)
                .withActionBarDrawerToggle(true)
                .withDrawerItems(drawer_items)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
//                        Intent intent = null;
                        long id = drawerItem.getIdentifier();

                        if (id == INDEX_SUGGEST) {
                            Statics.openURL(BaseActivity.this, "https://www.akapnos.gr/add-your-listing", false);
                        } else if (id == INDEX_REPORT) {
                            Statics.openURL(BaseActivity.this, "https://www.akapnos.gr/%CE%B1%CE%BD%CE%B1%CF%86%CE%BF%CF%81%CE%AC-%CE%BA%CE%B1%CF%84%CE%B1%CF%83%CF%84%CE%AE%CE%BC%CE%B1%CF%84%CE%BF%CF%82", false);
                        } else if (id == INDEX_ABOUT_US) {
                            Statics.openURL(BaseActivity.this, "https://www.akapnos.gr/gia-mas", false);
                        } else if (id == INDEX_FB_LINK) {
                            Statics.openURL(BaseActivity.this, "https://www.facebook.com/akapnos.gr", false);
                        }
//                        if (intent != null) {
//                            drawerResult.closeDrawer();
//                            startActivity(intent);
//                        }
                        return false;
                    }
                })
                .withSelectedItem(-1)
                .withStickyFooter(drawerFooter)
                .build();

        //Set custom header
        if (inflater != null) {
            View drawerHeader = inflater.inflate(R.layout.drawer_header, null);
            drawerResult.setHeader(drawerHeader, false);

            CardView card = drawerHeader.findViewById(R.id.card);
            card.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    Helper.showToast("Updating store list...");
                    Helper.downloadStores(BaseActivity.this, true, new Runnable() {
                        @Override
                        public void run() {
                            Helper.showToast("Stores list updated, please restart app");
                        }
                    });
                    return false;
                }
            });
        }

        if(getSupportActionBar() != null && !support_bar_set) {
            support_bar_set = true;
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            drawerResult.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(this instanceof SearchStoresActivity) {
            onCreateDrawer();
        }
    }


    @Override
    public void onBackPressed() {
        if (drawerResult != null && drawerResult.isDrawerOpen()) {
            drawerResult.closeDrawer();
            return;
        }
        super.onBackPressed();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle SavedInstanceState) {
        super.onSaveInstanceState(SavedInstanceState);
        SavedInstanceState.clear();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cleanupNotificationListeners();
    }
    //======== Notification Listeners helper ==================================
    private ArrayList<BroadcastReceiver> notificationListeners;
    public void listenToNotification(NotificationCenter.NotificationType type, BroadcastReceiver receiver) {
        if(receiver != null) {
            if(notificationListeners == null) { notificationListeners = new ArrayList<>(); }
            notificationListeners.add(receiver);
            NotificationCenter.addObserver(this, type, receiver);
        }
    }
    private void cleanupNotificationListeners() {
        if(notificationListeners != null) {
            for (BroadcastReceiver receiver : notificationListeners) {
                NotificationCenter.removeObserver(this, receiver);
            }
        }
    }
    //====================================================================

    public boolean hasLocationPermissions() {
        return hasGrantedPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
                hasGrantedPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
    }
    private boolean hasGrantedPermission(String type) {
        return ActivityCompat.checkSelfPermission(Helper.appCtx(), type) == PackageManager.PERMISSION_GRANTED;
    }
}