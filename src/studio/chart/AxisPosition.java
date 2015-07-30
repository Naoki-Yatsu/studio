package studio.chart;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Axis Position
 */
public enum AxisPosition {
    
    X1(0, "X"),
    Y1(0, "Y1"),
    
    Y1_LEFT1(0, "Y1 Left1"),
    Y1_LEFT2(0, "Y1 Left2"),
    Y1_LEFT3(0, "Y1 Left3"),
    Y1_LEFT4(0, "Y1 Left4"),

    Y1_RIGHT(0, "Y1 Right"),
    
    Y2_LEFT(1, "Y2 Left"),
    Y2_RIGHT(1, "Y2 Right"),
    
    Y3_LEFT(2, "Y3 Left"),
    Y3_RIGHT(2, "Y3 Right"),
    
    Y4_LEFT(3, "Y4 Left"),
    Y4_RIGHT(3, "Y4 Right"),
    
    Y5_LEFT(4, "Y5 Left"),
    Y5_RIGHT(4, "Y5 Right");
    
    public static final List<AxisPosition> AXIS_SUB_ALL = Collections.unmodifiableList(Arrays.asList(
            Y1_LEFT1, Y1_LEFT2, Y1_LEFT3, Y1_LEFT4, 
            Y1_RIGHT, Y2_LEFT, Y2_RIGHT, Y3_LEFT, Y3_RIGHT, Y4_LEFT, Y4_RIGHT, Y5_LEFT, Y5_RIGHT));
    
    public static final List<AxisPosition> AXIS_SUB_WITHOUT_Y1LEFT = Collections.unmodifiableList(Arrays.asList(
            Y1_RIGHT, Y2_LEFT, Y2_RIGHT, Y3_LEFT, Y3_RIGHT, Y4_LEFT, Y4_RIGHT, Y5_LEFT, Y5_RIGHT));
    
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
