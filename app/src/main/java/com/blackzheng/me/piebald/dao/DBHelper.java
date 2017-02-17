package com.blackzheng.me.piebald.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by BlackZheng on 2016/4/6.
 */
public class DBHelper extends SQLiteOpenHelper {
    // 数据库名
    private static final String DB_NAME = "piebald.db";

    // 数据库版本
    private static final int VERSION = 4;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        ContentDataHelper.ContentDBInfo.TABLE.create(db);
        UserAlbumDataHelper.ContentDBInfo.TABLE.create(db);
        CollectionDataHelper.ContentDBInfo.TABLE.create(db);
        PhotoCollectionDataHelper.ContentDBInfo.TABLE.create(db);
        UserCollectionsDataHelper.ContentDBInfo.TABLE.create(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion){
            case 1:
                UserAlbumDataHelper.ContentDBInfo.TABLE.create(db);
                CollectionDataHelper.ContentDBInfo.TABLE.create(db);
                PhotoCollectionDataHelper.ContentDBInfo.TABLE.create(db);
                UserCollectionsDataHelper.ContentDBInfo.TABLE.create(db);
                break;
            case 2:
                CollectionDataHelper.ContentDBInfo.TABLE.create(db);
                PhotoCollectionDataHelper.ContentDBInfo.TABLE.create(db);
                UserCollectionsDataHelper.ContentDBInfo.TABLE.create(db);
                break;
            case 3:
                UserCollectionsDataHelper.ContentDBInfo.TABLE.create(db);
                break;
            default:
                break;
        }
    }
}
