package studio.chart;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TimeZone;
import java.util.TreeMap;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItemSource;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.block.BlockContainer;
import org.jfree.chart.block.BorderArrangement;
import org.jfree.chart.block.EmptyBlock;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.CombinedDomainCategoryPlot;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.chart.renderer.xy.HighLowRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.CompositeTitle;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.SeriesException;
import org.jfree.data.time.Day;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.Minute;
import org.jfree.data.time.Month;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.ohlc.OHLCSeries;
import org.jfree.data.time.ohlc.OHLCSeriesCollection;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

import studio.chart.ChartSetting.ChartAxisSetting;
import studio.kdb.K;
import studio.kdb.K.KBase;
import studio.kdb.KTableModel;
import studio.kdb.ToDouble;

public class ChartDataCreator {

    private static final TimeZone tz = TimeZone.getTimeZone("GMT");
    
    private static final Locale locale = Locale.getDefault();

    public static JFreeChart createChart(KTableModel tableModel, ChartSetting setting) {
        if (tableModel.getColumnCount() == 0) {
            return null;
        }

        //
        // create separate dataset for each axis
        //
        NavigableMap<Integer, Integer> fromToMap = new TreeMap<>();
        int endIndex = tableModel.getColumnCount();
        // Y5 Right
        if (setting.isY5RightEnable()) {
            int index = getColumnIndexFromName(tableModel, setting.getAxisSetting(AxisPosition.Y5_RIGHT).getColumnName());
            fromToMap.put(index, endIndex);
            endIndex = index;
        }
        // Y5 Left
        if (setting.isY5LeftEnable()) {
            int index = getColumnIndexFromName(tableModel, setting.getAxisSetting(AxisPosition.Y5_LEFT).getColumnName());
            fromToMap.put(index, endIndex);
            endIndex = index;
        }
        // Y4 Right
        if (setting.isY4RightEnable()) {
            int index = getColumnIndexFromName(tableModel, setting.getAxisSetting(AxisPosition.Y4_RIGHT).getColumnName());
            fromToMap.put(index, endIndex);
            endIndex = index;
        }
        // Y4 Left
        if (setting.isY4LeftEnable()) {
            int index = getColumnIndexFromName(tableModel, setting.getAxisSetting(AxisPosition.Y4_LEFT).getColumnName());
            fromToMap.put(index, endIndex);
            endIndex = index;
        }
        // Y3 Right
        if (setting.isY3RightEnable()) {
            int index = getColumnIndexFromName(tableModel, setting.getAxisSetting(AxisPosition.Y3_RIGHT).getColumnName());
            fromToMap.put(index, endIndex);
            endIndex = index;
        }
        // Y3 Left
        if (setting.isY3LeftEnable()) {
            int index = getColumnIndexFromName(tableModel, setting.getAxisSetting(AxisPosition.Y3_LEFT).getColumnName());
            fromToMap.put(index, endIndex);
            endIndex = index;
        }
        // Y2 Right
        if (setting.isY2RightEnable()) {
            int index = getColumnIndexFromName(tableModel, setting.getAxisSetting(AxisPosition.Y2_RIGHT).getColumnName());
            fromToMap.put(index, endIndex);
            endIndex = index;
        }
        // Y2 Left
        if (setting.isY2LeftEnable()) {
            int index = getColumnIndexFromName(tableModel, setting.getAxisSetting(AxisPosition.Y2_LEFT).getColumnName());
            fromToMap.put(index, endIndex);
            endIndex = index;
        }
        // Y1 Right
        if (setting.isY1RightEnable()) {
            int index = getColumnIndexFromName(tableModel, setting.getAxisSetting(AxisPosition.Y1_RIGHT).getColumnName());
            fromToMap.put(index, endIndex);
            endIndex = index;
        }
        // Y1 Left
        if (setting.isY1Left2Enable()) {
            int index = getColumnIndexFromName(tableModel, setting.getAxisSetting(AxisPosition.Y1_LEFT2).getColumnName());
            fromToMap.put(index, endIndex);
            endIndex = index;
        }
        // Y1
        fromToMap.put(1, endIndex);
        
        // Judge x axis type and create dataset
        List<XYDataset> xyDatasetList = new ArrayList<>();
        List<CategoryDataset> categoryDatasetList = new ArrayList<>();
        Class<?> xClass = tableModel.getColumnClass(0);
        AxisDataType dataType = AxisDataType.getAxisDataType(xClass);
        
        if (dataType == AxisDataType.DATE) {
            // TimeSeriesCollection
            for (Entry<Integer, Integer> fromToEntry : fromToMap.entrySet()) {
                xyDatasetList.add(createTimeSeriesCollection(tableModel, fromToEntry.getKey(), fromToEntry.getValue()));
            }
        } else if (dataType == AxisDataType.XY) {
            // XYSeriesCollection
            for (Entry<Integer, Integer> fromToEntry : fromToMap.entrySet()) {
                xyDatasetList.add(createXYSeriesCollection(tableModel, fromToEntry.getKey(), fromToEntry.getValue()));
            }
        } else if (dataType == AxisDataType.CATEGORY) {
            // CategoryDataset
            for (Entry<Integer, Integer> fromToEntry : fromToMap.entrySet()) {
                categoryDatasetList.add(createCategoryDataset(tableModel, fromToEntry.getKey(), fromToEntry.getValue()));
            }
            return createMultipleDomainCategoryChart(categoryDatasetList, setting);
        }

        // if no dataset or dataset is less than expected, throw exception
        if (xyDatasetList.size() == 0 || xyDatasetList.size() < setting.getDatasetCount()) {
            throw new RuntimeException("No Dataset created or Dataset is less than expected. Created dataset is "
                        + xyDatasetList.size() + ", but exetected is " +setting.getDatasetCount());
        }

        //
        // create chart for XYDataset
        //
        if (xyDatasetList.size() == 1) {
            // single axis, single plot
            return createSingleLineChart(xyDatasetList.get(0), setting);
            
        } else {
            // multiple plot
            return createMultipleDomainChart(xyDatasetList, setting);
        }
    }

    // //////////////////////////////////////
    // Method - Create Chart
    // //////////////////////////////////////
    
    /**
     * Create single axis simple chart
     * 
     * @param xyDataset
     * @param setting
     * @return
     */
    private static JFreeChart createSingleLineChart(XYDataset xyDataset, ChartSetting setting) {
        JFreeChart chart = null;
        ChartAxisSetting xSetting = setting.getAxisSetting(AxisPosition.X1);
        ChartAxisSetting ySetting = setting.getAxisSetting(AxisPosition.Y1);
        
        if (xyDataset instanceof TimeSeriesCollection) {
            TimeSeriesCollection dataset = (TimeSeriesCollection) xyDataset;
            switch (ySetting.getChartType()) {
                case LINE:
                    chart = ChartFactory.createTimeSeriesChart(setting.getTitle(), xSetting.getLabel(), ySetting.getLabel(), dataset);
                    break;
                case LINE_MARK:
                    chart = ChartFactory.createTimeSeriesChart(setting.getTitle(), xSetting.getLabel(), ySetting.getLabel(), dataset);
                    ((XYLineAndShapeRenderer) chart.getXYPlot().getRenderer()).setBaseShapesVisible(true);
                    break;
                case BAR:
                    chart = ChartFactory.createXYBarChart(setting.getTitle(), xSetting.getLabel(), true, ySetting.getLabel(), dataset);
                    break;
                case BAR_DENSITY:
                    chart = ChartFactory.createXYBarChart(setting.getTitle(), xSetting.getLabel(), true, ySetting.getLabel(), dataset);
                    break;
                case SCATTER:
                case SCATTER_UD:
                    chart = ChartFactory.createScatterPlot(setting.getTitle(), xSetting.getLabel(), ySetting.getLabel(), dataset);
                    break;
                case OHLC:
                    chart = ChartFactory.createCandlestickChart(setting.getTitle(), xSetting.getLabel(), ySetting.getLabel(), convertToOHLCDataset(dataset), true);
                    chart.getXYPlot().setRenderer(RendererFactory.createXYItemRenderer(ChartType.OHLC, true, true, false));
                    break;
                case HIGH_LOW:
                    chart = ChartFactory.createHighLowChart(setting.getTitle(), xSetting.getLabel(), ySetting.getLabel(), convertToOHLCDataset(dataset), true);
                    break;
                default:
            }

        } else if (xyDataset instanceof XYSeriesCollection) {
            XYSeriesCollection dataset = (XYSeriesCollection) xyDataset;
            switch (ySetting.getChartType()) {
                case LINE:
                    chart = ChartFactory.createXYLineChart(setting.getTitle(), xSetting.getLabel(), ySetting.getLabel(), dataset);
                    break;
                case LINE_MARK:
                    chart = ChartFactory.createXYLineChart(setting.getTitle(), xSetting.getLabel(), ySetting.getLabel(), dataset);
                    ((XYLineAndShapeRenderer) chart.getXYPlot().getRenderer()).setBaseShapesVisible(true);
                    break;
                case BAR:
                    chart = ChartFactory.createXYBarChart(setting.getTitle(), xSetting.getLabel(), false, ySetting.getLabel(), dataset);
                    break;
                case BAR_DENSITY:
                    chart = ChartFactory.createHistogram(setting.getTitle(), xSetting.getLabel(), ySetting.getLabel(), dataset, PlotOrientation.VERTICAL, true, true, false);
                    break;
                case SCATTER:
                case SCATTER_UD:
                    chart = ChartFactory.createScatterPlot(setting.getTitle(), xSetting.getLabel(), ySetting.getLabel(), dataset);
                    break;
                default:
            }
        }
        if (chart == null) {
            return null;
        }
        
        // Set auto-range IncludeZero
        ValueAxis domainAxis = chart.getXYPlot().getDomainAxis();
        ValueAxis rangeAxis = chart.getXYPlot().getRangeAxis();
        if (domainAxis instanceof NumberAxis) {
            ((NumberAxis) domainAxis).setAutoRangeIncludesZero(xSetting.isIncludeZero());
        }
        if (rangeAxis instanceof NumberAxis) {
            ((NumberAxis) rangeAxis).setAutoRangeIncludesZero(ySetting.isIncludeZero());
        }
        return chart;
    }

    /**
     * Create multiple plot chart
     * 
     * @param xyDatasetList
     * @param setting
     * @return
     */
    private static JFreeChart createMultipleDomainChart(List<XYDataset> xyDatasetList, ChartSetting setting) {
        int datasetNo = 0;
        
        // Domain Axis
        ChartAxisSetting xSetting = setting.getAxisSetting(AxisPosition.X1);
        ValueAxis domainAxis = null;
        boolean isDateDomainAxis = false;
        if (xyDatasetList.get(0) instanceof TimeSeriesCollection) {
            domainAxis = new DateAxis(xSetting.getLabel());
            domainAxis.setLowerMargin(0.02);
            domainAxis.setUpperMargin(0.02);
            isDateDomainAxis = true;
        } else if (xyDatasetList.get(0) instanceof XYSeriesCollection) {
            domainAxis = new NumberAxis(xSetting.getLabel());
            ((NumberAxis) domainAxis).setAutoRangeIncludesZero(xSetting.isIncludeZero());
        } else {
            domainAxis = new NumberAxis(xSetting.getLabel());
            ((NumberAxis) domainAxis).setAutoRangeIncludesZero(xSetting.isIncludeZero());
        }

        // CombinedDomainXYPlot
        CombinedDomainXYPlot combinedDomainXYPlot = new CombinedDomainXYPlot(domainAxis);
        combinedDomainXYPlot.setGap(setting.getCombinedGap());
        combinedDomainXYPlot.setOrientation(PlotOrientation.VERTICAL);

        // plot 1 - Y1
        ChartAxisSetting y1LeftSetting = setting.getAxisSetting(AxisPosition.Y1);
        XYPlot xyPlot1 = createXYPlot(xyDatasetList.get(datasetNo), y1LeftSetting.getLabel(), AxisLocation.BOTTOM_OR_LEFT, 
                    RendererFactory.createXYItemRenderer(y1LeftSetting.getChartType(), isDateDomainAxis), y1LeftSetting.isIncludeZero());
        datasetNo++;
        // Y1 left2, right
        if (setting.isY1Left2Enable()) {
            addDatasetToPlotMapAxis(xyPlot1, 2, 0, xyDatasetList.get(datasetNo), AxisLocation.BOTTOM_OR_LEFT, RendererFactory.
                    createXYItemRenderer(setting.getAxisSetting(AxisPosition.Y1_LEFT2).getChartType(), isDateDomainAxis));
            datasetNo++;
        }
        if (setting.isY1RightEnable()) {
            ChartAxisSetting rightSetting = setting.getAxisSetting(AxisPosition.Y1_RIGHT);
            addDatasetToPlot(xyPlot1, 1, xyDatasetList.get(datasetNo), rightSetting.getLabel(), AxisLocation.BOTTOM_OR_RIGHT, 
                    RendererFactory.createXYItemRenderer(rightSetting.getChartType(), isDateDomainAxis), rightSetting.isIncludeZero());
            datasetNo++;
        }
        combinedDomainXYPlot.add(xyPlot1, (int) Math.round(100 * y1LeftSetting.getWeight()));
        
        // plot 2
        if (setting.getPlotCount() >= 2) {
            ChartAxisSetting leftSetting = setting.getAxisSetting(AxisPosition.Y2_LEFT);
            XYPlot xyPlot2 = createXYPlot(xyDatasetList.get(datasetNo), leftSetting.getLabel(), AxisLocation.TOP_OR_LEFT,
                    RendererFactory.createXYItemRenderer(leftSetting.getChartType(), isDateDomainAxis), leftSetting.isIncludeZero());
            datasetNo++;
            if (setting.isY2RightEnable()) {
                ChartAxisSetting rightSetting = setting.getAxisSetting(AxisPosition.Y2_RIGHT);
                addDatasetToPlot(xyPlot2, 1, xyDatasetList.get(datasetNo), rightSetting.getLabel(), AxisLocation.TOP_OR_RIGHT, 
                        RendererFactory.createXYItemRenderer(rightSetting.getChartType(), isDateDomainAxis), rightSetting.isIncludeZero());
                datasetNo++;
            }
            combinedDomainXYPlot.add(xyPlot2, (int) Math.round(100 * leftSetting.getWeight()));
        }
        
        // plot 3
        if (setting.getPlotCount() >= 3) {
            ChartAxisSetting leftSetting = setting.getAxisSetting(AxisPosition.Y3_LEFT);
            XYPlot xyPlot3 = createXYPlot(xyDatasetList.get(datasetNo), leftSetting.getLabel(), AxisLocation.TOP_OR_LEFT, 
                    RendererFactory.createXYItemRenderer(leftSetting.getChartType(), isDateDomainAxis), leftSetting.isIncludeZero());
            datasetNo++;
            if (setting.isY3RightEnable()) {
                ChartAxisSetting rightSetting = setting.getAxisSetting(AxisPosition.Y3_RIGHT);
                addDatasetToPlot(xyPlot3, 1, xyDatasetList.get(datasetNo), rightSetting.getLabel(), AxisLocation.TOP_OR_RIGHT, 
                        RendererFactory.createXYItemRenderer(rightSetting.getChartType(), isDateDomainAxis), rightSetting.isIncludeZero());
                datasetNo++;
            }
            combinedDomainXYPlot.add(xyPlot3, (int) Math.round(100 * leftSetting.getWeight()));
        }

        // plot 4
        if (setting.getPlotCount() >= 4) {
            ChartAxisSetting leftSetting = setting.getAxisSetting(AxisPosition.Y4_LEFT);
            XYPlot xyPlot4 = createXYPlot(xyDatasetList.get(datasetNo), leftSetting.getLabel(), AxisLocation.TOP_OR_LEFT,
                    RendererFactory.createXYItemRenderer(leftSetting.getChartType(), isDateDomainAxis), leftSetting.isIncludeZero());
            datasetNo++;
            if (setting.isY4RightEnable()) {
                ChartAxisSetting rightSetting = setting.getAxisSetting(AxisPosition.Y4_RIGHT);
                addDatasetToPlot(xyPlot4, 1, xyDatasetList.get(datasetNo), rightSetting.getLabel(), AxisLocation.TOP_OR_RIGHT, 
                        RendererFactory.createXYItemRenderer(rightSetting.getChartType(), isDateDomainAxis), rightSetting.isIncludeZero());
                datasetNo++;
            }
            combinedDomainXYPlot.add(xyPlot4, (int) Math.round(100 * leftSetting.getWeight()));
        }
        
        // plot 5
        if (setting.getPlotCount() >= 5) {
            ChartAxisSetting leftSetting = setting.getAxisSetting(AxisPosition.Y5_LEFT);
            XYPlot xyPlot5 = createXYPlot(xyDatasetList.get(datasetNo), leftSetting.getLabel(), AxisLocation.TOP_OR_LEFT,
                    RendererFactory.createXYItemRenderer(leftSetting.getChartType(), isDateDomainAxis), leftSetting.isIncludeZero());
            datasetNo++;
            if (setting.isY5RightEnable()) {
                ChartAxisSetting rightSetting = setting.getAxisSetting(AxisPosition.Y5_RIGHT);
                addDatasetToPlot(xyPlot5, 1, xyDatasetList.get(datasetNo), rightSetting.getLabel(), AxisLocation.TOP_OR_RIGHT, 
                        RendererFactory.createXYItemRenderer(rightSetting.getChartType(), isDateDomainAxis), rightSetting.isIncludeZero());
                datasetNo++;
            }
            combinedDomainXYPlot.add(xyPlot5, (int) Math.round(100 * leftSetting.getWeight()));
        }

        // create chart
        JFreeChart chart = null;
        if (setting.isSeparateLegend()) {
            chart = new JFreeChart(setting.getTitle(), JFreeChart.DEFAULT_TITLE_FONT, combinedDomainXYPlot, false);
            // separate legend
            addSepareteLegendTitle(chart);
        } else {
            chart = new JFreeChart(setting.getTitle(), JFreeChart.DEFAULT_TITLE_FONT, combinedDomainXYPlot, true);
        }
        
        ChartUtilities.applyCurrentTheme(chart);
        return chart;
    }

    private static XYPlot createXYPlot(XYDataset dataset, String label, AxisLocation location, XYItemRenderer renderer, boolean includeZero) {
        NumberAxis numberAxis = new NumberAxis(label);
        numberAxis.setAutoRangeIncludesZero(includeZero);
        XYPlot plot = null;
        if (dataset instanceof TimeSeriesCollection && 
                (renderer instanceof CandlestickRenderer || renderer instanceof HighLowRenderer)) {
            plot = new XYPlot(convertToOHLCDataset((TimeSeriesCollection)dataset), null, numberAxis, renderer);
        } else {
            plot = new XYPlot(dataset, null, numberAxis, renderer);
        }
        plot.setRangeAxisLocation(location);
        plot.setRangePannable(true);
        return plot;
    }

    private static void addDatasetToPlot(XYPlot plot, int index, XYDataset dataset, String label, AxisLocation location, XYItemRenderer renderer, boolean includeZero) {
        plot.setDataset(index, dataset);
        NumberAxis numberAxis = new NumberAxis(label);
        numberAxis.setAutoRangeIncludesZero(includeZero);
        plot.setRangeAxis(index, numberAxis);
        plot.setRangeAxisLocation(index, location);
        plot.setRenderer(index, renderer);
        plot.mapDatasetToRangeAxis(index, index);
        plot.setRangePannable(true);
    }
    
    private static void addDatasetToPlotMapAxis(XYPlot plot, int index, int rangeAxis, XYDataset dataset, AxisLocation location, XYItemRenderer renderer) {
        plot.setDataset(index, dataset);
        plot.setRangeAxisLocation(index, location);
        plot.setRenderer(index, renderer);
        plot.mapDatasetToRangeAxis(index, rangeAxis);
    }
    
    /**
     * Create composite title of left-right separated.
     * 
     * @param source1
     * @param source2
     * @return
     */
    private static void addSepareteLegendTitle(JFreeChart chart) {
        // regtend items
        List<LegendItemSource> leftSourceList = new ArrayList<>();
        List<LegendItemSource> rightSourceList = new ArrayList<>();
        
        Plot plot = chart.getPlot();
        if (plot instanceof CombinedDomainXYPlot) {
            // Multi-plot
            @SuppressWarnings("unchecked")
            List<XYPlot> plots = ((CombinedDomainXYPlot) plot).getSubplots();
            for (XYPlot xyPlot : plots) {
                // left for 1st dataset
                leftSourceList.add(xyPlot.getRenderer(0));
                // right for 2nd dataset
                if (xyPlot.getDatasetCount() >= 2) {
                    rightSourceList.add(xyPlot.getRenderer(1));
                }
                // left for 3nd dataset (only Y1 axis)
                if (xyPlot.getDatasetCount() >= 3) {
                    leftSourceList.add(xyPlot.getRenderer(2));
                }
            }
        } else if (plot instanceof XYPlot) {
            XYPlot xyPlot = (XYPlot) plot;
            // left for 1st dataset
            leftSourceList.add(xyPlot.getRenderer(0));
            // right for 2nd dataset
            if (xyPlot.getDatasetCount() >= 2) {
                rightSourceList.add(xyPlot.getRenderer(1));
            }
            // left for 3nd dataset (only Y1 axis)
            if (xyPlot.getDatasetCount() >= 3) {
                leftSourceList.add(xyPlot.getRenderer(2));
            }
        }
        
        // create legends
        LegendItemSource[] leftSources = leftSourceList.toArray(new LegendItemSource[leftSourceList.size()]);
        LegendItemSource[] rightSources = rightSourceList.toArray(new LegendItemSource[rightSourceList.size()]);
        
        LegendTitle leftLegend = new LegendTitle(null);
        leftLegend.setSources(leftSources);
        leftLegend.setMargin(new RectangleInsets(2.0D, 2.0D, 2.0D, 2.0D));
        leftLegend.setFrame(new BlockBorder());

        LegendTitle rightLegend = new LegendTitle(null);
        rightLegend.setSources(rightSources);
        rightLegend.setMargin(new RectangleInsets(2.0D, 2.0D, 2.0D, 2.0D));
        rightLegend.setFrame(new BlockBorder());
        
        // Container for legends
        BlockContainer blockContainer = new BlockContainer(new BorderArrangement());
        blockContainer.add(leftLegend, RectangleEdge.LEFT);
        blockContainer.add(rightLegend, RectangleEdge.RIGHT);
        blockContainer.add(new EmptyBlock(2000.0D, 0.0D));

        // create sub-title of legend
        CompositeTitle compositeTitle = new CompositeTitle(blockContainer);
        compositeTitle.setPosition(RectangleEdge.BOTTOM);
        
        // add
        chart.addSubtitle(compositeTitle);
    }
    
    /**
     * Get column index from column name
     * 
     * @param table
     * @param name
     * @return
     */
    private static int getColumnIndexFromName(KTableModel table, String name) {
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (name.equals(table.getColumnName(i))) {
                return i;
            }
        }
        throw new RuntimeException("Column Name : " + name + " is not exist in KTable." );
    }

    /**
     * Create multiple plot category chart
     * 
     * @param xyDatasetList
     * @param setting
     * @return
     */
    private static JFreeChart createMultipleDomainCategoryChart(List<CategoryDataset> categoryDatasetList, ChartSetting setting) {
        int datasetNo = 0;
        
        // Domain Axis
        ChartAxisSetting xSetting = setting.getAxisSetting(AxisPosition.X1);
        CategoryAxis categoryAxis = new CategoryAxis(xSetting.getLabel());

        // CombinedDomainXYPlot
        CombinedDomainCategoryPlot combinedDomainCategoryPlot = new CombinedDomainCategoryPlot(categoryAxis);
        combinedDomainCategoryPlot.setGap(-5.0D);
        combinedDomainCategoryPlot.setOrientation(PlotOrientation.VERTICAL);

        // plot 1 - Y1
        ChartAxisSetting y1Setting = setting.getAxisSetting(AxisPosition.Y1);
        CategoryPlot plot1 = createCategoryPlot(categoryDatasetList.get(datasetNo), y1Setting.getLabel(), AxisLocation.BOTTOM_OR_LEFT, 
                    RendererFactory.createCategoryItemRenderer(y1Setting.getChartType()));
        datasetNo++;
        combinedDomainCategoryPlot.add(plot1, (int) Math.round(100 * y1Setting.getWeight()));
        
        // plot 2
        if (setting.getPlotCount() >= 2) {
            ChartAxisSetting leftSetting = setting.getAxisSetting(AxisPosition.Y2_LEFT);
            CategoryPlot plot2 = createCategoryPlot(categoryDatasetList.get(datasetNo), leftSetting.getLabel(), AxisLocation.TOP_OR_LEFT,
                    RendererFactory.createCategoryItemRenderer(leftSetting.getChartType()));
            datasetNo++;
            combinedDomainCategoryPlot.add(plot2, (int) Math.round(100 * leftSetting.getWeight()));
        }
        
        // plot 3
        if (setting.getPlotCount() >= 3) {
            ChartAxisSetting leftSetting = setting.getAxisSetting(AxisPosition.Y3_LEFT);
            CategoryPlot plot3 = createCategoryPlot(categoryDatasetList.get(datasetNo), leftSetting.getLabel(), AxisLocation.TOP_OR_LEFT,
                    RendererFactory.createCategoryItemRenderer(leftSetting.getChartType()));
            datasetNo++;
            combinedDomainCategoryPlot.add(plot3, (int) Math.round(100 * leftSetting.getWeight()));
        }

        // plot 4
        if (setting.getPlotCount() >= 4) {
            ChartAxisSetting leftSetting = setting.getAxisSetting(AxisPosition.Y4_LEFT);
            CategoryPlot plot4 = createCategoryPlot(categoryDatasetList.get(datasetNo), leftSetting.getLabel(), AxisLocation.TOP_OR_LEFT,
                    RendererFactory.createCategoryItemRenderer(leftSetting.getChartType()));
            datasetNo++;
            combinedDomainCategoryPlot.add(plot4, (int) Math.round(100 * leftSetting.getWeight()));
        }
        
        // plot 5
        if (setting.getPlotCount() >= 5) {
            ChartAxisSetting leftSetting = setting.getAxisSetting(AxisPosition.Y5_LEFT);
            CategoryPlot plot5 = createCategoryPlot(categoryDatasetList.get(datasetNo), leftSetting.getLabel(), AxisLocation.TOP_OR_LEFT,
                    RendererFactory.createCategoryItemRenderer(leftSetting.getChartType()));
            datasetNo++;
            combinedDomainCategoryPlot.add(plot5, (int) Math.round(100 * leftSetting.getWeight()));
        }

        // create chart
        JFreeChart chart = new JFreeChart(setting.getTitle(), JFreeChart.DEFAULT_TITLE_FONT, combinedDomainCategoryPlot, true);
        ChartUtilities.applyCurrentTheme(chart);

        return chart;
    }
    
    private static CategoryPlot createCategoryPlot(CategoryDataset dataset, String label, AxisLocation location, CategoryItemRenderer renderer) {
        NumberAxis numberAxis = new NumberAxis(label);
        numberAxis.setAutoRangeIncludesZero(false);
        CategoryPlot plot = new CategoryPlot(dataset, null, numberAxis, renderer);
        plot.setRangeAxisLocation(location);
        plot.setRangePannable(true);
        return plot;
    }
    
    // //////////////////////////////////////
    // Method - Dataset
    // //////////////////////////////////////

    private static TimeSeriesCollection createTimeSeriesCollection(KTableModel table, int fromCol, int toCol) {
        Class<?> klass = table.getColumnClass(0);
        TimeSeriesCollection tsc = new TimeSeriesCollection(tz);

        for (int col = fromCol; col < toCol; col++) {
            TimeSeries series = null;

            try {
                if (klass == K.KDateVector.class) {
                    series = new TimeSeries(table.getColumnName(col));
                    K.KDateVector dates = (K.KDateVector) table.getColumn(0);

                    for (int row = 0; row < dates.getLength(); row++) {
                        K.KDate date = (K.KDate) dates.at(row);
                        Day day = new Day(date.toDate(), tz, locale);
                        addData(series, day, table, row, col);
                    }
                } else if (klass == K.KTimeVector.class) {
                    series = new TimeSeries(table.getColumnName(col));

                    K.KTimeVector times = (K.KTimeVector) table.getColumn(0);
                    for (int row = 0; row < table.getRowCount(); row++) {
                        K.KTime time = (K.KTime) times.at(row);
                        Millisecond ms = new Millisecond(time.toTime(), tz, locale);
                        addData(series, ms, table, row, col);
                    }
                } else if (klass == K.KTimestampVector.class) {
                    series = new TimeSeries(table.getColumnName(col));
                    K.KTimestampVector dates = (K.KTimestampVector) table.getColumn(0);

                    for (int row = 0; row < dates.getLength(); row++) {
                        K.KTimestamp date = (K.KTimestamp) dates.at(row);
                        Millisecond ms = new Millisecond(date.toTimestamp(), tz, locale);
                        addData(series, ms, table, row, col);
                    }
                } else if (klass == K.KTimespanVector.class) {
                    series = new TimeSeries(table.getColumnName(col));

                    K.KTimespanVector times = (K.KTimespanVector) table.getColumn(0);
                    for (int row = 0; row < table.getRowCount(); row++) {
                        K.KTimespan time = (K.KTimespan) times.at(row);
                        Millisecond ms = new Millisecond(time.toTime(), tz, locale);
                        addData(series, ms, table, row, col);
                    }
                } else if (klass == K.KDatetimeVector.class) {
                    series = new TimeSeries(table.getColumnName(col));
                    K.KDatetimeVector times = (K.KDatetimeVector) table.getColumn(0);

                    for (int row = 0; row < table.getRowCount(); row++) {
                        K.KDatetime time = (K.KDatetime) times.at(row);
                        Millisecond ms = new Millisecond(time.toTimestamp(), tz, locale);
                        addData(series, ms, table, row, col);
                    }
                } else if (klass == K.KMonthVector.class) {
                    series = new TimeSeries(table.getColumnName(col));
                    K.KMonthVector times = (K.KMonthVector) table.getColumn(0);
                    for (int row = 0; row < table.getRowCount(); row++) {
                        K.Month time = (K.Month) times.at(row);
                        int m = time.i + 24000;
                        int y = m / 12;
                        m = 1 + m % 12;

                        Month month = new Month(m, y);
                        addData(series, month, table, row, col);
                    }
                } else if (klass == K.KSecondVector.class) {
                    series = new TimeSeries(table.getColumnName(col));
                    K.KSecondVector times = (K.KSecondVector) table.getColumn(0);
                    for (int row = 0; row < table.getRowCount(); row++) {
                        K.Second time = (K.Second) times.at(row);
                        Second second = new Second(time.i % 60, time.i / 60, 0, 1, 1, 2001);
                        addData(series, second, table, row, col);
                    }
                } else if (klass == K.KMinuteVector.class) {
                    series = new TimeSeries(table.getColumnName(col));
                    K.KMinuteVector times = (K.KMinuteVector) table.getColumn(0);
                    for (int row = 0; row < table.getRowCount(); row++) {
                        K.Minute time = (K.Minute) times.at(row);
                        Minute minute = new Minute(time.i % 60, time.i / 60, 1, 1, 2001);
                        addData(series, minute, table, row, col);
                    }
                }
            } catch (SeriesException e) {
                System.err.println("Error adding to series");
            }

            if (series.getItemCount() > 0)
                tsc.addSeries(series);
        }

        return tsc;
    }

    private static void addData(TimeSeries series, RegularTimePeriod period, KTableModel table, int row, int col) {
        Object o = table.getValueAt(row, col);
        if (o instanceof K.KBase
                && !((K.KBase) o).isNull()
                && o instanceof ToDouble) {
            double value = ((ToDouble) o).toDouble();
            if (Double.isFinite(value)) {
                series.addOrUpdate(period, ((ToDouble) o).toDouble());
            }
        }
    }
    
    private static XYSeriesCollection createXYSeriesCollection(KTableModel table, int fromCol, int toCol) {
        XYSeriesCollection xysc = new XYSeriesCollection();

        for (int col = fromCol; col < toCol; col++) {
            XYSeries series = null;

            try {
                series = new XYSeries(table.getColumnName(col));

                for (int row = 0; row < table.getRowCount(); row++) {
                    // check x
                    KBase xBase = (KBase) table.getValueAt(row, 0);
                    double x = ((ToDouble) xBase).toDouble();
                    if (xBase.isNull() || Double.isInfinite(x)) {
                        continue;
                    }
                    // check y
                    Object yObject = table.getValueAt(row, col);
                    if (yObject instanceof K.KBase
                            && !((K.KBase) yObject).isNull()
                            && yObject instanceof ToDouble) {
                        double y = ((ToDouble) yObject).toDouble();
                        if (Double.isFinite(y)) {
                            series.add(x, y);
                        }
                    }
                }
            } catch (SeriesException e) {
                System.err.println("Error adding to series");
            }

            if (series.getItemCount() > 0)
                xysc.addSeries(series);
        }

        return xysc;
    }
    
    private static CategoryDataset createCategoryDataset(KTableModel table, int fromCol, int toCol) {
        String defaultSeries = "Series";
        String category;
        String series;
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        int seriesCol = -1;
        int valueCol = -1;
        // search series/value column
        for (int col = toCol - 1; col >= fromCol; col--) {
            Object object = table.getValueAt(0, col);
            if (canBeCategory(object.getClass())) {
                seriesCol = col;
            } else if (object instanceof ToDouble) {
                valueCol = col;
            }
        }
        
        if (valueCol < 0) {
            return null;
        }
        
        for (int row = 0; row < table.getRowCount(); row++) {
            category = ((KBase)table.getValueAt(row, 0)).toString(false);
            if (seriesCol > 0) {
                series = ((KBase)table.getValueAt(row, seriesCol)).toString(false);
            } else {
                series = defaultSeries;
            }
            double value = ((ToDouble) table.getValueAt(row, valueCol)).toDouble();
            dataset.addValue(value, series, category);
        }
        return dataset;
    }
    
    private static boolean canBeCategory(Class<?> kClass) {
        if ((kClass == K.KSymbol.class)
                || (kClass == K.KCharacterVector.class)
                || (kClass == K.KCharacter.class)) {
            return true;
        } else {
            return false;
        }
    }
    
    private static OHLCDataset convertToOHLCDataset(TimeSeriesCollection tsc) {
        // If name has open/high/low/close, use them for OHLC items
        // If not and count = 4, use 1-open, 2-high, 3-low, 4-close
        if (tsc.getSeriesCount() < 4) {
            return null;
        }
        
        // search ohlc coloumns
        int openIndex = -1;
        int highIndex = -1;
        int lowIndex = -1;
        int closeIndex = -1;
        for (int seriesIndex = 0; seriesIndex < tsc.getSeriesCount(); seriesIndex++) {
            switch (((String) tsc.getSeries(seriesIndex).getKey()).toUpperCase()) {
                case "OPEN":
                case "O":
                    openIndex = seriesIndex;
                    break;
                case "HIGH":
                case "H":
                    highIndex = seriesIndex;                    
                    break;
                case "LOW":
                case "L":
                    lowIndex = seriesIndex;                    
                    break;
                case "CLOSE":
                case "C":
                    closeIndex = seriesIndex;
                    break;
                default:
                    break;
            }
        }
        // If names are not decided, use as order.
        if (openIndex < 0 || highIndex < 0 || lowIndex < 0 || closeIndex < 0) {
            openIndex = 0;
            highIndex = 1;
            lowIndex = 2;
            closeIndex = 3;
        }

        OHLCSeries ohlcSeries = new OHLCSeries("OHLC");
        for (int i = 0; i < tsc.getSeries(0).getItemCount(); i++) {
            ohlcSeries.add(
                    tsc.getSeries(0).getTimePeriod(i), 
                    (double) tsc.getSeries(openIndex).getValue(i), 
                    (double) tsc.getSeries(highIndex).getValue(i), 
                    (double) tsc.getSeries(lowIndex).getValue(i), 
                    (double) tsc.getSeries(closeIndex).getValue(i));
        }
        OHLCSeriesCollection dataset = new OHLCSeriesCollection();
        dataset.addSeries(ohlcSeries);
        
        return dataset;
    }

}
