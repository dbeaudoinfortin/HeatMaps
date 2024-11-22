package com.dbf.heatmaps;

import java.awt.Color;
import java.awt.Font;

public class HeatMapOptions {
	
	/* FONTS */
	private static final Color DEFAULT_FONT_COLOUR = Color.BLACK;
	private static final Font  DEFAULT_BASIC_FONT          = new Font("Calibri", Font.PLAIN, 20);
	private static final Font  DEFAULT_AXIS_TITLE_FONT     = new Font("Calibri", Font.BOLD, 20);
	private static final Font  DEFAULT_HEATMAP_TITLE_FONT  = new Font("Calibri", Font.BOLD, 36);
	
	/* MAIN CANVAS */
	private static final Color DEFAULT_GRID_COLOUR = Color.BLACK;
	
	private Color backgroundColour;
	
	/* CELLS */
	private static final int DEFAULT_CELL_WIDTH  = 50;
	private static final int DEFAULT_CELL_HEIGHT = DEFAULT_CELL_WIDTH;
	private static final int DEFAULT_GRID_WIDTH  = 1;
	private int cellWidth  = DEFAULT_CELL_WIDTH;
	private int cellHeight = DEFAULT_CELL_HEIGHT;
	
	/* GRID LINES */
	private boolean showGridlines   = false;
	private int gridLineWidth = DEFAULT_GRID_WIDTH;
	private Color gridLineColour = DEFAULT_GRID_COLOUR;
	
	/* AXIS LABELS */
	private boolean showXAxisLabels   = true;
	private boolean showYAxisLabels   = true;
	private boolean xAxisLabelsBelow  = false;
	private boolean xAxisLabelsRotate = false;
	private Font  axisLabelFont = DEFAULT_BASIC_FONT;
	private Color axisLabelFontColour = DEFAULT_FONT_COLOUR;
	
	/* TITLES */
	private Font  axisTitleFont    = DEFAULT_AXIS_TITLE_FONT;
	private Font  heatMapTitleFont = DEFAULT_HEATMAP_TITLE_FONT;
	private Color axisTitleFontColour    = DEFAULT_FONT_COLOUR;
	private Color heatMapTitleFontColour = DEFAULT_FONT_COLOUR;
	
	/* DATA VALUES */
	private static final String DEFAULT_GRID_FORMAT = "0.#";
	private boolean showGridValues = false;
	private String  gridValuesFormat = DEFAULT_GRID_FORMAT;
	private Font    gridValuesFont    = DEFAULT_BASIC_FONT;
	private Color   gridValuesFontColour = DEFAULT_FONT_COLOUR;
	
	/* COLOUR BLENDING */
	private boolean blendColours    = false;
	private static final int DEFAULT_BLEND_SCALE = 3;
	private int blendColoursScale = DEFAULT_BLEND_SCALE;
	
	/* PADDING */
	private static final int DEFAULT_LABEL_PADDING = 10;
	private static final int DEFAULT_AXIS_TITLE_PADDING = DEFAULT_LABEL_PADDING * 2;
	private static final int DEFAULT_CHART_TITLE_PADDING = DEFAULT_LABEL_PADDING * 4;
	private static final int DEFAULT_LEGEND_PADDING = DEFAULT_CHART_TITLE_PADDING;
	private static final int DEFAULT_OUTSIDE_PADDING = 5;
	
	private int axisLabelPadding    = DEFAULT_LABEL_PADDING;
	private int axisTitlePadding    = DEFAULT_AXIS_TITLE_PADDING;
	private int heatMapTitlePadding = DEFAULT_CHART_TITLE_PADDING;
	private int outsidePadding      = DEFAULT_OUTSIDE_PADDING;
	private int legendPadding       = DEFAULT_LEGEND_PADDING;
	
	/* LEGEND */
	private boolean showLegend = true;
	private static final String DEFAULT_LEGEND_FORMAT = "0.##";
	private String  legendTextFormat = DEFAULT_LEGEND_FORMAT;
	private Font    legendLabelFont  = DEFAULT_BASIC_FONT;
	private Color   legendLabelFontColour = DEFAULT_FONT_COLOUR;
	private Integer legendSteps;
	
	/* GRADIENT */
	private HeatMapGradient gradient = HeatMapGradient.BASIC_GRADIENT;
	private Double colourScaleLowerBound;
	private Double colourScaleUpperBound;

	private HeatMapOptions(Builder builder) {
		this.backgroundColour = builder.backgroundColour;
		this.cellWidth = builder.cellWidth;
		this.cellHeight = builder.cellHeight;
		this.showGridlines = builder.showGridlines;
		this.gridLineWidth = builder.gridLineWidth;
		this.gridLineColour = builder.gridLineColour;
		this.showXAxisLabels = builder.showXAxisLabels;
		this.showYAxisLabels = builder.showYAxisLabels;
		this.xAxisLabelsBelow = builder.xAxisLabelsBelow;
		this.xAxisLabelsRotate = builder.xAxisLabelsRotate;
		this.axisLabelFont = builder.axisLabelFont;
		this.axisLabelFontColour = builder.axisLabelFontColour;
		this.axisTitleFont = builder.axisTitleFont;
		this.heatMapTitleFont = builder.heatMapTitleFont;
		this.axisTitleFontColour = builder.axisTitleFontColour;
		this.heatMapTitleFontColour = builder.heatMapTitleFontColour;
		this.showGridValues = builder.showGridValues;
		this.gridValuesFormat = builder.gridValuesFormat;
		this.gridValuesFont = builder.gridValuesFont;
		this.gridValuesFontColour = builder.gridValuesFontColour;
		this.blendColours = builder.blendColours;
		this.blendColoursScale = builder.blendColoursScale;
		this.axisLabelPadding = builder.axisLabelPadding;
		this.axisTitlePadding = builder.axisTitlePadding;
		this.heatMapTitlePadding = builder.heatMapTitlePadding;
		this.outsidePadding = builder.outsidePadding;
		this.legendPadding = builder.legendPadding;
		this.showLegend = builder.showLegend;
		this.legendTextFormat = builder.legendTextFormat;
		this.legendLabelFont = builder.legendLabelFont;
		this.legendLabelFontColour = builder.legendLabelFontColour;
		this.legendSteps = builder.legendSteps;
		this.gradient = builder.gradient;
		this.colourScaleLowerBound = builder.colourScaleLowerBound;
		this.colourScaleUpperBound = builder.colourScaleUpperBound;
	}

	public HeatMapOptions() {}
	
	/**
	 * Performs some basic checks to see if rendering is possible. 
	 */
	public void validate() {
		//Basic sanity checks
		if(cellWidth < 1) throw new IllegalArgumentException("Cell width must be at least 1.");
		if(cellHeight < 1) throw new IllegalArgumentException("Cell height must be at least 1.");
		
		if(gridLineWidth < 0) throw new IllegalArgumentException("Grid line width cannot be negative.");
		if(showGridlines && gridLineWidth < 1) throw new IllegalArgumentException("Grid line width must be at least 1 when grid line rendering is enabled.");
		
		if(axisLabelPadding < 0) throw new IllegalArgumentException("Label padding cannot be negative.");
		if(axisTitlePadding < 0) throw new IllegalArgumentException("Axis title padding cannot be negative.");
		if(heatMapTitlePadding < 0) throw new IllegalArgumentException("Chart title padding cannot be negative.");
		if(outsidePadding < 0) throw new IllegalArgumentException("Outside padding cannot be negative.");
		if(legendPadding < 0) throw new IllegalArgumentException("Legend padding cannot be negative.");
		
		if(null == axisLabelFont) axisLabelFont = DEFAULT_BASIC_FONT;
		if(null == axisTitleFont) axisTitleFont = DEFAULT_AXIS_TITLE_FONT;
		if(null == heatMapTitleFont) heatMapTitleFont = DEFAULT_HEATMAP_TITLE_FONT;
		if(null == legendLabelFont) legendLabelFont = DEFAULT_BASIC_FONT;
		if(null == gridValuesFont) gridValuesFont = DEFAULT_BASIC_FONT;
		
		if(null == axisLabelFontColour) axisLabelFontColour = DEFAULT_FONT_COLOUR;
		if(null == axisTitleFontColour) axisTitleFontColour = DEFAULT_FONT_COLOUR;
		if(null == heatMapTitleFontColour) heatMapTitleFontColour = DEFAULT_FONT_COLOUR;
		if(null == legendLabelFontColour) legendLabelFontColour = DEFAULT_FONT_COLOUR;
		if(null == gridValuesFontColour) gridValuesFontColour = DEFAULT_FONT_COLOUR;
		
		if(null == gradient) gradient = HeatMapGradient.BASIC_GRADIENT;
		
		if(null != legendSteps && legendSteps < 2) throw new IllegalArgumentException("The number of steps of the legend must be at least 2.");
		if(blendColoursScale < 2 || blendColoursScale>20)  throw new IllegalArgumentException("The colour blend scale must be between 2 and 20, inclusive.");
	}
	
	public int getCellWidth() {
		return cellWidth;
	}
	
	public void setCellWidth(int cellWidth) {
		this.cellWidth = cellWidth;
	}
	
	public int getCellHeight() {
		return cellHeight;
	}
	
	public void setCellHeight(int cellHeight) {
		this.cellHeight = cellHeight;
	}
	
	public int getGridLineWidth() {
		return gridLineWidth;
	}
	
	public void setGridWidth(int gridWidth) {
		this.gridLineWidth = gridWidth;
	}
	
	public boolean isShowGridlines() {
		return showGridlines;
	}
	
	public void setShowGridlines(boolean showGridlines) {
		this.showGridlines = showGridlines;
	}
	
	public int getAxisLabelPadding() {
		return axisLabelPadding;
	}
	
	public void setAxisLabelPadding(int axisLabelPadding) {
		this.axisLabelPadding = axisLabelPadding;
	}
	
	public int getHeatMapTitlePadding() {
		return heatMapTitlePadding;
	}
	
	public void setHeatMapTitlePadding(int heatMapTitlePadding) {
		this.heatMapTitlePadding = heatMapTitlePadding;
	}
	
	public int getOutsidePadding() {
		return outsidePadding;
	}
	
	public void setOutsidePadding(int outsidePadding) {
		this.outsidePadding = outsidePadding;
	}
	
	public int getLegendPadding() {
		return legendPadding;
	}
	
	public void setLegendPadding(int legendPadding) {
		this.legendPadding = legendPadding;
	}
	
	public Font getAxisLabelFont() {
		return axisLabelFont;
	}
	
	public void setBasicFont(Font basicFont) {
		this.axisLabelFont = basicFont;
	}
	
	public Font getAxisTitleFont() {
		return axisTitleFont;
	}
	
	public void setAxisTitleFont(Font axisTitleFont) {
		this.axisTitleFont = axisTitleFont;
	}
	
	public Font getHeatMapTitleFont() {
		return heatMapTitleFont;
	}
	
	public void setHeatMapTitleFont(Font bigTitleFont) {
		this.heatMapTitleFont = bigTitleFont;
	}
	
	public String getLegendTextFormat() {
		return legendTextFormat;
	}

	public void setLegendTextFormat(String legendTextFormat) {
		this.legendTextFormat = legendTextFormat;
	}

	public boolean isShowLegend() {
		return showLegend;
	}

	public void setShowLegend(boolean showLegend) {
		this.showLegend = showLegend;
	}

	public HeatMapGradient getGradient() {
		return gradient;
	}

	public void setGradient(HeatMapGradient gradient) {
		this.gradient = gradient;
	}

	public Double getColourScaleLowerBound() {
		return colourScaleLowerBound;
	}

	public void setColourScaleLowerBound(Double colourScaleLowerBound) {
		this.colourScaleLowerBound = colourScaleLowerBound;
	}

	public Double getColourScaleUpperBound() {
		return colourScaleUpperBound;
	}

	public void setColourScaleUpperBound(Double colourScaleUpperBound) {
		this.colourScaleUpperBound = colourScaleUpperBound;
	}

	public Color getGridLineColour() {
		return gridLineColour;
	}

	public void setGridLineColour(Color gridLineColour) {
		this.gridLineColour = gridLineColour;
	}

	public Color getBackgroundColour() {
		return backgroundColour;
	}

	public void setBackgroundColour(Color backgroundColour) {
		this.backgroundColour = backgroundColour;
	}

	public boolean isShowXAxisLabels() {
		return showXAxisLabels;
	}

	public void setShowXAxisLabels(boolean showXAxisLabels) {
		this.showXAxisLabels = showXAxisLabels;
	}

	public boolean isShowYAxisLabels() {
		return showYAxisLabels;
	}

	public void setShowYAxisLabels(boolean showYAxisLabels) {
		this.showYAxisLabels = showYAxisLabels;
	}

	public boolean isBlendColours() {
		return blendColours;
	}

	public void setBlendColours(boolean blendColours) {
		this.blendColours = blendColours;
	}

	public Color getAxisLabelFontColour() {
		return axisLabelFontColour;
	}

	public void setAxisLabelFontColour(Color axisLabelFontColour) {
		this.axisLabelFontColour = axisLabelFontColour;
	}

	public Color getAxisTitleFontColour() {
		return axisTitleFontColour;
	}

	public void setAxisTitleFontColour(Color axisTitleFontColour) {
		this.axisTitleFontColour = axisTitleFontColour;
	}

	public Color getHeatMapTitleFontColour() {
		return heatMapTitleFontColour;
	}

	public void setHeatMapTitleFontColour(Color heatMapTitleFontColour) {
		this.heatMapTitleFontColour = heatMapTitleFontColour;
	}

	public void setGridLineWidth(int gridLineWidth) {
		this.gridLineWidth = gridLineWidth;
	}

	public int getBlendColoursScale() {
		return blendColoursScale;
	}

	public void setBlendColoursScale(int blendColoursScale) {
		this.blendColoursScale = blendColoursScale;
	}

	public boolean isxAxisLabelsBelow() {
		return xAxisLabelsBelow;
	}

	public void setxAxisLabelsBelow(boolean xAxisLabelsBelow) {
		this.xAxisLabelsBelow = xAxisLabelsBelow;
	}

	public boolean isShowGridValues() {
		return showGridValues;
	}

	public void setShowGridValues(boolean showGridValues) {
		this.showGridValues = showGridValues;
	}

	public String getGridValuesFormat() {
		return gridValuesFormat;
	}

	public void setGridValuesFormat(String gridValuesFormat) {
		this.gridValuesFormat = gridValuesFormat;
	}

	public Font getGridValuesFont() {
		return gridValuesFont;
	}

	public void setGridValuesFont(Font gridValuesFont) {
		this.gridValuesFont = gridValuesFont;
	}

	public Color getGridValuesFontColour() {
		return gridValuesFontColour;
	}

	public void setGridValuesFontColour(Color gridValuesFontColour) {
		this.gridValuesFontColour = gridValuesFontColour;
	}

	public Font getLegendLabelFont() {
		return legendLabelFont;
	}

	public void setLegendLabelFont(Font legendLabelFont) {
		this.legendLabelFont = legendLabelFont;
	}

	public Color getLegendLabelFontColour() {
		return legendLabelFontColour;
	}

	public void setLegendLabelFontColour(Color legendLabelFontColour) {
		this.legendLabelFontColour = legendLabelFontColour;
	}

	public void setAxisLabelFont(Font axisLabelFont) {
		this.axisLabelFont = axisLabelFont;
	}

	public Integer getLegendSteps() {
		return legendSteps;
	}

	public void setLegendSteps(Integer legendSteps) {
		this.legendSteps = legendSteps;
	}

	public int getAxisTitlePadding() {
		return axisTitlePadding;
	}

	public void setAxisTitlePadding(int axisTitlePadding) {
		this.axisTitlePadding = axisTitlePadding;
	}

	public boolean isxAxisLabelsRotate() {
		return xAxisLabelsRotate;
	}

	public void setxAxisLabelsRotate(boolean xAxisLabelsRotate) {
		this.xAxisLabelsRotate = xAxisLabelsRotate;
	}

	/**
	 * Creates builder to build {@link HeatMapOptions}.
	 * @return created builder
	 */
	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private Color backgroundColour;
		private int cellWidth = DEFAULT_CELL_WIDTH;
		private int cellHeight = DEFAULT_CELL_HEIGHT;
		private boolean showGridlines = false;
		private int gridLineWidth = DEFAULT_GRID_WIDTH;
		private Color gridLineColour = DEFAULT_GRID_COLOUR;
		private boolean showXAxisLabels = true;
		private boolean showYAxisLabels = true;
		private boolean xAxisLabelsBelow = false;
		private boolean xAxisLabelsRotate = false;
		private Font axisLabelFont = DEFAULT_BASIC_FONT;
		private Color axisLabelFontColour = DEFAULT_FONT_COLOUR;
		private Font axisTitleFont = DEFAULT_AXIS_TITLE_FONT;
		private Font heatMapTitleFont = DEFAULT_HEATMAP_TITLE_FONT;
		private Color axisTitleFontColour = DEFAULT_FONT_COLOUR;
		private Color heatMapTitleFontColour = DEFAULT_FONT_COLOUR;
		private boolean showGridValues = false;
		private String gridValuesFormat = DEFAULT_GRID_FORMAT;
		private Font gridValuesFont = DEFAULT_BASIC_FONT;
		private Color gridValuesFontColour = DEFAULT_FONT_COLOUR;
		private boolean blendColours = false;
		private int blendColoursScale = DEFAULT_BLEND_SCALE;
		private int axisLabelPadding = DEFAULT_LABEL_PADDING;
		private int axisTitlePadding = DEFAULT_AXIS_TITLE_PADDING;
		private int heatMapTitlePadding = DEFAULT_CHART_TITLE_PADDING;
		private int outsidePadding = DEFAULT_OUTSIDE_PADDING;
		private int legendPadding = DEFAULT_LEGEND_PADDING;
		private boolean showLegend = true;
		private String legendTextFormat = DEFAULT_LEGEND_FORMAT;
		private Font legendLabelFont = DEFAULT_BASIC_FONT;
		private Color legendLabelFontColour = DEFAULT_FONT_COLOUR;
		private Integer legendSteps;
		private HeatMapGradient gradient = HeatMapGradient.BASIC_GRADIENT;
		private Double colourScaleLowerBound;
		private Double colourScaleUpperBound;

		private Builder(Builder builder) {
			this.backgroundColour = builder.backgroundColour;
			this.cellWidth = builder.cellWidth;
			this.cellHeight = builder.cellHeight;
			this.showGridlines = builder.showGridlines;
			this.gridLineWidth = builder.gridLineWidth;
			this.gridLineColour = builder.gridLineColour;
			this.showXAxisLabels = builder.showXAxisLabels;
			this.showYAxisLabels = builder.showYAxisLabels;
			this.xAxisLabelsBelow = builder.xAxisLabelsBelow;
			this.xAxisLabelsRotate = builder.xAxisLabelsRotate;
			this.axisLabelFont = builder.axisLabelFont;
			this.axisLabelFontColour = builder.axisLabelFontColour;
			this.axisTitleFont = builder.axisTitleFont;
			this.heatMapTitleFont = builder.heatMapTitleFont;
			this.axisTitleFontColour = builder.axisTitleFontColour;
			this.heatMapTitleFontColour = builder.heatMapTitleFontColour;
			this.showGridValues = builder.showGridValues;
			this.gridValuesFormat = builder.gridValuesFormat;
			this.gridValuesFont = builder.gridValuesFont;
			this.gridValuesFontColour = builder.gridValuesFontColour;
			this.blendColours = builder.blendColours;
			this.blendColoursScale = builder.blendColoursScale;
			this.axisLabelPadding = builder.axisLabelPadding;
			this.axisTitlePadding = builder.axisTitlePadding;
			this.heatMapTitlePadding = builder.heatMapTitlePadding;
			this.outsidePadding = builder.outsidePadding;
			this.legendPadding = builder.legendPadding;
			this.showLegend = builder.showLegend;
			this.legendTextFormat = builder.legendTextFormat;
			this.legendLabelFont = builder.legendLabelFont;
			this.legendLabelFontColour = builder.legendLabelFontColour;
			this.legendSteps = builder.legendSteps;
			this.gradient = builder.gradient;
			this.colourScaleLowerBound = builder.colourScaleLowerBound;
			this.colourScaleUpperBound = builder.colourScaleUpperBound;
		}

		private Builder() {
		}

		/**
		* Builder method for backgroundColour parameter.
		* Sets the background colour of the whole chart.
		* @param backgroundColour field to set
		* @return builder
		*/
		public Builder withBackgroundColour(Color backgroundColour) {
			this.backgroundColour = backgroundColour;
			return this;
		}

		/**
		* Builder method for cellWidth parameter.
		* Sets the desired cell width in pixels.  Minimum is 1.
		* Will be enlarged if it's too small to fit the labels.
		* @param cellWidth field to set
		* @return builder
		*/
		public Builder withCellWidth(int cellWidth) {
			this.cellWidth = cellWidth;
			return this;
		}

		/**
		* Builder method for cellHeight parameter.
		* Sets the desired cell width in pixels.  Minimum is 1.
		* Will be enlarged if it's too small to fit the labels.
		* @param cellHeight field to set
		* @return builder
		*/
		public Builder withCellHeight(int cellHeight) {
			this.cellHeight = cellHeight;
			return this;
		}

		/**
		* Builder method for showGridlines parameter.
		* Toggles the rendering of grid lines on the heat map between the cells.
		* @param showGridlines field to set
		* @return builder
		*/
		public Builder withShowGridlines(boolean showGridlines) {
			this.showGridlines = showGridlines;
			return this;
		}

		/**
		* Builder method for gridLineWidth parameter.
		* Sets the width, in pixels, of the grid lines.
		* @param gridLineWidth field to set
		* @return builder
		*/
		public Builder withGridLineWidth(int gridLineWidth) {
			this.gridLineWidth = gridLineWidth;
			return this;
		}

		/**
		* Builder method for gridLineColour parameter.
		* Sets the colour of the grid lines.
		* @param gridLineColour field to set
		* @return builder
		*/
		public Builder withGridLineColour(Color gridLineColour) {
			this.gridLineColour = gridLineColour;
			return this;
		}

		/**
		* Builder method for showXAxisLabels parameter.
		* Toggles the rendering of the labels for the X-axis.
		* @param showXAxisLabels field to set
		* @return builder
		*/
		public Builder withShowXAxisLabels(boolean showXAxisLabels) {
			this.showXAxisLabels = showXAxisLabels;
			return this;
		}

		/**
		* Builder method for showYAxisLabels parameter.
		* Toggles the rendering of the labels for the Y-axis.
		* @param showYAxisLabels field to set
		* @return builder
		*/
		public Builder withShowYAxisLabels(boolean showYAxisLabels) {
			this.showYAxisLabels = showYAxisLabels;
			return this;
		}

		/**
		* Builder method for xAxisLabelsBelow parameter.
		* Toggles the rendering of the X-axis labels below the heat map instead of above.
		* @param xAxisLabelsBelow field to set
		* @return builder
		*/
		public Builder withXAxisLabelsBelow(boolean xAxisLabelsBelow) {
			this.xAxisLabelsBelow = xAxisLabelsBelow;
			return this;
		}

		/**
		* Builder method for xAxisLabelsRotate parameter.
		* Forces the X-axis labels to be rotated 90 degrees and rendered vertically.
		* Otherwise, the X-axis labels will be automatically rotated if they are bigger than the cell width.
		* When the labels are rendered above the heat map they are vertically aligned to the bottom,
		* and when they are rendered below the heat map they are vertically aligned to the top.
		* @param xAxisLabelsRotate field to set
		* @return builder
		*/
		public Builder withXAxisLabelsRotate(boolean xAxisLabelsRotate) {
			this.xAxisLabelsRotate = xAxisLabelsRotate;
			return this;
		}

		/**
		* Builder method for axisLabelFont parameter.
		* Sets the font used to render the X-axis and Y-axis labels.
		* @param axisLabelFont field to set
		* @return builder
		*/
		public Builder withAxisLabelFont(Font axisLabelFont) {
			this.axisLabelFont = axisLabelFont;
			return this;
		}

		/**
		* Builder method for axisLabelFontColour parameter.
		* Sets the colour used to render the X-axis and Y-axis labels.
		* @param axisLabelFontColour field to set
		* @return builder
		*/
		public Builder withAxisLabelFontColour(Color axisLabelFontColour) {
			this.axisLabelFontColour = axisLabelFontColour;
			return this;
		}

		/**
		* Builder method for axisTitleFont parameter.
		* Sets the font used to render the X-axis and Y-axis titles.
		* @param axisTitleFont field to set
		* @return builder
		*/
		public Builder withAxisTitleFont(Font axisTitleFont) {
			this.axisTitleFont = axisTitleFont;
			return this;
		}

		/**
		* Builder method for heatMapTitleFont parameter.
		* Sets the font used to render the overall chart title.
		* @param heatMapTitleFont field to set
		* @return builder
		*/
		public Builder withHeatMapTitleFont(Font heatMapTitleFont) {
			this.heatMapTitleFont = heatMapTitleFont;
			return this;
		}

		/**
		* Builder method for axisTitleFontColour parameter.
		* Sets the colour used to render the X-axis and Y-axis titles.
		* @param axisTitleFontColour field to set
		* @return builder
		*/
		public Builder withAxisTitleFontColour(Color axisTitleFontColour) {
			this.axisTitleFontColour = axisTitleFontColour;
			return this;
		}

		/**
		* Builder method for heatMapTitleFontColour parameter.
		* Sets the colour used to render the overall chart title.
		* @param heatMapTitleFontColour field to set
		* @return builder
		*/
		public Builder withHeatMapTitleFontColour(Color heatMapTitleFontColour) {
			this.heatMapTitleFontColour = heatMapTitleFontColour;
			return this;
		}

		/**
		* Builder method for showGridValues parameter.
		* Toggles the rendering of the values within each cell of the heat map.
		* @param showGridValues field to set
		* @return builder
		*/
		public Builder withShowGridValues(boolean showGridValues) {
			this.showGridValues = showGridValues;
			return this;
		}

		/**
		* Builder method for gridValuesFormat parameter.
		* Sets the decimal format used to display the values within each cell of the heat map.
		* The Double to String conversion makes use of the Java DecimalFormat class.
		* @param gridValuesFormat field to set
		* @return builder
		*/
		public Builder withGridValuesFormat(String gridValuesFormat) {
			this.gridValuesFormat = gridValuesFormat;
			return this;
		}

		/**
		* Builder method for gridValuesFont parameter.
		* Sets the font used to render the values within each cell of the heat map.
		* @param gridValuesFont field to set
		* @return builder
		*/
		public Builder withGridValuesFont(Font gridValuesFont) {
			this.gridValuesFont = gridValuesFont;
			return this;
		}

		/**
		* Builder method for gridValuesFontColour parameter.
		* Sets the colour used to render the values within each cell of the heat map.
		* @param gridValuesFontColour field to set
		* @return builder
		*/
		public Builder withGridValuesFontColour(Color gridValuesFontColour) {
			this.gridValuesFontColour = gridValuesFontColour;
			return this;
		}

		/**
		* Builder method for blendColours parameter.
		* Toggles the blending of colours between adjacent cells of the heat map grid.
		* @param blendColours field to set
		* @return builder
		*/
		public Builder withBlendColours(boolean blendColours) {
			this.blendColours = blendColours;
			return this;
		}

		/**
		* Builder method for blendColoursScale parameter.
		* Sets the amount (strength) of blending to use.
		* This corresponds to the scaling factor of the bilinear interpolation, essentially how "smooth" the result will be.
		* Minimum is 2, maximum is 20.
		* @param blendColoursScale field to set
		* @return builder
		*/
		public Builder withBlendColoursScale(int blendColoursScale) {
			this.blendColoursScale = blendColoursScale;
			return this;
		}

		/**
		* Builder method for axisLabelPadding parameter.
		* 	Sets the amount of blank space (padding), in pixels, between the X-axis and Y-axis labels and the heat map grid.
		* @param axisLabelPadding field to set
		* @return builder
		*/
		public Builder withAxisLabelPadding(int axisLabelPadding) {
			this.axisLabelPadding = axisLabelPadding;
			return this;
		}

		/**
		* Builder method for axisTitlePadding parameter.
		* Sets the amount of blank space (padding), in pixels, below (for the X-axis) and to the right (for the Y-axis) of the axis titles.
		* @param axisTitlePadding field to set
		* @return builder
		*/
		public Builder withAxisTitlePadding(int axisTitlePadding) {
			this.axisTitlePadding = axisTitlePadding;
			return this;
		}

		/**
		* Builder method for heatMapTitlePadding parameter.
		* Sets the amount of blank space (padding), in pixels, below the overall chart title.
		* @param heatMapTitlePadding field to set
		* @return builder
		*/
		public Builder withHeatMapTitlePadding(int heatMapTitlePadding) {
			this.heatMapTitlePadding = heatMapTitlePadding;
			return this;
		}

		/**
		* Builder method for outsidePadding parameter.
		* Sets the amount of blank space (padding), in pixels, on the perimeter of the entire chart.
		* @param outsidePadding field to set
		* @return builder
		*/
		public Builder withOutsidePadding(int outsidePadding) {
			this.outsidePadding = outsidePadding;
			return this;
		}

		/**
		* Builder method for legendPadding parameter.
		* Sets the amount of blank space (padding), in pixels, between the legend and the heat map grid.
		* @param legendPadding field to set
		* @return builder
		*/
		public Builder withLegendPadding(int legendPadding) {
			this.legendPadding = legendPadding;
			return this;
		}

		/**
		* Builder method for showLegend parameter.
		* Toggles the rendering of the legend, including the legend labels.
		* @param showLegend field to set
		* @return builder
		*/
		public Builder withShowLegend(boolean showLegend) {
			this.showLegend = showLegend;
			return this;
		}

		/**
		* Builder method for legendTextFormat parameter.
		* Sets the decimal format used to display the values of the legend labels.
		* The Double to String conversion makes use of the Java DecimalFormat class.
		* @param legendTextFormat field to set
		* @return builder
		*/
		public Builder withLegendTextFormat(String legendTextFormat) {
			this.legendTextFormat = legendTextFormat;
			return this;
		}

		/**
		* Builder method for legendLabelFont parameter.
		* Sets the font used to render the legend labels.
		* @param legendLabelFont field to set
		* @return builder
		*/
		public Builder withLegendLabelFont(Font legendLabelFont) {
			this.legendLabelFont = legendLabelFont;
			return this;
		}

		/**
		* Builder method for legendLabelFontColour parameter.
		* Sets the colour used to render the legend labels.
		* @param legendLabelFontColour field to set
		* @return builder
		*/
		public Builder withLegendLabelFontColour(Color legendLabelFontColour) {
			this.legendLabelFontColour = legendLabelFontColour;
			return this;
		}

		/**
		* Builder method for legendSteps parameter.
		* Sets the number of discrete colour steps to include in the legend.
		* The minimum value is 2.
		* @param legendSteps field to set
		* @return builder
		*/
		public Builder withLegendSteps(Integer legendSteps) {
			this.legendSteps = legendSteps;
			return this;
		}

		/**
		* Builder method for gradient parameter.
		* Sets the colour gradient for the heat map.
		* @param gradient field to set
		* @return builder
		*/
		public Builder withGradient(HeatMapGradient gradient) {
			this.gradient = gradient;
			return this;
		}

		/**
		* Builder method for colourScaleLowerBound parameter.
		* Restricts the minimum value (low bound) of the heat map gradient.
		* Any value below this threshold will be assigned the same minimum colour according to the chosen gradient.
		* @param colourScaleLowerBound field to set
		* @return builder
		*/
		public Builder withColourScaleLowerBound(Double colourScaleLowerBound) {
			this.colourScaleLowerBound = colourScaleLowerBound;
			return this;
		}

		/**
		* Builder method for colourScaleUpperBound parameter.
		* Restricts the maximum value (upper bound) of the heat map gradient.
		* Any value above this threshold will be assigned the same maximum colour according to the chosen gradient.
		* @param colourScaleUpperBound field to set
		* @return builder
		*/
		public Builder withColourScaleUpperBound(Double colourScaleUpperBound) {
			this.colourScaleUpperBound = colourScaleUpperBound;
			return this;
		}

		/**
		* Builder method of the builder.
		* @return built class
		*/
		public HeatMapOptions build() {
			return new HeatMapOptions(this);
		}

		/**
		 * Creates builder to build {@link Builder}.
		 * @return created builder
		 */
		public static Builder builder() {
			return new Builder();
		}
	}
}
