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
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

import studio.kdb.K;
import studio.kdb.K.KBase;
import studio.kdb.KTableModel;
import studio.kdb.ToDouble;

public class ChartDataCreator {

    private static final TimeZone tz = TimeZone.getTimeZone("GMT");
    
    private static final Locale locale = Locale.getDefault();

    public static JFreeChart createChart(KTableModel tableModel, SmartChartSetting setting) {
        if (tableModel.getColumnCount() == 0) {
            return null;
        }

        //
        // create separate dataset for each axis
        //
        NavigableMap<Integer, Integer> fromToMap = new TreeMap<>();
        int endIndex = tableModel.getColumnCount();

        // Y4 Left
        if (setting.isY4LeftEnable()) {
            int index = getColumnIndexFromName(tableModel, setting.getY4LeftColumnName());
            fromToMap.put(index, endIndex);
            endIndex = index;
        }
        // Y3 Left
        if (setting.isY3LeftEnable()) {
            int index = getColumnIndexFromName(tableModel, setting.getY3LeftColumnName());
            fromToMap.put(index, endIndex);
            endIndex = index;
        }
        // Y2 Right
        if (setting.isY2RightEnable()) {
            int index = getColumnIndexFromName(tableModel, setting.getY2RightColumnName());
            fromToMap.put(index, endIndex);
            endIndex = index;
        }
        // Y2 Left
        if (setting.isY2LeftEnable()) {
            int index = getColumnIndexFromName(tableModel, setting.getY2LeftColumnName());
            fromToMap.put(index, endIndex);
            endIndex = index;
        }
        // Y1 Right
        if (setting.isY1RightEnable()) {
            int index = getColumnIndexFromName(tableModel, setting.getY1RightColumnName());
            fromToMap.put(index, endIndex);
            endIndex = index;
        }
        // Y1 Left 2
        if (setting.isY1LeftEnable()) {
            int index = getColumnIndexFromName(tableModel, setting.getY1LeftColumnName());
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

        // if no dataset,  or datataset is less than expected throw exception
        if (xyDatasetList.size() == 0 || xyDatasetList.size() < setting.getDatasetCount()) {
            throw new RuntimeException("No Dataset created or Dataset is less than expected. Created dataset is "
                        + xyDatasetList.size() + ", but exetected is " +setting.getDatasetCount());
        }

        //
        // create chart
        //

        // single axis
        if (xyDatasetList.size() == 1) {
            return createSingleLineChart(xyDatasetList.get(0), setting);
        }

        // single plot, multiple range axis
        if (setting.getPlotCount() == 1) {
            return createSinglePlotLineChart(xyDatasetList, setting);
        }

        // multiple plot
        return createMultipleDomainChart(xyDatasetList, setting);

    }

    /**
     * Create single axis simple chart
     * 
     * @param xyDataset
     * @param setting
     * @return
     */
    private static JFreeChart createSingleLineChart(XYDataset xyDataset, SmartChartSetting setting) {
        JFreeChart chart = null;
        if (xyDataset instanceof TimeSeriesCollection) {
            TimeSeriesCollection dataset = (TimeSeriesCollection) xyDataset;
            switch (setting.getY1Chart()) {
                case LINE:
                case LINE_MARK:
                    chart = ChartFactory.createTimeSeriesChart(setting.getTitle(), setting.getxLabel(), setting.getY1Label(), dataset);
                    break;
                case BAR:
                    chart = ChartFactory.createXYBarChart(setting.getTitle(), setting.getxLabel(), true, setting.getY1Label(), dataset);
                    break;
                case HISTGRAM:
                    chart = ChartFactory.createXYBarChart(setting.getTitle(), setting.getxLabel(), true, setting.getY1Label(), dataset);
                    break;
                case SCATTER:
                    chart = ChartFactory.createScatterPlot(setting.getTitle(), setting.getxLabel(), setting.getY1Label(), dataset);
                    break;
                default:
            }

        } else if (xyDataset instanceof XYSeriesCollection) {
            XYSeriesCollection dataset = (XYSeriesCollection) xyDataset;
            switch (setting.getY1Chart()) {
                case LINE:
                case LINE_MARK:
                    chart = ChartFactory.createXYLineChart(setting.getTitle(), setting.getxLabel(), setting.getY1Label(), dataset);
                    break;
                case BAR:
                    chart = ChartFactory.createXYBarChart(setting.getTitle(), setting.getxLabel(), false, setting.getY1Label(), dataset);
                    break;
                case HISTGRAM:
                    chart = ChartFactory.createHistogram(setting.getTitle(), setting.getxLabel(), setting.getY1Label(), dataset, PlotOrientation.VERTICAL, true, true, false);
                    break;
                case SCATTER:
                    chart = ChartFactory.createScatterPlot(setting.getTitle(), setting.getxLabel(), setting.getY1Label(), dataset);
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
            ((NumberAxis) domainAxis).setAutoRangeIncludesZero(setting.isxIncludeZero());
        }
        if (rangeAxis instanceof NumberAxis) {
            ((NumberAxis) rangeAxis).setAutoRangeIncludesZero(setting.isY1IncludeZero());
        }
        return chart;
    }

    /**
     * Create one plot multiple axis chart.
     * 
     * @param xyDatasetList
     * @param setting
     * @return
     */
    private static JFreeChart createSinglePlotLineChart(List<XYDataset> xyDatasetList, SmartChartSetting setting) {
        // create main chart
        XYDataset xyDataset0 = xyDatasetList.get(0);
        JFreeChart chart = null;
        // boolean createNormalLegend = (setting.getY1RightAxis() != AxisPosition.RIGHT_1S);
        boolean createNormalLegend = true;
        
        boolean isDateDomainAxis = false;
        if (xyDataset0 instanceof TimeSeriesCollection) {
            chart = ChartFactory.createTimeSeriesChart(setting.getTitle(), setting.getxLabel(), setting.getY1Label(), xyDataset0, 
                    createNormalLegend, true, false);
            isDateDomainAxis = true;
        } else if (xyDataset0 instanceof XYSeriesCollection) {
            chart = ChartFactory.createXYLineChart(setting.getTitle(), setting.getxLabel(), setting.getY1Label(), xyDataset0, PlotOrientation.VERTICAL,
                    createNormalLegend, true, false);
        } else {
            return null;
        }
        // Set auto-range IncludeZero
        ValueAxis domainAxis = chart.getXYPlot().getDomainAxis();
        ValueAxis rangeAxis = chart.getXYPlot().getRangeAxis();
        if (domainAxis instanceof NumberAxis) {
            ((NumberAxis) domainAxis).setAutoRangeIncludesZero(setting.isxIncludeZero());
        }
        if (rangeAxis instanceof NumberAxis) {
            ((NumberAxis) rangeAxis).setAutoRangeIncludesZero(setting.isY1IncludeZero());
        }
        
        
        // plot 1
        XYPlot plot = chart.getXYPlot();
        plot.setDomainPannable(true);
        plot.setRangePannable(true);

        // add right axis
        addDatasetToPlot(plot, 1, xyDatasetList.get(1), setting.getY1RightLabel(), AxisLocation.BOTTOM_OR_RIGHT, 
                RendererFactory.createXYItemRenderer(setting.getY1Chart(), isDateDomainAxis), setting.isY1RightIncludeZero());
        
        // separate legend
        if (!createNormalLegend) {
            chart.addSubtitle(getSepareteLegendTitle(plot.getRenderer(0), plot.getRenderer(1)));
        }
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
    private static JFreeChart createMultipleDomainChart(List<XYDataset> xyDatasetList, SmartChartSetting setting) {
        int datasetNo = 0;
        
        // Domain Axis
        ValueAxis domainAxis = null;
        boolean isDateDomainAxis = false;
        if (xyDatasetList.get(0) instanceof TimeSeriesCollection) {
            domainAxis = new DateAxis(setting.getxLabel());
            domainAxis.setLowerMargin(0.02);
            domainAxis.setUpperMargin(0.02);
            isDateDomainAxis = true;
        } else if (xyDatasetList.get(0) instanceof XYSeriesCollection) {
            domainAxis = new NumberAxis(setting.getxLabel());
            ((NumberAxis) domainAxis).setAutoRangeIncludesZero(setting.isxIncludeZero());
        } else {
            domainAxis = new NumberAxis(setting.getxLabel());
            ((NumberAxis) domainAxis).setAutoRangeIncludesZero(setting.isxIncludeZero());
        }

        // CombinedDomainXYPlot
        CombinedDomainXYPlot combinedDomainXYPlot = new CombinedDomainXYPlot(domainAxis);
        combinedDomainXYPlot.setGap(-5.0D);
        combinedDomainXYPlot.setOrientation(PlotOrientation.VERTICAL);

        // plot 1 - Y1
        XYPlot xyPlot1 = createXYPlot(xyDatasetList.get(datasetNo), setting.getY1Label(), AxisLocation.BOTTOM_OR_LEFT, 
                    RendererFactory.createXYItemRenderer(setting.getY1Chart(), isDateDomainAxis), setting.isY1IncludeZero());
        datasetNo++;
        // Y1 left, right
        if (setting.isY1LeftEnable()) {
            addDatasetToPlotMapAxis(xyPlot1, 2, 0, xyDatasetList.get(datasetNo), AxisLocation.BOTTOM_OR_LEFT, RendererFactory.
                    createXYItemRenderer(setting.getY1LeftChart(), isDateDomainAxis));
            datasetNo++;
        }
        if (setting.isY1RightEnable()) {
            addDatasetToPlot(xyPlot1, 1, xyDatasetList.get(datasetNo), setting.getY1RightLabel(), AxisLocation.BOTTOM_OR_RIGHT, 
                    RendererFactory.createXYItemRenderer(setting.getY1RightChart(), isDateDomainAxis), setting.isY1RightIncludeZero());
            datasetNo++;
        }
        combinedDomainXYPlot.add(xyPlot1, (int) Math.round(100 * setting.getY1Weight()));

        
        // plot 2
        if (setting.getPlotCount() >= 2) {
            XYPlot xyPlot2 = createXYPlot(xyDatasetList.get(datasetNo), setting.getY2LeftLabel(), AxisLocation.TOP_OR_LEFT,
                    RendererFactory.createXYItemRenderer(setting.getY2LeftChart(), isDateDomainAxis), setting.isY2LeftIncludeZero());
            datasetNo++;
            if (setting.isY2RightEnable()) {
                addDatasetToPlot(xyPlot2, 1, xyDatasetList.get(datasetNo), setting.getY2RightLabel(), AxisLocation.TOP_OR_RIGHT, 
                        RendererFactory.createXYItemRenderer(setting.getY2RightChart(), isDateDomainAxis), setting.isY2RightIncludeZero());
                datasetNo++;
            }
            combinedDomainXYPlot.add(xyPlot2, (int) Math.round(100 * setting.getY2LeftWeight()));
        }
        
        // plot 3
        if (setting.getPlotCount() >= 3) {
            XYPlot xyPlot3 = createXYPlot(xyDatasetList.get(datasetNo), setting.getY3LeftLabel(), AxisLocation.TOP_OR_LEFT, 
                    RendererFactory.createXYItemRenderer(setting.getY3LeftChart(), isDateDomainAxis), setting.isY3LeftIncludeZero());
            datasetNo++;
            combinedDomainXYPlot.add(xyPlot3, (int) Math.round(100 * setting.getY3LeftWeight()));
        }

        // plot 4
        if (setting.getPlotCount() >= 4) {
            XYPlot xyPlot4 = createXYPlot(xyDatasetList.get(datasetNo), setting.getY4LeftLabel(), AxisLocation.TOP_OR_LEFT,
                    RendererFactory.createXYItemRenderer(setting.getY4LeftChart(), isDateDomainAxis), setting.isY4LeftIncludeZero());
            datasetNo++;
            combinedDomainXYPlot.add(xyPlot4, (int) Math.round(100 * setting.getY4LeftWeight()));
        }

        // create chart
        JFreeChart chart = new JFreeChart(setting.getTitle(), JFreeChart.DEFAULT_TITLE_FONT, combinedDomainXYPlot, true);
        ChartUtilities.applyCurrentTheme(chart);

        return chart;
    }

    private static XYPlot createXYPlot(XYDataset dataset, String label, AxisLocation location, XYItemRenderer renderer, boolean includeZero) {
        NumberAxis numberAxis = new NumberAxis(label);
        numberAxis.setAutoRangeIncludesZero(includeZero);
        XYPlot plot = new XYPlot(dataset, null, numberAxis, renderer);
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
    private static CompositeTitle getSepareteLegendTitle(LegendItemSource source1, LegendItemSource source2) {
        LegendTitle legendTitle1 = new LegendTitle(source1);
        legendTitle1.setMargin(new RectangleInsets(2.0D, 2.0D, 2.0D, 2.0D));
        legendTitle1.setFrame(new BlockBorder());

        LegendTitle legendTitle2 = new LegendTitle(source2);
        legendTitle2.setMargin(new RectangleInsets(2.0D, 2.0D, 2.0D, 2.0D));
        legendTitle2.setFrame(new BlockBorder());

        BlockContainer blockContainer = new BlockContainer(new BorderArrangement());
        blockContainer.add(legendTitle1, RectangleEdge.LEFT);
        blockContainer.add(legendTitle2, RectangleEdge.RIGHT);
        blockContainer.add(new EmptyBlock(2000.0D, 0.0D));

        CompositeTitle compositeTitle = new CompositeTitle(blockContainer);
        compositeTitle.setPosition(RectangleEdge.BOTTOM);
        return compositeTitle;
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
     * Create multiple plot chart
     * 
     * @param xyDatasetList
     * @param setting
     * @return
     */
    private static JFreeChart createMultipleDomainCategoryChart(List<CategoryDataset> categoryDatasetList, SmartChartSetting setting) {
        int datasetNo = 0;
        
        // Domain Axis
        CategoryAxis categoryAxis = new CategoryAxis(setting.getxLabel());

        // CombinedDomainXYPlot
        CombinedDomainCategoryPlot combinedDomainCategoryPlot = new CombinedDomainCategoryPlot(categoryAxis);
        combinedDomainCategoryPlot.setGap(-5.0D);
        combinedDomainCategoryPlot.setOrientation(PlotOrientation.VERTICAL);

        // plot 1 - Y1
        CategoryPlot plot1 = createCategoryPlot(categoryDatasetList.get(datasetNo), setting.getY1Label(), AxisLocation.BOTTOM_OR_LEFT, 
                    RendererFactory.createCategoryItemRenderer(setting.getY1Chart()));
        datasetNo++;
        combinedDomainCategoryPlot.add(plot1, (int) Math.round(100 * setting.getY1Weight()));
        
        // plot 2
        if (setting.getPlotCount() >= 2) {
            CategoryPlot plot2 = createCategoryPlot(categoryDatasetList.get(datasetNo), setting.getY2LeftLabel(), AxisLocation.TOP_OR_LEFT,
                    RendererFactory.createCategoryItemRenderer(setting.getY2LeftChart()));
            datasetNo++;
            combinedDomainCategoryPlot.add(plot2, (int) Math.round(100 * setting.getY2LeftWeight()));
        }
        
        // plot 3
        if (setting.getPlotCount() >= 3) {
            CategoryPlot plot3 = createCategoryPlot(categoryDatasetList.get(datasetNo), setting.getY3LeftLabel(), AxisLocation.TOP_OR_LEFT, 
                    RendererFactory.createCategoryItemRenderer(setting.getY3LeftChart()));
            datasetNo++;
            combinedDomainCategoryPlot.add(plot3, (int) Math.round(100 * setting.getY3LeftWeight()));
        }

        // plot 4
        if (setting.getPlotCount() >= 4) {
            CategoryPlot plot4 = createCategoryPlot(categoryDatasetList.get(datasetNo), setting.getY4LeftLabel(), AxisLocation.TOP_OR_LEFT,
                    RendererFactory.createCategoryItemRenderer(setting.getY4LeftChart()));
            datasetNo++;
            combinedDomainCategoryPlot.add(plot4, (int) Math.round(100 * setting.getY4LeftWeight()));
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

                        Object o = table.getValueAt(row, col);
                        if (o instanceof K.KBase)
                            if (!((K.KBase) o).isNull())
                                if (o instanceof ToDouble)
                                    series.addOrUpdate(day, ((ToDouble) o).toDouble());
                    }
                } else if (klass == K.KTimeVector.class) {
                    series = new TimeSeries(table.getColumnName(col));

                    K.KTimeVector times = (K.KTimeVector) table.getColumn(0);
                    for (int row = 0; row < table.getRowCount(); row++) {
                        K.KTime time = (K.KTime) times.at(row);
                        Millisecond ms = new Millisecond(time.toTime(), tz, locale);

                        Object o = table.getValueAt(row, col);
                        if (o instanceof K.KBase)
                            if (!((K.KBase) o).isNull())
                                if (o instanceof ToDouble)
                                    series.addOrUpdate(ms, ((ToDouble) o).toDouble());
                    }
                } else if (klass == K.KTimestampVector.class) {
                    series = new TimeSeries(table.getColumnName(col));
                    K.KTimestampVector dates = (K.KTimestampVector) table.getColumn(0);

                    for (int row = 0; row < dates.getLength(); row++) {
                        K.KTimestamp date = (K.KTimestamp) dates.at(row);
                        Millisecond ms = new Millisecond(date.toTimestamp(), tz, locale);

                        Object o = table.getValueAt(row, col);
                        if (o instanceof K.KBase)
                            if (!((K.KBase) o).isNull())
                                if (o instanceof ToDouble)
                                    series.addOrUpdate(ms, ((ToDouble) o).toDouble());
                    }
                } else if (klass == K.KTimespanVector.class) {
                    series = new TimeSeries(table.getColumnName(col));

                    K.KTimespanVector times = (K.KTimespanVector) table.getColumn(0);
                    for (int row = 0; row < table.getRowCount(); row++) {
                        K.KTimespan time = (K.KTimespan) times.at(row);
                        Millisecond ms = new Millisecond(time.toTime(), tz, locale);

                        Object o = table.getValueAt(row, col);
                        if (o instanceof K.KBase)
                            if (!((K.KBase) o).isNull())
                                if (o instanceof ToDouble)
                                    series.addOrUpdate(ms, ((ToDouble) o).toDouble());
                    }
                } else if (klass == K.KDatetimeVector.class) {
                    series = new TimeSeries(table.getColumnName(col));
                    K.KDatetimeVector times = (K.KDatetimeVector) table.getColumn(0);

                    for (int row = 0; row < table.getRowCount(); row++) {
                        K.KDatetime time = (K.KDatetime) times.at(row);
                        Millisecond ms = new Millisecond(time.toTimestamp(), tz, locale);

                        Object o = table.getValueAt(row, col);
                        if (o instanceof K.KBase)
                            if (!((K.KBase) o).isNull())
                                if (o instanceof ToDouble)
                                    series.addOrUpdate(ms, ((ToDouble) o).toDouble());
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

                        Object o = table.getValueAt(row, col);
                        if (o instanceof K.KBase)
                            if (!((K.KBase) o).isNull())
                                if (o instanceof ToDouble)
                                    series.addOrUpdate(month, ((ToDouble) o).toDouble());
                    }
                } else if (klass == K.KSecondVector.class) {
                    series = new TimeSeries(table.getColumnName(col));
                    K.KSecondVector times = (K.KSecondVector) table.getColumn(0);
                    for (int row = 0; row < table.getRowCount(); row++) {
                        K.Second time = (K.Second) times.at(row);
                        Second second = new Second(time.i % 60, time.i / 60, 0, 1, 1, 2001);

                        Object o = table.getValueAt(row, col);
                        if (o instanceof K.KBase)
                            if (!((K.KBase) o).isNull())
                                if (o instanceof ToDouble)
                                    series.addOrUpdate(second, ((ToDouble) o).toDouble());

                    }
                } else if (klass == K.KMinuteVector.class) {
                    series = new TimeSeries(table.getColumnName(col));
                    K.KMinuteVector times = (K.KMinuteVector) table.getColumn(0);
                    for (int row = 0; row < table.getRowCount(); row++) {
                        K.Minute time = (K.Minute) times.at(row);
                        Minute minute = new Minute(time.i % 60, time.i / 60, 1, 1, 2001);
                        Object o = table.getValueAt(row, col);
                        if (o instanceof K.KBase)
                            if (!((K.KBase) o).isNull())
                                if (o instanceof ToDouble)
                                    series.addOrUpdate(minute, ((ToDouble) o).toDouble());
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

    private static XYSeriesCollection createXYSeriesCollection(KTableModel table, int fromCol, int toCol) {
        XYSeriesCollection xysc = new XYSeriesCollection();

        for (int col = fromCol; col < toCol; col++) {
            XYSeries series = null;

            try {
                series = new XYSeries(table.getColumnName(col));

                for (int row = 0; row < table.getRowCount(); row++) {
                    double x = ((ToDouble) table.getValueAt(row, 0)).toDouble();
                    double y = ((ToDouble) table.getValueAt(row, col)).toDouble();
                    series.add(x, y);
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

}
