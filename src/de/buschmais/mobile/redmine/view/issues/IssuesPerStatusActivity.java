package de.buschmais.mobile.redmine.view.issues;

import static de.buschmais.mobile.redmine.Constants.INTENT_KEY_ISSUES;
import static de.buschmais.mobile.redmine.Constants.INTENT_KEY_PROJECT;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.buschmais.mobile.redmine.Constants;
import de.buschmais.mobile.redmine.R;
import de.buschmais.mobile.redmine.RedmineMobileApplication;
import de.buschmais.mobile.redmine.dao.Redmine;
import de.buschmais.mobile.redmine.data.Issue;
import de.buschmais.mobile.redmine.data.Issue.Status;
import de.buschmais.mobile.redmine.data.Project;
import de.buschmais.mobile.redmine.exception.InvalidCredentialsException;
import de.buschmais.mobile.redmine.exception.InvalidURLException;
import de.buschmais.mobile.redmine.util.AsyncTaskResult;
import de.buschmais.mobile.redmine.view.PasswordDialog;
import de.buschmais.mobile.redmine.view.PasswordDialog.OnPasswordDialogClosedListener;
import de.buschmais.mobile.redmine.view.issues.charts.IssueStatusPieChartView.OnStatusSelectionChangedListener;

/**
 * The activity to show the {@link IssuesPerStatusFragment} fragment. (This activity is currently not used.)
 */
public class IssuesPerStatusActivity extends Activity implements OnStatusSelectionChangedListener, OnPasswordDialogClosedListener
{
    /** The log tag. */
    private static final String TAG = IssuesPerStatusActivity.class.getSimpleName();
    
    /** The fragment showing the issues in a pie chart */
    private IssuesPerStatusFragment fragment;
    
    /** Progress dialog shown while the issues are loaded */
    private ProgressDialog getIssuesProgress;
    
    /** The by status sorted map of issues of the given project */
    private HashMap<Status, List<Issue>> issues = new HashMap<Issue.Status, List<Issue>>();
    
    /** The project for which this activity should show the issues */
    private Project project;
    
    /** The text view to show the project name */
    private TextView textViewProjectName;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issues_per_status);
        
        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            project = (Project) extras.getSerializable(INTENT_KEY_PROJECT);

            if (project == null)
            {
                throw new IllegalStateException("Unable to find project in bundle extras for key '"
                        + INTENT_KEY_PROJECT + "'");
            }
        }
        else
        {
            throw new IllegalStateException(
                    "No extras found, make sure to bundle a project in the starting intent with key '"
                            + INTENT_KEY_PROJECT + "'");
        }

        setupActionBar();
        
        fragment = (IssuesPerStatusFragment) getFragmentManager().findFragmentById(R.id.fragmentIssuesPerStatus);
        
        textViewProjectName = (TextView) findViewById(R.id.textViewProjectName);
    }
    
    @Override
    protected void onResume()
    {
        super.onResume();
        
        initializeIssueMap();

        textViewProjectName.setText(project.getName());

        String password = ((RedmineMobileApplication) getApplication()).getPassword();
        if (password == null)
        {
            PasswordDialog dialog = new PasswordDialog();
            dialog.show(getFragmentManager(), "passwordDialog");
        }
        else
        {
            executeTask(password);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.issues_per_status, menu);
        return true;
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
    
    @Override
    public void onStatusSelected(Status status)
    {
        Intent i = new Intent(getApplicationContext(), IssuesPerAssigneeActivity.class);
        i.putExtra(INTENT_KEY_PROJECT, project);
        i.putExtra(INTENT_KEY_ISSUES, (Serializable) issues.get(status));
        
        startActivity(i);
    }
    
    @Override
    public void onStatusUnselected(Status status)
    {
        // do nothing, we do not support unselection
    }
    
    @Override
    public void onPositiveButtonClicked(String password, boolean tempStorePassword)
    {
        if(tempStorePassword)
        {
            ((RedmineMobileApplication) getApplication()).setPassword(password);
        }
        executeTask(password);
    }
    
    @Override
    public void onNegativeButtonClicked()
    {
        // the user cancels the update dialog of the password, so we will leave this activity
        finish();
    }

    /**
     * Set up the {@link android.app.ActionBar}.
     */
    private void setupActionBar()
    {
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }
    
    /**
     * Execute the getIssues task using the given password.
     * @param password
     */
    private void executeTask(String password)
    {
        new GetIssuesTask().execute(project, password);
    }
    
    /**
     * (Re) initialize the issue map. That means clear the content of the map and add 
     * all status with empty issue lists.
     */
    private void initializeIssueMap()
    {
        issues.clear();
        int statusLength = Status.values().length;
        for (int i = 0; i < statusLength; i++)
        {
            issues.put(Status.values()[i], new ArrayList<Issue>());
        }
    }
    
    /**
     * A task that will be called to get the issues for a particular project. The task then updated the overview
     * fragment.
     */
    private class GetIssuesTask extends AsyncTask<Object, Void, AsyncTaskResult<List<Issue>>>
    {
        @Override
        protected void onPreExecute()
        {
            getIssuesProgress = new ProgressDialog(IssuesPerStatusActivity.this);
            getIssuesProgress.setTitle(R.string.issues_loading);
            getIssuesProgress.setCancelable(false);
            getIssuesProgress.show();
        }

        @Override
        protected AsyncTaskResult<List<Issue>> doInBackground(Object ... params)
        {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String url = preferences.getString(Constants.SETTINGS_KEY_REDMINE_URL, "");
            String username = preferences.getString(Constants.SETTINGS_KEY_USERNAME, "");
            String password = (String) params[1];

            try
            {
                return new AsyncTaskResult<List<Issue>>(Redmine.getIssues((Project) params[0], url, username, password));
            }
            catch (Exception e)
            {
                return new AsyncTaskResult<List<Issue>>(e);
            }
        }

        @Override
        protected void onCancelled(AsyncTaskResult<List<Issue>> result)
        {
            if (getIssuesProgress != null)
            {
                getIssuesProgress.dismiss();
            }
        }

        @Override
        protected void onPostExecute(AsyncTaskResult<List<Issue>> result)
        {
            if (result.isError())
            {
                Exception e = result.getError();
                if (e instanceof InvalidURLException)
                {
                    Toast.makeText(getApplicationContext(), R.string.general_error_invalid_url, Toast.LENGTH_LONG)
                            .show();
                }
                else if (e instanceof InvalidCredentialsException)
                {
                    Toast.makeText(getApplicationContext(), R.string.general_error_invalid_credentials,
                            Toast.LENGTH_LONG).show();
                    
                    PasswordDialog dialog = new PasswordDialog();
                    Bundle arguments = new Bundle(1);
                    arguments.putString(Constants.BUNDLE_KEY_PASSWORD, ((RedmineMobileApplication) getApplication()).getPassword());
                    dialog.setArguments(arguments);
                    dialog.show(getFragmentManager(), "passwordDialog");
                }
                else
                {
                    Toast.makeText(getApplicationContext(), R.string.general_error_general, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "There was an error retrieving the list of projects.", e);
                }
            }
            else
            {
                // clear the map so we can refill it with the result
                initializeIssueMap();
                
                for (Issue issue : result.getResult())
                {
                    issues.get(issue.getStatus()).add(issue);
                }
                fragment.reDraw(issues);

                Log.d(TAG, "Got " + result.getResult().size() + " issues.");
            }

            if (getIssuesProgress != null)
            {
                getIssuesProgress.dismiss();
            }
        }
    }
}
