package com.blackzheng.me.piebald.api;

/**
 * Created by BlackZheng on 2016/4/4.
 */
public class UnsplashAPI {
    public static final String HOST = "https://api.unsplash.com";

    public static final String CLIENT_ID = "?client_id=f38967a340ba240a903458e54fedb4346f02c86cf4bbfd2d867734bc78a03339";

    public static final String PHOTOS = "/photos";

    public static final String CATEGORIES = "/categories";

    public static final String LIST_PHOTOS = HOST + PHOTOS + CLIENT_ID +  "&page=%1$s";

    public static final String GET_SPECIFIC_PHOTO = HOST + PHOTOS + "/%1$s" + CLIENT_ID;

    public static final String GET_PHOTOS_BY_CATEGORY = HOST + CATEGORIES + "/%1$s" + PHOTOS + CLIENT_ID + "&page=%2$s";
}
