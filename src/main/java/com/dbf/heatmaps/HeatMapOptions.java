package com.dbf.heatmaps;

import java.awt.Font;

public class HeatMapOptions {
	
	/* MAIN CANVAS */
	//TODO: add custom background colour
	
	/* CELLS */
	private static final int DEFAULT_CELL_WIDTH  = 50;
	private static final int DEFAULT_CELL_HEIGHT = DEFAULT_CELL_WIDTH;
	private static final int DEFAULT_GRID_WIDTH  = 1;
	private int cellWidth  = DEFAULT_CELL_WIDTH;
	private int cellHeight = DEFAULT_CELL_HEIGHT;
	private int gridLineWidth  = DEFAULT_GRID_WIDTH;
	
	/* OPTIONAL REDNDERING */
	private boolean showGridlines = false;
	private boolean showLegend    = true;
	
	//TODO: Implement me
	private boolean showXAxisLabels    = true;
	private boolean showYAxisLabels    = true;
	
	/* PADDING */
	private static final int DEFAULT_LABEL_PADDING  = 10;
	private static final int DEFAULT_CHART_TITLE_PADDING = DEFAULT_LABEL_PADDING * 4;
	private static final int DEFAULT_LEGEND_PADDING = DEFAULT_CHART_TITLE_PADDING;
	private static final int DEFAULT_OUTSIDE_PADDING = 5;
	
	private int labelPadding        = DEFAULT_LABEL_PADDING;
	private int heatMapTitlePadding = DEFAULT_CHART_TITLE_PADDING;
	private int outsidePadding      = DEFAULT_OUTSIDE_PADDING;
	private int legendPadding       = DEFAULT_LEGEND_PADDING;
	
	/* FONTS */
	private static final Font DEFAULT_BASIC_FONT = new Font("Calibri", Font.PLAIN, 20);
	private static final Font DEFAULT_AXIS_TITLE_FONT = new Font("Calibri", Font.BOLD, 20);
	private static final Font DEFAULT_HEATMAP_TITLE_FONT  = new Font("Calibri", Font.BOLD, 36);
	
	private Font basicFont = DEFAULT_BASIC_FONT;
	private Font axisTitleFont = DEFAULT_AXIS_TITLE_FONT;
	private Font heatMapTitleFont = DEFAULT_HEATMAP_TITLE_FONT;
	
	//TODO: add custom font colours
	
	/* LEGEND */
	private String legendTextFormat = "0.####";
	private HeatMapGradient gradient = HeatMapGradient.BASIC_GRADIENT;
	private Double colourScaleLowerBound;
	private Double colourScaleUpperBound;

	private HeatMapOptions(Builder builder) {
		this.cellWidth = builder.cellWidth;
		this.cellHeight = builder.cellHeight;
		this.gridLineWidth = builder.gridLineWidth;
		this.showGridlines = builder.showGridlines;
		this.showLegend = builder.showLegend;
		this.labelPadding = builder.labelPadding;
		this.heatMapTitlePadding = builder.heatMapTitlePadding;
		this.outsidePadding = builder.outsidePadding;
		this.legendPadding = builder.legendPadding;
		this.basicFont = builder.basicFont;
		this.axisTitleFont = builder.axisTitleFont;
		this.heatMapTitleFont = builder.heatMapTitleFont;
		this.legendTextFormat = builder.legendTextFormat;
		this.gradient = builder.gradient;
		this.colourScaleLowerBound = builder.colourScaleLowerBound;
		this.colourScaleUpperBound = builder.colourScaleUpperBound;
	}
	
	/**
	 * 
	 * @return a new empty instance of the HeatMapOptions class
	 */
	public static HeatMapOptions instance() {
		return new HeatMapOptions();
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
		
		if(labelPadding < 0) throw new IllegalArgumentException("Label padding cannot be negative.");
		if(heatMapTitlePadding < 0) throw new IllegalArgumentException("Chart title padding cannot be negative.");
		if(outsidePadding < 0) throw new IllegalArgumentException("Outside padding cannot be negative.");
		if(legendPadding < 0) throw new IllegalArgumentException("Legend padding cannot be negative.");
		
		if(null == basicFont) basicFont = DEFAULT_BASIC_FONT;
		if(null == axisTitleFont) axisTitleFont = DEFAULT_AXIS_TITLE_FONT;
		if(null == heatMapTitleFont) heatMapTitleFont = DEFAULT_HEATMAP_TITLE_FONT;
		
		if(null == gradient) gradient = HeatMapGradient.BASIC_GRADIENT;
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
	
	public int getLabelPadding() {
		return labelPadding;
	}
	
	public void setLabelPadding(int labelPadding) {
		this.labelPadding = labelPadding;
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
	
	public Font getBasicFont() {
		return basicFont;
	}
	
	public void setBasicFont(Font basicFont) {
		this.basicFont = basicFont;
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

	/**
	 * Creates builder to build {@link HeatMapOptions}.
	 * @return created builder
	 */
	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private int cellWidth = DEFAULT_CELL_WIDTH;
		private int cellHeight = DEFAULT_CELL_HEIGHT;
		private int gridLineWidth = DEFAULT_GRID_WIDTH;
		private boolean showGridlines = false;
		private boolean showLegend = true;
		private int labelPadding = DEFAULT_LABEL_PADDING;
		private int heatMapTitlePadding = DEFAULT_CHART_TITLE_PADDING;
		private int outsidePadding = DEFAULT_OUTSIDE_PADDING;
		private int legendPadding = DEFAULT_LEGEND_PADDING;
		private Font basicFont = DEFAULT_BASIC_FONT;
		private Font axisTitleFont = DEFAULT_AXIS_TITLE_FONT;
		private Font heatMapTitleFont = DEFAULT_HEATMAP_TITLE_FONT;
		private String legendTextFormat = "0.####";
		private HeatMapGradient gradient = HeatMapGradient.BASIC_GRADIENT;
		private Double colourScaleLowerBound;
		private Double colourScaleUpperBound;

		private Builder() {
		}

		/**
		* Builder method for cellWidth parameter.
		* @param cellWidth field to set
		* @return builder
		*/
		public Builder withCellWidth(int cellWidth) {
			this.cellWidth = cellWidth;
			return this;
		}

		/**
		* Builder method for cellHeight parameter.
		* @param cellHeight field to set
		* @return builder
		*/
		public Builder withCellHeight(int cellHeight) {
			this.cellHeight = cellHeight;
			return this;
		}

		/**
		* Builder method for gridLineWidth parameter.
		* @param gridLineWidth field to set
		* @return builder
		*/
		public Builder withGridLineWidth(int gridLineWidth) {
			this.gridLineWidth = gridLineWidth;
			return this;
		}

		/**
		* Builder method for showGridlines parameter.
		* @param showGridlines field to set
		* @return builder
		*/
		public Builder withShowGridlines(boolean showGridlines) {
			this.showGridlines = showGridlines;
			return this;
		}

		/**
		* Builder method for showLegend parameter.
		* @param showLegend field to set
		* @return builder
		*/
		public Builder withShowLegend(boolean showLegend) {
			this.showLegend = showLegend;
			return this;
		}

		/**
		* Builder method for labelPadding parameter.
		* @param labelPadding field to set
		* @return builder
		*/
		public Builder withLabelPadding(int labelPadding) {
			this.labelPadding = labelPadding;
			return this;
		}

		/**
		* Builder method for heatMapTitlePadding parameter.
		* @param heatMapTitlePadding field to set
		* @return builder
		*/
		public Builder withHeatMapTitlePadding(int heatMapTitlePadding) {
			this.heatMapTitlePadding = heatMapTitlePadding;
			return this;
		}

		/**
		* Builder method for outsidePadding parameter.
		* @param outsidePadding field to set
		* @return builder
		*/
		public Builder withOutsidePadding(int outsidePadding) {
			this.outsidePadding = outsidePadding;
			return this;
		}

		/**
		* Builder method for legendPadding parameter.
		* @param legendPadding field to set
		* @return builder
		*/
		public Builder withLegendPadding(int legendPadding) {
			this.legendPadding = legendPadding;
			return this;
		}

		/**
		* Builder method for basicFont parameter.
		* @param basicFont field to set
		* @return builder
		*/
		public Builder withBasicFont(Font basicFont) {
			this.basicFont = basicFont;
			return this;
		}

		/**
		* Builder method for axisTitleFont parameter.
		* @param axisTitleFont field to set
		* @return builder
		*/
		public Builder withAxisTitleFont(Font axisTitleFont) {
			this.axisTitleFont = axisTitleFont;
			return this;
		}

		/**
		* Builder method for heatMapTitleFont parameter.
		* @param heatMapTitleFont field to set
		* @return builder
		*/
		public Builder withHeatMapTitleFont(Font heatMapTitleFont) {
			this.heatMapTitleFont = heatMapTitleFont;
			return this;
		}

		/**
		* Builder method for legendTextFormat parameter.
		* @param legendTextFormat field to set
		* @return builder
		*/
		public Builder withLegendTextFormat(String legendTextFormat) {
			this.legendTextFormat = legendTextFormat;
			return this;
		}

		/**
		* Builder method for gradient parameter.
		* @param gradient field to set
		* @return builder
		*/
		public Builder withGradient(HeatMapGradient gradient) {
			this.gradient = gradient;
			return this;
		}

		/**
		* Builder method for colourScaleLowerBound parameter.
		* @param colourScaleLowerBound field to set
		* @return builder
		*/
		public Builder withColourScaleLowerBound(Double colourScaleLowerBound) {
			this.colourScaleLowerBound = colourScaleLowerBound;
			return this;
		}

		/**
		* Builder method for colourScaleUpperBound parameter.
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
	}
}
