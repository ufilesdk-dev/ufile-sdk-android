package cn.ucloud.ufilesdk;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jerry on 15/12/16.
 */
public class UFilePart implements Serializable {
    private String uploadId;
    private long blkSize;
    private String bucket;
    private String key;

    private Map<Integer, String> etags;

    public UFilePart() {
    }

    public String getUploadId() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    public long getBlkSize() {
        return blkSize;
    }

    public void setBlkSize(long blkSize) {
        this.blkSize = blkSize;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setEtags() {
        this.etags = new HashMap<>();
    }

    public void addEtag(int partNumber, String etag) {
        etags.put(partNumber, etag);
    }

    public String getEtags() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < etags.size(); i++) {
            sb.append(etags.get(i));
            if (i != etags.size() - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    /**
     * 获取当前分片进度
     * @return 当前以上传或下载分片个数
     */
    public int getProcess(){
        return etags.size();
    }

    @Override
    public String toString() {
        return "UFilePart{" +
                "uploadId='" + uploadId + '\'' +
                ", blkSize=" + blkSize +
                ", bucket='" + bucket + '\'' +
                ", key='" + key + '\'' +
                '}';
    }
}
