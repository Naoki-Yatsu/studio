package studio.chart;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;

import org.jfree.chart.labels.HighLowItemLabelGenerator;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.chart.renderer.xy.HighLowRenderer;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.urls.StandardCategoryURLGenerator;
import org.jfree.chart.urls.StandardXYURLGenerator;
import org.jfree.ui.TextAnchor;

public class RendererFactory {

    public static XYItemRenderer createXYItemRenderer(ChartType chartType, boolean isDateDomainAxis) {
        return createXYItemRenderer(chartType, isDateDomainAxis, true, false);
    }

    public static XYItemRenderer createXYItemRenderer(ChartType chartType, boolean isDateDomainAxis, boolean tooltips, boolean urls) {
        XYItemRenderer renderer = null;
        switch (chartType) {
            case LINE:
                return createLineChartRenderer(tooltips, urls, false); 
            case LINE_MARK:
            case LINE_DT:
                renderer = createLineChartRenderer(tooltips, urls, true);
                if (chartType == ChartType.LINE_DT) {
                    changeShapeDot(renderer); 
                }
                return renderer;
                
            case BAR:
            case BAR_DENSITY:
                return createBarChartRenderer(isDateDomainAxis, tooltips, urls);
                
            case SCATTER:
            case SCATTER_UD:
            case SCATTER_DT:
                renderer = createScatterChartRenderer(tooltips, urls);
                if (chartType == ChartType.SCATTER_UD) {
                    changeShapeArrow((XYLineAndShapeRenderer) renderer); 
                } else if (chartType == ChartType.SCATTER_DT) {
                    changeShapeDot(renderer); 
                }
                return renderer;
                
            case OHLC:
                CandlestickRenderer candlestickRenderer = new CandlestickRenderer();
                candlestickRenderer.setAutoWidthGap(5.0);
                candlestickRenderer.setUseOutlinePaint(true);
                candlestickRenderer.setSeriesOutlinePaint(0, Color.DARK_GRAY);
                //// Fallowing settings also works.
                // candlestickRenderer.setSeriesPaint(0, Color.BLACK);
                // candlestickRenderer.setAutoPopulateSeriesPaint(false);
                // candlestickRenderer.setAutoPopulateSeriesStroke(false);
                return candlestickRenderer;
                
            case HIGH_LOW:
                renderer = new HighLowRenderer();
                renderer.setBaseToolTipGenerator(new HighLowItemLabelGenerator());
                return renderer;
            default:
                return null;
        }
    }

    public static XYItemRenderer createLineChartRenderer(boolean tooltips, boolean urls, boolean shapes) {
        XYItemRenderer renderer = new XYLineAndShapeRenderer(true, shapes);
        if (tooltips) {
            renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
        }
        if (urls) {
            renderer.setURLGenerator(new StandardXYURLGenerator());
        }
        return renderer;
    }

    public static XYItemRenderer createBarChartRenderer(boolean isDateAxis, boolean tooltips, boolean urls) {
        XYBarRenderer renderer = new XYBarRenderer();
        if (tooltips) {
            XYToolTipGenerator tt;
            if (isDateAxis) {
                tt = StandardXYToolTipGenerator.getTimeSeriesInstance();
            }
            else {
                tt = new StandardXYToolTipGenerator();
            }
            renderer.setBaseToolTipGenerator(tt);
        }
        if (urls) {
            renderer.setURLGenerator(new StandardXYURLGenerator());
        }
        return renderer;
    }

    public static XYItemRenderer createScatterChartRenderer(boolean tooltips, boolean urls) {
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(false, true);
        if (tooltips) {
            renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
        }
        if (urls) {
            renderer.setURLGenerator(new StandardXYURLGenerator());
        }
        return renderer;
    }
    
    public static XYLineAndShapeRenderer changeShapeArrow(XYLineAndShapeRenderer renderer) {
        //Shape up = ShapeUtilities.createUpTriangle(4);
        // Shape down = ShapeUtilities.createDownTriangle(4);
        Shape up = createUpArrow(5);
        Shape down = createDownArrow(5);

        renderer.setSeriesShape(0, up);
        renderer.setSeriesShape(1, down);
        
        // additional
        renderer.setSeriesShape(2, up);
        renderer.setSeriesShape(3, down);
        renderer.setSeriesShape(4, up);
        renderer.setSeriesShape(5, down);

        renderer.setUseOutlinePaint(true);

        // renderer.setSeriesOutlinePaint(0, Color.black);
        // renderer.setSeriesOutlinePaint(1, Color.black);

        return renderer;
    }
    
    public static XYItemRenderer changeShapeDot(XYItemRenderer renderer) {
        // Shape dot = new Rectangle2D.Double(-3.0, -3.0, 6.0, 6.0);
        Shape dot = new Ellipse2D.Double(0, 0, 2, 2);
        for (int i = 0; i < 20; i++) {
            renderer.setSeriesShape(i, dot);
        }
        return renderer;
    }
    
    /**
     * Arrow shape up
     * @param s
     * @return
     */
    public static Shape createUpArrow(final float s) {
        final GeneralPath p0 = new GeneralPath();
        p0.moveTo(0.0f, -s);
        p0.lineTo(-s, 0);
        p0.lineTo(-s * 0.5, 0);
        p0.lineTo(-s * 0.5, s);
        p0.lineTo(s * 0.5, s);
        p0.lineTo(s * 0.5, 0);
        p0.lineTo(s, 0);
        p0.closePath();
        return p0;
    }
    
    /** 
     * Arrow shape down
     * @param s
     * @return
     */
    public static Shape createDownArrow(final float s) {
        final GeneralPath p0 = new GeneralPath();
        p0.moveTo(0.0f, s);
        p0.lineTo(-s, 0);
        p0.lineTo(-s * 0.5, 0);
        p0.lineTo(-s * 0.5, -s);
        p0.lineTo(s * 0.5, -s);
        p0.lineTo(s * 0.5, 0);
        p0.lineTo(s, 0);
        p0.closePath();
        return p0;
    }

    /**
     * Category
     * @param chartType
     * @param tooltips
     * @param urls
     * @return
     */
    public static CategoryItemRenderer createCategoryItemRenderer(ChartType chartType, boolean tooltips, boolean urls) {
        CategoryItemRenderer renderer = null;
        switch (chartType) {
            case LINE:
            case LINE_MARK:
                renderer = new LineAndShapeRenderer(true, false);
                break;
            case BAR:
            case BAR_DENSITY:
                renderer = new LineAndShapeRenderer(true, false);
                ItemLabelPosition position1 = new ItemLabelPosition(
                        ItemLabelAnchor.OUTSIDE12, TextAnchor.BOTTOM_CENTER);
                renderer.setBasePositiveItemLabelPosition(position1);
                ItemLabelPosition position2 = new ItemLabelPosition(
                        ItemLabelAnchor.OUTSIDE6, TextAnchor.TOP_CENTER);
                renderer.setBaseNegativeItemLabelPosition(position2);
                break;
            case SCATTER:
            case SCATTER_UD:
                renderer = new LineAndShapeRenderer(false, true);
                break;
            default:
                return null;
        }
        if (tooltips) {
            renderer.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator());
        }
        if (urls) {
            renderer.setBaseItemURLGenerator(new StandardCategoryURLGenerator());
        }
        return renderer;
    }
    
    public static CategoryItemRenderer createCategoryItemRenderer(ChartType chartType) {
        return createCategoryItemRenderer(chartType, true, false);
    }

}
