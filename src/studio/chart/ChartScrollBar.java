package studio.chart;

import java.awt.Color;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JScrollBar;

import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.SegmentedTimeline;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.event.AxisChangeListener;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;

public class ChartScrollBar extends JScrollBar implements AdjustmentListener, AxisChangeListener, DatasetChangeListener, MouseListener {

    private static final long serialVersionUID = 1L;

    // //////////////////////////////////////
    // Filed
    // //////////////////////////////////////

    /** scroll increment ratio */
    public static final double SCROLL_INCREMENT_PAGE = 1.0;
    public static final double SCROLL_INCREMENT_LARGE = 0.1;
    public static final double SCROLL_INCREMENT_SMALL = 0.02;

    /** bar size */
    private static final int BAR_MIN = 0;
    private static final int BAR_MAX = 1_000_000;

    /** Plot (plot count is NOT the same as axis count) */
    private List<XYPlot> plots;

    /** axis */
    private List<ValueAxis> valueAxisList = new ArrayList<>();

    /** min/max of data of axis */
    private List<Double> axisDataMinList = new ArrayList<>();
    private List<Double> axisDataMaxList = new ArrayList<>();

    /** ratio bar vs axis */
    private List<Double> barAxisRatioList = new ArrayList<>();

    /** For X axis, if use timeline. Otherwise null */
    private SegmentedTimeline timeline;
    private int originalExtent = 0;
    private int originalIncludeSegmentCount = 0;
    private long displayTimeMilliSec = 0;

    /** bar color backup */
    private Color oldColor;

    /** flag in updating */
    private boolean updating = false;

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    /**
     * For combined chart
     * 
     * @param orientation
     * @param plots
     */
    public ChartScrollBar(int orientation, List<XYPlot> plots, SegmentedTimeline timeline) {
        super(orientation);
        this.plots = plots;
        this.timeline = timeline;

        // setup axis
        if (orientation == HORIZONTAL) {
            // one domain axis
            ValueAxis axis = plots.get(0).getDomainAxis();
            setupAxisValues(plots.get(0), axis);
        } else {
            // VERTICAL
            for (XYPlot plot : plots) {
                ValueAxis axis = plot.getRangeAxis();
                setupAxisValues(plot, axis);

                if (plot.getRangeAxisCount() >= 2) {
                    ValueAxis axis2 = plot.getRangeAxis(1);
                    setupAxisValues(plot, axis2);
                }
            }
        }

        // add listener to axis (first axis only)
        valueAxisList.get(0).addChangeListener(this);
        addAdjustmentListener(this);
        // TODO other datasets?
        plots.get(0).getDataset().addChangeListener(this);
        addMouseListener(this);

        // Update initially
        for (ValueAxis axis : valueAxisList) {
            axisUpdate(axis);
        }
    }

    /**
     * Setup axis values initially
     */
    private void setupAxisValues(XYPlot plot, ValueAxis axis) {
        // data range
        Range dataRange = plot.getDataRange(axis);
        double axisDataMin = dataRange.getLowerBound();
        double axisDataMax = dataRange.getUpperBound();
        // margin
        double margin = dataRange.getLength() * 0.005;
        axisDataMin -= margin;
        axisDataMax += margin;
        // ratio
        double barAxisRatio = BAR_MAX / (axisDataMax - axisDataMin);
        // add to list
        valueAxisList.add(axis);
        axisDataMinList.add(axisDataMin);
        axisDataMaxList.add(axisDataMax);
        barAxisRatioList.add(barAxisRatio);
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    public List<XYPlot> getPlots() {
        return plots;
    }

    public int searchIndex(ValueAxis valueAxis) {
        for (int i = 0; i < valueAxisList.size(); i++) {
            if (valueAxisList.get(i).equals(valueAxis)) {
                return i;
            }
        }
        return -1;
    }

    // //////////////////////////////////////
    // Method for axis
    // //////////////////////////////////////

    public void axisUpdate(ValueAxis valueAxis) {
        if (valueAxis.isAutoRange()) {
            if (oldColor == null) {
                oldColor = getBackground();
            }
            setBackground(oldColor.brighter());
            setValues(0, BAR_MAX, BAR_MIN, BAR_MAX);
            return;
        } else if (oldColor != null) {
            setBackground(oldColor);
            oldColor = null;
        }

        // check updating
        if (updating) {
            return;
        }

        // Update
        try {
            updating = true;
            int index = searchIndex(valueAxis);

            // update view length
            Range dataRange = valueAxis.getRange();
            double viewLength = dataRange.getLength();
            double axisViewMin = dataRange.getLowerBound();
            double axisViewMax = dataRange.getUpperBound();

            int barExtent = (int) (viewLength * barAxisRatioList.get(index));
            int barValue;
            if (orientation == VERTICAL) {
                barValue = (int) ((axisDataMaxList.get(index) - axisViewMax) * barAxisRatioList.get(index));
            } else {
                barValue = (int) ((axisViewMin - axisDataMinList.get(index)) * barAxisRatioList.get(index));
            }
            // System.out.println("ChartScrollBar.axisUpdate(): newValue: " + barValue + " newExtent: " + barExtent + " newMin: " + BAR_MIN + " newMax: " + BAR_MAX);
            setValues(barValue, barExtent, BAR_MIN, BAR_MAX);

            // update increment
            setUnitIncrement(barExtent / 10);
            setBlockIncrement(barExtent);

        } finally {
            updating = false;
        }
    }

    @Override
    public void axisChanged(AxisChangeEvent event) {
        // System.out.println("ChartScrollBar.axisChanged() " + event.getType() + " " + ((ValueAxis) event.getAxis()).getRange());
        // axis bar is change ONLY first axis
        if (event.getAxis().equals(valueAxisList.get(0))) {
            axisUpdate(valueAxisList.get(0));
        }
    }

    @Override
    public void datasetChanged(DatasetChangeEvent event) {
        // System.out.println("ChartScrollBar.datasetChanged()");
        axisUpdate(valueAxisList.get(0));
    }

    @Override
    public void adjustmentValueChanged(AdjustmentEvent e) {
        // check updating
        if (updating) {
            return;
        }

        // Update
        try {
            updating = true;

            for (int i = 0; i < valueAxisList.size(); i++) {
                ValueAxis axis = valueAxisList.get(i);
                double viewLength = axis.getRange().getLength();
                double lower = 0;
                double upper = 0;
                if (orientation == VERTICAL) {
                    upper = axisDataMaxList.get(i) - (getValue() / barAxisRatioList.get(i));
                    lower = upper - viewLength;
                } else {
                    lower = getValue() / barAxisRatioList.get(i) + axisDataMinList.get(i);
                    upper = lower + viewLength;
                }

                if (upper > lower) {
                    axis.setRange(lower, upper);
                }
            }

        } finally {
            updating = false;
        }
    }

    private void updateDisplayRangeForTimeline(boolean isPlus) {
        DateAxis domainAxis = (DateAxis) valueAxisList.get(0);
        if (timeline.containsDomainRange(domainAxis.getMinimumDate(), domainAxis.getMaximumDate())) {
            // if not contain exception value
            if (getModel().getExtent() != originalExtent && originalExtent != 0) {
                System.out.println("Reset Extent: " + getModel().getExtent() + " -> " + originalExtent);
                setValues(getValue(), originalExtent, BAR_MIN, BAR_MAX);
                originalExtent = 0;
                axisUpdate(domainAxis);
            }
            return;
        }
//        if (originalExtent == 0) {
//            originalExtent = getModel().getExtent();
//        }

        long lower = timeline.getTime(domainAxis.getMinimumDate());
        long upper = timeline.getTime(domainAxis.getMaximumDate());
        long segmentSize = timeline.getSegmentSize();

        // lower / upper
        if (isPlus) {
            // Plus
            if (!timeline.containsDomainValue(lower)) {
                lower = lower % segmentSize == 0 ? lower : lower + segmentSize - (lower % segmentSize);
                while (!timeline.containsDomainValue(lower)) {
                    lower += segmentSize;
                }
            }
            if (!timeline.containsDomainValue(upper)) {
                upper = upper % segmentSize == 0 ? upper : upper + segmentSize - (upper % segmentSize);
                while (!timeline.containsDomainValue(upper)) {
                    upper += segmentSize;
                }
            }
        } else {
            // Minus
            if (!timeline.containsDomainValue(lower)) {
                lower = lower - (lower % segmentSize);
                while (!timeline.containsDomainValue(lower)) {
                    lower -= segmentSize;
                }
            }
            if (!timeline.containsDomainValue(upper)) {
                upper = upper - (upper % segmentSize);
                while (!timeline.containsDomainValue(upper)) {
                    upper -= segmentSize;
                }
            }
        }

        // between
        int icludeSegmentCount = 0;
        int excludeSegmentCount = 0;
        long time;
        if (isPlus) {
            time = lower;
            while (time < upper) {
                if (!timeline.containsDomainValue(time)) {
                    excludeSegmentCount++;
                } else {
                    icludeSegmentCount++;
                }
                time += segmentSize;
            }
        } else {
            time = upper;
            while (time > lower) {
                if (!timeline.containsDomainValue(time)) {
                    excludeSegmentCount++;
                } else {
                    icludeSegmentCount++;
                }
                time -= segmentSize;
            }
        }

//        int barExtent = (int) (excludeSegmentCount * segmentSize * barAxisRatioList.get(0) + originalExtent);
        // setValues(getValue(), barExtent, BAR_MIN, BAR_MAX);
        
//        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//        System.out.println("Extent Cur/Org = " + barExtent + " / " + originalExtent + " | " + format.format(domainAxis.getMinimumDate()) + " - " + format.format(domainAxis.getMaximumDate())
//                + " | " + lower + " - " + upper + " - " + (upper - lower) + " | " + icludeSegmentCount);

        
        if (originalIncludeSegmentCount == 0) {
            originalIncludeSegmentCount = icludeSegmentCount;
        }

        if (originalExtent == 0 || icludeSegmentCount < originalIncludeSegmentCount * 0.95 || icludeSegmentCount > originalIncludeSegmentCount * 1.05) {
            originalExtent = getModel().getExtent();
        }
        
        // Update Range
        if (lower < upper) {
            domainAxis.setRange(lower, upper);
        }
    }
    
    private boolean isTimelineEnabled() {
        // Only X-axis and DateAxis
        if (orientation == HORIZONTAL && timeline != null && valueAxisList.get(0) instanceof DateAxis) {
            return true;
        } else {
            return false;
        }
    }

    // //////////////////////////////////////
    // Method (Mouse Event)
    // //////////////////////////////////////

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            zoomFull();
        }
    }

    private void zoomFull() {
        for (ValueAxis axis : valueAxisList) {
            axis.setAutoRange(true);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // do nothing
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // do nothing
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // do nothing
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // do nothing
    }

    /**
     * scroll with mouse wheel or cursor key
     * 
     * @param isDownOrRight
     * @param incrementRatio
     */
    public void incrementScroll(boolean isDownOrRight, double incrementRatio) {
        if (isTimelineEnabled()) {
            updateDisplayRangeForTimeline(isDownOrRight);
        }

        // increment value = extent * ratio
        int extent = originalExtent != 0 ? originalExtent : getModel().getExtent();
        int incrementValue = (int) (extent * incrementRatio);

        // up/left or down/right
        int newValue = 0;
        if (isDownOrRight) {
            newValue = getValue() + incrementValue;
        } else {
            newValue = getValue() - incrementValue;
        }

        // set value
        if (newValue <= getMinimum()) {
            setValue(Math.min(getValue(), 0));
        } else if (newValue >= getMaximum()) {
            setValue(Math.max(getValue(), getMaximum()));
        } else {
            setValue(newValue);
        }
    }

}
