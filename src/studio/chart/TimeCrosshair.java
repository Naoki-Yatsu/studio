package studio.chart;

import java.awt.Paint;
import java.awt.Stroke;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.DateTickUnitType;
import org.jfree.chart.labels.StandardCrosshairLabelGenerator;
import org.jfree.chart.plot.Crosshair;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;

public class TimeCrosshair extends Crosshair {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private DateAxis dateAxis;
    
    public TimeCrosshair(DateAxis dateAxis, XYPlot plot) {
        super();
        this.dateAxis = dateAxis;
        setLabelGenerator(new TimeCrosshairLabelGenerator(dateAxis, plot));
    }
    
    public TimeCrosshair(double value, DateAxis dateAxis, XYPlot plot) {
        super(value);
        this.dateAxis = dateAxis;
        setLabelGenerator(new TimeCrosshairLabelGenerator(dateAxis, plot));
    }

    public TimeCrosshair(double value, Paint paint, Stroke stroke, DateAxis dateAxis, XYPlot plot) {
        super(value, paint, stroke);
        this.dateAxis = dateAxis;
        setLabelGenerator(new TimeCrosshairLabelGenerator(dateAxis, plot));
    }
}

class TimeCrosshairLabelGenerator extends StandardCrosshairLabelGenerator {

    private static final long serialVersionUID = 1L;
    
    private DateAxis dateAxis;
    private XYPlot plot;
    private Range dataRange;
    
    public TimeCrosshairLabelGenerator(DateAxis dateAxis, XYPlot plot) {
        super();
        this.dateAxis = dateAxis;
        this.plot = plot;
        this.dataRange = plot.getDataRange(dateAxis);
        
//        RegularTimePeriod period = ((TimeSeriesCollection) plot.getDataset()).getSeries(0).getTimePeriod(0);
//        if (period instanceof Month) {
//            
//        } else if (period instanceof Week) {
//            
//        } else if (period instanceof Day) {
//
//        } else if (period instanceof Hour) {
//            
//        } else if (period instanceof Minute) {
//            
//        } else if (period instanceof Second) {
//            
//        } else if (period instanceof Millisecond) {
//            
//        }
    }
    
    //
    // ref) DateAxis.createStandardDateTickUnits
    //
    @Override
    public String generateLabel(Crosshair crosshair) {
        if (crosshair instanceof TimeCrosshair) {
            long millis = (long) crosshair.getValue();
            String label = getAjustedDateFormat().format(new Date(millis));
            // dateAxis.getTickUnit().valueToString(millis)
            return " " + label + " ";
        } else {
            return " " + super.generateLabel(crosshair) + " ";
        }
    }
    
    private DateFormat getAjustedDateFormat() {
        DateTickUnit unit = dateAxis.getTickUnit();
        DateTickUnitType unitType = unit.getUnitType();

        // Format source
        String formatDatePart = "";
        String formatTimePart = "";

        // Date Part
        // data length/days
        double length = dataRange.getLength();
        double days = length / (24 * 60 * 60 * 1000);
        if (unitType == DateTickUnitType.YEAR) {
            formatDatePart = "yyyy";
        } else if (unitType == DateTickUnitType.MONTH) {
            formatDatePart = "MMM-yyyy";
        } else {
            if (days > 3) {
                // formatDatePart = "d-MMM";
                formatDatePart = "M/d";
            } else {
                formatDatePart = "";
            }
        }

        // Time Part
        if (unitType == DateTickUnitType.MILLISECOND) {
            formatTimePart = "HH:mm:ss.SSS";
        } else if (unitType == DateTickUnitType.SECOND) {
            formatTimePart = "HH:mm:ss";
        } else if (unitType == DateTickUnitType.MINUTE) {
            formatTimePart = "HH:mm:ss";
        } else if (unitType == DateTickUnitType.HOUR) {
            formatTimePart = "HH:mm";
        } else if (unitType == DateTickUnitType.DAY) {
            formatTimePart = "HH:mm";
        }

        // Format
        String pattern = null;
        if (formatDatePart.isEmpty() || formatTimePart.isEmpty()) {
            pattern = formatDatePart + formatTimePart;
        } else {
            pattern = formatDatePart + " " + formatTimePart;
        }
        DateFormat format = new SimpleDateFormat(pattern, Locale.getDefault());
        
        // 元のUnitがほしい
        // 上限は元のUNIT
        // 下限はデータの精度による - day / min / sec / msec
        
//        Locale locale = Locale.getDefault();
//        DateFormat f1 = new SimpleDateFormat("HH:mm:ss.SSS", locale);
//        DateFormat f2 = new SimpleDateFormat("HH:mm:ss", locale);
//        DateFormat f3 = new SimpleDateFormat("HH:mm", locale);
//        DateFormat f4 = new SimpleDateFormat("d-MMM, HH:mm", locale);
//        DateFormat f5 = new SimpleDateFormat("d-MMM", locale);
//        DateFormat f6 = new SimpleDateFormat("MMM-yyyy", locale);
//        DateFormat f7 = new SimpleDateFormat("yyyy", locale);
        
        return format;
    }
    
}

