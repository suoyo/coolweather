package com.suoyo.coolweather.util;

/**
 * Created by SUOYC on 2016/6/29.
 */
public interface HttpCallbackListener {
    void onFinish(String response);

    void onError(Exception e);
}
