package de.buschmais.mobile.redmine.view.issues;

import static de.buschmais.mobile.redmine.Constants.BUNDLE_KEY_ISSUE;
import static de.buschmais.mobile.redmine.Constants.BUNDLE_KEY_ISSUES;
import static de.buschmais.mobile.redmine.Constants.INTENT_KEY_PROJECT;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
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
import de.buschmais.mobile.redmine.data.User;
import de.buschmais.mobile.redmine.exception.InvalidCredentialsException;
import de.buschmais.mobile.redmine.exception.InvalidURLException;
import de.buschmais.mobile.redmine.util.AsyncTaskResult;
import de.buschmais.mobile.redmine.view.PasswordDialog;
import de.buschmais.mobile.redmine.view.PasswordDialog.OnPasswordDialogClosedListener;
import de.buschmais.mobile.redmine.view.issues.IssuesPerStatusAndAssigneeFragment.OnIssueSelectedListener;
import de.buschmais.mobile.redmine.view.issues.charts.IssueStatusPieChartView.OnStatusSelectionChangedListener;
import de.buschmais.mobile.redmine.view.issues.charts.IssuesPerAssigneeBarChartView.OnStatusAndAssigneeSelectionListener;

/**
 * The activity to handle different issue fragments.
 */
public class IssuesActivity 
    extends Activity 
    implements OnStatusSelectionChangedListener, OnPasswordDialogClosedListener, OnStatusAndAssigneeSelectionListener, OnIssueSelectedListener
{
    /** The log tag. */
    private static final String TAG = IssuesActivity.class.getSimpleName();

    /** The project for which this activity should show the issues */
    private Project project;

    /** The status sorted map of issues of the given project */
    private HashMap<Status, ArrayList<Issue>> issues;

    /** The text view to show the project name */
    private TextView projectName;

    /** Progress dialog shown while the isses are loaded */
    private ProgressDialog getIssuesProgress;
    
    /** The fragment manager */
    private FragmentManager fragMan;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.i(TAG, "onCreate() " + savedInstanceState);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issues);
        // Show the Up button in the action bar.
        setupActionBar();

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

        projectName = (TextView) findViewById(R.id.textViewProjectItemName);

        issues = new HashMap<Issue.Status, ArrayList<Issue>>();
        int statusLength = Status.values().length;
        for (int i = 0; i < statusLength; i++)
        {
            issues.put(Status.values()[i], new ArrayList<Issue>());
        }
        
        fragMan = getFragmentManager();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        projectName.setText(project.getName());

        String password = ((RedmineMobileApplication) getApplication()).getPassword();
        if (password == null)
        {
            PasswordDialog dialog = new PasswordDialog();
            dialog.show(getFragmentManager(), PasswordDialog.FRAGMENT_TRANSACTION_TAG);
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
        getMenuInflater().inflate(R.menu.issues, menu);
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
        Log.d(TAG, "onStatusSelected() " + status);
        
        showIssuesPerAssigneeFragment(status);
    }

    @Override
    public void onStatusUnselected(Status status)
    {
        Log.d(TAG, "onStatusUnselected() " + status);
    }

    @Override
    public void onNegativeButtonClicked()
    {
        finish();
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
    public void onStatusAndAssigneeSelected(Status status, User assignee)
    {
        showIssuesPerStatusAndAssigneeFragment(status, assignee);
    }
    
    @Override
    public void onIssueSelected(Issue issue)
    {
        showIssueDetailsFragment(issue);
    }
    
    /**
     * Execute the getIssues task using the given password.
     * @param password
     */
    private void executeTask(String password)
    {
        new GetIssesTask().execute(project, password);
    }

    /**
     * Set up the {@link android.app.ActionBar}.
     */
    private void setupActionBar()
    {
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }
    
    /**
     * Shows the {@link IssuesPerStatusFragment} using the current map of issues.
     */
    private void showIssuesPerStatusFragment()
    {
        IssuesPerStatusFragment ipsf = new IssuesPerStatusFragment();
        Bundle arguments = new Bundle();
        arguments.putSerializable(BUNDLE_KEY_ISSUES, issues);
        ipsf.setArguments(arguments);
        
        showFragment(ipsf, IssuesPerStatusFragment.FRAGMENT_TRANSACTION_TAG);
    }
    
    /**
     * Shows the {@link IssuesPerAssigneeFragment} using the current map of issues and filtering 
     * the issues by the given status.
     * 
     * @param status
     */
    private void showIssuesPerAssigneeFragment(Status status)
    {
        IssuesPerAssigneeFragment ipaf = new IssuesPerAssigneeFragment();
        Bundle arguments = new Bundle();
        arguments.putSerializable(BUNDLE_KEY_ISSUES, issues.get(status));
        ipaf.setArguments(arguments);
        
        replaceFragment(ipaf, IssuesPerAssigneeFragment.FRAGMENT_TRANSACTION_TAG);
    }
    
    /**
     * Shows the {@link IssuesPerStatusAndAssigneeFragment} using the given status and assignee to
     * filter a list of issues from the current map of issues. This filtered list will be added 
     * as argument to the created fragment.
     * 
     * @param status
     * @param assignee
     */
    private void showIssuesPerStatusAndAssigneeFragment(Status status, User assignee)
    {
        ArrayList<Issue> issuesPerAssignee = new ArrayList<Issue>();
        for (Issue i : issues.get(status))
        {
            User issueAssignee = i.getAssignee();
            if (issueAssignee == null)
            {
                issueAssignee = i.getAuthor();
            }
            
            if (issueAssignee.equals(assignee))
            {
                issuesPerAssignee.add(i);
            }
        }
        
        IssuesPerStatusAndAssigneeFragment ipsaaf = new IssuesPerStatusAndAssigneeFragment();
        Bundle arguments = new Bundle();
        arguments.putSerializable(BUNDLE_KEY_ISSUES, issuesPerAssignee);
        ipsaaf.setArguments(arguments);
        
        replaceFragment(ipsaaf, IssuesPerStatusAndAssigneeFragment.FRAGMENT_TRANSACTION_TAG);
    }
    
    /**
     * Shows the fragment that displays issue details. 
     * 
     * @param issue
     */
    private void showIssueDetailsFragment(Issue issue)
    {
        IssueDetailsFragment idf = new IssueDetailsFragment();
        Bundle arguments = new Bundle();
        arguments.putSerializable(BUNDLE_KEY_ISSUE, issue);
        idf.setArguments(arguments);
        
        replaceFragment(idf, IssueDetailsFragment.FRAGMENT_TRANSACTION_TAG);
    }
    
    /**
     * Add the fragment to the view. This will not add the fragment to the backstack.
     * @param f
     * @param fragmentTag
     */
    private void showFragment(Fragment f, String fragmentTag)
    {
        FragmentTransaction ft = fragMan.beginTransaction();
        ft.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left);
        ft.replace(R.id.fragmentContainer, f, fragmentTag);
        ft.commit();
    }
    
    /**
     * Replace the current active fragment with the given one.
     * 
     * @param f the fragment to replace
     * @param fragmentTag the tag to set for this fragment
     */
    private void replaceFragment(Fragment f, String fragmentTag)
    {
        FragmentTransaction ft = fragMan.beginTransaction();
        ft.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left, R.animator.slide_in_left, R.animator.slide_out_right);
        ft.replace(R.id.fragmentContainer, f, fragmentTag);
        ft.addToBackStack(fragmentTag);
        ft.commit();
    }
    
    /**
     * A task that will be called to get the issues for a particular project. The task then updated the overview
     * fragment.
     */
    private class GetIssesTask extends AsyncTask<Object, Void, AsyncTaskResult<List<Issue>>>
    {
        @Override
        protected void onPreExecute()
        {
            getIssuesProgress = new ProgressDialog(IssuesActivity.this);
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
                for (Issue issue : result.getResult())
                {
                    issues.get(issue.getStatus()).add(issue);
                }
                
                showIssuesPerStatusFragment();
            }

            if (getIssuesProgress != null)
            {
                getIssuesProgress.dismiss();
            }
        }
    }
}
