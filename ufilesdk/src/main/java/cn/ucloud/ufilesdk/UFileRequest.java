package cn.ucloud.ufilesdk;

import java.io.Serializable;

/**
 * UFileRequest
 *
 * Name	            Type	Description	                Required
 * HttpMethod	    String	http method 	              Yes
 * KeyName          String  待上传文件名                    Yes
 * Content-Type	    String	请求body部分即待上传文件的类型    No
 * Content-MD5	    String	文件内容的MD5摘要	              No
 *
 * 上传策略
 *
 * CallbackUrl      String  上传策略回调url                 No
 * CallbackBody     String  上传策略回调body                No
 * CallbackMethod   String  上传策略请求方式                No
 *
 * Created by jerry on 15/12/17.
 */
public class UFileRequest implements Serializable {

    // bucket name
    public static String bucket;

    // signature server
    public static String authServer;

    // http request method
    private String httpMethod;

    // http headers
    private String contentType;
    private String contentMD5;

    // auth parameter
    private String date;
    private String keyName;

    // put policy(上传策略)
    private String callbackUrl;
    private String callbackBody;
    private String callbackMethod;

    public String getContentType() {
        return contentType == null ? "" : contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContentMD5() {
        return contentMD5 == null ? "" : contentMD5;
    }

    public void setContentMD5(String contentMD5) {
        this.contentMD5 = contentMD5;
    }

    public String getHttpMethod() {
        return httpMethod == null ? "" : httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getDate() {
        return date == null ? "" : date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getKeyName() {
        return keyName == null ? "" : keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    public String getCallbackUrl() {
        return callbackUrl == null ? "" : callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public String getCallbackBody() {
        return callbackBody == null ? "" : callbackBody;
    }

    public void setCallbackBody(String callbackBody) {
        this.callbackBody = callbackBody;
    }

    public String getCallbackMethod() {
        return callbackMethod == null ? "" : callbackMethod;
    }

    public void setCallbackMethod(String callbackMethod) {
        this.callbackMethod = callbackMethod;
    }

    @Override
    public String toString() {
        return "UFileRequest{" +
                "httpMethod='" + httpMethod + '\'' +
                ", contentType='" + contentType + '\'' +
                ", contentMD5='" + contentMD5 + '\'' +
                ", date='" + date + '\'' +
                ", keyName='" + keyName + '\'' +
                ", callbackUrl='" + callbackUrl + '\'' +
                ", callbackBody='" + callbackBody + '\'' +
                ", callbackMethod='" + callbackMethod + '\'' +
                '}';
    }
}
