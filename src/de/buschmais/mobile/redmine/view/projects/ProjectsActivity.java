package de.buschmais.mobile.redmine.view.projects;

import static de.buschmais.mobile.redmine.Constants.INTENT_KEY_PROJECT;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import de.buschmais.mobile.redmine.Constants;
import de.buschmais.mobile.redmine.R;
import de.buschmais.mobile.redmine.RedmineMobileApplication;
import de.buschmais.mobile.redmine.dao.Redmine;
import de.buschmais.mobile.redmine.data.Project;
import de.buschmais.mobile.redmine.exception.InvalidCredentialsException;
import de.buschmais.mobile.redmine.exception.InvalidURLException;
import de.buschmais.mobile.redmine.util.AsyncTaskResult;
import de.buschmais.mobile.redmine.view.PasswordDialog;
import de.buschmais.mobile.redmine.view.PasswordDialog.OnPasswordDialogClosedListener;
import de.buschmais.mobile.redmine.view.issues.IssuesActivity;

/**
 * The activity to show projects in a list.
 */
public class ProjectsActivity extends ListActivity implements OnPasswordDialogClosedListener
{
    /** The log tag. */
    private static final String TAG = ProjectsActivity.class.getSimpleName();

    /** The adapter that provides the projects */
    private ProjectItemAdapter adapter;

    /** The load progress dialog */
    private ProgressDialog loadProgress;
    
    /** The editable that is used for filtering the list of projects */
    private EditText filterText;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.i(TAG, "onCreate()");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projects);
        // Show the Up button in the action bar.
        setupActionBar();

        adapter = new ProjectItemAdapter(getApplicationContext());
        setListAdapter(adapter);

        filterText = (EditText) findViewById(R.id.textViewProjectsFilter);
        filterText.addTextChangedListener(new FilteringTextWatcher(adapter));
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        RedmineMobileApplication application = (RedmineMobileApplication) getApplication();

        if (!application.isNetworkAvailable())
        {
            Toast.makeText(getApplicationContext(), R.string.general_error_no_network, Toast.LENGTH_LONG).show();
            return;
        }

        String password = application.getPassword();
        if (password != null)
        {
            executeTask(password);
        }
        else
        {
            PasswordDialog dialog = new PasswordDialog();
            dialog.show(getFragmentManager(), "passwordDialog");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.projects, menu);
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
    public void onPositiveButtonClicked(String password, boolean tempStorePassword)
    {
        if (tempStorePassword)
        {
            ((RedmineMobileApplication) getApplication()).setPassword(password);
        }
        executeTask(password);
    }

    @Override
    public void onNegativeButtonClicked()
    {
        finish();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id)
    {
        Object o = v.getTag(R.id.tagProjectItemProject);
        if (o != null)
        {
            Project p = (Project) o;
            Log.d(TAG, "List item for project with id=" + p.getId() + " clicked.");

            //Intent i = new Intent(getApplicationContext(), IssuesPerStatusActivity.class);
            Intent i = new Intent(getApplicationContext(), IssuesActivity.class);
            i.putExtra(INTENT_KEY_PROJECT, p);
            startActivity(i);
        }
        else
        {
            Log.e(TAG, "Unexpected list element clicked: " + v);
        }
    }
    
    /**
     * Set up the {@link android.app.ActionBar}.
     */
    private void setupActionBar()
    {
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Executes the task for retriving the projects with the given password.
     * 
     * @param password
     */
    private void executeTask(String password)
    {
        new GetProjectsTask().execute(password);
    }

    /**
     * A task that uses URL, user name and password from the shared preferences to retrieve a list of projects from the
     * given URL. The tasks afterwards updates the list adapter.
     */
    private class GetProjectsTask extends AsyncTask<String, Void, AsyncTaskResult<List<Project>>>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            loadProgress = new ProgressDialog(ProjectsActivity.this);
            loadProgress.setTitle(R.string.projects_loading);
            loadProgress.setCancelable(false);
            loadProgress.show();
        }

        @Override
        protected AsyncTaskResult<List<Project>> doInBackground(String... params)
        {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String url = preferences.getString(Constants.SETTINGS_KEY_REDMINE_URL, "");
            String username = preferences.getString(Constants.SETTINGS_KEY_USERNAME, "");
            String password = params[0];

            try
            {
                return new AsyncTaskResult<List<Project>>(Redmine.getAllProjects(url, username, password));
            }
            catch (Exception e)
            {
                return new AsyncTaskResult<List<Project>>(e);
            }
        }

        @Override
        protected void onPostExecute(AsyncTaskResult<List<Project>> result)
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
                    Toast.makeText(getApplicationContext(), R.string.general_error_invalid_credentials, Toast.LENGTH_LONG)
                    .show();
                    
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
                List<Project> projects = result.getResult();
                adapter.setProjects(projects);
                
                adapter.getFilter().filter(filterText.getText());
            }

            if (loadProgress != null)
            {
                loadProgress.dismiss();
            }
        }

        @Override
        protected void onCancelled(AsyncTaskResult<List<Project>> result)
        {
            if (loadProgress != null)
            {
                loadProgress.dismiss();
            }
        }
    }

    /**
     * Text watcher that uses the filter provided by the given adapter to filter text.
     */
    private class FilteringTextWatcher implements TextWatcher
    {
        private ProjectItemAdapter adapter;

        public FilteringTextWatcher(ProjectItemAdapter adapter)
        {
            this.adapter = adapter;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        {
            // do nothing
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count)
        {
            // do nothing
        }

        @Override
        public void afterTextChanged(Editable s)
        {
            adapter.getFilter().filter(s);
        }
    }
}
