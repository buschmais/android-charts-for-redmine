package de.buschmais.mobile.redmine.view.issues;

import static de.buschmais.mobile.redmine.Constants.INTENT_KEY_ASSIGNEE;
import static de.buschmais.mobile.redmine.Constants.INTENT_KEY_ISSUES;
import static de.buschmais.mobile.redmine.Constants.INTENT_KEY_PROJECT;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import de.buschmais.mobile.redmine.R;
import de.buschmais.mobile.redmine.data.Issue;
import de.buschmais.mobile.redmine.data.Issue.Status;
import de.buschmais.mobile.redmine.data.Project;
import de.buschmais.mobile.redmine.data.User;
import de.buschmais.mobile.redmine.view.issues.charts.IssuesPerAssigneeBarChartView.OnStatusAndAssigneeSelectionListener;

/**
 * The activity to show the {@link IssuesPerAssigneeFragment}. (This activity is currently not used.) 
 */
public class IssuesPerAssigneeActivity extends Activity implements OnStatusAndAssigneeSelectionListener
{
    /** The log tag. */
    private static final String TAG = IssuesPerAssigneeActivity.class.getSimpleName();
    
    /** The project for which this activity should show the issues */
    private Project project;

    /** The list of issues to show */
    private ArrayList<Issue> issues;

    /** The fragment that will display the content */
    private IssuesPerAssigneeFragment fragment;
    
    /** Text view for the project name */
    private TextView projectNameTextView;
    
    /** Text view for the selected status */
    private TextView statusNameTextView;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.i(TAG, "onCreate()");
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issues_per_assignee);

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

            issues = (ArrayList<Issue>) extras.getSerializable(INTENT_KEY_ISSUES);
            if (issues == null)
            {
                throw new IllegalStateException("Unable to find issues in bundle extras for key '" + INTENT_KEY_ISSUES
                        + "'");
            }
        }
        else
        {
            throw new IllegalStateException("No extras found");
        }
        
        fragment = (IssuesPerAssigneeFragment) getFragmentManager().findFragmentById(R.id.fragmentIssuesPerAssignee);
        
        projectNameTextView = (TextView) findViewById(R.id.textViewProjectName);
        statusNameTextView = (TextView) findViewById(R.id.textViewStatusName);
    }
    
    @Override
    protected void onResume()
    {
        super.onResume();
        
        projectNameTextView.setText(project.getName());
        
        if (issues.size() > 0)
        {
            StringBuilder sb = new StringBuilder();
            Set<Status> usedStatus = new HashSet<Status>();
            Iterator<Issue> it = issues.iterator();
            while (it.hasNext())
            {
                Status s = it.next().getStatus();
                
                if (!usedStatus.contains(s))
                {
                    sb.append(getString(s.getStringResourceId()));
                    
                    usedStatus.add(s);
                }
            }
            statusNameTextView.setText(sb.toString());
        }
        else
        {
            statusNameTextView.setText("");
        }
        
        fragment.reDraw(issues);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.issues_per_assignee, menu);
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
    public void onStatusAndAssigneeSelected(Status status, User assignee)
    {
        ArrayList<Issue> extraIssues = new ArrayList<Issue>();
        for (Issue i : issues)
        {
            Status issueStatus = i.getStatus();
            User issueUser = i.getAssignee();
            if (issueUser == null)
            {
                issueUser = i.getAuthor();
            }
            
            if (issueStatus.equals(status) && issueUser.equals(assignee))
            {
                extraIssues.add(i);
            }
        }
        
        Intent i = new Intent(getApplicationContext(), IssuesPerStatusAndAssigneeActivity.class);
        i.putExtra(INTENT_KEY_ISSUES, extraIssues);
        i.putExtra(INTENT_KEY_PROJECT, project);
        i.putExtra(INTENT_KEY_ASSIGNEE, assignee);
        
        startActivity(i);
    }

    /**
     * Set up the {@link android.app.ActionBar}.
     */
    private void setupActionBar()
    {
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
