package studio.chart;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

/**
 * Panel for Additional Axis
 */
public abstract class AddtionalAxisPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    
    protected String itemName;
    protected AxisPosition axisPosition;
    protected Color panelColor;
    
    public AddtionalAxisPanel(String itemName, AxisPosition axisPosition, Color panelColor) {
        this.itemName = itemName;
        this.axisPosition = axisPosition;
        this.panelColor = panelColor;
    }
    
    public abstract void clearSetting();
    
    public abstract void setupColumnNameCombo(List<String> items );

    /**
     * Set background color to TitledBorder 
     * @param border
     * @param color
     */
    private static void setBackgroundBorderLabel(TitledBorder border, Color color) {
        try {
            Class<TitledBorder> clazz = TitledBorder.class;
            Field borderLabelField = clazz.getDeclaredField("label");
            borderLabelField.setAccessible(true);
            JLabel label = (JLabel) borderLabelField.get(border);
            label.setBackground(color);
            label.setOpaque(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // //////////////////////////////////////
    // Implement Class
    // //////////////////////////////////////
    
    /**
     * Panel for Additional Axis Default
     */
    public static class AddtionalAxisPanelDefault extends AddtionalAxisPanel {
        private static final long serialVersionUID = 1L;
        
        protected JTextField labelField = new GuideTextField("Label", SmartChartPanel.TEXT_FIELD_COLUMNS_NORMAL);
        protected JTextField minField = new GuideTextField("Min", SmartChartPanel.TEXT_FIELD_COLUMNS_NORMAL);
        protected JTextField maxField = new GuideTextField("Max", SmartChartPanel.TEXT_FIELD_COLUMNS_NORMAL);
        protected JTextField lengthField = new GuideTextField("Min Length", SmartChartPanel.TEXT_FIELD_COLUMNS_NORMAL);
        protected JTextField unitField = new GuideTextField("Tick Unit", SmartChartPanel.TEXT_FIELD_COLUMNS_NORMAL);
        protected JCheckBox includeZeroCheckbox = new JCheckBox("Inc. 0", false);
        protected JTextField markerLineField = new GuideTextField("Markers(,)", SmartChartPanel.TEXT_FIELD_COLUMNS_NORMAL);
        protected JComboBox<ChartType> chartCombo = new JComboBox<>(ChartType.values());
        protected JComboBox<String> columnNameCombo = new JComboBox<>(new String[]{SmartChartPanel.DEFAULT_COLUMN_NAME});
        protected JComboBox<Color> colorCombo = new JComboBox<>(SeriesColor.COLORS);
        protected JTextField weightField = new GuideTextField("weight", "1", SmartChartPanel.TEXT_FIELD_COLUMNS_SHORT);
        
        public AddtionalAxisPanelDefault(String itemName, AxisPosition axisPosition, Color color) {
            super(itemName, axisPosition, color);
            
            // Component
            chartCombo.setPreferredSize(new Dimension(100, 25));
            columnNameCombo.setPreferredSize(new Dimension(120, 25));
            colorCombo.setPreferredSize(new Dimension(80, 25));
            if (axisPosition == AxisPosition.X1) {
                minField = new GuideTextField("Min", SmartChartPanel.TEXT_FIELD_COLUMNS_LONGLONG);
                maxField = new GuideTextField("Max", SmartChartPanel.TEXT_FIELD_COLUMNS_LONGLONG);
                lengthField = new GuideTextField("Domain Length", SmartChartPanel.TEXT_FIELD_COLUMNS_LONGLONG);
            }
            
            // chartCombo.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
            // columnNameCombo.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
            // colorCombo.setFont(new Font("Arial", Font.PLAIN, 11));
            
            // initialize panel
            this.add(labelField);
            this.add(minField);
            this.add(maxField);
            this.add(lengthField);
            this.add(unitField);
            this.add(includeZeroCheckbox);
            this.add(markerLineField);
            this.add(chartCombo);
            this.add(columnNameCombo);
            this.add(colorCombo);
            this.add(weightField);
            
            // panel layout
            TitledBorder border = new TitledBorder(itemName + " Axis");
            setBorder(border);
            if (color != null) {
                setBackgroundBorderLabel(border, color);
            }
            // layout
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
            layout.setConstraints(lengthField, gbc);
            gbc.gridx++;
            layout.setConstraints(unitField, gbc);
            gbc.gridx++;
            layout.setConstraints(includeZeroCheckbox, gbc);
            gbc.gridx++;
            layout.setConstraints(markerLineField, gbc);
            gbc.gridx++;
            layout.setConstraints(chartCombo, gbc);
            gbc.gridx++;
            layout.setConstraints(columnNameCombo, gbc);
            gbc.gridx++;
            layout.setConstraints(colorCombo, gbc);
            gbc.gridx++;
            layout.setConstraints(weightField, gbc);
        }
        
        @Override
        public void clearSetting() {
            ((GuideTextField) labelField).clearText("");
            ((GuideTextField) minField).clearText("");
            ((GuideTextField) maxField).clearText("");
            ((GuideTextField) lengthField).clearText("");
            ((GuideTextField) unitField).clearText("");
            includeZeroCheckbox.setSelected(false);
            ((GuideTextField) markerLineField).clearText("");
            chartCombo.setSelectedIndex(0);
            columnNameCombo.removeAllItems();
            columnNameCombo.addItem(SmartChartPanel.DEFAULT_COLUMN_NAME);
            colorCombo.setSelectedIndex(0);
            ((GuideTextField) weightField).clearText("1");
        }
        
        @Override
        public void setupColumnNameCombo(List<String> items ) {
            columnNameCombo.removeAllItems();
            for (String columnName : items) {
                columnNameCombo.addItem(columnName);
            }
        }
        
        public void disableForRightAxis() {
            weightField.setEnabled(false);
            markerLineField.setEnabled(false);
            weightField.setBackground(Color.GRAY);
            markerLineField.setBackground(Color.GRAY);
        }
    }
    

    /**
     * Panel for Y1 Left Second Axis
     */
    public static class AddtionalAxisPanelY1Left extends AddtionalAxisPanel {
        protected static final long serialVersionUID = 1L;
        
        private static final JLabel BLANK_LABEL = new JLabel("     ");
        
        protected JLabel label1 = new JLabel(" Left-1 ");
        protected JLabel label2 = new JLabel(" Left-2 ");
        protected JLabel label3 = new JLabel(" Left-3 ");
        protected JLabel label4 = new JLabel(" Left-4 ");
        
        protected JComboBox<ChartType> chartCombo1 = new JComboBox<>(ChartType.values());
        protected JComboBox<ChartType> chartCombo2 = new JComboBox<>(ChartType.values());
        protected JComboBox<ChartType> chartCombo3 = new JComboBox<>(ChartType.values());
        protected JComboBox<ChartType> chartCombo4 = new JComboBox<>(ChartType.values());

        protected JComboBox<String> columnNameCombo1 = new JComboBox<>(new String[]{SmartChartPanel.DEFAULT_COLUMN_NAME});
        protected JComboBox<String> columnNameCombo2 = new JComboBox<>(new String[]{SmartChartPanel.DEFAULT_COLUMN_NAME});
        protected JComboBox<String> columnNameCombo3 = new JComboBox<>(new String[]{SmartChartPanel.DEFAULT_COLUMN_NAME});
        protected JComboBox<String> columnNameCombo4 = new JComboBox<>(new String[]{SmartChartPanel.DEFAULT_COLUMN_NAME});
        
        protected JComboBox<Color> colorCombo1 = new JComboBox<>(SeriesColor.COLORS);
        protected JComboBox<Color> colorCombo2 = new JComboBox<>(SeriesColor.COLORS);
        protected JComboBox<Color> colorCombo3 = new JComboBox<>(SeriesColor.COLORS);
        protected JComboBox<Color> colorCombo4 = new JComboBox<>(SeriesColor.COLORS);
        
        public AddtionalAxisPanelY1Left(String itemName, AxisPosition axisPosition, Color color) {
            super(itemName, axisPosition, color);
            
            // Component
            chartCombo1.setPreferredSize(new Dimension(100, 25));
            chartCombo2.setPreferredSize(new Dimension(100, 25));
            chartCombo3.setPreferredSize(new Dimension(100, 25));
            chartCombo4.setPreferredSize(new Dimension(100, 25));
            
            columnNameCombo1.setPreferredSize(new Dimension(120, 25));
            columnNameCombo2.setPreferredSize(new Dimension(120, 25));
            columnNameCombo3.setPreferredSize(new Dimension(120, 25));
            columnNameCombo4.setPreferredSize(new Dimension(120, 25));
            
            colorCombo1.setPreferredSize(new Dimension(80, 25));
            colorCombo2.setPreferredSize(new Dimension(80, 25));
            colorCombo3.setPreferredSize(new Dimension(80, 25));
            colorCombo4.setPreferredSize(new Dimension(80, 25));
            
            // initialize panel
            this.add(BLANK_LABEL);
            this.add(label1);
            this.add(label2);
            this.add(label3);
            this.add(label4);
            this.add(chartCombo1);
            this.add(chartCombo2);
            this.add(chartCombo3);
            this.add(chartCombo4);
            this.add(columnNameCombo1);
            this.add(columnNameCombo2);
            this.add(columnNameCombo3);
            this.add(columnNameCombo4);
            this.add(colorCombo1);
            this.add(colorCombo2);
            this.add(colorCombo3);
            this.add(colorCombo4);
            
            // panel layout
            TitledBorder border = new TitledBorder(itemName + " Axis");
            setBorder(border);
            if (color != null) {
                setBackgroundBorderLabel(border, color);
            }
            
            // layout
            GridBagLayout layout = new GridBagLayout();
            this.setLayout(layout);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(1, 2, 1, 2);
            
            gbc.gridx = 0;
            gbc.gridy = 0;
            layout.setConstraints(label1, gbc);
            gbc.gridx++;
            layout.setConstraints(chartCombo1, gbc);
            gbc.gridx++;
            layout.setConstraints(columnNameCombo1, gbc);
            gbc.gridx++;
            layout.setConstraints(colorCombo1, gbc);
            gbc.gridx++;
            layout.setConstraints(BLANK_LABEL, gbc);
            gbc.gridx++;
            layout.setConstraints(label2, gbc);
            gbc.gridx++;
            layout.setConstraints(chartCombo2, gbc);
            gbc.gridx++;
            layout.setConstraints(columnNameCombo2, gbc);
            gbc.gridx++;
            layout.setConstraints(colorCombo2, gbc);
            gbc.gridy++;
            gbc.gridx = 0;
            layout.setConstraints(label3, gbc);
            gbc.gridx++;
            layout.setConstraints(chartCombo3, gbc);
            gbc.gridx++;
            layout.setConstraints(columnNameCombo3, gbc);
            gbc.gridx++;
            layout.setConstraints(colorCombo3, gbc);
            gbc.gridx++;
            layout.setConstraints(BLANK_LABEL, gbc);
            gbc.gridx++;
            layout.setConstraints(label4, gbc);
            gbc.gridx++;
            layout.setConstraints(chartCombo4, gbc);
            gbc.gridx++;
            layout.setConstraints(columnNameCombo4, gbc);
            gbc.gridx++;
            layout.setConstraints(colorCombo4, gbc);
        }
        
        @Override
        public void clearSetting() {
            chartCombo1.setSelectedIndex(0);
            chartCombo2.setSelectedIndex(0);
            chartCombo3.setSelectedIndex(0);
            chartCombo4.setSelectedIndex(0);
            setupColumnNameCombo(new ArrayList<>(Arrays.asList(SmartChartPanel.DEFAULT_COLUMN_NAME)));
            
            colorCombo1.setSelectedIndex(0);
            colorCombo2.setSelectedIndex(0);
            colorCombo3.setSelectedIndex(0);
            colorCombo4.setSelectedIndex(0);
        }
        
        @Override
        public void setupColumnNameCombo(List<String> items ) {
            columnNameCombo1.removeAllItems();
            columnNameCombo2.removeAllItems();
            columnNameCombo3.removeAllItems();
            columnNameCombo4.removeAllItems();
            for (String columnName : items) {
                columnNameCombo1.addItem(columnName);
                columnNameCombo2.addItem(columnName);
                columnNameCombo3.addItem(columnName);
                columnNameCombo4.addItem(columnName);
            }
        }
    }
}
