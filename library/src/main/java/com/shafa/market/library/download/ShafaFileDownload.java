package com.shafa.market.library.download;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.shafa.market.library.manager.ShafaDownloadManager;
import com.shafa.market.library.net.INetBase;
import com.shafa.market.library.utils.Util;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;

/**
 * Created by caoqihao on 2016/11/15.
 */

public class ShafaFileDownload implements INetBase {
    private DownloadInfo mDownloadInfo;
    private IDownloadListener mListener;
    private ProgressAsker mAsker;

    public ShafaFileDownload(DownloadInfo downloadInfo) {
        mDownloadInfo = downloadInfo;
        mAsker = new ProgressAsker();
    }

    public boolean deleteDownload(){
        boolean deleted = false;
        try {
            mDownloadInfo.mStatus = DownloadInfo.STATUS_PAUSE;
            stopNetAccess();
            mAsker.stopQuery();

            deleteDownloadFile(ShafaDownloadManager.TEMP_FILE + mDownloadInfo.mPackageName);
            deleteDownloadFile(mDownloadInfo.mFilePath);

            deleted = true;

        }catch (Exception e){
            e.printStackTrace();
        }

        return deleted;
    }

    private void deleteDownloadFile(String filePath){
        try {
            if(!TextUtils.isEmpty(filePath)){
                File file = new File(filePath);
                if(file.isFile() && file.exists()){
                    file.delete();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public DownloadInfo getDownloadInfo(){
        return mDownloadInfo;
    }

    public ShafaFileDownload beginDownload(IDownloadListener listener){
        File file = new File(mDownloadInfo.mFilePath);
        if(file.exists()){
            file.delete();
        }

        mDownloadInfo.mStatus = DownloadInfo.STATUS_DOWNLOADING;
        DownLoadThreadPool.getSinglePools().execute(new DownloadRunnable(mDownloadInfo , listener));

        return this;
    }

    @Override
    public void stopNetAccess() {
        stop = true;
    }

    class DownloadRunnable implements Runnable {
        DownloadInfo mInfo;
        IDownloadListener mDownloadListener;

        public DownloadRunnable(DownloadInfo info , IDownloadListener downloadListener) {
            mInfo = info;
            mDownloadListener = downloadListener;
        }

        @Override
        public void run() {
            downloadFileFromNet(mInfo , mDownloadListener);
        }
    }

    private boolean stop = false;
    private void downloadFileFromNet(DownloadInfo info , IDownloadListener listener){
        stop = false;
        mDownloadInfo = info;
        mListener = listener;

        HttpURLConnection urlConnection = null;
        URL url;
        try {
            url = new URL(info.mUri);
            urlConnection = (HttpURLConnection) url.openConnection();
        } catch (MalformedURLException e) {
            if(null != listener){
                listener.onFailed(info.mUri , info.mFilePath);
            }
            e.printStackTrace();
        } catch (IOException e) {
            if (mListener != null) {
                mListener.onFailed(info.mUri, info.mFilePath);
            }
            e.printStackTrace();
        }

        if(null == urlConnection){
            return;
        }

        int responseCode = 0;
        try {
            responseCode = urlConnection.getResponseCode();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(responseCode != 200){
            urlConnection.disconnect();
            if(null != mListener){
                mListener.onFailed(info.mUri , info.mFilePath);
            }
            return;
        }

        int totalLength = urlConnection.getContentLength();
        mDownloadInfo.setTotalBytes(totalLength);
        mDownloadInfo.setCurrentBytes(0);

        File tempFile = new File(ShafaDownloadManager.TEMP_FILE + info.mPackageName);

        File finalFile = new File(info.mFilePath);

        try {
            if(null != finalFile && finalFile.exists()){
                finalFile.delete();
            }
            if(null != tempFile && tempFile.exists()){
                tempFile.delete();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        mAsker.startQuery();

        createFile(tempFile);
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(tempFile, "rw");
            raf.setLength(totalLength);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        InputStream is = null;
        try {
            is = urlConnection.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 若输入流为空 则不继续下面的操作
        if (null == is) {
            return;
        }

        int currentPosition = 0;

        byte temp[] = new byte[100 << 10];
        int count = 0;

        try {

            while ((count = is.read(temp)) != -1) {
                if (stop) {

                    if (null != is) {
                        is.close();
                    }

                    if (null != raf) {
                        raf.close();
                    }

                    mAsker.stopQuery();
                    // mInfo.mStatus = DatabaseOperation.STATUS_PAUSE;
                    // mdbOperation.updateDownloadProgressInfo(mInfo);
                    urlConnection.disconnect();
                    return;
                }
                if (raf != null) {
                    raf.seek(currentPosition);
                    raf.write(temp, 0, count);
                }

                currentPosition += count;
                info.setCurrentBytes(currentPosition);
                // mdbOperation.updateDownloadInfo(info);

            }
            info.mStatus = DownloadInfo.STATUS_FINISH;
            copyNewFile(info.mUri, tempFile, finalFile);

        } catch (Exception e) {
            e.printStackTrace();

            if (mListener != null) {
                mListener.onFailed(info.mUri, info.mFilePath);
            }
        }

        if (null != raf) {
            try {
                raf.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        if (null != is) {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    private boolean createFile(File file) {
        File parentFile = file.getParentFile();

        parentFile.mkdirs();
        boolean ret = false;
        try {
            ret = file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ret;
    }

    private boolean copyNewFile(String uri, File src, File dst) {
        try {
            if (dst != null) {
                if (!dst.getParentFile().exists()) {
                    dst.getParentFile().mkdirs();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//		if (dst != null) {
//			int count = 0;
//		CreatFile:
//			try {
//				dst.createNewFile();
//			} catch (Exception e) {
//				e.printStackTrace();
//				try {
//					if (count > 10) {
//						++count;
//						Thread.sleep(2000);
//						break CreatFile;
//					}
//				} catch (Exception e2) {
//					e.printStackTrace();
//				}
//			}
//		}
//		boolean copySuccess = src.renameTo(dst);
        boolean copySuccess=doCopyFile(src,dst);
        if (copySuccess) {
            src.delete();
            mAsker.downLoadFinish(uri);
            try {
                if (!Util.checkSDcardCanReadAndWrite()) {
                    //File tmpFile = dst.getAbsoluteFile();

                    String command = "chmod 777 " + dst.getAbsolutePath();
                    Runtime runtime = Runtime.getRuntime();
                    runtime.exec(command);
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return copySuccess;
    }

    private  boolean doCopyFile(final File srcFile, final File destFile ) {
        if (srcFile!=null&&destFile!=null && destFile.exists() && destFile.isDirectory()) {
            return false;
        }
        FileInputStream fis = null;
        FileOutputStream fos = null;
        FileChannel input = null;
        FileChannel output = null;
        try {
            int buff=1024*100;
            fis = new FileInputStream(srcFile);
            fos = new FileOutputStream(destFile);
            input  = fis.getChannel();
            output = fos.getChannel();
            final long size = input.size();
            long pos = 0;
            long count = 0;
            while (pos < size) {
                final long remain = size - pos;
                count = remain > buff ? buff : remain;
                final long bytesCopied = output.transferFrom(input, pos, count);
                if (bytesCopied == 0) {
                    break;
                }
                pos += bytesCopied;
            }
        }catch (Exception e){
            e.printStackTrace();
            for(StackTraceElement element: e.getStackTrace()) {
            }
            return false;
        } finally{
            closeQuietly(output, fos, input, fis);
        }
        return srcFile.length()==destFile.length();
    }

    private void closeQuietly(final Closeable... closeable){
        if(closeable != null ){
            for(Closeable cls:closeable){
                try{
                    if(cls != null)
                        cls.close();
                }catch (Exception e){

                }
            }
        }

    }

    public interface IDownloadListener{
        void onProgressChange(DownloadInfo info , int currentBytes , int totalBytes);
        void onEnd(DownloadInfo mDownloadInfo);
        void onFailed(String uri , String appName);
        void onError(String error);
    }

    class ProgressAsker {

        public final int MSG_QUERY = 1;

        public final int MSG_DOWNLOAD_OVER = 2;

        public void startQuery() {
            mHandler.removeMessages(MSG_QUERY);
            mHandler.sendEmptyMessage(MSG_QUERY);
        }

        public void stopQuery() {
            mHandler.removeMessages(MSG_QUERY);
        }

        public void downLoadFinish(String uri) {
            mHandler.sendEmptyMessage(MSG_DOWNLOAD_OVER);
        }

        @SuppressLint("HandlerLeak")
        private Handler mHandler = new Handler(Looper.getMainLooper()) {

            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case MSG_QUERY:

                        if (null != mListener) {
                            mListener.onProgressChange(mDownloadInfo,
                                    (int) mDownloadInfo.getCurrentBytes(),
                                    (int) mDownloadInfo.getTotalBytes());
                        }

                        if (null != mDownloadInfo && (mDownloadInfo.getCurrentBytes() == mDownloadInfo.getTotalBytes() && mDownloadInfo.getCurrentBytes() != 0)) {
                            stopQuery();
                        } else {
                            sendEmptyMessageDelayed(MSG_QUERY, 800);
                        }

                        break;

                    case MSG_DOWNLOAD_OVER:
                        if (null != mListener) {
                            mListener.onEnd(mDownloadInfo);
                        }
                        break;

                    default:
                        break;
                }
            };
        };

    }



}
