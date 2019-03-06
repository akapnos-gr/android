package gr.akapnos.app.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.javiersantos.appupdater.AppUpdaterUtils;
import com.github.javiersantos.appupdater.enums.AppUpdaterError;
import com.github.javiersantos.appupdater.enums.UpdateFrom;
import com.github.javiersantos.appupdater.objects.Update;

import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import gr.akapnos.app.Helper;
import gr.akapnos.app.LocaleManager;
import gr.akapnos.app.R;
import gr.akapnos.app.Statics;
import gr.akapnos.app.objects.LanguageObject;
import gr.akapnos.app.utilities.ImageDL;
import trikita.log.Log;

public class LanguagesActivity extends BaseActivity {

    @BindView(R.id.langs_recycler) RecyclerView recyclerView;
    @BindView(R.id.title) TextView title_view;
    @BindView(R.id.subtitle) TextView subtitle_view;
    @BindView(R.id.retry_button) AppCompatButton retryBtn;
    @BindView(R.id.progress) ProgressBar progress;
    @BindView(R.id.langs_container) LinearLayout langs_container;

    private LanguageObject selected_lang;
    private ArrayList<LanguageObject> langsArray;
    public LanguagesAdapter adapter;

    private boolean wait_for_selection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_languages);
        super.onCreate(savedInstanceState);

        //an epistrefei apo to main activity, na deixnei ta languages
        wait_for_selection = false;
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            String waitStr = extras.getString(Statics.LANGUAGE_SELECTION_WAIT);
            if (waitStr != null) {
                wait_for_selection = waitStr.equals("1");
            }
        }

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new org.solovyev.android.views.llm.LinearLayoutManager(this, org.solovyev.android.views.llm.LinearLayoutManager.HORIZONTAL, false));
        adapter = new LanguagesAdapter();

        retryBtn.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                getLanguages();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        getLanguages();
    }



//    public static String data_got_timestamp = "data_got_timestamp";
    public void getLanguages() {
        gotoMain();
//        boolean ok_to_download = !Helper.hasMainData();
//        if(ok_to_download) {
//            Log.v("OKtoCHECK: " + (Helper.mainData() != null) + " == " + (Helper.mainData() != null && Helper.mainData().secondsToRefreshData() > 0) + " --- " + Helper.getValueFromPrefs(data_got_timestamp));
//            if (Helper.mainData() != null && Helper.mainData().secondsToRefreshData() > 0) {
//                String stored = Helper.getValueFromPrefs(data_got_timestamp);
//                if (stored.length() > 0) {
//                    Long now = System.currentTimeMillis() / 1000;
//                    ok_to_download = now - Long.parseLong(stored) > Helper.mainData().secondsToRefreshData();
//
//                    Log.v("CHECK_DOWNLOAD_MAIN_DATA: " + now + " - " + Long.parseLong(stored) + " > " + Helper.mainData().secondsToRefreshData() + " ==> " + ok_to_download);
//                }
//            }
//        }
//        if(!ok_to_download) {
//            ok_to_download = Helper.mainData() == null;
//        }
//
//        showRetry(false);
//        showLoading(true);
//
//        if(ok_to_download) {
//            Observer<LanguagesResponse> observer = new Observer<LanguagesResponse>() {
//                @Override
//                public void onCompleted() {
//                }
//
//                @Override
//                public void onError(Throwable e) {
//                    Log.e("Languages", "ERROR");
//                    e.printStackTrace();
//
//                    boolean show_retry = true;
//                    if (open_offline) {
//                        show_retry = setLangFromStored(false);
//                    }
//
//                    if (show_retry) {
//                        showRetry(true);
//                    }
//                }
//
//                @Override
//                public void onNext(LanguagesResponse response) {
//                    langsArray = response.getLangs();
//
//                    Helper.setValuesToPrefs(new HashMap<String, String>() {{
//                        put(Statics.LANGS_AVAILABLE, Integer.toString(langsArray.size()));
//                    }});
//
//                    if (langsArray.size() == 0) {
//                        showRetry(true);
//                    } else {
//                        if (langsArray.size() == 1) {
//                            LanguageObject lang = langsArray.get(0);
//                            setTheSelectedLang(lang);
//                        } else {
//                            boolean shouldShowLanguages = wait_for_selection;
//                            LanguageObject lang = null;
//                            if (!shouldShowLanguages) {
////                                String langName = Helper.getValueFromPrefs(Statics.LANG_CODE);
////                                String langImage = Helper.getValueFromPrefs(Statics.LANG_IMAGE);
////                                if (langName.length() > 0 && langImage.length() > 0) {
////                                    lang = new LanguageObject();
////                                    lang.setLang_code(langName);
////                                    lang.setIcon_large(langImage);
////                                }
//                                String langName = LocaleManager.getLanguage(LanguagesActivity.this);
//
//                                if(langName != null && langName.length() > 0) {
//                                    lang = new LanguageObject();
//                                    lang.setLang_code(langName);
//                                }
//                                shouldShowLanguages = lang == null;
//                            }
//
//                            if (!shouldShowLanguages) {
//                                showLoading(true);
//                                setTheSelectedLang(lang);
//                            } else {
//                                showLanguages();
//                                showLoading(false);
//                            }
//                        }
//                    }
//                }
//            };
//
//            RetrofitManager manager = new RetrofitManager(this, observer);
//            manager.getLanguages();
//        }
//        else {
//            setLangFromStored(true);
//        }
    }

    private boolean setLangFromStored(boolean fast) {
        boolean show_retry = true;
//        String langName = Helper.getValueFromPrefs(Statics.LANG_CODE);
//        String langImage = Helper.getValueFromPrefs(Statics.LANG_IMAGE);
//        if (langName.length() > 0 && langImage.length() > 0) {
        String langName = LocaleManager.getLanguage(LanguagesActivity.this);

        if(langName != null && langName.length() > 0) {
            show_retry = false;

            LanguageObject lang = new LanguageObject();
            lang.setLang_code(langName);
//            lang.setIcon_large(langImage);

            showLoading(true);
            setTheSelectedLang(lang, fast);
        }
        return show_retry;
    }

    private void getData() {
//        Log.v("Langs: GetData " + selected_lang.getLang_code());
//        if (selected_lang != null) {
//            showRetry(false);
//            showLoading(true);
//
//            Observer<MainAppObject> observer = new Observer<MainAppObject>() {
//                @Override
//                public void onCompleted() {}
//                @Override
//                public void onError(Throwable e) {
//                    Log.e("MainObject GET", "ERROR: " + langsArray + ": " + e.getLocalizedMessage() + " ==> ");
//                    e.printStackTrace();
//
//                    boolean show_retry = true;
//                    if(open_offline) {
//                        if(Helper.mainData() != null) {
//                            tryAutoLogin();
//                            show_retry = false;
//                        }
//                    }
//
//                    if(show_retry) {
//                        if (langsArray == null) {
//                            showRetry(true);
//                        } else {
//                            if (langsArray.size() == 1) {
//                                title_view.setText(R.string.THERE_WAS_A_PROBLEM);
//                                subtitle_view.setText(R.string.PLEASE_TRY_AGAIN_LATER);
//                                showRetry(true);
//                            } else {
//                                showLoading(false);
//                                Helper.showToast(getString(R.string.THERE_WAS_A_PROBLEM), Toast.LENGTH_LONG);
//
//                                showLanguages();
//                                showRetry(false);
//                            }
//                        }
//                    }
//                }
//
//                @Override
//                public void onNext(MainAppObject mainAppObject) {
//                    Helper.setMainAppObject(mainAppObject);
//                    boolean is_active = mainAppObject != null;
//                    if (is_active) {
//                        Long tsLong = System.currentTimeMillis() / 1000;
//                        String ts = tsLong.toString();
//                        Helper.setValueToPrefs(data_got_timestamp, ts);
//
//                        OfflineDataStorage.getInstance().storeMainData(mainAppObject);
//                        tryAutoLogin();
//                    } else {
//                        title_view.setText(R.string.NOT_AVAILABLE_TITLE);
//                        subtitle_view.setText(R.string.NOT_AVAILABLE_MSG);
//                        showRetry(true);
//                    }
//                }
//            };
//
//            RetrofitManager manager = new RetrofitManager(this, observer);
//            manager.getMainData();
//        }
    }

    private void showLanguages() {
        Helper.setVisibilityTo(langs_container, (langsArray != null && langsArray.size() > 1));
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void setTheSelectedLang(LanguageObject language) {
        setTheSelectedLang(language, false);
    }
    private void setTheSelectedLang(LanguageObject language, boolean fast) {
        Helper.setVisibilityTo(langs_container, false);
        selected_lang = language;
        changeLanguage(language);
        checkUpdate();
    }

    private void checkUpdate() {

        Log.v("Checking version...");
        AppUpdaterUtils appUpdaterUtils = new AppUpdaterUtils(this)
                .setUpdateFrom(UpdateFrom.GOOGLE_PLAY)
                .withListener(new AppUpdaterUtils.UpdateListener() {
                    @Override
                    public void onSuccess(final Update update, Boolean isUpdateAvailable) {
                        Log.d("Latest Version", update.getLatestVersion());
                        Log.d("Latest Version Code", update.getLatestVersionCode());
                        Log.d("Release notes", update.getReleaseNotes());
                        Log.d("URL", update.getUrlToDownload());
                        Log.d("Is update available?", Boolean.toString(isUpdateAvailable));
                        if (isUpdateAvailable) {
                            Helper.createAlertDialog(LanguagesActivity.this,
                                    getResources().getString(R.string.UPDATE_APP_TITLE),
                                    getResources().getString(R.string.UPDATE_APP_MESSAGE),
                                    getResources().getString(R.string.UPDATE_NOW),
                                    null, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Statics.openURL(LanguagesActivity.this, update.getUrlToDownload().toString(), true);
                                        }
                                    }, null, false); //non cancelable
                        } else {
                            getData();
                        }
                    }

                    @Override
                    public void onFailed(AppUpdaterError error) {
                        Log.e("AppUpdater Error: " + error);
                        getData();
                    }
                });
        appUpdaterUtils.start();
    }

    public void changeLanguage(LanguageObject lang) {
        if (lang == null) {
            lang = new LanguageObject();
            lang.setLang_code("el");
        }


        LocaleManager.setNewLocale(this, lang.getLang_code());


//        Locale locale = new Locale(lang.getLang_code());
//        Locale.setDefault(locale);
//        Configuration config = new Configuration();
//        config.locale = locale;
//        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

        //Saved shared preferences if language is picked;
//        final LanguageObject finalLang = lang;
//        Helper.setValuesToPrefs(new HashMap<String, String>() {{
//            put(Statics.LANG_IMAGE, finalLang.getIcon_large());
//            put(Statics.LANG_CODE, finalLang.getLang_code());
//        }});
    }

    private void gotoMain() {
        Intent intent = new Intent(this, SearchStoresActivity.class);

        Bundle extras = getIntent().getExtras();
        if(extras != null){
            intent.putExtras(extras);
        }
        if(Helper.isDebug()) {
            Log.v("GoToMain NOW");
            Helper.dumpBundle(intent.getExtras());
        }

//        ImageView MAIN_LOGO = (ImageView) findViewById(R.id.MAIN_LOGO);
//        if(MAIN_LOGO != null) {
//            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, Pair.create((View) MAIN_LOGO, "MAIN_LOGO"));
//            ActivityCompat.startActivity(this, intent, options.toBundle());
//        }
//        else {
//            startActivityForResult(intent, 0);
//        }
        startActivityForResult(intent, 0);
    }



    private void showLoading(boolean show) {
        Helper.setVisibilityTo(progress, show);
    }

    private void showRetry(boolean show) {
        if (show) {
            showLoading(false);
        }
        Helper.setVisibilityTo(langs_container,
                (show && selected_lang == null)
                        && langsArray != null && langsArray.size() > 1 && wait_for_selection);

        Helper.setVisibilityTo(title_view, show);
        Helper.setVisibilityTo(subtitle_view, show);
        Helper.setVisibilityTo(retryBtn, show);
    }

    class LanguagesAdapter extends RecyclerView.Adapter<LanguagesAdapter.LanguageItem> {
        LanguagesAdapter(){}
        @NonNull
        @Override
        public LanguageItem onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            LayoutInflater mInflater = LayoutInflater.from(viewGroup.getContext());
            ViewGroup mainGroup = (ViewGroup) mInflater.inflate(R.layout.language_item, viewGroup, false);
            return new LanguageItem(mainGroup);
        }

        class LanguageItem extends RecyclerView.ViewHolder {
            @BindView(R.id.title) TextView title;
            @BindView(R.id.image) ImageView imageview;

            LanguageItem(View view) {
                super(view);
                ButterKnife.bind(this, view);
//                title.setTextColor(clr_text1);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setTheSelectedLang(langsArray.get(getAdapterPosition()));
                    }
                });
            }
        }

        @Override
        public void onBindViewHolder(@NonNull LanguagesAdapter.LanguageItem holder, int position) {
            LanguageObject langObj = langsArray.get(position);
            holder.title.setText(new Locale(langObj.getLang_code()).getDisplayName());
            ImageDL.get().setImage(langObj.getIcon_large(), holder.imageview);
        }

        @Override
        public int getItemCount() {
            return langsArray == null ? 0 : langsArray.size();
        }
    }
}