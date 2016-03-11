package ru.sukharev.networktest.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import ru.sukharev.networktest.R;
import ru.sukharev.networktest.presenter.NetworkHelper;
import ru.sukharev.networktest.utils.ByteUnit;
import ru.sukharev.networktest.utils.DataVolume;
import ru.sukharev.networktest.utils.NetworkTextUtils;

public class TestActivity extends AppCompatActivity implements NetworkHelper.NetworkHelperCallback{

    TextView mPingText;
    TextView mSpeedText;
    TextView mInfoText;
    TextView mConnectionTypeText;
    TextView mProviderText;
    ProgressBar mProgressBar;
    NetworkTextUtils mNetworkTextUtils;
    FloatingActionButton mActionButton;
    private int mMaxProgress;
    private int mCurrentProgress;

    private final static String EXTRA_MAX_PROGRESS_BAR = "extra_max_progress";
    private final static String EXTRA_CURRENT_PROGRESS_BAR = "extra_current_progress";

    private final static String EMPTY_FIELD = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (!(savedInstanceState == null)) {
            if (savedInstanceState.containsKey(EXTRA_MAX_PROGRESS_BAR))
                mMaxProgress = savedInstanceState.getInt(EXTRA_MAX_PROGRESS_BAR);
            if (savedInstanceState.containsKey(EXTRA_CURRENT_PROGRESS_BAR))
                mCurrentProgress = savedInstanceState.getInt(EXTRA_CURRENT_PROGRESS_BAR);
        }

        setUpViewFields();

        setUpNetworkHelper();

        if (NetworkHelper.getInstance().isTestStarted()) {
            setTestingEnvironment();
        }

        mNetworkTextUtils = new NetworkTextUtils(this);
        mActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {

                    SharedPreferences prefs = PreferenceManager.
                            getDefaultSharedPreferences(TestActivity.this);
                    String server = prefs.getString(getString(R.string.pref_server_key),
                            getString(R.string.pref_server_def_value));
                    int volumeInMB = Integer.parseInt(prefs.getString(getString(R.string.pref_volume_key),
                            getString(R.string.pref_volume_def_value)));
                    int timeoutInSeconds = Integer.parseInt(prefs.getString(getString(R.string.pref_timeout_key),
                            getString(R.string.pref_timeout_def_value)));
                    int timeoutInMs = (int) TimeUnit.SECONDS.toMillis(timeoutInSeconds);
                    URL url = new URL(server);

                    //max is 500 mb so never will be more than {@link #Integer.MAX_VALUE};
                    mMaxProgress = (int) ByteUnit.MEGABYTES.toBytes(volumeInMB);

                    NetworkHelper.getInstance().executeTest(url, volumeInMB, timeoutInMs);
                    setConnectionTypeField();
                    setProviderField();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    public void setConnectionTypeField(){
        ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (manager != null && manager.getActiveNetworkInfo() != null) {
            String fullInfo = manager.getActiveNetworkInfo().getTypeName() + NetworkTextUtils.SPACE +
                    manager.getActiveNetworkInfo().getSubtypeName();
            mConnectionTypeText.setText(fullInfo);
        }
        else mConnectionTypeText.setText(getString(R.string.no_info));
    }

    public void setProviderField(){
        TelephonyManager manager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if (manager!=null && !manager.getNetworkOperatorName().equals(""))
            mProviderText.setText(manager.getNetworkOperatorName());
        else mProviderText.setText(getString(R.string.no_info));
    }

    public void setUpViewFields(){
        mPingText = (TextView) findViewById(R.id.ping_text);
        mSpeedText = (TextView) findViewById(R.id.speed_text);
        mConnectionTypeText = (TextView) findViewById(R.id.connection_type_text);
        mProviderText = (TextView) findViewById(R.id.provider_text);
        mInfoText = (TextView) findViewById(R.id.info_text);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mActionButton = (FloatingActionButton) findViewById(R.id.fab);
    }

    public void setUpNetworkHelper() {
        NetworkHelper.getInstance().setUpTester(TestActivity.this);
    }

    public void clearActivityState() {
        mActionButton.setImageDrawable(ContextCompat.getDrawable(this, (R.drawable.ic_start)));
        setProgressBar(0);
        changeProgressBarVisibility(false);
    }

    private void setTestingEnvironment(){
        mProgressBar.setMax(mMaxProgress);
        setProgress(mCurrentProgress);
        mActionButton.setImageDrawable(ContextCompat.getDrawable(this, (R.drawable.ic_cancel)));
    }

    public void primarySettingUp(){
        mPingText.setText(EMPTY_FIELD);
        mSpeedText.setText(EMPTY_FIELD);
        mInfoText.setText(EMPTY_FIELD);
        mConnectionTypeText.setText(EMPTY_FIELD);
        mProviderText.setText(EMPTY_FIELD);
        mCurrentProgress = 0;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_test, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(TestActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPingReceived(int ping) {
        mPingText.setText(mNetworkTextUtils.formatTime(TimeUnit.MILLISECONDS, ping));
    }

    public void setProgressBar(int progress) {
        changeProgressBarVisibility(true);
        mProgressBar.setProgress(progress);
    }

    @Override
    public void onDownloadProgressChanged(int downloadProgress) {
        setProgressBar(downloadProgress);
    }

    private void changeProgressBarVisibility(boolean visible){
        if (visible) mProgressBar.setVisibility(View.VISIBLE);
        else mProgressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onTestFinished(DataVolume speed) {
        mSpeedText.setText(mNetworkTextUtils.formatSpeed(speed.unit,
                TimeUnit.SECONDS,
                speed.value));
        changeProgressBarVisibility(false);
    }

    @Override
    public void onDownloadError() {
        clearActivityState();
        mInfoText.setText(getString(R.string.result_error_download));
    }

    @Override
    public void onConnectionError() {
        clearActivityState();
        mInfoText.setText(getString(R.string.result_error_connect));
    }

    @Override
    public void onCancelled() {
        clearActivityState();
        mInfoText.setText(getString(R.string.result_cancelled));
    }

    @Override
    public void onSuccess() {
        clearActivityState();
        mInfoText.setText(getString(R.string.result_success));
    }

    @Override
    public void onTestStarted() {
        primarySettingUp();
        setTestingEnvironment();
    }

    @Override
    public void onTimeout() {
        clearActivityState();
        mInfoText.setText(getString(R.string.result_timeout));
    }


    @Override
    public void onSaveInstanceState(Bundle bundle){
        super.onSaveInstanceState(bundle);
        bundle.putInt(EXTRA_MAX_PROGRESS_BAR, mMaxProgress);
        bundle.putInt(EXTRA_CURRENT_PROGRESS_BAR, mMaxProgress);
    }
}
