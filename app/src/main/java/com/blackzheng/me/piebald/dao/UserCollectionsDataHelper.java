package com.blackzheng.me.piebald.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.v4.content.CursorLoader;

import com.blackzheng.me.piebald.model.Collection;
import com.blackzheng.me.piebald.util.database.Column;
import com.blackzheng.me.piebald.util.database.SQLiteTable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by BlackZheng on 2017/2/12.
 */

public class UserCollectionsDataHelper extends BaseDataHelper{

    private String mUserId;

    public UserCollectionsDataHelper(Context context, String userId) {
        super(context);
        mUserId = userId;
    }

    @Override
    protected Uri getContentUri() {
        return DataProvider.USER_COLLECTIONS_URI;
    }

    @Override
    public CursorLoader getCursorLoader() {
        return new CursorLoader(getContext(), getContentUri(), null, ContentDBInfo.USER_ID + "=?",
                new String[] {
                        mUserId
                }, ContentDBInfo._ID + " ASC");
    }

    private ContentValues getContentValues(Collection collection) {
        ContentValues values = new ContentValues();
        values.put(ContentDBInfo.ID, String.valueOf(collection.id));
        values.put(ContentDBInfo.USER_ID, String.valueOf(collection.user.id));
        values.put(ContentDBInfo.CURATED, collection.curated);
        values.put(ContentDBInfo.JSON, collection.toJosn());
        return values;
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
            int row = db.delete(UserCollectionsDataHelper.ContentDBInfo.TABLE_NAME, ContentDBInfo.USER_ID + "=?", new String[] {
                    mUserId
            });
            return row;
        }
    }

    /**
     * 清空该表格的所有数据，用于清除缓存
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

    public static final class ContentDBInfo implements BaseColumns {
        private ContentDBInfo() {
        }

        public static final String TABLE_NAME = "user_collections";

        public static final String ID = "id";

        public static final String USER_ID = "user_id";

        public static final String CURATED = "curated";

        public static final String JSON = "json";

        public static final SQLiteTable TABLE = new SQLiteTable(TABLE_NAME)
                .addColumn(ID, Column.DataType.TEXT).addColumn(USER_ID, Column.DataType.TEXT)
                .addColumn(CURATED, Column.DataType.INTEGER).addColumn(JSON, Column.DataType.TEXT);
    }
}
