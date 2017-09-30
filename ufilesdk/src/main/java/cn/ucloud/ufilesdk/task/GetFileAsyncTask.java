package cn.ucloud.ufilesdk.task;

import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;

import cn.ucloud.ufilesdk.UFileRequest;

/**
 * Created by jerry on 15/12/14.
 */
public class GetFileAsyncTask extends HttpAsyncTask {

    private final static String TAG = GetFileAsyncTask.class.getSimpleName();

    private File file;

    public GetFileAsyncTask(String url, UFileRequest uFileRequest, File file, HttpCallback callback) {
        super(url, uFileRequest, callback);
        this.file = file;
    }

    @Override
    protected void onRead(InputStream inputStream, JSONObject response) throws Exception {
        if(file.exists()) file.delete();
        RandomAccessFile accessFile = null;
        try {
            accessFile = new RandomAccessFile(file, "rwd");
            byte[] buffer = new byte[1024];
            int len;
            long total = 0;
            while ((len = inputStream.read(buffer)) != -1 && !isCancelled()) {
                total += len;
                accessFile.write(buffer, 0, len);
                publishProgress(READ, total);
            }
        } finally {
            if (accessFile != null)
                accessFile.close();
        }
    }
}
