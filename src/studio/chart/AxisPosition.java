package studio.chart;

/**
 * Axis Position
 */
public enum AxisPosition {
    
    X1(0, "X"),
    Y1(0, "Y1"),
    Y1_LEFT2(0, "Y1 Left2"),
    Y1_RIGHT(0, "Y1 Right"),
    Y2_LEFT(1, "Y2 Left"),
    Y2_RIGHT(1, "Y2 Right"),
    Y3_LEFT(2, "Y3 Left"),
    Y3_RIGHT(2, "Y3 Right"),
    Y4_LEFT(3, "Y4 Left"),
    Y4_RIGHT(3, "Y4 Right"),
    Y5_LEFT(4, "Y5 Left"),
    Y5_RIGHT(4, "Y5 Right");
    
    /** Index of plot in Plot class*/
    private int plotIndex;
    private String name;
    private AxisPosition(int plotIndex, String name) {
        this.plotIndex = plotIndex;
        this.name = name;
    }
    public int getPlotIndex() {
        return plotIndex;
    }
    public String getName() {
        return name;
    }
}
