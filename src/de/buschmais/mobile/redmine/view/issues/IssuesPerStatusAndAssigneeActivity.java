package de.buschmais.mobile.redmine.view.issues;

import static de.buschmais.mobile.redmine.Constants.BUNDLE_KEY_ISSUE;
import static de.buschmais.mobile.redmine.Constants.INTENT_KEY_ASSIGNEE;
import static de.buschmais.mobile.redmine.Constants.INTENT_KEY_ISSUES;
import static de.buschmais.mobile.redmine.Constants.INTENT_KEY_PROJECT;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.buschmais.mobile.redmine.R;
import de.buschmais.mobile.redmine.data.Issue;
import de.buschmais.mobile.redmine.data.Issue.Status;
import de.buschmais.mobile.redmine.data.Project;
import de.buschmais.mobile.redmine.data.User;
import de.buschmais.mobile.redmine.view.issues.IssuesPerStatusAndAssigneeFragment.OnIssueSelectedListener;

/**
 * The activity to show the {@link IssuesPerStatusAndAssigneeFragment}. (This activity is currently not used.)
 */
public class IssuesPerStatusAndAssigneeActivity extends Activity implements OnIssueSelectedListener
{
    /** The project to show. */
    private Project project;

    /** The issues to show. */
    private List<Issue> issues;

    /** The assignee to show. */
    private User assignee;
    
    /** The fragment that shows the content. */
    private IssuesPerStatusAndAssigneeFragment fragment;
    
    /** Text view for the project name */
    private TextView projectNameTextView;
    
    /** Text view for the selected status */
    private TextView statusNameTextView;
    
    /** Text view for showing the name of the assignee */
    private TextView assigneeNameTextView;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issues_per_status_and_assignee);

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

            issues = (List<Issue>) extras.getSerializable(INTENT_KEY_ISSUES);
            if (issues == null)
            {
                throw new IllegalStateException("Unable to find isses in bundle extras for key '" + INTENT_KEY_ISSUES
                        + "'");
            }
            assignee = (User) extras.getSerializable(INTENT_KEY_ASSIGNEE);
            if (assignee == null)
            {
                throw new IllegalStateException("Unable to find assignee in bundle extras for key '"
                        + INTENT_KEY_ASSIGNEE + "'");
            }

        }
        else
        {
            throw new IllegalStateException(
                    "No extras found, make sure to bundle a project in the starting intent with key '"
                            + INTENT_KEY_PROJECT + "'");
        }
        
        fragment = (IssuesPerStatusAndAssigneeFragment) getFragmentManager().findFragmentById(R.id.fragmentIssuesPerStatusAndAssignee);
        
        projectNameTextView = (TextView) findViewById(R.id.textViewProjectName);
        statusNameTextView = (TextView) findViewById(R.id.textViewStatusName);
        assigneeNameTextView = (TextView) findViewById(R.id.textViewAssigneeName);
    }
    
    @Override
    protected void onResume()
    {
        super.onResume();
        
        projectNameTextView.setText(project.getName());
        assigneeNameTextView.setText(assignee.getName());
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
        
        fragment.addIssues(issues);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.issues_per_status_and_assignee, menu);
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
    public void onIssueSelected(Issue issue)
    {
        IssueDialog issueDialog = new IssueDialog();
        Bundle arguments = new Bundle();
        arguments.putSerializable(BUNDLE_KEY_ISSUE, issue);
        issueDialog.setArguments(arguments);
        
        issueDialog.show(getFragmentManager(), "issueDialog");
    }

    /**
     * Set up the {@link android.app.ActionBar}.
     */
    private void setupActionBar()
    {

        getActionBar().setDisplayHomeAsUpEnabled(true);

    }
}
