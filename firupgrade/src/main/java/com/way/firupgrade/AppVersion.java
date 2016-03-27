package com.way.firupgrade;

/**
 * Created by way on 16/3/27.
 */
public class AppVersion {
    private int versionCode;
    private String versionName;
    private String changeLog;
    private String updateUrl;
    private long fileSize;
    private long updatedTime;

    public AppVersion(int versionCode, String versionName, String changeLog, String updateUrl, long fileSize, long updatedTime) {
        this.versionCode = versionCode;
        this.versionName = versionName;
        this.changeLog = changeLog;
        this.updateUrl = updateUrl;
        this.fileSize = fileSize;
        this.updatedTime = updatedTime;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getChangeLog() {
        return changeLog;
    }

    public void setChangeLog(String changeLog) {
        this.changeLog = changeLog;
    }

    public String getUpdateUrl() {
        return updateUrl;
    }

    public void setUpdateUrl(String updateUrl) {
        this.updateUrl = updateUrl;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public long getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(long updatedTime) {
        this.updatedTime = updatedTime;
    }

    @Override
    public String toString() {
        return "AppVersion{" +
                "versionCode=" + versionCode +
                ", versionName='" + versionName + '\'' +
                ", changeLog='" + changeLog + '\'' +
                ", updateUrl='" + updateUrl + '\'' +
                ", fileSize='" + fileSize + '\'' +
                '}';
    }
}
