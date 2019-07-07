package gr.akapnos.app.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.ClusterManager;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Pattern;

import br.com.mauker.materialsearchview.MaterialSearchView;
import butterknife.BindView;
import butterknife.ButterKnife;
import gr.akapnos.app.Helper;
import gr.akapnos.app.R;
import gr.akapnos.app.Statics;
import gr.akapnos.app.TheApplication;
import gr.akapnos.app.libraries.custombottomsheetbehavior.BottomSheetBehaviorGoogleMapsLike;
import gr.akapnos.app.libraries.custombottomsheetbehavior.MergedAppBarLayoutBehavior;
import gr.akapnos.app.map_classes.AddPinsToMapTask;
import gr.akapnos.app.map_classes.DirectionsGetter;
import gr.akapnos.app.map_classes.MapClusterItem;
import gr.akapnos.app.objects.Store;
import gr.akapnos.app.utilities.ImageDL;
import gr.akapnos.app.utilities.LocatorGoogle;
import gr.akapnos.app.utilities.RunnableArg;
import ru.alexbykov.nopermission.PermissionHelper;
import trikita.log.Log;

import static gr.akapnos.app.objects.Store.typeBAR;
import static gr.akapnos.app.objects.Store.typeCAFE;
import static gr.akapnos.app.objects.Store.typeFAVORITE;
import static gr.akapnos.app.objects.Store.typeNONE;
import static gr.akapnos.app.objects.Store.typeRESTAURANT;
import static java.lang.Math.abs;
import static java.lang.Math.min;


public class SearchStoresActivity extends BaseActivity {
    PermissionHelper permissionHelper;
    View.OnTouchListener list_drag_listener;

    private Menu the_menu;
    private String temp_tel, search_string;
    private GoogleMap google_map;
    private BottomSheetBehaviorGoogleMapsLike behavior;
    private MergedAppBarLayoutBehavior mergedAppBarLayoutBehavior;
    private int store_category_to_show = 0;
    private boolean mapLoaded = false;
    private boolean permissions_finished = false;
    private boolean stores_got = false;
    private boolean opened_detail_from_list = false;
    private boolean disable_drag = false;
    private ArrayList<Integer> filtersData;
    private MapListAdapter map_list_adapter;
    private FiltersAdapter filtersAdapter;
    @BindView(R.id.coordinatorLayout) CoordinatorLayout coordinatorLayout;
    @BindView(R.id.bottom_sheet) View bottomSheet;
    @BindView(R.id.merged_app_bar_layout) AppBarLayout mergedAppBarLayout;

    @BindView(R.id.top_layout) RelativeLayout top_layout;
    @BindView(R.id.top) View top;
    @BindView(R.id.map_searchView) MaterialSearchView searchView;
    @BindView(R.id.listBtn) Button listBtn;
    @BindView(R.id.listContainer) View list_container;
    @BindView(R.id.root) LinearLayout bottom_root;
    @BindView(R.id.walking_time) TextView walking_time;
    @BindView(R.id.distance_container) ViewGroup distance_container;
    @BindView(R.id.mode) ImageView mode;
    @BindView(R.id.clearSearchContainer) CardView clearSearchContainer;
    @BindView(R.id.clearSearchText) TextView clearSearchText;
    @BindView(R.id.list_recycler) RecyclerView list_recycler;
    @BindView(R.id.map_fragment) MapView mMapView;
    @BindView(R.id.map_fab) View map_fab;

    @BindView(R.id.bottom_sheet_title) TextView title;
    @BindView(R.id.bottom_sheet_subtitle) TextView address;
    @BindView(R.id.store_type) TextView store_type;
    @BindView(R.id.btn_phone) View btn_phone;
    @BindView(R.id.btn_web) View btn_web;
    @BindView(R.id.btn_tripadvisor) View btn_tripadvisor;
    @BindView(R.id.btn_etable) View btn_etable;

    @BindView(R.id.fav_button) ImageView fav_button;
    @BindView(R.id.store_image) ImageView store_image;
    @BindView(R.id.image_progress) ProgressBar image_progress;

    @BindView(R.id.loading_view) RelativeLayout loading_view;

//    @BindView(R.id.filters_container) RelativeLayout filters_container;
    @BindView(R.id.filters_recycler) RecyclerView filters_recycler;


    //region Basic
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_search_stores);
        super.onCreate(savedInstanceState);

        TheApplication.initCache();
        if(getSupportActionBar() == null) { setSupportActionBar(this.toolbar()); }
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();// needed to get the map to display immediately
        try {
            MapsInitializer.initialize(Helper.appCtx());
        } catch (Exception e) {
            e.printStackTrace();
        }


        behavior = BottomSheetBehaviorGoogleMapsLike.from(bottomSheet);
        behavior.setHideable(true);
        behavior.setPeekHeight((int)getResources().getDimension(R.dimen.bottom_sheet_peek_height));
        behavior.setAnchorPoint((int)getResources().getDimension(R.dimen.anchor_point));

        behavior.addBottomSheetCallback(new BottomSheetBehaviorGoogleMapsLike.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED:
//                        Log.d("STATE_COLLAPSED");
                        showHideMenu(true);
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_DRAGGING:
//                        Log.d("STATE_DRAGGING");
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_EXPANDED:
                        bottomSheet.getLayoutParams().height = Helper.screenHeight();
//                        Log.d("STATE_EXPANDED");
                        showHideMenu(false);
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT:
//                        Log.d("STATE_ANCHOR_POINT");
                        showHideMenu(false);
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN:
//                        Log.d("STATE_HIDDEN");
                        showHideMenu(true);
                        break;
                    default:
//                        Log.d("STATE_SETTLING");
                        break;
                }
                bottomSheet.requestLayout();
                bottomSheet.invalidate();
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {}
        });

        mergedAppBarLayoutBehavior = MergedAppBarLayoutBehavior.from(mergedAppBarLayout);
        mergedAppBarLayoutBehavior.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                behavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED);
            }
        });
        behavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN);

        ViewTreeObserver vto = bottom_root.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int screenHeight = Helper.screenHeight();
                int newHeight = min((int)(screenHeight * 0.6f), abs(screenHeight - bottom_root.getMeasuredHeight()));
                behavior.setAnchorPoint(newHeight);
            }
        });


        filtersData = new ArrayList<>();
        filtersData.add(typeNONE);
        filtersData.add(typeFAVORITE);
        filtersData.add(typeCAFE);
        filtersData.add(typeBAR);
        filtersData.add(typeRESTAURANT);

        filters_recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        filtersAdapter = new FiltersAdapter();
        filters_recycler.setAdapter(filtersAdapter);

        showHideClearSearch();

        showLoading(true);

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                Log.v("OnMapReady");
                google_map = googleMap;
                google_map.getUiSettings().setMapToolbarEnabled(false);
                google_map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(38.369186, 24.032198), 6.0f));
                google_map.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                    @Override
                    public void onMapLoaded() {
                        mapLoaded = true;
                        if(permissions_finished && stores_got) {
                            Log.v("DisplayMarkers - OnMapLoaded");
                            displayMarkers();
                        }
                    }
                });
                google_map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        behavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_EXPANDED);
                    }
                });
                google_map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        SearchStoresActivity.this.behavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN);
                    }
                });
//googleMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(this));
                permissionHelper = LocatorGoogle.getInstance().init(SearchStoresActivity.this, new Runnable() {
                    @Override
                    public void run() {
                        permissions_finished = true;
                        Log.v("GotLocationPermissions: " + hasLocationPermissions());
                        if(hasLocationPermissions()) {
                            if (LocatorGoogle.getInstance().getUserLocation() != null) {
                                google_map.moveCamera(CameraUpdateFactory.newLatLngZoom(LocatorGoogle.getInstance().getUserLatLng(), 16.0f));
                            }
                            gotLocation();
                        }
                        if(mapLoaded && stores_got) {
                            Log.v("DisplayMarkers - GotUserLocation: " + LocatorGoogle.getInstance().getUserLocation());
                            displayMarkers();
                        }
                    }
                });
            }
        });

        Helper.downloadStores(this, new Runnable() {
            @Override
            public void run() {
                stores_got = true;
                startNow();
                if(permissions_finished && mapLoaded) {
                    Log.v("DisplayMarkers - GotStores");
                    displayMarkers();
                }
            }
        });
    }

    private void showLoading(boolean show) {
        Helper.setVisibilityTo(loading_view, show);
    }

    private void startNow() {
        showLoading(false);

        setupFilteredStores();

        if(list_recycler != null) {
            map_list_adapter = new MapListAdapter();
            list_recycler.setVerticalScrollBarEnabled(true);
            list_recycler.setAdapter(map_list_adapter);
            final LinearLayoutManager layoutManager = new LinearLayoutManager(Helper.appCtx());
            list_recycler.setLayoutManager(layoutManager);
            list_drag_listener = new View.OnTouchListener() {
                private boolean dragging = false;
                float mInitialPoint, mCurrentPoint;

                public void reset() {
                    dragging = false;
                    mInitialPoint = 0;
                    mCurrentPoint = 0;
                }

                //https://stackoverflow.com/questions/47355286/recyclerview-scroll-to-drag-and-dissmiss
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(disable_drag) {
                        return false;
                    }
                    if (event.getAction() == MotionEvent.ACTION_UP && dragging) {
                        dragging = false;
                        mInitialPoint = 0;
                        move(0);
                        return true;
                    }

                    if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                        if (!dragging && !list_recycler.canScrollVertically(-1)) {
                            mInitialPoint = event.getRawY();
                            mCurrentPoint = event.getRawY();
                            dragging = true;
                            return true;
                        } else if (dragging && event.getAction() == MotionEvent.ACTION_MOVE) {
                            mCurrentPoint = event.getRawY();

                            float dy = mInitialPoint - mCurrentPoint;
                            if (dy < 0) {
                                move(-dy);
                            } else {
                                move(0);
                                dragging = false;
                                return false;
                            }
                            return true;
                        }
                    }
                    return false;
                }

                private void move(float dy) {
                    list_container.setTranslationY(dy);
                    if(dy > 200) {
                        disable_drag = true;
                        showHideList();

                        reset();
                    }
                }
            };
            list_recycler.setOnTouchListener(list_drag_listener);
        }


        clearSearchContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search_now("");
            }
        });
        listBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHideList();
            }
        });
        top.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHideList();
            }
        });

        top_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                behavior.setState(
                        behavior.getState() == BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED ?
                                BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT :
                                BottomSheetBehaviorGoogleMapsLike.STATE_EXPANDED);
            }
        });

        setupSearch();
    }
    private void showHideMenu(boolean show) {
        if(the_menu != null) {
            the_menu.setGroupVisible(R.id.map_menu_group, show);
        }
    }

    private boolean showingList = false;
    private void showHideList() {
        boolean is_visible = list_container.getVisibility() == View.VISIBLE;
        if(!is_visible && (stores_list == null || stores_list.size() == 0)) {
            return;
        }
        is_visible = !is_visible;
        showingList = is_visible;

        Animation animation = AnimationUtils.loadAnimation(this, is_visible ? R.anim.bottom_up : R.anim.bottom_down);
        final boolean finalIs_visible = is_visible;
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                if(!finalIs_visible) {
                    list_container.setTranslationY(0);
                }
            }
            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        list_container.startAnimation(animation);

        if(is_visible) {
            disable_drag = false;
        }
        Helper.setVisibilityTo(list_container, is_visible);
        updateListBtn();
    }
    private void updateListBtn() {
        int count = stores_list == null ? 0 : stores_list.size();
        listBtn.setText(String.format(Locale.getDefault(), "%d %s",count, count == 1 ? "Κατάστημα" : "Καταστήματα"));
    }
    //endregion

    //region Map functions

    private int current_data_category = -1;
    private String current_data_search_string = "";
    private ArrayList<Store> stores_list;
    Location current_user_location = null;

    private void setupFilteredStores() {
        search_string = Helper.safeString(search_string);
        if(current_data_category == store_category_to_show &&
                current_data_search_string.equals(search_string) &&
                current_user_location == LocatorGoogle.getInstance().getUserLocation()
                ) {
            return; //same search criteria
        }
        current_data_category = store_category_to_show;
        current_data_search_string = search_string;
        current_user_location = LocatorGoogle.getInstance().getUserLocation();

        Log.e("Filters: " + current_data_category + " : " + current_data_search_string);

        ArrayList<Store> list1;
        int cat_to_show = filtersData.get(store_category_to_show);

        if(cat_to_show == typeNONE) {
            list1 = Helper.getStores();
        }
        else {
            list1 = new ArrayList<>();
            boolean fav = cat_to_show == typeFAVORITE;
            for (Store store : Helper.getStores()) {
                boolean ok = false;

                if(fav) {
                    ok = Helper.isFavorite(store);
                }
                else {
                    int store_type = store.getStoreType();
                    switch (cat_to_show) {
                        case 1:
                            ok = store_type == typeBAR;
                            break;
                        case 2:
                            ok = store_type == typeCAFE;
                            break;
                        case 3:
                            ok = store_type == typeRESTAURANT;
                            break;
                        default:
                            break;
                    }
                }
                if (ok) {
				    list1.add(store);
                }

                if(fav && list1.size() == Helper.favStores().size()) {
                    break;
                }
            }
        }

        boolean search_in_data = true;//!Helper.connectionAvailable(this);

        if(search_in_data) {
            ArrayList<Store> list2;
            if (search_string != null && search_string.length() > 0) {
                list2 = new ArrayList<>();

                search_string = deAccent(search_string.toLowerCase());
                if (list1 != null) {
                    for (Store store : list1) {
                        String test_string = deAccent(store.getTitle().toLowerCase());

                        boolean found = test_string.contains(search_string);
                        if (!found) {
                            test_string = deAccent(store.getAddress().toLowerCase());
                            found = test_string.contains(search_string);
                        }
                        if (!found) {
                            test_string = deAccent(store.getAddress().toLowerCase());
                            found = test_string.contains(search_string);
                        }
                        if (!found) {
                            test_string = deAccent(store.distanceToUserDesc().toLowerCase());
                            found = test_string.contains(search_string);
                        }
                        if (!found) {
                            test_string = deAccent(store.getTripadvisor().toLowerCase());
                            found = test_string.contains(search_string);
                        }

                        if (found) {
                            list2.add(store);
                        }
                    }
                }
            } else {
                list2 = list1;
            }

            Helper.sortStores(list2);
            stores_list = list2;
        }
        else {
            Helper.sortStores(list1);
            stores_list = list1;
        }
        updateListBtn();

        Log.e("StoresCount: " + stores_list.size() + " -- " + " Closest=" + (stores_list != null && stores_list.size() > 0 ? stores_list.get(0).getTitle() : "NONE"));
    }
    public String deAccent(String str) {
        String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("");
    }

    private void displayMarkers() {
        setupFilteredStores();


        if(stores_list.size() == 0 && showingList) {
            showHideList();
        }

        ArrayList<Store> storesListMap = new ArrayList<>();
        if(stores_list != null) {
            for (Store store : stores_list) {
                if (store.has_location()) {
                    storesListMap.add(store);
                }
            }
        }



        boolean use_distance_limit = search_string == null || search_string.length() == 0;
        new AddPinsToMapTask(
                storesListMap,
                SearchStoresActivity.this,
                google_map,
                use_distance_limit,
                store_category_to_show,
                new ClusterManager.OnClusterItemClickListener<MapClusterItem>() {
                    @Override
                    public boolean onClusterItemClick(MapClusterItem mapClusterItem) {
                        Log.v("CLICKED SINGLE ITEM -- " + mapClusterItem);
                        Helper.currentlyClickedClusterItem = mapClusterItem;
                        selectedStore(mapClusterItem);
                        return false;
                    }
                });
    }
    //endregion

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if(requestCode == REQUEST_PHONE_CALL) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                make_call_now(temp_tel);
                return;
            }
        }
        try {
            if (permissionHelper != null) {
                permissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
        catch(Exception ignored) {}
    }

    private static final int REQUEST_PHONE_CALL = 1232;
    private void make_call(final String tel) {
        Log.v("MakeCall: " + tel);
        Helper.createAlertDialog(this, getString(R.string.CALL_PHONE), tel, getResources().getString(R.string.YES), getResources().getString(R.string.CANCEL), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (ContextCompat.checkSelfPermission(SearchStoresActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    temp_tel = tel;
                    ActivityCompat.requestPermissions(SearchStoresActivity.this, new String[]{Manifest.permission.CALL_PHONE},REQUEST_PHONE_CALL);
                }
                else
                {
                    make_call_now(tel);
                }
            }
        }, null);
    }

    @SuppressLint("MissingPermission")
    private void make_call_now(String tel) {
        startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + tel)));
    }


    @SuppressLint("MissingPermission")
    private void gotLocation() {
        google_map.setMyLocationEnabled(true);
        google_map.getUiSettings().setMyLocationButtonEnabled(true);
    }



    private void selectedStore(MapClusterItem item) {
        if (item != null) {
            selectedStoreNow(item.getStore(), item.getPosition());
        }
    }
    private void selectedStoreNow(final Store store) {
        if (store != null) {
            selectedStoreNow(store, store.getLatLng());
        }
    }
    private void selectedStoreNow(final Store store, LatLng position) {
        if (store != null) {
            mergedAppBarLayoutBehavior.setToolbarTitle(store.getTitle());
            mergedAppBarLayoutBehavior.setToolbarSubTitle(store.getAddress());

            Helper.setVisibilityTo(distance_container, false);

            Location user_location = LocatorGoogle.getInstance().getUserLocation();
            if(user_location != null) {
                DirectionsGetter directionsGetter = new DirectionsGetter();
                LatLng origin = new LatLng(user_location.getLatitude(), user_location.getLongitude());
                final boolean is_walking = directionsGetter.isWalkingDistance(origin, position);
                directionsGetter.get_directions_time(origin, position, is_walking, new RunnableArg() {
                    @Override
                    public void run() {
                        boolean has_result = getResult().length() > 0;
                        Helper.setVisibilityTo(distance_container, has_result);
                        if(has_result) {
                            walking_time.setText(getResult());
                            IconicsDrawable drawable = new IconicsDrawable(SearchStoresActivity.this)
                                    .icon(is_walking ? GoogleMaterial.Icon.gmd_directions_walk : GoogleMaterial.Icon.gmd_directions_car)
                                    .color(walking_time.getCurrentTextColor())
                                    .sizeDp((int)walking_time.getTextSize());
                            mode.setImageDrawable(drawable);
                        }
                    }
                });
            }




            map_fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Uri.Builder builder = new Uri.Builder();
                    builder.scheme("https").authority("www.google.com").appendPath("maps").appendPath("dir").appendPath("").appendQueryParameter("api", "1").appendQueryParameter("destination", store.getLatitude() + "," + store.getLongitude());
                    String url = builder.build().toString();
                    Log.v("URL = " + url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                }
            });




            title.setText(store.getTitle());
            address.setText(store.getAddress());

            //-----------------------------------------------------------------

            updateFavBtn(store);
            Helper.setVisibilityTo(btn_phone, store.getPhone().length() > 0);
            Helper.setVisibilityTo(btn_web, store.getWebsite().length() > 0);
            Helper.setVisibilityTo(btn_tripadvisor, store.getTripadvisor().length() > 0);
            Helper.setVisibilityTo(btn_etable, store.getEtableLink().length() > 0 || store.getEtableSlug().length() > 0);

            store_type.setText(store.getType_string());
            btn_phone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    make_call(store.getPhone());
                }
            });
            btn_web.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Statics.openURL(SearchStoresActivity.this, store.getWebsite(), true);
                }
            });
            btn_tripadvisor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Statics.openURL(SearchStoresActivity.this, "https://www.link_tripadvisor.com/"+store.getTripadvisor(), true);
                }
            });
            btn_etable.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(store.getEtableLink().length() > 0) {
                        Statics.openURL(SearchStoresActivity.this, store.getEtableLink(), true);
                    }
                    else {
                        Statics.openURL(SearchStoresActivity.this, "https://www.e-table.gr/restaurant/" + store.getEtableSlug() + "?etref=akapnos&utm_source=akapnos.gr&utm_medium=referral&utm_campaign=akapnosaffiliate", true);
                    }
                }
            });

            fav_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Helper.toggleFavorite(SearchStoresActivity.this, store);
                    updateFavBtn(store);
                }
            });
//            store_image.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//
//                }
//            });

            String image_url = store.getFirstImage();
            boolean has_image = image_url.length() > 0;
            Helper.setVisibilityTo(store_image, has_image);
            Helper.setVisibilityTo(image_progress, has_image);
            if(has_image) {
                ImageDL.get().setImage(image_url, store_image, new ImageDL.ImageDLCompletion() {
                    @Override
                    public void completed(String image_url, boolean success) {
                        Helper.setVisibilityTo(image_progress, false);
                    }
                });
            }



            //-----------------------------------------------------------------

            behavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED);
        }
    }

    private void updateFavBtn(Store store) {
        fav_button.setImageResource(Helper.isFavorite(store) ? R.drawable.icon_fav_filled : R.drawable.icon_fav);
    }

    private void showTextValueWith(int container_id, int lbl_id, String value) {
        LinearLayout container = findViewById(container_id);
        boolean has_value = value.length() > 0;
        Helper.setVisibilityTo(container, has_value);
        if(has_value) { ((TextView) findViewById(lbl_id)).setText(value); }
    }

    private void setupSearch() {
        //https://github.com/Mauker1/MaterialSearchView
//        searchView.disableCamera();
        searchView.setHint(getString(R.string.SEARCH_STORES));
        searchView.adjustTintAlpha(0.9f);
        searchView.setInputType(InputType.TYPE_CLASS_TEXT);
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.v("onQueryTextSubmit==="+query);
                search_now(query);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        searchView.setSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewOpened() {
                searchView.clearHistory();
                searchView.clearSuggestions();
            }
            @Override
            public void onSearchViewClosed() {}
        });

        searchView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Do something when the suggestion list is clicked.
                String suggestion = searchView.getSuggestionAtPosition(position);
                searchView.setQuery(suggestion, false);
                search_now(suggestion);
            }
        });

//        searchView.mSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
//                    if(!Helper.connectionAvailable(SearchStoresActivity.this)) {
//                        //Search offline
//                        the_place = null;
//                        search_now(searchView.mSearchEditText.getText().toString());
//                        hideKeyboard();
//                    }
//                    return true;
//                }
//                return false;
//            }
//        });
    }

    private void search_now(String query) {
        if(query.length() > 0) {
            int cat_to_show = filtersData.get(store_category_to_show);
            if(cat_to_show == typeFAVORITE) {
                updateCategory(0);
            }
        }
        query = Helper.safeString(query);
        if(behavior != null) { behavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN); }
        if(searchView != null) { searchView.closeSearch(); }

        //clear search container
//        if(query.length() == 0) {
//            the_place = null;
//        }

        search_string = query;
        clearSearchText.setText(search_string);
        showHideClearSearch();
//        boolean search_by_google_maps = false;//Helper.connectionAvailable(this) && the_place != null;
//        if(!search_by_google_maps) {
            Log.v("DisplayMarkers - SearchNow: " + search_string);
            displayMarkers();
//        }
        if(map_list_adapter != null) {
            map_list_adapter.notifyDataSetChanged();
        }
    }

    private void showHideClearSearch() {
        Helper.setVisibilityTo(clearSearchContainer, search_string != null && search_string.length() > 0);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(the_menu == null) { //gia na parei to kentriko menu kai oxi to menu tou bottom drawer
            the_menu = menu;
            getMenuInflater().inflate(R.menu.menu_map, menu);
        }
        return true;
    }

    private boolean goBack() {
        boolean handled = false;
        if(list_container != null && list_container.getVisibility() == View.VISIBLE) {
            showHideList();
            handled = true;
        } else if(behavior != null) {
            if(behavior.getState() == BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT
                    || behavior.getState() == BottomSheetBehaviorGoogleMapsLike.STATE_EXPANDED
                    ) {
                behavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED);
                handled = true;
            }
            else if(behavior.getState() == BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED) {
                behavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN);
                if(opened_detail_from_list) {
                    opened_detail_from_list = false;
                    showHideList();
                }
                handled = true;
            }
        } else if(searchView.isOpen()) {
            searchView.closeSearch();
            handled = true;
        }

        return handled;
    }

    @Override
    public void onBackPressed() {
        if(!goBack()) {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if(!goBack()) {
                    return super.onOptionsItemSelected(item);
                }
                return true;
            case R.id.action_search:
                searchView.openSearch();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    class MapListAdapter extends RecyclerView.Adapter<MapListAdapter.MapListItem> {
        private int transparent_color = getResources().getColor(android.R.color.transparent);
        private ArrayList<ImageView> icons;

        MapListAdapter() {}

        @NonNull
        @Override
        public MapListItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(Helper.MAP_ICONS_WITH_BADGES ? R.layout.map_list_item2 : R.layout.map_list_item, parent, false);
            return new MapListItem(v);
        }

        @Override
        public int getItemCount() {
            return stores_list == null ? 0 : stores_list.size();
        }

        class MapListItem extends RecyclerView.ViewHolder {

            @BindView(R.id.bulletPoint) ImageView bulletPoint;
            @Nullable
            @BindView(R.id.bulletPoint2) ImageView bulletPoint2;
            @Nullable
            @BindView(R.id.bulletPoint3) ImageView bulletPoint3;
            @BindView(R.id.title) TextView title;
            @BindView(R.id.subtitle) TextView subtitle;
            @BindView(R.id.distance_label) TextView distance_label;
            @BindView(R.id.etable_indicator) ImageView etable_indicator;

            MapListItem(View view) {
                super(view);
                ButterKnife.bind(this, view);

                if(Helper.MAP_ICONS_WITH_BADGES) {
                    icons = new ArrayList<>();
                    icons.add(bulletPoint);
                    icons.add(bulletPoint2);
                    icons.add(bulletPoint3);
                }

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Store store = stores_list.get(getAdapterPosition());
                        Helper.currentlyClickedClusterItem = new MapClusterItem(store);
                        opened_detail_from_list = true;
                        selectedStoreNow(store);

                        showHideList();
                        if(google_map != null) {
                            google_map.moveCamera(CameraUpdateFactory.newLatLngZoom(store.getLatLng(), 16.0f));
                        }
                    }
                });
            }
        }

        @Override
        public void onBindViewHolder(@NonNull MapListItem holder, int position) {
            final Store store = stores_list.get(position);

            if(Helper.MAP_ICONS_WITH_BADGES) {
                boolean is_cafe = store.isCafe();
                boolean is_bar = store.isBar();
                boolean is_restaurant = store.isRestaurant();

                int index = 0;

                if(is_cafe) {
                    ImageView img = icons.get(index);
                    img.setImageDrawable(Helper.drawableForStoreType(SearchStoresActivity.this, typeCAFE));
                    index++;
                    Helper.setVisibilityTo(img, true);
                }
                if(is_bar) {
                    ImageView img = icons.get(index);
                    img.setImageDrawable(Helper.drawableForStoreType(SearchStoresActivity.this, typeBAR));
                    index++;
                    Helper.setVisibilityTo(img, true);
                }
                if(is_restaurant) {
                    ImageView img = icons.get(index);
                    img.setImageDrawable(Helper.drawableForStoreType(SearchStoresActivity.this, typeRESTAURANT));
                    index++;
                    Helper.setVisibilityTo(img, true);
                }
//                Log.e("AKSDJNA == " + store.getTitle() + " == " + store.getType_string() + " - " + is_cafe + "," +is_bar + ","+is_restaurant+ " == " + index);
                for(int i=index;i<icons.size();i++) {
                    ImageView img = icons.get(i);
                    img.setImageDrawable(null);
                    Helper.setVisibilityTo(img, false);
                }
            }
            else {
                Drawable drawable = Helper.drawableForStoreType(SearchStoresActivity.this, store.getStoreType());

                int store_type = store.getStoreType();
                if (drawable == null) {
                    ((GradientDrawable) holder.bulletPoint.getBackground()).setColor((store_type == typeNONE) ? transparent_color : Helper.colorForStoreType(store_type));
                    holder.bulletPoint.setImageDrawable(null);
                } else {
                    ((GradientDrawable) holder.bulletPoint.getBackground()).setColor(transparent_color);
                    holder.bulletPoint.setImageDrawable(drawable);
                }
            }

            holder.title.setText(store.getTitle());
            holder.subtitle.setText(store.getAddress());

            holder.distance_label.setText(store.distanceToUserDesc());

            Helper.setVisibilityTo(holder.etable_indicator, store.getEtableLink().length() > 0 || store.getEtableSlug().length() > 0);

            Helper.setVisibilityToTextView(holder.title);
            Helper.setVisibilityToTextView(holder.subtitle);
            Helper.setVisibilityToTextView(holder.distance_label);
        }
    }

    private boolean updateCategory(int category) {
        if(category != store_category_to_show) {
            store_category_to_show = category;
            filtersAdapter.notifyDataSetChanged();
            if(map_list_adapter != null) {
                map_list_adapter.notifyDataSetChanged();
            }
            return true;
        }
        return false;
    }

    class FiltersAdapter extends RecyclerView.Adapter<FiltersAdapter.FilterItem> {
        FiltersAdapter() {}

        @NonNull
        @Override
        public FilterItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.filter_stores_item, parent, false);
            return new FilterItem(v);
        }

        @Override
        public int getItemCount() {
            return filtersData == null ? 0 : filtersData.size();
        }

        class FilterItem extends RecyclerView.ViewHolder {

            @BindView(R.id.root) CardView root;
            @BindView(R.id.image) ImageView image;
            @BindView(R.id.title) TextView title;

            FilterItem(View view) {
                super(view);
                ButterKnife.bind(this, view);

                root.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int position = getAdapterPosition();
                        if(updateCategory(position)) {
                            Log.v("DisplayMarkers - ChangedCategory");
                            displayMarkers();
                        }
                    }
                });
            }
        }


        @Override
        public void onBindViewHolder(@NonNull FilterItem holder, int position) {
            Integer store_type = filtersData.get(position);

            Drawable drawable = Helper.drawableForStoreType(SearchStoresActivity.this, store_type);

            boolean is_fav = store_type == typeFAVORITE;
            if(drawable == null) {
                if(is_fav) {
                    holder.image.setImageDrawable(getResources().getDrawable(R.drawable.icon_fav_filled));
                }
                else {
                    ((GradientDrawable) holder.image.getBackground()).setColor((store_type == typeNONE) ? getResources().getColor(android.R.color.transparent) : Helper.colorForStoreType(store_type));
                    holder.image.setImageDrawable(null);
                }
            }
            else {
                ((GradientDrawable) holder.image.getBackground()).setColor(getResources().getColor(android.R.color.transparent));
                holder.image.setImageDrawable(drawable);
            }
            Helper.setVisibilityTo(holder.image, drawable != null || is_fav);
            holder.title.setText(Helper.titleForStoreType(store_type));

//            holder.root.setCardBackgroundColor(getResources().getColor(position == store_category_to_show ? R.color.result_dark : R.color.gray_light));
            boolean current_selection = position == store_category_to_show;
            holder.root.setCardBackgroundColor(getResources().getColor(current_selection ? R.color.keyColor2 : R.color.map_category_bg));
            holder.title.setTextColor(getResources().getColor(current_selection ? R.color.white : R.color.map_category_title));
        }
    }


    private void hideKeyboard() {
        if(searchView != null) { searchView.closeSearch(); }
        Helper.hideKeyboard(this);
    }
}