package de.buschmais.mobile.redmine.view.issues.charts;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.util.Log;

import java.text.NumberFormat;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.afree.chart.AFreeChart;
import org.afree.chart.ChartFactory;
import org.afree.chart.axis.CategoryAxis;
import org.afree.chart.axis.NumberTickUnit;
import org.afree.chart.axis.TickUnits;
import org.afree.chart.axis.ValueAxis;
import org.afree.chart.entity.CategoryItemEntity;
import org.afree.chart.entity.ChartEntity;
import org.afree.chart.plot.CategoryPlot;
import org.afree.chart.plot.PlotOrientation;
import org.afree.chart.plot.PlotRenderingInfo;
import org.afree.chart.plot.SlidingCategoryPlot;
import org.afree.chart.renderer.category.AndroidStyleBarRenderer;
import org.afree.chart.renderer.category.BarRenderer;
import org.afree.data.category.CategoryDataset;
import org.afree.data.category.DefaultCategoryDataset;
import org.afree.data.category.SlidingCategoryDataset;
import org.afree.graphics.PaintType;
import org.afree.graphics.SolidColor;
import org.afree.graphics.geom.Font;

import de.buschmais.mobile.redmine.R;
import de.buschmais.mobile.redmine.data.Issue;
import de.buschmais.mobile.redmine.data.Issue.Priority;
import de.buschmais.mobile.redmine.data.Issue.Status;
import de.buschmais.mobile.redmine.data.User;

/**
 * A view to render a bar chart.
 */
public class IssuesPerAssigneeBarChartView extends AbstractChartView
{
    /** The log tag. */
    private static final String TAG = IssuesPerAssigneeBarChartView.class.getSimpleName();

    /** The listener if a bar was clicked. */
    private OnStatusAndAssigneeSelectionListener listener;
    
    /** The renderer that is used by this chart view. */
    private BarChartViewRenderer renderer;

    /**
     * Create a new {@link IssuesPerAssigneeBarChartView} using the given application context
     * 
     * @param context
     */
    public IssuesPerAssigneeBarChartView(Context context)
    {
        this(context, null);
    }
    
    /**
     * Create a new {@link IssuesPerAssigneeBarChartView} using the given context and issues.
     * @param context
     * @param issues
     */
    public IssuesPerAssigneeBarChartView(Context context, List<Issue> issues)
    {
        super(context);
        
        chart = initializeChart(issues);
    }

    @Override
    protected void handleActionDownTouchEvent(ChartEntity entity)
    {
        if (entity instanceof CategoryItemEntity)
        {
            CategoryItemEntity cie = (CategoryItemEntity) entity;
            
            CategoryDataset dataset = cie.getDataset();
            
            int columnIndex = dataset.getColumnIndex(cie.getColumnKey());
            int rowIndex = dataset.getRowIndex(cie.getRowKey());
            renderer.setActiveIndexes(rowIndex, columnIndex);
            
            invalidate();
        }
    }
    
    @Override
    protected void handleActionUpTouchEvent(ChartEntity entity)
    {
        if (entity instanceof CategoryItemEntity)
        {
            CategoryItemEntity cie = (CategoryItemEntity) entity;
            User assignee = (User) cie.getColumnKey();
            Status status = (Status) cie.getRowKey();

            if (listener != null)
            {
                listener.onStatusAndAssigneeSelected(status, assignee);
            }
            
            renderer.setIndexesInactive();

            invalidate();
        }
    }

    @Override
    protected void handleActionMoveEvent(PointF source, PointF target, PlotRenderingInfo info)
    {
        renderer.setIndexesInactive();

        CategoryPlot plot = chart.getCategoryPlot();

        double moveX = source.x - target.x;
        double horizontalMovePercentage = moveX / info.getDataArea().getWidth();

        plot.moveDomainAxes(horizontalMovePercentage, info, source);
        
        invalidate();
    }

    /**
     * Set the {@link OnStatusAndAssigneeSelectionListener}.
     * 
     * @param listener
     */
    public void setOnStatusAndAssigneeSelectionListener(OnStatusAndAssigneeSelectionListener listener)
    {
        this.listener = listener;
    }

    /**
     * Redraw the chart using the given issues and active status objects.
     * 
     * @param issues the issues with which the chart should be drawn
     * @param activeStatus the status objects which will be treated as active
     */
    public void reDraw(List<Issue> issues)
    {
        Log.d(TAG, "reDraw()");

        chart = initializeChart(issues);

        invalidate();
    }

    /**
     * Initialize and return a chart that will be drawn.
     * 
     * @param issues the issues used to initialize
     * @param activeStatus the status object that should be shown
     * @return the chart that will be drawn
     */
    private AFreeChart initializeChart(List<Issue> issues)
    {
        Log.d(TAG, "initializeChart()");

        String chartTitle;
        if (showTitle)
        {
            chartTitle = getContext().getString(R.string.issues_details_chart_title);
        }
        else
        {
            chartTitle = null;
        }

        String xAxisTitle = getContext().getString(R.string.issues_details_chart_x_title);
        String yAxisTitle = getContext().getString(R.string.issues_details_chart_y_title);

        Map<UserStatusKey, Integer> userPerStatusMap = countUsersPerStatus(issues);
        CategoryDataset dataset = createDataset(userPerStatusMap);

        boolean legend = showLegend;
        boolean tooltips = false;
        boolean urls = false;

        AFreeChart barChart = ChartFactory.createSlidingBarChart(chartTitle, xAxisTitle, yAxisTitle, dataset,
                PlotOrientation.VERTICAL, legend, tooltips, urls);
        barChart.setBackgroundPaintType(new SolidColor(Color.TRANSPARENT));
        barChart.setBorderPaintType(new SolidColor(Color.TRANSPARENT));
        
        int maxValue = 1;
        for (Map.Entry<UserStatusKey, Integer> entry : userPerStatusMap.entrySet())
        {
            Integer value = entry.getValue();
            if (value > maxValue)
            {
                maxValue = value;
            }
        }

        customizePlot(barChart.getCategoryPlot(), (maxValue+1));

        return barChart;
    }

    /**
     * Customize the given plot.
     * 
     * @param plot the plot to customize
     * @param maxValue the max value for the y-axis
     */
    @SuppressWarnings("unchecked")
    private void customizePlot(CategoryPlot plot, int maxValue)
    {
        /* A style renderer not using any shadow or "3D" effect */
        AndroidStyleBarRenderer androidStyleBarRenderer = new AndroidStyleBarRenderer();

        List<Status> rowKeys = plot.getDataset().getRowKeys();
        renderer = new BarChartViewRenderer(rowKeys);
        renderer.setBarPainter(androidStyleBarRenderer);
        renderer.setItemMargin(0.01);
        renderer.setDrawBarOutline(true);
        plot.setRenderer(renderer);
        
        Log.d(TAG, "active stroke size: " + getResources().getDimension(R.dimen.graph_active_element_stroke_size));

        TickUnits tickUnitSource = new TickUnits();
        tickUnitSource.add(new NumberTickUnit(1, NumberFormat.getInstance(), 0));
        tickUnitSource.add(new NumberTickUnit(10, NumberFormat.getInstance(), 0));
        tickUnitSource.add(new NumberTickUnit(50, NumberFormat.getInstance(), 0));

        Font fontMedium = new Font(Typeface.DEFAULT, Typeface.NORMAL, Math.round(textSizeMedium));
        Font fontSmall = new Font(Typeface.DEFAULT, Typeface.NORMAL, Math.round(textSizeSmall));

        ValueAxis yAxis = plot.getRangeAxis();
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(maxValue);
        yAxis.setMinorTickMarksVisible(false);
        yAxis.setStandardTickUnits(tickUnitSource);
        yAxis.setLabelFont(fontMedium);
        yAxis.setTickLabelFont(fontSmall);

        CategoryAxis xAxis = plot.getDomainAxis();
        xAxis.setLabelFont(fontMedium);
        xAxis.setTickLabelFont(fontSmall);
        
        ((SlidingCategoryPlot)plot).setSlideRatio(0.04f);
    }

    /**
     * Creates a data set using the given issues.
     * 
     * @param issues the issues, if this is {@code null} or empty, an empty data set will be returned
     * @param activeStatus the active status objects, if this is empty or {@code null} all status will be active
     * @return
     */
    private CategoryDataset createDataset(Map<UserStatusKey, Integer> userPerStatusMap)
    {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (Entry<UserStatusKey, Integer> entry : userPerStatusMap.entrySet())
        {
            UserStatusKey key = entry.getKey();

            Integer value = entry.getValue();
            User columnKey = key.getUser();
            Status rowKey = key.getStatus();

            dataset.addValue(value, rowKey, columnKey);
        }
        
        SlidingCategoryDataset slidingCategoryDataset = new SlidingCategoryDataset(dataset, 0, 5);
        return slidingCategoryDataset;
    }

    /**
     * Initialize the map with zero values for all user/status combinations. Count this combinations and return their
     * values.
     * 
     * @param issues
     * @return
     */
    private Map<UserStatusKey, Integer> countUsersPerStatus(List<Issue> issues)
    {
        Map<UserStatusKey, Integer> map = new TreeMap<UserStatusKey, Integer>();

        if (issues != null)
        {
            for (Issue i : issues)
            {
                User user = i.getAssignee();
                if (user == null)
                {
                    user = i.getAuthor();
                }

                UserStatusKey key = new UserStatusKey(user, i.getStatus());

                if (!map.containsKey(key))
                {
                    map.put(key, Integer.valueOf(1));
                }
                else
                {
                    Integer value = map.get(key);
                    value++;
                    map.put(key, value);
                }
            }
        }

        return map;
    }

    /**
     * Helper class for maps based upon the {@link Priority} and {@link Status}.
     */
    private class UserStatusKey implements Comparable<UserStatusKey>
    {
        private User u;
        private Status s;

        public UserStatusKey(User user, Status status)
        {
            u = user;
            s = status;
        }

        public User getUser()
        {
            return u;
        }

        public Status getStatus()
        {
            return s;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((s == null) ? 0 : s.hashCode());
            result = prime * result + ((u == null) ? 0 : u.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            UserStatusKey other = (UserStatusKey) obj;
            if (!getOuterType().equals(other.getOuterType()))
                return false;
            if (s != other.s)
                return false;
            if (u == null)
            {
                if (other.u != null)
                    return false;
            }
            else if (!u.equals(other.u))
                return false;
            return true;
        }

        @Override
        public String toString()
        {
            return "[" + u + "$" + s + "]";
        }

        @Override
        public int compareTo(UserStatusKey another)
        {
            int result = u.compareTo(another.u);
            if (result == 0)
            {
                result = s.compareTo(another.s);
            }
            return result;
        }

        private IssuesPerAssigneeBarChartView getOuterType()
        {
            return IssuesPerAssigneeBarChartView.this;
        }
    }

    /**
     * A renderer to render the bars with the proper color.
     */
    private class BarChartViewRenderer extends BarRenderer
    {
        /** serialVersionUID */
        private static final long serialVersionUID = 7060980878067192495L;
        
        /** A marker that an index is inactive. */
        private static final int INACTIVE_MARKER = -1;
        
        private final List<Status> rows;
        
        private int activeRow = INACTIVE_MARKER;
        private int activeColumn = INACTIVE_MARKER;

        public BarChartViewRenderer(List<Status> rowKeys)
        {
            rows = rowKeys;
        }
        
        @Override
        public PaintType getItemOutlinePaintType(int row, int column)
        {
            if (activeRow == INACTIVE_MARKER
                    && activeColumn == INACTIVE_MARKER)
            {
                return super.getItemOutlinePaintType(row, column);
            } 
            else if (activeRow == row && activeColumn == column) 
            {
                return new SolidColor(getResources().getColor(R.color.color_active_element_stroke));
            }
            else
            {
                
                return super.getItemOutlinePaintType(row, column);
            }
        }
        
        @Override
        public Float getItemOutlineStroke(int row, int column)
        {
            if (activeRow == row && activeColumn == column) 
            {
                return getResources().getDimension(R.dimen.graph_active_element_stroke_size);
            }
            else
            {
                return super.getItemStroke(row, column);
            }
        }
        
        @Override
        public PaintType getItemPaintType(int row, int column)
        {
            Status s = rows.get(row);
            switch (s)
            {
            case New:
                return new SolidColor(getResources().getColor(R.color.color_status_new));
            case In_Progress:
                return new SolidColor(getResources().getColor(R.color.color_status_in_progress));
            case Resolved:
                return new SolidColor(getResources().getColor(R.color.color_status_resolved));
            case Feedback:
                return new SolidColor(getResources().getColor(R.color.color_status_feedback));
            case Closed:
                return new SolidColor(getResources().getColor(R.color.color_status_closed));
            case Rejected:
                return new SolidColor(getResources().getColor(R.color.color_status_rejected));
            default:
                return super.getItemPaintType(row, column);
            }
        }
        
        /**
         * Set the given row/column as active indexes.
         * 
         * @param row
         * @param column
         */
        public void setActiveIndexes(int row, int column)
        {
            activeRow = row;
            activeColumn = column;
        }
        
        /**
         * Set the internal indexes to be inactive.
         */
        public void setIndexesInactive()
        {
            activeRow = -1;
            activeColumn = -1;
        }
    }

    /**
     * A listener that will be called if a status and assignee will be selected.
     */
    public interface OnStatusAndAssigneeSelectionListener
    {
        /**
         * Called if the status and assignee are selected.
         * 
         * @param status
         * @param assignee
         */
        void onStatusAndAssigneeSelected(Status status, User assignee);
    }
}
