package de.buschmais.mobile.redmine.view.projects;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.buschmais.mobile.redmine.R;
import de.buschmais.mobile.redmine.data.Project;

/**
 * An adapter to display information about projects in a list view.
 */
public final class ProjectItemAdapter extends BaseAdapter implements Filterable
{
    /** Object to keep locks */
    private final Object lock = new Object();
    
    /** Layout inflater used to create views */
    private final LayoutInflater inflater;
    
    /** The original list of projects */
    private List<Project> projects;
    
    /** The list of unfiltered projects */
    private List<Project> originalProjects;

    /**
     * Creating a new adapter using the given context.
     * 
     * @param context the application context
     */
    public ProjectItemAdapter(Context context)
    {
        super();
        
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        projects = new ArrayList<Project>();
    }

    @Override
    public int getCount()
    {
        return projects.size();
    }

    @Override
    public Project getItem(int position)
    {
        return projects.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return getItem(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        Project item = getItem(position); 
        if (item == null)
        {
            return null;
        }
        
        View view;
        ViewHolder viewHolder;
        if (convertView != null)
        {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag(R.id.tagProjectItemViewHolder);
        }
        else
        {
            view = inflater.inflate(R.layout.item_project, parent, false);
            viewHolder = new ViewHolder();
            
            viewHolder.nameView = (TextView) view.findViewById(R.id.textViewProjectItemName);
            viewHolder.descriptionView = (TextView) view.findViewById(R.id.textViewProjectItemDescription);
            viewHolder.createdView = (TextView) view.findViewById(R.id.textViewProjectItemCreated);
            viewHolder.updatedView = (TextView) view.findViewById(R.id.textViewProjectItemUpdated);
            
            view.setTag(R.id.tagProjectItemViewHolder, viewHolder);
        }
        
        viewHolder.nameView.setText(item.getName());
        viewHolder.descriptionView.setText(item.getDescription());
        viewHolder.createdView.setText(item.getCreated().toString());
        viewHolder.updatedView.setText(item.getUpdated().toString());
        
        view.setTag(R.id.tagProjectItemProject, item);
        
        return view;
    }
    
    /**
     * Replace the existing list of projects (if any) with the given new one. The 
     * method will notify any listeners for that item change.
     * 
     * @param newProjects the new list of projects to use
     */
    public void setProjects(List<Project> newProjects)
    {
        synchronized (lock)
        {
            if (originalProjects != null)
            {
                originalProjects.clear();
                originalProjects.addAll(newProjects);
            }
            else
            {
                projects.clear();
                projects.addAll(newProjects);
            }
        }
        notifyDataSetChanged();
    }
    
    @Override
    public Filter getFilter()
    {
        return new ProjectsFilter();
    }

    /**
     * Helper class to hold the elements of a view object. This avoids the calls to {@link View#findViewById(int)}.
     */
    private class ViewHolder
    {
        TextView nameView;
        TextView descriptionView;
        TextView createdView;
        TextView updatedView;
    }
    
    private class ProjectsFilter extends Filter
    {
        @Override
        protected FilterResults performFiltering(CharSequence aConstraint)
        {
            if (originalProjects == null)
            {
                synchronized (lock)
                {
                    originalProjects = new ArrayList<Project>(projects);
                }
            }
            
            final String constraint = aConstraint.toString().toLowerCase();
            
            FilterResults result = new FilterResults();
            
            List<Project> values;
            synchronized (lock)
            {
                values = new ArrayList<Project>(originalProjects);
            }

            if (constraint == null || constraint.isEmpty())
            {
                // do nothing, keep the list as it is
                result.values = values;
                result.count = values.size();
            }
            else
            {
                List<Project> newValues = new ArrayList<Project>();
                
                if (constraint.startsWith("id:"))
                {
                    // filter for the id of projects
                    String[] splitted = constraint.split(":");
                    
                    if (splitted.length == 2)
                    {
                        // only proceed if the contraint was "id:<aNumber>", so lenght should be 2
                        String projectId = splitted[1];
                        synchronized (lock)
                        {                        
                            for (Project p : originalProjects)
                            {
                                if (Integer.toString(p.getId()).startsWith(projectId))
                                {
                                    newValues.add(p);
                                }
                            }
                        }
                    }
                }
                else if (constraint.startsWith("identifier:"))
                {
                    // filter for the identifier of projects
                    String[] splitted = constraint.split(":");
                    
                    if (splitted.length == 2)
                    {
                        // only proceed if the contraint was "id:<aNumber>", so lenght should be 2
                        String projectId = splitted[1];
                        synchronized (lock)
                        {                        
                            for (Project p : originalProjects)
                            {
                                if (p.getIdentifier().startsWith(projectId))
                                {
                                    newValues.add(p);
                                }
                            }
                        }
                    }
                }
                else
                {
                    // filter for project name
                    synchronized (lock)
                    {                        
                        for (Project p : originalProjects)
                        {
                            if (p.getName().contains(constraint))
                            {
                                newValues.add(p);
                            }
                        }
                    }
                }
                
                result.values = newValues;
                result.count = newValues.size();
            }
            
            return result;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results)
        {
            projects = (List<Project>) results.values;
            if (results.count > 0)
            {
                notifyDataSetChanged();
            }
            else
            {
                notifyDataSetInvalidated();
            }
        }
        
    }
}
