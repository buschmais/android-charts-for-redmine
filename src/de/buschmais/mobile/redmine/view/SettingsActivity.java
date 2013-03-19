package de.buschmais.mobile.redmine.view;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;

import de.buschmais.mobile.redmine.R;

/**
 * The settings activity.
 */
public class SettingsActivity extends Activity
{
    /** The log tag. */
    private static final String TAG = SettingsActivity.class.getSimpleName();
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.i(TAG, "onCreate()");
        
        super.onCreate(savedInstanceState);

        setupActionBar();

        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();

    }

    /**
     * Set up the {@link android.app.ActionBar}.
     */
    private void setupActionBar()
    {

        getActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
        case android.R.id.home:
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * The settings fragment shown in the settings activity.
     */
    public static class SettingsFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.settings);
        }
    }

}
