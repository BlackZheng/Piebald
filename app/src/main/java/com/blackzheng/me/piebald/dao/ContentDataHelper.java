package com.blackzheng.me.piebald.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.v4.content.CursorLoader;

import com.blackzheng.me.piebald.model.Photo;
import com.blackzheng.me.piebald.ui.MainActivity;
import com.blackzheng.me.piebald.util.database.Column;
import com.blackzheng.me.piebald.util.database.SQLiteTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by BlackZheng on 2016/4/6.
 */
public class ContentDataHelper extends BaseDataHelper {

    public static final Map<String, Integer> CATEGORY_ID = new HashMap<String, Integer>();
    static{
        CATEGORY_ID.put(MainActivity.LATEST, 0);
        CATEGORY_ID.put(MainActivity.BUILDINGS, 2);
        CATEGORY_ID.put(MainActivity.FOOD_AND_DRINK, 3);
        CATEGORY_ID.put(MainActivity.NATURE, 4);
        CATEGORY_ID.put(MainActivity.PEOPLE, 6);
        CATEGORY_ID.put(MainActivity.TECHNOLOGY, 7);
        CATEGORY_ID.put(MainActivity.OBJECTS, 8);
    }
    private String mCategory;

    public ContentDataHelper(Context context, String category) {
        super(context);
        mCategory = category;
    }

    @Override
    protected Uri getContentUri() {
        return DataProvider.CONTENTS_URI;
    }

    private ContentValues getContentValues(Photo photo) {
        ContentValues values = new ContentValues();
        values.put(ContentDBInfo.ID, photo.id);
        values.put(ContentDBInfo.CATEGORY, CATEGORY_ID.get(mCategory));
        values.put(ContentDBInfo.JSON, photo.toJosn());
        return values;
    }

    public Photo query(long id) {
        Photo photo = null;
        Cursor cursor = query(null, ContentDBInfo.CATEGORY + "=?" + " AND " + ContentDBInfo.ID + "= ?",
                new String[] {
                        String.valueOf(CATEGORY_ID.get(mCategory)), String.valueOf(id)
                }, null);
        if (cursor.moveToFirst()) {
            photo = Photo.fromCursor(cursor);
        }
        cursor.close();
        return photo;
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
            int row = db.delete(ContentDBInfo.TABLE_NAME, ContentDBInfo.CATEGORY + "=?", new String[] {
                    String.valueOf(CATEGORY_ID.get(mCategory))
            });
            return row;
        }
    }
    @Override
    public CursorLoader getCursorLoader() {
        return new CursorLoader(getContext(), getContentUri(), null, ContentDBInfo.CATEGORY + "=?",
                new String[] {
                        String.valueOf(CATEGORY_ID.get(mCategory))
                }, ContentDBInfo._ID + " ASC");
    }

    public static final class ContentDBInfo implements BaseColumns {
        private ContentDBInfo() {
        }

        public static final String TABLE_NAME = "contents";

        public static final String ID = "id";

        public static final String CATEGORY = "category";

        public static final String JSON = "json";

        public static final SQLiteTable TABLE = new SQLiteTable(TABLE_NAME)
                .addColumn(ID, Column.DataType.INTEGER)
                .addColumn(CATEGORY, Column.DataType.INTEGER).addColumn(JSON, Column.DataType.TEXT);
    }
}
