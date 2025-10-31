package com.dbf.heatmaps.common;

public abstract class AbstractHeatMapGradient<C> {
	
	protected static final String[] EXTENDED_GRADIENT = new String[] {
		"#3288bd", // Blue
		"#65c1a5",
		"#91d5a6",
		"#c9e89a", 
		"#eaf79b",
		"#fcffba",
		"#fffbba", 
		"#fee492",
		"#fdc771",
		"#fda159", 
		"#f46c43", 
		"#d53e4f"  //Red
	};
	
	protected static final String[] BASIC_GRADIENT = new String[] {
		"#1d4877", //Dark Blue
		"#1b8a5a", //Green
		"#fbb021", //Golden Yellow
		"#f68838", //Orange
		"#ee3e32"  //Red
	};
	
	protected static final String[] TWO_COLOUR_GRADIENT = new String[] {
		"#0000FF", //Blue
		"#FF0000" //Red
	};
	
	protected static final String[] COLOUR_BLIND_GRADIENT = new String[] {
		"#e4ff7a", //Light Greenish Yellow
		"#ffe81a", //Bright Yellow
		"#ffbd00", //Deep Yellow
		"#ffa000", //Orange
		"#fc7f00"  //Darker Orange
	};
	
	/**
	 * A 3 step black-red-orange gradient, similar to black-body radiation, up to approximately 1300 degrees kelvin.
	 */
	protected static final String[] BLACK_RED_ORANGE_GRADIENT = new String[] {
		"#000000", //Black
		"#8e060a", //Red
		"#fda32b"  //Orange
	};
	
	protected static final String[] WHITE_HOT_GRADIENT = new String[] {
		"#000000", //Black
		"#8e060a", //Red
		"#fda32b", //Orange
		"#FFCF9F", //Light Orange
		"#FEF9FF"  //Whitish-blue
	};

	protected static final String[] CUBEHELIX_GRADIENT = new String[] {
		"#000000",
		"#090309",
		"#100614",
		"#160a1f",
		"#190f2b",
		"#1a1536",
		"#1a1c3f",
		"#182448",
		"#152d4e",
		"#123752",
		"#104153",
		"#0e4b53",
		"#0d544f",
		"#0e5d4b",
		"#126644",
		"#176d3d",
		"#207336",
		"#2a782f",
		"#387b29",
		"#477d25",
		"#577d23",
		"#697d24",
		"#7b7c28",
		"#8d7a2f",
		"#9e7938",
		"#ae7745",
		"#bd7654",
		"#c87564",
		"#d27677",
		"#d9788a",
		"#dd7b9d",
		"#de80af",
		"#de86c1",
		"#db8dd1",
		"#d795de",
		"#d29fe9",
		"#cca9f1",
		"#c7b3f7",
		"#c3bdfa",
		"#c0c8fb",
		"#bed1fa",
		"#bfdaf8",
		"#c2e2f6",
		"#c7e9f3",
		"#cdeff1",
		"#d6f3f0",
		"#e0f7f0",
		"#eafaf3",
		"#f5fdf8",
		"#ffffff"
	};
	

	protected static final String[] GREY_GRADIENT = new String[] {
		"#E3E3E3", //Grey
		"#000000" //Black
	};
	
	private final C[] steps;
	
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
	protected AbstractHeatMapGradient(C[] steps) {
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
	 * @param brightness The brightness of the colours, as a fractional value between zero and one.
	 * @param clockwise Whether the hue should be applied clockwise (true) or counter-clockwise (false).
	 * 
	 * @throws IllegalArgumentException if any parameters are outside their allowed range.
	 */
	protected AbstractHeatMapGradient(int hueStart, int hueEnd, float saturation, float brightness, boolean clockwise) {
		if(hueStart < 0 || hueStart > 360) throw new IllegalArgumentException("Hue Start must be at least zero and less than or equal to 360.");
		if(hueEnd < 0 || hueEnd > 360) throw new IllegalArgumentException("Hue End must be at least zero and less than or equal to 360.");
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
		this.hueStart = hueStart/360f;
		this.hueRange = Math.abs(this.hueStart - (hueEnd/360f));
	}
	
	/**
	 * Calculates a colour based on the gradient defined.
	 * 
	 * @param value The pre-scaled value to be mapped to the colour gradient, between 0 and 1 (inclusive).
	 * 
	 * @return The output colour 
	 * 
	 * @throws IllegalArgumentException if the value is out of bounds.
	 */
	public C getColour(double value) {
		if (value > 1.0 || value < 0.0) {
			throw new IllegalArgumentException("Provided value is out of bounds: " + value);
		}

		if (null == steps) {
			return getColourForValueHSB(value);
		}

		return getColourForValueStops(value);
	}

	private C getColourForValueHSB(double value) {
		float hue = (float) value * hueRange;
		hue = clockwise ? (hueStart + hue) : (hueStart - hue);
        return createColour(hue, saturation, brightness);
    }
	
	private C getColourForValueStops(double value) {
		//TODO: Add option for a logarithmic scale
		if(value > 1.0 || value < 0.0) {
			throw new RuntimeException("Unexpect value: " + value);
		}
		//Determine between which colours we sit
		double scaledPosition = value * (steps.length - 1);
		int stopIndex = (int) Math.floor(scaledPosition);
		if (stopIndex == steps.length -1) {
			return steps[stopIndex];
		}
		C colour1 = steps[stopIndex];
		C colour2 = steps[stopIndex + 1];
		
		//Linearly interpolate between the two stops
		return createColour(colour1, colour2, (float) scaledPosition - stopIndex);
    }
	
	/**
	 *  Create a colour object based on the HSB colour model.
	 *  
	 * @param hue The hue of the colour, in degrees, between zero and 360 (inclusive).
	 * @param saturation The saturation of the colour, as a fractional value between zero and one.
	 * @param brightness The brightness of the colour, as a fractional value between zero and one.
	 * @return The output colour.
	 */
	protected abstract C createColour(float hue, float saturation, float brightness);
	
	/**
	 * Create a colour object based on the interpolation of two provided colour objects.
	 * 
	 * @param colour1 The first colour object to use for interpolation.
	 * @param colour2 The second colour object to use for interpolation.
	 * @param fraction The amount of linear interpolation between the two colours as afractional value between zero and one.
	 * @return The output colour
	 */
    protected abstract C createColour(C colour1, C colour2, float fraction);
    
    public static abstract class Builder<C> {
		private C[] steps;
		private int hueStart;
		private int hueEnd;
		private float saturation;
		private float brightness;
		private boolean clockwise;

		public Builder() {}
		
		/**
		* Builder method for steps parameter.
		* @param steps field to set
		* @return builder
		*/
		public Builder<C> withSteps(C[] steps) {
			this.steps = steps;
			return this;
		}

		/**
		* Builder method for hueStart parameter.
		* @param hueStart field to set
		* @return builder
		*/
		public Builder<C> withHueStart(int hueStart) {
			this.hueStart = hueStart;
			return this;
		}

		/**
		* Builder method for hueEnd parameter.
		* @param hueEnd field to set
		* @return builder
		*/
		public Builder<C> withHueEnd(int hueEnd) {
			this.hueEnd = hueEnd;
			return this;
		}

		/**
		* Builder method for saturation parameter.
		* @param saturation field to set
		* @return builder
		*/
		public Builder<C> withSaturation(float saturation) {
			this.saturation = saturation;
			return this;
		}

		/**
		* Builder method for brightness parameter.
		* @param brightness field to set
		* @return builder
		*/
		public Builder<C> withBrightness(float brightness) {
			this.brightness = brightness;
			return this;
		}

		/**
		* Builder method for clockwise parameter.
		* @param clockwise field to set
		* @return builder
		*/
		public Builder<C> withClockwise(boolean clockwise) {
			this.clockwise = clockwise;
			return this;
		}

		/**
		* Build method of the builder.
		* @return built class
		*/
		public AbstractHeatMapGradient<C> build() {
			if(null != steps) {
				return createGradient(steps);
			} else {
				return createGradient(hueStart, hueEnd, saturation, brightness, clockwise);
			}
		}
		
		protected abstract AbstractHeatMapGradient<C> createGradient(C[] steps);
		protected abstract AbstractHeatMapGradient<C> createGradient(int hueStart, int hueEnd, float saturation, float brightness, boolean clockwise);
	}

}
