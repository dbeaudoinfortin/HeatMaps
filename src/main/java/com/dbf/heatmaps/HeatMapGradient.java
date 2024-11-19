package com.dbf.heatmaps;

import java.awt.Color;
import java.util.ArrayList;

public class HeatMapGradient {
	
	/**
	 * A smooth (no steps) gradient based on the colour wheel (HSB model) from blue to red, passing through green.
	 * All the of the colours are fully saturated and full brightness
	 */
	public static final HeatMapGradient SMOOTH_GRADIENT = new HeatMapGradient(240, 360, 1.0f, 1.0f, false);
	
	/**
	 * A 12 step gradient from blue to green to red, with medium saturation.
	 * The yellow to red range has been extended, while the blue to green range has been compressed.
	 */
	public static final HeatMapGradient EXTENDED_GRADIENT = new HeatMapGradient(new Color[] {
		Color.decode("#3288bd"), // Blue
		Color.decode("#65c1a5"),
		Color.decode("#91d5a6"),
		Color.decode("#c9e89a"), 
		Color.decode("#eaf79b"),
		Color.decode("#fcffba"),
		Color.decode("#fffbba"), 
		Color.decode("#fee492"),
		Color.decode("#fdc771"),
		Color.decode("#fda159"), 
		Color.decode("#f46c43"), 
		Color.decode("#d53e4f")  //Red
	});
	
	/**
	 * A simplified 5 step gradient from blue to red. Saturated and a bit darker.
	 */
	public static final HeatMapGradient BASIC_GRADIENT = new HeatMapGradient(new Color[] {
		Color.decode("#1d4877"), //Dark Blue
		Color.decode("#1b8a5a"), //Green
		Color.decode("#fbb021"), //Golden Yellow
		Color.decode("#f68838"), //Orange
		Color.decode("#ee3e32")  //Red
	});
	
	/**
	 * A simple two colour gradient from blue to red, with purple mixed in-between.
	 */
	public static final HeatMapGradient TWO_COLOUR_GRADIENT = new HeatMapGradient(new Color[] {
		Color.decode("#0000FF"), //Blue
		Color.decode("#FF0000"), //Red
	});
	
	/**
	 * A 5 step colour blind friendly gradient of greenish-yellow to dark orange.
	 */
	public static final HeatMapGradient COLOUR_BLIND_GRADIENT = new HeatMapGradient(new Color[] {
		Color.decode("#e4ff7a"), //Light Greenish Yellow
		Color.decode("#ffe81a"), //Bright Yellow
		Color.decode("#ffbd00"), //Deep Yellow
		Color.decode("#ffa000"), //Orange
		Color.decode("#fc7f00")  //Darker Orange
	});
	
	/**
	 * A 3 step black-red-orange gradient, similar to black-body radiation, up to approximately 1300 degrees kelvin.
	 */
	public static final HeatMapGradient BLACK_RED_ORANGE_GRADIENT = new HeatMapGradient(new Color[] {
		Color.decode("#000000"), //Black
		Color.decode("#8e060a"), //Red
		Color.decode("#fda32b")  //Orange
	});
	
	/**
	 * A 5 step black-red-orange-white gradient, similar to black-body radiation, extended  up to approximately 6500k degrees kelvin.
	 */
	public static final HeatMapGradient WHITE_HOT_GRADIENT = new HeatMapGradient(new Color[] {
		Color.decode("#000000"), //Black
		Color.decode("#8e060a"), //Red
		Color.decode("#fda32b"), //Orange
		Color.decode("#FFCF9F"), //Light Orange
		Color.decode("#FEF9FF")  //Whitish-blue
	});

	/**
	 * A 50 step colour gradient based on Dave Green's ‘cubehelix’ colour scheme.
	 * https://people.phy.cam.ac.uk/dag9/CUBEHELIX/
	 */
	public static final HeatMapGradient CUBEHELIX_GRADIENT = new HeatMapGradient(new Color[] {
		Color.decode("#000000"),
		Color.decode("#090309"),
		Color.decode("#100614"),
		Color.decode("#160a1f"),
		Color.decode("#190f2b"),
		Color.decode("#1a1536"),
		Color.decode("#1a1c3f"),
		Color.decode("#182448"),
		Color.decode("#152d4e"),
		Color.decode("#123752"),
		Color.decode("#104153"),
		Color.decode("#0e4b53"),
		Color.decode("#0d544f"),
		Color.decode("#0e5d4b"),
		Color.decode("#126644"),
		Color.decode("#176d3d"),
		Color.decode("#207336"),
		Color.decode("#2a782f"),
		Color.decode("#387b29"),
		Color.decode("#477d25"),
		Color.decode("#577d23"),
		Color.decode("#697d24"),
		Color.decode("#7b7c28"),
		Color.decode("#8d7a2f"),
		Color.decode("#9e7938"),
		Color.decode("#ae7745"),
		Color.decode("#bd7654"),
		Color.decode("#c87564"),
		Color.decode("#d27677"),
		Color.decode("#d9788a"),
		Color.decode("#dd7b9d"),
		Color.decode("#de80af"),
		Color.decode("#de86c1"),
		Color.decode("#db8dd1"),
		Color.decode("#d795de"),
		Color.decode("#d29fe9"),
		Color.decode("#cca9f1"),
		Color.decode("#c7b3f7"),
		Color.decode("#c3bdfa"),
		Color.decode("#c0c8fb"),
		Color.decode("#bed1fa"),
		Color.decode("#bfdaf8"),
		Color.decode("#c2e2f6"),
		Color.decode("#c7e9f3"),
		Color.decode("#cdeff1"),
		Color.decode("#d6f3f0"),
		Color.decode("#e0f7f0"),
		Color.decode("#eafaf3"),
		Color.decode("#f5fdf8"),
		Color.decode("#ffffff")
	});
	
	/**
	 * A 2 step grey-scale gradient that should be used for non-colour screens/print-outs.
	 */
	public static final HeatMapGradient GREY_GRADIENT = new HeatMapGradient(new Color[] {
			Color.decode("#E3E3E3"), //Grey
			Color.decode("#000000"), //Black
		});
	
	/**
	 * List of all canned (pre-defined) gradients that can be used as-is.
	 */
	public static final ArrayList<HeatMapGradient> CANNED_GRADIENTS = new ArrayList<HeatMapGradient>();
	
	static {
		CANNED_GRADIENTS.add(SMOOTH_GRADIENT);
		CANNED_GRADIENTS.add(EXTENDED_GRADIENT);
		CANNED_GRADIENTS.add(BASIC_GRADIENT);
		CANNED_GRADIENTS.add(TWO_COLOUR_GRADIENT);
		CANNED_GRADIENTS.add(COLOUR_BLIND_GRADIENT);
		CANNED_GRADIENTS.add(BLACK_RED_ORANGE_GRADIENT);
		CANNED_GRADIENTS.add(WHITE_HOT_GRADIENT);
		CANNED_GRADIENTS.add(CUBEHELIX_GRADIENT);
		CANNED_GRADIENTS.add(GREY_GRADIENT);
	}
	
	private final Color[] steps;
	
	private final float   hueStart;
	private final float   hueRange;
	private final float   saturation;
	private final float   brightness;
	private final boolean clockwise;
	
	/**
	 * Creates a custom HeatMapGradient based on an array of discrete predefined gradient steps.
	 * Values that fall between the discrete steps will be assigned a linearly interpolated colour.
	 * 
	 * @param steps Ordered array of java.awt.Color gradient steps
	 * 
	 * @throws IllegalArgumentException if less than 2 steps are specified
	 */
	public HeatMapGradient(Color[] steps) {
		if(steps.length < 2) throw new IllegalArgumentException("A minimum of 2 colour steps is required.");
		this.steps = steps;
		this.hueStart = 0;
		this.hueRange = 0;
		this.saturation = 0;
		this.brightness = 0;
		this.clockwise = false;
	}
	
	/**
	 * Creates a custom HeatMapGradient using a smooth gradient based on the colour wheel. HSB colour model is used.
	 * The hue is applied either clockwise from hueStart to hueEnd, or counter-clockwise from hueStart to hueEnd.
	 * 
	 * @param hueStart The starting hue for the colour gradient, in degrees, between zero and 360 (inclusive).
	 * @param hueEnd The ending hue for the colour gradient, in degrees, between zero and 360 (inclusive).
	 * @param saturation The saturation of the colours, as a fractional value between zero and one.
	 * @param brightness The saturation of the colours, as a fractional value between zero and one.
	 * @param clockwise Whether the hue should be applied clockwise (true) or counter-clockwise (false).
	 * 
	 * @throws IllegalArgumentException if any parameters are outside their allowed range.
	 */
	public HeatMapGradient(int hueStart, int hueEnd, float saturation, float brightness, boolean clockwise) {
		if(hueStart < 0 || hueStart >= 360) throw new IllegalArgumentException("Hue Start must be at least zero and less than or equal to 360.");
		if(hueEnd < 0 || hueEnd >= 360) throw new IllegalArgumentException("Hue End must be at least zero and less than or equal to 360.");
		if(hueEnd == hueStart) throw new IllegalArgumentException("Hue Start and Hue End cannot be the same.");
		if(saturation < 0f || saturation > 1.0f) throw new IllegalArgumentException("Saturation must be at least zero and less than or equal to 1.");
		if(brightness < 0f || brightness > 1.0f) throw new IllegalArgumentException("Brightness must be at least zero and less than or equal to 1.");
		
		//Adjust the start and end values when crossing the 360/0 degree point
		if(clockwise && (hueStart > hueEnd)) {
			hueEnd +=360;
		} else if (!clockwise && (hueEnd > hueStart)){
			hueStart +=360;
		}
		
		this.steps = null;
		this.saturation = saturation;
		this.brightness = brightness;
		this.clockwise = clockwise;
		this.hueStart = hueStart/360;
		this.hueRange = Math.abs(this.hueStart - (hueEnd/360));
	}
	
	/**
	 * Returns a HeatMapGradient using a canned (pre-defined) gradient.
	 * 
	 * @param gradientIndex The gradient index between zero and getCannedGradientCount() - 1, inclusive.
	 * 
	 * @return a HeatMapGradient
	 * 
	 * @throws IllegalArgumentException if gradientIndex is out of bounds
	 * 
	 * @see com.dbf.heatmaps.HeatMapGradient#getCannedGradientCount()
	 */
	public static HeatMapGradient getCannedGradient(int gradientIndex) {
		if(gradientIndex < 0 || gradientIndex >= getCannedGradientCount())
			throw new IllegalArgumentException("Invalid gradient index. Must be between zero and " + (getCannedGradientCount()-1) + ", inclusive.");
		
		return CANNED_GRADIENTS.get(gradientIndex);
	}

	/**
	 * @return total number or canned gradients that are defined
	 * @see com.dbf.heatmaps.HeatMapGradient#getCannedGradient(int)
	 */
	public static int getCannedGradientCount() {
		return CANNED_GRADIENTS.size();
	}
	
	/**
	 * Calculates a colour based on the gradient defined.
	 * 
	 * @param value The pre-scaled value to be mapped to the colour gradient, between 0 and 1 (inclusive).
	 * 
	 * @return java.awt.Color The output colour 
	 * 
	 * @throws IllegalArgumentException if the value is out of bounds.
	 */
	public Color getColour(double value) {
		if(value > 1.0 || value < 0.0) {
			throw new IllegalArgumentException("Provided value is out of bounds: " + value);
		}
		
       if(null == steps) {
    	   return getColourForValueHSB(value);
       } 
       
       return getColourForValueStops(value);
    }
	
	private Color getColourForValueHSB(double value) {
		float hue = (float) value * hueRange;
		if(clockwise) {
			hue = hueStart + hue;
		} else {
			hue = hueStart - hue;
		}
        return Color.getHSBColor(hue, saturation, brightness);
    }
	
	private Color getColourForValueStops(double value) {
		if(value > 1.0 || value < 0.0) {
			throw new RuntimeException("Unexpect value: " + value);
		}
        //Determine between which colours we sit
        double scaledPosition = value * (steps.length - 1);
        int stopIndex = (int) Math.floor(scaledPosition);
        if (stopIndex == steps.length -1) {
        	return steps[stopIndex];
        }
        Color colour1 = steps[stopIndex];
        Color colour2 = steps[stopIndex + 1];

        //Linearly interpolate between the two stops
        double stopFraction = scaledPosition - stopIndex;
        int r = (int) (colour1.getRed() * (1 - stopFraction) + colour2.getRed() * stopFraction);
        int g = (int) (colour1.getGreen() * (1 - stopFraction) + colour2.getGreen() * stopFraction);
        int b = (int) (colour1.getBlue() * (1 - stopFraction) + colour2.getBlue() * stopFraction);
        return new Color(r, g, b);
    }
}
