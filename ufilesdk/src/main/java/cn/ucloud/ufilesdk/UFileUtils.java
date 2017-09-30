package cn.ucloud.ufilesdk;

import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by jerry on 15/12/15.
 */
public class UFileUtils {

    private final static String TAG = UFileUtils.class.getSimpleName();

    private final static String MD5 = "MD5";
    private final static String SHA1 = "SHA-1";

    public static String readString(InputStream inputStream) throws Exception{
        BufferedInputStream stream = new BufferedInputStream(inputStream);
        StringBuilder answer = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
        String line;
        while ((line = reader.readLine()) != null) {
            answer.append(line);
        }
        reader.close();
        return answer.toString();
    }

    public static JSONObject passHeaders(Map<String, List<String>> map) throws JSONException {
        JSONObject headers = new JSONObject();
        for (Map.Entry<String, List<String>> k : map.entrySet()) {
            int len = k.getValue().size();
            if (k.getKey() == null){
                headers.put("http", k.getValue());
                continue;
            }
            if (len == 1){
                headers.put(k.getKey().toLowerCase(), k.getValue().get(0));
            }else if (len > 1){
                JSONArray arr = new JSONArray();
                for (String value : k.getValue()){
                    arr.put(value);
                }
                headers.put(k.getKey().toLowerCase(), arr);
            }else {
                Log.e(TAG, k.getKey().toLowerCase() + " error no value");
            }
        }
        return headers;
    }

    public static String calcSha1(File file) {
        long fileLength = file.length();
        if (fileLength <= 4*1024*1024) {
            return smallFileSha1(file);
        } else {
            return largeFileSha1(file);
        }
    }

    public static String largeFileSha1(File file) {
        InputStream inputStream = null;
        try {
            MessageDigest gsha1 = MessageDigest.getInstance("SHA1");
            MessageDigest sha1 = MessageDigest.getInstance("SHA1");
            inputStream = new BufferedInputStream(new FileInputStream(file));
            byte[] buffer = new byte[1024];
            int nRead = 0;
            int block = 0;
            int count = 0;
            final int BLOCK_SIZE = 4*1024*1024;
            while ((nRead = inputStream.read(buffer)) != -1) {
                count += nRead;
                sha1.update(buffer, 0, nRead);
                if (BLOCK_SIZE == count) {
                    gsha1.update(sha1.digest());
                    sha1 = MessageDigest.getInstance("SHA1");
                    block++;
                    count = 0;
                }
            }
            if (count != 0) {
                gsha1.update(sha1.digest());
                block++;
            }
            byte[] digest = gsha1.digest();

            byte[] blockBytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(block).array();

            byte[] result = ByteBuffer.allocate(4 + digest.length)
                    .put(blockBytes, 0, blockBytes.length)
                    .put(digest, 0, digest.length)
                    .array();

            return Base64.encodeToString(result, Base64.URL_SAFE | Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static String smallFileSha1(File file) {
        InputStream inputStream = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            inputStream = new BufferedInputStream(new FileInputStream(file));
            byte[] buffer = new byte[1024];
            int nRead = 0;
            while ((nRead = inputStream.read(buffer)) != -1) {
                md.update(buffer, 0, nRead);
            }
            byte[] digest = md.digest();
            byte[] blockBytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(1).array();
            byte[] result = ByteBuffer.allocate(4+digest.length)
                    .put(blockBytes, 0, 4)
                    .put(digest, 0, digest.length)
                    .array();
            return Base64.encodeToString(result, Base64.URL_SAFE | Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static byte[] hmacSha1(String key, String value)
            throws UnsupportedEncodingException, NoSuchAlgorithmException,
            InvalidKeyException {
        String type = "HmacSHA1";
        SecretKeySpec secret = new SecretKeySpec(key.getBytes(), type);
        Mac mac = Mac.getInstance(type);
        mac.init(secret);
        return mac.doFinal(value.getBytes());
    }

    public static String getFileMD5(File file){
        return toHexString(getFileDigest(file, MD5));
    }

    public static String getFileSHA1(File file){
        return toHexString(getFileDigest(file, SHA1));
    }

    public static byte[] getFileDigest(File file, String algorithm) {
        InputStream fis = null;
        byte[] buffer = new byte[1024];
        int numRead;
        MessageDigest md;
        try{
            fis = new FileInputStream(file);
            md = MessageDigest.getInstance(algorithm);
            while((numRead=fis.read(buffer)) > 0) {
                md.update(buffer,0,numRead);
            }
            fis.close();
            return md.digest();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                }
            }
        }
        return null;
    }

    public static final char HEX_DIGITS[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8','9', 'a', 'b', 'c', 'd', 'e', 'f'};
    //转化成16进制
    public static String toHexString(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (int i = 0; i < b.length; i++) {
            sb.append(HEX_DIGITS[(b[i]& 0xf0) >>> 4]);
            sb.append(HEX_DIGITS[b[i] & 0x0f]);
        }
        return sb.toString();
    }

    public static String urlEncode(String key){
        String result = key;
        try {
            result = URLEncoder.encode(key, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

}
