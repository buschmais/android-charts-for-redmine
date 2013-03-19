package de.buschmais.mobile.redmine.view.issues;

import static de.buschmais.mobile.redmine.Constants.BUNDLE_KEY_ISSUES;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.List;

import de.buschmais.mobile.redmine.R;
import de.buschmais.mobile.redmine.data.Issue;
import de.buschmais.mobile.redmine.view.issues.charts.IssuesPerAssigneeBarChartView;
import de.buschmais.mobile.redmine.view.issues.charts.IssuesPerAssigneeBarChartView.OnStatusAndAssigneeSelectionListener;

/**
 * A fragment to show issues for a selected status and project by owner.
 */
public class IssuesPerAssigneeFragment extends Fragment
{
    /** The tag that can be used within a fragment transaction */
    public static final String FRAGMENT_TRANSACTION_TAG = IssuesPerAssigneeFragment.class.getName();
    
    /** The log tag. */
    private static final String TAG = IssuesPerAssigneeFragment.class.getSimpleName();

    /** The chart view that displays the issues */
    private IssuesPerAssigneeBarChartView chartView;
    
    @SuppressWarnings("unchecked")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Log.i(TAG, "onCreateView()");
        
        View v = inflater.inflate(R.layout.fragment_issues_per_assignee, container, false);
        
        List<Issue> issues = null;
        if (getArguments() != null && getArguments().containsKey(BUNDLE_KEY_ISSUES))
        {
            issues = (List<Issue>) getArguments().getSerializable(BUNDLE_KEY_ISSUES);
        }
        
        chartView = new IssuesPerAssigneeBarChartView(getActivity(), issues);
        chartView.setOnStatusAndAssigneeSelectionListener((OnStatusAndAssigneeSelectionListener) getActivity());
        
        ((FrameLayout) v.findViewById(R.id.chartContainer)).addView(chartView);
        
        return v;
    }
    
    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        
        try
        {
            OnStatusAndAssigneeSelectionListener.class.cast(activity);
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException("Ensure activity implements '" + OnStatusAndAssigneeSelectionListener.class.getName() + "'.");
        }
    }
    
    /**
     * Redraw this activity using the provides issues.
     * 
     * @param issues
     */
    public void reDraw(List<Issue> issues)
    {
        chartView.reDraw(issues);
    }
}
