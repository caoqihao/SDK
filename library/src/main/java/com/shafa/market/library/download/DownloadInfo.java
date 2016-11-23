package com.shafa.market.library.download;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by caoqihao on 2016/11/15.
 */

public class DownloadInfo implements Parcelable {
    public DownloadInfo() {
    }

    public String mUri;
    public String mPackageName;
    public long mCurrentBytes;
    public long mTotalBytes;
    public String mFilePath;
    public int mStatus;

    /**
     * 下载中
     */
    public static final int STATUS_DOWNLOADING = 1001;
    /**
     * 下载暂停
     */
    public static final int STATUS_PAUSE = 1002;
    /**
     * 下载完成
     */
    public static final int STATUS_FINISH = 1003;
    /**
     * 下载失败
     */
    public static final int STATUS_FAILED = 1004;

    public void setCurrentBytes(long mCurrentBytes) {
        this.mCurrentBytes = mCurrentBytes;
    }

    public void setTotalBytes(long mTotalBytes) {
        this.mTotalBytes = mTotalBytes;
    }

    public long getCurrentBytes() {
        return mCurrentBytes;
    }

    public long getTotalBytes() {
        return mTotalBytes;
    }

    protected DownloadInfo(Parcel in) {
        mUri = in.readString();
        mPackageName = in.readString();
        mCurrentBytes = in.readLong();
        mTotalBytes = in.readLong();
        mFilePath = in.readString();
        mStatus = in.readInt();
    }

    public static final Creator<DownloadInfo> CREATOR = new Creator<DownloadInfo>() {
        @Override
        public DownloadInfo createFromParcel(Parcel in) {
            return new DownloadInfo(in);
        }

        @Override
        public DownloadInfo[] newArray(int size) {
            return new DownloadInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mUri);
        dest.writeString(mPackageName);
        dest.writeLong(mCurrentBytes);
        dest.writeLong(mTotalBytes);
        dest.writeString(mFilePath);
        dest.writeInt(mStatus);
    }
}
