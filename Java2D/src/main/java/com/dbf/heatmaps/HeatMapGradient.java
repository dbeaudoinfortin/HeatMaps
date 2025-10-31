package com.dbf.heatmaps;

import java.awt.Color;
import java.util.ArrayList;

import com.dbf.heatmaps.common.AbstractHeatMapGradient;

public class HeatMapGradient extends AbstractHeatMapGradient<Color> {
	
	/**
	 * A smooth (no steps) gradient based on the colour wheel (HSB model) from blue to red, passing through green.
	 * All the of the colours are fully saturated and full brightness
	 */
	public static final HeatMapGradient SMOOTH_GRADIENT = new HeatMapGradient(240, 360, 1.0f, 1.0f, false);
	
	/**
	 * A 12 step gradient from blue to green to red, with medium saturation.
	 * The yellow to red range has been extended, while the blue to green range has been compressed.
	 */
	public static final HeatMapGradient EXTENDED_GRADIENT = new HeatMapGradient(createHeatMapGradient(AbstractHeatMapGradient.EXTENDED_GRADIENT));
	
	/**
	 * A simplified 5 step gradient from blue to red. Saturated and a bit darker.
	 */
	public static final HeatMapGradient BASIC_GRADIENT = new HeatMapGradient(createHeatMapGradient(AbstractHeatMapGradient.BASIC_GRADIENT));
	
	/**
	 * A simple two colour gradient from blue to red, with purple mixed in-between.
	 */
	public static final HeatMapGradient TWO_COLOUR_GRADIENT = new HeatMapGradient(createHeatMapGradient(AbstractHeatMapGradient.TWO_COLOUR_GRADIENT));
	
	/**
	 * A 5 step colour blind friendly gradient of greenish-yellow to dark orange.
	 */
	public static final HeatMapGradient COLOUR_BLIND_GRADIENT = new HeatMapGradient(createHeatMapGradient(AbstractHeatMapGradient.COLOUR_BLIND_GRADIENT));
	
	/**
	 * A 3 step black-red-orange gradient, similar to black-body radiation, up to approximately 1300 degrees kelvin.
	 */
	public static final HeatMapGradient BLACK_RED_ORANGE_GRADIENT = new HeatMapGradient(createHeatMapGradient(AbstractHeatMapGradient.BLACK_RED_ORANGE_GRADIENT));
	
	/**
	 * A 5 step black-red-orange-white gradient, similar to black-body radiation, extended  up to approximately 6500k degrees kelvin.
	 */
	public static final HeatMapGradient WHITE_HOT_GRADIENT = new HeatMapGradient(createHeatMapGradient(AbstractHeatMapGradient.WHITE_HOT_GRADIENT));

	/**
	 * A 50 step colour gradient based on Dave Green's ‘cubehelix’ colour scheme.
	 * https://people.phy.cam.ac.uk/dag9/CUBEHELIX/
	 */
	public static final HeatMapGradient CUBEHELIX_GRADIENT = new HeatMapGradient(createHeatMapGradient(AbstractHeatMapGradient.CUBEHELIX_GRADIENT));
	
	/**
	 * A 2 step grey-scale gradient that should be used for non-colour screens/print-outs.
	 */
	public static final HeatMapGradient GREY_GRADIENT = new HeatMapGradient(createHeatMapGradient(AbstractHeatMapGradient.GREY_GRADIENT));
	
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
	
	public HeatMapGradient(Color[] steps) {
		super(steps);
	}
	
	public HeatMapGradient(int hueStart, int hueEnd, float saturation, float brightness, boolean clockwise) {
		super(hueStart, hueEnd, saturation, brightness, clockwise);
	}
	
	private static Color[] createHeatMapGradient(String[] colours) {
		final Color[] colourArray = new Color[colours.length];
		for(int i = 0; i < colours.length; i++) {
			colourArray[i] = Color.decode(colours[i]);
		}
		return colourArray;
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
	
	@Override
	protected Color createColour(float hue, float saturation, float brightness) {
		return Color.getHSBColor(hue, saturation, brightness);
	}
	
	@Override
	protected Color createColour(Color colour1, Color colour2, float fraction) {
		final int r = (int) (colour1.getRed() * (1 - fraction) + colour2.getRed() * fraction);
		final int g = (int) (colour1.getGreen() * (1 - fraction) + colour2.getGreen() * fraction);
		final int b = (int) (colour1.getBlue() * (1 - fraction) + colour2.getBlue() * fraction);
		return new Color(r, g, b);
	}

	/**
	 * Creates builder to build {@link HeatMapGradient}.
	 * @return created builder
	 */
	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder extends AbstractHeatMapGradient.Builder<Color> {
		@Override
		protected AbstractHeatMapGradient<Color> createGradient(Color[] steps) {
			return new HeatMapGradient(steps);
		}

		@Override
		protected AbstractHeatMapGradient<Color> createGradient(int hueStart, int hueEnd, float saturation, float brightness, boolean clockwise) {
			return new HeatMapGradient(hueStart, hueEnd, saturation, brightness, clockwise);
		}
		
	}
}
