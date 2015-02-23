package studio.chart;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.panel.CrosshairOverlay;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.Crosshair;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.ui.RectangleEdge;

public class CombindAxisCrosshairOverlay extends CrosshairOverlay {

    private static final long serialVersionUID = 1L;
    
    private List<Crosshair> xCrosshairs2;
    private List<Crosshair> yCrosshairs2;
        
    @SuppressWarnings("unchecked")
    public CombindAxisCrosshairOverlay() {
        super();

        // get super field using reflection
        try {
            Class<CrosshairOverlay> clazz = CrosshairOverlay.class;
            // xCrosshairs
            Field xCrosshairsFiled = clazz.getDeclaredField("xCrosshairs");
            xCrosshairsFiled.setAccessible(true);
            xCrosshairs2 = (List<Crosshair>) xCrosshairsFiled.get(this);

            // yCrosshairs
            Field yCrosshairsFiled = clazz.getDeclaredField("yCrosshairs");
            yCrosshairsFiled.setAccessible(true);
            yCrosshairs2 = (List<Crosshair>) yCrosshairsFiled.get(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Paints the crosshairs in the layer.
     *
     * @param g2 the graphics target.
     * @param chartPanel the chart panel.
     */
    @Override
    public void paintOverlay(Graphics2D g2, ChartPanel chartPanel) {
        Shape savedClip = g2.getClip();
        Rectangle2D dataArea = chartPanel.getScreenDataArea();
        g2.clip(dataArea);
        JFreeChart chart = chartPanel.getChart();
        XYPlot plot = (XYPlot) chart.getPlot();
        ValueAxis xAxis = plot.getDomainAxis();
        RectangleEdge xAxisEdge = plot.getDomainAxisEdge();
        Iterator<Crosshair> iterator = this.xCrosshairs2.iterator();
        while (iterator.hasNext()) {
            Crosshair ch = (Crosshair) iterator.next();
            if (ch.isVisible()) {
                double x = ch.getValue();
                double xx = xAxis.valueToJava2D(x, dataArea, xAxisEdge);
                if (plot.getOrientation() == PlotOrientation.VERTICAL) {
                    drawVerticalCrosshair(g2, dataArea, xx, ch);
                }
                else {
                    drawHorizontalCrosshair(g2, dataArea, xx, ch);
                }
            }
        }
        
        // Plot and Range
        if (plot instanceof CombinedDomainXYPlot) {
            CrosshairOverlayChartPanel overlayChartPanel = (CrosshairOverlayChartPanel) chartPanel;
            // Multi-plot
            @SuppressWarnings("unchecked")
            List<XYPlot> plots = ((CombinedDomainXYPlot) plot).getSubplots();
            int subplotIndex = -1;
            switch (plots.size()) {
                case 4:
                    subplotIndex = 3;
                    paintOverlayRange(g2, overlayChartPanel.getScreenDataArea(subplotIndex), yCrosshairs2.get(subplotIndex), plots.get(subplotIndex));
                case 3:
                    subplotIndex = 2;
                    paintOverlayRange(g2, overlayChartPanel.getScreenDataArea(subplotIndex), yCrosshairs2.get(subplotIndex), plots.get(subplotIndex));
                case 2:
                    subplotIndex = 1;
                    paintOverlayRange(g2, overlayChartPanel.getScreenDataArea(subplotIndex), yCrosshairs2.get(subplotIndex), plots.get(subplotIndex));
                default:
                    subplotIndex = 0;
                    paintOverlayRange(g2, overlayChartPanel.getScreenDataArea(subplotIndex), yCrosshairs2.get(subplotIndex), plots.get(subplotIndex));
            }

        } else if (plot instanceof XYPlot) {
            paintOverlayRange(g2, dataArea, yCrosshairs2.get(0), plot);
        }
        g2.setClip(savedClip);
    }
    
    private void paintOverlayRange(Graphics2D g2, Rectangle2D dataArea, Crosshair yCrosshair, XYPlot plot) {
        if (yCrosshair.isVisible()) {
            double y = yCrosshair.getValue();
            double yy = plot.getRangeAxis().valueToJava2D(y, dataArea, plot.getRangeAxisEdge());
            if (plot.getOrientation() == PlotOrientation.VERTICAL) {
                drawHorizontalCrosshair(g2, dataArea, yy, yCrosshair);
            }
            else {
                drawVerticalCrosshair(g2, dataArea, yy, yCrosshair);
            }
        }
    }
}
