package de.buschmais.mobile.redmine.view.issues.charts;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import org.afree.chart.AFreeChart;
import org.afree.chart.ChartRenderingInfo;
import org.afree.chart.entity.ChartEntity;
import org.afree.chart.plot.PlotRenderingInfo;
import org.afree.graphics.geom.Dimension;
import org.afree.graphics.geom.RectShape;
import org.afree.ui.RectangleInsets;

import de.buschmais.mobile.redmine.R;

/**
 * Abstract class providing common functionality to display charts. <br/>
 * <br/>
 * To implement this, ensure that the protected member variable {@code chart} is set up properly.
 */
public abstract class AbstractChartView extends View
{
    /** The log tag. */
    private static final String TAG = AbstractChartView.class.getSimpleName();

    /** The minimum legth to move before a move event will be triggered */
    private static final int MOVE_TRIGGER_DISTANCE = 10;

    /** Used for drawing the chart */
    private RectangleInsets insets;
    /** Used for drawing the chart */
    private Dimension size;

    /** Rendering info, used to get informations from the rendered chart. */
    private ChartRenderingInfo renderingInfo = new ChartRenderingInfo();

    /** Helper class for managing touch/move events */
    private SingleTouchStartInfo singleTouchStartInfo = null;

    /** Indicating that the event is a move event */
    private boolean moveEvent = false;

    /** The chart to be drawn */
    protected AFreeChart chart;

    /** Show labels? */
    protected boolean showLabels;

    /** Show legend? */
    protected boolean showLegend;

    /** Show the title? */
    protected boolean showTitle;

    /** Small text size */
    protected float textSizeSmall;

    /** Medium text size */
    protected float textSizeMedium;

    public AbstractChartView(Context context)
    {
        super(context);
        
        Log.d(TAG, "AbstractChartView()");

        textSizeSmall = getResources().getDimension(R.dimen.graph_text_size_small);
        textSizeMedium = getResources().getDimension(R.dimen.graph_text_size_medium);
        
        Log.d(TAG, "text sizes: small=" + textSizeSmall + ", medium=" + textSizeMedium);
    }

    /**
     * Does the {@link IssueStatusPieChartView} show labels?
     * 
     * @return
     */
    public boolean isShowLabels()
    {
        return showLabels;
    }

    /**
     * Set the {@link IssueStatusPieChartView} to show labels.
     * 
     * @param showLabels
     */
    public void setShowLabels(boolean showLabels)
    {
        this.showLabels = showLabels;
    }

    /**
     * Does the {@link IssueStatusPieChartView} show a legend?
     * 
     * @return
     */
    public boolean isShowLegend()
    {
        return showLegend;
    }

    /**
     * Set the {@link IssueStatusPieChartView} to show a legend.
     * 
     * @param showLegend
     */
    public void setShowLegend(boolean showLegend)
    {
        this.showLegend = showLegend;
    }

    /**
     * Does the {@link IssueStatusPieChartView} show a title?
     * 
     * @return
     */
    public boolean isShowTitle()
    {
        return showTitle;
    }

    /**
     * Set the {@link IssueStatusPieChartView} to show a title.
     * 
     * @param showTitle
     */
    public void setShowTitle(boolean showTitle)
    {
        this.showTitle = showTitle;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        super.onTouchEvent(event);

        int action = event.getAction();
        int pointerCount = event.getPointerCount();
        float x = event.getX();
        float y = event.getY();

        switch (action & MotionEvent.ACTION_MASK)
        {
        case MotionEvent.ACTION_DOWN:
        case MotionEvent.ACTION_POINTER_DOWN:
            if (pointerCount == 1 && singleTouchStartInfo == null)
            {
                singleTouchStartInfo = new SingleTouchStartInfo(x, y);
                
                ChartEntity entity = renderingInfo.getEntityCollection().getEntity(x, y);
                handleActionDownTouchEvent(entity);
            }
            break;

        case MotionEvent.ACTION_MOVE:
            if (pointerCount == 1 && singleTouchStartInfo != null)
            {

                boolean moveTriggerX = Math.abs(x - singleTouchStartInfo.getX()) >= MOVE_TRIGGER_DISTANCE;
                boolean moveTriggerY = Math.abs(y - singleTouchStartInfo.getY()) >= MOVE_TRIGGER_DISTANCE;

                if (moveTriggerX || moveTriggerY)
                {
                    // movement
                    moveEvent = true;
                    
                    handleActionMoveEvent(new PointF(singleTouchStartInfo.getX(), singleTouchStartInfo.getY()), new PointF(x, y), renderingInfo.getPlotInfo());
                }

                singleTouchStartInfo = new SingleTouchStartInfo(x, y);
            }
            break;

        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_POINTER_UP:
            if (!moveEvent)
            {
                ChartEntity entity = renderingInfo.getEntityCollection().getEntity(x, y);
                handleActionUpTouchEvent(entity);
            }
            singleTouchStartInfo = null;
            moveEvent = false;
            break;

        default:
            break;
        }

        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        insets = new RectangleInsets(0, 0, 0, 0);
        size = new Dimension(w, h);

        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        drawChart(canvas);
    }

    /**
     * Handles the chart entity on which an "ACTION_UP" event was triggered in the {@link AbstractChartView#onTouchEvent(MotionEvent)}.
     * 
     * @param entity the entity that was touched, can be {@code null}
     */
    protected abstract void handleActionUpTouchEvent(ChartEntity entity);

    /**
     * Handles the chart entity on which an "ACTION_DOWN" event was triggered in the {@link AbstractChartView#onTouchEvent(MotionEvent)}.
     * 
     * @param entity the entity that was touched, can be {@code null}
     */
    protected abstract void handleActionDownTouchEvent(ChartEntity entity);

    /**
     * Handles a move event in the chart area.
     * 
     * @param source the point from where the movement should start
     * @param target the point to where to move
     * @param info additional info about the drawing area
     */
    protected abstract void handleActionMoveEvent(PointF source, PointF target, PlotRenderingInfo info);

    /**
     * Draw the chart.
     * 
     * @param canvas
     */
    protected void drawChart(Canvas canvas)
    {
        RectShape available = new RectShape(insets.getLeft(), insets.getTop(), size.getWidth() - insets.getLeft()
                - insets.getRight(), size.getHeight() - insets.getTop() - insets.getBottom());
        double drawWidth = available.getWidth();
        double drawHeight = available.getHeight();

        if (drawWidth < 10)
        {
            drawWidth = 10;
        }
        else if (drawWidth > 1280)
        {
            drawWidth = 1280;
        }

        if (drawHeight < 10)
        {
            drawHeight = 10;
        }
        else if (drawHeight > 1280)
        {
            drawHeight = 1280;
        }

        RectShape chartArea = new RectShape(0.0, 0.0, drawWidth, drawHeight);

        chart.draw(canvas, chartArea, renderingInfo);
    }

    /**
     * Information for Single touch start
     */
    private class SingleTouchStartInfo
    {
        private final float x;
        private final float y;

        public SingleTouchStartInfo(float x, float y)
        {
            this.x = x;
            this.y = y;
        }

        public float getX()
        {
            return x;
        }

        public float getY()
        {
            return y;
        }
    }
}
