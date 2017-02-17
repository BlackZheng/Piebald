package com.blackzheng.me.piebald.api;

import android.util.Log;

import com.blackzheng.me.piebald.App;
import com.blackzheng.me.piebald.util.LogHelper;
import com.blackzheng.me.piebald.util.NetworkUtils;

import java.io.File;
import java.io.IOException;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by BlackZheng on 2016/4/4.
 */
public class UnsplashAPI {

    private static final String TAG = LogHelper.makeLogTag(UnsplashAPI.class);

    public static final String HOST = "https://api.unsplash.com";

    public static final String CLIENT_ID = "73e6a5754dc21008e77d834adf6f182b1375547bc65fa57e2f9200d2e3dbe8c3";

    public static final String PHOTOS = "/photos";

    public static final String CATEGORIES = "/categories";

    public static final String USER = "/users";

    public static final String COLLECTIONS = "/collections";

    public static final String LIST_PHOTOS = HOST + PHOTOS + CLIENT_ID +  "&page=%1$s";

    public static final String GET_SPECIFIC_PHOTO = HOST + PHOTOS + "/%1$s" + CLIENT_ID;

    public static final String GET_PHOTOS_BY_CATEGORY = HOST + CATEGORIES + "/%1$s" + PHOTOS + CLIENT_ID + "&page=%2$s";

    public static final String GET_USERPROFILE = HOST + USER + "/%1$s" + CLIENT_ID;

    public static final String GET_PHOTOS_BY_USER = HOST + USER + "/%1$s"  + PHOTOS + CLIENT_ID + "&page=%2$s";

    public static final String GET_RANDOM_PHOTOS = "https://source.unsplash.com/random/%1$sx%2$s";

    public static final String GET_CURATED = HOST + COLLECTIONS + "/%1$s" + CLIENT_ID + "&page=%2$s";

    public static final String GET_FEATURED_PHOTOS = HOST + COLLECTIONS + "/%1$s" + PHOTOS + CLIENT_ID + "&page=%2$s";

    public static final String GET_CURATED_PHOTOS = HOST + COLLECTIONS + "/curated" + "/%1$s" + PHOTOS + CLIENT_ID + "&page=%2$s";

    public static final String GET_A_FEATURED_COLLECTION = HOST + COLLECTIONS + "/%1$s" + CLIENT_ID;

    public static final String GET_A_CURATED_COLLECTION = HOST + COLLECTIONS + "/curated" + "/%1$s" + CLIENT_ID;

    private static final Interceptor REWRITE_CACHE_CONTROL_INTERCEPTOR = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Log.d("UnsplashAPI", chain.request().url().toString());
            Response originalResponse = chain.proceed(chain.request());
            if (NetworkUtils.getNetworkType(App.getContext()) != NetworkUtils.NOT_NETWORK) {
                int maxAge = 60; // 在线缓存在1分钟内可读取
                return originalResponse.newBuilder()
                        .removeHeader("Pragma")
                        .removeHeader("Cache-Control")
                        .header("Cache-Control", "public, max-age=" + maxAge)
                        .build();
            } else {
                int maxStale = 60 * 60 * 24; // 离线时缓存保存1天
                return originalResponse.newBuilder()
                        .removeHeader("Pragma")
                        .removeHeader("Cache-Control")
                        .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                        .build();
            }
        }
    };

    private static File httpCacheDirectory = new File(App.getContext().getCacheDir(), "UnsplashCache");
    private static int cacheSize = 10 * 1024 * 1024; // 10 MiB
    private static Cache cache = new Cache(httpCacheDirectory, cacheSize);
    public static OkHttpClient client = new OkHttpClient.Builder()
            .addNetworkInterceptor(REWRITE_CACHE_CONTROL_INTERCEPTOR)
            .addInterceptor(REWRITE_CACHE_CONTROL_INTERCEPTOR)
            .cache(cache)
            .build();
    private static UnsplashAPI unsplashAPI;
    private static UnsplashService unsplashService;
    private Object monitor = new Object();
    private UnsplashAPI(){}

    public static UnsplashAPI getInstance(){
        if (unsplashAPI == null) {
            synchronized (UnsplashAPI.class) {
                if (unsplashAPI == null) {
                    unsplashAPI = new UnsplashAPI();
                }
            }
        }
        return unsplashAPI;
    }
    public UnsplashService getUnsplashService() {
        if (unsplashService == null) {
            synchronized (monitor) {
                if (unsplashService == null) {
                    unsplashService = new Retrofit.Builder()
                            .baseUrl(HOST)
                            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                            .client(client)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build().create(UnsplashService.class);
                }
            }
        }
        return unsplashService;
    }

}
