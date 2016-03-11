package ru.sukharev.networktest.model;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * AsyncTask that loads data via HTTP from {@link #mUrl} we gave it.
 * It loads only {@link #mMaxByteCount} bytes and it should load them in {@link #mMaxTime} time, or
 * it cancel itself. And it return callback about download rocess and its results to
 * {@link ru.sukharev.networktest.presenter.NetworkHelper} with predefined codes.
 * @see ru.sukharev.networktest.model.NetworkTestTask.NetworkTestTaskCallback
 * @see ru.sukharev.networktest.model.NetworkTestTask.CODE
 * @see ru.sukharev.networktest.model.NetworkTestTask.RESULT_CODE
 */
public class NetworkTestTask extends AsyncTask<Void, Integer, Integer>{

    private final static int MESSAGE_TYPE_INDEX = 0;
    private final static int DATA_INDEX = 1;
    private final static int UPDATE_INTERVAL = 500;
    private final static int MTU = 1500;
    private final static int SIZE_OF_BUFFER = 2 * MTU;

    private byte[] buffer = new byte[SIZE_OF_BUFFER];
    private URL mUrl;
    private int mMaxByteCount;
    private NetworkTestTaskCallback mCallback;
    private boolean isInterrupted = false;
    private int mMaxTime;


    public NetworkTestTask(URL url, int byteCount, int timeout, NetworkTestTaskCallback callback){
        mUrl = url;
        mMaxByteCount = byteCount;
        mMaxTime = timeout;
        mCallback = callback;
    }

    public void interrupt(){
        isInterrupted = true;
    }

    @Override
    protected Integer doInBackground(Void... params) {

        HttpURLConnection connection;
        InputStream stream= null;
        try {
            connection = (HttpURLConnection) mUrl.openConnection();
            long startConnectedTime = System.currentTimeMillis();
            connection.setUseCaches(false);
            connection.setConnectTimeout(20000);
            connection.connect();
            stream = connection.getInputStream();
            long endConnectedTime = System.currentTimeMillis();
            int ping = (int) (endConnectedTime - startConnectedTime);
            publishProgress(CODE.PING, ping);
        } catch (IOException e) {
            return RESULT_CODE.CONNECTION_ERROR;
        }
        try {
            long startTime=System.currentTimeMillis();

            //count all download data
            int downloadBytes = 0;

            //we need to send info about progress periodically, that variables counts period
            long timeFromLastUpdate = 0;
            long lastUpdate = System.currentTimeMillis();

            int readBytes = 0;
            while((readBytes = stream.read(buffer))!=-1 && downloadBytes < mMaxByteCount){
                if (isInterrupted)
                    return RESULT_CODE.CANCELLED;

                downloadBytes += readBytes;
                if (timeFromLastUpdate > UPDATE_INTERVAL) {
                    //Send amount of data to refresh test progress in UI
                    publishProgress(CODE.SIZE_OF_DOWNLOADED, downloadBytes);
                    lastUpdate = System.currentTimeMillis();

                    //Timeout case
                    if (System.currentTimeMillis() - startTime > mMaxTime) {
                        int downloadTime = (int) (System.currentTimeMillis() - startTime);
                        publishProgress(CODE.DOWNLOAD_TIME_MEASURED, downloadTime);
                        return RESULT_CODE.TIMEOUT;

                    };
                }
                timeFromLastUpdate = System.currentTimeMillis() - lastUpdate;

            }

            //Sending resulting amount of data
            publishProgress(CODE.SIZE_OF_DOWNLOADED, downloadBytes);

            if (downloadBytes < mMaxByteCount) return RESULT_CODE.DOWNLOAD_ERROR;
            int downloadTime = (int) (System.currentTimeMillis() - startTime);
            publishProgress(CODE.DOWNLOAD_TIME_MEASURED, downloadTime);
            return RESULT_CODE.SUCCESS;
        } catch (IOException e) {
            return RESULT_CODE.DOWNLOAD_ERROR;
        } finally {
            Log.i(getClass().getSimpleName(), "cleanup");
            connection.disconnect();
            try {
                if (stream!= null) stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    @Override
    protected void onPreExecute(){
        mCallback.onTestStarted();
    }

    @Override
    protected void onProgressUpdate(Integer ... values){
        switch (values[MESSAGE_TYPE_INDEX]) {
            case CODE.PING:
                mCallback.onPingCalculated(values[DATA_INDEX]);
                break;
            case CODE.SIZE_OF_DOWNLOADED:
                mCallback.onDownloadProgressUpdated(values[DATA_INDEX]);
                break;
            case CODE.DOWNLOAD_TIME_MEASURED:
                mCallback.onDownloadTimeMeasured(values[DATA_INDEX]);
                break;
            default: break;
        }

    }

    @Override
    protected void onPostExecute(Integer resultCode){
        switch (resultCode) {
            case RESULT_CODE.SUCCESS:
                Log.i(getClass().getSimpleName(),"result callback");
                mCallback.onSuccess();
                break;
            case RESULT_CODE.CANCELLED:
                mCallback.onCancelled();
                break;
            case RESULT_CODE.CONNECTION_ERROR:
                mCallback.onConnectionError();
                break;
            case RESULT_CODE.DOWNLOAD_ERROR:
                mCallback.onDownloadError();
                break;
            case RESULT_CODE.TIMEOUT:
                mCallback.onTimeout();
                break;
            default: break;
        }
    }

    public interface NetworkTestTaskCallback {

        void onPingCalculated(int ping);
        void onDownloadProgressUpdated(int downloadBytes);
        void onDownloadTimeMeasured(int downloadTime);
        void onConnectionError();
        void onDownloadError();
        void onCancelled();
        void onSuccess();
        void onTimeout();
        void onTestStarted();

    }

    public interface CODE {

        int PING = 1;
        int SIZE_OF_DOWNLOADED = 2;
        int DOWNLOAD_TIME_MEASURED = 3;
    }

    public interface RESULT_CODE {

        int CONNECTION_ERROR = 10;
        int DOWNLOAD_ERROR = 11;
        int SUCCESS = 12;
        int CANCELLED = 13;
        int TIMEOUT = 14;

    }
}
