package studio.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
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
import org.jfree.chart.labels.StandardCrosshairLabelGenerator;
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
    
    private ChartRenderingInfo info;
    
    private Crosshair xCrosshair;
    private List<Crosshair> yCrosshairs = new ArrayList<>();

    private boolean showValue;
    private boolean showCursor;
    private int cursorYPlotIndex = 0;
    
    public CrosshairOverlayChartPanel(JFreeChart chart) {
        this(chart, true, false);
    }
    
    public CrosshairOverlayChartPanel(JFreeChart chart, boolean showValue, boolean showCursor) {
        super(chart);
        this.showValue = showValue;
        this.showCursor = showCursor;
        
        // get info field   
        try {
            Class<ChartPanel> clazz = ChartPanel.class;
            // xCrosshairs
            Field infoFiled = clazz.getDeclaredField("info");
            infoFiled.setAccessible(true);
            info = (ChartRenderingInfo) infoFiled.get(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // overlay
        CrosshairOverlay crosshairOverlay = new CombindAxisCrosshairOverlay(showValue, showCursor);
        
        Plot plot = chart.getPlot();
        
        // yCrosshair (value)
        if (showValue) {
            if (plot instanceof CombinedDomainXYPlot) {
                for (int i = 0; i < ((CombinedDomainXYPlot) plot).getSubplots().size(); i++) {
                    Crosshair crosshair = createCrosshair();
                    yCrosshairs.add(crosshair);
                    crosshairOverlay.addRangeCrosshair(crosshair);
                }
            } else if (plot instanceof XYPlot) {
                Crosshair crosshair = createCrosshair();
                yCrosshairs.add(crosshair);
                crosshairOverlay.addRangeCrosshair(crosshair);
            } else if (plot instanceof CategoryPlot) {
                // TODO
            }
        }
        // yCrosshair (cursor)
        if (showCursor) {
            Crosshair crosshair = createCrosshair();
            yCrosshairs.add(crosshair);
            crosshairOverlay.addRangeCrosshair(crosshair);
        }
        
        // xCrosshair
        ValueAxis xAxis = null;
        if (plot instanceof CombinedDomainXYPlot) {
            xAxis = ((CombinedDomainXYPlot) plot).getDomainAxis();
        } else if (plot instanceof XYPlot) {
            xAxis = ((XYPlot) plot).getDomainAxis();
        } else if (plot instanceof CategoryPlot) {
            // TODO
        }
        if (xAxis instanceof DateAxis) {
            xCrosshair = createTimeCrosshair((DateAxis)xAxis, (XYPlot)plot);
        } else {
            xCrosshair = createCrosshair();
        }
        crosshairOverlay.addDomainCrosshair(xCrosshair);
        
        // add to chart
        addOverlay(crosshairOverlay);
        
        // add listener
        addChartMouseListener(this);
    }
    
    @SuppressWarnings("serial")
    private Crosshair createCrosshair() {
        Crosshair crosshair = new Crosshair(Double.NaN, Color.GRAY, new BasicStroke(0f));
        crosshair.setLabelVisible(true);
        crosshair.setLabelBackgroundPaint(Color.WHITE);
        crosshair.setLabelGenerator(new StandardCrosshairLabelGenerator() {
            @Override
            public String generateLabel(Crosshair crosshair) {
                return " " + super.generateLabel(crosshair) + " ";
            }
        });
        return crosshair;
    }

    private Crosshair createTimeCrosshair(DateAxis dateAxis, XYPlot plot) {
        Crosshair crosshair = new TimeCrosshair(Double.NaN, Color.GRAY, new BasicStroke(0f), dateAxis, plot);
        crosshair.setLabelVisible(true);
        crosshair.setLabelBackgroundPaint(Color.WHITE);
        crosshair.setLabelFont(new Font("Tahoma", Font.PLAIN, 10));
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
        if (showValue) {
            if (plot instanceof CombinedDomainXYPlot) {
                @SuppressWarnings("unchecked")
                List<XYPlot> plots = ((CombinedDomainXYPlot) plot).getSubplots();
                switch (plots.size()) {
                    case 5:
                        double y5 = DatasetUtilities.findYValue(plots.get(4).getDataset(), 0, x);
                        yCrosshairs.get(4).setValue(y5);
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

        if (showCursor) {
            Crosshair yCursorCrosshair = yCrosshairs.get(yCrosshairs.size() - 1);
            if (plot instanceof CombinedDomainXYPlot) {
                @SuppressWarnings("unchecked")
                List<XYPlot> plots = ((CombinedDomainXYPlot) plot).getSubplots();
                for (int i = 0; i < plots.size(); i++) {
                    Rectangle2D subDataArea = getScreenDataArea(i);
                    if (subDataArea.contains(event.getTrigger().getX(), event.getTrigger().getY())) {
                        cursorYPlotIndex = i;
                        double yCursor = plots.get(i).getRangeAxis().java2DToValue(event.getTrigger().getY(), subDataArea, RectangleEdge.LEFT);
                        yCursorCrosshair.setValue(yCursor);
                        break;
                    }
                }
            } else if (plot instanceof XYPlot) {
                double yCursor = plot.getRangeAxis().java2DToValue(event.getTrigger().getY(), dataArea, RectangleEdge.LEFT);
                yCursorCrosshair.setValue(yCursor);
            }
        }
    }
    
    /**
     * Returns the data area (the area inside the axes) for the plot or subplot,
     * @return The scaled data area.
     */
    public Rectangle2D getScreenDataArea(int subplotIndex) {
        PlotRenderingInfo plotInfo = this.info.getPlotInfo();
        if (plotInfo.getSubplotCount() == 0) {
            return getScreenDataArea();
        } else {
            return scale(plotInfo.getSubplotInfo(subplotIndex).getDataArea());
        }
    }

    public int getCursorYPlotIndex() {
        return cursorYPlotIndex;
    }
    
}
