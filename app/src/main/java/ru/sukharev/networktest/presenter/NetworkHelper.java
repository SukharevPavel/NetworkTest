package ru.sukharev.networktest.presenter;

import android.os.AsyncTask;
import android.util.Log;

import java.net.URL;
import java.util.concurrent.TimeUnit;

import ru.sukharev.networktest.model.NetworkTestTask;
import ru.sukharev.networktest.utils.DataVolume;

/**
 * Thin layer between View {@link ru.sukharev.networktest.view.TestActivity} and Model
 * {@link NetworkTestTask}. Used for running Test Task with parameters set by user. Uses callbacks
 * to interact with activity
 * @see NetworkHelperCallback
 */
public class NetworkHelper implements NetworkTestTask.NetworkTestTaskCallback {

    private static NetworkHelper ourInstance = new NetworkHelper();

    private URL mUrl;
    private int mMaxBytes;
    private NetworkHelperCallback mCallback;
    private int mPing;
    private int mDownloadTime;
    private int mDownloadBytes;
    private NetworkTestTask mTask;

    private int BYTES_IN_MB = 1024 * 1024;
    private int mMaxTime;

    public static NetworkHelper getInstance() {
        return ourInstance;
    }

    private NetworkHelper() {
    }

    public void setUpTester(NetworkHelperCallback callback){
        mCallback = callback;
    }

    public void executeTest(URL url, int megabytes, int time){
        mUrl= url;
        mMaxBytes = megabytes * BYTES_IN_MB;
        mMaxTime = time;
        if (!isTestStarted()) {
            mTask = new NetworkTestTask(mUrl, mMaxBytes, mMaxTime, this);
            mTask.execute();
        } else mTask.interrupt();
    }

    public boolean isTestStarted(){
        return (!(mTask == null || mTask.getStatus() == AsyncTask.Status.FINISHED));
    }

    @Override
    public void onPingCalculated(int ping) {
        mPing = ping;
        mCallback.onPingReceived(ping);
    }

    @Override
    public void onDownloadProgressUpdated(int downloadBytes) {
        mDownloadBytes = downloadBytes;
        mCallback.onDownloadProgressChanged(mDownloadBytes);
    }

    @Override
    public void onDownloadTimeMeasured(int downloadTimeInMs) {
        mDownloadTime = downloadTimeInMs;
        if (downloadTimeInMs != 0) {
            // Because we need to check situations when download time < 1 s. So, for translating
            // value in denominator from ms to s, we must multiple it by 1000 and if mDownloadBytes
            // less than downloadTimeInMs then we got a 0 as result, so we must multiply before division
            Log.i(getClass().getSimpleName(), "downloadBytes " + mDownloadBytes + " downloadTime " + downloadTimeInMs);
            long downloadSpeedInBytesPerSec = (TimeUnit.SECONDS.toMillis(mDownloadBytes) / downloadTimeInMs);
            Log.i(getClass().getSimpleName(), "speed " + downloadSpeedInBytesPerSec);
            mCallback.onTestFinished(DataVolume.appropriateFormat(downloadSpeedInBytesPerSec));
        }

    }

    @Override
    public void onConnectionError() {
        mCallback.onConnectionError();
    }

    @Override
    public void onDownloadError() {
        mCallback.onDownloadError();
    }

    @Override
    public void onCancelled() {
        mCallback.onCancelled();
    }

    @Override
    public void onSuccess() {
        mCallback.onSuccess();
    }

    @Override
    public void onTestStarted() {
        mCallback.onTestStarted();
    }

    @Override
    public void onTimeout() {
        mCallback.onTimeout();
    }

    public interface NetworkHelperCallback {

        void onPingReceived(int ping);

        void onDownloadProgressChanged(int downloadProgress);

        void onTestFinished(DataVolume speed);

        void onConnectionError();

        void onDownloadError();

        void onCancelled();

        void onSuccess();

        void onTestStarted();

        void onTimeout();


    }
}
