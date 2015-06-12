package studio.chart;

import java.awt.Color;
import java.awt.Shape;

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
import org.jfree.util.ShapeUtilities;

public class RendererFactory {

    public static XYItemRenderer createXYItemRenderer(ChartType chartType, boolean isDateDomainAxis) {
        return createXYItemRenderer(chartType, isDateDomainAxis, true, false);
    }

    public static XYItemRenderer createXYItemRenderer(ChartType chartType, boolean isDateDomainAxis, boolean tooltips, boolean urls) {
        switch (chartType) {
            case LINE:
                return createLineChartRenderer(tooltips, urls, false); 
            case LINE_MARK:
                return createLineChartRenderer(tooltips, urls, true);
            case BAR:
            case BAR_DENSITY:
                return createBarChartRenderer(isDateDomainAxis, tooltips, urls);
            case SCATTER:
                return createScatterChartRenderer(tooltips, urls);
            case SCATTER_UD:
                return createScatterUpDownChartRenderer(tooltips, urls);
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
                HighLowRenderer renderer = new HighLowRenderer();
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
    
    public static XYItemRenderer createScatterUpDownChartRenderer(boolean tooltips, boolean urls) {
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(false, true);
        if (tooltips) {
            renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
        }
        if (urls) {
            renderer.setURLGenerator(new StandardXYURLGenerator());
        }
        Shape up = ShapeUtilities.createUpTriangle(4);
        renderer.setSeriesShape(0, up);
        Shape down = ShapeUtilities.createDownTriangle(4);
        renderer.setSeriesShape(1, down);
        
        // additional
        renderer.setSeriesShape(2, up);
        renderer.setSeriesShape(3, down);

        // renderer.setUseOutlinePaint(true);
        // renderer.setSeriesOutlinePaint(0, Color.black);
        // renderer.setSeriesOutlinePaint(1, Color.black);

        return renderer;
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
