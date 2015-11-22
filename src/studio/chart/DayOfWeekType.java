package studio.chart;

public enum DayOfWeekType {

    ALL(-1),
    MON(0),
    TUE(1),
    WED(2),
    THU(3),
    FRI(4),
    SAT(5),
    SUN(6);

    private int dayNo;

    private DayOfWeekType(int dayNo) {
        this.dayNo = dayNo;
    }
    
    public int getDayNo() {
        return dayNo;
    }
}
