package gr.akapnos.app.utilities;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.snatik.storage.EncryptConfiguration;
import com.snatik.storage.Storage;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import gr.akapnos.app.Helper;
import gr.akapnos.app.objects.Store;

//    compile 'com.snatik:storage:2.1.0'

public class OfflineDataStorage {
    private static final String PATH_SEPARATOR = "/";
    private Context context;
    Storage _storage, _storage_persistent;
    private static OfflineDataStorage _instance;
    public static OfflineDataStorage getInstance() {
        if (_instance == null) {
            _instance = new OfflineDataStorage(Helper.appCtx());
        }
        return _instance;
    }
    OfflineDataStorage(Context context) {
        super();
        this.context = context;
    }
    private Storage storage() {
        if(_storage == null) {
            _storage = new Storage(context);
            String folder = _storage.getInternalCacheDirectory() + PATH_SEPARATOR + "offline_data";
            File f = new File(folder);
            if(!f.isDirectory()) {
                _storage.createDirectory(folder);
            }

            // set encryption
            String IVX = "74zFAs3IoT5HNVv2"; // 16 lenght - not secret
            String SECRET_KEY = "DvnsNOa2m9OB2LrJ"; // 16 lenght - secret
            byte[] SALT = "0111011101010111".getBytes(); // random 16 bytes array

            EncryptConfiguration configuration = new EncryptConfiguration.Builder()
                    .setEncryptContent(IVX, SECRET_KEY, SALT)
                    .build();
            _storage.setEncryptConfiguration(configuration);
        }
        return _storage;
    }

    private String storagePath() {
        return storage().getInternalCacheDirectory() + PATH_SEPARATOR + "offline_data" + PATH_SEPARATOR;
    }


    public boolean storeData(String key, String json) {
        return storage().createFile(storagePath() + key, json);
    }
    public String getData(String key) {
        try {
            return storage().readTextFile(storagePath() + key);
        } catch (Exception e){
//            Log.e(key, e.getLocalizedMessage());
        }
        return null;
    }


    public boolean deleteFileFor(String key) {
        try {
            return storage().deleteFile(storagePath() + key);
        } catch (Exception e){
//            Log.e(key, e.getLocalizedMessage());
        }
        return false;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private static final String STORES_KEY = "STORES";
    public boolean storeStores(ArrayList<Store> stores) {
        return stores != null && storeData(STORES_KEY, new Gson().toJson(stores));
    }
    public ArrayList<Store> getStoredStores() {
        String json = getData(STORES_KEY);
        if(json != null && json.length() > 0) {
            return new Gson().fromJson(json, new TypeToken<ArrayList<Store>>(){}.getType());
        }
        return new ArrayList<>();
    }

    //-------------------------

    private static final String FAV_STORES_KEY = "FAV_STORES_KEY";
    public boolean saveFavoriteStores(HashMap<String, String> data) {
        return storeData(FAV_STORES_KEY, new Gson().toJson(data));
    }
    public HashMap<String, String> getFavoriteStores() {
        String json = getData(FAV_STORES_KEY);
        if(json != null && json.length() > 0) {
            return new Gson().fromJson(json, new TypeToken<HashMap<String, String>>(){}.getType());
        }
        return new HashMap<>();
    }
}
