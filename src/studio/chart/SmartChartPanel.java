package studio.chart;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
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
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.ValueAxisPlot;
import org.jfree.data.Range;

import studio.chart.AddtionalAxisPanel.AddtionalAxisPanelDefault;
import studio.chart.AddtionalAxisPanel.AddtionalAxisPanelY1Left;
import studio.chart.ChartSetting.ChartAxisSetting;
import studio.kdb.KTableModel;

/**
 * Setting Panel for Smart Chart
 */
public class SmartChartPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    
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
        int panelWidthBase = 830;
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
    
    private SmartChartManager manager;
    
    private JFrame frame = new JFrame("Smart Chart Console");
    private ChartSetting setting;
    
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
    private JButton updateButton = new JButton("Update Axis");
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
    // Constructor
    // //////////////////////////////////////
    
    public SmartChartPanel(SmartChartManager manager, ChartSetting setting) {
        this.manager = manager;
        this.setting = setting;
        
        // setup panel
        setupSettingPanel();
        intOpenCloseLabels();

        // frame setting
        frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.PAGE_AXIS));
        frame.getContentPane().add(this);
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
    
    // //////////////////////////////////////
    // Method - Button Action
    // //////////////////////////////////////

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

        setting.setNewFrame(newChartFrameCheckBox.isSelected());
        setting.setReverseRendering(reverseRenderingOrderCheckBox.isSelected());
        setting.setCrossHair(crosshairOverlayCheckBox.isSelected());
        setting.setScrollBar(scrollBarCheckBox.isSelected());
        setting.setScrollAdjust(scrollAdjustRangeCheckBox.isSelected());
        
        // Axis Items
        // It contains NOT used items
        for (Entry<AxisPosition, AddtionalAxisPanel> entry : axisPanelMap.entrySet()) {
            if (entry.getValue() instanceof AddtionalAxisPanelDefault) {
                AddtionalAxisPanelDefault panel = (AddtionalAxisPanelDefault) entry.getValue();

                ChartAxisSetting axisSetting = setting.getAxisSetting(entry.getKey());
                axisSetting.setLabel(panel.labelField.getText());
                axisSetting.setRangeMin(evalDoubleField(panel.minField));
                axisSetting.setRangeMax(evalDoubleField(panel.maxField));
                axisSetting.setRangeLength(evalDoubleField(panel.lengthField));
                axisSetting.setIncludeZero(panel.includeZeroCheckbox.isSelected());
                axisSetting.setMarkerLines(evalMarkerLine(panel.markerLineField.getText()));
                axisSetting.setChartType((ChartType) panel.chartCombo.getSelectedItem());
                axisSetting.setColumnName(getColumnNameValue(panel.columnNameCombo));
                axisSetting.setWeight(evalWeihgt(panel.weightField.getText()));
                axisSetting.setSeriesColor((SeriesColor) panel.colorCombo.getSelectedItem());
                
            } else if (entry.getValue() instanceof AddtionalAxisPanelY1Left) {
                // Y1 Left 1-4
                AddtionalAxisPanelY1Left panel = (AddtionalAxisPanelY1Left) entry.getValue();

                ChartAxisSetting axisSetting1 = setting.getAxisSetting(AxisPosition.Y1_LEFT1);
                axisSetting1.setChartType((ChartType) panel.chartCombo1.getSelectedItem());
                axisSetting1.setColumnName(getColumnNameValue(panel.columnNameCombo1));
                axisSetting1.setSeriesColor((SeriesColor) panel.colorCombo1.getSelectedItem());

                ChartAxisSetting axisSetting2 = setting.getAxisSetting(AxisPosition.Y1_LEFT2);
                axisSetting2.setChartType((ChartType) panel.chartCombo2.getSelectedItem());
                axisSetting2.setColumnName(getColumnNameValue(panel.columnNameCombo2));
                axisSetting2.setSeriesColor((SeriesColor) panel.colorCombo2.getSelectedItem());
                
                ChartAxisSetting axisSetting3 = setting.getAxisSetting(AxisPosition.Y1_LEFT3);
                axisSetting3.setChartType((ChartType) panel.chartCombo3.getSelectedItem());
                axisSetting3.setColumnName(getColumnNameValue(panel.columnNameCombo3));
                axisSetting3.setSeriesColor((SeriesColor) panel.colorCombo3.getSelectedItem());

                ChartAxisSetting axisSetting4 = setting.getAxisSetting(AxisPosition.Y1_LEFT4);
                axisSetting4.setChartType((ChartType) panel.chartCombo4.getSelectedItem());
                axisSetting4.setColumnName(getColumnNameValue(panel.columnNameCombo4));
                axisSetting4.setSeriesColor((SeriesColor) panel.colorCombo4.getSelectedItem());
            }
        }
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
        
        // reset setting
        copySettings();
    }


    /**
     * Fill range value from plot axis
     * @param axis
     * @param axisPosition
     */
    public void fillRangeToField(ValueAxis axis, AxisPosition axisPosition) {
        if (!(axisPanelMap.get(axisPosition) instanceof AddtionalAxisPanelDefault)) {
            return;
        }
        AddtionalAxisPanelDefault panel = (AddtionalAxisPanelDefault) axisPanelMap.get(axisPosition);
        
        Range range = axis.getRange();
        if (axis instanceof DateAxis) {
            DateAxis dateAxis = (DateAxis) axis;
            
            // get original full range
            Range dataRange = ((ValueAxisPlot) dateAxis.getPlot()).getDataRange(dateAxis);
            Date dataMinDate = new Date((long) dataRange.getLowerBound());
            Date dataMaxDate = new Date((long) dataRange.getUpperBound());
            
            // get view range
            Date lowerDate = dateAxis.getMinimumDate();
            Date upperDate = dateAxis.getMaximumDate();
            if (!DateUtils.isSameDay(dataMinDate, dataMaxDate) || DateUtility.compareDate(lowerDate, upperDate) != 0) {
                // show date and time
                ((GuideTextField) panel.minField).clearText(DateUtility.parseString(lowerDate, true));
                ((GuideTextField) panel.maxField).clearText(DateUtility.parseString(upperDate, true));
            } else {
                // if lower and upper is the same date, don't show date.
                ((GuideTextField) panel.minField).clearText(DateUtility.parseString(lowerDate, false));
                ((GuideTextField) panel.maxField).clearText(DateUtility.parseString(upperDate, false));
            }
        } else {
            // set precision 6
            int basePrecision = 6;
            BigDecimal min = new BigDecimal(range.getLowerBound());
            BigDecimal max = new BigDecimal(range.getUpperBound());
            if (min.precision() > basePrecision && min.scale() > 0) {
                if (min.precision() > min.scale()) {
                    min = min.setScale(Math.max(0, basePrecision - min.precision() + min.scale()), RoundingMode.HALF_UP);
                } else {
                    min = min.setScale(basePrecision + min.scale() - min.precision(), RoundingMode.HALF_UP);
                }
            }
            if (max.precision() > basePrecision && max.scale() > 0) {
                if (max.precision() > max.scale()) {
                    max = max.setScale(Math.max(0, basePrecision - max.precision() + max.scale()), RoundingMode.HALF_UP);
                } else {
                    max = max.setScale(basePrecision + max.scale() - max.precision(), RoundingMode.HALF_UP);
                }
            }
            ((GuideTextField) panel.minField).clearText(min.toPlainString());
            ((GuideTextField) panel.maxField).clearText(max.toPlainString());
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
    
    private String getColumnNameValue(JComboBox<String> columnNameCombo) {
        String columnName = (String) columnNameCombo.getSelectedItem();
        if (DEFAULT_COLUMN_NAME.equals(columnName)) {
            return "";
        } 
        
        // check existence of column
        KTableModel table = manager.getKTableModel();
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
     * Set Column Name Combo from KTable
     */
    private void setupColumnNameCombo() {
        KTableModel table = manager.getKTableModel();
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
    
    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    public JFrame getFrame() {
        return frame;
    }
    
    private String getStackTraceString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }
    
    ///////////////////////////////////////////////////
    // Method - Field Utility
    ///////////////////////////////////////////////////
    
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
                    copySettings();
                    manager.showChart();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, getStackTraceString(ex), "ERROR_MESSAGE", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    copySettings();
                    manager.updateChart();
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
                    setupColumnNameCombo();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, getStackTraceString(ex), "ERROR_MESSAGE", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        fillRangeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    clearAllRange();
                    manager.fillCurrentRangeAll();
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
        add(chartButtonPanel);
        add(clearButtonPanel);
        add(windowPanel);
        add(xPanel);
        add(y1Panel);
        add(yOpenCloseWrapperPanel);
        add(y1LeftPanel);
        add(y1RightPanel);
        add(y2LeftPanel);
        add(y2RightPanel);
        add(y3LeftPanel);
        add(y3RightPanel);
        add(y4LeftPanel);
        add(y4RightPanel);
        add(y5LeftPanel);
        add(y5RightPanel);

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
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT_BASE + countVisiblePanel() * PANEL_HEIGHT_PER_ITEM));
        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(1, 2, 1, 2);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weighty = 0.0d;
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        layout.setConstraints(windowPanel, gbc);
        gbc.gridx = 1;
        gbc.gridheight = 2;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        layout.setConstraints(chartButtonPanel, gbc);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridheight = 1;
        gbc.gridx = 0;
        
        gbc.gridy++;
        layout.setConstraints(xPanel, gbc);
        gbc.gridy++;
        gbc.gridwidth = 2;
        layout.setConstraints(y1Panel, gbc);
        gbc.gridwidth = 1;

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
        openClosePanel(y1LeftPanel, y1Left2Label);
        openClosePanel(y2LeftPanel, y2LeftLabel);
        
        // delete not use items
        // X
        xPanel.chartCombo.setVisible(false);
        xPanel.columnNameCombo.setVisible(false);
        xPanel.weightField.setVisible(false);
        xPanel.colorCombo.setVisible(false);
        
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
            label.setBackground(panel.panelColor);
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
        setPreferredSize(new Dimension(PANEL_WIDTH, height));
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
