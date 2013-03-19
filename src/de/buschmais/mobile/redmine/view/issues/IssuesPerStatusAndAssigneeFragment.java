package de.buschmais.mobile.redmine.view.issues;

import static de.buschmais.mobile.redmine.Constants.BUNDLE_KEY_ISSUES;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import de.buschmais.mobile.redmine.R;
import de.buschmais.mobile.redmine.data.Issue;

/** 
 * A fragment that displays a list of issues.
 */
public class IssuesPerStatusAndAssigneeFragment extends Fragment implements OnItemClickListener
{
    /** The transaction tag than can be used within fragment transactions. */
    public static final String FRAGMENT_TRANSACTION_TAG = IssuesPerStatusAndAssigneeFragment.class.getName();
    
    /** The adapter that is used to show the issues. */
    private IssueItemAdapter adapter;
    
    /** The list of issues to show. */
    private List<Issue> issues = new ArrayList<Issue>();
    
    /** The listener */
    private OnIssueSelectedListener listener;
    
    @SuppressWarnings("unchecked")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_issues_per_status_and_assignee, container, false);
        
        adapter = new IssueItemAdapter(getActivity());
        
        ListView list = (ListView) v.findViewById(R.id.listViewIssues);
        list.setAdapter(adapter);
        list.setOnItemClickListener(this);
        
        if (getArguments() != null && getArguments().containsKey(BUNDLE_KEY_ISSUES))
        {
            issues = (List<Issue>) getArguments().getSerializable(BUNDLE_KEY_ISSUES);
        }
        
        return v;
    }
    
    @Override
    public void onResume()
    {
        super.onResume();
        
        adapter.addIssues(issues);
        adapter.notifyDataSetChanged();
    }
    
    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        
        try
        {
            listener = (OnIssueSelectedListener) activity;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException("Ensure that the attaching activity implements '" + OnIssueSelectedListener.class.getName() + "'.");
        }
    }
    
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        Issue issue = (Issue) view.getTag(R.id.tagIssueItemIssue);
        
        if (issue != null)
        {
            listener.onIssueSelected(issue);
        }
    }
    
    /**
     * Add the given list of issues to the issues of the fragment. This usually should be done before the 
     * fragment is actually drawn.
     * 
     * @param listOfIssues
     */
    public void addIssues(List<Issue> listOfIssues)
    {
        issues.addAll(listOfIssues);
    }
    
    /**
     * Listener for events if an issue was selected.
     */
    public interface OnIssueSelectedListener
    {
        /**
         * Fired if an issue was selected.
         * 
         * @param issue the issue that was selected
         */
        void onIssueSelected(Issue issue);
    }
}
