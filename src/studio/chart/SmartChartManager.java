package studio.chart;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

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
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;

import studio.kdb.Config;
import studio.kdb.KTableModel;
import studio.ui.Studio;
import studio.ui.Util;

public class SmartChartManager {
    
    // //////////////////////////////////////
    // Filed
    // //////////////////////////////////////

    private static final int PANEL_WIDTH = 680;
    private static final int PANEL_HEIGHT = 560;
    
    private static final int TEXT_FIELD_COLUMNS_NORMAL = 6;
    private static final int TEXT_FIELD_COLUMNS_LONG = 8;
    private static final int TEXT_FIELD_COLUMNS_SHORT = 4;
    private static final String DEFAULT_COLUMN_NAME = "(Separator)";
    
    private static final Color OPEN_PANEL_COLOR = new Color(255, 255, 127);
    private static final Color CLOSE_PANEL_COLOR = Color.LIGHT_GRAY;
    
    private static SmartChartManager instance;
    
    private JFrame frame = new JFrame("Smart Chart Console");
    private JPanel settingPanel = new JPanel();
    
    private JFrame chartFrame;
    private ChartPanel chartPanel;
    private JFreeChart chart;
    
    private Studio studio;
    
    private SmartChartSetting setting = new SmartChartSetting();

    //
    // Component Items
    //
    
    // Panel
    private JPanel windowPanel = new JPanel();
    private JPanel yOpenClosePanel = new JPanel();
    private JPanel chartButtonPanel = new JPanel();
    private JPanel columnNameButtonPanel = new JPanel();
    private JPanel clearButtonPanel = new JPanel();
    
    // Button
    private JButton chartButton = new JButton("Create Chart");
    private JButton updateButton = new JButton("Update Chart");
    private JButton clearButton = new JButton("Clear Settings");
    private JButton columnNameButton = new JButton("Update Separator");
    
    // Window
    private JTextField titleField = new GuideTextField("Title", TEXT_FIELD_COLUMNS_LONG);
    private JTextField xSizeField = new GuideTextField("X size", String.valueOf(SmartChartSetting.WINDOW_X_DEFAULT), TEXT_FIELD_COLUMNS_NORMAL);
    private JTextField ySizeField = new GuideTextField("Y size", String.valueOf(SmartChartSetting.WINDOW_Y_DEFAULT), TEXT_FIELD_COLUMNS_NORMAL);
    private JCheckBox newChartFrameCheckBox = new JCheckBox("New Frame", true);
    private JComboBox<ChartTheme> themeCombo = new JComboBox<>(ChartTheme.values());
    
    // Open/Close label
    private JLabel y1Left2Label = new JLabel(" + Y1 Left2");
    private JLabel y1RightLabel = new JLabel(" + Y1 Right");
    private JLabel y2LeftLabel = new JLabel(" + Y2 Left");
    private JLabel y2RithtLabel = new JLabel(" + Y2 Right");
    private JLabel y3LeftLabel = new JLabel(" + Y3 Left");
    private JLabel y4LeftLabel = new JLabel(" + Y4 Left");
    
    // Additional Axis Panel
    private AddtionalAxisPanel xPanel = new AddtionalAxisPanel("X");
    private AddtionalAxisPanel y1Panel = new AddtionalAxisPanel("Y1");
    private AddtionalAxisPanel y1LeftPanel = new AddtionalAxisPanel("Y1 Left2");
    private AddtionalAxisPanel y1RightPanel = new AddtionalAxisPanel("Y1 Right");
    private AddtionalAxisPanel y2LeftPanel = new AddtionalAxisPanel("Y2 Left");
    private AddtionalAxisPanel y2RightPanel = new AddtionalAxisPanel("Y2 Right");
    private AddtionalAxisPanel y3LeftPanel = new AddtionalAxisPanel("Y3 Left");
    private AddtionalAxisPanel y4LeftPanel = new AddtionalAxisPanel("Y4 Left");

    
    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////
    
    public static synchronized SmartChartManager getInstance(Studio studio) {
        if (instance == null) {
            instance = new SmartChartManager(studio);
        }
        return instance;
    }
    
    public void showPanel() {
        frame.setVisible(true);
        frame.requestFocus();
        frame.toFront();
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
//        frame.setVisible(true);
        frame.requestFocus();
        frame.toFront();
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
                
            }
    }

    public void createChart(KTableModel table, boolean newFrame) {
        chart = ChartDataCreator.createChart(table, setting);
        if (chart != null) {
            if (newFrame) {
                chartFrame = creatChartFrame();
            }
            
            chartPanel = new ChartPanel(chart);
            chartPanel.setMouseZoomable(true, false);
            updateChart();
            
            chartFrame.setContentPane(chartPanel);
            chartFrame.pack();
            chartFrame.setVisible(true);
            chartFrame.requestFocus();
            chartFrame.toFront();
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
        chartPanel.setPreferredSize(new Dimension(setting.getxSize(), setting.getySize()));

        // Label, Range
        if (!StringUtils.isBlank(setting.getxLabel())) {
            chart.getXYPlot().getDomainAxis().setLabel(setting.getxLabel());
        }
        Plot plot = chart.getPlot();

        // Domain Label, range
        if (plot instanceof XYPlot) {
            setupRangeAxis(chart.getXYPlot().getDomainAxis(), setting.getxLabel(), setting.getxMin(), setting.getxMax(), setting.isxIncludeZero());
        } else if (plot instanceof CategoryPlot) {
            chart.getCategoryPlot().getDomainAxis().setLabel(setting.getxLabel());
        }
        
        // Plot and Range
        if (plot instanceof CombinedDomainXYPlot) {
            // Multi-plot
            @SuppressWarnings("unchecked")
            List<XYPlot> plots = ((CombinedDomainXYPlot) plot).getSubplots();
            switch (plots.size()) {
                case 4:
                    XYPlot plot4 = plots.get(3);
                    setupRangeAxis(plot4.getRangeAxis(), setting.getY4LeftLabel(), setting.getY4LeftMin(), setting.getY4LeftMax(), setting.isY4LeftIncludeZero());
                    setupPlot(plot4, setting.isxShowLine(), setting.isY4LeftShowLine());
                case 3:
                    XYPlot plot3 = plots.get(2);
                    setupRangeAxis(plot3.getRangeAxis(), setting.getY3LeftLabel(), setting.getY3LeftMin(), setting.getY3LeftMax(), setting.isY3LeftIncludeZero());
                    setupPlot(plot3, setting.isxShowLine(), setting.isY3LeftShowLine());
                case 2:
                    XYPlot plot2 = plots.get(1);
                    setupRangeAxis(plot2.getRangeAxis(), setting.getY2LeftLabel(), setting.getY2LeftMin(), setting.getY2LeftMax(), setting.isY2LeftIncludeZero());
                    setupPlot(plot2, setting.isxShowLine(), setting.isY2LeftShowLine());
                    if (plot2.getRangeAxisCount() == 2) {
                        setupRangeAxis(plot2.getRangeAxis(1), setting.getY2RightLabel(), setting.getY2RightMin(), setting.getY2RightMax(), setting.isY2RightIncludeZero());
                    }
                default:
                    XYPlot plot1 = plots.get(0);
                    setupRangeAxis(plot1.getRangeAxis(), setting.getY1Label(), setting.getY1Min(), setting.getY1Max(), setting.isY1IncludeZero());
                    setupPlot(plot1, setting.isxShowLine(), setting.isY1ShowLine());
                    if (plot1.getRangeAxisCount() == 2) {
                        setupRangeAxis(plot1.getRangeAxis(1), setting.getY1RightLabel(), setting.getY1RightMin(), setting.getY1RightMax(), setting.isY1RightIncludeZero());
                    }
                    break;
            }
            
        } else if (plot instanceof XYPlot) {
            // one plot
            XYPlot xyPlot = (XYPlot) plot;
            setupRangeAxis(xyPlot.getRangeAxis(), setting.getY1Label(), setting.getY1Min(), setting.getY1Max(), setting.isY1IncludeZero());
            setupPlot(xyPlot, setting.isxShowLine(), setting.isY1ShowLine());
            if (xyPlot.getRangeAxisCount() == 2) {
                setupRangeAxis(xyPlot.getRangeAxis(1), setting.getY1RightLabel(), setting.getY1RightMin(), setting.getY1RightMax(), setting.isY1RightIncludeZero());
            }

        } else if (plot instanceof CategoryPlot) {
            // TBD
        }
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
            return;
            
        } else {
            return;
        }
    }
    
    private void setupPlot(Plot plot, boolean showLineDomain, boolean showLineRange) {
        // show line
        if (plot instanceof XYPlot) {
            XYPlot xyPlot = (XYPlot) plot;
            xyPlot.setDomainZeroBaselineVisible(showLineDomain);
            xyPlot.setRangeZeroBaselineVisible(showLineRange);
        }
    }

    private void copySettings() {
        // Window
        setting.setTitle(titleField.getText());
        setting.setxSize(evalWidnowSize(xSizeField.getText(), true));
        setting.setySize(evalWidnowSize(ySizeField.getText(), false));
        setting.setTheme((ChartTheme) themeCombo.getSelectedItem());
        
        // Label
        setting.setxLabel(xPanel.labelField.getText());
        setting.setY1Label(y1Panel.labelField.getText());
        //
        setting.setY1RightLabel(y1RightPanel.labelField.getText());
        setting.setY2LeftLabel(y2LeftPanel.labelField.getText());
        setting.setY2RightLabel(y2RightPanel.labelField.getText());
        setting.setY3LeftLabel(y3LeftPanel.labelField.getText());
        setting.setY4LeftLabel(y4LeftPanel.labelField.getText());

        // Range
        setting.setxMin(evalDoubleField(xPanel.minField));
        setting.setxMax(evalDoubleField(xPanel.maxField));
        setting.setY1Min(evalDoubleField(y1Panel.minField));
        setting.setY1Max(evalDoubleField(y1Panel.maxField));
        //
        //
        setting.setY1RightMin(evalDoubleField(y1RightPanel.minField));
        setting.setY1RightMax(evalDoubleField(y1RightPanel.maxField));
        setting.setY2LeftMin(evalDoubleField(y2LeftPanel.minField));
        setting.setY2LeftMax(evalDoubleField(y2LeftPanel.maxField));
        setting.setY2RightMin(evalDoubleField(y2RightPanel.minField));
        setting.setY2RightMax(evalDoubleField(y2RightPanel.maxField));
        setting.setY3LeftMin(evalDoubleField(y3LeftPanel.minField));
        setting.setY3LeftMax(evalDoubleField(y3LeftPanel.maxField));
        setting.setY4LeftMin(evalDoubleField(y4LeftPanel.minField));
        setting.setY4LeftMax(evalDoubleField(y4LeftPanel.maxField));

        // Include zero
        setting.setxIncludeZero(xPanel.includeZeroCheckbox.isSelected());
        setting.setY1IncludeZero(y1Panel.includeZeroCheckbox.isSelected());
        setting.setY1RightIncludeZero(y1RightPanel.includeZeroCheckbox.isSelected());
        setting.setY2LeftIncludeZero(y2LeftPanel.includeZeroCheckbox.isSelected());
        setting.setY2RightIncludeZero(y2RightPanel.includeZeroCheckbox.isSelected());
        setting.setY3LeftIncludeZero(y3LeftPanel.includeZeroCheckbox.isSelected());
        setting.setY4LeftIncludeZero(y4LeftPanel.includeZeroCheckbox.isSelected());
        
        // Show line
        setting.setxShowLine(xPanel.showLineCheckbox.isSelected());
        setting.setY1ShowLine(y1Panel.showLineCheckbox.isSelected());
        //
        //
        setting.setY2LeftShowLine(y2LeftPanel.showLineCheckbox.isSelected());
        //
        setting.setY3LeftShowLine(y3LeftPanel.showLineCheckbox.isSelected());
        setting.setY4LeftShowLine(y4LeftPanel.showLineCheckbox.isSelected());
        
        // Chart Type
        setting.setY1Chart((ChartType) y1Panel.chartCombo.getSelectedItem());
        setting.setY1LeftChart((ChartType) y1LeftPanel.chartCombo.getSelectedItem());
        setting.setY1RightChart((ChartType) y1RightPanel.chartCombo.getSelectedItem());
        setting.setY2LeftChart((ChartType) y2LeftPanel.chartCombo.getSelectedItem());
        setting.setY2RightChart((ChartType) y2RightPanel.chartCombo.getSelectedItem());
        setting.setY3LeftChart((ChartType) y3LeftPanel.chartCombo.getSelectedItem());
        setting.setY4LeftChart((ChartType) y4LeftPanel.chartCombo.getSelectedItem());
        
        // Colname
        setting.setY1LeftColumnName(getColumnNameValue(y1LeftPanel.columnNameCombo));
        setting.setY1RightColumnName(getColumnNameValue(y1RightPanel.columnNameCombo));
        setting.setY2LeftColumnName(getColumnNameValue(y2LeftPanel.columnNameCombo));
        setting.setY2RightColumnName(getColumnNameValue(y2RightPanel.columnNameCombo));
        setting.setY3LeftColumnName(getColumnNameValue(y3LeftPanel.columnNameCombo));
        setting.setY4LeftColumnName(getColumnNameValue(y4LeftPanel.columnNameCombo));
        
        // Weight
        setting.setY1Weight(evalWeihgt(y1Panel.weightField.getText()));
        setting.setY2LeftWeight(evalWeihgt(y2LeftPanel.weightField.getText()));
        setting.setY3LeftWeight(evalWeihgt(y3LeftPanel.weightField.getText()));
        setting.setY4LeftWeight(evalWeihgt(y4LeftPanel.weightField.getText()));
    }
    
    private double evalDoubleField(JTextField field) {
        if (NumberUtils.isNumber(field.getText())) {
            return Double.parseDouble(field.getText());
        } else {
            return Double.NaN;
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
                return SmartChartSetting.WINDOW_X_DEFAULT;
            } else {
                return SmartChartSetting.WINDOW_Y_DEFAULT;
            }
        }
        // invalid size
        JOptionPane.showMessageDialog(frame, "Window size must be between 50 and 2000.", "ERROR_MESSAGE", JOptionPane.ERROR_MESSAGE);
        throw new RuntimeException();
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
        ((GuideTextField) xSizeField).clearText(String.valueOf(SmartChartSetting.WINDOW_X_DEFAULT));
        ((GuideTextField) ySizeField).clearText(String.valueOf(SmartChartSetting.WINDOW_Y_DEFAULT));
        newChartFrameCheckBox.setSelected(true);
        themeCombo.setSelectedIndex(0);
        
        // Additional panels
        xPanel.clearSetting();
        y1Panel.clearSetting();
        y1LeftPanel.clearSetting();
        y1RightPanel.clearSetting();
        y2LeftPanel.clearSetting();
        y2RightPanel.clearSetting();
        y3LeftPanel.clearSetting();
        y4LeftPanel.clearSetting();
        
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
        
        y1LeftPanel.columnNameCombo.removeAllItems();
        y1RightPanel.columnNameCombo.removeAllItems();
        y2LeftPanel.columnNameCombo.removeAllItems();
        y2RightPanel.columnNameCombo.removeAllItems();
        y3LeftPanel.columnNameCombo.removeAllItems();
        y4LeftPanel.columnNameCombo.removeAllItems();
        
        for (String columnName : columnNameList) {
            y1LeftPanel.columnNameCombo.addItem(columnName);
            y1RightPanel.columnNameCombo.addItem(columnName);
            y2LeftPanel.columnNameCombo.addItem(columnName);
            y2RightPanel.columnNameCombo.addItem(columnName);
            y3LeftPanel.columnNameCombo.addItem(columnName);
            y4LeftPanel.columnNameCombo.addItem(columnName);
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

        //
        // Add components
        //

        // Panels
        settingPanel.add(chartButtonPanel);
        settingPanel.add(columnNameButtonPanel);
        settingPanel.add(clearButtonPanel);
        settingPanel.add(windowPanel);
        settingPanel.add(xPanel);
        settingPanel.add(y1Panel);
        settingPanel.add(yOpenClosePanel);
        settingPanel.add(y1LeftPanel);
        settingPanel.add(y1RightPanel);
        settingPanel.add(y2LeftPanel);
        settingPanel.add(y2RightPanel);
        settingPanel.add(y3LeftPanel);
        settingPanel.add(y4LeftPanel);

        // Button
        chartButtonPanel.add(chartButton);
        chartButtonPanel.add(updateButton);
        columnNameButtonPanel.add(columnNameButton);
        clearButtonPanel.add(clearButton);
        
        // window
        windowPanel.add(titleField);
        windowPanel.add(xSizeField);
        windowPanel.add(ySizeField);
        windowPanel.add(newChartFrameCheckBox);
        windowPanel.add(themeCombo);
        
        // Y Open/Close
        yOpenClosePanel.add(y1Left2Label);
        yOpenClosePanel.add(y1RightLabel);
        yOpenClosePanel.add(y2LeftLabel);
        yOpenClosePanel.add(y2RithtLabel);
        yOpenClosePanel.add(y3LeftLabel);
        yOpenClosePanel.add(y4LeftLabel);
        
        //
        // Set layout
        //
        
        // setting panel
        settingPanel.setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
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
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.SOUTHEAST;
        layout.setConstraints(columnNameButtonPanel, gbc);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy++;
        layout.setConstraints(yOpenClosePanel, gbc);
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
        layout.setConstraints(y4LeftPanel, gbc);
        gbc.gridy++;
        gbc.weighty = 1.0d;
        gbc.anchor = GridBagConstraints.NORTHEAST;
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
        gbc.insets = new Insets(1, 10, 1, 10);
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        layout.setConstraints(y1Left2Label, gbc);
        gbc.gridx++;
        layout.setConstraints(y1RightLabel, gbc);
        gbc.gridx++;
        layout.setConstraints(y2LeftLabel, gbc);
        gbc.gridx++;
        layout.setConstraints(y2RithtLabel, gbc);
        gbc.gridx++;
        layout.setConstraints(y3LeftLabel, gbc);
        gbc.gridx++;
        layout.setConstraints(y4LeftLabel, gbc);
        
        // colume name panel
        
        // window panel
        windowPanel.setBorder(new TitledBorder("Window"));
        layout = new GridBagLayout();
        windowPanel.setLayout(layout);
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
    }
    
    private void intOpenCloseLabels() {
        y1Left2Label.setBackground(OPEN_PANEL_COLOR);
        y1RightLabel.setBackground(OPEN_PANEL_COLOR);
        y2LeftLabel.setBackground(OPEN_PANEL_COLOR);
        y2RithtLabel.setBackground(OPEN_PANEL_COLOR);
        y3LeftLabel.setBackground(OPEN_PANEL_COLOR);
        y4LeftLabel.setBackground(OPEN_PANEL_COLOR);

        y1Left2Label.setOpaque(true);
        y1RightLabel.setOpaque(true);
        y2LeftLabel.setOpaque(true);
        y2RithtLabel.setOpaque(true);
        y3LeftLabel.setOpaque(true);
        y4LeftLabel.setOpaque(true);
        
        y1Left2Label.setPreferredSize(new Dimension(80, 25));
        y1RightLabel.setPreferredSize(new Dimension(80, 25));
        y2LeftLabel.setPreferredSize(new Dimension(80, 25));
        y2RithtLabel.setPreferredSize(new Dimension(80, 25));
        y3LeftLabel.setPreferredSize(new Dimension(80, 25));
        y4LeftLabel.setPreferredSize(new Dimension(80, 25));
        
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
        y4LeftLabel.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                openClosePanel(y4LeftPanel, y4LeftLabel);
            }
        });
        
        // close minor panels
        openClosePanel(y1LeftPanel, y1Left2Label);
        openClosePanel(y2RightPanel, y2RithtLabel);
        openClosePanel(y3LeftPanel, y3LeftLabel);
        openClosePanel(y4LeftPanel, y4LeftLabel);
        
        // delete not use items
        // X
        xPanel.chartCombo.setVisible(false);
        xPanel.columnNameCombo.setVisible(false);
        xPanel.weightField.setVisible(false);
        
        // Y1
        y1Panel.columnNameCombo.setVisible(false);
        
        // Y1 Left
        y1LeftPanel.labelField.setEnabled(false);
        y1LeftPanel.minField.setEnabled(false);
        y1LeftPanel.maxField.setEnabled(false);
        y1LeftPanel.includeZeroCheckbox.setEnabled(false);
        y1LeftPanel.showLineCheckbox.setEnabled(false);
        y1LeftPanel.weightField.setEnabled(false);
        
        y1LeftPanel.labelField.setBackground(Color.GRAY);
        y1LeftPanel.minField.setBackground(Color.GRAY);
        y1LeftPanel.maxField.setBackground(Color.GRAY);
        y1LeftPanel.includeZeroCheckbox.setBackground(Color.GRAY);
        y1LeftPanel.showLineCheckbox.setBackground(Color.GRAY);
        y1LeftPanel.weightField.setBackground(Color.GRAY);
        
        // Y1 Right
        y1RightPanel.weightField.setEnabled(false);
        y1RightPanel.showLineCheckbox.setEnabled(false);
        y1RightPanel.weightField.setBackground(Color.GRAY);
        y1RightPanel.showLineCheckbox.setBackground(Color.GRAY);
        
        // Y2 Right
        y2RightPanel.weightField.setEnabled(false);
        y2RightPanel.showLineCheckbox.setEnabled(false);
        y2RightPanel.weightField.setBackground(Color.GRAY);
        y2RightPanel.showLineCheckbox.setBackground(Color.GRAY);
    }
    
    private void openClosePanel(JPanel panel, JLabel label) {
        panel.setVisible(!panel.isVisible());
        boolean isVisible = panel.isVisible();
        if (isVisible) {
            label.setText(" -" +  label.getText().substring(2));
            label.setBackground(OPEN_PANEL_COLOR);
        } else {
            label.setText(" +" +  label.getText().substring(2));
            label.setBackground(CLOSE_PANEL_COLOR);
        }
        panel.revalidate();
    }
    
    class AddtionalAxisPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        @SuppressWarnings("unused")
        private String itemName;

        private JTextField labelField;
        private JTextField minField;
        private JTextField maxField;
        private JCheckBox includeZeroCheckbox = new JCheckBox("Inc. 0", false);
        private JCheckBox showLineCheckbox = new JCheckBox("Line", false);
        private JComboBox<ChartType> chartCombo = new JComboBox<>(ChartType.values());
        private JComboBox<String> columnNameCombo = new JComboBox<>(new String[]{DEFAULT_COLUMN_NAME});
        private JTextField weightField = new GuideTextField("weight", "1", TEXT_FIELD_COLUMNS_SHORT);
        
        public AddtionalAxisPanel(String itemName) {
            this.itemName = itemName;
            
            // Component
            labelField = new GuideTextField("Label", TEXT_FIELD_COLUMNS_LONG);
            minField = new GuideTextField("Min", TEXT_FIELD_COLUMNS_NORMAL);
            maxField = new GuideTextField("Max", TEXT_FIELD_COLUMNS_NORMAL);
            
            columnNameCombo.setPreferredSize(new Dimension(120, 25));
            
            // int panel
            this.add(labelField);
            this.add(minField);
            this.add(maxField);
            this.add(includeZeroCheckbox);
            this.add(showLineCheckbox);
            this.add(chartCombo);
            this.add(columnNameCombo);
            this.add(weightField);
            
            // panel layout
            this.setBorder(new TitledBorder(itemName + " Axis"));
            GridBagLayout layout = new GridBagLayout();
            this.setLayout(layout);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(1, 2, 1, 2);
            
            gbc.gridx = 0;
            gbc.gridy = 0;
            layout.setConstraints(labelField, gbc);
            gbc.gridx++;
            layout.setConstraints(minField, gbc);
            gbc.gridx++;
            layout.setConstraints(maxField, gbc);
            gbc.gridx++;
            layout.setConstraints(includeZeroCheckbox, gbc);
            gbc.gridx++;
            layout.setConstraints(showLineCheckbox, gbc);
            gbc.gridx++;
            layout.setConstraints(chartCombo, gbc);
            gbc.gridx++;
            layout.setConstraints(columnNameCombo, gbc);
            gbc.gridx++;
            layout.setConstraints(weightField, gbc);
        }
        
        private void clearSetting() {
            ((GuideTextField) labelField).clearText("");
            ((GuideTextField) minField).clearText("");
            ((GuideTextField) maxField).clearText("");
            includeZeroCheckbox.setSelected(false);
            showLineCheckbox.setSelected(false);
            chartCombo.setSelectedIndex(0);
            columnNameCombo.removeAllItems();
            columnNameCombo.addItem(DEFAULT_COLUMN_NAME);
            ((GuideTextField) weightField).clearText("1");
        }
    }
}
