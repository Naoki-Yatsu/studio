package studio.chart;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.xy.XYDataset;

/**
 * If dataset has NULL value, XYPlot.getDataRange does'nt work properly...
 */
public class XYNullablePlot extends XYPlot {

    private static final long serialVersionUID = 1L;

    public XYNullablePlot(XYDataset dataset, ValueAxis domainAxis, ValueAxis rangeAxis, XYItemRenderer renderer) {
        super(dataset, domainAxis,  rangeAxis,  renderer);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Range getDataRange(ValueAxis axis) {
        Range dataRange = super.getDataRange(axis);

        if (dataRange == null) {
            double lower = Double.MAX_VALUE;
            double upper = Double.MIN_VALUE;
            List<XYDataset> mappedDatasets = new ArrayList<>();
            // consider range axis only
            try {
                int domainIndex = getDomainAxisIndex(axis);
                if (domainIndex >= 0) {
                    Method method = XYPlot.class.getDeclaredMethod("getDatasetsMappedToDomainAxis", Integer.class);
                    method.setAccessible(true);
                    mappedDatasets.addAll((List<XYDataset>) method.invoke(this, new Integer(domainIndex)));
                }
                int rangeIndex = getRangeAxisIndex(axis);
                if (rangeIndex >= 0) {
                    Method method = XYPlot.class.getDeclaredMethod("getDatasetsMappedToRangeAxis", Integer.class);
                    method.setAccessible(true);
                    mappedDatasets.addAll((List<XYDataset>) method.invoke(this, new Integer(rangeIndex)));
                }
                
                for (XYDataset xyDataset : mappedDatasets) {
                    Range findRangeBounds = DatasetUtilities.findRangeBounds(xyDataset);
                    lower = Math.min(findRangeBounds.getLowerBound(), lower);
                    upper = Math.max(findRangeBounds.getUpperBound(), upper);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (lower < upper) {
                dataRange = new Range(lower, upper);
            }
        }
        return dataRange;
    }

}
