package studio.chart;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class SmartChartSetting {

    // //////////////////////////////////////
    // Filed
    // //////////////////////////////////////

    private static final double RANGE_DEFAILT = Double.NaN;

    public static final int WINDOW_X_DEFAULT = 600;
    public static final int WINDOW_Y_DEFAULT = 350;

    // Window
    private String title;
    private int xSize = WINDOW_X_DEFAULT;
    private int ySize = WINDOW_Y_DEFAULT;
    private ChartTheme theme;

    // Axis Label
    private String xLabel;
    private String y1Label;
    // private String y1LeftLabel; // Label is only one for axis
    private String y1RightLabel;
    private String y2LeftLabel;
    private String y2RightLabel;
    private String y3LeftLabel;
    private String y4LeftLabel;

    // Range
    private double xMin;
    private double xMax;
    private double y1Min;
    private double y1Max;
    //private double y1LeftMin;
    //private double y1LeftMax;
    private double y1RightMin;
    private double y1RightMax;
    private double y2LeftMin;
    private double y2LeftMax;
    private double y2RightMin;
    private double y2RightMax;
    private double y3LeftMin;
    private double y3LeftMax;
    private double y4LeftMin;
    private double y4LeftMax;

    // Include zero
    private boolean xIncludeZero;
    private boolean y1IncludeZero;
    // private boolean y1LeftIncludeZero;
    private boolean y1RightIncludeZero;
    private boolean y2LeftIncludeZero;
    private boolean y2RightIncludeZero;
    private boolean y3LeftIncludeZero;
    private boolean y4LeftIncludeZero;
    
    // Marker Line values
    private List<Double> x1MarkerLines;
    private List<Double>  y1MarkerLines;
    //
    //
    private List<Double>  y2LeftMarkerLines;
    //
    private List<Double>  y3LeftMarkerLines;
    private List<Double>  y4LeftMarkerLines;
    
    // Chart Type
    private ChartType y1Chart;
    private ChartType y1LeftChart;
    private ChartType y1RightChart;
    private ChartType y2LeftChart;
    private ChartType y2RightChart;
    private ChartType y3LeftChart;
    private ChartType y4LeftChart;

    // Colname
    private String y1LeftColumnName;
    private String y1RightColumnName;
    private String y2LeftColumnName;
    private String y2RightColumnName;
    private String y3LeftColumnName;
    private String y4LeftColumnName;
    
    // weight
    private double y1Weight = 1.0;
    // private double y1LeftWeight = 1.0;
    // private double y1RightWeight = 1.0;
    private double y2LeftWeight = 1.0;
    // private double y2RightWeight = 1.0;
    private double y3LeftWeight = 1.0;
    private double y4LeftWeight = 1.0;

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    public SmartChartSetting() {
        initialize();
    }

    public void initialize() {
        title = null;
        xSize = WINDOW_X_DEFAULT;
        ySize = WINDOW_Y_DEFAULT;

        xLabel = null;
        y1Label = null;
        y1RightLabel = null;
        y2LeftLabel = null;
        y2RightLabel = null;
        y3LeftLabel = null;
        y4LeftLabel = null;

        xMin = RANGE_DEFAILT;
        xMax = RANGE_DEFAILT;
        y1Min = RANGE_DEFAILT;
        y1Max = RANGE_DEFAILT;
        y1RightMin = RANGE_DEFAILT;
        y1RightMax = RANGE_DEFAILT;
        y2LeftMin = RANGE_DEFAILT;
        y2LeftMax = RANGE_DEFAILT;
        y2RightMin = RANGE_DEFAILT;
        y2RightMax = RANGE_DEFAILT;
        y3LeftMin = RANGE_DEFAILT;
        y3LeftMax = RANGE_DEFAILT;
        y4LeftMin = RANGE_DEFAILT;
        y4LeftMax = RANGE_DEFAILT;

        xIncludeZero = false;
        y1IncludeZero = false;
        y1RightIncludeZero = false;
        y2LeftIncludeZero = false;
        y2RightIncludeZero = false;
        y3LeftIncludeZero = false;
        y4LeftIncludeZero = false;
        
        x1MarkerLines = Collections.emptyList();
        y1MarkerLines = Collections.emptyList();
        y2LeftMarkerLines = Collections.emptyList();
        y3LeftMarkerLines = Collections.emptyList();
        y4LeftMarkerLines = Collections.emptyList();
        
        y1Chart = null;
        y1LeftChart = null;
        y1RightChart = null;
        y2LeftChart = null;
        y2RightChart = null;
        y3LeftChart = null;
        y4LeftChart = null;

        y1LeftColumnName = null;
        y1RightColumnName = null;
        y2LeftColumnName = null;
        y2RightColumnName = null;
        y3LeftColumnName = null;
        y4LeftColumnName = null;

        y1Weight = 1.0;
        y2LeftWeight = 1.0;
        y3LeftWeight = 1.0;
        y4LeftWeight = 1.0;
    }

    /**
     * Return number of y axis, which is defined by existence of columnName(separator).
     * 
     * @return
     */
    public int getDatasetCount() {
        int datasetCount = 1;
        if (isY1LeftEnable()) {
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
        return datasetCount;
    }

    /**
     * Get plot count. Plot is different when use LEFT_2, LEFT_3, etc...
     * 
     * @return
     */
    public int getPlotCount() {
        if (isY4LeftEnable()) {
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
    public boolean isY1LeftEnable() {
        return !StringUtils.isBlank(y1LeftColumnName) ? true: false;
    }
    public boolean isY1RightEnable() {
        return !StringUtils.isBlank(y1RightColumnName) ? true: false;
    }
    public boolean isY2LeftEnable() {
        return !StringUtils.isBlank(y2LeftColumnName) ? true: false;
    }
    public boolean isY2RightEnable() {
        return !StringUtils.isBlank(y2RightColumnName) ? true: false;
    }
    public boolean isY3LeftEnable() {
        return !StringUtils.isBlank(y3LeftColumnName) ? true: false;
    }
    public boolean isY4LeftEnable() {
        return !StringUtils.isBlank(y4LeftColumnName) ? true: false;
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

    public String getxLabel() {
        return xLabel;
    }

    public void setxLabel(String xLabel) {
        this.xLabel = xLabel;
    }

    public String getY1Label() {
        return y1Label;
    }

    public void setY1Label(String y1Label) {
        this.y1Label = y1Label;
    }

    public String getY1RightLabel() {
        return y1RightLabel;
    }

    public void setY1RightLabel(String y1RightLabel) {
        this.y1RightLabel = y1RightLabel;
    }

    public String getY2LeftLabel() {
        return y2LeftLabel;
    }

    public void setY2LeftLabel(String y2LeftLabel) {
        this.y2LeftLabel = y2LeftLabel;
    }

    public String getY2RightLabel() {
        return y2RightLabel;
    }

    public void setY2RightLabel(String y2RightLabel) {
        this.y2RightLabel = y2RightLabel;
    }

    public String getY3LeftLabel() {
        return y3LeftLabel;
    }

    public void setY3LeftLabel(String y3LeftLabel) {
        this.y3LeftLabel = y3LeftLabel;
    }

    public String getY4LeftLabel() {
        return y4LeftLabel;
    }

    public void setY4LeftLabel(String y4LeftLabel) {
        this.y4LeftLabel = y4LeftLabel;
    }

    public double getxMin() {
        return xMin;
    }

    public void setxMin(double xMin) {
        this.xMin = xMin;
    }

    public double getxMax() {
        return xMax;
    }

    public void setxMax(double xMax) {
        this.xMax = xMax;
    }

    public double getY1Min() {
        return y1Min;
    }

    public void setY1Min(double y1Min) {
        this.y1Min = y1Min;
    }

    public double getY1Max() {
        return y1Max;
    }

    public void setY1Max(double y1Max) {
        this.y1Max = y1Max;
    }

    public double getY1RightMin() {
        return y1RightMin;
    }

    public void setY1RightMin(double y1RightMin) {
        this.y1RightMin = y1RightMin;
    }

    public double getY1RightMax() {
        return y1RightMax;
    }

    public void setY1RightMax(double y1RightMax) {
        this.y1RightMax = y1RightMax;
    }

    public double getY2LeftMin() {
        return y2LeftMin;
    }

    public void setY2LeftMin(double y2LeftMin) {
        this.y2LeftMin = y2LeftMin;
    }

    public double getY2LeftMax() {
        return y2LeftMax;
    }

    public void setY2LeftMax(double y2LeftMax) {
        this.y2LeftMax = y2LeftMax;
    }

    public double getY2RightMin() {
        return y2RightMin;
    }

    public void setY2RightMin(double y2RightMin) {
        this.y2RightMin = y2RightMin;
    }

    public double getY2RightMax() {
        return y2RightMax;
    }

    public void setY2RightMax(double y2RightMax) {
        this.y2RightMax = y2RightMax;
    }

    public double getY3LeftMin() {
        return y3LeftMin;
    }

    public void setY3LeftMin(double y3LeftMin) {
        this.y3LeftMin = y3LeftMin;
    }

    public double getY3LeftMax() {
        return y3LeftMax;
    }

    public void setY3LeftMax(double y3LeftMax) {
        this.y3LeftMax = y3LeftMax;
    }

    public double getY4LeftMin() {
        return y4LeftMin;
    }

    public void setY4LeftMin(double y4LeftMin) {
        this.y4LeftMin = y4LeftMin;
    }

    public double getY4LeftMax() {
        return y4LeftMax;
    }

    public void setY4LeftMax(double y4LeftMax) {
        this.y4LeftMax = y4LeftMax;
    }
    
    public boolean isxIncludeZero() {
        return xIncludeZero;
    }

    public void setxIncludeZero(boolean xIncludeZero) {
        this.xIncludeZero = xIncludeZero;
    }

    public boolean isY1IncludeZero() {
        return y1IncludeZero;
    }

    public void setY1IncludeZero(boolean y1IncludeZero) {
        this.y1IncludeZero = y1IncludeZero;
    }

    public boolean isY1RightIncludeZero() {
        return y1RightIncludeZero;
    }

    public void setY1RightIncludeZero(boolean y1RightIncludeZero) {
        this.y1RightIncludeZero = y1RightIncludeZero;
    }

    public boolean isY2LeftIncludeZero() {
        return y2LeftIncludeZero;
    }

    public void setY2LeftIncludeZero(boolean y2LeftIncludeZero) {
        this.y2LeftIncludeZero = y2LeftIncludeZero;
    }

    public boolean isY2RightIncludeZero() {
        return y2RightIncludeZero;
    }

    public void setY2RightIncludeZero(boolean y2RightIncludeZero) {
        this.y2RightIncludeZero = y2RightIncludeZero;
    }

    public boolean isY3LeftIncludeZero() {
        return y3LeftIncludeZero;
    }

    public void setY3LeftIncludeZero(boolean y3LeftIncludeZero) {
        this.y3LeftIncludeZero = y3LeftIncludeZero;
    }

    public boolean isY4LeftIncludeZero() {
        return y4LeftIncludeZero;
    }

    public void setY4LeftIncludeZero(boolean y4LeftIncludeZero) {
        this.y4LeftIncludeZero = y4LeftIncludeZero;
    }
    
    public List<Double> getX1MarkerLines() {
        return x1MarkerLines;
    }

    public void setX1MarkerLines(List<Double> x1MarkerLines) {
        this.x1MarkerLines = x1MarkerLines;
    }

    public List<Double> getY1MarkerLines() {
        return y1MarkerLines;
    }

    public void setY1MarkerLines(List<Double> y1MarkerLines) {
        this.y1MarkerLines = y1MarkerLines;
    }

    public List<Double> getY2LeftMarkerLines() {
        return y2LeftMarkerLines;
    }

    public void setY2LeftMarkerLines(List<Double> y2LeftMarkerLines) {
        this.y2LeftMarkerLines = y2LeftMarkerLines;
    }

    public List<Double> getY3LeftMarkerLines() {
        return y3LeftMarkerLines;
    }

    public void setY3LeftMarkerLines(List<Double> y3LeftMarkerLines) {
        this.y3LeftMarkerLines = y3LeftMarkerLines;
    }

    public List<Double> getY4LeftMarkerLines() {
        return y4LeftMarkerLines;
    }

    public void setY4LeftMarkerLines(List<Double> y4LeftMarkerLines) {
        this.y4LeftMarkerLines = y4LeftMarkerLines;
    }
    
    public ChartType getY1Chart() {
        return y1Chart;
    }

    public void setY1Chart(ChartType y1Chart) {
        this.y1Chart = y1Chart;
    }

    public ChartType getY1LeftChart() {
        return y1LeftChart;
    }

    public void setY1LeftChart(ChartType y1LeftChart) {
        this.y1LeftChart = y1LeftChart;
    }

    public ChartType getY1RightChart() {
        return y1RightChart;
    }

    public void setY1RightChart(ChartType y1RightChart) {
        this.y1RightChart = y1RightChart;
    }

    public ChartType getY2LeftChart() {
        return y2LeftChart;
    }

    public void setY2LeftChart(ChartType y2LeftChart) {
        this.y2LeftChart = y2LeftChart;
    }

    public ChartType getY2RightChart() {
        return y2RightChart;
    }

    public void setY2RightChart(ChartType y2RightChart) {
        this.y2RightChart = y2RightChart;
    }

    public ChartType getY3LeftChart() {
        return y3LeftChart;
    }

    public void setY3LeftChart(ChartType y3LeftChart) {
        this.y3LeftChart = y3LeftChart;
    }

    public ChartType getY4LeftChart() {
        return y4LeftChart;
    }

    public void setY4LeftChart(ChartType y4LeftChart) {
        this.y4LeftChart = y4LeftChart;
    }

    public String getY1LeftColumnName() {
        return y1LeftColumnName;
    }

    public void setY1LeftColumnName(String y1LeftColumnName) {
        this.y1LeftColumnName = y1LeftColumnName;
    }

    public String getY1RightColumnName() {
        return y1RightColumnName;
    }

    public void setY1RightColumnName(String y1RightColumnName) {
        this.y1RightColumnName = y1RightColumnName;
    }

    public String getY2LeftColumnName() {
        return y2LeftColumnName;
    }

    public void setY2LeftColumnName(String y2LeftColumnName) {
        this.y2LeftColumnName = y2LeftColumnName;
    }

    public String getY2RightColumnName() {
        return y2RightColumnName;
    }

    public void setY2RightColumnName(String y2RightColumnName) {
        this.y2RightColumnName = y2RightColumnName;
    }

    public String getY3LeftColumnName() {
        return y3LeftColumnName;
    }

    public void setY3LeftColumnName(String y3LeftColumnName) {
        this.y3LeftColumnName = y3LeftColumnName;
    }

    public String getY4LeftColumnName() {
        return y4LeftColumnName;
    }

    public void setY4LeftColumnName(String y4LeftColumnName) {
        this.y4LeftColumnName = y4LeftColumnName;
    }

    public double getY1Weight() {
        return y1Weight;
    }

    public void setY1Weight(double y1Weight) {
        this.y1Weight = y1Weight;
    }

    public double getY2LeftWeight() {
        return y2LeftWeight;
    }

    public void setY2LeftWeight(double y2LeftWeight) {
        this.y2LeftWeight = y2LeftWeight;
    }

    public double getY3LeftWeight() {
        return y3LeftWeight;
    }

    public void setY3LeftWeight(double y3LeftWeight) {
        this.y3LeftWeight = y3LeftWeight;
    }

    public double getY4LeftWeight() {
        return y4LeftWeight;
    }

    public void setY4LeftWeight(double y4LeftWeight) {
        this.y4LeftWeight = y4LeftWeight;
    }

}

