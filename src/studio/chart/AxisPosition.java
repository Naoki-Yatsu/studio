package studio.chart;

/**
 * Axis Position
 */
public enum AxisPosition {

    /** Left axis, additional plot */
    LEFT_1(0),
    
    /** Left axis 2nd position */
    LEFT_2(1),

    /** Right axis */
    RIGHT_1(0),
    
    /** Right axis and separate legend */
    RIGHT_1S(0),

    RIGHT_2(1),

    LEFT_3(2),
    LEFT_4(3);
    
    /** Index of plot in Plot class*/
    private int plotIndex;
    private AxisPosition(int plotIndex) {
        this.plotIndex = plotIndex;
    }
    public int getPlotIndex() {
        return plotIndex;
    }
}
