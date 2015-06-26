package studio.chart;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.KeyListener;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.commons.lang3.time.DateUtils;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.SeriesRenderingOrder;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;

import studio.chart.ChartSetting.ChartAxisSetting;
import studio.kdb.Config;
import studio.kdb.KTableModel;
import studio.ui.Studio;
import studio.ui.Util;

/**
 * Smart Chart Manager
 * @TestContext
 */
public class SmartChartManager {
    
    // //////////////////////////////////////
    // Filed
    // //////////////////////////////////////
    
    private static SmartChartManager instance;
    
    private SmartChartPanel settingPanel;
    private ChartSetting setting;
    
    private JFrame chartFrame;
    // private ChartPanel chartPanel;
    private JPanel chartPanel;
    private JFreeChart chart;
    
    private Studio studio;
    
    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////
    
    public static synchronized SmartChartManager getInstance(Studio studio) {
        if (instance == null) {
            instance = new SmartChartManager(studio);
        }
        return instance;
    }

    private SmartChartManager(Studio studio) {
        this.studio = studio;
        this.setting = new ChartSetting();
        this.settingPanel = new SmartChartPanel(this, setting);
        
        // setup each panel
        chartFrame = creatChartFrame();
    }
    
    private JFrame creatChartFrame() {
        JFrame newFrame = new JFrame("Studio for kdb+ [smart chart]");
        newFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        newFrame.setIconImage(Util.getImage(Config.imageBase2 + "chart_24.png").getImage());
        return newFrame;
    }
    
    public void showPanel() {
        settingPanel.showPanel();
    }
    
    public KTableModel getKTableModel() {
        return studio.getKTableModel();
    }
    
    // //////////////////////////////////////
    // Method - Action
    // //////////////////////////////////////
    
    /**
     * show chart using settings
     */
    public void showChart() {
        KTableModel table = studio.getKTableModel();
        if (table != null) {
            createChart(table);
        } else {
            JOptionPane.showMessageDialog(settingPanel.getFrame(), "No table for creating chart.", "ERROR_MESSAGE", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void createChart(KTableModel table) {
        // check row limit
        if (!Util.checkItemCountForGraph(table)) {
            JOptionPane.showMessageDialog(settingPanel.getFrame(), "Over max count limit = " + 
                    Config.getInstance().getGraphMaxCount() + " (row count * column count). \n" + 
                    "If you change limit, please set config graph.maxcount.", "ERROR_MESSAGE", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Create Chart
        chart = ChartDataCreator.createChart(table, setting);
        
        // Chart Setting
        if (chart != null) {
            // save size for restore
            int tmpWidth = 0;
            int tmpHeight = 0;
            if (setting.isNewFrame()) {
                chartFrame = creatChartFrame();
            } else {
                if (chartPanel != null && chartFrame.isVisible()) {
                    tmpWidth = chartPanel.getWidth();
                    tmpHeight = chartPanel.getHeight();
                }
            }
            
            // add overlay
            if (setting.isCrossHair()) {
                chartPanel = new CrosshairOverlayChartPanel(chart);
            } else {
                chartPanel = new ChartPanel(chart);
            }
            ((ChartPanel)chartPanel).setMouseZoomable(true, false);
            
            // wrap scroll bar panel
            if (setting.isScrollBar()) {
                chartPanel = new ChartScrollPanel((ChartPanel)chartPanel, setting.isScrollAdjust(), setting.getRangeLengthList());
                chartFrame.addKeyListener((KeyListener) chartPanel);
            }
            
            // update axis
            updateChart();
            if (!setting.isNewFrame() && tmpWidth > 0 && tmpHeight > 0) {
                // when use same frame, restore window size
                changeWindowSize(tmpWidth, tmpHeight);
            }
            
            // Set Frame
            chartFrame.setContentPane(chartPanel);
            chartFrame.pack();
            chartFrame.setVisible(true);
            chartFrame.requestFocus();
            chartFrame.toFront();
            chartFrame.setState(Frame.NORMAL);
        } else {
            throw new RuntimeException("No chart was created.");
        }
    }
    
    /**
     * Update Window, Label and Range
     */
    public void updateChart() {
        // theme
        setting.getTheme().setTheme(chart);

        // Window
        chart.setTitle(setting.getTitle());
        changeWindowSize(setting.getxSize(), setting.getySize());
        
        // Plot Rendering Order
        Plot plot = chart.getPlot();
        if (!setting.isReverseRendering()) {
            if (plot instanceof CombinedDomainXYPlot) {
                ((XYPlot) plot).setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
                ((XYPlot) plot).setSeriesRenderingOrder(SeriesRenderingOrder.FORWARD);
                for (Object plotObj : ((CombinedDomainXYPlot) plot).getSubplots()) {
                    ((XYPlot) plotObj).setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
                    ((XYPlot) plotObj).setSeriesRenderingOrder(SeriesRenderingOrder.FORWARD);
                }
            } else if (plot instanceof XYPlot) {
                ((XYPlot) plot).setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
                ((XYPlot) plot).setSeriesRenderingOrder(SeriesRenderingOrder.FORWARD);
                
            } else if (plot instanceof CategoryPlot) {
                ((CategoryPlot) plot).setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
            } 
        }
        
        // Domain Label, range
        ChartAxisSetting x1Setting = setting.getAxisSetting(AxisPosition.X1);
        if (plot instanceof XYPlot) {
            ValueAxis axis = chart.getXYPlot().getDomainAxis();
            double min = x1Setting.getRangeMin();
            double max = x1Setting.getRangeMax();
            double length = x1Setting.getRangeLength();
            if (!Double.isNaN(length)) {
                // use length
                if (Double.isNaN(min) && Double.isNaN(max)) {
                    // min/max both NOT set
                    min = axis.getLowerBound();
                    max = min + length;
                } else if (!Double.isNaN(min) && Double.isNaN(max)){
                    // min is set
                    max = min + length;
                } else if (Double.isNaN(min) && !Double.isNaN(max)) {
                    // max is set
                    min = max - length;
                }
            }
            setupAxis(axis, x1Setting.getLabel(), min, max, x1Setting.isIncludeZero());
        } else if (plot instanceof CategoryPlot) {
            chart.getCategoryPlot().getDomainAxis().setLabel(x1Setting.getLabel());
        }
        
        // Plot and Range
        if (plot instanceof CombinedDomainXYPlot) {
            // Multi-plot
            @SuppressWarnings("unchecked")
            List<XYPlot> plots = ((CombinedDomainXYPlot) plot).getSubplots();
            switch (plots.size()) {
                case 5:
                    XYPlot plot5 = plots.get(4);
                    ChartAxisSetting ySetting = setting.getAxisSetting(AxisPosition.Y5_LEFT);
                    setupAxis(plot5.getRangeAxis(), ySetting.getLabel(), ySetting.getRangeMin(), ySetting.getRangeMax(), ySetting.isIncludeZero());
                    setupPlot(plot5, x1Setting.getMarkerLines(), ySetting.getMarkerLines());
                    if (plot5.getRangeAxisCount() == 2) {
                        ChartAxisSetting rightSetting = setting.getAxisSetting(AxisPosition.Y5_RIGHT);
                        setupAxis(plot5.getRangeAxis(1), rightSetting.getLabel(), rightSetting.getRangeMin(), rightSetting.getRangeMax(), rightSetting.isIncludeZero());
                    }
                case 4:
                    XYPlot plot4 = plots.get(3);
                    ySetting = setting.getAxisSetting(AxisPosition.Y4_LEFT);
                    setupAxis(plot4.getRangeAxis(), ySetting.getLabel(), ySetting.getRangeMin(), ySetting.getRangeMax(), ySetting.isIncludeZero());
                    setupPlot(plot4, x1Setting.getMarkerLines(), ySetting.getMarkerLines());
                    if (plot4.getRangeAxisCount() == 2) {
                        ChartAxisSetting rightSetting = setting.getAxisSetting(AxisPosition.Y4_RIGHT);
                        setupAxis(plot4.getRangeAxis(1), rightSetting.getLabel(), rightSetting.getRangeMin(), rightSetting.getRangeMax(), rightSetting.isIncludeZero());
                    }
                case 3:
                    XYPlot plot3 = plots.get(2);
                    ySetting = setting.getAxisSetting(AxisPosition.Y3_LEFT);
                    setupAxis(plot3.getRangeAxis(), ySetting.getLabel(), ySetting.getRangeMin(), ySetting.getRangeMax(), ySetting.isIncludeZero());
                    setupPlot(plot3, x1Setting.getMarkerLines(), ySetting.getMarkerLines());
                    if (plot3.getRangeAxisCount() == 2) {
                        ChartAxisSetting rightSetting = setting.getAxisSetting(AxisPosition.Y3_RIGHT);
                        setupAxis(plot3.getRangeAxis(1), rightSetting.getLabel(), rightSetting.getRangeMin(), rightSetting.getRangeMax(), rightSetting.isIncludeZero());
                    }
                case 2:
                    XYPlot plot2 = plots.get(1);
                    ySetting = setting.getAxisSetting(AxisPosition.Y2_LEFT);
                    setupAxis(plot2.getRangeAxis(), ySetting.getLabel(), ySetting.getRangeMin(), ySetting.getRangeMax(), ySetting.isIncludeZero());
                    setupPlot(plot2, x1Setting.getMarkerLines(), ySetting.getMarkerLines());
                    if (plot2.getRangeAxisCount() == 2) {
                        ChartAxisSetting rightSetting = setting.getAxisSetting(AxisPosition.Y2_RIGHT);
                        setupAxis(plot2.getRangeAxis(1), rightSetting.getLabel(), rightSetting.getRangeMin(), rightSetting.getRangeMax(), rightSetting.isIncludeZero());
                    }
                default:
                    XYPlot plot1 = plots.get(0);
                    ySetting = setting.getAxisSetting(AxisPosition.Y1);
                    setupAxis(plot1.getRangeAxis(), ySetting.getLabel(), ySetting.getRangeMin(), ySetting.getRangeMax(), ySetting.isIncludeZero());
                    setupPlot(plot1, x1Setting.getMarkerLines(), ySetting.getMarkerLines());
                    if (plot1.getRangeAxisCount() == 2) {
                        ChartAxisSetting rightSetting = setting.getAxisSetting(AxisPosition.Y1_RIGHT);
                        setupAxis(plot1.getRangeAxis(1), rightSetting.getLabel(), rightSetting.getRangeMin(), rightSetting.getRangeMax(), rightSetting.isIncludeZero());
                    }
                    break;
            }
            
        } else if (plot instanceof XYPlot) {
            // one plot
            XYPlot xyPlot = (XYPlot) plot;
            ChartAxisSetting ySetting = setting.getAxisSetting(AxisPosition.Y1);
            setupAxis(xyPlot.getRangeAxis(), ySetting.getLabel(), ySetting.getRangeMin(), ySetting.getRangeMax(), ySetting.isIncludeZero());
            setupPlot(xyPlot, x1Setting.getMarkerLines(), ySetting.getMarkerLines());
            if (xyPlot.getRangeAxisCount() == 2) {
                ChartAxisSetting rightSetting = setting.getAxisSetting(AxisPosition.Y1_RIGHT);
                setupAxis(xyPlot.getRangeAxis(1), rightSetting.getLabel(), rightSetting.getRangeMin(), rightSetting.getRangeMax(), rightSetting.isIncludeZero());
            }

        } else if (plot instanceof CategoryPlot) {
            // TBD
        }
    }
    
    /**
     * fill all axis range using current min/max
     */
    public void fillCurrentRangeAll() {
        // clear all ranges
        if (chart == null) {
            JOptionPane.showMessageDialog(settingPanel.getFrame(), "No chart.", "ERROR_MESSAGE", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // plot
        Plot plot = chart.getPlot();
        
        // do nothing for CategoryPlot
        if (plot instanceof CategoryPlot) {
            return;
        }
        
        // x
        if (plot instanceof XYPlot) {
            settingPanel.fillRangeToField(chart.getXYPlot().getDomainAxis(), AxisPosition.X1);
        }
        
        // y
        if (plot instanceof CombinedDomainXYPlot) {
            // Multi-plot
            @SuppressWarnings("unchecked")
            List<XYPlot> plots = ((CombinedDomainXYPlot) plot).getSubplots();
            switch (plots.size()) {
                case 5:
                    XYPlot xyPlot = plots.get(4);
                    settingPanel.fillRangeToField(xyPlot.getRangeAxis(), AxisPosition.Y5_LEFT);
                    if (xyPlot.getRangeAxisCount() == 2) {
                        settingPanel.fillRangeToField(xyPlot.getRangeAxis(), AxisPosition.Y5_RIGHT);
                    }
                case 4:
                    xyPlot = plots.get(3);
                    settingPanel.fillRangeToField(xyPlot.getRangeAxis(), AxisPosition.Y4_LEFT);
                    if (xyPlot.getRangeAxisCount() == 2) {
                        settingPanel.fillRangeToField(xyPlot.getRangeAxis(), AxisPosition.Y4_RIGHT);
                    }
                case 3:
                    xyPlot = plots.get(2);
                    settingPanel.fillRangeToField(xyPlot.getRangeAxis(), AxisPosition.Y3_LEFT);
                    if (xyPlot.getRangeAxisCount() == 2) {
                        settingPanel.fillRangeToField(xyPlot.getRangeAxis(), AxisPosition.Y3_RIGHT);
                    }
                case 2:
                    xyPlot = plots.get(1);
                    settingPanel.fillRangeToField(xyPlot.getRangeAxis(), AxisPosition.Y2_LEFT);
                    if (xyPlot.getRangeAxisCount() == 2) {
                        settingPanel.fillRangeToField(xyPlot.getRangeAxis(), AxisPosition.Y2_RIGHT);
                    }
                default:
                    xyPlot = plots.get(0);
                    settingPanel.fillRangeToField(xyPlot.getRangeAxis(), AxisPosition.Y1);
                    if (xyPlot.getRangeAxisCount() == 2) {
                        settingPanel.fillRangeToField(xyPlot.getRangeAxis(), AxisPosition.Y1_RIGHT);
                    }
            }
            
        } else if (plot instanceof XYPlot) {
            // one plot
            XYPlot xyPlot = (XYPlot) plot;
            settingPanel.fillRangeToField(xyPlot.getRangeAxis(), AxisPosition.Y1);
            if (xyPlot.getRangeAxisCount() == 2) {
                settingPanel.fillRangeToField(xyPlot.getRangeAxis(), AxisPosition.Y1_RIGHT);
            }
        }        
    }

    
    
    private void changeWindowSize(int width, int height) {
        chartPanel.setPreferredSize(new Dimension(width, height));
    }
    
    private void setupAxis(ValueAxis axis, String label, double lower, double upper, boolean includeZero) {
        // label
        axis.setLabel(label);
        
        // set auto range for all at first
        axis.setAutoRange(true);
        
        // set range
        if (axis instanceof NumberAxis) {
            // NumberAxis
            if (Double.isNaN(lower) && Double.isNaN(upper)) {
                axis.setAutoRange(true);
            } else if (lower >= upper && !Double.isNaN(lower) && !Double.isNaN(upper)) {
                axis.setAutoRange(true);
            } else {
                lower = Double.isNaN(lower)? axis.getRange().getLowerBound() : lower;
                upper = Double.isNaN(upper)? axis.getRange().getUpperBound() : upper;
                axis.setRange(lower, upper);
            }
            
            // include zero
            ((NumberAxis)axis).setAutoRangeIncludesZero(includeZero);
            
        } else if (axis instanceof DateAxis) {
            // DateAxis
            if (Double.isNaN(lower) && Double.isNaN(upper)) {
                axis.setAutoRange(true);
            } else if (lower >= upper && !Double.isNaN(lower) && !Double.isNaN(upper)) {
                axis.setAutoRange(true);
            } else {
                // lower
                if (Double.isNaN(lower)) {
                    lower = axis.getRange().getLowerBound();
                } else {
                    // check day is set or not
                    Calendar cal = DateUtils.toCalendar(new Date((long) lower));
                    if (cal.get(Calendar.YEAR) == 1970) {
                        Calendar axisCal = DateUtils.toCalendar(new Date((long) axis.getRange().getLowerBound()));
                        if (cal.get(Calendar.MONTH) == 0 && cal.get(Calendar.DAY_OF_MONTH) == 1) {
                            // replace year/month/day
                            cal.set(axisCal.get(Calendar.YEAR), axisCal.get(Calendar.MONTH), axisCal.get(Calendar.DAY_OF_MONTH)); 
                        } else {
                         // replace year
                            cal.set(Calendar.YEAR, axisCal.get(Calendar.YEAR));
                        }
                        lower = cal.getTimeInMillis();
                    }
                }
                // upper
                if (Double.isNaN(upper)) {
                    upper = axis.getRange().getUpperBound();
                } else {
                    // check day is set or not
                    Calendar cal = DateUtils.toCalendar(new Date((long) upper));
                    if (cal.get(Calendar.YEAR) == 1970) {
                        Calendar axisCal = DateUtils.toCalendar(new Date((long) axis.getRange().getUpperBound()));
                        if (cal.get(Calendar.MONTH) == 0 && cal.get(Calendar.DAY_OF_MONTH) == 1) {
                            // replace year/month/day
                            cal.set(axisCal.get(Calendar.YEAR), axisCal.get(Calendar.MONTH), axisCal.get(Calendar.DAY_OF_MONTH)); 
                        } else {
                         // replace year
                            cal.set(Calendar.YEAR, axisCal.get(Calendar.YEAR));
                        }
                        upper = cal.getTimeInMillis();
                    }
                }
                axis.setRange(lower, upper);
            }
            
        } else {
            return;
        }
    }
    
    private void setupPlot(Plot plot, List<Double> markerLinesDomain, List<Double> markerLinesRange) {
        // add marker line
        if (plot instanceof XYPlot) {
            XYPlot xyPlot = (XYPlot) plot;
            if (!markerLinesDomain.isEmpty()) {
                for (Double value : markerLinesDomain) {
                    Marker marker = new ValueMarker(value);
                    // marker.setLabelOffsetType(LengthAdjustmentType.EXPAND);
                    marker.setPaint(Color.BLACK);
                    xyPlot.addDomainMarker(marker);
                }
            }
            if (!markerLinesRange.isEmpty()) {
                for (Double value : markerLinesRange) {
                    Marker marker = new ValueMarker(value);
                    // marker.setLabelOffsetType(LengthAdjustmentType.EXPAND);
                    marker.setPaint(Color.BLACK);
                    xyPlot.addRangeMarker(marker);
                }
            }
        }
    }
    
    
}
