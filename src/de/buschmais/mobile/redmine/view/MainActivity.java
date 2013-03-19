package de.buschmais.mobile.redmine.view;

import static de.buschmais.mobile.redmine.Constants.SETTINGS_KEY_REDMINE_URL;
import static de.buschmais.mobile.redmine.Constants.SETTINGS_KEY_USERNAME;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import de.buschmais.mobile.redmine.R;
import de.buschmais.mobile.redmine.view.projects.ProjectsActivity;

/**
 * The main activity.
 */
public class MainActivity extends Activity
{
    /** The log tag. */
    private static final String TAG = MainActivity.class.getSimpleName();
    
    private TextView redmineUrl;
    private TextView username;
    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.i(TAG, "onCreate()");
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        
        redmineUrl = (TextView) findViewById(R.id.textViewURL);
        username = (TextView) findViewById(R.id.textViewUserName);
        
        Button connectButton = (Button) findViewById(R.id.buttonConnect);
        connectButton.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v)
            {
                handleConnectButtonClick();
            }
        });
    }
    
    @Override
    protected void onResume()
    {
        Log.i(TAG, "onResume()");

        super.onResume();
        
        String url = settings.getString(SETTINGS_KEY_REDMINE_URL, "");
        if (url.isEmpty())
        {
            url = getString(R.string.settings_not_set);
        }
        redmineUrl.setText(url);
        
        String userName = settings.getString(SETTINGS_KEY_USERNAME, "");
        if (userName.isEmpty())
        {
            userName = getString(R.string.settings_not_set);
        }
        username.setText(userName);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item)
    {
        if (item.getItemId() == R.id.action_settings)
        {
            startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
            return true;
        }

        return super.onMenuItemSelected(featureId, item);
    }

    /**
     * Handles the connect button click.
     */
    private void handleConnectButtonClick()
    {
        String url = settings.getString(SETTINGS_KEY_REDMINE_URL, "");
        String userName = settings.getString(SETTINGS_KEY_USERNAME, "");
        if (url.isEmpty() || userName.isEmpty())
        {
            Toast.makeText(getApplicationContext(), R.string.main_error_empty_parameter, Toast.LENGTH_LONG).show();
            return;
        }

        startActivity(new Intent(getApplicationContext(), ProjectsActivity.class));
    }
}
