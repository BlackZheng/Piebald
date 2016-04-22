package com.blackzheng.me.piebald.model;

import com.google.gson.Gson;

/**
 * Created by BlackZheng on 2016/4/4.
 */
public abstract class BaseModel {
    public String toJosn(){
        return new Gson().toJson(this);
    }
}
