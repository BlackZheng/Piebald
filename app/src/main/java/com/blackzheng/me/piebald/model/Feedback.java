package com.blackzheng.me.piebald.model;

import cn.bmob.v3.BmobObject;

/**
 * Created by BlackZheng on 2016/8/28.
 */
public class Feedback extends BmobObject {
    private String subject = "default_subject";
    private String content;
    private String model = "default";
    private int sdk_int;

    public String getSubject() {
        return subject;
    }

    public int getSdk_int() {
        return sdk_int;
    }

    public String getModel() {
        return model;
    }

    public String getContent() {
        return content;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setSdk_int(int sdk_int) {
        this.sdk_int = sdk_int;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
