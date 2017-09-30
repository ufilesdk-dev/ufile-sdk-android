package cn.ucloud.ufilesdk;

import android.os.AsyncTask;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.HttpURLConnection;

import cn.ucloud.ufilesdk.task.GetFileAsyncTask;
import cn.ucloud.ufilesdk.task.HttpAsyncTask;
import cn.ucloud.ufilesdk.task.PutFileAsyncTask;
import cn.ucloud.ufilesdk.task.UploadPartAsyncTask;
import cn.ucloud.ufilesdk.task.WriteAsyncTask;


/**
 * Created by jerry on 15/12/11.
 */
public class UFileSDK {

    private static final String TAG = UFileSDK.class.getSimpleName();

    public static final String VERSION_NAME = "1.0.1";
    public static final int VERSION_CODE = 10001;

    private static final String DEFAULT_PROXY_SUFFFIX = ".ufile.ucloud.cn";

    private String defaultUrl;

    /**
     * sdk init
     *
     * @param bucket     bucket name
     * @param authServer 签名服务器
     */
    public UFileSDK(String bucket, String authServer) {
        this(bucket, DEFAULT_PROXY_SUFFFIX, authServer);
    }

    /**
     * sdk init
     *
     * @param bucket      bucket name
     * @param proxySuffix 域名后缀
     * @param authServer  签名服务器
     */
    public UFileSDK(String bucket, String proxySuffix, String authServer) {
        if (TextUtils.isEmpty(bucket) || TextUtils.isEmpty(proxySuffix) || TextUtils.isEmpty(authServer)) {
            throw new IllegalArgumentException("Bucket, proxySuffix and authServer should not be empty!");
        }
        this.defaultUrl = "http://" + bucket + proxySuffix;
        UFileRequest.bucket = bucket;
        UFileRequest.authServer = authServer;
    }

//    public HttpAsyncTask getAuthorization(UFileRequest uFileRequest, String key_name, final Callback callback) {
//        HttpAsyncTask httpAsyncTask = getDefaultHttpAsyncTask(this.defaultUrl + "/" + UFileUtils.urlEncode(key_name), uFileRequest, callback);
//        httpAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//        return httpAsyncTask;
//    }

    /**
     * 上传文件 - PutFile
     *
     * @param uFileRequest 请参考 UFileRequest
     * @param file         待上传的文件
     * @param key_name     待上传的文件名
     * @param callback     callback
     *                     void onSuccess(JSONObject message);  请参考 private void cb(JSONObject response, Callback callback)
     *                     void onProcess(long len);            已上传文件大小
     *                     void onFail(JSONObject message);  请参考 private void cb(JSONObject response, Callback callback)
     * @return http async task 用于取消掉当前任务
     */
    public HttpAsyncTask putFile(final UFileRequest uFileRequest, final File file, String key_name, final Callback callback) {
        String url = this.defaultUrl + "/" + UFileUtils.urlEncode(key_name);
        Log.i(TAG, url);
        HttpAsyncTask httpAsyncTask = new PutFileAsyncTask(url, uFileRequest, file, new HttpAsyncTask.HttpCallback() {
            @Override
            public void onProgressUpdate(Object... progress) {
                switch ((String) progress[0]) {
                    case HttpAsyncTask.WRITE:
                        callback.onProcess((long) progress[1]);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onPostExecute(JSONObject response) {
                cb(response, callback);
            }
        });
        httpAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        return httpAsyncTask;
    }

    /**
     * 秒传文件 - UploadHit
     *
     * @param uFileRequest 请参考 UFileRequest
     * @param file         待上传的文件
     * @param callback     callback
     *                     void onSuccess(JSONObject message);  请参考 private void cb(JSONObject response, Callback callback)
     *                     void onProcess(long len);            无
     *                     void onFail(JSONObject message);  请参考 private void cb(JSONObject response, Callback callback)
     * @return http async task 用于取消掉当前任务
     */
    public HttpAsyncTask uploadHit(UFileRequest uFileRequest, final File file, final Callback callback) {
        String url = this.defaultUrl + "/uploadhit?Hash=" + UFileUtils.calcSha1(file) + "&FileName=" + UFileUtils.urlEncode(file.getName()) + "&FileSize=" + file.length();
        HttpAsyncTask httpAsyncTask = getDefaultHttpAsyncTask(url, uFileRequest, callback);
        httpAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        return httpAsyncTask;
    }

    /**
     * 下载文件 - GetFile
     *
     * @param uFileRequest 请参考 UFileRequest
     * @param file_name    待下载的文件名
     * @param saveFile     下载后存储位置
     * @param callback     callback
     *                     void onSuccess(JSONObject message);  请参考 private void cb(JSONObject response, Callback callback)
     *                     void onProcess(long len);            已下载文件大小
     *                     void onFail(JSONObject message);  请参考 private void cb(JSONObject response, Callback callback)
     * @return http async task 用于取消掉当前任务
     */
    public HttpAsyncTask getFile(UFileRequest uFileRequest, String file_name, final File saveFile, final Callback callback) {
        String url = this.defaultUrl + "/" + UFileUtils.urlEncode(file_name);
        Log.i(TAG, url);
        HttpAsyncTask httpAsyncTask = new GetFileAsyncTask(url, uFileRequest, saveFile, new HttpAsyncTask.HttpCallback() {
            @Override
            public void onProgressUpdate(Object... progress) {
                switch ((String) progress[0]) {
                    case HttpAsyncTask.READ:
                        callback.onProcess((long) progress[1]);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onPostExecute(JSONObject response) {
                cb(response, callback);
            }
        });
        httpAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        return httpAsyncTask;
    }

    /**
     * 下载文件 - GetFile
     *
     * @param downloadUrl 待下载的文件名
     * @param saveFile    下载后存储位置
     * @param callback    callback
     *                    void onSuccess(JSONObject message);  请参考 private void cb(JSONObject response, Callback callback)
     *                    void onProcess(long len);            已下载文件大小
     *                    void onFail(JSONObject message);  请参考 private void cb(JSONObject response, Callback callback)
     * @return http async task 用于取消掉当前任务
     */
    public HttpAsyncTask getFile(String downloadUrl, final File saveFile, final Callback callback) {
        UFileRequest uFileRequest = new UFileRequest();
        uFileRequest.setHttpMethod("GET");

        HttpAsyncTask httpAsyncTask = new GetFileAsyncTask(downloadUrl, uFileRequest, saveFile, new HttpAsyncTask.HttpCallback() {
            @Override
            public void onProgressUpdate(Object... progress) {
                switch ((String) progress[0]) {
                    case HttpAsyncTask.READ:
                        callback.onProcess((long) progress[1]);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onPostExecute(JSONObject response) {
                cb(response, callback);
            }
        });
        httpAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        return httpAsyncTask;
    }

    /**
     * 查询文件基本信息 - HEADFile
     *
     * @param uFileRequest 请参考 UFileRequest
     * @param key_name     待查询文件名
     * @param callback     callback
     *                     void onSuccess(JSONObject message);  请参考 private void cb(JSONObject response, Callback callback)
     *                     void onProcess(long len);            无
     *                     void onFail(JSONObject message);  请参考 private void cb(JSONObject response, Callback callback)
     * @return http async task 用于取消掉当前任务
     */
    public HttpAsyncTask headFile(UFileRequest uFileRequest, String key_name, final Callback callback) {
        HttpAsyncTask httpAsyncTask = getDefaultHttpAsyncTask(this.defaultUrl + "/" + UFileUtils.urlEncode(key_name), uFileRequest, callback);
        httpAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        return httpAsyncTask;
    }

    /**
     * 删除文件 - DeleteFile
     *
     * @param uFileRequest 请参考 UFileRequest
     * @param key_name     待删除文件名
     * @param callback     callback
     *                     void onSuccess(JSONObject message);  请参考 private void cb(JSONObject response, Callback callback)
     *                     void onProcess(long len);            无
     *                     void onFail(JSONObject message);  请参考 private void cb(JSONObject response, Callback callback)
     * @return http async task 用于取消掉当前任务
     */
    public HttpAsyncTask deleteFile(UFileRequest uFileRequest, String key_name, final Callback callback) {
        HttpAsyncTask httpAsyncTask = getDefaultHttpAsyncTask(this.defaultUrl + "/" + UFileUtils.urlEncode(key_name), uFileRequest, callback);
        httpAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        return httpAsyncTask;
    }

    /**
     * 初始化分片 - InitiateMultipartUpload
     *
     * @param uFileRequest 请参考 UFileRequest
     * @param key_name     待上传文件名
     * @param callback     callback
     *                     void onSuccess(JSONObject message);  返回值请参考 private void cb(JSONObject response, Callback callback)
     *                     注意组 UFilePart 对象 具体参照demo
     *                     <p/>
     *                     void onProcess(long len);            无
     *                     void onFail(JSONObject message);  请参考 private void cb(JSONObject response, Callback callback)
     * @return http async task 用于取消掉当前任务
     */
    public HttpAsyncTask initiateMultipartUpload(UFileRequest uFileRequest, String key_name, final Callback callback) {
        HttpAsyncTask httpAsyncTask = getDefaultHttpAsyncTask(this.defaultUrl + "/" + UFileUtils.urlEncode(key_name) + "?uploads", uFileRequest, callback);
        httpAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        return httpAsyncTask;
    }

    /**
     * 上传分片 - UploadPart
     * <p/>
     * 仅提供上传某个分片功能
     *
     * @param uFileRequest 请参考 UFileRequest
     * @param key_name     待上传文件名
     * @param uploadId     initiateMultipartUpload中获得 当前upload id
     * @param file         待上传文件
     * @param partNumber   分片上传part number (注: 从0开始)
     * @param blk_size     initiateMultipartUpload中获得 每一个分片大小
     * @param callback     callback
     *                     void onSuccess(JSONObject message);  返回值请参考 private void cb(JSONObject response, Callback callback)
     *                     void onProcess(long len);            已上传文件大小
     *                     void onFail(JSONObject message);  请参考 private void cb(JSONObject response, Callback callback)
     * @return http async task 用于取消掉当前任务
     */
    public HttpAsyncTask uploadPart(UFileRequest uFileRequest, String key_name, String uploadId, final File file, final int partNumber, final long blk_size, final Callback callback) {
        String url = this.defaultUrl + "/" + UFileUtils.urlEncode(key_name) + "?uploadId=" + uploadId + "&partNumber=" + partNumber;
        Log.i(TAG, url);

        HttpAsyncTask httpAsyncTask = new UploadPartAsyncTask(url, uFileRequest, new HttpAsyncTask.HttpCallback() {
            @Override
            public void onProgressUpdate(Object... progress) {
                switch ((String) progress[0]) {
                    case HttpAsyncTask.WRITE:
                        callback.onProcess((long) progress[1]);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onPostExecute(JSONObject response) {
                cb(response, callback);
            }
        }, file, partNumber, blk_size);
        httpAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        return httpAsyncTask;
    }

    /**
     * 上传分片 - UploadPart
     * <p/>
     * 仅提供上传某个分片功能
     *
     * @param uFileRequest 请参考 UFileRequest
     * @param key_name     待上传文件名
     * @param uploadId     initiateMultipartUpload中获得 当前upload id
     * @param file         待上传文件
     * @param partNumber   分片上传part number (注: 从0开始)
     * @param blk_size     initiateMultipartUpload中获得 每一个分片大小
     * @param callback     callback
     *                     void onSuccess(JSONObject message);  返回值请参考 private void cb(JSONObject response, Callback callback)
     *                     void onProcess(long len);            已上传文件大小
     *                     void onFail(JSONObject message);  请参考 private void cb(JSONObject response, Callback callback)
     * @return http async task 用于取消掉当前任务
     */
    public UploadPartManager uploadPart(final UFileRequest uFileRequest,
                                    final String key_name,
                                    final String uploadId,
                                    final File file,
                                    final int partNumber,
                                    final long blk_size,
                                    final Callback callback,
                                    final int retryCount,
                                    final long retryTime,
                                    final Handler handler) {
        String url = this.defaultUrl + "/" + UFileUtils.urlEncode(key_name) + "?uploadId=" + uploadId + "&partNumber=" + partNumber;
        Log.i(TAG, url);
        UploadPartManager uploadPartManager = new UploadPartManager(this);
        uploadPartManager.start(uFileRequest, key_name, uploadId, file, partNumber, blk_size, callback, retryCount, retryTime, handler);
        return uploadPartManager;
    }

    public class UploadPartManager{

        private UFileSDK uFileSDK;
        private HttpAsyncTask httpAsyncTask;
        private boolean isStop = false;

        public UploadPartManager(UFileSDK uFileSDK){
            this.uFileSDK = uFileSDK;
        }

        public void start(final UFileRequest uFileRequest,
                          final String key_name,
                          final String uploadId,
                          final File file,
                          final int partNumber,
                          final long blk_size,
                          final Callback callback,
                          final int retryCount,
                          final long retryTime,
                          final Handler handler){
            httpAsyncTask = uFileSDK.uploadPart(uFileRequest, key_name, uploadId, file, partNumber, blk_size, new Callback() {
                @Override
                public void onSuccess(JSONObject message) {
                    callback.onSuccess(message);
                }

                @Override
                public void onProcess(long len) {

                }

                @Override
                public void onFail(JSONObject message) {
                    if (retryCount > 0 && !isStop) {
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                int count = retryCount - 1;
                                start(uFileRequest, key_name, uploadId, file, partNumber, blk_size, callback, count, retryTime, handler);
                            }
                        }, retryTime);
                    }else {
                        callback.onFail(message);
                    }
                }
            });
        }

        public void stop(){
            if (httpAsyncTask != null && httpAsyncTask.isRunning())
                this.httpAsyncTask.cancel();
            isStop = true;
        }
    }

    /**
     * 完成分片 - FinishMultipartUpload
     *
     * @param uFileRequest 请参考 UFileRequest
     * @param key_name     待上传文件名
     * @param uploadId     initiateMultipartUpload中获得 当前 upload id
     * @param etags        上传成功返回的etags 具体参照demo
     * @param new_key_name 分片上传part number (注: 从0开始)
     * @param callback     callback
     *                     void onSuccess(JSONObject message);  返回值请参考 private void cb(JSONObject response, Callback callback)
     *                     void onProcess(long len);            已上传文件大小
     *                     void onFail(JSONObject message);  请参考 private void cb(JSONObject response, Callback callback)
     * @return http async task 用于取消掉当前任务
     */
    public HttpAsyncTask finishMultipartUpload(final UFileRequest uFileRequest, String key_name, final String uploadId, final String etags, String new_key_name, final Callback callback) {
        String url = this.defaultUrl + "/" + UFileUtils.urlEncode(key_name) + "?uploadId=" + uploadId + "&newKey=" + new_key_name;
        Log.i(TAG, url);

        HttpAsyncTask httpAsyncTask = new WriteAsyncTask(url, uFileRequest, new HttpAsyncTask.HttpCallback() {
            @Override
            public void onProgressUpdate(Object... progress) {

            }

            @Override
            public void onPostExecute(JSONObject response) {
                cb(response, callback);
            }
        }, etags);
        httpAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        return httpAsyncTask;
    }

    /**
     * 放弃分片 - AbortMultipartUpload
     *
     * @param uFileRequest 请参考 UFileRequest
     * @param key_name     待上传文件名
     * @param uploadId     initiateMultipartUpload中获得 当前upload id
     * @param callback     callback
     *                     void onSuccess(JSONObject message);  返回值请参考 private void cb(JSONObject response, Callback callback)
     *                     void onProcess(long len);            无
     *                     void onFail(JSONObject message);  请参考 private void cb(JSONObject response, Callback callback)
     * @return http async task 用于取消掉当前任务
     */
    public HttpAsyncTask abortMultipartUpload(UFileRequest uFileRequest, String key_name, String uploadId, Callback callback) {
        String url = this.defaultUrl + "/" + UFileUtils.urlEncode(key_name) + "?uploadId=" + uploadId;
        HttpAsyncTask httpAsyncTask = getDefaultHttpAsyncTask(url, uFileRequest, callback);
        httpAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        return httpAsyncTask;
    }

    private HttpAsyncTask getDefaultHttpAsyncTask(String url, UFileRequest uFileRequest, final Callback callback) {
        Log.i(TAG, "url " + url);
        Log.i(TAG, "ufile request " + uFileRequest.toString());
        return new HttpAsyncTask(url, uFileRequest, new HttpAsyncTask.HttpCallback() {
            @Override
            public void onProgressUpdate(Object... progress) {

            }

            @Override
            public void onPostExecute(JSONObject response) {
                cb(response, callback);
            }
        });
    }

    /**
     * callback
     * <p/>
     * on Success
     * httpCode 200/204
     * ETag 上传成功 返回的文件ETag  算法参照 UFileSDK.calcSha1
     * message 成功返回的http body 可能不存在
     * 例:  {"httpCode":200,"ETag":"\"AQAAAGcc-_T1XXW2U0Qbu0RqSyCmCe1Y\""}
     * <p/>
     * on Fail
     * httpCode http code
     * X-SessionId 本次操作服务器可查询序列号
     * message 成功返回的http body 可能不存在
     * 例:  {"httpCode":403,"X-SessionId":"5e1f79c8-bda7-42b0-9ef7-c3d612bef458","message":{"RetCode":-148658,"ErrMsg":"invalid signature"}}
     *
     * @param response http response
     * @param callback callback
     */
    private void cb(JSONObject response, Callback callback) {
        try {
            if (response.has("httpCode") && (response.getInt("httpCode") == HttpURLConnection.HTTP_OK || response.getInt("httpCode") == HttpURLConnection.HTTP_NO_CONTENT)) {
                JSONObject message = new JSONObject();
                message.put("httpCode", response.getInt("httpCode"));
                if (response.has("headers")) {
                    JSONObject headers = response.getJSONObject("headers");
                    if (headers.has("etag"))
                        message.put("ETag", headers.getString("etag"));
                }
                if (response.has("body"))
                    message.put("message", response.getJSONObject("body"));
                Log.i(TAG, "cb " + message);
                callback.onSuccess(message);
            } else {
                JSONObject message = new JSONObject();
                if (response.has("httpCode"))
                    message.put("httpCode", response.getInt("httpCode"));
                if (response.has("headers")) {
                    JSONObject headers = response.getJSONObject("headers");
                    if (headers.has("x-sessionid"))
                        message.put("X-SessionId", response.getJSONObject("headers").getString("x-sessionid"));
                }
                if (response.has("body"))
                    message.put("message", response.getJSONObject("body"));
                if (response.has("message"))
                    message.put("message", response.getString("message"));
                Log.i(TAG, "cb " + message);
                callback.onFail(message);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("message", e.toString());
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            callback.onFail(jsonObject);
        }
    }
}
