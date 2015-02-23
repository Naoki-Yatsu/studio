package studio.chart;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;

/**
 * JFreechart theme
 * @see StandardChartTheme
 */
public enum ChartTheme {
    JFREE,
    JFREE_SHADOW,
    DARKNESS,
    LEGACY;
    
    public void setTheme(JFreeChart chart) {
        switch (this) {
            case JFREE:
                ChartFactory.setChartTheme(StandardChartTheme.createJFreeTheme());
                break;
            case JFREE_SHADOW:
                ChartFactory.setChartTheme(new StandardChartTheme("JFreeChart/Shadow", true));
                break;
            case DARKNESS:
                ChartFactory.setChartTheme(StandardChartTheme.createDarknessTheme());
                break;
            case LEGACY:
                ChartFactory.setChartTheme(StandardChartTheme.createLegacyTheme());
                break;
            default:
                // set jfree
                ChartFactory.setChartTheme(StandardChartTheme.createJFreeTheme());
                break;
        }
        ChartUtilities.applyCurrentTheme(chart);
    }
}
