package com.blackzheng.me.piebald.model;

import android.database.Cursor;

import com.blackzheng.me.piebald.dao.CollectionDataHelper;
import com.blackzheng.me.piebald.dao.ContentDataHelper;
import com.google.gson.Gson;

import java.util.HashMap;

/**
 * Created by BlackZheng on 2016/8/27.
 */
public class Collection extends BaseModel {

    private static final HashMap<Integer, Collection> CACHE = new HashMap<Integer, Collection>();
    public int id;
    public String title;
    public String description;
    public String published_at;
    public boolean curated;
    public int total_photos;
    public Photo cover_photo;
    public User user;

    public static void addToCache(Collection collection) {
        CACHE.put(collection.id, collection);
    }

    public static Collection getFromCache(int id) {
        return CACHE.get(id);
    }

    public static Collection fromJson(String json) {
        return new Gson().fromJson(json, Collection.class);
    }

    public static Collection fromCursor(Cursor cursor) {
        int id;
        try{
            id = Integer.parseInt(cursor.getString(cursor.getColumnIndex(CollectionDataHelper.ContentDBInfo.ID)));
        }catch (Exception e){
            String error = cursor.getString(cursor.getColumnIndex(CollectionDataHelper.ContentDBInfo.JSON));
            throw new RuntimeException(error);
        }

        Collection collection = getFromCache(id);
        if (collection != null) {
            return collection;
        }
        collection = new Gson().fromJson(
                cursor.getString(cursor.getColumnIndex(CollectionDataHelper.ContentDBInfo.JSON)),
                Collection.class);
        addToCache(collection);
        return collection;
    }
}
