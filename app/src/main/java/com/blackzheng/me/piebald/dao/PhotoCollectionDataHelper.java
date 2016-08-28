package com.blackzheng.me.piebald.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.v4.content.CursorLoader;

import com.blackzheng.me.piebald.model.Photo;
import com.blackzheng.me.piebald.util.database.Column;
import com.blackzheng.me.piebald.util.database.SQLiteTable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by BlackZheng on 2016/8/28.
 */
public class PhotoCollectionDataHelper extends  BaseDataHelper {

    private String mCollectionID;

    public PhotoCollectionDataHelper(Context context, String id) {
        super(context);
        mCollectionID = id;
    }

    @Override
    protected Uri getContentUri() {
        return DataProvider.PHOTO_COLLECTION_URI;
    }

    private ContentValues getContentValues(Photo photo) {
        ContentValues values = new ContentValues();
        values.put(ContentDBInfo.ID, String.valueOf(photo.id));
        values.put(ContentDBInfo.COLLECTION, mCollectionID);
        values.put(ContentDBInfo.JSON, photo.toJosn());
        return values;
    }

    public void bulkInsert(List<Photo> photos) {
        ArrayList<ContentValues> contentValues = new ArrayList<ContentValues>();
        for (Photo photo : photos) {
            ContentValues values = getContentValues(photo);
            contentValues.add(values);
        }
        ContentValues[] valueArray = new ContentValues[contentValues.size()];
        bulkInsert(contentValues.toArray(valueArray));
    }

    public int deleteAll() {
        synchronized (DataProvider.DBLock) {
            DBHelper mDBHelper = DataProvider.getDBHelper();
            SQLiteDatabase db = mDBHelper.getWritableDatabase();
            int row = db.delete(ContentDBInfo.TABLE_NAME, ContentDBInfo.COLLECTION + "=?", new String[] {
                    mCollectionID
            });
            return row;
        }
    }

    /**
     * 清空该表格的所有数据，用于清楚缓存
     * @return
     */
    public static int deleteAllRows(){
        synchronized (DataProvider.DBLock) {
            DBHelper mDBHelper = DataProvider.getDBHelper();
            SQLiteDatabase db = mDBHelper.getWritableDatabase();
            int row = db.delete(ContentDBInfo.TABLE_NAME, null, null);
            return row;
        }
    }

    @Override
    public CursorLoader getCursorLoader() {
        return new CursorLoader(getContext(), getContentUri(), null, ContentDBInfo.COLLECTION + "=?",
                new String[] {
                        mCollectionID
                }, ContentDBInfo._ID + " ASC");
    }

    public static final class ContentDBInfo implements BaseColumns {
        private ContentDBInfo() {
        }

        public static final String TABLE_NAME = "photo_collection";

        public static final String ID = "id";

        public static final String COLLECTION = "collection";

        public static final String JSON = "json";

        public static final SQLiteTable TABLE = new SQLiteTable(TABLE_NAME)
                .addColumn(ID, Column.DataType.TEXT)
                .addColumn(COLLECTION, Column.DataType.TEXT).addColumn(JSON, Column.DataType.TEXT);
    }
}
