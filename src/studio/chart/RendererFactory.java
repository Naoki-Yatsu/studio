package studio.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
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
import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.chart.renderer.xy.HighLowRenderer;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.urls.StandardCategoryURLGenerator;
import org.jfree.chart.urls.StandardXYURLGenerator;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.ui.TextAnchor;

import studio.chart.ChartSetting.ChartAxisSetting;

public class RendererFactory {

    public static XYItemRenderer createXYItemRenderer(ChartAxisSetting axisSetting, boolean isDateDomainAxis, int seriesCount) {
        return createXYItemRenderer(axisSetting, isDateDomainAxis, true, false, seriesCount);
    }

    public static AbstractXYItemRenderer createXYItemRenderer(ChartAxisSetting axisSetting, boolean isDateDomainAxis, boolean tooltips, boolean urls, int seriesCount) {
        ChartType chartType = axisSetting.getChartType();
        Color seriesColor = axisSetting.getColor();
        AbstractXYItemRenderer renderer = null;
        
        switch (chartType) {
            case LINE:
            case LINE_GRAD:
                renderer = createLineChartRenderer(tooltips, urls, false);
                break;
            case LINE_MARK:
            case LINE_DT:
                renderer = createLineChartRenderer(tooltips, urls, true);
                if (chartType == ChartType.LINE_DT) {
                    changeShapeDot(renderer, seriesCount); 
                }
                break;
                
            case BAR:
                renderer = createBarChartRenderer(isDateDomainAxis, tooltips, urls);
                break;
                
            case SCATTER:
                renderer = createScatterChartRenderer(tooltips, urls);
                break;
            case SCATTER_UD:
                renderer = createScatterChartRenderer(tooltips, urls);
                changeShapeArrow((XYLineAndShapeRenderer) renderer, seriesCount);
                break;
            case SCATTER_DT:
                renderer = createScatterChartRenderer(tooltips, urls);
                changeShapeDot((XYLineAndShapeRenderer) renderer, seriesCount); 
                break;

            case OHLC:
                renderer = createCandlestickRenderer(seriesColor);
                break;
            case OHLC2:
                renderer = createCandlestickRenderer2(seriesColor);
                break;
                
            case HIGH_LOW:
                renderer = new HighLowRenderer();
                renderer.setBaseToolTipGenerator(new HighLowItemLabelGenerator());
                break;
            default:
                return null;
        }
        
        // set color
        if (chartType == ChartType.SCATTER_UD) {
            setSeriesColorForScatterUD(renderer, seriesColor, seriesCount);
        } else if (chartType == ChartType.LINE_GRAD) {
            setSeriesColorForLineGradation(renderer, seriesColor, seriesCount);
        } else {
            setSeriesColor(renderer, seriesColor, seriesCount);
        }   
        
        return renderer;
    }

    public static AbstractXYItemRenderer createLineChartRenderer(boolean tooltips, boolean urls, boolean shapes) {
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, shapes);
        if (tooltips) {
            renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
        }
        if (urls) {
            renderer.setURLGenerator(new StandardXYURLGenerator());
        }
        return renderer;
    }

    public static AbstractXYItemRenderer createBarChartRenderer(boolean isDateAxis, boolean tooltips, boolean urls) {
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

    public static XYLineAndShapeRenderer createScatterChartRenderer(boolean tooltips, boolean urls) {
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(false, true);
        if (tooltips) {
            renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
        }
        if (urls) {
            renderer.setURLGenerator(new StandardXYURLGenerator());
        }
        return renderer;
    }

    public static CandlestickRenderer createCandlestickRenderer(Color color) {
        CandlestickRenderer candlestickRenderer = new CandlestickRenderer();
        // candlestickRenderer.setAutoWidthGap(5.0);
        candlestickRenderer.setUseOutlinePaint(true);
        candlestickRenderer.setSeriesOutlinePaint(0, Color.DARK_GRAY);
        candlestickRenderer.setBaseOutlineStroke(new BasicStroke(1));
        
        if (color != null) {
            candlestickRenderer.setUpPaint(color);
            //candlestickRenderer.setDownPaint(color.darker());
            candlestickRenderer.setDownPaint(SeriesColor.rotateColor(color).darker());
        }
        
        //// Following settings also works.
        // candlestickRenderer.setSeriesPaint(0, Color.BLACK);
        // candlestickRenderer.setAutoPopulateSeriesPaint(false);
        // candlestickRenderer.setAutoPopulateSeriesStroke(false);
        return candlestickRenderer;
    }
    
    
    public static CandlestickRenderer createCandlestickRenderer2(Color color) {
        CandlestickRenderer candlestickRenderer = new CandlestickRenderer() {
            private static final long serialVersionUID = 1L;
            @Override
            public Paint getItemPaint(int row, int column) {
                OHLCDataset dataset = (OHLCDataset)getPlot().getDataset();
                double open = dataset.getOpenValue(row, column);
                double close = dataset.getCloseValue(row, column);
                if(open <= close){
                    return getUpPaint();
                }else{
                    return getDownPaint();
                }
            }
            
        };
        candlestickRenderer.setUseOutlinePaint(false);
        //candlestickRenderer.setCandleWidth(2.0);
        
        if (color != null) {
            candlestickRenderer.setUpPaint(color);
            candlestickRenderer.setDownPaint(SeriesColor.rotateColor(color).darker());
        }
        return candlestickRenderer;
    }
    
    /**
     * Set color for all series
     * 
     * @param renderer
     * @param seriesColor
     * @param seriesCount
     */
    private static void setSeriesColor(AbstractXYItemRenderer renderer, Color seriesColor, int seriesCount) {
        if (seriesColor == null) {
            return;
        }
        renderer.setAutoPopulateSeriesStroke(false);
        renderer.setAutoPopulateSeriesPaint(false);
        for (int i = 0; i < seriesCount; i++) {
            renderer.setSeriesPaint(i, seriesColor);
        }
    }
    
    private static void setSeriesColorForLineGradation(AbstractXYItemRenderer renderer, Color seriesColor, int seriesCount) {
        if (seriesColor == null) {
            return;
        }
        renderer.setAutoPopulateSeriesStroke(false);
        renderer.setAutoPopulateSeriesPaint(false);
        // change color gradation from color -> white by seriesCount+1
        // color + (255 - color)*ratio = color(1-ratio) + 255*ratio
        for (int i = 0; i < seriesCount; i++) {
            double ratio = i / (double)seriesCount;
            Color color = new Color(
                    (int) (seriesColor.getRed() * (1 - ratio) + 255 * ratio),
                    (int) (seriesColor.getGreen() * (1 - ratio) + 255 * ratio),
                    (int) (seriesColor.getBlue() * (1 - ratio) + 255 * ratio),
                    seriesColor.getAlpha());
            renderer.setSeriesPaint(i, color);
        }
    }
    
    private static void setSeriesColorForScatterUD(AbstractXYItemRenderer renderer, Color seriesColor, int seriesCount) {
        if (seriesColor == null) {
            return;
        }
        renderer.setAutoPopulateSeriesStroke(false);
        renderer.setAutoPopulateSeriesPaint(false);
        Color tmpColor = seriesColor;
        for (int i = 0; i < seriesCount; i++) {
            renderer.setSeriesPaint(i, tmpColor);
            tmpColor = SeriesColor.rotateColor(tmpColor);
        }
    }
    
    
    private static void changeShapeArrow(XYLineAndShapeRenderer renderer, int seriesCount) {
        // Shape up = ShapeUtilities.createUpTriangle(4);
        // Shape down = ShapeUtilities.createDownTriangle(4);
        
        Shape up = createUpArrow(5);
        Shape down = createDownArrow(5);
        for (int i = 0; i < seriesCount; i++) {
            if(i % 2 == 0) {
                renderer.setSeriesShape(i, up);
            } else {
                renderer.setSeriesShape(i, down);
            }
            
        }
        renderer.setUseOutlinePaint(true);
        // renderer.setSeriesOutlinePaint(0, Color.black);
        // renderer.setSeriesOutlinePaint(1, Color.black);
    }
    
    private static void changeShapeDot(XYItemRenderer renderer, int seriesCount) {
        // Shape dot = new Rectangle2D.Double(-3.0, -3.0, 6.0, 6.0);
        Shape dot = new Ellipse2D.Double(0, 0, 2, 2);
        for (int i = 0; i < seriesCount; i++) {
            renderer.setSeriesShape(i, dot);
        }
    }
    
    /**
     * Arrow shape up
     * @param s
     * @return
     */
    private static Shape createUpArrow(final float s) {
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
    private static Shape createDownArrow(final float s) {
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
    public static CategoryItemRenderer createCategoryItemRenderer(ChartAxisSetting axisSetting, boolean tooltips, boolean urls) {
        ChartType chartType = axisSetting.getChartType();
        CategoryItemRenderer renderer = null;
        
        
        switch (chartType) {
            case LINE:
            case LINE_MARK:
                renderer = new LineAndShapeRenderer(true, false);
                break;
                
            case BAR:
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
    
    public static CategoryItemRenderer createCategoryItemRenderer(ChartAxisSetting axisSetting) {
        return createCategoryItemRenderer(axisSetting, true, false);
    }

}
