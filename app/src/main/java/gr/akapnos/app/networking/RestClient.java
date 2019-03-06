package gr.akapnos.app.networking;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.http.GET;
import rx.Observable;

class RestClient {
    private static API_Interface api_interface;

    public interface API_Interface {
        @GET("akapnos-data.csv")
        Observable<ResponseBody> call_DownloadStores();
    }

    static API_Interface getClientRx() {
        if (api_interface == null) {
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

//            httpClient.addInterceptor(new HttpLoggingInterceptor().setLevel(Helper.isDebug() ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE));

            Retrofit client = new Retrofit.Builder()
                    .baseUrl("https://www.akapnos.gr/app/")
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
//                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient.build())
                    .build();
            api_interface = client.create(API_Interface.class);
        }
        return api_interface;
    }
}