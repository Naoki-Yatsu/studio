package studio.chart;

import studio.kdb.K;

public enum AxisDataType {
    DATE,
    XY,
    CATEGORY;
    
    public static AxisDataType getAxisDataType(Class<?> kClass) {
        if ((kClass == K.KTimestampVector.class)
                || (kClass == K.KTimespanVector.class)
                || (kClass == K.KDateVector.class)
                || (kClass == K.KTimeVector.class)
                || (kClass == K.KMonthVector.class)
                || (kClass == K.KMinuteVector.class)
                || (kClass == K.KSecondVector.class)
                || (kClass == K.KDatetimeVector.class)) {
            // TimeSerisCollection
            return DATE;

        } else if ((kClass == K.KDoubleVector.class)
                || (kClass == K.KFloatVector.class)
                || (kClass == K.KShortVector.class)
                || (kClass == K.KIntVector.class)
                || (kClass == K.KLongVector.class)) {
            // XYSeriesCollection
            return XY;

        } else if ((kClass == K.KSymbol.class)
                || (kClass == K.KCharacterVector.class)
                || (kClass == K.KCharacter.class)) {
            // CategoryDataset
            return CATEGORY;
        }
        return null;
    }
    
}
