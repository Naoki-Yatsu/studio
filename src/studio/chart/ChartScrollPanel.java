package studio.chart;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollBar;

import org.apache.commons.lang3.math.NumberUtils;
import org.jfree.chart.ChartPanel;
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
    
    /** chart panel */
    private ChartPanel chartPanel;
    private XYPlot plot;
    
    private boolean autoScrollRange;
    private double minAutoRange = 0.0;
    
    /** scroll bar*/
    private ChartScrollBar scrollBarX;
    private ChartScrollBar scrollBarY;
    
    /** values list */
    private List<Double> domainList = new ArrayList<>();
    private List<Double> rangeMinList = new ArrayList<>();
    private List<Double> rangeMaxList = new ArrayList<>();
    
    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////
    
    @SuppressWarnings("unchecked")
    public ChartScrollPanel(ChartPanel chartPanel, boolean autoScrollRange, String minAutoRangeStr) {
        this.chartPanel = chartPanel;
        this.autoScrollRange = autoScrollRange;
        if (NumberUtils.isNumber(minAutoRangeStr)) {
            this.minAutoRange = Double.valueOf(minAutoRangeStr);
        }
        
        setLayout(new BorderLayout());
        add(chartPanel, BorderLayout.CENTER);
        addMouseWheelListener(this);
        
        // plot
        this.plot = chartPanel.getChart().getXYPlot();
        if (chartPanel.getChart().getXYPlot() instanceof CombinedDomainXYPlot) {
            List<XYPlot> plots = ((CombinedDomainXYPlot) plot).getSubplots();
            this.plot = plots.get(0);
        }
        
        // scroll X
        this.scrollBarX = new ChartScrollBar(JScrollBar.HORIZONTAL, plot);
        add(scrollBarX, BorderLayout.SOUTH);
        
        // scroll Y 
        this.scrollBarY = new ChartScrollBar(JScrollBar.VERTICAL, plot);
        add(scrollBarY, BorderLayout.EAST);
        
        setupViewRangeForRangeAxis();
    }

    /**
     * setup value list for auto scroll
     */
    public void setupViewRangeForRangeAxis() {
        if (!autoScrollRange) {
            return;
        }
        
        XYDataset dataset = plot.getDataset();
        int series = dataset.getSeriesCount();

        if (dataset instanceof OHLCSeriesCollection) {
            // For OHLC
            OHLCSeriesCollection ohlcDataset = (OHLCSeriesCollection) dataset;
            for (int itemIndex = 0; itemIndex < ohlcDataset.getItemCount(0); itemIndex++) {
                rangeMinList.add(ohlcDataset.getLowValue(0, itemIndex));
                rangeMaxList.add(ohlcDataset.getHighValue(0, itemIndex));
                domainList.add(dataset.getXValue(0, itemIndex));
            }
            
        } else {
            for (int itemIndex = 0; itemIndex < dataset.getItemCount(0); itemIndex++) {
                double minValue = dataset.getYValue(0, itemIndex);
                double maxValue = dataset.getYValue(0, itemIndex);
                // min/max in series
                for (int seriesIndex = 1; seriesIndex < series; seriesIndex++) {
                    // some series have less count.
                    if (itemIndex >= dataset.getItemCount(seriesIndex)) {
                        continue;
                    }
                    minValue = Math.min(minValue, dataset.getYValue(seriesIndex, itemIndex));
                    maxValue = Math.max(maxValue, dataset.getYValue(seriesIndex, itemIndex));
                }
                rangeMinList.add(minValue);
                rangeMaxList.add(maxValue);
                
                domainList.add(dataset.getXValue(0, itemIndex));
            }
        }
    }
        
    
    // //////////////////////////////////////
    // Method (Listener)
    // //////////////////////////////////////

    @Override
    public void keyPressed(KeyEvent e) {
        // System.out.println("### keyPressed");
        if (plot.getDomainAxis().isAutoRange()) {
            return;
        }

        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                scrollBarY.incrementScroll(false, true);
                break;
            case KeyEvent.VK_DOWN:
                scrollBarY.incrementScroll(true, true);
                break;
            case KeyEvent.VK_LEFT:
                scrollBarX.incrementScroll(false, true);
                adjustRangeAxis();
                break;
            case KeyEvent.VK_RIGHT:
                scrollBarX.incrementScroll(true, true);
                adjustRangeAxis();
                break;
            case KeyEvent.VK_PAGE_UP:
                scrollBarX.incrementScroll(true, false);
                adjustRangeAxis();
                break;
            case KeyEvent.VK_PAGE_DOWN:
                scrollBarX.incrementScroll(false, false);
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
        if (plot.getDomainAxis().isAutoRange()) {
            return;
        }
        
        if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
            // System.out.println("WHEEL_UNIT_SCROLL");
            if (e.getWheelRotation() > 0) {
                scrollBarX.incrementScroll(true, true);
                adjustRangeAxis();
            } else if (e.getWheelRotation() < 0)  {
                scrollBarX.incrementScroll(false, true);
                adjustRangeAxis();
            }

        } else if (e.getScrollType() == MouseWheelEvent.WHEEL_BLOCK_SCROLL) {
            // System.out.println("WHEEL_BLOCK_SCROLL");
            if (e.getWheelRotation() > 0) {
                scrollBarX.incrementScroll(true, false);
            } else if (e.getWheelRotation() < 0)  {
                scrollBarX.incrementScroll(false, false);
            }
        }
    }
    
    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    public ChartPanel getChartPanel() {
        return chartPanel;
    }
    
    public void adjustRangeAxis() {
        if (!autoScrollRange) {
            return;
        }
        
        // search domain indices
        int lowerIndex = 0;
        int upperIndex = 0;
        Range domainRange = plot.getDomainAxis().getRange();
        
        // lower
        for (int i = 0; i < domainList.size(); i++) {
            if (domainList.get(i) > domainRange.getLowerBound()) {
                break;
            }
            lowerIndex = i;
        }
        // upper
        for (int i = lowerIndex; i < domainList.size(); i++) {
            upperIndex = i;
            if (domainList.get(i) > domainRange.getUpperBound()) {
                break;
            }
        }
        
        // new range for range axis
        double rangeMin = rangeMinList.subList(lowerIndex, upperIndex + 1).stream().mapToDouble(d -> d).min().getAsDouble();
        double rangeMax = rangeMaxList.subList(lowerIndex, upperIndex + 1).stream().mapToDouble(d -> d).max().getAsDouble();
        double margin = (rangeMax - rangeMin) * 0.05;
        
        rangeMin = rangeMin - margin;
        rangeMax = rangeMax + margin;
        
        if (rangeMax - rangeMin < minAutoRange) {
            double additional = (minAutoRange - (rangeMax - rangeMin)) / 2;
            rangeMin = rangeMin - additional;
            rangeMax = rangeMax + additional;
        }
        
        scrollBarY.adjustRange(rangeMin - margin, rangeMax + margin);
    }
    
}
