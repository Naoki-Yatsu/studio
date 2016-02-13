package studio.chart;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TimeZone;
import java.util.TreeMap;

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
import org.jfree.chart.renderer.xy.XYItemRenderer;
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
        // create separate dataset for each axis, with REVERSE order.
        //
        NavigableMap<Integer, Integer> fromToMap = new TreeMap<>();
        NavigableMap<Integer, InvalidValueType> fromInvalidValueMap = new TreeMap<>();
        int endIndex = tableModel.getColumnCount();

        // reverse order
        List<AxisPosition> targetAxisList = AxisPosition.AXIS_SUB_ALL;
        for (int i = targetAxisList.size() - 1; i >= 0; i--) {
            AxisPosition axisPosition = targetAxisList.get(i);
            if (setting.isAxisEnable(axisPosition)) {
                int index = getColumnIndexFromName(tableModel, setting.getAxisSetting(axisPosition).getColumnName());
                fromToMap.put(index, endIndex);
                fromInvalidValueMap.put(index, setting.getAxisSetting(axisPosition).getInvalidValueType());
                endIndex = index;
            }
        }
        // Y1, in the end
        fromToMap.put(1, endIndex);
        fromInvalidValueMap.put(1, setting.getAxisSetting(AxisPosition.Y1).getInvalidValueType());
        
        // Judge x axis type and create dataset
        List<XYDataset> xyDatasetList = new ArrayList<>();
        List<CategoryDataset> categoryDatasetList = new ArrayList<>();
        Class<?> xClass = tableModel.getColumnClass(0);
        AxisDataType dataType = AxisDataType.getAxisDataType(xClass);
        
        if (dataType == AxisDataType.DATE) {
            // TimeSeriesCollection
            for (Entry<Integer, Integer> fromToEntry : fromToMap.entrySet()) {
                xyDatasetList.add(createTimeSeriesCollection(tableModel, fromToEntry.getKey(), fromToEntry.getValue(), fromInvalidValueMap.get(fromToEntry.getKey())));
            }
        } else if (dataType == AxisDataType.XY) {
            // XYSeriesCollection
            for (Entry<Integer, Integer> fromToEntry : fromToMap.entrySet()) {
                xyDatasetList.add(createXYSeriesCollection(tableModel, fromToEntry.getKey(), fromToEntry.getValue(), fromInvalidValueMap.get(fromToEntry.getKey())));
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
        // Domain Axis
        // ChartAxisSetting xSetting = setting.getAxisSetting(AxisPosition.X1);
        ValueAxis domainAxis = null;
        boolean isDateDomainAxis = false;
        if (xyDataset instanceof TimeSeriesCollection) {
            domainAxis = new DateAxis();
            domainAxis.setLowerMargin(0.02);
            domainAxis.setUpperMargin(0.02);
            isDateDomainAxis = true;
        } else if (xyDataset instanceof XYSeriesCollection) {
            domainAxis = new NumberAxis();
        } else {
            domainAxis = new NumberAxis();
        }

        // Range Axis / Plot
        ChartAxisSetting ySetting = setting.getAxisSetting(AxisPosition.Y1);
        XYPlot plot = createXYPlot(xyDataset, AxisLocation.BOTTOM_OR_LEFT, 
                RendererFactory.createXYItemRenderer(ySetting, isDateDomainAxis, xyDataset.getSeriesCount()), ySetting.getChartType());
        plot.setDomainAxis(domainAxis);
        
        // create chart
        JFreeChart chart = new JFreeChart(setting.getTitle(), JFreeChart.DEFAULT_TITLE_FONT, plot, setting.showLegend());
        ChartUtilities.applyCurrentTheme(chart);
        
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
        // ChartAxisSetting xSetting = setting.getAxisSetting(AxisPosition.X1);
        ValueAxis domainAxis = null;
        boolean isDateDomainAxis = false;
        if (xyDatasetList.get(0) instanceof TimeSeriesCollection) {
            domainAxis = new DateAxis();
            domainAxis.setLowerMargin(0.02);
            domainAxis.setUpperMargin(0.02);
            isDateDomainAxis = true;
        } else if (xyDatasetList.get(0) instanceof XYSeriesCollection) {
            domainAxis = new NumberAxis();
        } else {
            domainAxis = new NumberAxis();
        }

        // CombinedDomainXYPlot
        CombinedDomainXYPlot combinedDomainXYPlot = new CombinedDomainXYPlot(domainAxis);
        combinedDomainXYPlot.setGap(setting.getCombinedGap());
        combinedDomainXYPlot.setOrientation(PlotOrientation.VERTICAL);

        // plot 1 - Y1
        ChartAxisSetting axisSetting = setting.getAxisSetting(AxisPosition.Y1);
        XYPlot xyPlot = createXYPlot(xyDatasetList.get(datasetNo), AxisLocation.BOTTOM_OR_LEFT, 
                    RendererFactory.createXYItemRenderer(axisSetting, isDateDomainAxis, xyDatasetList.get(datasetNo).getSeriesCount()), axisSetting.getChartType());
        combinedDomainXYPlot.add(xyPlot, (int) Math.round(100 * axisSetting.getWeight()));
        datasetNo++;
        // Y1 left 1-4
        AxisPosition axisPosition = AxisPosition.Y1_LEFT1;
        if (setting.isAxisEnable(axisPosition)) {
            axisSetting = setting.getAxisSetting(axisPosition);
            addDatasetToPlotMapAxis(xyPlot, 2, 0, xyDatasetList.get(datasetNo), AxisLocation.BOTTOM_OR_LEFT,
                    RendererFactory.createXYItemRenderer(axisSetting, isDateDomainAxis, xyDatasetList.get(datasetNo).getSeriesCount()), axisSetting.getChartType());
            datasetNo++;
        }
        axisPosition = AxisPosition.Y1_LEFT2;
        if (setting.isAxisEnable(axisPosition)) {
            axisSetting = setting.getAxisSetting(axisPosition);
            addDatasetToPlotMapAxis(xyPlot, 3, 0, xyDatasetList.get(datasetNo), AxisLocation.BOTTOM_OR_LEFT,
                    RendererFactory.createXYItemRenderer(axisSetting, isDateDomainAxis, xyDatasetList.get(datasetNo).getSeriesCount()), axisSetting.getChartType());
            datasetNo++;
        }
        axisPosition = AxisPosition.Y1_LEFT3;
        if (setting.isAxisEnable(axisPosition)) {
            axisSetting = setting.getAxisSetting(axisPosition);
            addDatasetToPlotMapAxis(xyPlot, 4, 0, xyDatasetList.get(datasetNo), AxisLocation.BOTTOM_OR_LEFT,
                    RendererFactory.createXYItemRenderer(axisSetting, isDateDomainAxis, xyDatasetList.get(datasetNo).getSeriesCount()), axisSetting.getChartType());
            datasetNo++;
        }
        axisPosition = AxisPosition.Y1_LEFT4;
        if (setting.isAxisEnable(axisPosition)) {
            axisSetting = setting.getAxisSetting(axisPosition);
            addDatasetToPlotMapAxis(xyPlot, 5, 0, xyDatasetList.get(datasetNo), AxisLocation.BOTTOM_OR_LEFT,
                    RendererFactory.createXYItemRenderer(axisSetting, isDateDomainAxis, xyDatasetList.get(datasetNo).getSeriesCount()), axisSetting.getChartType());
            datasetNo++;
        }
        // Y1 right
        axisPosition = AxisPosition.Y1_RIGHT;
        if (setting.isAxisEnable(axisPosition)) {
            axisSetting = setting.getAxisSetting(axisPosition);
            addDatasetToPlot(xyPlot, 1, xyDatasetList.get(datasetNo), AxisLocation.BOTTOM_OR_RIGHT, 
                    RendererFactory.createXYItemRenderer(axisSetting, isDateDomainAxis, xyDatasetList.get(datasetNo).getSeriesCount()), axisSetting.getChartType());
            datasetNo++;
        }

        // plot 2
        if (setting.getPlotCount() >= 2) {
            axisPosition = AxisPosition.Y2_LEFT;
            axisSetting = setting.getAxisSetting(axisPosition);
            xyPlot = createXYPlot(xyDatasetList.get(datasetNo), AxisLocation.TOP_OR_LEFT,
                    RendererFactory.createXYItemRenderer(axisSetting, isDateDomainAxis, xyDatasetList.get(datasetNo).getSeriesCount()), axisSetting.getChartType());
            combinedDomainXYPlot.add(xyPlot, (int) Math.round(100 * axisSetting.getWeight()));
            datasetNo++;
            axisPosition = AxisPosition.Y2_RIGHT;
            if (setting.isAxisEnable(axisPosition)) {
                axisSetting = setting.getAxisSetting(axisPosition);
                addDatasetToPlot(xyPlot, 1, xyDatasetList.get(datasetNo), AxisLocation.TOP_OR_RIGHT, 
                        RendererFactory.createXYItemRenderer(axisSetting, isDateDomainAxis, xyDatasetList.get(datasetNo).getSeriesCount()), axisSetting.getChartType());
                datasetNo++;
            }
        }
        
        // plot 3
        if (setting.getPlotCount() >= 3) {
            axisPosition = AxisPosition.Y3_LEFT;
            axisSetting = setting.getAxisSetting(axisPosition);
            xyPlot = createXYPlot(xyDatasetList.get(datasetNo), AxisLocation.TOP_OR_LEFT,
                    RendererFactory.createXYItemRenderer(axisSetting, isDateDomainAxis, xyDatasetList.get(datasetNo).getSeriesCount()), axisSetting.getChartType());
            combinedDomainXYPlot.add(xyPlot, (int) Math.round(100 * axisSetting.getWeight()));
            datasetNo++;
            axisPosition = AxisPosition.Y3_RIGHT;
            if (setting.isAxisEnable(axisPosition)) {
                axisSetting = setting.getAxisSetting(axisPosition);
                addDatasetToPlot(xyPlot, 1, xyDatasetList.get(datasetNo), AxisLocation.TOP_OR_RIGHT, 
                        RendererFactory.createXYItemRenderer(axisSetting, isDateDomainAxis, xyDatasetList.get(datasetNo).getSeriesCount()), axisSetting.getChartType());
                datasetNo++;
            }
        }

        // plot 4
        if (setting.getPlotCount() >= 4) {
            axisPosition = AxisPosition.Y4_LEFT;
            axisSetting = setting.getAxisSetting(axisPosition);
            xyPlot = createXYPlot(xyDatasetList.get(datasetNo), AxisLocation.TOP_OR_LEFT,
                    RendererFactory.createXYItemRenderer(axisSetting, isDateDomainAxis, xyDatasetList.get(datasetNo).getSeriesCount()), axisSetting.getChartType());
            combinedDomainXYPlot.add(xyPlot, (int) Math.round(100 * axisSetting.getWeight()));
            datasetNo++;
            axisPosition = AxisPosition.Y4_RIGHT;
            if (setting.isAxisEnable(axisPosition)) {
                axisSetting = setting.getAxisSetting(axisPosition);
                addDatasetToPlot(xyPlot, 1, xyDatasetList.get(datasetNo), AxisLocation.TOP_OR_RIGHT, 
                        RendererFactory.createXYItemRenderer(axisSetting, isDateDomainAxis, xyDatasetList.get(datasetNo).getSeriesCount()), axisSetting.getChartType());
                datasetNo++;
            }
        }
        
        // plot 5
        if (setting.getPlotCount() >= 5) {
            axisPosition = AxisPosition.Y5_LEFT;
            axisSetting = setting.getAxisSetting(axisPosition);
            xyPlot = createXYPlot(xyDatasetList.get(datasetNo), AxisLocation.TOP_OR_LEFT,
                    RendererFactory.createXYItemRenderer(axisSetting, isDateDomainAxis, xyDatasetList.get(datasetNo).getSeriesCount()), axisSetting.getChartType());
            combinedDomainXYPlot.add(xyPlot, (int) Math.round(100 * axisSetting.getWeight()));
            datasetNo++;
            axisPosition = AxisPosition.Y5_RIGHT;
            if (setting.isAxisEnable(axisPosition)) {
                axisSetting = setting.getAxisSetting(axisPosition);
                addDatasetToPlot(xyPlot, 1, xyDatasetList.get(datasetNo), AxisLocation.TOP_OR_RIGHT, 
                        RendererFactory.createXYItemRenderer(axisSetting, isDateDomainAxis, xyDatasetList.get(datasetNo).getSeriesCount()), axisSetting.getChartType());
                datasetNo++;
            }
        }

        // create chart
        JFreeChart chart = null;
        if (setting.isSeparateLegend()) {
            chart = new JFreeChart(setting.getTitle(), JFreeChart.DEFAULT_TITLE_FONT, combinedDomainXYPlot, false);
            // separate legend
            addSepareteLegendTitle(chart);
        } else {
            chart = new JFreeChart(setting.getTitle(), JFreeChart.DEFAULT_TITLE_FONT, combinedDomainXYPlot, setting.showLegend());
        }
        
        ChartUtilities.applyCurrentTheme(chart);
        return chart;
    }

    private static XYPlot createXYPlot(XYDataset dataset, AxisLocation location, XYItemRenderer renderer, ChartType chartType) {
        if (chartType == ChartType.OHLC || chartType == ChartType.OHLC2 || chartType == ChartType.HIGH_LOW) {
            if (dataset instanceof TimeSeriesCollection) {
                dataset = convertToOHLCDataset((TimeSeriesCollection)dataset);
            } else {
                throw new RuntimeException(dataset.getClass().getSimpleName() + " cannot convert to OHLC dataset.");
            }
        }
        NumberAxis numberAxis = new NumberAxis();
        // XYPlot plot = new XYPlot(dataset, null, numberAxis, renderer);
        XYPlot plot = new XYNullablePlot(dataset, null, numberAxis, renderer);
        plot.setRangeAxisLocation(location);
        plot.setRangePannable(true);
        return plot;
    }

    /**
     * Add dataset to plot using "new" axis
     */
    private static void addDatasetToPlot(XYPlot plot, int index, XYDataset dataset, AxisLocation location, XYItemRenderer renderer, ChartType chartType) {
        if (chartType == ChartType.OHLC || chartType == ChartType.OHLC2 || chartType == ChartType.HIGH_LOW) {
            if (dataset instanceof TimeSeriesCollection) {
                dataset = convertToOHLCDataset((TimeSeriesCollection)dataset);
            } else {
                throw new RuntimeException(dataset.getClass().getSimpleName() + " cannot convert to OHLC dataset.");
            }
        }
        plot.setDataset(index, dataset);
        NumberAxis numberAxis = new NumberAxis();
        plot.setRangeAxis(index, numberAxis);
        plot.setRangeAxisLocation(index, location);
        plot.setRenderer(index, renderer);
        plot.mapDatasetToRangeAxis(index, index);
        plot.setRangePannable(true);
    }
    
    /**
     * Add dataset to plot using "existing" axis
     */
    private static void addDatasetToPlotMapAxis(XYPlot plot, int index, int rangeIndex, XYDataset dataset, AxisLocation location, XYItemRenderer renderer, ChartType chartType) {
        if (chartType == ChartType.OHLC || chartType == ChartType.OHLC2 || chartType == ChartType.HIGH_LOW) {
            if (dataset instanceof TimeSeriesCollection) {
                dataset = convertToOHLCDataset((TimeSeriesCollection)dataset);
            } else {
                throw new RuntimeException(dataset.getClass().getSimpleName() + " cannot convert to OHLC dataset.");
            }
        }
        plot.setDataset(index, dataset);
        plot.setRangeAxisLocation(index, location);
        plot.setRenderer(index, renderer);
        plot.mapDatasetToRangeAxis(index, rangeIndex);
    }
    
    /**
     * Create composite title of left-right separated.
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
                for (int i = 0; i < xyPlot.getDatasetCount(); i++) {
                    if (i == 1) {
                        // right for dataset 1
                        rightSourceList.add(xyPlot.getRenderer(i));
                    } else {
                        // left for dataset 0,2,3,...
                        leftSourceList.add(xyPlot.getRenderer(i));
                    }
                }
            }
        } else if (plot instanceof XYPlot) {
            XYPlot xyPlot = (XYPlot) plot;
            for (int i = 0; i < xyPlot.getDatasetCount(); i++) {
                if (i == 1) {
                    // right for dataset 1
                    rightSourceList.add(xyPlot.getRenderer(i));
                } else {
                    // left for dataset 0,2,3,...
                    leftSourceList.add(xyPlot.getRenderer(i));
                }
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
                    RendererFactory.createCategoryItemRenderer(y1Setting));
        datasetNo++;
        combinedDomainCategoryPlot.add(plot1, (int) Math.round(100 * y1Setting.getWeight()));
        
        // plot 2
        if (setting.getPlotCount() >= 2) {
            ChartAxisSetting leftSetting = setting.getAxisSetting(AxisPosition.Y2_LEFT);
            CategoryPlot plot2 = createCategoryPlot(categoryDatasetList.get(datasetNo), leftSetting.getLabel(), AxisLocation.TOP_OR_LEFT,
                    RendererFactory.createCategoryItemRenderer(leftSetting));
            datasetNo++;
            combinedDomainCategoryPlot.add(plot2, (int) Math.round(100 * leftSetting.getWeight()));
        }
        
        // plot 3
        if (setting.getPlotCount() >= 3) {
            ChartAxisSetting leftSetting = setting.getAxisSetting(AxisPosition.Y3_LEFT);
            CategoryPlot plot3 = createCategoryPlot(categoryDatasetList.get(datasetNo), leftSetting.getLabel(), AxisLocation.TOP_OR_LEFT,
                    RendererFactory.createCategoryItemRenderer(leftSetting));
            datasetNo++;
            combinedDomainCategoryPlot.add(plot3, (int) Math.round(100 * leftSetting.getWeight()));
        }

        // plot 4
        if (setting.getPlotCount() >= 4) {
            ChartAxisSetting leftSetting = setting.getAxisSetting(AxisPosition.Y4_LEFT);
            CategoryPlot plot4 = createCategoryPlot(categoryDatasetList.get(datasetNo), leftSetting.getLabel(), AxisLocation.TOP_OR_LEFT,
                    RendererFactory.createCategoryItemRenderer(leftSetting));
            datasetNo++;
            combinedDomainCategoryPlot.add(plot4, (int) Math.round(100 * leftSetting.getWeight()));
        }
        
        // plot 5
        if (setting.getPlotCount() >= 5) {
            ChartAxisSetting leftSetting = setting.getAxisSetting(AxisPosition.Y5_LEFT);
            CategoryPlot plot5 = createCategoryPlot(categoryDatasetList.get(datasetNo), leftSetting.getLabel(), AxisLocation.TOP_OR_LEFT,
                    RendererFactory.createCategoryItemRenderer(leftSetting));
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

    private static TimeSeriesCollection createTimeSeriesCollection(KTableModel table, int fromCol, int toCol, InvalidValueType invalidValueType) {
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
                        addData(series, day, table, row, col, invalidValueType);
                    }
                } else if (klass == K.KTimeVector.class) {
                    series = new TimeSeries(table.getColumnName(col));

                    K.KTimeVector times = (K.KTimeVector) table.getColumn(0);
                    for (int row = 0; row < table.getRowCount(); row++) {
                        K.KTime time = (K.KTime) times.at(row);
                        Millisecond ms = new Millisecond(time.toTime(), tz, locale);
                        addData(series, ms, table, row, col, invalidValueType);
                    }
                } else if (klass == K.KTimestampVector.class) {
                    series = new TimeSeries(table.getColumnName(col));
                    K.KTimestampVector dates = (K.KTimestampVector) table.getColumn(0);

                    for (int row = 0; row < dates.getLength(); row++) {
                        K.KTimestamp date = (K.KTimestamp) dates.at(row);
                        Millisecond ms = new Millisecond(date.toTimestamp(), tz, locale);
                        addData(series, ms, table, row, col, invalidValueType);
                    }
                } else if (klass == K.KTimespanVector.class) {
                    series = new TimeSeries(table.getColumnName(col));

                    K.KTimespanVector times = (K.KTimespanVector) table.getColumn(0);
                    for (int row = 0; row < table.getRowCount(); row++) {
                        K.KTimespan time = (K.KTimespan) times.at(row);
                        Millisecond ms = new Millisecond(time.toTime(), tz, locale);
                        addData(series, ms, table, row, col, invalidValueType);
                    }
                } else if (klass == K.KDatetimeVector.class) {
                    series = new TimeSeries(table.getColumnName(col));
                    K.KDatetimeVector times = (K.KDatetimeVector) table.getColumn(0);

                    for (int row = 0; row < table.getRowCount(); row++) {
                        K.KDatetime time = (K.KDatetime) times.at(row);
                        Millisecond ms = new Millisecond(time.toTimestamp(), tz, locale);
                        addData(series, ms, table, row, col, invalidValueType);
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
                        addData(series, month, table, row, col, invalidValueType);
                    }
                } else if (klass == K.KSecondVector.class) {
                    series = new TimeSeries(table.getColumnName(col));
                    K.KSecondVector times = (K.KSecondVector) table.getColumn(0);
                    for (int row = 0; row < table.getRowCount(); row++) {
                        K.Second time = (K.Second) times.at(row);
                        Second second = new Second(time.i % 60, time.i / 60, 0, 1, 1, 2001);
                        addData(series, second, table, row, col, invalidValueType);
                    }
                } else if (klass == K.KMinuteVector.class) {
                    series = new TimeSeries(table.getColumnName(col));
                    K.KMinuteVector times = (K.KMinuteVector) table.getColumn(0);
                    for (int row = 0; row < table.getRowCount(); row++) {
                        K.Minute time = (K.Minute) times.at(row);
                        Minute minute = new Minute(time.i % 60, time.i / 60, 1, 1, 2001);
                        addData(series, minute, table, row, col, invalidValueType);
                    }
                }
            } catch (SeriesException e) {
                System.err.println("Error adding to series");
            }

            if (series.getItemCount() > 0) {
                tsc.addSeries(series);
            }
        }

        return tsc;
    }

    private static void addData(TimeSeries series, RegularTimePeriod period, KTableModel table, int row, int col, InvalidValueType invalidValueType ) {
        Object o = table.getValueAt(row, col);
        if (o instanceof K.KBase
                && !((K.KBase) o).isNull()
                && o instanceof ToDouble) {
            double value = ((ToDouble) o).toDouble();
            if (Double.isFinite(value)) {
                series.addOrUpdate(period, ((ToDouble) o).toDouble());
            } else {
                if (invalidValueType.isUseInf()) {
                    series.addOrUpdate(period, Double.NaN);
                }
            }
        } else {
            if (invalidValueType.isUseNan()) {
                series.addOrUpdate(period, Double.NaN);
            }
        }
    }
    
    private static XYSeriesCollection createXYSeriesCollection(KTableModel table, int fromCol, int toCol, InvalidValueType invalidValueType) {
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
                        } else {
                            if (invalidValueType.isUseInf()) {
                                series.addOrUpdate(x, Double.NaN);
                            }
                        }
                    } else {
                        if (invalidValueType.isUseNan()) {
                            series.addOrUpdate(x, Double.NaN);
                        }
                    }
                }
            } catch (SeriesException e) {
                e.printStackTrace();
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
        
        // search ohlc columns
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
