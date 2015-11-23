package studio.chart;

public enum DayOfWeekType {

    // FX (TKY Mon 6:00 ~ Sat 7:00)
    FX(-1),
    
    // Days
    MON(0),
    TUE(1),
    WED(2),
    THU(3),
    FRI(4),
    SAT(5),
    SUN(6),
    
    // one day
    DAY(-1),
    
    // Tokyo Stock Exchange
    TSE(-1);

    private int dayNo;

    private DayOfWeekType(int dayNo) {
        this.dayNo = dayNo;
    }
    
    public int getDayNo() {
        return dayNo;
    }
}
