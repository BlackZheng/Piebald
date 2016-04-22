package com.blackzheng.me.piebald.ui.adapter;

import android.database.Cursor;
import android.widget.Filter;

/**
 * Created by BlackZheng on 2016/4/7.
 */
public class CursorFilter extends Filter {
    CursorFilterClient mClient;

    interface CursorFilterClient {
        CharSequence convertToString(Cursor cursor);
        Cursor runQueryOnBackgroundThread(CharSequence constraint);
        Cursor getCursor();
        void changeCursor(Cursor cursor);
    }

    CursorFilter(CursorFilterClient client) {
        mClient = client;
    }

    @Override
    public CharSequence convertResultToString(Object resultValue) {
        return mClient.convertToString((Cursor) resultValue);
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        Cursor cursor = mClient.runQueryOnBackgroundThread(constraint);

        FilterResults results = new FilterResults();
        if (cursor != null) {
            results.count = cursor.getCount();
            results.values = cursor;
        } else {
            results.count = 0;
            results.values = null;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        Cursor oldCursor = mClient.getCursor();

        if (results.values != null && results.values != oldCursor) {
            mClient.changeCursor((Cursor) results.values);
        }
    }
}
