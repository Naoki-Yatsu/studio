package studio.chart;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.SegmentedTimeline;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.event.AxisChangeListener;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;

import studio.kdb.Config;
import studio.ui.Util;

public class CustomChartFrame extends JFrame implements AxisChangeListener {
    
    private static final long serialVersionUID = 1L;
    
    // //////////////////////////////////////
    // Filed
    // //////////////////////////////////////
    
    private SmartChartManager manager;
    
    /** The chart panel. */
    private ChartPanel chartPanel;
    
    /** Main Panel */
    private JPanel mainPanel = new JPanel(new BorderLayout());
    
    // Top Panel Items
    private JButton chartButton = createSmallButton("Update Chart");
    private JButton fillRangeButton = createSmallButton("Fill Range");
    private JButton backZoomButton = createSmallButton("Back Zoom");
    private JLabel domainLabel = new JLabel();
    
    
    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////
    
    public CustomChartFrame(String title, SmartChartManager manager, boolean showTopButton) {
        super(title);
        this.manager = manager;
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setIconImage(Util.getImage(Config.imageBase2 + "chart_24.png").getImage());
        setContentPane(mainPanel);
        if (showTopButton) {
            mainPanel.add(createTopPanel(), BorderLayout.PAGE_START);
        }   
    }
    
    private JPanel createTopPanel() {
        JFrame frame = this;

        // setup component
        domainLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        
        // create panel
        JPanel topPanel = new JPanel();
        topPanel.add(chartButton);
        topPanel.add(domainLabel);
        topPanel.add(backZoomButton);
        topPanel.add(fillRangeButton);
        
        GridBagLayout layout = new GridBagLayout();
        topPanel.setLayout(layout);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(1, 10, 1, 10);
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0d;
        gbc.anchor = GridBagConstraints.WEST;
        layout.setConstraints(chartButton, gbc);
        gbc.gridx++;
        gbc.anchor = GridBagConstraints.CENTER;
        layout.setConstraints(domainLabel, gbc);
        gbc.gridx++;
        gbc.anchor = GridBagConstraints.CENTER;
        layout.setConstraints(backZoomButton, gbc);
        gbc.gridx++;
        gbc.anchor = GridBagConstraints.EAST;
        layout.setConstraints(fillRangeButton, gbc);
        
        //
        // Action
        //
        chartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    System.gc();
                    manager.getSettingPanel().setupSettings();
                    boolean success = manager.showChart(getBounds(), chartPanel.getSize());
                    if (success) {
                        frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
                    }
                    System.gc();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, getStackTraceString(ex), "ERROR_MESSAGE", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        fillRangeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    manager.getSettingPanel().clearAllRange();
                    manager.fillCurrentRange(chartPanel.getChart());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, getStackTraceString(ex), "ERROR_MESSAGE", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        backZoomButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    executeBackZoom();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, getStackTraceString(ex), "ERROR_MESSAGE", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        return topPanel;
    }
    
    /**
     * Set ChartPanel using scroll bar
     * @param chartPanel
     * @param autoScrollRange
     * @param scrollRangeLengthList
     */
    public void setChartPanelScroll(ChartPanel chartPanel, boolean autoScrollRange, List<Double> scrollRangeLengthList, SegmentedTimeline timeline) {
        ChartScrollPanel scrollPanel = new ChartScrollPanel((ChartPanel)chartPanel, autoScrollRange, scrollRangeLengthList, timeline);
        addKeyListener(scrollPanel);
        mainPanel.add(scrollPanel, BorderLayout.CENTER);
        
        this.chartPanel = chartPanel;
        setupAxisRange(chartPanel.getChart());
    }
    
    /**
     * Set ChartPanel 
     * @param chartPanel
     */
    public void setChartPanel(ChartPanel chartPanel) {
        mainPanel.add(chartPanel, BorderLayout.CENTER);
        
        this.chartPanel = chartPanel;
        setupAxisRange(chartPanel.getChart());
    }
    
    ///////////////////////////////////////////////////
    // Axis (For Back)
    ///////////////////////////////////////////////////
   
    private Map<Integer, ValueAxis> axisIndexMap = new HashMap<>();
    private Map<Integer, List<Range>> axisRangeHistoryMap = new HashMap<>();
    private ValueAxis domainAxis;
    
    private void setupAxisRange(JFreeChart chart) {
        if (!(chartPanel.getChart().getPlot() instanceof XYPlot)) {
            return;
        }
        XYPlot xyPlot = chartPanel.getChart().getXYPlot();
        int index = 0;
        
        // domain
        ValueAxis domainAxis = xyPlot.getDomainAxis();
        setupEachAxis(index++, domainAxis);
        this.domainAxis = domainAxis;
        
        // range
        if (xyPlot instanceof CombinedDomainXYPlot) {
            @SuppressWarnings("unchecked")
            List<XYPlot> plots = ((CombinedDomainXYPlot) xyPlot).getSubplots();
            for (XYPlot subPlot : plots) {
                for (int i = 0; i < subPlot.getRangeAxisCount(); i++) {
                    setupEachAxis(index++, subPlot.getRangeAxis(i));
                }
            }
        } else {
            for (int i = 0; i < xyPlot.getRangeAxisCount(); i++) {
                setupEachAxis(index++, xyPlot.getRangeAxis(i));
            }
        }
    }
    
    private void setupEachAxis(int index, ValueAxis axis) {
        axis.addChangeListener(this);
        List<Range> rangeList = new ArrayList<>();
        rangeList.add(axis.getRange());
        axisIndexMap.put(index, axis);
        axisRangeHistoryMap.put(index, rangeList);
    }

    private void executeBackZoom() {
        for (Entry<Integer, ValueAxis> entry : axisIndexMap.entrySet()) {
            ValueAxis axis = entry.getValue();
            List<Range> rangeList = axisRangeHistoryMap.get(entry.getKey());
            if (rangeList.size() < 2) {
                continue;
            }
            Range lastBeforeRange = rangeList.get(rangeList.size() - 2);
            axis.setRange(lastBeforeRange);
            // remove last/this change
            rangeList.remove(rangeList.size() - 1);
            rangeList.remove(rangeList.size() - 1);
            requestFocus();
        }
    }

    
    ///////////////////////////////////////////////////
    // Listener
    ///////////////////////////////////////////////////
    
    public void axisChanged(AxisChangeEvent event) {
        // add last range value
        List<Range> rangeList = null;
        for (Entry<Integer, ValueAxis> entry : axisIndexMap.entrySet()) {
            if (entry.getValue().equals(event.getAxis())) {
                rangeList = axisRangeHistoryMap.get(entry.getKey());
            }
        }
        if (rangeList != null) {
            if (rangeList.size() > 100) {
                List<Range> sublist = rangeList.subList(0, rangeList.size() - 50);
                sublist.clear();
            }
            rangeList.add(((ValueAxis)event.getAxis()).getRange());
        }
        
        // update domdin label
        if (event.getAxis().equals(domainAxis)) {
            updateDomainLabel();
        }
    }
    
    /**
     * Update X-Axis range label on top-panel
     */
    private void updateDomainLabel() {
        if (domainAxis instanceof DateAxis) {
            DateAxis dateAxis = (DateAxis) domainAxis;
            Date lowerDate = dateAxis.getMinimumDate();
            String label = DateUtility.parseStringWithDayOfWeek(lowerDate) + " - ";
            if (getWidth() > 1000) {
                Date upperDate = dateAxis.getMaximumDate();
                label = label + DateUtility.parseStringWithDayOfWeek(upperDate);
            }
            // String label = DateUtility.parseString(lowerDate, true) + " - " + DateUtility.parseString(upperDate, true);
            domainLabel.setText(label);
        } else {
            String label = domainAxis.getLowerBound() + " - " + domainAxis.getUpperBound();
            domainLabel.setText(label);
        }
    }
    
    
    ///////////////////////////////////////////////////
    // Utils
    ///////////////////////////////////////////////////
    
    private JButton createSmallButton(String title) {
        JButton button = new JButton(title);
        button.setPreferredSize(new Dimension(120, 20));
        button.setBackground(Color.WHITE);
        button.setFont(new Font("Arial", Font.PLAIN, 12));
        return button;
    }
    
    private String getStackTraceString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }

}
