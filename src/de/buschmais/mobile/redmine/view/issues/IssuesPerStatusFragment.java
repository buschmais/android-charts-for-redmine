package de.buschmais.mobile.redmine.view.issues;

import static de.buschmais.mobile.redmine.Constants.BUNDLE_KEY_ISSUES;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.buschmais.mobile.redmine.R;
import de.buschmais.mobile.redmine.data.Issue;
import de.buschmais.mobile.redmine.data.Issue.Status;
import de.buschmais.mobile.redmine.view.issues.charts.IssueStatusPieChartView;
import de.buschmais.mobile.redmine.view.issues.charts.IssueStatusPieChartView.OnStatusSelectionChangedListener;

/**
 * A fragment to show show all issues of a project in a pie chart.
 */
public class IssuesPerStatusFragment extends Fragment implements OnCheckedChangeListener
{
    /** The tag that can be used within a fragment transaction */
    public static final String FRAGMENT_TRANSACTION_TAG = IssuesPerStatusFragment.class.getName();
    
    /** The log tag. */
    private static final String TAG = IssuesPerStatusFragment.class.getSimpleName();
    
    /** The pie chart view that draws the issues */
    private IssueStatusPieChartView pieChartView;
    
    /** The map of issues */
    private HashMap<Issue.Status, List<Issue>> issues;
    
    private CheckBox cbNew;
    private CheckBox cbInProgress;
    private CheckBox cbFeedback;
    private CheckBox cbResolved;
    private CheckBox cbClosed;
    private CheckBox cbRejected;
    
    @SuppressWarnings("unchecked")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Log.i(TAG, "onCreateView()");
        
        View view = inflater.inflate(R.layout.fragment_issues_per_status, container, false);
        
        pieChartView = new IssueStatusPieChartView(getActivity());
        pieChartView.setOnStatusSelectedListener((OnStatusSelectionChangedListener) getActivity());
        
        FrameLayout chartContainer = (FrameLayout) view.findViewById(R.id.chartContainer);
        chartContainer.addView(pieChartView);
        
        cbNew        = (CheckBox) view.findViewById(R.id.checkBoxStatusNew); 
        cbInProgress = (CheckBox) view.findViewById(R.id.checkBoxStatusInProgress); 
        cbFeedback   = (CheckBox) view.findViewById(R.id.checkBoxStatusFeedback);
        cbResolved   = (CheckBox) view.findViewById(R.id.checkBoxStatusResolved);
        cbClosed     = (CheckBox) view.findViewById(R.id.checkBoxStatusClosed);
        cbRejected   = (CheckBox) view.findViewById(R.id.checkBoxStatusRejected);
        
        cbNew.setOnCheckedChangeListener(this); 
        cbInProgress.setOnCheckedChangeListener(this);
        cbFeedback.setOnCheckedChangeListener(this); 
        cbResolved.setOnCheckedChangeListener(this); 
        cbClosed.setOnCheckedChangeListener(this); 
        cbRejected.setOnCheckedChangeListener(this); 
        
        cbNew.setText(getString(R.string.issues_status_new_details, 0));
        cbInProgress.setText(getString(R.string.issues_status_in_progress_details, 0));
        cbFeedback.setText(getString(R.string.issues_status_feedback_details, 0));
        cbResolved.setText(getString(R.string.issues_status_resolved_details, 0));
        cbClosed.setText(getString(R.string.issues_status_closed_details, 0));
        cbRejected.setText(getString(R.string.issues_status_rejected_details, 0));
        
        Log.d(TAG, "checkbox padding left: " + cbNew.getPaddingLeft());
        Log.d(TAG, "checkbox padding left offset: " + getResources().getDimension(R.dimen.checkbox_padding_left_offset));
        fixCheckboxPaddings(cbNew, (int)getResources().getDimension(R.dimen.checkbox_padding_left_offset));
        fixCheckboxPaddings(cbInProgress, (int)getResources().getDimension(R.dimen.checkbox_padding_left_offset));
        fixCheckboxPaddings(cbFeedback, (int)getResources().getDimension(R.dimen.checkbox_padding_left_offset));
        fixCheckboxPaddings(cbResolved, (int)getResources().getDimension(R.dimen.checkbox_padding_left_offset));
        fixCheckboxPaddings(cbClosed, (int)getResources().getDimension(R.dimen.checkbox_padding_left_offset));
        fixCheckboxPaddings(cbRejected, (int)getResources().getDimension(R.dimen.checkbox_padding_left_offset));
        
        // recreated from a configuration change
        if (savedInstanceState != null && savedInstanceState.containsKey(BUNDLE_KEY_ISSUES))
        {
            issues = (HashMap<Status, List<Issue>>) savedInstanceState.get(BUNDLE_KEY_ISSUES);
        }
        // created from a fragment arguments
        else if (getArguments() != null && getArguments().containsKey(BUNDLE_KEY_ISSUES))
        {
            issues = (HashMap<Status, List<Issue>>) getArguments().getSerializable(BUNDLE_KEY_ISSUES);
        }
        
        return view;
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        outState.putSerializable(BUNDLE_KEY_ISSUES, issues);
        
        super.onSaveInstanceState(outState);
    }
    
    @Override
    public void onResume()
    {
        super.onResume();
        
        if (issues != null)
        {
            updateView();
        }
    }
    
    @Override
    public void onAttach(Activity activity)
    {
        Log.i(TAG, "onAttach()");
        
        super.onAttach(activity);
        
        try
        {
            OnStatusSelectionChangedListener.class.cast(activity);
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException("Ensure the attaching activity implements '" + OnStatusSelectionChangedListener.class.getName() + "'.");
        }
    }
    
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    {
        updateView();
    }
    
    /**
     * Set the issues (and replace existing issues) to this overview fragment and immediately update the content of the fragment.
     * 
     * @param issues
     */
    public void reDraw(HashMap<Issue.Status, List<Issue>> issues)
    {
        Log.i(TAG, "reDraw()");
        
        this.issues = issues;
        updateView();
    }
    
    /**
     * Fixes the padding of the check boxes.
     * 
     * @param checkBox
     * @param scale
     */
    private void fixCheckboxPaddings(CheckBox checkBox, int paddingLeftOffset)
    {
        checkBox.setPadding((checkBox.getPaddingLeft() + paddingLeftOffset),
                checkBox.getPaddingTop(),
                checkBox.getPaddingRight(),
                checkBox.getPaddingBottom());
    }

    /**
     * Updates the view using the given map of issues.
     * 
     * @param issues
     */
    private void updateView()
    {
        List<Issue> drawingList = new ArrayList<Issue>();
        
        // new
        int newStatusSize = issues.get(Status.New).size();
        cbNew.setText(getString(R.string.issues_status_new_details, newStatusSize));
        if (newStatusSize == 0)
        {
            cbNew.setChecked(false);
            cbNew.setEnabled(false);
        }
        else
        {
            if (cbNew.isChecked())
            {
                drawingList.addAll(issues.get(Status.New));
            }
        }
        
        // in progress
        int inProgressStatusSize = issues.get(Status.In_Progress).size();
        cbInProgress.setText(getString(R.string.issues_status_in_progress_details, inProgressStatusSize));
        if (inProgressStatusSize == 0)
        {
            cbInProgress.setChecked(false);
            cbInProgress.setEnabled(false);
        }
        else
        {
            if (cbInProgress.isChecked())
            {
                drawingList.addAll(issues.get(Status.In_Progress));
            }
        }
        
        // feedback
        int feedbackStatusSize = issues.get(Status.Feedback).size();
        cbFeedback.setText(getString(R.string.issues_status_feedback_details, feedbackStatusSize));
        if (feedbackStatusSize == 0)
        {
            cbFeedback.setChecked(false);
            cbFeedback.setEnabled(false);
        }
        else
        {
            if (cbFeedback.isChecked())
            {
                drawingList.addAll(issues.get(Status.Feedback));
            }
        }
        
        // resolved
        int resolvedStatusSize = issues.get(Status.Resolved).size();
        cbResolved.setText(getString(R.string.issues_status_resolved_details, resolvedStatusSize));
        if (resolvedStatusSize == 0)
        {
            cbResolved.setChecked(false);
            cbResolved.setEnabled(false);
        }
        else
        {
            if (cbResolved.isChecked())
            {
                drawingList.addAll(issues.get(Status.Resolved));
            }
        }
        
        // closed
        int closedStatusSize = issues.get(Status.Closed).size();
        cbClosed.setText(getString(R.string.issues_status_closed_details, closedStatusSize));
        if (closedStatusSize == 0)
        {
            cbClosed.setChecked(false);
            cbClosed.setEnabled(false);
        }
        else
        {
            if (cbClosed.isChecked())
            {
                drawingList.addAll(issues.get(Status.Closed));
            }
        }
        
        // rejected
        int rejectedStatusSize = issues.get(Status.Rejected).size();
        cbRejected.setText(getString(R.string.issues_status_rejected_details, rejectedStatusSize));
        if (rejectedStatusSize == 0)
        {
            cbRejected.setChecked(false);
            cbRejected.setEnabled(false);
        }
        else
        {
            if (cbRejected.isChecked())
            {
                drawingList.addAll(issues.get(Status.Rejected));
            }
        }
        
        pieChartView.reDraw(drawingList);
    }
}
