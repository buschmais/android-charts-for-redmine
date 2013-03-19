package de.buschmais.mobile.redmine.view.issues;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.buschmais.mobile.redmine.R;
import de.buschmais.mobile.redmine.data.Issue;

/**
 * An adapter that shows issue items. A tag will be set at any view element containing the
 * actual {@link Issue}. The id to get the issue is {@code R.id.tagIssueItemIssue}.
 */
public class IssueItemAdapter extends BaseAdapter
{
    private final LayoutInflater inflater;
    private final List<Issue> issues;
    
    public IssueItemAdapter(Context context)
    {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        issues = new ArrayList<Issue>();
    }

    @Override
    public int getCount()
    {
        return issues.size();
    }

    @Override
    public Issue getItem(int position)
    {
        return issues.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return getItem(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        Issue item = getItem(position);
        if (item == null)
        {
            return null;
        }
        
        View view;
        ViewHolder viewholder;
        if (convertView != null)
        {
            view = convertView;
            viewholder = (ViewHolder) convertView.getTag(R.id.tagIssueItemViewHolder);
        }
        else
        {
            view = inflater.inflate(R.layout.item_issue, parent, false);
            
            viewholder = new ViewHolder();
            viewholder.priorityView = (TextView) view.findViewById(R.id.textViewIssuePriority);
            viewholder.subjectView = (TextView) view.findViewById(R.id.textViewIssueSubject);
            viewholder.statusView = (TextView) view.findViewById(R.id.textViewIssueStatus);
            
            view.setTag(R.id.tagIssueItemViewHolder, viewholder);
        }
        
        viewholder.priorityView.setText(item.getPriority().toString());
        viewholder.statusView.setText(item.getStatus().toString());
        viewholder.subjectView.setText(item.getSubject());
        
        view.setTag(R.id.tagIssueItemIssue, item);
        
        return view;
    }
    
    /**
     * Add the given list of issues to the list of issues of this adapter. The adapter does <b>not</b> notify 
     * anyone that the set of data has changed.
     * 
     * @param issueList the list to add
     */
    public void addIssues(List<Issue> issueList)
    {
        issues.addAll(issueList);
    }
    
    /**
     * Remove the given list of issues from the list of issues of this adapter. The adapter does <b>not</b> notify 
     * anyone that the set of data has changed.
     * 
     * @param issueList the list to remove
     */
    public void removeIssues(List<Issue> issueList)
    {
        issues.removeAll(issueList);
    }
    
    /**
     * Clear the list of issues.  The adapter does <b>not</b> notify 
     * anyone that the set of data has changed.
     */
    public void clearIssues()
    {
        issues.clear();
    }

    /**
     * Helper class to hold the elements of a view object. This avoids the calls to {@link View#findViewById(int)}.
     */
    private class ViewHolder
    {
        TextView statusView;
        TextView subjectView;
        TextView priorityView;
    }
}
