package com.dbf.heatmaps.android;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.dbf.heatmaps.android.util.Utils;
import com.dbf.heatmaps.axis.Axis;
import com.dbf.heatmaps.data.DataRecord;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class HeatMap {

    private HeatMapOptions options;
    
    private Axis<?> xAxis;
    private Axis<?> yAxis;
    private String title = "";

    private HeatMap(Builder builder) {
		this.options = builder.options;
		this.xAxis = builder.xAxis;
		this.yAxis = builder.yAxis;
		this.title = builder.title;
	}
    
    public HeatMap() {}
    
    public HeatMap(HeatMapOptions options, Axis<?> xAxis, Axis<?> yAxis, String title) {
        this.options = options;
        this.xAxis = xAxis;
        this.yAxis = yAxis;
        this.title = title;
    }

    /**
     * Renders the heat map data to a bitmap image.
     */
    public Bitmap render(Collection<DataRecord> data) {
    	
    	//Basic sanity checks
        if (data == null || data.isEmpty())
            throw new IllegalArgumentException("Missing data.");

        validate();

        //Determine the bounds of the data values
        double minValue = Double.MAX_VALUE;
		double maxValue = Double.MIN_VALUE;
		if(null == options.getColourScaleLowerBound() || null == options.getColourScaleUpperBound()) {
			for (DataRecord record: data) {
				if(record.getValue() < minValue) minValue = record.getValue();
				if(record.getValue() > maxValue) maxValue = record.getValue();
			}
		}
		
		//Adjust the data bounds if the scale is clamped
		boolean minClamped = false;
		boolean maxClamped = false;
		if(null != options.getColourScaleLowerBound()) {
			minClamped = true;
			minValue = options.getColourScaleLowerBound();
		}
		
		if(null != options.getColourScaleUpperBound()) {
			maxClamped = true;
			maxValue = options.getColourScaleUpperBound();
		}
		
        //Prepare Paint objects for drawing
        Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(options.getHeatMapTitleFontColour().toArgb());
        titlePaint.setTextSize(options.getHeatMapTitleFontSize());
        titlePaint.setTypeface(options.getHeatMapTitleFontTypeface());

        Paint axisTitlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        axisTitlePaint.setColor(options.getAxisTitleFontColour().toArgb());
        axisTitlePaint.setTextSize(options.getAxisTitleFontSize());
        axisTitlePaint.setTypeface(options.getAxisTitleFontTypeface());

        Paint axisLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        axisLabelPaint.setColor(options.getAxisLabelFontColour().toArgb());
        axisLabelPaint.setTextSize(options.getAxisLabelFontSize());
        axisLabelPaint.setTypeface(options.getAxisLabelFontTypeface());

        Paint gridValuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridValuePaint.setColor(options.getGridValuesFontColour().toArgb());
        gridValuePaint.setTextSize(options.getGridValuesFontSize());
        gridValuePaint.setTypeface(options.getGridValuesFontTypeface());

        Paint cellPaint = new Paint();
        cellPaint.setStyle(Paint.Style.FILL);

        Paint gridPaint = new Paint();
        gridPaint.setColor(options.getGridLineColour().toArgb());
        gridPaint.setStrokeWidth(options.getGridLineWidth());
        gridPaint.setStyle(Paint.Style.STROKE);


        //Compute label dimensions
        float maxXLabelWidth = 0;
        float maxXLabelHeight = 0;
        for (String label : xAxis.getEntryLabels().values()) {
            float width = axisLabelPaint.measureText(label);
            if (width > maxXLabelWidth) {
                maxXLabelWidth = width;
            }
            Paint.FontMetrics fm = axisLabelPaint.getFontMetrics();
            float height = fm.descent - fm.ascent;
            if (height > maxXLabelHeight) {
                maxXLabelHeight = height;
            }
        }

        float maxYLabelWidth = 0;
        float maxYLabelHeight = 0;
        for (String label : yAxis.getEntryLabels().values()) {
            float width = axisLabelPaint.measureText(label);
            if (width > maxYLabelWidth) {
                maxYLabelWidth = width;
            }
            Paint.FontMetrics fm = axisLabelPaint.getFontMetrics();
            float height = fm.descent - fm.ascent;
            if (height > maxYLabelHeight) {
                maxYLabelHeight = height;
            }
        }

        //Determine cell dimensions and layout offsets.
        int cellWidth = options.getCellWidth();
        int cellHeight = options.getCellHeight();
        // Ensure cells are big enough for rotated x-axis labels:
        cellWidth = Math.max(cellWidth, (int) maxXLabelHeight + options.getAxisLabelPadding());
        cellHeight = Math.max(cellHeight, (int) maxYLabelHeight + options.getAxisLabelPadding());

        // Calculate title dimensions.
        float titleWidth = titlePaint.measureText(title);
        Paint.FontMetrics titleFm = titlePaint.getFontMetrics();
        float titleHeight = titleFm.descent - titleFm.ascent;

        // Define padding values.
        int outsidePadding = options.getOutsidePadding();

        // Offsets for axis titles and labels.
        int yAxisArea = (int) maxYLabelWidth + options.getAxisLabelPadding() + 50; // 50 px extra for y-axis title.
        int xAxisArea = (int) maxXLabelHeight + options.getAxisLabelPadding() + 50; // 50 px extra for x-axis title.

        // Starting position for the matrix (heat map grid)
        int matrixStartX = outsidePadding + yAxisArea;
        int matrixStartY = outsidePadding + (title.isEmpty() ? 0 : (int) titleHeight + options.getHeatMapTitlePadding()) + xAxisArea / 2;

        // Matrix dimensions based on axis counts and cell sizes.
        int numCols = xAxis.getCount();
        int numRows = yAxis.getCount();
        int matrixWidth = numCols * cellWidth;
        int matrixHeight = numRows * cellHeight;

        // Overall image dimensions (adding extra space for legend on the right)
        int legendWidthEstimate = 150;
        int imageWidth = matrixStartX + matrixWidth + outsidePadding + legendWidthEstimate;
        int imageHeight = matrixStartY + matrixHeight + outsidePadding +
                ((options.isShowXAxisLabels() && options.isxAxisLabelsBelow()) ? (int) maxXLabelHeight + options.getAxisLabelPadding() : 0);

        //Create Bitmap and Canvas, and draw background.
        Bitmap bitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(options.getBackgroundColour().toArgb());

        //Draw the heat map title (centered at the top)
        if (!title.isEmpty()) {
            float titleX = (imageWidth - titleWidth) / 2;
            // Draw the title just below the top padding.
            float titleY = outsidePadding - titleFm.ascent; 
            canvas.drawText(title, titleX, titleY, titlePaint);
        }

        //Draw Axis Titles
        String xAxisTitle = xAxis.getTitle();
        if (xAxisTitle != null && !xAxisTitle.isEmpty()) {
            float xAxisTitleWidth = axisTitlePaint.measureText(xAxisTitle);
            float xAxisTitleX = matrixStartX + matrixWidth / 2 - xAxisTitleWidth / 2;
            float xAxisTitleY = matrixStartY - options.getAxisTitlePadding();
            canvas.drawText(xAxisTitle, xAxisTitleX, xAxisTitleY, axisTitlePaint);
        }

        // Y-axis title (drawn vertically to the left)
        String yAxisTitle = yAxis.getTitle();
        if (yAxisTitle != null && !yAxisTitle.isEmpty()) {
            float yAxisTitleWidth = axisTitlePaint.measureText(yAxisTitle);
            // Save canvas state, then translate and rotate.
            canvas.save();
            // Translate so that rotated text appears in the left margin.
            canvas.translate(outsidePadding, matrixStartY + matrixHeight / 2 + yAxisTitleWidth / 2);
            canvas.rotate(-90);
            canvas.drawText(yAxisTitle, 0, 0, axisTitlePaint);
            canvas.restore();
        }

        //Draw Axis Labels
        boolean rotateXLabels = options.isxAxisLabelsRotate();
        if (options.isShowXAxisLabels()) {
            for (Map.Entry<String, Integer> entry : xAxis.getLabelIndices().entrySet()) {
                String label = entry.getKey();
                int index = entry.getValue();
                float labelWidth = axisLabelPaint.measureText(label);
                float centerX = matrixStartX + index * cellWidth + cellWidth / 2;
                if (rotateXLabels) {
                    // Draw rotated labels
                    canvas.save();
                    // For rotated text, translate to the label center; adjust Y based on label width.
                    float x = centerX;
                    float y = matrixStartY + matrixHeight + options.getAxisLabelPadding() + labelWidth;
                    canvas.translate(x, y);
                    canvas.rotate(-90);
                    canvas.drawText(label, 0, 0, axisLabelPaint);
                    canvas.restore();
                } else {
                    // Draw horizontal labels centered below the matrix.
                    float x = centerX - labelWidth / 2;
                    float y = matrixStartY + matrixHeight + options.getAxisLabelPadding() +
                            (axisLabelPaint.getFontMetrics().descent - axisLabelPaint.getFontMetrics().ascent);
                    canvas.drawText(label, x, y, axisLabelPaint);
                }
            }
        }
        // Y-axis labels: drawn to the left of the matrix.
        if (options.isShowYAxisLabels()) {
            for (Map.Entry<String, Integer> entry : yAxis.getLabelIndices().entrySet()) {
                String label = entry.getKey();
                int index = entry.getValue();
                float labelWidth = axisLabelPaint.measureText(label);
                float x = matrixStartX - options.getAxisLabelPadding() - labelWidth;
                float y = matrixStartY + index * cellHeight + cellHeight / 2 +
                        (axisLabelPaint.getFontMetrics().descent - axisLabelPaint.getFontMetrics().ascent) / 2;
                canvas.drawText(label, x, y, axisLabelPaint);
            }
        }

        //Draw the Heat Map Cells
        for (DataRecord record : data) {
            if (record.getValue() == null)
                continue;
            int xIndex = xAxis.getIndex(record.getX());
            int yIndex = yAxis.getIndex(record.getY());
            double value = record.getValue();
            // Clamp if necessary.
            if (minClamped || maxClamped) {
                value = Math.max(Math.min(value, maxValue), minValue);
            }
            double normalized = (value - minValue) / valueRange;
            int cellColor = options.getGradient().getColour(normalized).toArgb();
            cellPaint.setColor(cellColor);

            int left = matrixStartX + xIndex * cellWidth + (options.isShowGridlines() ? options.getGridLineWidth() : 0);
            int top = matrixStartY + yIndex * cellHeight + (options.isShowGridlines() ? options.getGridLineWidth() : 0);
            int right = left + cellWidth - (options.isShowGridlines() ? options.getGridLineWidth() : 0);
            int bottom = top + cellHeight - (options.isShowGridlines() ? options.getGridLineWidth() : 0);
            canvas.drawRect(left, top, right, bottom, cellPaint);
        }

        //Draw Legend (if enabled)
        if (options.isShowLegend()) {
            // Determine the number of legend steps (either from options or a default value).
            int legendSteps = (options.getLegendSteps() != null) ? options.getLegendSteps() : Math.max(yAxis.getCount(), 5);
            int legendBoxWidth = cellWidth;
            int legendBoxHeight = cellHeight;
            int legendStartX = matrixStartX + matrixWidth + options.getLegendPadding();
            int legendStartY = matrixStartY;

            // Calculate the value increment per legend step.
            double stepValue = valueRange / (legendSteps - 1);
            Paint legendLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            legendLabelPaint.setColor(options.getLegendLabelFontColour().toArgb());
            legendLabelPaint.setTextSize(options.getLegendLabelFontSize());
            legendLabelPaint.setTypeface(options.getLegendLabelFontTypeface());

            for (int i = 0; i < legendSteps; i++) {
                double legendValue = minValue + i * stepValue;
                double norm = (legendValue - minValue) / valueRange;
                int legendColor = options.getGradient().getColour(norm).toArgb();
                cellPaint.setColor(legendColor);
                int boxTop = legendStartY + i * legendBoxHeight;
                canvas.drawRect(legendStartX, boxTop, legendStartX + legendBoxWidth, boxTop + legendBoxHeight, cellPaint);

                // Format the label using the legend text format.
                String label = String.format(options.getLegendTextFormat(), legendValue);
                // If clamped, add indicators to the first or last label.
                if (i == 0 && minClamped) {
                    label = "<= " + label;
                }
                if (i == legendSteps - 1 && maxClamped) {
                    label = ">= " + label;
                }
                float labelX = legendStartX + legendBoxWidth + options.getAxisLabelPadding();
                // Center the label vertically in the box.
                Paint.FontMetrics lmFm = legendLabelPaint.getFontMetrics();
                float labelY = boxTop + legendBoxHeight / 2 + (lmFm.descent - lmFm.ascent) / 2;
                canvas.drawText(label, labelX, labelY, legendLabelPaint);
            }
            // Optionally draw a border around the legend.
            canvas.drawRect(legendStartX, legendStartY, legendStartX + legendBoxWidth, legendStartY + legendSteps * legendBoxHeight, gridPaint);
        }

        return bitmap;
    }

    /**
     * Renders the heat map and writes it as a PNG file.
     */
    public void render(File file, Collection<DataRecord> data) throws IOException {
        Bitmap bitmap = render(data);
        FileOutputStream fos = new FileOutputStream(file);
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } finally {
            fos.close();
        }
    }
    
    /**
	 * Performs some basic checks to see if rendering is possible. 
	 */
	public void validate() {
		//Basic sanity checks
		if(null == xAxis || xAxis.getCount() < 1) throw new IllegalArgumentException("The X-axis is undefined.");
		if(null == yAxis || yAxis.getCount() < 1) throw new IllegalArgumentException("The Y-axis is undefined.");
		if(null == options) throw new IllegalArgumentException("The heat map options are undefined.");
		options.validate();
	}

    // Getters and setters
    public HeatMapOptions getOptions() {
        return options;
    }

    public void setOptions(HeatMapOptions options) {
        this.options = options;
    }

    public Axis<?> getxAxis() {
        return xAxis;
    }

    public void setxAxis(Axis<?> xAxis) {
        this.xAxis = xAxis;
    }

    public Axis<?> getyAxis() {
        return yAxis;
    }

    public void setyAxis(Axis<?> yAxis) {
        this.yAxis = yAxis;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    
    /**
	 * Creates builder to build {@link HeatMap}.
	 * @return created builder
	 */
	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private HeatMapOptions options;
		private Axis<?> xAxis;
		private Axis<?> yAxis;
		private String title = "";

		private Builder() {
		}

		/**
		* Builder method for options parameter.
		* @param options field to set
		* @return builder
		*/
		public Builder withOptions(HeatMapOptions options) {
			this.options = options;
			return this;
		}

		/**
		* Builder method for xAxis parameter.
		* @param xAxis field to set
		* @return builder
		*/
		public Builder withXAxis(Axis<?> xAxis) {
			this.xAxis = xAxis;
			return this;
		}

		/**
		* Builder method for yAxis parameter.
		* @param yAxis field to set
		* @return builder
		*/
		public Builder withYAxis(Axis<?> yAxis) {
			this.yAxis = yAxis;
			return this;
		}

		/**
		* Builder method for title parameter.
		* @param title field to set
		* @return builder
		*/
		public Builder withTitle(String title) {
			this.title = title;
			return this;
		}

		/**
		* Builder method of the builder.
		* @return built class
		*/
		public HeatMap build() {
			return new HeatMap(this);
		}
	}
}
