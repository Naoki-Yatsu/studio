package studio.chart;

import java.awt.BorderLayout;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollBar;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.jfree.data.time.ohlc.OHLCSeriesCollection;
import org.jfree.data.xy.XYDataset;

/**
 * JPanel for scroll chart
 */
public class ChartScrollPanel extends JPanel implements MouseWheelListener, KeyListener {

    private static final long serialVersionUID = 1L;
    
    // //////////////////////////////////////
    // Filed
    // //////////////////////////////////////
    
    private boolean autoScrollRange;
    private List<Double> rangeLengthList = new ArrayList<>();
    
    /** scroll bar*/
    private ChartScrollBar scrollBarX;
    private ChartScrollBar scrollBarY;
    
    /** Plot */
    private List<XYPlot> plots;
    
    /** Range Axis */
    private ValueAxis domainAxis;
    private Map<Integer, ValueAxis> rangeAxisMap = new HashMap<>();
    
    /** values list */
    private Map<Integer, List<List<Double>>> domainValuesListMap = new HashMap<>();
    private Map<Integer, List<List<Double>>> rangeValuesListMap = new HashMap<>();
    
    
    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////
    
    @SuppressWarnings("unchecked")
    public ChartScrollPanel(ChartPanel chartPanel, boolean autoScrollRange, List<Double> scrollRangeLengthList) {
        this.autoScrollRange = autoScrollRange;
        for (Double length : scrollRangeLengthList) {
            if (Double.isNaN(length)) {
                rangeLengthList.add(0.0);
            } else {
                rangeLengthList.add(length);
            }
        }
        
        setLayout(new BorderLayout());
        add(chartPanel, BorderLayout.CENTER);
        addMouseWheelListener(this);
        
        // plot
        XYPlot plot = chartPanel.getChart().getXYPlot();
        domainAxis = plot.getDomainAxis();
        // CombinedDomainXYPlot
        if (plot instanceof CombinedDomainXYPlot) {
            this.plots = ((CombinedDomainXYPlot) plot).getSubplots();
        } else {
            this.plots = new ArrayList<>();
            plots.add(plot);
        }
        
        // scroll X
        this.scrollBarX = new ChartScrollBar(JScrollBar.HORIZONTAL, plots);
        add(scrollBarX, BorderLayout.SOUTH);
        
        // scroll Y 
        this.scrollBarY = new ChartScrollBar(JScrollBar.VERTICAL, plots);
        add(scrollBarY, BorderLayout.EAST);
        
        // setup data range for auto scroll (VERTICAL only)
        if (autoScrollRange) {
            int axisIndex = 0;
            for (XYPlot xyPlot : plots) {
                rangeAxisMap.put(axisIndex, xyPlot.getRangeAxis());
                setupViewRangeForRangeAxis(axisIndex, xyPlot.getDataset());
                // for Y1 Left-N
                // data count without right axis
                int dataCount = xyPlot.getDatasetCount();
                if (xyPlot.getRangeAxisCount() >= 2) {
                    dataCount--;
                }
                for (int i = 2; i < dataCount + 1; i++) {
                    setupViewRangeForRangeAxis(axisIndex, xyPlot.getDataset(i));
                }
                axisIndex++;
                
                // Right Axis
                if (xyPlot.getRangeAxisCount() >= 2) {
                    rangeAxisMap.put(axisIndex, xyPlot.getRangeAxis(1));
                    setupViewRangeForRangeAxis(axisIndex, xyPlot.getDataset(1));
                    axisIndex++;
                }
            }
        }
    }
    
    private void setupViewRangeForRangeAxis(Integer axisIndex, XYDataset dataset) {
        if (!domainValuesListMap.containsKey(axisIndex)) {
            domainValuesListMap.put(axisIndex, new ArrayList<>());
        }
        if (!rangeValuesListMap.containsKey(axisIndex)) {
            rangeValuesListMap.put(axisIndex, new ArrayList<>());
        }
        List<List<Double>> domainValuesList = domainValuesListMap.get(axisIndex);
        List<List<Double>> rangeValuesList = rangeValuesListMap.get(axisIndex);

        if (dataset instanceof OHLCSeriesCollection) {
            // For OHLC
            List<Double> domainList = new ArrayList<>();
            List<Double> rangeListHigh = new ArrayList<>();
            List<Double> rangeListLow = new ArrayList<>();
            // add domain twice for high/low
            domainValuesList.add(domainList);
            domainValuesList.add(domainList);
            rangeValuesList.add(rangeListHigh);
            rangeValuesList.add(rangeListLow);
            
            OHLCSeriesCollection ohlcDataset = (OHLCSeriesCollection) dataset;
            for (int itemIndex = 0; itemIndex < ohlcDataset.getItemCount(0); itemIndex++) {
                domainList.add(dataset.getXValue(0, itemIndex));
                rangeListHigh.add(ohlcDataset.getHighValue(0, itemIndex));
                rangeListLow.add(ohlcDataset.getLowValue(0, itemIndex));
            }

        } else {
            // For XYSeriesCollection, TimeSeriesCollection
            for (int series = 0; series < dataset.getSeriesCount(); series++) {
                List<Double> domainList = new ArrayList<>();
                List<Double> rangeList = new ArrayList<>();
                domainValuesList.add(domainList);
                rangeValuesList.add(rangeList);
                
                // value list for series
                for (int item = 0; item < dataset.getItemCount(series); item++) {
                    domainList.add(dataset.getXValue(series, item));
                    rangeList.add(dataset.getYValue(series, item));
                }
            }
        }
    }
    
    // //////////////////////////////////////
    // Method (Listener)
    // //////////////////////////////////////

    @Override
    public void keyPressed(KeyEvent e) {
        // System.out.println("### keyPressed");
        if (domainAxis.isAutoRange()) {
            return;
        }

        // increment for cursor, down/right or up/left
        double cursorIncrement = ChartScrollBar.SCROLL_INCREMENT_SMALL;

        // shift or ctrl mask
        int mod = e.getModifiersEx();
        if ((mod & InputEvent.SHIFT_DOWN_MASK) != 0 || (mod & InputEvent.CTRL_DOWN_MASK) != 0) {
            // mask on, use large scroll
            cursorIncrement = ChartScrollBar.SCROLL_INCREMENT_LARGE;
        }

        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                scrollBarY.incrementScroll(false, cursorIncrement);
                break;
            case KeyEvent.VK_DOWN:
                scrollBarY.incrementScroll(true, cursorIncrement);
                break;
            case KeyEvent.VK_LEFT:
                scrollBarX.incrementScroll(false, cursorIncrement);
                adjustRangeAxis();
                break;
            case KeyEvent.VK_RIGHT:
                scrollBarX.incrementScroll(true, cursorIncrement);
                adjustRangeAxis();
                break;
            case KeyEvent.VK_PAGE_UP:
                scrollBarX.incrementScroll(true, ChartScrollBar.SCROLL_INCREMENT_PAGE);
                adjustRangeAxis();
                break;
            case KeyEvent.VK_PAGE_DOWN:
                scrollBarX.incrementScroll(false, ChartScrollBar.SCROLL_INCREMENT_PAGE);
                adjustRangeAxis();
                break;
            default:
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // do nothing
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // do nothing
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        // scroll X
        if (domainAxis.isAutoRange()) {
            return;
        }

        if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
            // System.out.println("WHEEL_UNIT_SCROLL");
            if (e.getWheelRotation() > 0) {
                scrollBarX.incrementScroll(true, ChartScrollBar.SCROLL_INCREMENT_LARGE);
                adjustRangeAxis();
            } else if (e.getWheelRotation() < 0) {
                scrollBarX.incrementScroll(false, ChartScrollBar.SCROLL_INCREMENT_LARGE);
                adjustRangeAxis();
            }

        } else if (e.getScrollType() == MouseWheelEvent.WHEEL_BLOCK_SCROLL) {
            // System.out.println("WHEEL_BLOCK_SCROLL");
            if (e.getWheelRotation() > 0) {
                scrollBarX.incrementScroll(true, ChartScrollBar.SCROLL_INCREMENT_PAGE);
            } else if (e.getWheelRotation() < 0) {
                scrollBarX.incrementScroll(false, ChartScrollBar.SCROLL_INCREMENT_PAGE);
            }
        }
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    /**
     * Adjust Range for scroll
     */
    private void adjustRangeAxis() {
        if (!autoScrollRange) {
            return;
        }
        for (Integer axisIndex : rangeAxisMap.keySet()) {
            adjustRangeAxis(axisIndex);
        }
    }

    private void adjustRangeAxis(Integer axisIndex) {
        // if fixed length range axis, return
        if (rangeLengthList.get(axisIndex) == ChartSetting.RANGE_LENGTH_FIXED) {
            return;
        }
        
        List<List<Double>> domainValuesList = domainValuesListMap.get(axisIndex);
        List<List<Double>> rangeValuesList = rangeValuesListMap.get(axisIndex);
        Range domainRange = domainAxis.getRange();
        double rangeMin = Double.MAX_VALUE;
        double rangeMax = Double.MIN_VALUE;

        for (int i = 0; i < domainValuesList.size(); i++) {
            List<Double> domainList = domainValuesList.get(i);
            List<Double> rangeList = rangeValuesList.get(i);

            // search domain indices
            int lowerIndex = Collections.binarySearch(domainList, domainRange.getLowerBound());
            int upperIndex = Collections.binarySearch(domainList, domainRange.getUpperBound());
            lowerIndex = lowerIndex > 0 ? lowerIndex : ~lowerIndex;
            upperIndex = upperIndex > 0 ? upperIndex : ~upperIndex;
            if (lowerIndex == upperIndex) {
                // No point in this range
                continue;
            }
            double rangeMinTemp = Collections.min(rangeList.subList(lowerIndex, upperIndex));
            double rangeMaxTemp = Collections.max(rangeList.subList(lowerIndex, upperIndex));
            
            // update max/min
            rangeMin = Math.min(rangeMin, rangeMinTemp);
            rangeMax = Math.max(rangeMax, rangeMaxTemp);
        }
        // if no data point in this range, return
        if (rangeMin == Double.MAX_VALUE || rangeMax == Double.MIN_VALUE) {
            return;
        }
        
        // Margin
        double margin = (rangeMax - rangeMin) * 0.03;
        rangeMin = rangeMin - margin;
        rangeMax = rangeMax + margin;
        
        // minimum length
        double minLength = rangeLengthList.get(axisIndex);
        if (rangeMax - rangeMin < minLength) {
            double additional = (minLength - (rangeMax - rangeMin)) / 2;
            rangeMin = rangeMin - additional;
            rangeMax = rangeMax + additional;
        }

        // if it has positive range, set new range
        if (Double.compare(rangeMin, rangeMax) < 0) {
            rangeAxisMap.get(axisIndex).setRange(rangeMin, rangeMax);
        }
    }
 
}
