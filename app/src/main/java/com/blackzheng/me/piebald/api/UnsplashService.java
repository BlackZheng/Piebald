package com.blackzheng.me.piebald.api;

import com.blackzheng.me.piebald.model.Collection;
import com.blackzheng.me.piebald.model.Photo;
import com.blackzheng.me.piebald.model.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by BlackZheng on 2016/12/1.
 */

public interface UnsplashService {

    @GET("photos")
    Observable<List<Photo>> getLatestPhotos(@Query("page") String page, @Query("client_id") String client_id);

    @GET("categories/{category_id}/photos")
    Observable<List<Photo>> getPhotosByCategory(@Path("category_id") String category_id, @Query("page") String page, @Query("client_id") String client_id);

    @GET("users/{username}/photos")
    Observable<List<Photo>> getPhotosByUser(@Path("username") String username, @Query("page") int page, @Query("client_id") String client_id);

    @GET("photos/{id}")
    Observable<Photo> getPhoto(@Path("id") String id, @Query("client_id") String client_id);

    @GET("users/{username}")
    Observable<User> getUserByUsername(@Path("username") String username, @Query("client_id") String client_id);

    @GET("collections/{type}")
    Observable<List<Collection>> getCollections(@Path("type") String type, @Query("page") String page, @Query("client_id") String client_id);

    @GET("collections/{id}")
    Observable<Collection> getFeatureCollection(@Path("id") String id, @Query("client_id") String client_id);

    @GET("collections/curated/{id}")
    Observable<Collection> getCuratedCollection(@Path("id") String id, @Query("client_id") String client_id);
    @GET("collections/{id}/photos")
    Observable<List<Photo>> getFeaturePhotos(@Path("id") String id, @Query("page") int page, @Query("client_id") String client_id);

    @GET("collections/curated/{id}/photos")
    Observable<List<Photo>> getCuratedPhotos(@Path("id") String id, @Query("page") int page, @Query("client_id") String client_id);

}
