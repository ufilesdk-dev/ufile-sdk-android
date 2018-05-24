package com.example.ufilesdk;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.ucloud.ufilesdk.Callback;
import cn.ucloud.ufilesdk.UFilePart;
import cn.ucloud.ufilesdk.UFileRequest;
import cn.ucloud.ufilesdk.UFileSDK;
import cn.ucloud.ufilesdk.UFileUtils;
import cn.ucloud.ufilesdk.task.HttpAsyncTask;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    /**
     * 先用内置签名算法计算签名，传入publicToken和privateToken测试demo的功能是否可用
     * 再用服务器计算签名，部署好服务器，传入authServer测试签名服务器是否可用
     *
     * bucket : bucket name 必填参数
     * proxySuffix : 域名后缀 默认域名后缀.ufile.ucloud.cn
     * authServer : 签名服务器url（服务器计算签名必填参数）
     * publicToken : 公钥（内置签名算法计算签名必填参数）
     * privateToken : 私钥（内置签名算法计算签名必填参数）
     */
    private static final String bucket = "xxz001";
    private static final String proxySuffix = ".ufile.ucloud.cn";//使用默认域名后缀 .ufile.ucloud.cn
    private static final String authServer = "http://192.168.160.63:8008";
    private static final String publicToken = "TOKEN_48b6c1e0-3a6c-42b5-9853-4d25cc22927b";
    private static final String privateToken = "eda25855-dc0c-40a7-be17-adf7a6bc5d59";

    private String testKey = "123.jpg";//sdcard 根目录放置文件123.jpg

    //test file
    private File testFile = getTestFile();
    private ProgressDialog progressDialog;
    private Button initiateMultipartUpload, uploadPart, uploadPartRetry, finishMultipartUpload, abortMultipartUpload;
    private Button init, upload, finish, abort;

    private UFileSDK uFileSDK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById();
        progressDialog = new ProgressDialog(this);
        //init ufile sdk
        //内置签名算法计算签名
        uFileSDK = new UFileSDK(bucket, proxySuffix, publicToken, privateToken);
        //服务器计算签名
//        uFileSDK = new UFileSDK(bucket, proxySuffix, authServer);
    }

    public void putFile(View view) {
        String http_method = "PUT";
        String content_md5 = UFileUtils.getFileMD5(testFile);
        String content_type = "text/plain";
        String date = "";
        String key_name = testFile.getName();

        final UFileRequest request = new UFileRequest();
        request.setHttpMethod(http_method);
        request.setContentMD5(content_md5);
        request.setContentType(content_type);
        request.setDate(date);
        request.setKeyName(key_name);

        final ProgressDialog dialog = new ProgressDialog(this);
        final HttpAsyncTask httpAsyncTask = uFileSDK.putFile(request, testFile, key_name, new Callback() {
            @Override
            public void onSuccess(JSONObject response) {
                Log.i(TAG, "onSuccess " + response);
                dialog.dismiss();
                showDialog("success " + response.toString());
            }

            @Override
            public void onProcess(long len) {
                int value = (int) (len * 100 / testFile.length());
                dialog.setProgress(value);
                Log.i(TAG, "progress value is " + value);
            }

            @Override
            public void onFail(JSONObject response) {
                Log.i(TAG, "onFail " + response);
                dialog.dismiss();
                showDialog(response.toString());
            }
        });

        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setMax(100);
        dialog.setButton(DialogInterface.BUTTON_NEUTRAL, "取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                httpAsyncTask.cancel();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void uploadHit(View view) {
        String http_method = "POST";
        String key_name = testFile.getName();

        UFileRequest request = new UFileRequest();
        request.setHttpMethod(http_method);
        request.setKeyName(key_name);

        final HttpAsyncTask httpAsyncTask = uFileSDK.uploadHit(request, testFile, getDefaultCallback());
        showProcessDialog(httpAsyncTask);
    }

    public void getFile(View view) {
        final String http_method = "GET";
        String key_name = testKey;

        UFileRequest request = new UFileRequest();
        request.setHttpMethod(http_method);
        request.setKeyName(key_name);

        final ProgressDialog dialog = new ProgressDialog(this);

        final HttpAsyncTask httpAsyncTask = uFileSDK.getFile(request, key_name, getSaveFile(), new Callback() {
            @Override
            public void onSuccess(JSONObject response) {
                Log.i(TAG, "onSuccess " + response);
                dialog.dismiss();
                showDialog("success " + response.toString());
            }

            @Override
            public void onProcess(long len) {
                int value = (int) (len * 100 / testFile.length());
                dialog.setProgress(value);
                Log.i(TAG, "progress value is " + value);
            }

            @Override
            public void onFail(JSONObject response) {
                Log.i(TAG, "onFail " + response);
                dialog.dismiss();
                showDialog(response.toString());
            }
        });
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setMax(100);
        dialog.setButton(DialogInterface.BUTTON_NEUTRAL, "取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                httpAsyncTask.cancel();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void headFile(View view) {
        String http_method = "HEAD";
        String key_name = testKey;

        UFileRequest request = new UFileRequest();
        request.setHttpMethod(http_method);
        request.setKeyName(key_name);

        HttpAsyncTask httpAsyncTask = uFileSDK.headFile(request, key_name, getDefaultCallback());
        showProcessDialog(httpAsyncTask);
    }

    public void deleteFile(View view) {
        String http_method = "DELETE";
        String key_name = testKey;

        UFileRequest request = new UFileRequest();
        request.setHttpMethod(http_method);
        request.setKeyName(key_name);

        HttpAsyncTask httpAsyncTask = uFileSDK.deleteFile(request, key_name, getDefaultCallback());
        showProcessDialog(httpAsyncTask);
    }

    //分片相关
    private UFilePart uFilePart = null;
    private File partFile = getTestFile();

    public void initiateMultipartUpload(View view) {
        String http_method = "POST";
        String key_name = partFile.getName();

        UFileRequest request = new UFileRequest();
        request.setHttpMethod(http_method);
        request.setKeyName(key_name);

        HttpAsyncTask httpAsyncTask = uFileSDK.initiateMultipartUpload(request, key_name, new Callback() {
            @Override
            public void onSuccess(JSONObject response) {
                Log.i(TAG, "onSuccess " + response);
                uFilePart = new UFilePart();
                try {
                    JSONObject message = response.getJSONObject("message");
                    uFilePart.setUploadId(message.getString("UploadId"));
                    uFilePart.setBlkSize(Long.valueOf(message.getString("BlkSize")));
                    uFilePart.setBucket(message.getString("Bucket"));
                    uFilePart.setKey(message.getString("Key"));
                    uFilePart.setEtags();
                    Log.e(TAG, uFilePart.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                uploadPart.setEnabled(true);
                uploadPartRetry.setEnabled(true);
                abortMultipartUpload.setEnabled(true);

                progressDialog.dismiss();
                showDialog("success" + response.toString());
            }

            @Override
            public void onProcess(long len) {

            }

            @Override
            public void onFail(JSONObject response) {
                Log.i(TAG, "onFail " + response);
                progressDialog.dismiss();
                showDialog(response.toString());
            }
        });
        showProcessDialog(httpAsyncTask);
    }

    private int count = 0;

    public void uploadPart(View view) {
        if (uFilePart == null) {
            return;
        }
        uploadPartRetry.setEnabled(false);
        final String http_method = "PUT";
        String key_name = partFile.getName();
        String content_type = "application/octet-stream";

        long blk_size = uFilePart.getBlkSize();
        long file_len = partFile.length();

        final int part = (int) Math.ceil(file_len / blk_size);

        final List<HttpAsyncTask> list = new ArrayList<>();
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setMax(part + 1);

        for (int i = 0; i <= part; i++) {
            UFileRequest request = new UFileRequest();
            request.setHttpMethod(http_method);
            request.setContentType(content_type);
            request.setKeyName(key_name);

            HttpAsyncTask httpAsyncTask = uFileSDK.uploadPart(request, key_name, uFilePart.getUploadId(), partFile, i, uFilePart.getBlkSize(), new Callback() {
                @Override
                public void onSuccess(JSONObject response) {
                    Log.i(TAG, "onSuccess " + response);
                    try {
                        String etag = response.getString("ETag");
                        int partNumber = response.getJSONObject("message").getInt("PartNumber");
                        uFilePart.addEtag(partNumber, etag);
                        if (count < part) {
                            count++;
                            dialog.setProgress(count);
                        } else {
                            count = 0;
                            dialog.dismiss();

                            finishMultipartUpload.setEnabled(true);

                            showDialog("success " + response.toString());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onProcess(long len) {

                }

                @Override
                public void onFail(JSONObject response) {
                    Log.i(TAG, "onFail " + response);
                    dialog.dismiss();
                    showDialog(response.toString());
                }
            });
            list.add(httpAsyncTask);
        }
        dialog.setButton(DialogInterface.BUTTON_NEUTRAL, "取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for (HttpAsyncTask httpAsyncTask : list) {
                    if (httpAsyncTask.isRunning())
                        httpAsyncTask.cancel();
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void uploadPartRetry(View view) {
        if (uFilePart == null) {
            return;
        }
        uploadPart.setEnabled(false);
        final String http_method = "PUT";
        String key_name = partFile.getName();
        String content_type = "application/octet-stream";

        long blk_size = uFilePart.getBlkSize();
        long file_len = partFile.length();

        final int part = (int) Math.ceil(file_len / blk_size);

        final List<UFileSDK.UploadPartManager> list = new ArrayList<>();
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setMax(part + 1);

        for (int i = 0; i <= part; i++) {
            UFileRequest request = new UFileRequest();
            request.setHttpMethod(http_method);
            request.setContentType(content_type);
            request.setKeyName(key_name);

            UFileSDK.UploadPartManager uploadPartManager = uFileSDK.uploadPart(request, key_name, uFilePart.getUploadId(), partFile, i, uFilePart.getBlkSize(), new Callback() {
                @Override
                public void onSuccess(JSONObject response) {
                    Log.i(TAG, "onSuccess " + response);
                    try {
                        String etag = response.getString("ETag");
                        int partNumber = response.getJSONObject("message").getInt("PartNumber");
                        uFilePart.addEtag(partNumber, etag);
                        if (count < part) {
                            count++;
                            dialog.setProgress(count);
                        } else {
                            count = 0;
                            dialog.dismiss();

                            finishMultipartUpload.setEnabled(true);

                            showDialog("success " + response.toString());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onProcess(long len) {

                }

                @Override
                public void onFail(JSONObject response) {
                    Log.i(TAG, "onFail " + response);
                    dialog.dismiss();
                    showDialog(response.toString());
                }
            }, 3, 1000, new Handler());
            list.add(uploadPartManager);
        }
        dialog.setButton(DialogInterface.BUTTON_NEUTRAL, "取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for (UFileSDK.UploadPartManager uploadPartManager : list) {
                    uploadPartManager.stop();
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void finishMultipartUpload(View view) {
        if (uFilePart == null) {
            return;
        }
        String http_method = "POST";
        String key_name = partFile.getName();
        String content_type = "text/plain";
        String etags = uFilePart.getEtags();

        String callbackUrl = "http://api.ucloud.cn";
        String callbackBody = "";
        String callbackMethod = "GET";

        UFileRequest request = new UFileRequest();
        request.setHttpMethod(http_method);
        request.setContentType(content_type);
        request.setKeyName(key_name);
        request.setCallbackUrl(callbackUrl);
        request.setCallbackBody(callbackBody);
        request.setCallbackMethod(callbackMethod);

        HttpAsyncTask httpAsyncTask = uFileSDK.finishMultipartUpload(request, key_name, uFilePart.getUploadId(), etags, "new_" + key_name, getDefaultCallback());
        showProcessDialog(httpAsyncTask);

        uploadPart.setEnabled(false);
        uploadPartRetry.setEnabled(false);
        finishMultipartUpload.setEnabled(false);
        abortMultipartUpload.setEnabled(false);

        uFilePart = null;
    }

    public void abortMultipartUpload(View view) {
        if (uFilePart == null) {
            return;
        }
        String http_method = "DELETE";
        String key_name = partFile.getName();

        UFileRequest request = new UFileRequest();
        request.setHttpMethod(http_method);
        request.setKeyName(key_name);

        HttpAsyncTask httpAsyncTask = uFileSDK.abortMultipartUpload(request, key_name, uFilePart.getUploadId(), getDefaultCallback());
        showProcessDialog(httpAsyncTask);

        uploadPart.setEnabled(false);
        uploadPartRetry.setEnabled(false);
        finishMultipartUpload.setEnabled(false);
        abortMultipartUpload.setEnabled(false);

        uFilePart = null;
    }

    private File getTestFile() {
        File sdcard = Environment.getExternalStorageDirectory();
        return new File(sdcard, testKey);
    }

    private File getTestFile(String name) {
        File sdcard = Environment.getExternalStorageDirectory();
        return new File(sdcard, name);
    }

    private File getSaveFile() {
        File sdcard = Environment.getExternalStorageDirectory();
        File download = new File(sdcard, "ufiledown");
        if (!download.exists()) {
            download.mkdir();
        }
        return new File(download, testKey);
    }

    private Callback getDefaultCallback() {
        Callback callback = new Callback() {
            @Override
            public void onSuccess(JSONObject response) {
                Log.i(TAG, "onSuccess " + response);
                if (progressDialog != null && progressDialog.isShowing())
                    progressDialog.dismiss();
                showDialog("success " + response.toString());
            }

            @Override
            public void onProcess(long len) {

            }

            @Override
            public void onFail(JSONObject response) {
                Log.i(TAG, "onFail " + response);
                if (progressDialog != null && progressDialog.isShowing())
                    progressDialog.dismiss();
                showDialog(response.toString());
            }
        };

        return callback;
    }

    private void showProcessDialog(final HttpAsyncTask httpAsyncTask) {
        progressDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (httpAsyncTask != null)
                    httpAsyncTask.cancel();
                dialog.dismiss();
            }
        });
        progressDialog.show();
    }

    private void showDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setMessage(message);
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void findViewById() {
        initiateMultipartUpload = (Button) findViewById(R.id.initiateMultipartUpload);
        uploadPart = (Button) findViewById(R.id.uploadPart);
        finishMultipartUpload = (Button) findViewById(R.id.finishMultipartUpload);
        abortMultipartUpload = (Button) findViewById(R.id.abortMultipartUpload);
        uploadPartRetry = (Button) findViewById(R.id.uploadPartRetry);
    }

}
