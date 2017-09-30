package cn.ucloud.ufilesdk.task;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import cn.ucloud.ufilesdk.UFileRequest;
import cn.ucloud.ufilesdk.UFileSDK;
import cn.ucloud.ufilesdk.UFileUtils;

/**
 * Created by jerry on 15/12/14.
 */
public class HttpAsyncTask extends AsyncTask<Object, Object, Object> {

    private final static String TAG = HttpAsyncTask.class.getSimpleName();
    public final static String READ = "read";
    public final static String WRITE = "write";
    protected UFileRequest uFileRequest;
    private String url;
    private HttpCallback callback;

    public HttpAsyncTask(String url, UFileRequest uFileRequest, HttpCallback callback) {
        this.url = url;
        this.uFileRequest = uFileRequest;
        this.callback = callback;
    }

    public boolean isRunning() {
        return this.getStatus() == Status.RUNNING;
    }

    public void cancel() {
        Log.i(TAG, "user cancel" + this.getStatus());
        this.cancel(false);
    }

    @Override
    protected Object doInBackground(Object... params) {
        String authorization = getAuthorization(uFileRequest);
        String putPolicy = getPutPolicy();
        if (!TextUtils.isEmpty(putPolicy)) {
            authorization = authorization + ":" + putPolicy;
            Log.i(TAG, "authorization: " + authorization);
        }
        
        HttpURLConnection conn = null;
        JSONObject response = new JSONObject();
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();

            conn.setRequestProperty("UserAgent", "UFile Android/" + UFileSDK.VERSION_NAME);

            conn.setRequestProperty("Content-Type", "");
            if (!TextUtils.isEmpty(authorization))
                conn.setRequestProperty("Authorization", authorization);
            if (uFileRequest.getContentMD5() != null)
                conn.setRequestProperty("Content-MD5", uFileRequest.getContentMD5());
            if (uFileRequest.getContentType() != null)
                conn.setRequestProperty("Content-Type", uFileRequest.getContentType());
            else conn.setRequestProperty("Content-Type", "");
            String method = uFileRequest.getHttpMethod();
            conn.setRequestMethod(method);
            if (!method.equals("GET") && !method.equals("HEAD") && !method.equals("DELETE")) {
                conn.setDoInput(true);
                conn.setDoOutput(true);
                onWrite(conn.getOutputStream());
            }
            int httpCode = conn.getResponseCode();
            response.put("httpCode", httpCode);
            Map<String, List<String>> responseHeaders = conn.getHeaderFields();
            JSONObject response_headers = UFileUtils.passHeaders(responseHeaders);
            response.put("headers", response_headers);
            InputStream is;
            if (httpCode == HttpURLConnection.HTTP_OK || httpCode == HttpURLConnection.HTTP_NO_CONTENT) {
                is = conn.getInputStream();
            } else {
                is = conn.getErrorStream();
            }
            int response_length = conn.getContentLength();
            if (response_length > 0) {
                onRead(is, response);
            }
            Log.i(TAG, "response " + response);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return response;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    protected void onWrite(OutputStream outputStream) throws Exception {
    }

    protected void onRead(InputStream inputStream, JSONObject response) throws Exception {
        BufferedInputStream stream = new BufferedInputStream(inputStream);
        StringBuilder answer = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
        String line;
        while ((line = reader.readLine()) != null && !isCancelled()) {
            answer.append(line);
        }
        reader.close();
        String as = answer.toString();
        if (!as.isEmpty()) {
            if ("application/json".equals(response.getJSONObject("headers").getString("Content-Type"))) {
                JSONObject body = new JSONObject(as);
                response.put("body", body);
            } else {
                response.put("body", as);
            }
        } else {
            Log.e(TAG, "read null!!!");
        }
    }

    protected String getAuthorization(UFileRequest uFileRequest) {
        if (!UFileRequest.authServer.endsWith("/")) {
            UFileRequest.authServer += "/";
        }
        String url = UFileRequest.authServer + "token_server.php"
                + "?method=" + uFileRequest.getHttpMethod()
                + "&bucket=" + UFileRequest.bucket
                + "&key=" + uFileRequest.getKeyName()
                + "&content_md5=" + uFileRequest.getContentMD5()
                + "&content_type=" + uFileRequest.getContentType()
                + "&date=" + uFileRequest.getDate()
                + "&put_policy=" + getPutPolicy();

        HttpURLConnection conn = null;
        StringBuilder auth = new StringBuilder();
        BufferedReader reader = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");

            int httpCode = conn.getResponseCode();
            if (httpCode == HttpURLConnection.HTTP_OK) {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                String line;
                while ((line = reader.readLine()) != null) {
                    auth.append(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (conn != null) {
                    conn.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return auth.toString();
    }

    protected String getPutPolicy() {
        return "";
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("message", "user cancel, response is null");
            callback.onPostExecute(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onProgressUpdate(Object... progress) {
        super.onProgressUpdate(progress);
        callback.onProgressUpdate(progress);
    }

    @Override
    protected void onPostExecute(Object response) {
        super.onPostExecute(response);
        if (response == null) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("message", "http async task on post execute, response is null");
                callback.onPostExecute(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            callback.onPostExecute((JSONObject) response);
        }
    }

    public interface HttpCallback {
        //UI thread
        void onProgressUpdate(Object... progress);

        //UI thread
        void onPostExecute(JSONObject response);
    }
}
