package com.dbf.heatmaps.options;

import com.dbf.heatmaps.HeatMapGradient;

public class GradientOptions {
	
	private HeatMapGradient gradient = HeatMapGradient.BASIC_GRADIENT;
	private Double colourLowerBound;
	private Double colourUpperBound;
	
	public HeatMapGradient getGradient() {
		return gradient;
	}
	public void setGradient(HeatMapGradient gradient) {
		this.gradient = gradient;
	}
	public Double getColourLowerBound() {
		return colourLowerBound;
	}
	public void setColourLowerBound(Double colourLowerBound) {
		this.colourLowerBound = colourLowerBound;
	}
	public Double getColourUpperBound() {
		return colourUpperBound;
	}
	public void setColourUpperBound(Double colourUpperBound) {
		this.colourUpperBound = colourUpperBound;
	}
}
