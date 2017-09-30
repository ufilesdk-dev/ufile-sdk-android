package cn.ucloud.ufilesdk.task;

import android.text.TextUtils;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;

import cn.ucloud.ufilesdk.UFileRequest;

/**
 * Created by jerry on 15/12/14.
 */
public class PutFileAsyncTask extends HttpAsyncTask {

    private final static String TAG = PutFileAsyncTask.class.getSimpleName();

    private File file;

    public PutFileAsyncTask(String url, UFileRequest uFileRequest, File file, HttpCallback callback) {
        super(url, uFileRequest, callback);
        this.file = file;
    }

    @Override
    protected void onWrite(OutputStream outputStream) throws Exception {
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            byte[] bytes = new byte[1024];
            int len;
            long total = 0;
            while ((len = fileInputStream.read(bytes)) != -1 && !isCancelled()) {
                total += len;
                dataOutputStream.write(bytes, 0, len);
                publishProgress(WRITE, total);
            }
        } finally {
            if (fileInputStream != null)
                fileInputStream.close();
            dataOutputStream.flush();
            dataOutputStream.close();
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
