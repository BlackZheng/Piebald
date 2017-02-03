package com.blackzheng.me.piebald.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.v4.content.CursorLoader;

import com.blackzheng.me.piebald.model.Collection;
import com.blackzheng.me.piebald.ui.MainActivity;
import com.blackzheng.me.piebald.util.database.Column;
import com.blackzheng.me.piebald.util.database.SQLiteTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by BlackZheng on 2016/8/27.
 */
public class CollectionDataHelper extends  BaseDataHelper{
    public static final Map<String, String> COLLECTION_TYPE = new HashMap<String, String>();
    static{
        COLLECTION_TYPE.put(MainActivity.CURATED, "curated");
        COLLECTION_TYPE.put(MainActivity.FEATURED, "featured");
    }
    private boolean isCurated;
    
    public CollectionDataHelper(Context context, boolean isCurated) {
        super(context);
        this.isCurated = isCurated;
    }

    @Override
    protected Uri getContentUri() {
        return DataProvider.COLLECTIONS_URI;
    }

    private ContentValues getContentValues(Collection collection) {
        ContentValues values = new ContentValues();
        values.put(ContentDBInfo.ID, String.valueOf(collection.id));
        values.put(ContentDBInfo.CURATED, isCurated);
        values.put(ContentDBInfo.JSON, collection.toJosn());
        return values;
    }

    public Collection query(long id) {
        Collection collection = null;
        Cursor cursor = query(null, ContentDBInfo.CURATED + "= ?" + " AND " + ContentDBInfo.ID + "= ?",
                new String[] {
                        String.valueOf(isCurated ? 1 : 0), String.valueOf(id)
                }, null);
        if (cursor.moveToFirst()) {
            collection = Collection.fromCursor(cursor);
        }
        cursor.close();
        return collection;
    }
    public void bulkInsert(List<Collection> collections) {
        ArrayList<ContentValues> contentValues = new ArrayList<ContentValues>();
        for (Collection collection : collections) {
            ContentValues values = getContentValues(collection);
            contentValues.add(values);
        }
        ContentValues[] valueArray = new ContentValues[contentValues.size()];
        bulkInsert(contentValues.toArray(valueArray));
    }

    public int deleteAll() {
        synchronized (DataProvider.DBLock) {
            DBHelper mDBHelper = DataProvider.getDBHelper();
            SQLiteDatabase db = mDBHelper.getWritableDatabase();
            int row = db.delete(ContentDBInfo.TABLE_NAME, ContentDBInfo.CURATED + "=?", new String[] {
                    String.valueOf(isCurated ? 1 : 0)
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
        return new CursorLoader(getContext(), getContentUri(), null, ContentDBInfo.CURATED + "=?",
                new String[] {
                        String.valueOf(isCurated ? 1 : 0)
                }, ContentDBInfo._ID + " ASC");
    }

    public static final class ContentDBInfo implements BaseColumns {
        private ContentDBInfo() {
        }

        public static final String TABLE_NAME = "collections";

        public static final String ID = "id";

        public static final String CURATED = "curated";

        public static final String JSON = "json";

        public static final SQLiteTable TABLE = new SQLiteTable(TABLE_NAME)
                .addColumn(ID, Column.DataType.TEXT)
                .addColumn(CURATED, Column.DataType.INTEGER).addColumn(JSON, Column.DataType.TEXT);
    }
}
