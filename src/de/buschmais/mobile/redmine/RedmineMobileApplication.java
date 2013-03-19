package de.buschmais.mobile.redmine;

import static de.buschmais.mobile.redmine.Constants.SETTINGS_KEY_REDMINE_URL;
import static de.buschmais.mobile.redmine.Constants.SETTINGS_KEY_USERNAME;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

import de.buschmais.mobile.redmine.dao.Redmine;

/**
 * The Redmine application.
 */
public class RedmineMobileApplication extends Application implements OnSharedPreferenceChangeListener
{
    /** The log tag. */
    private static final String TAG = RedmineMobileApplication.class.getSimpleName();
    
    /** The password used to connect to the Redmine system */
    private String password;
    
    @Override
    public void onCreate()
    {
        Log.i(TAG, "onCreate()");
        
        super.onCreate();
        
        password = null;
        
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).registerOnSharedPreferenceChangeListener(this);
    }
    
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        if (SETTINGS_KEY_REDMINE_URL.equals(key) || SETTINGS_KEY_USERNAME.equals(key))
        {
            Redmine.invalidateCache();
            password = null;
        }
    }

    /**
     * Set the password that can be used to connect to the Redmine system.
     * 
     * @param aPassword
     */
    public void setPassword(String aPassword)
    {
        password = aPassword;
    }

    /**
     * Get the password that can be used to connect to the Redmine system.
     * 
     * @return
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * Check if network is available.
     * 
     * @return
     */
    public boolean isNetworkAvailable()
    {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        // if no network is available networkInfo will be null
        // otherwise check if we are connected
        if (networkInfo != null && networkInfo.isConnected())
        {
            return true;
        }
        return false;
    }
}
