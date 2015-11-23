package studio.chart;

/**
 * Represents invalid value types.
 * This show whether Inf, Nan is include of not to dataset.
 */
public enum InvalidValueType {

    NO_USE("-", false, false),
    INF("Inf", true, false),
    NAN("NaN", false, true),
    INF_NAN("I/N", true, true);
    
    private String display;
    private boolean useInf;
    private boolean useNan;
    
    
    private InvalidValueType(String display, boolean useInf, boolean useNan) {
        this.display = display;
        this.useInf = useInf;
        this.useNan = useNan;
    }

    public String getDisplay() {
        return display;
    }
    
    public boolean isUseInf() {
        return useInf;
    }
    
    public boolean isUseNan() {
        return useNan;
    }
    
    @Override
    public String toString() {
        return display;
    }
}
