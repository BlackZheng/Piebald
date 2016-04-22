package com.blackzheng.me.piebald.model;

import android.database.Cursor;

import com.blackzheng.me.piebald.dao.ContentDataHelper;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by BlackZheng on 2016/4/4.
 */
public class Photo extends  BaseModel {

    private static final HashMap<String, Photo> CACHE = new HashMap<String, Photo>();

    public String id;
    public int width;
    public int height;
    public String color;
    public int likes;
    public Exif exif;
    public Location location;
    public Urls urls;
    public ArrayList<Category> categories;
    public Links links;
    public User user;



    public class Exif{
        public String make;
        public String model;
        public String exposure_time;
        public String aperture;
        public String focal_length;
        public int iso;
    }
    public class Location{
        public String city;
        public String country;
        public Position position;

        public class Position{
            public double latitude;
            public double longitude;
        }
    }

    public class Urls{
        public String raw;
        public String full;
        public String regular;
        public String small;
        public String thumb;
    }

    public class Category{
        public int id;
        public String title;
    }

    public class Links{
        public String self;
        public String html;
        public String download;
    }

    public class User{
        public String id;
        public String name;
        public Profile_Image profile_image;

        public class Profile_Image{
            public String small;
            public String medium;
            public String large;
        }
    }

    public static void addToCache(Photo photo) {
        CACHE.put(photo.id, photo);
    }

    public static Photo getFromCache(String id) {
        return CACHE.get(id);
    }

    public static Photo fromJson(String json) {
        return new Gson().fromJson(json, Photo.class);
    }

    public static Photo fromCursor(Cursor cursor) {
        String id = cursor.getString(cursor.getColumnIndex(ContentDataHelper.ContentDBInfo.ID));
        Photo photo = getFromCache(id);
        if (photo != null) {
            return photo;
        }
        photo = new Gson().fromJson(
                cursor.getString(cursor.getColumnIndex(ContentDataHelper.ContentDBInfo.JSON)),
                Photo.class);
        addToCache(photo);
        return photo;
    }
}
