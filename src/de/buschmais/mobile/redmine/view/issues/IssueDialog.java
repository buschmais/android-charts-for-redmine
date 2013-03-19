package de.buschmais.mobile.redmine.view.issues;

import static de.buschmais.mobile.redmine.Constants.BUNDLE_KEY_ISSUE;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import de.buschmais.mobile.redmine.R;
import de.buschmais.mobile.redmine.data.Issue;

/**
 * A dialog fragment to show issue details.
 */
public class IssueDialog extends DialogFragment
{
    /** The tag that can be used within fragment transactions. */
    public static final String FRAGMENT_TRANSACTION_TAG = IssueDialog.class.getName();
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Issue issue = (Issue) getArguments().getSerializable(BUNDLE_KEY_ISSUE);

        LayoutInflater inflater = getActivity().getLayoutInflater();

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

        return new Builder(getActivity()).setView(view).setNeutralButton(R.string.issues_dialog_close, null)
                .create();
    }
}