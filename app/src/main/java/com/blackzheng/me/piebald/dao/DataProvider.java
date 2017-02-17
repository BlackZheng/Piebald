package com.blackzheng.me.piebald.dao;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import com.blackzheng.me.piebald.App;

/**
 * Created by BlackZheng on 2016/4/7.
 */
public class DataProvider extends ContentProvider {

    static final String TAG = DataProvider.class.getSimpleName();

    static final Object DBLock = new Object();

    public static final String AUTHORITY = "com.blackzheng.piebald.provider";

    public static final String SCHEME = "content://";

    // messages
    public static final String PATH_CONTENTS = "/contents";

    public static final String PATH_USERALBUM = "/useralbum";

    public static final String PATH_COLLECTIONS = "/collections";

    public static final String PATH_PHOTO_COLLECTION = "/photo_collection";

    public static final String PATH_USER_COLLECTIONS = "/user_collections";

    public static final Uri CONTENTS_URI = Uri.parse(SCHEME + AUTHORITY + PATH_CONTENTS);

    public static final Uri USERALBUM_URI = Uri.parse(SCHEME + AUTHORITY + PATH_USERALBUM);

    public static final Uri COLLECTIONS_URI = Uri.parse(SCHEME + AUTHORITY + PATH_COLLECTIONS);

    public static final Uri PHOTO_COLLECTION_URI = Uri.parse(SCHEME + AUTHORITY + PATH_PHOTO_COLLECTION);

    public static final Uri USER_COLLECTIONS_URI = Uri.parse(SCHEME + AUTHORITY + PATH_USER_COLLECTIONS);

    private static final int CONTENTS = 0;

    private static final int USERALBUM = 1;

    private static final int COLLECTIONS = 2;

    private static final int PHOTO_COLLECTION = 3;

    private static final int USER_COLLECTIONS = 4;

    /*
     * MIME type definitions
     */
    public static final String CONTENTS_TYPE = "vnd.android.cursor.dir/vnd.blackzheng.piebald.contents";

    public static final String USERALBUM_TYPE = "vnd.android.cursor.dir/vnd.blackzheng.piebald.useralbum";

    public static final String COLLECTIONS_TYPE = "vnd.android.cursor.dir/vnd.blackzheng.piebald.collections";

    public static final String PHOTO_COLLECTION_TYPE = "vnd.android.cursor.dir/vnd.blackzheng.piebald.photo_collection";

    public static final String USER_COLLECTIONS_TYPE = "vnd.android.cursor.dir/vnd.blackzheng.piebald.user_collections";

    private static final UriMatcher sUriMatcher;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, "contents", CONTENTS);
        sUriMatcher.addURI(AUTHORITY, "useralbum", USERALBUM);
        sUriMatcher.addURI(AUTHORITY, "collections", COLLECTIONS);
        sUriMatcher.addURI(AUTHORITY, "photo_collection", PHOTO_COLLECTION);
        sUriMatcher.addURI(AUTHORITY, "user_collections", USER_COLLECTIONS);
    }

    private static DBHelper mDBHelper;

    public static DBHelper getDBHelper() {
        if (mDBHelper == null) {
            mDBHelper = new DBHelper(App.getContext());
        }
        return mDBHelper;
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        synchronized (DBLock) {
            SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
            String table = matchTable(uri);
            queryBuilder.setTables(table);

            SQLiteDatabase db = getDBHelper().getReadableDatabase();
            Cursor cursor = queryBuilder.query(db, // The database to
                    // queryFromDB
                    projection, // The columns to return from the queryFromDB
                    selection, // The columns for the where clause
                    selectionArgs, // The values for the where clause
                    null, // don't group the rows
                    null, // don't filter by row groups
                    sortOrder // The sort order
            );

            cursor.setNotificationUri(getContext().getContentResolver(), uri);
            return cursor;
        }
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case CONTENTS:
                return CONTENTS_TYPE;
            case USERALBUM:
                return USERALBUM_TYPE;
            case COLLECTIONS:
                return COLLECTIONS_TYPE;
            case PHOTO_COLLECTION:
                return PHOTO_COLLECTION_TYPE;
            case USER_COLLECTIONS:
                return USER_COLLECTIONS_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        synchronized (DBLock) {
            String table = matchTable(uri);
            SQLiteDatabase db = getDBHelper().getWritableDatabase();
            long rowId = 0;
            db.beginTransaction();
            try {
                rowId = db.insert(table, null, values);
                db.setTransactionSuccessful();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            } finally {
                db.endTransaction();
            }
            if (rowId > 0) {
                Uri returnUri = ContentUris.withAppendedId(uri, rowId);
                getContext().getContentResolver().notifyChange(uri, null);
                return returnUri;
            }
            throw new SQLException("Failed to insert row into " + uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        synchronized (DBLock) {
            SQLiteDatabase db = getDBHelper().getWritableDatabase();

            int count = 0;
            String table = matchTable(uri);
            db.beginTransaction();
            try {
                count = db.delete(table, selection, selectionArgs);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            getContext().getContentResolver().notifyChange(uri, null);
            return count;
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        synchronized (DBLock) {
            SQLiteDatabase db = getDBHelper().getWritableDatabase();
            int count;
            String table = matchTable(uri);
            db.beginTransaction();
            try {
                count = db.update(table, values, selection, selectionArgs);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            getContext().getContentResolver().notifyChange(uri, null);

            return count;
        }
    }

    private String matchTable(Uri uri) {
        String table = null;
        switch (sUriMatcher.match(uri)) {
            case CONTENTS:
                table = ContentDataHelper.ContentDBInfo.TABLE_NAME;
                break;
            case USERALBUM:
                table = UserAlbumDataHelper.ContentDBInfo.TABLE_NAME;
                break;
            case COLLECTIONS:
                table = CollectionDataHelper.ContentDBInfo.TABLE_NAME;
                break;
            case PHOTO_COLLECTION:
                table = PhotoCollectionDataHelper.ContentDBInfo.TABLE_NAME;
                break;
            case USER_COLLECTIONS:
                table = UserCollectionsDataHelper.ContentDBInfo.TABLE_NAME;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        return table;
    }
}

