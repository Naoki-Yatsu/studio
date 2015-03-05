package studio.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.panel.CrosshairOverlay;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.Crosshair;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.ui.RectangleEdge;

public class CrosshairOverlayChartPanel extends ChartPanel implements ChartMouseListener {

    private static final long serialVersionUID = 1L;
    
    private ChartRenderingInfo info2;
    
    private Crosshair xCrosshair;
    private List<Crosshair> yCrosshairs = new ArrayList<>();

    public CrosshairOverlayChartPanel(JFreeChart chart) {
        super(chart);
        
        // get info field   
        try {
            Class<ChartPanel> clazz = ChartPanel.class;
            // xCrosshairs
            Field infoFiled = clazz.getDeclaredField("info");
            infoFiled.setAccessible(true);
            info2 = (ChartRenderingInfo) infoFiled.get(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // overlay
        CrosshairOverlay crosshairOverlay = new CombindAxisCrosshairOverlay();
        
        // yCrosshair
        Plot plot = chart.getPlot();
        ValueAxis xAxis = null;
        if (plot instanceof CombinedDomainXYPlot) {
            xAxis = ((CombinedDomainXYPlot) plot).getDomainAxis();
            for (int i = 0; i < ((CombinedDomainXYPlot) plot).getSubplots().size(); i++) {
                yCrosshairs.add(createCrosshair());
            }
        } else if (plot instanceof XYPlot) {
            xAxis = ((XYPlot) plot).getDomainAxis();
            yCrosshairs.add(createCrosshair());
        } else if (plot instanceof CategoryPlot) {
        }
        for (Crosshair crosshair : yCrosshairs) {
            crosshairOverlay.addRangeCrosshair(crosshair);
        }
        
        // xCrosshair
        if (xAxis instanceof DateAxis) {
            xCrosshair = createTimeCrosshair((DateAxis)xAxis);
        } else {
            xCrosshair = createCrosshair();
        }
        crosshairOverlay.addDomainCrosshair(xCrosshair);
        
        // add to chart
        addOverlay(crosshairOverlay);
        
        // add listener
        addChartMouseListener(this);
    }
    
    private Crosshair createCrosshair() {
        Crosshair crosshair = new Crosshair(Double.NaN, Color.GRAY, new BasicStroke(0f));
        crosshair.setLabelVisible(true);
        crosshair.setLabelBackgroundPaint(Color.WHITE);
        return crosshair;
    }

    private Crosshair createTimeCrosshair(DateAxis dateAxis) {
        Crosshair crosshair = new TimeCrosshair(Double.NaN, Color.GRAY, new BasicStroke(0f), dateAxis);
        crosshair.setLabelVisible(true);
        crosshair.setLabelBackgroundPaint(Color.WHITE);
        return crosshair;
    }
    
    @Override
    public void chartMouseClicked(ChartMouseEvent event) {
        // ignore
    }

    @Override
    public void chartMouseMoved(ChartMouseEvent event) {
        Rectangle2D dataArea = getScreenDataArea();
        JFreeChart chart = event.getChart();
        XYPlot plot = (XYPlot) chart.getPlot();
        ValueAxis xAxis = plot.getDomainAxis();
        double x = xAxis.java2DToValue(event.getTrigger().getX(), dataArea, RectangleEdge.BOTTOM);
        // make the crosshairs disappear if the mouse is out of range
        if (!xAxis.getRange().contains(x)) {
            x = Double.NaN;
        }
        this.xCrosshair.setValue(x);
        
        // y value
        if (plot instanceof CombinedDomainXYPlot) {
            @SuppressWarnings("unchecked")
            List<XYPlot> plots = ((CombinedDomainXYPlot) plot).getSubplots();
            switch (plots.size()) {
                case 4:
                    double y4 = DatasetUtilities.findYValue(plots.get(3).getDataset(), 0, x);
                    yCrosshairs.get(3).setValue(y4);
                case 3:
                    double y3 = DatasetUtilities.findYValue(plots.get(2).getDataset(), 0, x);
                    yCrosshairs.get(2).setValue(y3);                    
                case 2:
                    double y2 = DatasetUtilities.findYValue(plots.get(1).getDataset(), 0, x);
                    yCrosshairs.get(1).setValue(y2);
                default:
                    double y1 = DatasetUtilities.findYValue(plots.get(0).getDataset(), 0, x);
                    yCrosshairs.get(0).setValue(y1);
            }
        } else if (plot instanceof XYPlot) {
            double y = DatasetUtilities.findYValue(plot.getDataset(), 0, x);
            this.yCrosshairs.get(0).setValue(y);
        }
    }
    
    
    /**
     * Returns the data area (the area inside the axes) for the plot or subplot,
     * @return The scaled data area.
     */
    public Rectangle2D getScreenDataArea(int subplotIndex) {
        PlotRenderingInfo plotInfo = this.info2.getPlotInfo();
        if (plotInfo.getSubplotCount() == 0) {
            return getScreenDataArea();
        } else {
            return scale(plotInfo.getSubplotInfo(subplotIndex).getDataArea());
        }
    }
}
