package ru.sukharev.networktest.view;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;

import ru.sukharev.networktest.R;

/**
 * A placeholder fragment containing a preference screen
 */
public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {


    private String[] server_names;
    private String[] server_locs;
    private String[] volume_names;
    private String[] volume_values;
    private String[] timeout_names;
    private String[] timeout_values;

    private ListPreference mServers;
    private ListPreference mVolumes;
    private ListPreference mTimeouts;


    public SettingsFragment() {
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        addPreferencesFromResource(R.xml.settings);
    }


    private void setSummary() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        setSummaryServer(prefs);
        setSummaryVolume(prefs);
        setSummaryTimeout(prefs);
    }

    private void setSummaryServer(SharedPreferences prefs) {
        String key = getString(R.string.pref_server_key);

        if (mServers == null)
            mServers = (ListPreference) findPreference(key);

        if (server_names == null)
            server_names = getResources().getStringArray(R.array.pref_servers);

        if (server_locs == null)
            server_locs = getResources().getStringArray(R.array.pref_servers_values);

        String server = prefs.getString(key,
                getString(R.string.pref_server_def));
        for (int i=0; i<server_locs.length; i++)
            if (server.equals(server_locs[i]))
                mServers.setSummary(server_names[i]);
    }

    private void setSummaryVolume(SharedPreferences prefs) {
        String key = getString(R.string.pref_volume_key);

        if (mVolumes == null)
            mVolumes = (ListPreference) findPreference(key);

        if (volume_names == null)
            volume_names = getResources().getStringArray(R.array.pref_volumes);

        if (volume_values == null)
            volume_values = getResources().getStringArray(R.array.pref_volumes_values);

        String volume = prefs.getString(key,
                getString(R.string.pref_volume_def));
        for (int i=0; i< volume_values.length; i++)
            if (volume.equals(volume_values[i]))
                mVolumes.setSummary(volume_names[i]);
    }


    private void setSummaryTimeout(SharedPreferences prefs) {
        String key = getString(R.string.pref_timeout_key);

        if (mTimeouts == null)
            mTimeouts = (ListPreference) findPreference(key);

        if (timeout_names == null)
            timeout_names = getResources().getStringArray(R.array.pref_timeouts);

        if (timeout_values == null)
            timeout_values = getResources().getStringArray(R.array.pref_timeouts_values);

        String timeout = prefs.getString(key,
                getString(R.string.pref_timeout_def));
        for (int i=0; i<timeout_values.length; i++)
            if (timeout.equals(timeout_values[i]))
                mTimeouts.setSummary(timeout_names[i]);
    }



    @Override
    public void onResume() {
        super.onResume();
        setSummary();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_server_key)))
            setSummaryServer(sharedPreferences);
        if (key.equals(getString(R.string.pref_volume_key)))
            setSummaryVolume(sharedPreferences);
        if (key.equals(getString(R.string.pref_timeout_key)))
            setSummaryTimeout(sharedPreferences);
    }


}
