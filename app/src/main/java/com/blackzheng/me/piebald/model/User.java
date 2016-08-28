package com.blackzheng.me.piebald.model;

import android.database.Cursor;

import com.blackzheng.me.piebald.dao.ContentDataHelper;
import com.blackzheng.me.piebald.dao.UserAlbumDataHelper;
import com.google.gson.Gson;

import java.util.HashMap;

/**
 * Created by BlackZheng on 2016/8/17.
 */
public class User {
    private static final HashMap<String, User> CACHE = new HashMap<String, User>();

    public String username;
    public String name;
    public String portfolio_url;
    public String bio;
    public String location;
    public int total_likes;
    public int total_photos;
    public Profile_Image profile_image;

    public class Profile_Image{
        public String small;
        public String medium;
        public String large;
    }

    public static void addToCache(User user) {
        CACHE.put(user.username, user);
    }

    public static User getFromCache(String username) {
        return CACHE.get(username);
    }

    public static User fromJson(String json) {
        return new Gson().fromJson(json, User.class);
    }

    public static User fromCursor(Cursor cursor) {
        String username = cursor.getString(cursor.getColumnIndex(UserAlbumDataHelper.ContentDBInfo.USER));
        User user = getFromCache(username);
        if (user != null) {
            return user;
        }
        user = new Gson().fromJson(
                cursor.getString(cursor.getColumnIndex(ContentDataHelper.ContentDBInfo.JSON)),
                User.class);
        addToCache(user);
        return user;
    }
}
