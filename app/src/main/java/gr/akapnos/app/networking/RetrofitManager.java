package gr.akapnos.app.networking;
import android.content.Context;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
public class RetrofitManager {
    private Context context;
    private Observer observer;

    public RetrofitManager(Context context, Observer observer) {
        this.context = context;
        this.observer = observer;
    }
    private void subscribeObservable(Observable call) {
        call.subscribeOn(Schedulers.newThread()) // Create a new Thread
                .observeOn(AndroidSchedulers.mainThread()) // Use the UI thread
                .subscribe(observer);
    }

    public void get_Stores() {
        subscribeObservable(RestClient.getClientRx().call_DownloadStores());
    }
}