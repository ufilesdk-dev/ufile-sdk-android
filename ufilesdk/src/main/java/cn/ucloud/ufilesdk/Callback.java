package cn.ucloud.ufilesdk;

import org.json.JSONObject;

/**
 * 回调函数  所有回调均在主线程
 * Created by jerry on 16/3/10.
 */
public interface Callback {
    void onSuccess(JSONObject message);

    void onProcess(long len);

    void onFail(JSONObject message);
}