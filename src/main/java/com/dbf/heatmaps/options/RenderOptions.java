package com.dbf.heatmaps.options;

import java.awt.Font;

public class RenderOptions {
	
	/* CELLS */
	private static final int DEFAULT_CELL_WIDTH  = 50;
	private static final int DEFAULT_CELL_HEIGHT = DEFAULT_CELL_WIDTH;
	private static final int DEFAULT_GRID_WIDTH  = 1;
	private int cellWidth  = DEFAULT_CELL_WIDTH;
	private int cellHeight = DEFAULT_CELL_HEIGHT;
	private int gridWidth  = DEFAULT_GRID_WIDTH;
	
	private boolean showGridlines = false;
	
	/* PADDING */
	private static final int DEFAULT_LABEL_PADDING  = 10;
	private static final int DEFAULT_CHART_TITLE_PADDING = DEFAULT_LABEL_PADDING * 4;
	private static final int DEFAULT_LEGEND_PADDING = DEFAULT_CHART_TITLE_PADDING;
	private static final int DEFAULT_OUTSIDE_PADDING = 5;
	
	private int labelPadding = DEFAULT_LABEL_PADDING;
	private int chartTitlePadding = DEFAULT_CHART_TITLE_PADDING;
	private int outsidePadding = DEFAULT_OUTSIDE_PADDING;
	private int legendPadding = DEFAULT_LEGEND_PADDING;
	
	private Font basicFont = new Font("Calibri", Font.PLAIN, 20);
	private Font smallTitleFont = new Font("Calibri", Font.BOLD, 20);
	private Font bigTitleFont = new Font("Calibri", Font.BOLD, 36);
	
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
	public int getGridWidth() {
		return gridWidth;
	}
	public void setGridWidth(int gridWidth) {
		this.gridWidth = gridWidth;
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
	public int getChartTitlePadding() {
		return chartTitlePadding;
	}
	public void setChartTitlePadding(int chartTitlePadding) {
		this.chartTitlePadding = chartTitlePadding;
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
	public Font getSmallTitleFont() {
		return smallTitleFont;
	}
	public void setSmallTitleFont(Font smallTitleFont) {
		this.smallTitleFont = smallTitleFont;
	}
	public Font getBigTitleFont() {
		return bigTitleFont;
	}
	public void setBigTitleFont(Font bigTitleFont) {
		this.bigTitleFont = bigTitleFont;
	}
}
