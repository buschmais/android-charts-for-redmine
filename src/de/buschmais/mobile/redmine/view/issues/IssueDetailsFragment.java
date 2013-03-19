package de.buschmais.mobile.redmine.view.issues;

import static de.buschmais.mobile.redmine.Constants.BUNDLE_KEY_ISSUE;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.buschmais.mobile.redmine.R;
import de.buschmais.mobile.redmine.data.Issue;

/**
 * A fragment to display the details for an issue.
 */
public class IssueDetailsFragment extends Fragment
{
    /** The tag that can be used within fragment transactions. */
    public static final String FRAGMENT_TRANSACTION_TAG = IssueDetailsFragment.class.getName();
    
    /** The log tag. */
    private static final String TAG = IssueDetailsFragment.class.getSimpleName();
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreateView()");
        
        Issue issue = (Issue) getArguments().getSerializable(BUNDLE_KEY_ISSUE);
        if (issue == null)
        {
            throw new IllegalStateException("Can not start fragment without any issue. Please provide an issue in the arguments bundle" +
            		" with key: " + BUNDLE_KEY_ISSUE);
        }

        View view = inflater.inflate(R.layout.dialog_issue, null);
        ((TextView) view.findViewById(R.id.textViewIssueSubject)).setText(issue.getSubject());
        ((TextView) view.findViewById(R.id.textViewIssueDescription)).setText(issue.getDescription());
        if (issue.getAssignee() != null)
        {
            ((TextView) view.findViewById(R.id.textViewIssueAssignee)).setText(issue.getAssignee().getName());
        }
        ((TextView) view.findViewById(R.id.textViewIssueAuthor)).setText(issue.getAuthor().getName());
        ((TextView) view.findViewById(R.id.textViewIssueCreated)).setText(issue.getCreated().toString());
        ((TextView) view.findViewById(R.id.textViewIssueUpdated)).setText(issue.getUpdated().toString());
        ((TextView) view.findViewById(R.id.textViewIssueStatus)).setText(issue.getStatus().toString());
        ((TextView) view.findViewById(R.id.textViewIssuePriority)).setText(issue.getPriority().toString());
        
        return view;
    }
}
