package com.blackzheng.me.piebald.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
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
 * Created by BlackZheng on 2016/8/17.
 */
public class UserAlbumDataHelper extends BaseDataHelper {

    private String mUser;

    public UserAlbumDataHelper(Context context, String username) {
        super(context);
        mUser = username;
    }
    //Need to change
    @Override
    protected Uri getContentUri() {
        return DataProvider.USERALBUM_URI;
    }

    private ContentValues getContentValues(Photo photo) {
        ContentValues values = new ContentValues();
        values.put(ContentDBInfo.ID, photo.id);
        values.put(ContentDBInfo.USER, photo.user.username);
        values.put(ContentDBInfo.JSON, photo.toJosn());
        return values;
    }

    public Photo query(long id) {
        Photo photo = null;
        Cursor cursor = query(null, ContentDBInfo.USER + "=?" + " AND " + ContentDBInfo.ID + "= ?",
                new String[] {
                        mUser, String.valueOf(id)
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
    public static int deleteAllRows(){
        synchronized (DataProvider.DBLock) {
            DBHelper mDBHelper = DataProvider.getDBHelper();
            SQLiteDatabase db = mDBHelper.getWritableDatabase();
            int row = db.delete(ContentDBInfo.TABLE_NAME, null, null);
            return row;
        }
    }
    //只删除某个用户的全部数据
    public int deleteAll() {
        synchronized (DataProvider.DBLock) {
            DBHelper mDBHelper = DataProvider.getDBHelper();
            SQLiteDatabase db = mDBHelper.getWritableDatabase();
            int row = db.delete(ContentDBInfo.TABLE_NAME, ContentDBInfo.USER + "=?", new String[] {mUser});
            return row;
        }
    }
    @Override
    public CursorLoader getCursorLoader() {
        return new CursorLoader(getContext(), getContentUri(), null, ContentDBInfo.USER + "=?",
                new String[] {mUser}, ContentDBInfo._ID + " ASC");
    }

    public static final class ContentDBInfo implements BaseColumns {
        private ContentDBInfo() {
        }
        public static final String TABLE_NAME = "useralbum";

        public static final String ID = "id";

        public static final String USER = "username";

        public static final String JSON = "json";

        public static final SQLiteTable TABLE = new SQLiteTable(TABLE_NAME)
                .addColumn(ID, Column.DataType.INTEGER)
                .addColumn(USER, Column.DataType.TEXT).addColumn(JSON, Column.DataType.TEXT);
    }
}
