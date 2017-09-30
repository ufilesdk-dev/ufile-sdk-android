package cn.ucloud.ufilesdk.task;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import cn.ucloud.ufilesdk.UFileRequest;

/**
 * Created by jerry on 15/12/14.
 */
public class WriteAsyncTask extends HttpAsyncTask {

    private final static String TAG = WriteAsyncTask.class.getSimpleName();

    private String upload;

    public WriteAsyncTask(String url, UFileRequest uFileRequest, HttpCallback callback, String upload) {
        super(url, uFileRequest, callback);
        this.upload = upload;
    }

    @Override
    protected void onWrite(OutputStream outputStream) throws Exception {
        Log.i(TAG, "onWrite length=" + upload.length() + " " + upload);
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(
                    new OutputStreamWriter(outputStream, "UTF-8"));
            writer.write(upload);
        } finally {
            if (writer != null) {
                writer.flush();
                writer.close();
            }
        }
    }

    @Override
    protected String getPutPolicy() {
        String callbackUrl = uFileRequest.getCallbackUrl();
        String callbackBody = uFileRequest.getCallbackBody();
        String callbackMethod = uFileRequest.getCallbackMethod();

        if (TextUtils.isEmpty(callbackUrl) || TextUtils.isEmpty(callbackMethod)) {
            return super.getPutPolicy();
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("callbackUrl", callbackUrl);
            jsonObject.put("callbackBody", callbackBody);
            jsonObject.put("callbackMethod", callbackMethod);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return Base64.encodeToString(jsonObject.toString().getBytes(), Base64.URL_SAFE | Base64.NO_WRAP);
    }
}
