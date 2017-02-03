package com.blackzheng.me.piebald.model;

import android.database.Cursor;

import com.google.gson.Gson;

import java.util.HashMap;

/**
 * Created by BlackZheng on 2016/4/4.
 */
public abstract class BaseModel {
    public String toJosn(){
        return new Gson().toJson(this);
    }
}
