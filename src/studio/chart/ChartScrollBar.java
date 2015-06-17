package studio.chart;

import java.awt.Color;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

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

    /** Plot */
    private XYPlot plot;

    /** flag in updating */
    private boolean updating = false;
    
    private double axisDataMin;
    private double axisDataMax;
    
    private double axisViewMin;
    private double axisViewMax;
    private double viewLength;
    
    /** ratio bar vs axis */
    private double barAxisRatio;
    
    private Color oldColor;
    
    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////
    
    public ChartScrollBar(int orientation, XYPlot plot) {
        super(orientation);
        this.plot = plot;
        
        // set min/max value
        ValueAxis axis = getValueAxis();
        Range originalRange = axis.getRange();
        axis.setAutoRange(true);
        axisDataMin = axis.getLowerBound();
        axisDataMax = axis.getUpperBound();
        axis.setRange(originalRange);
        
        // ratio
        barAxisRatio = BAR_MAX / (axisDataMax - axisDataMin);
        
        if (getXYPlot() != null && getValueAxis() != null) {
            getValueAxis().addChangeListener(this);
            addAdjustmentListener(this);
            if (getXYPlot().getDataset() != null) {
                getXYPlot().getDataset().addChangeListener(this);
            }
            axisUpdate();
            addMouseListener(this);
        }
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////
    
    public XYPlot getXYPlot() {
        return plot;
    }

    public ValueAxis getValueAxis() {
        if (orientation == VERTICAL) {
            return (ValueAxis) getXYPlot().getRangeAxis();
        } else {
            return (ValueAxis) getXYPlot().getDomainAxis();
        }
    }

    // //////////////////////////////////////
    // Method for axis
    // //////////////////////////////////////
    
    public void axisUpdate() {
        ValueAxis valueAxis = getValueAxis();
        if (valueAxis.isAutoRange()) {
            if (oldColor == null) {
                oldColor = getBackground();
            }
            setBackground(oldColor.brighter());
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
            
            // update view length
            Range dataRange = getValueAxis().getRange();
            viewLength = dataRange.getLength();
            axisViewMin = dataRange.getLowerBound();
            axisViewMax = dataRange.getUpperBound();
        
            int barExtent = (int) (viewLength * barAxisRatio);
            int barValue;
            if (orientation == VERTICAL) {
                barValue = (int) ((axisDataMax - axisViewMax) * barAxisRatio);
            } else {
                barValue = (int) ((axisViewMin - axisDataMin) * barAxisRatio);
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
        axisUpdate();
    }

    public void datasetChanged(DatasetChangeEvent event) {
        // System.out.println("ChartScrollBar.datasetChanged()");
        axisUpdate();
    }

    public void adjustmentValueChanged(AdjustmentEvent e) {
        // check updating
        if (updating) {
            return;
        }
        
        // Update
        try {
            updating = true;

            double lower = 0;
            double upper = 0;
            if (orientation == VERTICAL) {
                upper = axisDataMax - (getValue() / barAxisRatio);
                lower = upper - viewLength;
            } else {
                lower = getValue() / barAxisRatio + axisDataMin;
                upper = lower + viewLength;
            }
    
            if (upper > lower) {
                getValueAxis().setRange(lower, upper);
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
        getValueAxis().setAutoRange(true);
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
    
    /**
     * Adjust Range for scroll
     */
    public void adjustRange(double lower, double upper) {
        getValueAxis().setRange(lower, upper);
    }
    
}
