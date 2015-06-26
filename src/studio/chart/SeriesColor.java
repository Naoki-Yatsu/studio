package studio.chart;

import java.awt.Color;

/**
 * Series Color,
 * Used to paint series
 */
public class SeriesColor extends Color {
    private static final long serialVersionUID = 1L;
    
    /** Represents auto paint */
    public static final SeriesColor AUTO = new SeriesColor(new Color(255, 255, 255), "AUTO");
    
    // Basic colors
    public static final SeriesColor RED = new SeriesColor(Color.RED, "RED");
    public static final SeriesColor PINK = new SeriesColor(Color.PINK, "PINK");
    public static final SeriesColor ORANGE = new SeriesColor(Color.ORANGE, "ORANGE");
    public static final SeriesColor YELLOW = new SeriesColor(Color.YELLOW, "YELLOW");
    public static final SeriesColor GREEN = new SeriesColor(Color.GREEN, "GREEN");
    public static final SeriesColor MAGENTA = new SeriesColor(Color.MAGENTA, "MAGENTA");
    public static final SeriesColor CYAN = new SeriesColor(Color.CYAN, "CYAN");
    public static final SeriesColor BLUE = new SeriesColor(Color.BLUE, "BLUE");
    
    public static final SeriesColor BLACK = new SeriesColor(Color.BLACK, "BLACK");
    public static final SeriesColor DARK_GRAY = new SeriesColor(Color.DARK_GRAY, "D_GRAY");
    public static final SeriesColor LIGHT_GRAY = new SeriesColor(Color.LIGHT_GRAY, "L_GRAY");
    public static final SeriesColor WHITE = new SeriesColor(Color.WHITE, "WHITE");
    
    // alpha 1/2
    public static final SeriesColor RED2 = new SeriesColor(Color.RED, "RED2", 127);
    public static final SeriesColor MAGENTA2 = new SeriesColor(Color.MAGENTA, "MAGEN2", 127);
    public static final SeriesColor GREEN2 = new SeriesColor(Color.GREEN, "GREEN2", 127);
    public static final SeriesColor CYAN2 = new SeriesColor(Color.CYAN, "CYAN2", 127);
    public static final SeriesColor BLUE2 = new SeriesColor(Color.BLUE, "BLUE2", 127);
    
    /** Color set */
    public static final Color[] COLORS = {AUTO, 
            RED, PINK, ORANGE, YELLOW, GREEN, MAGENTA, CYAN, BLUE, 
            BLACK, DARK_GRAY, LIGHT_GRAY, WHITE, 
            RED2, GREEN2, MAGENTA2, CYAN2, BLUE2};
    
    private final String name;
    
    private SeriesColor(Color color, String name) {
        super(color.getRed(), color.getGreen(), color.getBlue());
        this.name = name;
    }
    
    private SeriesColor(Color color, String name, int alpha) {
        super(color.getRed(), color.getGreen(), color.getBlue(), alpha);
        this.name = name;
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////
    
    public String getName() {
        return name;
    }
    
    public boolean isAuto() {
        if (this.equals(AUTO)) {
            return true;
        } else {
            return false;
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) && (obj instanceof SeriesColor) && ((SeriesColor)obj).getName().equals(this.getName());
    }
    
    @Override
    public String toString() {
        return name;
    }
    
    
    /**
     * Rotate Color R -> G -> B -> R
     * @return
     */
    public static Color rotateColor(Color color) {
        int change = 92;
        
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        int alpha = color.getAlpha();
        
        if (r == 255) {
            if (g == 255) {
                // minus red
                r = 255 - change;
            } else {
                // plus green
                g = (int) Math.min(g + change, 255);
            }
            
        } else if (g == 255) {
            if (b == 255) {
                // minus green
                g = 255 - change;
            } else {
                // plus blue
                b = (int) Math.min(b + change, 255);
            }
            
        } else if (g == 255) {
            if (r == 255) {
                // minus blue
                b = 255 - change;
            } else {
                // plus red
                r = (int) Math.min(r + change, 255);
            }
            
        } else {
            // plus max color
            if (r >= g && r >= b) {
                r = (int) Math.min(r + change, 255);
            } else if (g >= r && g >= b) {
                g = (int) Math.min(g + change, 255);
            } else {
                b = (int) Math.min(b + change, 255);
            }
        }
        
        return new Color(r, g, b, alpha);
    }
    
}
