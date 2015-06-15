package studio.chart;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
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
import org.jfree.data.Range;

import studio.chart.AddtionalAxisPanel.AddtionalAxisPanelDefault;
import studio.chart.AddtionalAxisPanel.AddtionalAxisPanelY1Left;
import studio.chart.ChartSetting.ChartAxisSetting;
import studio.kdb.Config;
import studio.kdb.KTableModel;
import studio.ui.Studio;
import studio.ui.Util;

public class SmartChartManager {
    
    // //////////////////////////////////////
    // Filed (final)
    // //////////////////////////////////////

    protected static final int PANEL_WIDTH;
    protected static final int PANEL_HEIGHT_BASE;
    protected static final int PANEL_HEIGHT_PER_ITEM = 52;
    protected static final int PANEL_HEIGHT_PLUS_Y1LEFT = 20;
    
    protected static final int TEXT_FIELD_COLUMNS_NORMAL = 6;
    protected static final int TEXT_FIELD_COLUMNS_LONG = 8;
    protected static final int TEXT_FIELD_COLUMNS_LONGLONG = 10;
    protected static final int TEXT_FIELD_COLUMNS_SHORT = 4;
    protected static final String DEFAULT_COLUMN_NAME = "(Separator)";
    
    protected static final Color CLOSE_PANEL_COLOR = Color.LIGHT_GRAY;
    protected static final Color COLOR1 = new Color(255, 207, 207);
    protected static final Color COLOR2 = new Color(255, 255, 167);
    protected static final Color COLOR3 = new Color(192, 255, 192);
    protected static final Color COLOR4 = new Color(192, 255, 255);
    protected static final Color COLOR5 = new Color(207, 207, 255);
    
    // set window size by OS
    static {
        int panelWidthBase = 740;
        int panelHeightBase = 240;
        if(System.getProperty("os.name","").contains("OS X")){ 
            PANEL_WIDTH = panelWidthBase + 120;
            PANEL_HEIGHT_BASE = panelHeightBase + 40;
        } else {
            PANEL_WIDTH = panelWidthBase;
            PANEL_HEIGHT_BASE = panelHeightBase;
        }
    }
    
    // //////////////////////////////////////
    // Filed
    // //////////////////////////////////////
    
    private static SmartChartManager instance;
    
    private JFrame frame = new JFrame("Smart Chart Console");
    private JPanel settingPanel = new JPanel();
    
    private JFrame chartFrame;
    // private ChartPanel chartPanel;
    private JPanel chartPanel;
    private JFreeChart chart;
    
    private Studio studio;
    
    private ChartSetting setting = new ChartSetting();

    //
    // Component Items
    //
    
    // Panel
    private JPanel windowPanel = new JPanel();
    private JPanel windowPanelInner1 = new JPanel();
    private JPanel windowPanelInner2 = new JPanel();
    private JPanel chartButtonPanel = new JPanel();
    private JPanel yOpenCloseWrapperPanel = new JPanel();    
    private JPanel yOpenClosePanel = new JPanel();
    private JPanel multiAxisPanel = new JPanel();
    private JPanel clearButtonPanel = new JPanel();
    
    // Button
    private JButton chartButton = new JButton("Create Chart");
    private JButton updateButton = new JButton("Update Chart");
    private JButton clearButton = new JButton("Clear Settings");
    private JButton columnNameButton = new JButton("Update Separator");
    private JButton fillRangeButton = new JButton("Fill Range");
    private JButton clearRangeButton = new JButton("Clear Range");
    private JTextField gapField = new GuideTextField("Multi-Gap(-5.0)", TEXT_FIELD_COLUMNS_LONG);
    private JCheckBox separateLegendCheckBox = new JCheckBox("Sep.Leg.", ChartSetting.SEPARETE_LEGEND_DEFAULT);
    
    // Window
    private JTextField titleField = new GuideTextField("Title", TEXT_FIELD_COLUMNS_LONG);
    private JTextField xSizeField = new GuideTextField("X size", String.valueOf(ChartSetting.WINDOW_X_DEFAULT), TEXT_FIELD_COLUMNS_NORMAL);
    private JTextField ySizeField = new GuideTextField("Y size", String.valueOf(ChartSetting.WINDOW_Y_DEFAULT), TEXT_FIELD_COLUMNS_NORMAL);
    private JComboBox<ChartTheme> themeCombo = new JComboBox<>(ChartTheme.values());
    private JCheckBox newChartFrameCheckBox = new JCheckBox("New Frame", ChartSetting.NEW_FRAME_DEFAULT);
    private JCheckBox reverseRenderingOrderCheckBox = new JCheckBox("Rev-Rendering", ChartSetting.REVERSE_RENDERING_DEFAULT);
    private JCheckBox crosshairOverlayCheckBox = new JCheckBox("Cross-hair", ChartSetting.CROSS_HAIR_DEFAULT);
    private JCheckBox scrollBarCheckBox = new JCheckBox("Scroll", ChartSetting.SCROLL_BAR_DEFAULT);
    private JCheckBox scrollAdjustRangeCheckBox = new JCheckBox("Scroll Adjust", ChartSetting.SCROLL_ADJUST_DEFAULT);
    private JTextField scrollMinRangeField = new GuideTextField("Min Range", TEXT_FIELD_COLUMNS_NORMAL);
    
    // Open/Close label
    private JLabel y1Left2Label = new JLabel(" + Y1 Left");
    private JLabel y1RightLabel = new JLabel(" + Y1 Right");
    private JLabel y2LeftLabel = new JLabel(" + Y2 Left");
    private JLabel y2RithtLabel = new JLabel(" + Y2 Right");
    private JLabel y3LeftLabel = new JLabel(" + Y3 Left");
    private JLabel y3RithtLabel = new JLabel(" + Y3 Right");
    private JLabel y4LeftLabel = new JLabel(" + Y4 Left");
    private JLabel y4RithtLabel = new JLabel(" + Y4 Right");
    private JLabel y5LeftLabel = new JLabel(" + Y5 Left");
    private JLabel y5RithtLabel = new JLabel(" + Y5 Right");
    
    // Additional Axis Panel
    private Map<AxisPosition, AddtionalAxisPanel> axisPanelMap = new EnumMap<>(AxisPosition.class);
    private AddtionalAxisPanelDefault xPanel = new AddtionalAxisPanelDefault("X", AxisPosition.X1, null);
    private AddtionalAxisPanelDefault y1Panel = new AddtionalAxisPanelDefault("Y1", AxisPosition.Y1, null);
    private AddtionalAxisPanelY1Left y1LeftPanel = new AddtionalAxisPanelY1Left("Y1 Left", AxisPosition.Y1_LEFT1, COLOR1);
    private AddtionalAxisPanelDefault y1RightPanel = new AddtionalAxisPanelDefault("Y1 Right", AxisPosition.Y1_RIGHT, COLOR1);
    private AddtionalAxisPanelDefault y2LeftPanel = new AddtionalAxisPanelDefault("Y2 Left", AxisPosition.Y2_LEFT, COLOR2);
    private AddtionalAxisPanelDefault y2RightPanel = new AddtionalAxisPanelDefault("Y2 Right", AxisPosition.Y2_RIGHT, COLOR2);
    private AddtionalAxisPanelDefault y3LeftPanel = new AddtionalAxisPanelDefault("Y3 Left", AxisPosition.Y3_LEFT, COLOR3);
    private AddtionalAxisPanelDefault y3RightPanel = new AddtionalAxisPanelDefault("Y3 Right", AxisPosition.Y3_RIGHT, COLOR3);
    private AddtionalAxisPanelDefault y4LeftPanel = new AddtionalAxisPanelDefault("Y4 Left", AxisPosition.Y4_LEFT, COLOR4);
    private AddtionalAxisPanelDefault y4RightPanel = new AddtionalAxisPanelDefault("Y4 Right", AxisPosition.Y4_RIGHT, COLOR4);
    private AddtionalAxisPanelDefault y5LeftPanel = new AddtionalAxisPanelDefault("Y5 Left", AxisPosition.Y5_LEFT, COLOR5);
    private AddtionalAxisPanelDefault y5RightPanel = new AddtionalAxisPanelDefault("Y5 Right", AxisPosition.Y5_RIGHT, COLOR5);

    
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
        
        // setup each panel
        setupSettingPanel();
        intOpenCloseLabels();
        chartFrame = creatChartFrame();

        // frame setting
        frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.PAGE_AXIS));
        frame.getContentPane().add(settingPanel);
        frame.pack();
        // frame.setVisible(true);
        // frame.requestFocus();
        // frame.toFront();
    }
    
    public void showPanel() {
        frame.setVisible(true);
        frame.requestFocus();
        frame.toFront();
        frame.setState(Frame.NORMAL);
    }
    
    private JFrame creatChartFrame() {
        JFrame newFrame = new JFrame("Studio for kdb+ [smart chart]");
        newFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        newFrame.setIconImage(Util.getImage(Config.imageBase2 + "chart_24.png").getImage());
        return newFrame;
    }
    
    // //////////////////////////////////////
    // Method - Button Action
    // //////////////////////////////////////
    
    /**
     * show chart using settings
     */
    private void showChart() {
        copySettings();
        KTableModel table = studio.getKTableModel();
        if (table != null) {
            createChart(table, newChartFrameCheckBox.isSelected());
        } else {
            JOptionPane.showMessageDialog(frame, "No table for creating chart.", "ERROR_MESSAGE", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void createChart(KTableModel table, boolean newFrame) {
        // check row limit
        if (!Util.checkItemCountForGraph(table)) {
            JOptionPane.showMessageDialog(frame, "Over max count limit = " + 
                    Config.getInstance().getGraphMaxCount() + " (row count * column count). \n" + 
                    "If you change limit, please set config graph.maxcount.", "ERROR_MESSAGE", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        chart = ChartDataCreator.createChart(table, setting);
        if (chart != null) {
            int tmpWidth = 0;
            int tmpHeight = 0;
            if (newFrame) {
                chartFrame = creatChartFrame();
            } else {
                if (chartPanel != null) {
                    tmpWidth = chartPanel.getWidth();
                    tmpHeight = chartPanel.getHeight();
                }
            }
            
            // add overlay
            if (crosshairOverlayCheckBox.isSelected()) {
                chartPanel = new CrosshairOverlayChartPanel(chart);
            } else {
                chartPanel = new ChartPanel(chart);
            }
            ((ChartPanel)chartPanel).setMouseZoomable(true, false);
            
            // wrap scroll bar panel
            if (scrollBarCheckBox.isSelected()) {
                chartPanel = new ChartScrollPanel((ChartPanel)chartPanel, scrollAdjustRangeCheckBox.isSelected(), scrollMinRangeField.getText());
                chartFrame.addKeyListener((KeyListener) chartPanel);
            }
            updateChart();
            if (!newFrame && tmpWidth > 0 && tmpHeight > 0) {
                // when use same frame, restore window size
                changeWindowSize(tmpWidth, tmpHeight);
            }
            
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
    private void updateChart() {
        copySettings();

        // theme
        setting.getTheme().setTheme(chart);

        // Window
        if (!StringUtils.isBlank(setting.getTitle())) {
            chart.setTitle(setting.getTitle());
        }
        changeWindowSize(setting.getxSize(), setting.getySize());
        
        // Plot Rendering Order
        Plot plot = chart.getPlot();
        if (!reverseRenderingOrderCheckBox.isSelected()) {
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
            setupRangeAxis(chart.getXYPlot().getDomainAxis(), x1Setting.getLabel(), x1Setting.getRangeMin(), x1Setting.getRangeMax(), x1Setting.isIncludeZero());
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
                    setupRangeAxis(plot5.getRangeAxis(), ySetting.getLabel(), ySetting.getRangeMin(), ySetting.getRangeMax(), ySetting.isIncludeZero());
                    setupPlot(plot5, x1Setting.getMarkerLines(), ySetting.getMarkerLines());
                    if (plot5.getRangeAxisCount() == 2) {
                        ChartAxisSetting rightSetting = setting.getAxisSetting(AxisPosition.Y5_RIGHT);
                        setupRangeAxis(plot5.getRangeAxis(1), rightSetting.getLabel(), rightSetting.getRangeMin(), rightSetting.getRangeMax(), rightSetting.isIncludeZero());
                    }
                case 4:
                    XYPlot plot4 = plots.get(3);
                    ySetting = setting.getAxisSetting(AxisPosition.Y4_LEFT);
                    setupRangeAxis(plot4.getRangeAxis(), ySetting.getLabel(), ySetting.getRangeMin(), ySetting.getRangeMax(), ySetting.isIncludeZero());
                    setupPlot(plot4, x1Setting.getMarkerLines(), ySetting.getMarkerLines());
                    if (plot4.getRangeAxisCount() == 2) {
                        ChartAxisSetting rightSetting = setting.getAxisSetting(AxisPosition.Y4_RIGHT);
                        setupRangeAxis(plot4.getRangeAxis(1), rightSetting.getLabel(), rightSetting.getRangeMin(), rightSetting.getRangeMax(), rightSetting.isIncludeZero());
                    }
                case 3:
                    XYPlot plot3 = plots.get(2);
                    ySetting = setting.getAxisSetting(AxisPosition.Y3_LEFT);
                    setupRangeAxis(plot3.getRangeAxis(), ySetting.getLabel(), ySetting.getRangeMin(), ySetting.getRangeMax(), ySetting.isIncludeZero());
                    setupPlot(plot3, x1Setting.getMarkerLines(), ySetting.getMarkerLines());
                    if (plot3.getRangeAxisCount() == 2) {
                        ChartAxisSetting rightSetting = setting.getAxisSetting(AxisPosition.Y3_RIGHT);
                        setupRangeAxis(plot3.getRangeAxis(1), rightSetting.getLabel(), rightSetting.getRangeMin(), rightSetting.getRangeMax(), rightSetting.isIncludeZero());
                    }
                case 2:
                    XYPlot plot2 = plots.get(1);
                    ySetting = setting.getAxisSetting(AxisPosition.Y2_LEFT);
                    setupRangeAxis(plot2.getRangeAxis(), ySetting.getLabel(), ySetting.getRangeMin(), ySetting.getRangeMax(), ySetting.isIncludeZero());
                    setupPlot(plot2, x1Setting.getMarkerLines(), ySetting.getMarkerLines());
                    if (plot2.getRangeAxisCount() == 2) {
                        ChartAxisSetting rightSetting = setting.getAxisSetting(AxisPosition.Y2_RIGHT);
                        setupRangeAxis(plot2.getRangeAxis(1), rightSetting.getLabel(), rightSetting.getRangeMin(), rightSetting.getRangeMax(), rightSetting.isIncludeZero());
                    }
                default:
                    XYPlot plot1 = plots.get(0);
                    ySetting = setting.getAxisSetting(AxisPosition.Y1);
                    setupRangeAxis(plot1.getRangeAxis(), ySetting.getLabel(), ySetting.getRangeMin(), ySetting.getRangeMax(), ySetting.isIncludeZero());
                    setupPlot(plot1, x1Setting.getMarkerLines(), ySetting.getMarkerLines());
                    if (plot1.getRangeAxisCount() == 2) {
                        ChartAxisSetting rightSetting = setting.getAxisSetting(AxisPosition.Y1_RIGHT);
                        setupRangeAxis(plot1.getRangeAxis(1), rightSetting.getLabel(), rightSetting.getRangeMin(), rightSetting.getRangeMax(), rightSetting.isIncludeZero());
                    }
                    break;
            }
            
        } else if (plot instanceof XYPlot) {
            // one plot
            XYPlot xyPlot = (XYPlot) plot;
            ChartAxisSetting ySetting = setting.getAxisSetting(AxisPosition.Y1);
            setupRangeAxis(xyPlot.getRangeAxis(), ySetting.getLabel(), ySetting.getRangeMin(), ySetting.getRangeMax(), ySetting.isIncludeZero());
            setupPlot(xyPlot, x1Setting.getMarkerLines(), ySetting.getMarkerLines());
            if (xyPlot.getRangeAxisCount() == 2) {
                ChartAxisSetting rightSetting = setting.getAxisSetting(AxisPosition.Y1_RIGHT);
                setupRangeAxis(xyPlot.getRangeAxis(1), rightSetting.getLabel(), rightSetting.getRangeMin(), rightSetting.getRangeMax(), rightSetting.isIncludeZero());
            }

        } else if (plot instanceof CategoryPlot) {
            // TBD
        }
    }
    
    private void changeWindowSize(int width, int height) {
        chartPanel.setPreferredSize(new Dimension(width, height));
    }
    
    private void setupRangeAxis(ValueAxis axis, String label, double lower, double upper, boolean includeZero) {
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
    
    /**
     * Copy all settings to ChartSetting instance
     */
    private void copySettings() {
        // Window
        setting.setTitle(titleField.getText());
        setting.setxSize(evalWidnowSize(xSizeField.getText(), true));
        setting.setySize(evalWidnowSize(ySizeField.getText(), false));
        setting.setTheme((ChartTheme) themeCombo.getSelectedItem());
        setting.setCombinedGap(evalDoubleField(gapField, ChartSetting.GAP_DEFAULT));
        setting.setSeparateLegend(separateLegendCheckBox.isSelected());

        // Axis Items
        // It contains NOT used items
        for (Entry<AxisPosition, AddtionalAxisPanel> entry : axisPanelMap.entrySet()) {
            if (entry.getValue() instanceof AddtionalAxisPanelDefault) {
                AddtionalAxisPanelDefault panel = (AddtionalAxisPanelDefault) entry.getValue();

                ChartAxisSetting axisSetting = setting.getAxisSetting(entry.getKey());
                axisSetting.setLabel(panel.labelField.getText());
                axisSetting.setRangeMin(evalDoubleField(panel.minField));
                axisSetting.setRangeMax(evalDoubleField(panel.maxField));
                axisSetting.setIncludeZero(panel.includeZeroCheckbox.isSelected());
                axisSetting.setMarkerLines(evalMarkerLine(panel.markerLineField.getText()));
                axisSetting.setChartType((ChartType) panel.chartCombo.getSelectedItem());
                axisSetting.setColumnName(getColumnNameValue(panel.columnNameCombo));
                axisSetting.setWeight(evalWeihgt(panel.weightField.getText()));
                
            } else if (entry.getValue() instanceof AddtionalAxisPanelY1Left) {
                // Y1 Left 1-4
                AddtionalAxisPanelY1Left panel = (AddtionalAxisPanelY1Left) entry.getValue();

                ChartAxisSetting axisSetting1 = setting.getAxisSetting(AxisPosition.Y1_LEFT1);
                axisSetting1.setChartType((ChartType) panel.chartCombo1.getSelectedItem());
                axisSetting1.setColumnName(getColumnNameValue(panel.columnNameCombo1));

                ChartAxisSetting axisSetting2 = setting.getAxisSetting(AxisPosition.Y1_LEFT2);
                axisSetting2.setChartType((ChartType) panel.chartCombo2.getSelectedItem());
                axisSetting2.setColumnName(getColumnNameValue(panel.columnNameCombo2));

                ChartAxisSetting axisSetting3 = setting.getAxisSetting(AxisPosition.Y1_LEFT3);
                axisSetting3.setChartType((ChartType) panel.chartCombo3.getSelectedItem());
                axisSetting3.setColumnName(getColumnNameValue(panel.columnNameCombo3));

                ChartAxisSetting axisSetting4 = setting.getAxisSetting(AxisPosition.Y1_LEFT4);
                axisSetting4.setChartType((ChartType) panel.chartCombo4.getSelectedItem());
                axisSetting4.setColumnName(getColumnNameValue(panel.columnNameCombo4));
            }
        }
    }

    private double evalDoubleField(JTextField field) {
        if (NumberUtils.isNumber(field.getText())) {
            return Double.parseDouble(field.getText());
        } else {
            // For date string parse
            Date date = DateUtility.parseDate(field.getText());
            if (date != null) {
                return date.getTime();
            } else {
                return Double.NaN; 
            }
        }
    }
    
    private double evalDoubleField(JTextField field, double defaultValue) {
        if (NumberUtils.isNumber(field.getText())) {
            return Double.parseDouble(field.getText());
        } else {
            return defaultValue;
        }
    }
    
    private int evalWidnowSize(String windowSizeStr, boolean isSizeX) {
        if (StringUtils.isNumeric(windowSizeStr)) {
            int size = Integer.parseInt(windowSizeStr);
            if (size > 50 && size < 2000) {
                return size;
            }
        }
        // blank
        if (StringUtils.isBlank(windowSizeStr)) {
            if (isSizeX) {
                return ChartSetting.WINDOW_X_DEFAULT;
            } else {
                return ChartSetting.WINDOW_Y_DEFAULT;
            }
        }
        // invalid size
        JOptionPane.showMessageDialog(frame, "Window size must be between 50 and 2000.", "ERROR_MESSAGE", JOptionPane.ERROR_MESSAGE);
        throw new RuntimeException();
    }
    
    private List<Double> evalMarkerLine(String markerLineStr) {
        if (StringUtils.isBlank(markerLineStr)) {
            return Collections.emptyList();
        }
        
        List<Double> markerLines = new ArrayList<>();
        for (String value : markerLineStr.split(",")) {
            if (NumberUtils.isNumber(value.trim())) {
                markerLines.add(Double.valueOf(value.trim()));
            }
        }
        return markerLines;
    }
    
    /**
     * evaluate weight.
     * weight must be between 0.1 ~ 10.0
     * 
     * @param weightStr
     * @return
     */
    private double evalWeihgt(String weightStr) {
        if (NumberUtils.isNumber(weightStr)) {
            double weight = Double.parseDouble(weightStr);
            if (weight >= 0.1 && weight <= 10.0) {
                return weight;
            }
        }
        // invalid weight
        JOptionPane.showMessageDialog(frame, "Weight must be between 0.1 and 10.", "ERROR_MESSAGE", JOptionPane.ERROR_MESSAGE);
        throw new RuntimeException();
    }
    
    private String getColumnNameValue(JComboBox<String> columnNameCombo) {
        String columnName = (String) columnNameCombo.getSelectedItem();
        if (DEFAULT_COLUMN_NAME.equals(columnName)) {
            return "";
        } 
        
        // check existence of column
        KTableModel table = studio.getKTableModel();
        if (table == null) {
            return "";
        }
        for (int i = 2; i < table.getColumnCount(); i++) {
            if (table.getColumnName(i).equals(columnName)) {
                return columnName;
            }
        }
        return "";
    }
    
    /**
     * clear all settings
     */
    private void clearSetting() {
        // window
        ((GuideTextField) titleField).clearText("");
        ((GuideTextField) xSizeField).clearText(String.valueOf(ChartSetting.WINDOW_X_DEFAULT));
        ((GuideTextField) ySizeField).clearText(String.valueOf(ChartSetting.WINDOW_Y_DEFAULT));
        themeCombo.setSelectedIndex(0);
        newChartFrameCheckBox.setSelected(ChartSetting.NEW_FRAME_DEFAULT);
        reverseRenderingOrderCheckBox.setSelected(ChartSetting.REVERSE_RENDERING_DEFAULT);
        crosshairOverlayCheckBox.setSelected(ChartSetting.CROSS_HAIR_DEFAULT);
        scrollBarCheckBox.setSelected(ChartSetting.SCROLL_BAR_DEFAULT);
        scrollAdjustRangeCheckBox.setSelected(ChartSetting.SCROLL_ADJUST_DEFAULT);
        ((GuideTextField) scrollMinRangeField).clearText("");

        // Multi
        ((GuideTextField) gapField).clearText("");
        separateLegendCheckBox.setSelected(ChartSetting.SEPARETE_LEGEND_DEFAULT);
        
        // Additional panels
        xPanel.clearSetting();
        y1Panel.clearSetting();
        y1LeftPanel.clearSetting();
        y1RightPanel.clearSetting();
        y2LeftPanel.clearSetting();
        y2RightPanel.clearSetting();
        y3LeftPanel.clearSetting();
        y3RightPanel.clearSetting();
        y4LeftPanel.clearSetting();
        y4RightPanel.clearSetting();
        y5LeftPanel.clearSetting();
        y5RightPanel.clearSetting();
        
        copySettings();
    }

    /** 
     * Set Column Name Combo from KTable
     */
    private void setColumnNameCombo() {
        KTableModel table = studio.getKTableModel();
        if (table == null) {
            return;
        }
        List<String> columnNameList = new ArrayList<>();
        columnNameList.add(DEFAULT_COLUMN_NAME);
        
        for (int i = 2; i < table.getColumnCount(); i++) {
            columnNameList.add(table.getColumnName(i));
        }
        
        // this contains unused axes (X1, Y1)
        for (AddtionalAxisPanel panel : axisPanelMap.values()) {
            panel.setupColumnNameCombo(columnNameList);
        }
        y1LeftPanel.setupColumnNameCombo(columnNameList);
    }
    
    /**
     * fill all axis range using current min/max
     */
    private void fillCurrentRangeAll() {
        // clear all ranges
        clearAllRange();
        if (chart == null) {
            JOptionPane.showMessageDialog(frame, "No chart.", "ERROR_MESSAGE", JOptionPane.ERROR_MESSAGE);
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
            fillRangeToField(chart.getXYPlot().getDomainAxis(), AxisPosition.X1);
        }
        
        // y
        if (plot instanceof CombinedDomainXYPlot) {
            // Multi-plot
            @SuppressWarnings("unchecked")
            List<XYPlot> plots = ((CombinedDomainXYPlot) plot).getSubplots();
            switch (plots.size()) {
                case 5:
                    XYPlot xyPlot = plots.get(4);
                    fillRangeToField(xyPlot.getRangeAxis(), AxisPosition.Y5_LEFT);
                    if (xyPlot.getRangeAxisCount() == 2) {
                        fillRangeToField(xyPlot.getRangeAxis(), AxisPosition.Y5_RIGHT);
                    }
                case 4:
                    xyPlot = plots.get(3);
                    fillRangeToField(xyPlot.getRangeAxis(), AxisPosition.Y4_LEFT);
                    if (xyPlot.getRangeAxisCount() == 2) {
                        fillRangeToField(xyPlot.getRangeAxis(), AxisPosition.Y4_RIGHT);
                    }
                case 3:
                    xyPlot = plots.get(2);
                    fillRangeToField(xyPlot.getRangeAxis(), AxisPosition.Y3_LEFT);
                    if (xyPlot.getRangeAxisCount() == 2) {
                        fillRangeToField(xyPlot.getRangeAxis(), AxisPosition.Y3_RIGHT);
                    }
                case 2:
                    xyPlot = plots.get(1);
                    fillRangeToField(xyPlot.getRangeAxis(), AxisPosition.Y2_LEFT);
                    if (xyPlot.getRangeAxisCount() == 2) {
                        fillRangeToField(xyPlot.getRangeAxis(), AxisPosition.Y2_RIGHT);
                    }
                default:
                    xyPlot = plots.get(0);
                    fillRangeToField(xyPlot.getRangeAxis(), AxisPosition.Y1);
                    if (xyPlot.getRangeAxisCount() == 2) {
                        fillRangeToField(xyPlot.getRangeAxis(), AxisPosition.Y1_RIGHT);
                    }
            }
            
        } else if (plot instanceof XYPlot) {
            // one plot
            XYPlot xyPlot = (XYPlot) plot;
            fillRangeToField(xyPlot.getRangeAxis(), AxisPosition.Y1);
            if (xyPlot.getRangeAxisCount() == 2) {
                fillRangeToField(xyPlot.getRangeAxis(), AxisPosition.Y1_RIGHT);
            }
        }        
    }
    
    private void fillRangeToField(ValueAxis axis, AxisPosition axisPosition) {
        if (!(axisPanelMap.get(axisPosition) instanceof AddtionalAxisPanelDefault)) {
            return;
        }
        AddtionalAxisPanelDefault panel = (AddtionalAxisPanelDefault) axisPanelMap.get(axisPosition);
        
        Range range = axis.getRange();
        if (axis instanceof DateAxis) {
            Date lowerDate = new Date((long)range.getLowerBound());
            Date upperDate = new Date((long)range.getUpperBound());
            if (DateUtility.compareDate(lowerDate, upperDate) != 0) {
                ((GuideTextField) panel.minField).clearText(DateUtility.parseString(lowerDate, true));
                ((GuideTextField) panel.maxField).clearText(DateUtility.parseString(upperDate, true));
            } else {
                // if lower and upper is the same date, don't show date.
                ((GuideTextField) panel.minField).clearText(DateUtility.parseString(lowerDate, false));
                ((GuideTextField) panel.maxField).clearText(DateUtility.parseString(upperDate, false));
            }
        } else {
            ((GuideTextField) panel.minField).clearText(Double.toString(range.getLowerBound()));
            ((GuideTextField) panel.maxField).clearText(Double.toString(range.getUpperBound()));
        }
    }
    
    private void clearAllRange() {
        // Axis Items
        for (Entry<AxisPosition, AddtionalAxisPanel> entry : axisPanelMap.entrySet()) {
            if (!(entry.getValue() instanceof AddtionalAxisPanelDefault)) {
                continue;
            }
            AddtionalAxisPanelDefault panel = (AddtionalAxisPanelDefault) entry.getValue();
            ((GuideTextField) panel.minField).clearText("");
            ((GuideTextField) panel.maxField).clearText("");
        }
    }
    
    private String getStackTraceString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }
    
    ///////////////////////////////////////////////////
    // Component Setting
    ///////////////////////////////////////////////////
    
    private void setupSettingPanel() {
        //
        // Action
        //
        chartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    showChart();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, getStackTraceString(ex), "ERROR_MESSAGE", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    updateChart();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, getStackTraceString(ex), "ERROR_MESSAGE", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    clearSetting();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, getStackTraceString(ex), "ERROR_MESSAGE", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        columnNameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    setColumnNameCombo();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, getStackTraceString(ex), "ERROR_MESSAGE", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        fillRangeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    fillCurrentRangeAll();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, getStackTraceString(ex), "ERROR_MESSAGE", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        clearRangeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    clearAllRange();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, getStackTraceString(ex), "ERROR_MESSAGE", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        //
        // Add components
        //

        // Panels
        settingPanel.add(chartButtonPanel);

        settingPanel.add(clearButtonPanel);
        settingPanel.add(windowPanel);
        settingPanel.add(xPanel);
        settingPanel.add(y1Panel);
        settingPanel.add(yOpenCloseWrapperPanel);
        settingPanel.add(y1LeftPanel);
        settingPanel.add(y1RightPanel);
        settingPanel.add(y2LeftPanel);
        settingPanel.add(y2RightPanel);
        settingPanel.add(y3LeftPanel);
        settingPanel.add(y3RightPanel);
        settingPanel.add(y4LeftPanel);
        settingPanel.add(y4RightPanel);
        settingPanel.add(y5LeftPanel);
        settingPanel.add(y5RightPanel);

        // Button
        chartButtonPanel.add(chartButton);
        chartButtonPanel.add(updateButton);
        
        clearButtonPanel.add(clearButton);
        clearButtonPanel.add(fillRangeButton);
        clearButtonPanel.add(clearRangeButton);
        
        multiAxisPanel.add(columnNameButton);
        multiAxisPanel.add(gapField);    
        multiAxisPanel.add(separateLegendCheckBox);    
        
        // window
        windowPanel.add(windowPanelInner1);
        windowPanel.add(windowPanelInner2);
        
        // window1
        windowPanelInner1.add(titleField);
        windowPanelInner1.add(xSizeField);
        windowPanelInner1.add(ySizeField);
        windowPanelInner1.add(themeCombo);
        windowPanelInner1.add(newChartFrameCheckBox);
        
        // window2
        windowPanelInner2.add(reverseRenderingOrderCheckBox);
        windowPanelInner2.add(crosshairOverlayCheckBox);
        windowPanelInner2.add(scrollBarCheckBox);
        windowPanelInner2.add(scrollAdjustRangeCheckBox);
        windowPanelInner2.add(scrollMinRangeField);
        
        // Y Open/Close
        yOpenCloseWrapperPanel.add(yOpenClosePanel);
        yOpenCloseWrapperPanel.add(multiAxisPanel);
        yOpenClosePanel.add(y1Left2Label);
        yOpenClosePanel.add(y1RightLabel);
        yOpenClosePanel.add(y2LeftLabel);
        yOpenClosePanel.add(y2RithtLabel);
        yOpenClosePanel.add(y3LeftLabel);
        yOpenClosePanel.add(y3RithtLabel);
        yOpenClosePanel.add(y4LeftLabel);
        yOpenClosePanel.add(y4RithtLabel);
        yOpenClosePanel.add(y5LeftLabel);
        yOpenClosePanel.add(y5RithtLabel);
        
        //
        // Set layout
        //
        
        // setting panel
        settingPanel.setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT_BASE + countVisiblePanel() * PANEL_HEIGHT_PER_ITEM));
        GridBagLayout layout = new GridBagLayout();
        settingPanel.setLayout(layout);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(1, 2, 1, 2);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weighty = 0.0d;
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        layout.setConstraints(windowPanel, gbc);
        gbc.gridx = 1;
        gbc.gridheight = 2;
        layout.setConstraints(chartButtonPanel, gbc);
        gbc.gridheight = 1;
        gbc.gridx = 0;
        
        gbc.gridy++;
        layout.setConstraints(xPanel, gbc);
        gbc.gridy++;
        layout.setConstraints(y1Panel, gbc);

        gbc.gridwidth = 2;
        gbc.gridy++;
        layout.setConstraints(yOpenCloseWrapperPanel, gbc);
        gbc.gridy++;
        layout.setConstraints(y1LeftPanel, gbc);
        gbc.gridy++;
        layout.setConstraints(y1RightPanel, gbc);
        gbc.gridy++;
        layout.setConstraints(y2LeftPanel, gbc);
        gbc.gridy++;
        layout.setConstraints(y2RightPanel, gbc);
        gbc.gridy++;
        layout.setConstraints(y3LeftPanel, gbc);
        gbc.gridy++;
        layout.setConstraints(y3RightPanel, gbc);
        gbc.gridy++;
        layout.setConstraints(y4LeftPanel, gbc);
        gbc.gridy++;
        layout.setConstraints(y4RightPanel, gbc);
        gbc.gridy++;
        layout.setConstraints(y5LeftPanel, gbc);
        gbc.gridy++;
        layout.setConstraints(y5RightPanel, gbc);
        gbc.gridy++;
        gbc.weighty = 1.0d;
        gbc.anchor = GridBagConstraints.NORTH;
        layout.setConstraints(clearButtonPanel, gbc);

        // button panel
        chartButtonPanel.setBorder(new TitledBorder("Chart"));
        layout = new GridBagLayout();
        chartButtonPanel.setLayout(layout);
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        layout.setConstraints(chartButton, gbc);
        gbc.gridy++;
        layout.setConstraints(updateButton, gbc);
        
        
        // Y Open/Close
        yOpenClosePanel.setBorder(new TitledBorder("Open/Close Addtional Panel"));
        layout = new GridBagLayout();
        yOpenClosePanel.setLayout(layout);
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 10, 2, 10);
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        layout.setConstraints(y1Left2Label, gbc);
        gbc.gridx++;
        layout.setConstraints(y2LeftLabel, gbc);
        gbc.gridx++;
        layout.setConstraints(y3LeftLabel, gbc);
        gbc.gridx++;
        layout.setConstraints(y4LeftLabel, gbc);
        gbc.gridx++;
        layout.setConstraints(y5LeftLabel, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        layout.setConstraints(y1RightLabel, gbc);
        gbc.gridx++;
        layout.setConstraints(y2RithtLabel, gbc);
        gbc.gridx++;
        layout.setConstraints(y3RithtLabel, gbc);
        gbc.gridx++;
        layout.setConstraints(y4RithtLabel, gbc);
        gbc.gridx++;
        layout.setConstraints(y5RithtLabel, gbc);
        
        // MultiAxis Panel
        multiAxisPanel.setBorder(new TitledBorder("Multi Axis"));
        layout = new GridBagLayout();
        multiAxisPanel.setLayout(layout);
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(2, 1, 2, 1);
        gbc.gridx = 0;
        gbc.gridy = 0;
        layout.setConstraints(gapField, gbc);
        gbc.gridx++;
        layout.setConstraints(separateLegendCheckBox, gbc);
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.gridy++;
        layout.setConstraints(columnNameButton, gbc);
        
        // clear button panel
        layout = new GridBagLayout();
        clearButtonPanel.setLayout(layout);
        clearButtonPanel.setPreferredSize(new Dimension(PANEL_WIDTH - 50, (int)clearButtonPanel.getPreferredSize().getHeight() + 10));
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 0;
        layout.setConstraints(fillRangeButton, gbc);
        gbc.gridx++;
        layout.setConstraints(clearRangeButton, gbc);
        gbc.gridx++;
        gbc.weightx = 1.0d;
        gbc.anchor = GridBagConstraints.EAST;
        layout.setConstraints(clearButton, gbc);
        
        // window panel
        windowPanel.setBorder(new TitledBorder("Window"));
        layout = new GridBagLayout();
        windowPanel.setLayout(layout);
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(1, 2, 1, 2);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        layout.setConstraints(windowPanelInner1, gbc);
        gbc.gridy++;
        layout.setConstraints(windowPanelInner2, gbc);

        // window panel 1
        layout = new GridBagLayout();
        windowPanelInner1.setLayout(layout);
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(1, 2, 1, 2);
        gbc.gridx = 0;
        gbc.gridy = 0;
        layout.setConstraints(titleField, gbc);
        gbc.gridx++;
        layout.setConstraints(xSizeField, gbc);
        gbc.gridx++;
        layout.setConstraints(ySizeField, gbc);
        gbc.gridx++;
        layout.setConstraints(themeCombo, gbc);
        gbc.gridx++;
        layout.setConstraints(newChartFrameCheckBox, gbc);
        
        // window panel 2
        layout = new GridBagLayout();
        windowPanelInner2.setLayout(layout);
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(1, 2, 1, 2);
        gbc.gridx = 0;
        gbc.gridy = 0;
        layout.setConstraints(reverseRenderingOrderCheckBox, gbc);
        gbc.gridx++;
        layout.setConstraints(crosshairOverlayCheckBox, gbc);
        gbc.gridx++;
        layout.setConstraints(scrollBarCheckBox, gbc);
        gbc.gridx++;
        layout.setConstraints(scrollAdjustRangeCheckBox, gbc);
        gbc.gridx++;
        layout.setConstraints(scrollMinRangeField, gbc);  
        
        // Map
        axisPanelMap.put(AxisPosition.X1, xPanel);
        axisPanelMap.put(AxisPosition.Y1, y1Panel);
        axisPanelMap.put(AxisPosition.Y1_LEFT1, y1LeftPanel);
        axisPanelMap.put(AxisPosition.Y1_RIGHT, y1RightPanel);
        axisPanelMap.put(AxisPosition.Y2_LEFT, y2LeftPanel);
        axisPanelMap.put(AxisPosition.Y2_RIGHT, y2RightPanel);
        axisPanelMap.put(AxisPosition.Y3_LEFT, y3LeftPanel);
        axisPanelMap.put(AxisPosition.Y3_RIGHT, y3RightPanel);
        axisPanelMap.put(AxisPosition.Y4_LEFT, y4LeftPanel);
        axisPanelMap.put(AxisPosition.Y4_RIGHT, y4RightPanel);
        axisPanelMap.put(AxisPosition.Y5_LEFT, y5LeftPanel);
        axisPanelMap.put(AxisPosition.Y5_RIGHT, y5RightPanel);
    }
    
    private void intOpenCloseLabels() {
        y1Left2Label.setOpaque(true);
        y1RightLabel.setOpaque(true);
        y2LeftLabel.setOpaque(true);
        y2RithtLabel.setOpaque(true);
        y3LeftLabel.setOpaque(true);
        y3RithtLabel.setOpaque(true);
        y4LeftLabel.setOpaque(true);
        y4RithtLabel.setOpaque(true);
        y5LeftLabel.setOpaque(true);
        y5RithtLabel.setOpaque(true);
        
        y1Left2Label.setPreferredSize(new Dimension(80, 25));
        y1RightLabel.setPreferredSize(new Dimension(80, 25));
        y2LeftLabel.setPreferredSize(new Dimension(80, 25));
        y2RithtLabel.setPreferredSize(new Dimension(80, 25));
        y3LeftLabel.setPreferredSize(new Dimension(80, 25));
        y3RithtLabel.setPreferredSize(new Dimension(80, 25));
        y4LeftLabel.setPreferredSize(new Dimension(80, 25));
        y4RithtLabel.setPreferredSize(new Dimension(80, 25));
        y5LeftLabel.setPreferredSize(new Dimension(80, 25));
        y5RithtLabel.setPreferredSize(new Dimension(80, 25));
        
        y1Left2Label.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                openClosePanel(y1LeftPanel, y1Left2Label);
            }
        });
        y1RightLabel.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                openClosePanel(y1RightPanel, y1RightLabel);
            }
        });
        y2LeftLabel.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                openClosePanel(y2LeftPanel, y2LeftLabel);
            }
        });
        y2RithtLabel.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                openClosePanel(y2RightPanel, y2RithtLabel);
            }
        });
        y3LeftLabel.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                openClosePanel(y3LeftPanel, y3LeftLabel);
            }
        });
        y3RithtLabel.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                openClosePanel(y3RightPanel, y3RithtLabel);
            }
        });
        y4LeftLabel.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                openClosePanel(y4LeftPanel, y4LeftLabel);
            }
        });
        y4RithtLabel.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                openClosePanel(y4RightPanel, y4RithtLabel);
            }
        });
        y5LeftLabel.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                openClosePanel(y5LeftPanel, y5LeftLabel);
            }
        });
        y5RithtLabel.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                openClosePanel(y5RightPanel, y5RithtLabel);
            }
        });
        
        // close all panels
        openClosePanel(y1LeftPanel, y1Left2Label);
        openClosePanel(y1RightPanel, y1RightLabel);
        openClosePanel(y2LeftPanel, y2LeftLabel);
        openClosePanel(y2RightPanel, y2RithtLabel);
        openClosePanel(y3LeftPanel, y3LeftLabel);
        openClosePanel(y3RightPanel, y3RithtLabel);
        openClosePanel(y4LeftPanel, y4LeftLabel);
        openClosePanel(y4RightPanel, y4RithtLabel);
        openClosePanel(y5LeftPanel, y5LeftLabel);
        openClosePanel(y5RightPanel, y5RithtLabel);
        
        // open default
        openClosePanel(y2LeftPanel, y2LeftLabel);
        
        // delete not use items
        // X
        xPanel.chartCombo.setVisible(false);
        xPanel.columnNameCombo.setVisible(false);
        xPanel.weightField.setVisible(false);
        
        // Y1
        y1Panel.columnNameCombo.setVisible(false);
        
        // Y1 Right
        y1RightPanel.disableForRightAxis();
        // Y2 Right
        y2RightPanel.disableForRightAxis();
        // Y3 Right
        y3RightPanel.disableForRightAxis();
        // Y4 Right
        y4RightPanel.disableForRightAxis();
        // Y5 Right
        y5RightPanel.disableForRightAxis();
    }
    
    private void openClosePanel(AddtionalAxisPanel panel, JLabel label) {
        panel.setVisible(!panel.isVisible());
        boolean isVisible = panel.isVisible();
        if (isVisible) {
            label.setText(" -" +  label.getText().substring(2));
            label.setBackground(panel.color);
        } else {
            label.setText(" +" +  label.getText().substring(2));
            label.setBackground(CLOSE_PANEL_COLOR);
        }
        panel.revalidate();
        
        // re-size frame
        int height = PANEL_HEIGHT_BASE + countVisiblePanel() * PANEL_HEIGHT_PER_ITEM;
        if (axisPanelMap.get(AxisPosition.Y1_LEFT1).isVisible()) {
            // If Y1-Left is visible, plus additional height.
            height += PANEL_HEIGHT_PLUS_Y1LEFT;
        }
        settingPanel.setPreferredSize(new Dimension(PANEL_WIDTH, height));
        frame.pack();
    }
    
    private int countVisiblePanel() {
        int countVisible = 0;
        for (AddtionalAxisPanel axisPanel : axisPanelMap.values()) {
            if (axisPanel.isVisible()) {
                countVisible++;
            }
        }
        return countVisible;
    }
    
}
