package studio.chart;

import java.awt.Paint;
import java.awt.Stroke;

import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.labels.StandardCrosshairLabelGenerator;
import org.jfree.chart.plot.Crosshair;

public class TimeCrosshair extends Crosshair {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private DateAxis dateAxis;
    
    public TimeCrosshair(DateAxis dateAxis) {
        super();
        this.dateAxis = dateAxis;
        setLabelGenerator(new TimeCrosshairLabelGenerator(dateAxis));
    }
    
    public TimeCrosshair(double value, DateAxis dateAxis) {
        super(value);
        this.dateAxis = dateAxis;
        setLabelGenerator(new TimeCrosshairLabelGenerator(dateAxis));
    }

    public TimeCrosshair(double value, Paint paint, Stroke stroke, DateAxis dateAxis) {
        super(value, paint, stroke);
        this.dateAxis = dateAxis;
        setLabelGenerator(new TimeCrosshairLabelGenerator(dateAxis));
    }
}

class TimeCrosshairLabelGenerator extends StandardCrosshairLabelGenerator {

    private static final long serialVersionUID = 1L;
    
    private DateAxis dateAxis;
    
    public TimeCrosshairLabelGenerator(DateAxis dateAxis) {
        super();
        this.dateAxis = dateAxis;
    }
    
    @Override
    public String generateLabel(Crosshair crosshair) {
        if (crosshair instanceof TimeCrosshair) {
            long millis = (long) crosshair.getValue();
            // TODO Format...
            return dateAxis.getTickUnit().valueToString(millis);
        } else {
            return super.generateLabel(crosshair);
        }
    }
}

