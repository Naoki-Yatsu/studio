package studio.chart;

import java.awt.Color;
import java.util.ArrayList;
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
    
    public static final boolean TOP_BUTTON_DEFAULT = true;
    public static final boolean REVERSE_RENDERING_DEFAULT = true;
    public static final boolean CROSS_HAIR_DEFAULT = false;
    public static final boolean SCROLL_BAR_DEFAULT = true;
    public static final boolean SCROLL_ADJUST_DEFAULT = true;
    public static final boolean TIMELINE_DEFAULT = false;
    
    public static final double RANGE_DEFAILT = Double.NaN;
    public static final double RANGE_LENGTH_FIXED = Double.NEGATIVE_INFINITY;
    
    public static final double GAP_DEFAULT = -5.0d;
    public static final boolean SEPARETE_LEGEND_DEFAULT = false;
    
    // Window
    private String title;
    private int xSize = WINDOW_X_DEFAULT;
    private int ySize = WINDOW_Y_DEFAULT;
    private ChartTheme theme = THEME_DEFAULT;
    
    private boolean topBUtton = TOP_BUTTON_DEFAULT;
    private boolean reverseRendering = REVERSE_RENDERING_DEFAULT;
    private boolean crossHair = CROSS_HAIR_DEFAULT;
    private boolean crossHairCursor = CROSS_HAIR_DEFAULT;
    private boolean scrollBar = SCROLL_BAR_DEFAULT;
    private boolean scrollAdjust = SCROLL_ADJUST_DEFAULT;
    
    private boolean useTimeline = TIMELINE_DEFAULT;
    private DayOfWeekType timelineFromDay;
    private int timelineFromTime;
    private DayOfWeekType timelineToDay;
    private int timelineToTime;
    
    
    // Multi-axis
    private double combinedGap = GAP_DEFAULT;
    private boolean separateLegend = SEPARETE_LEGEND_DEFAULT;

    // Axis Map
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

        topBUtton = TOP_BUTTON_DEFAULT;
        reverseRendering = REVERSE_RENDERING_DEFAULT;
        crossHair = CROSS_HAIR_DEFAULT;
        scrollBar = SCROLL_BAR_DEFAULT;
        scrollAdjust = SCROLL_ADJUST_DEFAULT;
        
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
        for (AxisPosition axisPosition : AxisPosition.AXIS_SUB_ALL) {
            if (isAxisEnable(axisPosition)) {
                datasetCount++;
            }
        }
        return datasetCount;
    }

    /**
     * Get plot count. Plot is different when use LEFT_2, LEFT_3, etc...
     * 
     * @return
     */
    public int getPlotCount() {
        // check only left axis
        if (isAxisEnable(AxisPosition.Y5_LEFT)) {
            return 5;
        } else if (isAxisEnable(AxisPosition.Y4_LEFT)) {
            return 4;
        } else if (isAxisEnable(AxisPosition.Y3_LEFT)) {
            return 3;
        } else if (isAxisEnable(AxisPosition.Y2_LEFT)) {
            return 2;
        }
        return 1;
    }

    /**
     * Judge additional axis is enable or not, by whether columnName is blank or not.
     * 
     * @return
     */
    public boolean isAxisEnable(AxisPosition axisPosition) {
        if (axisPosition == AxisPosition.X1 || axisPosition == AxisPosition.Y1) {
            return true;
        } else {
            return StringUtils.isBlank(getAxisSetting(axisPosition).getColumnName()) ? false : true;
        }
    }

   
    /**
     * Get range length list of all valid Y-axes
     * @return
     */
    public List<Double> getRangeLengthList() {
        List<Double> lengthList = new ArrayList<>();
        // Y1 add always
        lengthList.add(getAxisSetting(AxisPosition.Y1).getRangeLength());
        
        // without Y1_LEFT1-4
        for (AxisPosition axisPosition : AxisPosition.AXIS_SUB_WITHOUT_Y1LEFT) {
            if (isAxisEnable(axisPosition)) {
                if (isFixedRange(axisPosition)) {
                    lengthList.add(RANGE_LENGTH_FIXED); 
                } else {
                    lengthList.add(getAxisSetting(axisPosition).getRangeLength()); 
                }
            }
        }
        return lengthList;
    }
    
    private boolean isFixedRange(AxisPosition axisPosition) {
        // If both of range min/max are set, the range is fixed.
        if (!Double.isNaN(getAxisSetting(axisPosition).getRangeMin()) && !Double.isNaN(getAxisSetting(axisPosition).getRangeMax())) {
            return true;
        }
        return false;
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
    
    public boolean isTopButton() {
        return topBUtton;
    }
    public void setTopButton(boolean topBUtton) {
        this.topBUtton = topBUtton;
    }
    public boolean isReverseRendering() {
        return reverseRendering;
    }
    public void setReverseRendering(boolean reverseRendering) {
        this.reverseRendering = reverseRendering;
    }
    public boolean isCrossHair() {
        return crossHair;
    }
    public void setCrossHair(boolean crossHair) {
        this.crossHair = crossHair;
    }
    public boolean isCrossHairCursor() {
        return crossHairCursor;
    }
    public void setCrossHairCursor(boolean crossHairCursor) {
        this.crossHairCursor = crossHairCursor;
    }
    public boolean isScrollBar() {
        return scrollBar;
    }
    public void setScrollBar(boolean scrollBar) {
        this.scrollBar = scrollBar;
    }
    public boolean isScrollAdjust() {
        return scrollAdjust;
    }
    public void setScrollAdjust(boolean scrollAdjust) {
        this.scrollAdjust = scrollAdjust;
    }
    
    public boolean isUseTimeline() {
        return useTimeline;
    }
    public void setUseTimeline(boolean useTimeline) {
        this.useTimeline = useTimeline;
    }
    public DayOfWeekType getTimelineFromDay() {
        return timelineFromDay;
    }
    public void setTimelineFromDay(DayOfWeekType timelineFromDay) {
        this.timelineFromDay = timelineFromDay;
    }
    public int getTimelineFromTime() {
        return timelineFromTime;
    }
    public void setTimelineFromTime(int timelineFromTime) {
        this.timelineFromTime = timelineFromTime;
    }
    public DayOfWeekType getTimelineToDay() {
        return timelineToDay;
    }
    public void setTimelineToDay(DayOfWeekType timelineToDay) {
        this.timelineToDay = timelineToDay;
    }
    public int getTimelineToTime() {
        return timelineToTime;
    }
    public void setTimelineToTime(int timelineToTime) {
        this.timelineToTime = timelineToTime;
    }
    
    // //////////////////////////////////////
    // Inner Class
    // //////////////////////////////////////

    /**
     * Inner Class for each axis
     * 
     * [Not use ITEMS]
     * X1 - ChartType, columnName, weight
     * Y1 - columnName
     * Y1_LEFT - range, includeZero, markerLines, weight
     * YN_RIGHT - markerLines, weight
     */
    class ChartAxisSetting {
        // axis position
        private final AxisPosition axisPosition;

        // Axis Label
        private String label;

        // Range
        private double rangeMin;
        private double rangeMax;
        private double rangeLength;
        private double tickUnit;

        // Include zero
        private boolean includeZero;

        // Marker Line values
        private List<Double> markerLines;

        // Chart Type
        private ChartType chartType;

        // Column Name
        private String columnName;

        // series color
        private Color color;
        
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
            rangeLength = RANGE_DEFAILT;
            includeZero = false;
            markerLines = Collections.emptyList();
            chartType = null;
            columnName = null;
            weight = 1.0;
            color = null;
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
        public double getRangeLength() {
            return rangeLength;
        }
        public void setRangeLength(double rangeLength) {
            this.rangeLength = rangeLength;
        }
        public double getTickUnit() {
            return tickUnit;
        }
        public void setTickUnit(double tickUnit) {
            this.tickUnit = tickUnit;
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
        public Color getColor() {
            return color;
        }
        public void setColor(Color color) {
            this.color = color;
        }
        public double getWeight() {
            return weight;
        }
        public void setWeight(double weight) {
            this.weight = weight;
        }

        // Series color
        public void setSeriesColor(SeriesColor color) {
            if (!color.isAuto()) {
                // Don't set color for Auto
                this.color = color;
            } else {
                this.color = null;
            }
        }
        
        public AxisPosition getAxisPosition() {
            return axisPosition;
        }
    }
}
