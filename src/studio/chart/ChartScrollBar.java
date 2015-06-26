package studio.chart;

import java.awt.Color;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JScrollBar;

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
    private static final int BAR_MAX = 1000000;

    
    /** Plot (plot count is NOT the same as axis count) */
    private List<XYPlot> plots;
    
    /** axis */
    private List<ValueAxis> valueAxisList = new ArrayList<>();
    
    /** min/max of data of axis */
    private List<Double> axisDataMinList = new ArrayList<>();
    private List<Double> axisDataMaxList = new ArrayList<>();
    
    /** ratio bar vs axis */
    private List<Double> barAxisRatioList = new ArrayList<>();
    
    /** bar color backup */
    private Color oldColor;
    
    /** flag in updating */
    private boolean updating = false;
    
    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////
    
    /**
     * For combined chart
     * @param orientation
     * @param plots
     */
    public ChartScrollBar(int orientation, List<XYPlot> plots) {
        super(orientation);
        this.plots = plots;
        
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
            setUnitIncrement(barExtent/10);
            setBlockIncrement(barExtent);
            
        } finally {
            updating = false;
        }
    }

    public void axisChanged(AxisChangeEvent event) {
        // System.out.println("ChartScrollBar.axisChanged() " + ((ValueAxis) event.getAxis()).getRange());
        // axis bar is change ONLY first axis
        if (event.getAxis().equals(valueAxisList.get(0))) {
            axisUpdate(valueAxisList.get(0));
        }
    }

    public void datasetChanged(DatasetChangeEvent event) {
        // System.out.println("ChartScrollBar.datasetChanged()");
        // axisUpdate();
        axisUpdate(valueAxisList.get(0));
    }

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

    // //////////////////////////////////////
    // Method (Mouse Event)
    // //////////////////////////////////////
    
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            zoomFull();
        }
    }

    public void zoomFull() {
        for (ValueAxis axis : valueAxisList) {
            axis.setAutoRange(true);
        }
    }
    
    public void mouseEntered(MouseEvent e) {
        // do nothing
    }

    public void mouseExited(MouseEvent e) {
        // do nothing
    }

    public void mousePressed(MouseEvent e) {
        // do nothing
    }

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
        // increment value = extent * ratio
        int incrementValue = (int) (getModel().getExtent() * incrementRatio);
        
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
