package de.buschmais.mobile.redmine.view.issues.charts;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.afree.chart.AFreeChart;
import org.afree.chart.ChartFactory;
import org.afree.chart.entity.ChartEntity;
import org.afree.chart.entity.PieSectionEntity;
import org.afree.chart.labels.StandardPieSectionLabelGenerator;
import org.afree.chart.plot.PiePlot;
import org.afree.chart.plot.PlotRenderingInfo;
import org.afree.data.general.DefaultPieDataset;
import org.afree.data.general.PieDataset;
import org.afree.graphics.PaintType;
import org.afree.graphics.SolidColor;

import de.buschmais.mobile.redmine.R;
import de.buschmais.mobile.redmine.data.Issue;
import de.buschmais.mobile.redmine.data.Issue.Status;

/**
 * A view that displays pie charts for issue categories.
 */
public class IssueStatusPieChartView extends AbstractChartView
{
    /** The default stroke size. */
    private static final float DEFAULT_STROKE_SIZE = 0.5f;
    
    /** The listener that will be informed if a status was selected */
    private OnStatusSelectionChangedListener listener;

    /** A map holding the exploration status of explored status issue elements */
    private Map<Status, Boolean> exploredElements = new HashMap<Issue.Status, Boolean>();
    
    /** The one and only status that was sent in reDraw. We need this issue cause if only
     * on element is in the data set, the chart rendered does not return a pie section */
    private Status oneAndOnlyStatus;
    
    /** Set if parts of the pie should be explorable. */
    private boolean explorable;
    
    /** The plot to change based upon different touch events. */
    private PiePlot plot;
    
    /** The pie chart section that was highlighted with a touch event. */
    private PieSectionEntity highlightedSection;
    
    /**
     * Create a new {@link IssueStatusPieChartView} object.
     * 
     * @param context
     */
    public IssueStatusPieChartView(Context context)
    {
        super(context);

        int statusLength = Status.values().length;
        for (int i = 0; i < statusLength; i++)
        {
            exploredElements.put(Status.values()[i], Boolean.FALSE);
        }

        chart = initializeChart(null);
    }

    /**
     * Are parts of the pie explorable?
     * 
     * @return
     */
    public boolean isExplorable()
    {
        return explorable;
    }

    /**
     * Set if parts of the pie should be explorable.
     * 
     * @param explorable
     */
    public void setExplorable(boolean explorable)
    {
        this.explorable = explorable;
    }

    /**
     * Use the given issues and invalidate the whole view.
     * 
     * @param issues the list of issues to be drawn
     */
    public void reDraw(List<Issue> issues)
    {
        chart = initializeChart(issues);
        
        // try to determine if the list of issues contains more than one status
        boolean moreThanOneStatus = false;
        Status s = null;
        for (Issue i : issues)
        {
            if (s == null)
            {
                s = i.getStatus();
            }
            else
            {
                if (s != i.getStatus())
                {
                    moreThanOneStatus = true;
                    break;
                }
            }
        }
        if (!moreThanOneStatus)
        {
            oneAndOnlyStatus = s;
        }
        else
        {
            oneAndOnlyStatus = null;
        }
        
        invalidate();
    }

    /**
     * Set the listener to this view.
     * 
     * @param onStatusSelectedListener
     */
    public void setOnStatusSelectedListener(OnStatusSelectionChangedListener onStatusSelectedListener)
    {
        listener = onStatusSelectedListener;
    }

    @Override
    protected void handleActionUpTouchEvent(ChartEntity entity)
    {
        highlightChartSection(null);
        
        if (entity != null && (entity instanceof PieSectionEntity))
        {
            PieSectionEntity pieSection = (PieSectionEntity) entity;
            Status selectedStatus = (Status) pieSection.getSectionKey();

            toggleExploration(selectedStatus);
        }
        else if (oneAndOnlyStatus != null)
        {
            // the user has clicked on the chart, but there is no pie section in it
            // cause we only have one status
            // so we don't care the position he has clicked and pretend that he
            // has selected the only on status
            toggleExploration(oneAndOnlyStatus);
        }
    }
    
    @Override
    protected void handleActionDownTouchEvent(ChartEntity entity)
    {
        if (entity != null && (entity instanceof PieSectionEntity))
        {
            PieSectionEntity pieSection = (PieSectionEntity) entity;
            highlightChartSection(pieSection);
        }
        
        invalidate();
    }
    
    @Override
    protected void handleActionMoveEvent(PointF source, PointF target, PlotRenderingInfo info)
    {
        highlightChartSection(null);
        
        invalidate();
    }
    
    /**
     * Does some highlighting for the given section. The plot will be manipulated to achieve this.
     * 
     * @param section
     */
    private void highlightChartSection(PieSectionEntity section)
    {
        if (highlightedSection != null)
        {
            // we have to "un-highlight" something
            Status s = (Status) highlightedSection.getSectionKey();
            PaintType pt = new SolidColor(getResources().getColor(s.getColorResoureId()));
            
            plot.setSectionOutlineStroke(s, DEFAULT_STROKE_SIZE);
            plot.setSectionOutlinePaintType(s, pt);
        }
        
        if (section != null)
        {
            // highlight something
            Status s = (Status) section.getSectionKey();
            
            plot.setSectionOutlineStroke(s, getResources().getDimension(R.dimen.graph_active_element_stroke_size));
            plot.setSectionOutlinePaintType(s, new SolidColor(getResources().getColor(R.color.color_active_element_stroke)));
        }
        
        highlightedSection = section;
    }

    /**
     * Toggle the exploration of different status.
     * 
     * @param selectedStatus
     */
    private void toggleExploration(Status selectedStatus)
    {
        // if "explorable" is enabled we need to send 2 events
        // if not, we only send the status-selected event
        if (explorable)
        {
            Boolean exploration = exploredElements.get(selectedStatus);
            if (exploration == Boolean.TRUE) 
            // unselect the element
            {
                plot.setExplodePercent(selectedStatus, 0.0);
                exploredElements.put(selectedStatus, Boolean.FALSE);
    
                if (listener != null)
                {
                    listener.onStatusUnselected(selectedStatus);
                }
            }
            else
            // select the element
            {
                plot.setExplodePercent(selectedStatus, 0.2);
                exploredElements.put(selectedStatus, Boolean.TRUE);
    
                if (listener != null)
                {
                    listener.onStatusSelected(selectedStatus);
                }
            }
        }
        else
        {
            if (listener != null)
            {
                listener.onStatusSelected(selectedStatus);
            }
        }

        invalidate();
    }

    /**
     * Initialize and return a chart that can be drawn.
     * 
     * @param issues the issues used for initialization
     * @return the chart that should be drawn
     */
    private AFreeChart initializeChart(List<Issue> issues)
    {
        String chartTitle;
        if (showTitle)
        {
            chartTitle = getContext().getString(R.string.issues_overview_chart_title);
        }
        else
        {
            chartTitle = null;
        }

        boolean legend = showLegend;
        boolean tooltips = false;
        boolean urls = false;

        PieDataset dataset = createPieDataset(issues);

        AFreeChart pieChart = ChartFactory.createPieChart(chartTitle, dataset, legend, tooltips, urls);
        pieChart.setBackgroundPaintType(new SolidColor(Color.TRANSPARENT));
        pieChart.setBorderPaintType(new SolidColor(Color.TRANSPARENT));

        plot = (PiePlot) pieChart.getPlot();
        customizePlot(plot);

        return pieChart;
    }

    /**
     * Customize the view of the plot.
     * 
     * @param aPlot the plot to be customized
     */
    private void customizePlot(PiePlot aPlot)
    {
        if (showLabels)
        {
            aPlot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0} ({1})"));
        }
        else
        {
            aPlot.setLabelGenerator(null);
        }

        PaintType colorNew = new SolidColor(getResources().getColor(Status.New.getColorResoureId()));
        aPlot.setSectionPaintType(Status.New, colorNew);
        aPlot.setSectionOutlinePaintType(Status.New, colorNew);
        aPlot.setSectionOutlineStroke(Status.New, DEFAULT_STROKE_SIZE);
        
        PaintType colorProgress = new SolidColor(getResources().getColor(Status.In_Progress.getColorResoureId()));
        aPlot.setSectionPaintType(Status.In_Progress, colorProgress);
        aPlot.setSectionOutlinePaintType(Status.In_Progress, colorProgress);
        aPlot.setSectionOutlineStroke(Status.In_Progress, DEFAULT_STROKE_SIZE);
        
        PaintType colorResolved = new SolidColor(getResources().getColor(Status.Resolved.getColorResoureId())); 
        aPlot.setSectionPaintType(Status.Resolved, colorResolved);
        aPlot.setSectionOutlinePaintType(Status.Resolved, colorResolved);
        aPlot.setSectionOutlineStroke(Status.Resolved, DEFAULT_STROKE_SIZE);
        
        PaintType colorFeedback = new SolidColor(getResources().getColor(Status.Feedback.getColorResoureId()));
        aPlot.setSectionPaintType(Status.Feedback, colorFeedback);
        aPlot.setSectionOutlinePaintType(Status.Feedback, colorFeedback);
        aPlot.setSectionOutlineStroke(Status.Feedback, DEFAULT_STROKE_SIZE);
        
        PaintType colorClosed = new SolidColor(getResources().getColor(Status.Closed.getColorResoureId()));
        aPlot.setSectionPaintType(Status.Closed, colorClosed);
        aPlot.setSectionOutlinePaintType(Status.Closed, colorClosed);
        aPlot.setSectionOutlineStroke(Status.Closed, DEFAULT_STROKE_SIZE);
        
        PaintType colorRejected = new SolidColor(getResources().getColor(Status.Rejected.getColorResoureId())); 
        aPlot.setSectionPaintType(Status.Rejected, colorRejected);
        aPlot.setSectionOutlinePaintType(Status.Rejected, colorRejected);
        aPlot.setSectionOutlineStroke(Status.Rejected, DEFAULT_STROKE_SIZE);

        // set the exploration status of pie pieces (if exploration is enabled)
        if (explorable)
        {
            for (Entry<Status, Boolean> explorStatus : exploredElements.entrySet())
            {
                if (explorStatus.getValue() == Boolean.TRUE)
                {
                    aPlot.setExplodePercent(explorStatus.getKey(), 0.2);
                }
                else
                {
                    aPlot.setExplodePercent(explorStatus.getKey(), 0.0);
                }
            }
        }
        
        aPlot.setBackgroundPaintType(new SolidColor(Color.TRANSPARENT));
    }

    /**
     * Create a pie data set based upon the given list of issues.
     * 
     * @param issues the list of issues to be used
     * @return a possibly empty pie data set
     */
    private PieDataset createPieDataset(final List<Issue> issues)
    {
        Map<Status, Integer> filtered = filterIssues(issues);

        DefaultPieDataset pieDataset = new DefaultPieDataset();

        for (Entry<Status, Integer> filteredEntry : filtered.entrySet())
        {
            Integer count = filteredEntry.getValue();
            Status status = filteredEntry.getKey();

            if (status != Status.Unknown)
            {
                pieDataset.setValue(status, count);
            }
        }

        return pieDataset;
    }

    /**
     * Create a map of {@link Status} objects and count of them in the list of issues.
     * 
     * @param issues the issues to filter, can be {@code null}
     * @return a map with all available {@link Status} objects and their count (which all may be zero)
     */
    private Map<Status, Integer> filterIssues(List<Issue> issues)
    {
        final int statusLength = Status.values().length;
        Map<Status, Integer> filteredIssues = new HashMap<Issue.Status, Integer>(statusLength);
        for (int i = 0; i < statusLength; i++)
        {
            filteredIssues.put(Status.values()[i], Integer.valueOf(0));
        }

        if (issues != null)
        {
            for (Issue issue : issues)
            {
                Status status = issue.getStatus();
                Integer count = filteredIssues.get(status);
                count = count + 1;
                filteredIssues.put(status, count);
            }
        }

        return filteredIssues;
    }

    /**
     * The {@link OnStatusSelectionChangedListener} will be informed if a status was selected.
     */
    public interface OnStatusSelectionChangedListener
    {
        /**
         * Fired if a status was selected by the caller.
         * 
         * @param status the selected status
         */
        void onStatusSelected(Status status);

        /**
         * Fired if a status was unselected by the caller. <b>Note:</b> this event is only sent if {@link IssueStatusPieChartView#setExplorable(boolean)} is set to {@code true}.
         * 
         * @param status the status that was unselected
         */
        void onStatusUnselected(Status status);
    }
}
