package studio.chart;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class ChartSetting {

    // //////////////////////////////////////
    // Filed
    // //////////////////////////////////////

    // default values
    public static final int WINDOW_X_DEFAULT = 600;
    public static final int WINDOW_Y_DEFAULT = 350;
    public static final ChartTheme THEME_DEFAULT = ChartTheme.JFREE;
    
    public static final boolean NEW_FRAME_DEFAULT = false;
    public static final boolean CROSS_HAIR_DEFAULT = false;
    
    public static final double RANGE_DEFAILT = Double.NaN;
    public static final double GAP_DEFAULT = -5.0d;
    public static final boolean SEPARETE_LEGEND_DEFAULT = false;
    
    // Window
    private String title;
    private int xSize = WINDOW_X_DEFAULT;
    private int ySize = WINDOW_Y_DEFAULT;
    private ChartTheme theme = THEME_DEFAULT;
    
    // Multi-axis
    private double combinedGap = GAP_DEFAULT;
    private boolean separateLegend = SEPARETE_LEGEND_DEFAULT;

    // Axis
    private Map<AxisPosition, ChartAxisSetting> axisMap = new EnumMap<>(AxisPosition.class);

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    public ChartSetting() {
        initialize();
    }

    public void initialize() {
        title = null;
        xSize = WINDOW_X_DEFAULT;
        ySize = WINDOW_Y_DEFAULT;
        theme = THEME_DEFAULT;
        combinedGap = GAP_DEFAULT;
        separateLegend = SEPARETE_LEGEND_DEFAULT;

        // create each axis
        for (AxisPosition axisPosition : AxisPosition.values()) {
            axisMap.put(axisPosition, new ChartAxisSetting(axisPosition));
        }
    }

    /**
     * Return number of y axis, which is defined by existence of columnName(separator).
     * 
     * @return
     */
    public int getDatasetCount() {
        int datasetCount = 1;
        if (isY1Left2Enable()) {
            datasetCount++;
        }
        if (isY1RightEnable()) {
            datasetCount++;
        }
        if (isY2LeftEnable()) {
            datasetCount++;
        }
        if (isY2RightEnable()) {
            datasetCount++;
        }
        if (isY3LeftEnable()) {
            datasetCount++;
        }
        if (isY4LeftEnable()) {
            datasetCount++;
        }
        if (isY5LeftEnable()) {
            datasetCount++;
        }
        return datasetCount;
    }

    /**
     * Get plot count. Plot is different when use LEFT_2, LEFT_3, etc...
     * 
     * @return
     */
    public int getPlotCount() {
        if (isY5LeftEnable()) {
            return 5;
        } else if (isY4LeftEnable()) {
            return 4;
        } else if (isY3LeftEnable()) {
            return 3;
        } else if (isY2LeftEnable()) {
            return 2;
        }
        return 1;
    }

    /**
     * Judge additional axis is enable or not, by whether columnName is blank or not.
     * 
     * @return
     */
    public boolean isY1Left2Enable() {
        return StringUtils.isBlank(getAxisSetting(AxisPosition.Y1_LEFT2).getColumnName()) ? false : true;
    }

    public boolean isY1RightEnable() {
        return StringUtils.isBlank(getAxisSetting(AxisPosition.Y1_RIGHT).getColumnName()) ? false : true;
    }

    public boolean isY2LeftEnable() {
        return StringUtils.isBlank(getAxisSetting(AxisPosition.Y2_LEFT).getColumnName()) ? false : true;
    }

    public boolean isY2RightEnable() {
        return StringUtils.isBlank(getAxisSetting(AxisPosition.Y2_RIGHT).getColumnName()) ? false : true;
    }

    public boolean isY3LeftEnable() {
        return StringUtils.isBlank(getAxisSetting(AxisPosition.Y3_LEFT).getColumnName()) ? false : true;
    }

    public boolean isY3RightEnable() {
        return StringUtils.isBlank(getAxisSetting(AxisPosition.Y3_RIGHT).getColumnName()) ? false : true;
    }

    public boolean isY4LeftEnable() {
        return StringUtils.isBlank(getAxisSetting(AxisPosition.Y4_LEFT).getColumnName()) ? false : true;
    }

    public boolean isY4RightEnable() {
        return StringUtils.isBlank(getAxisSetting(AxisPosition.Y4_RIGHT).getColumnName()) ? false : true;
    }

    public boolean isY5LeftEnable() {
        return StringUtils.isBlank(getAxisSetting(AxisPosition.Y5_LEFT).getColumnName()) ? false : true;
    }

    public boolean isY5RightEnable() {
        return StringUtils.isBlank(getAxisSetting(AxisPosition.Y5_RIGHT).getColumnName()) ? false : true;
    }

    // //////////////////////////////////////
    // Getter & Setter
    // //////////////////////////////////////

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getxSize() {
        return xSize;
    }

    public void setxSize(int xSize) {
        this.xSize = xSize;
    }

    public int getySize() {
        return ySize;
    }

    public void setySize(int ySize) {
        this.ySize = ySize;
    }

    public ChartTheme getTheme() {
        return theme;
    }

    public void setTheme(ChartTheme theme) {
        this.theme = theme;
    }

    public ChartAxisSetting getAxisSetting(AxisPosition axisPosition) {
        return axisMap.get(axisPosition);
    }
    
    public double getCombinedGap() {
        return combinedGap;
    }
    
    public void setCombinedGap(double combinedGap) {
        this.combinedGap = combinedGap;
    }

    public boolean isSeparateLegend() {
        return separateLegend;
    }
    
    public void setSeparateLegend(boolean separateLegend) {
        this.separateLegend = separateLegend;
    }
    
    // //////////////////////////////////////
    // Inner Class
    // //////////////////////////////////////

    /**
     * Inner Class for each axis
     * 
     * Not use ITEMS
     * X1 - ChartType, colname, weight
     * Y1 - colname
     * Y1_LEFT2 - range, includeZero, markerLines, weight
     * YN_RIGHT - markerLines, weight
     * 
     */
    class ChartAxisSetting {
        // axis position
        private AxisPosition axisPosition;

        // Axis Label
        private String label;

        // Range
        private double rangeMin;
        private double rangeMax;

        // Include zero
        private boolean includeZero;

        // Marker Line values
        private List<Double> markerLines;

        // Chart Type
        private ChartType chartType;

        // Colname
        private String columnName;

        // weight
        private double weight;

        public ChartAxisSetting(AxisPosition axisPosition) {
            this.axisPosition = axisPosition;
            initialize();
        }

        public void initialize() {
            label = null;
            rangeMin = RANGE_DEFAILT;
            rangeMax = RANGE_DEFAILT;
            includeZero = false;
            markerLines = Collections.emptyList();
            chartType = null;
            columnName = null;
            weight = 1.0;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public double getRangeMin() {
            return rangeMin;
        }

        public void setRangeMin(double rangeMin) {
            this.rangeMin = rangeMin;
        }

        public double getRangeMax() {
            return rangeMax;
        }

        public void setRangeMax(double rangeMax) {
            this.rangeMax = rangeMax;
        }

        public boolean isIncludeZero() {
            return includeZero;
        }

        public void setIncludeZero(boolean includeZero) {
            this.includeZero = includeZero;
        }

        public List<Double> getMarkerLines() {
            return markerLines;
        }

        public void setMarkerLines(List<Double> markerLines) {
            this.markerLines = markerLines;
        }

        public ChartType getChartType() {
            return chartType;
        }

        public void setChartType(ChartType chartType) {
            this.chartType = chartType;
        }

        public String getColumnName() {
            return columnName;
        }

        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }

        public double getWeight() {
            return weight;
        }

        public void setWeight(double weight) {
            this.weight = weight;
        }

        public AxisPosition getAxisPosition() {
            return axisPosition;
        }
    }
}
