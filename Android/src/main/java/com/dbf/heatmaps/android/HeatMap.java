package com.dbf.heatmaps.android;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Typeface;

import com.dbf.heatmaps.android.util.Utils;
import com.dbf.heatmaps.axis.Axis;
import com.dbf.heatmaps.data.DataRecord;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class HeatMap {

	private static final Color ALTERNATIVE_BACKGROUND_COLOUR = Color.valueOf(0.8235f, 0.8235f, 0.8235f);
	
    private HeatMapOptions options;
    
    private Axis<?> xAxis;
    private Axis<?> yAxis;
    private String title = "";

    /**
	 * 
	 * @return a new empty instance of the HeatMap class
	 */
	public static HeatMap instance() {
		return new HeatMap();
	}
	
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
     * Renders the heat map and writes it as a PNG file.
     * 
     * This is a convenience method that calls {@link #render(Collection)} to build
     * an in-memory {@link Bitmap} and then encodes that image as a PNG.
     *
     * @param file the output file to write to. The file will be created if it does not
     *             exist, and overwritten if it does.
     * @param data the collection of {@code DataRecord} instances to render. Each record
     *             contributes to the heat map.
     *
     * @throws IllegalArgumentException if {@code file} is {@code null}, or if {@code data}
     *                                  is {@code null} or empty
     *                                  
     * @throws IOException if the rendered image cannot be encoded or written to {@code file}
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
     * Renders the heat map data to a {@link Bitmap} image.
     * 
     * @param data collection of {@code DataRecord} instances to render. Must not be {@code null} or empty.
     *
     * @return a {@link Bitmap} containing the rendered heat map
     *
     * @throws IllegalArgumentException if {@code data} is {@code null} or empty, or if the renderer
     *                                  is not in a valid state according to {@link #validate()}.
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
		final double valueRange = maxValue - minValue;
		
		//Determine the dimensions of the X-axis and Y-axis labels
		//We need to render all of the X & Y labels first so we can determine the maximum size the labels will take to render
		final Entry<Integer, Integer> xAxisLabelMaxSize = getMaxStringSize(options.isShowXAxisLabels() ? xAxis.getEntryLabels().values() : Collections.emptyList(), options.getAxisLabelFontTypeface(), options.getAxisLabelFontSize());
		final Entry<Integer, Integer> yAxisLabelMaxSize = getMaxStringSize(options.isShowXAxisLabels() ? yAxis.getEntryLabels().values() : Collections.emptyList(), options.getAxisLabelFontTypeface(), options.getAxisLabelFontSize());
		int xAxisLabelHeight = xAxisLabelMaxSize.getKey(); //Assume rotated by default, we'll check this assumption later
		final int yAxisLabelMaxWidth = yAxisLabelMaxSize.getKey();
		final int axisLabelFontHeight = Math.max(xAxisLabelMaxSize.getValue(), yAxisLabelMaxSize.getValue()); //May or may be set by either axis
		
		//Determine the dimensions of grid values
		final DecimalFormat dataValuesDF = new DecimalFormat(options.getGridValuesFormat()); //Not thread safe, don't make static
		final List<String> dataValues = options.isShowGridValues() ? data.stream().map(r-> r.getValue() == null ? "" : dataValuesDF.format(r.getValue())).collect(Collectors.toList()) : Collections.emptyList();
		final Entry<Integer, Integer> gridValuesDimensions = getMaxStringSize(dataValues, options.getGridValuesFontTypeface(), options.getGridValuesFontSize());
		
		//Determine the dimensions of the axis titles
		final Entry<Integer, Integer> xTitleDimensions = getMaxStringSize(Collections.singletonList(xAxis.getTitle()), options.getAxisTitleFontTypeface(), options.getAxisTitleFontSize());
		final Entry<Integer, Integer> yTitleDimensions = getMaxStringSize(Collections.singletonList(yAxis.getTitle()),  options.getAxisTitleFontTypeface(), options.getAxisTitleFontSize());
		
		//When labels are enabled, the cells need to be at least as big as the font height
		//This is true for the x-axis as  well since at a minimum we can rotate the text
		int cellWidth  = Math.max(options.getCellWidth(),  options.isShowXAxisLabels() ? axisLabelFontHeight + options.getAxisLabelPadding() : 0);
		int cellHeight = Math.max(options.getCellHeight(), options.isShowYAxisLabels() ? axisLabelFontHeight + options.getAxisLabelPadding() : 0);
		
		//The cells also need to be big enough to display the grid values if we are rendering those
		if(options.isShowGridValues()) {
			//We need to leave a bit of margin around the text to not squeeze it. 4 pixels on all sides should be fine.
			cellWidth  = Math.max(cellWidth,  gridValuesDimensions.getKey() + 8);
			cellHeight = Math.max(cellHeight, gridValuesDimensions.getValue() + 8);
		}
		
		//Save a little bit of math later on
		final int halfCellWidth  = cellWidth  / 2;
		final int halfCellHeight = cellHeight / 2;
				
		//Now that we now the width of our grid cells, we can determine if we need to rotate x-axis labels and print them out vertically
		boolean rotateXLabels = false;
		if(options.isShowXAxisLabels()) {
			//Only rotate the x-axis labels when they are too big
			rotateXLabels = options.isxAxisLabelsRotate() || ((xAxisLabelHeight ) > (cellWidth + (options.isShowGridlines() ? options.getGridLineWidth() : 0) - options.getAxisLabelPadding()));
			if(!rotateXLabels) {
				xAxisLabelHeight = xAxisLabelMaxSize.getValue();
			}
		}
		
		//Calculate the legend values
		//We use the defined size, if provided. Otherwise, we take the greater of either the number of cells of the Y-axis or 5.
		final int legendBoxes = options.isShowLegend() ? (options.getLegendSteps() != null ? options.getLegendSteps() : (Math.max(yAxis.getCount(), 5))) : 0;
		
		final double legendSteps = options.isShowLegend()  ? (valueRange > 0 ? valueRange / (legendBoxes-1) : 0) : 0;
		final List<Double> legendValues = new ArrayList<Double>(legendBoxes);
		if(options.isShowLegend()) {
			legendValues.add(minValue);
				for(int i = 1; i < legendBoxes -1; i++) {
					legendValues.add(minValue + (i*legendSteps));
				}
			legendValues.add(maxValue);
		}
		
		//Calculate the legend labels
		final DecimalFormat legendDF = new DecimalFormat(options.getLegendTextFormat()); //Not thread safe, don't make static
		final List<String> legendLabels = legendValues.stream().map(v->legendDF.format(v)).collect(Collectors.toList());
		
		if(options.isShowLegend()) {
			//We need to indicate in the legend if the values are being capped/bounded/clamped
			if(minClamped) legendLabels.set(0, "<= " + legendLabels.get(0));
			if(maxClamped) legendLabels.set(legendValues.size()-1, ">= " + legendLabels.get(legendValues.size()-1));
		}
		
		//Calculate legend sizes
		final Entry<Integer, Integer> legendLabelDimensions = getMaxStringSize(legendLabels, options.getLegendLabelFontTypeface(), options.getLegendLabelFontSize());
		final int legendLabelMaxWidth = legendLabelDimensions.getKey();
		final int legendLabelHeight = legendLabelDimensions.getValue();
		
		final int legendHeight = options.isShowLegend() ? ((cellHeight * legendBoxes) + (options.isShowGridlines() ? (legendBoxes + 1) * options.getGridLineWidth() : 0)) : 0 ;
		
		final int legendBoxesWidth = options.isShowLegend() ? (cellWidth + (options.isShowGridlines() ? 2 * options.getGridLineWidth() : 0)) : 0;
		final int legendWidth = options.isShowLegend() ? (legendBoxesWidth + options.getAxisLabelPadding() + legendLabelMaxWidth) : 0;
		
		//Calculate the X positional values of all of the elements first
		final int yAxisTitleStartPosX = options.getOutsidePadding() + (!yAxis.getTitle().isEmpty() ? yTitleDimensions.getValue() : 0);
		final int yAxisLabelStartPosX = yAxisTitleStartPosX + (!yAxis.getTitle().isEmpty() ? options.getAxisTitlePadding() : 0);
		final int matrixStartPosX = yAxisLabelStartPosX + (options.isShowYAxisLabels() ? (yAxisLabelMaxWidth + options.getAxisLabelPadding()) : 0);
		final int matrixWidth = (xAxis.getCount()  * cellWidth) + (options.isShowGridlines() ? (xAxis.getCount() + 1) * options.getGridLineWidth() : 0);
		final int matrixCentreX =  matrixStartPosX + (matrixWidth/2);
		final int xAxisLabelStartPosX = matrixStartPosX + (options.isShowGridlines() ? options.getGridLineWidth() : 0);
		final int legendStartPosX = matrixStartPosX + matrixWidth + (options.isShowLegend() ? options.getLegendPadding() : 0);
		final int legendLabelStartPosX = legendStartPosX + (options.isShowLegend() ? legendBoxesWidth +  options.getAxisLabelPadding() : 0);
		
		//Calculate the overall image width
		//Outside padding + Y Axis Title + label padding + Y Axis Labels + label padding + chart width + legend padding + legend width + outside padding
        final int imageWidth   = legendStartPosX + legendWidth + options.getOutsidePadding();
        final int imageCenterY = imageWidth/2;
		
        //Now that we know the image width we can figure out if we need to wrap the text of the big chart title
        final int chartTitleMaxWidth = imageWidth - (options.getOutsidePadding()*2);
        List<Entry<String, Entry<Integer, Integer>>> titleLines = null;
        if(!title.isEmpty()) titleLines = getTitleSized(title, chartTitleMaxWidth, options.getHeatMapTitleFontTypeface(), options.getHeatMapTitleFontSize()); //Empty chart titles are supported
        final int chartTitleLineHeight = title.isEmpty() ? 0 : titleLines.get(0).getValue().getValue();
        final int chartTitleHeight = title.isEmpty() ? 0 : titleLines.size() * chartTitleLineHeight;
        
		//Now that we know the chart title height, we can calculate the Y positional values
        final int chartTitleStartPosY = options.getOutsidePadding();
		final int xAxisTitleStartPosY = chartTitleStartPosY + (!title.isEmpty() ? chartTitleHeight + options.getHeatMapTitlePadding() : 0) + (!xAxis.getTitle().isEmpty() ? xTitleDimensions.getValue() : 0); //Text positions are bottom left!!
		int xAxisLabelStartPosY = xAxisTitleStartPosY + (!xAxis.getTitle().isEmpty() ? options.getAxisTitlePadding() : 0) + ((options.isShowXAxisLabels() && !options.isxAxisLabelsBelow()) ? xAxisLabelHeight : 0);
		final int matrixStartPosY = xAxisLabelStartPosY + ((options.isShowXAxisLabels() && !options.isxAxisLabelsBelow()) ? options.getAxisLabelPadding() : 0);
		final int matrixHeight = (yAxis.getCount()  * cellHeight) + (options.isShowGridlines() ? (yAxis.getCount() + 1) * options.getGridLineWidth() : 0);
		final int matrixCentreY =  matrixStartPosY + (matrixHeight/2);
		final int yAxisLabelStartPosY = matrixStartPosY + (options.isShowGridlines() ? options.getGridLineWidth() : 0);
		final int legendStartPosY = (matrixHeight>=legendHeight) ? (matrixCentreY - (legendHeight/2)) : matrixStartPosY; //Legend is centred with the Matrix only if the matrix is big enough
		final int legendLabelStartPosY = legendStartPosY + (int)(legendLabelHeight*0.75) + (options.isShowGridlines() ? options.getGridLineWidth() : 0); //Label positions are bottom left!! 1/4 font fudge factor
		
		//This part is a bit complicated. If the x-axis labels are rendered at the bottom then we need to recalculate xAxisLabelStartPosY,
		//This will be different if the axis labels are rotated because they need to be aligned vertically at the top. 
		//So the Y offset, which is measured from the bottom, will be different for every label when rotated.
		if(options.isShowXAxisLabels() && options.isxAxisLabelsBelow()) {
			final int bottomOfChartY = matrixStartPosY + matrixHeight + options.getAxisLabelPadding(); //Don't forget the padding
			if(rotateXLabels) {
				xAxisLabelStartPosY = bottomOfChartY; //We will add the text length for each label before rendering it
			} else {
				xAxisLabelStartPosY = bottomOfChartY + (int)(axisLabelFontHeight*0.75); //Text is drawn from the bottom corner, 3/4 fudge factor
			}
		}
		
		//Finally, we can figure out the overall image height
        //Outside padding + big title + title padding + X Axis Title + label padding + X Axis Labels + label padding + chart height + outside padding
        final int imageHeight = matrixStartPosY + Math.max(matrixHeight, legendHeight) + ((options.isShowXAxisLabels() && options.isxAxisLabelsBelow()) ? (options.getAxisLabelPadding() + xAxisLabelHeight): 0) + options.getOutsidePadding();
     
        //We are ready to start the actual drawing
        //Create Bitmap and Canvas, and draw background.
        Bitmap bitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint backgroundPaint = new Paint();
        backgroundPaint.setStyle(Paint.Style.FILL);
        //If the background colour is explicitly set, then use it. Otherwise, automatically determine a good colour.
		if(null != options.getBackgroundColour()) {
			backgroundPaint.setColor(options.getBackgroundColour().toArgb());
		} else {
			//Make the background all white, except if the colour scale goes to white
			Color maxColour = options.getGradient().getColour(1.0);
			if(maxColour.blue() > 240 && maxColour.green() > 240 && maxColour.red() > 240) {
				backgroundPaint.setColor(ALTERNATIVE_BACKGROUND_COLOUR.toArgb());
			} else {
				backgroundPaint.setColor(Color.WHITE);
			}
		}
		canvas.drawRect(0, 0, imageWidth, imageHeight, backgroundPaint);
		
		//Render the chart title
        if (!title.isEmpty()) {
        	Paint titlePaint = new Paint();
            titlePaint.setAntiAlias(true);
            titlePaint.setColor(options.getHeatMapTitleFontColour().toArgb());
            titlePaint.setTextSize(options.getHeatMapTitleFontSize());
            titlePaint.setTypeface(options.getHeatMapTitleFontTypeface());
            
            for (int i = 0; i < titleLines.size(); i++) {
				Entry<String, Entry<Integer, Integer>> line = titleLines.get(i);
				// Label positions are bottom left so we need to add 1 to the line number
				final int linePosY = chartTitleStartPosY + ((i + 1) * chartTitleLineHeight);
				//Centre each line horizontally
				canvas.drawText(line.getKey(), imageCenterY - (line.getValue().getKey()/2), linePosY, titlePaint);
			}
        }
        
      //Render the legend if needed
        if (options.isShowLegend()) {
            Paint legendLabelPaint = new Paint();
            legendLabelPaint.setAntiAlias(true);
            legendLabelPaint.setColor(options.getLegendLabelFontColour().toArgb());
            legendLabelPaint.setTextSize(options.getLegendLabelFontSize());
            legendLabelPaint.setTypeface(options.getLegendLabelFontTypeface());
            
            //Render the legend labels
    		//The number of legend boxes may be greater than the number of labels
            canvas.drawText(legendLabels.get(legendLabels.size() - 1), legendLabelStartPosX, legendLabelStartPosY, legendLabelPaint);
            canvas.drawText(legendLabels.get(0), legendLabelStartPosX, legendLabelStartPosY + (cellHeight * (legendBoxes - 1))
                    + (options.isShowGridlines() ? options.getGridLineWidth() * (legendBoxes - 1) : 0), legendLabelPaint);
            
            //Only render the rest of the labels if there is a range to the colours
            if (valueRange > 0) {
                for (int i = 1; i < legendBoxes - 1; i++) {
                    int legendLabelPosY = legendLabelStartPosY + (cellHeight * i)
                            + (options.isShowGridlines() ? options.getGridLineWidth() * i : 0);
                    canvas.drawText(legendLabels.get(legendBoxes - i - 1), legendLabelStartPosX, legendLabelPosY, legendLabelPaint);
                }
            }
            
            //Render the legend boxes, starting with the top (maximum colour value) first
            Paint legendBoxPaint = new Paint();
            for (int i = 0; i < legendBoxes; i++) {
                if (i == 0) {
                    legendBoxPaint.setColor(options.getGradient().getColour(1.0).toArgb());
                } else if (i == legendBoxes - 1) {
                    legendBoxPaint.setColor(options.getGradient().getColour(0.0).toArgb());
                } else {
                    final double factor = 1 - ((legendValues.get(i) - minValue) / valueRange);
                    legendBoxPaint.setColor(options.getGradient().getColour(factor).toArgb());
                }
                final int legendBoxPosX = legendStartPosX + (options.isShowGridlines() ? options.getGridLineWidth() : 0);
                final int legendBoxPosY = legendStartPosY + (options.isShowGridlines() ? (options.getGridLineWidth() + i * (cellHeight + options.getGridLineWidth())) : i * cellHeight);
                canvas.drawRect(legendBoxPosX, legendBoxPosY, legendBoxPosX + cellWidth, legendBoxPosY + cellHeight, legendBoxPaint);
                
                //Also render the dividing lines
				//The last box doesn't need a line, that's handled by the outside border
                if (options.isShowGridlines() && (i != legendBoxes - 1)) {
                    Paint gridLinePaint = new Paint();
                    gridLinePaint.setStyle(Paint.Style.FILL);
                    gridLinePaint.setColor(options.getGridLineColour().toArgb());
                    canvas.drawRect(legendBoxPosX, legendBoxPosY + cellHeight,
                            legendBoxPosX + cellWidth, legendBoxPosY + cellHeight + options.getGridLineWidth(), gridLinePaint);
                }
            }
            
            //Render the legend grid lines or outside border
            Paint gridLinePaint = new Paint();
            gridLinePaint.setStyle(Paint.Style.FILL);
            gridLinePaint.setColor(options.getGridLineColour().toArgb());
            if (options.isShowGridlines()) {
                canvas.drawRect(legendStartPosX, legendStartPosY, legendStartPosX + legendBoxesWidth, legendStartPosY + options.getGridLineWidth(), gridLinePaint);//Top
                canvas.drawRect(legendStartPosX, legendStartPosY + legendHeight - options.getGridLineWidth(), legendStartPosX + legendBoxesWidth, legendStartPosY + legendHeight, gridLinePaint); //Bottom
                canvas.drawRect(legendStartPosX, legendStartPosY, options.getGridLineWidth(), legendStartPosY + legendHeight, gridLinePaint); //Left
                canvas.drawRect(legendStartPosX + legendBoxesWidth - options.getGridLineWidth(), legendStartPosY, legendStartPosX + legendBoxesWidth, legendStartPosY + legendHeight, gridLinePaint); //Right
            } else {
            	//Render the legend border on top of the boxes
                gridLinePaint.setStyle(Paint.Style.STROKE);
                canvas.drawRect(legendStartPosX, legendStartPosY, legendStartPosX + cellWidth - 1, legendStartPosY + (cellHeight * legendBoxes) - 1, gridLinePaint);
            }
        }

        //Will will need to determine the width of each axis title individually
        Paint axisTitlePaint = new Paint();
        axisTitlePaint.setAntiAlias(true);
        axisTitlePaint.setColor(options.getAxisTitleFontColour().toArgb());
        axisTitlePaint.setTextSize(options.getAxisTitleFontSize());
        axisTitlePaint.setTypeface(options.getAxisTitleFontTypeface());
        
        //Render the X-axis title
        if (!xAxis.getTitle().isEmpty()) {
        	//TODO: Wrap the title if it's too long
            int xAxisTitleWidth = xTitleDimensions.getKey();
            canvas.drawText(xAxis.getTitle(), matrixCentreX - (xAxisTitleWidth / 2), xAxisTitleStartPosY, axisTitlePaint);
        }
        
        //Render the Y-axis title
        if (!yAxis.getTitle().isEmpty()) {
        	//TODO: Wrap the title if it's too long
            int yAxisTitleWidth = yTitleDimensions.getKey();
            canvas.save();
            canvas.translate(yAxisTitleStartPosX, matrixCentreY + (yAxisTitleWidth / 2));
            canvas.rotate(-90); // Rotate 90 degrees counter-clockwise
            canvas.drawText(yAxis.getTitle(), 0, 0, axisTitlePaint);
            canvas.restore();
        }

        //Will will need to determine the width of each label individually
        Paint axisLabelPaint = new Paint();
        axisLabelPaint.setAntiAlias(true);
        axisLabelPaint.setColor(options.getAxisLabelFontColour().toArgb());
        axisLabelPaint.setTextSize(options.getAxisLabelFontSize());
        axisLabelPaint.setTypeface(options.getAxisLabelFontTypeface());
        
        if (options.isShowXAxisLabels()) {
        	//Draw all of the x labels, drawn vertically or horizontally
            for (Entry<String, Integer> entry : xAxis.getLabelIndices().entrySet()) {
                if (rotateXLabels) {
                    canvas.save(); //Store the current transform
                    int cellOffsetX = xAxisLabelStartPosX + (entry.getValue() * cellWidth)
                            + (options.isShowGridlines() ? entry.getValue() * options.getGridLineWidth() : 0)
                            + halfCellWidth + (int)(axisLabelFontHeight * 0.25);
                    
                    //Need to align vertically at the top if the labels are drawn below the matrix
                    int translateY = xAxisLabelStartPosY + (options.isxAxisLabelsBelow() ? Utils.measureText(axisLabelPaint, entry.getKey()) : 0);
                    canvas.translate(cellOffsetX, translateY);
                    canvas.rotate(-90); // Rotate 90 degrees counter-clockwise
                    canvas.drawText(entry.getKey(), 0, 0, axisLabelPaint); // Draw the x axis label
                    canvas.restore(); //Restore the old transform
                } else {
                    float labelWidth = axisLabelPaint.measureText(entry.getKey());
                    int cellOffsetX = xAxisLabelStartPosX - (int)(labelWidth / 2)
                            + (entry.getValue() * cellWidth)
                            + (options.isShowGridlines() ? entry.getValue() * options.getGridLineWidth() : 0)
                            + halfCellWidth;
                    canvas.drawText(entry.getKey(), cellOffsetX, xAxisLabelStartPosY, axisLabelPaint);
                }
            }
        }
        if (options.isShowYAxisLabels()) {
        	//Add all of the Y labels, drawn horizontally
            int labelVerticalOffset = (int)(axisLabelFontHeight * 0.25);
            for (Entry<String, Integer> entry : yAxis.getLabelIndices().entrySet()) {
                float labelWidth = axisLabelPaint.measureText(entry.getKey());
                int cellOffsetY = yAxisLabelStartPosY + labelVerticalOffset
                        + (entry.getValue() * cellHeight)
                        + (options.isShowGridlines() ? entry.getValue() * options.getGridLineWidth() : 0)
                        + halfCellHeight;
                //Aligned right
                canvas.drawText(entry.getKey(), yAxisLabelStartPosX + (yAxisLabelMaxWidth - (int)labelWidth), cellOffsetY, axisLabelPaint);
            }
        }

        //Determine if we should smoothly blend the colours
        if (options.isBlendColours()) {
        	final int scaleFactor = options.getBlendColoursScale();
        	
        	//Blending colours means we are going to first render the graph as a tiny image and then scale it up using interpolation.
			//Render the whole map using a single pixel per cell
        	
        	final int tinyMatrixWidth = xAxis.getCount();
        	final int tinyMatrixHeight = yAxis.getCount();
        	final int bilinearMatrixWidth = tinyMatrixWidth * scaleFactor;
        	final int bilinearMatrixHeight = tinyMatrixHeight * scaleFactor;
        	
            Bitmap tinyMatrixBitmap = Bitmap.createBitmap(tinyMatrixWidth, tinyMatrixHeight, Bitmap.Config.ARGB_8888);
            Bitmap bilinearMatrixMask = Bitmap.createBitmap(bilinearMatrixWidth, bilinearMatrixHeight, Bitmap.Config.ARGB_8888);
            
            Canvas tinyCanvas = new Canvas(tinyMatrixBitmap);
            Canvas maskCanvas = new Canvas(bilinearMatrixMask);
            
            Paint maskPaint = new Paint();
            maskPaint.setStyle(Paint.Style.FILL);
            maskPaint.setColor(Color.BLACK); //Black means fully opaque
            
            Paint cellPaint = new Paint();
            for (DataRecord record : data) {
                if (record.getValue() == null) continue;  //No data, perfectly valid.
                
                //Determine the colour for this pixel of the map
                final double val = minClamped || maxClamped ? Math.max(Math.min(record.getValue(), maxValue), minValue) : record.getValue();
                final double factor = (valueRange == 0 ? 1.0 : (val - minValue) / valueRange);
                cellPaint.setColor(options.getGradient().getColour(factor).toArgb());
                final int x = xAxis.getIndex(record.getX());
                final int y = yAxis.getIndex(record.getY());
                tinyCanvas.drawRect(x, y, x + 1, y + 1, cellPaint);
                maskCanvas.drawRect(x * scaleFactor, y * scaleFactor, (x + 1) * scaleFactor, (y + 1) * scaleFactor, maskPaint);
            }
            
            //Now we scale the image up using bilinear scaling
	        //This will linearly interpolate the colours of each pixel
	        //We use bilinear interpolation because it will only consider a single neighbouring pixel in each direction
            Bitmap bilinearScaledBitmap = Bitmap.createScaledBitmap(tinyMatrixBitmap, bilinearMatrixWidth, bilinearMatrixHeight, true);
            Canvas bilinearCanvas = new Canvas(bilinearScaledBitmap);
            
            
            //We need to clean up the transparent sections of the image since they have been blended.
	        //The mask will give us crisp transparent edges after the scaling is applied
            Paint maskApplyPaint = new Paint();
            maskApplyPaint.setXfermode(new android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.DST_IN));
            bilinearCanvas.drawBitmap(bilinearMatrixMask, 0, 0, maskApplyPaint);
            
	        //Scale the tiny image up to the full size using nearest neighbour interpolation
	        //We use nearest neighbour interpolation because it will keep the edges sharp
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bilinearScaledBitmap, matrixWidth, matrixHeight, false);
            
	        //Draw the scaled image on top of the main canvas
	        //Note that the grid lines, if present, will be drawn directly on top
            canvas.drawBitmap(scaledBitmap, matrixStartPosX, matrixStartPosY, null);
        } else {
        	//Draw the heat map itself, normally. No scaling trickery, this is much simpler.
            Paint cellPaint = new Paint();
            for (DataRecord record : data) {
                if (record.getValue() == null) continue;
                
                int x = xAxis.getIndex(record.getX());
                int y = yAxis.getIndex(record.getY());
                
                //Determine the colour for this square of the map
                final double val = minClamped || maxClamped ? Math.max(Math.min(record.getValue(), maxValue), minValue) : record.getValue();
                final double factor = (valueRange == 0 ? 1.0 : (val - minValue) / valueRange);
                cellPaint.setColor(options.getGradient().getColour(factor).toArgb());
                
                final int matrixCellOffsetX = options.isShowGridlines() ? x * (cellWidth + options.getGridLineWidth()) : x * cellWidth;
                final int matrixCellOffsetY = options.isShowGridlines() ? y * (cellHeight + options.getGridLineWidth()) : y * cellHeight;
                
                final int matrixBoxPosX = matrixStartPosX + (options.isShowGridlines() ? options.getGridLineWidth() : 0) + matrixCellOffsetX;
                final int matrixBoxPosY = matrixStartPosY + (options.isShowGridlines() ? options.getGridLineWidth() : 0) + matrixCellOffsetY;
                canvas.drawRect(matrixBoxPosX, matrixBoxPosY, matrixBoxPosX + cellWidth, matrixBoxPosY + cellHeight, cellPaint);
            }
        }

        //Draw the grid values, if needed
        if (options.isShowGridValues()) {
            Paint gridValuesPaint = new Paint();
            gridValuesPaint.setAntiAlias(true);
            gridValuesPaint.setColor(options.getGridValuesFontColour().toArgb()); 
            gridValuesPaint.setTextSize(options.getGridValuesFontSize());
            gridValuesPaint.setTypeface(options.getGridValuesFontTypeface());
            
            final int textVerticalOffset = (int)(gridValuesPaint.getTextSize() * 0.25);
            int gridValIndex = 0;
            for (DataRecord record : data) {
            	final String val = dataValues.get(gridValIndex);
                if (!val.isEmpty()) {
                	final int x = xAxis.getIndex(record.getX());
                	final int y = yAxis.getIndex(record.getY());
                	
                    int matrixCellOffsetX = options.isShowGridlines() ? x * (cellWidth + options.getGridLineWidth()) : x * cellWidth;
                    int matrixCellOffsetY = options.isShowGridlines() ? y * (cellHeight + options.getGridLineWidth()) : y * cellHeight;
                    
                    int matrixBoxPosX = matrixStartPosX + (options.isShowGridlines() ? options.getGridLineWidth() : 0) + matrixCellOffsetX;
                    int matrixBoxPosY = matrixStartPosY + (options.isShowGridlines() ? options.getGridLineWidth() : 0) + matrixCellOffsetY;
                    
                    float textWidth = gridValuesPaint.measureText(val);
                    int textPosX = matrixBoxPosX + halfCellWidth - (int)(textWidth / 2);
                    int textPosY = matrixBoxPosY + halfCellHeight + textVerticalOffset;
                    
                    canvas.drawText(val, textPosX, textPosY, gridValuesPaint);
                }
                gridValIndex++;
            }
        }

        //Draw the grid lines
        if (options.isShowGridlines()) {
            Paint gridLinePaint = new Paint();
            gridLinePaint.setStyle(Paint.Style.FILL);
            gridLinePaint.setColor(options.getGridLineColour().toArgb());
            
            for (int y = 0; y <= yAxis.getCount(); y++) { //Y grid lines
                int matrixOffsetY = y * (cellHeight + options.getGridLineWidth());
                canvas.drawRect(matrixStartPosX, matrixStartPosY + matrixOffsetY, matrixStartPosX + matrixWidth,
                        matrixStartPosY + matrixOffsetY + options.getGridLineWidth(), gridLinePaint);
            }
            for (int x = 0; x <= xAxis.getCount(); x++) { //X grid lines
                int matrixOffsetX = x * (cellWidth + options.getGridLineWidth());
                canvas.drawRect(matrixStartPosX + matrixOffsetX, matrixStartPosY, matrixStartPosX + matrixOffsetX + options.getGridLineWidth(),
                        matrixStartPosY + matrixHeight, gridLinePaint);
            }
        }
        
      //We are done! 🙂
      return bitmap;
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
	
	/**
	 * Returns the X and Y dimensions of the longest string of text, in a collection of strings, when rendered.
	 * 
	 * Note that when rendered, the longest string is not necessarily the ones with the most characters.
	 * 
	 * @param strings A collection of strings to be measured. 
	 * @param typeface The Typeface used for rendering the text.
	 * @param textSize The size of the rendered the text.
	 * 
	 * @return The X and Y dimensions of the longest string of text, in the form of <code>Entry&lt;x_dimension, y_dimension&gt;</code>
	 */
	protected Entry<Integer, Integer> getMaxStringSize(Collection<String> strings, Typeface typeface, float textSize) {
		if (null == strings || strings.isEmpty()) return new AbstractMap.SimpleEntry<Integer, Integer>(0, 0);
		
		final Paint measurePaint = new Paint();
        measurePaint.setAntiAlias(true);
        measurePaint.setTextSize(textSize);
        measurePaint.setTypeface(typeface);
        
        //Assume it never changes
        final FontMetrics fontMetrics = measurePaint.getFontMetrics();
        
        int maxLabelLength = 0;
        final int labelHeight = Utils.getFontSize(fontMetrics);
        
        //Note: the max label size is not necessarily the one with the most characters.
        maxLabelLength = strings.stream().map(s->Utils.measureText(measurePaint, s)).max(Integer::compareTo).orElse(0);
        return new AbstractMap.SimpleEntry<Integer, Integer>(maxLabelLength, labelHeight);
	}
	
	/**
	 * Takes a long title and splits it out into multiple lines of text to make it fit within the <code>maxWidth</code> when rendered.
	 * 
	 * @param title The title in String form.
	 * @param maxWidth The maximum width, in pixel, that each line of the title will be when rendered.
	 * @param typeface The Typeface used for rendering the text.
	 * @param textSize The size of the rendered the text.
	 * 
	 * @return A list of the individual lines of text of the title, with each entry containing the X and Y dimensions of the line,
	 * in pixels. In the form of <code>List&lt;Entry&lt;line_of_text, Entry&lt;x_dimension, y_dimension&gt;&gt;&gt;</code> 
	 */
	protected List<Entry<String, Entry<Integer, Integer>>> getTitleSized(String title, int maxWidth, Typeface typeface, float textSize) {
		//Create a temporary image to get Graphics2D context for measuring
		final Paint measurePaint = new Paint();
        measurePaint.setAntiAlias(true);
        measurePaint.setTextSize(textSize);
        measurePaint.setTypeface(typeface);
        
        List<Entry<String, Entry<Integer, Integer>>> titleLines = new ArrayList<Entry<String, Entry<Integer, Integer>>>();

        //Assume it never changes
        final FontMetrics fontMetrics = measurePaint.getFontMetrics();
         
        final int fontHeight = Utils.getFontSize(fontMetrics);
         
         //Handle the case where the title is too long to fit on one line
         String[] words = title.split(" ");
         StringBuilder currentLine = new StringBuilder();
         String currentLineString;
         int lineWidth;
         
         for (String word : words) {
        	 //always add the first word to the line. Otherwise, we'd get stuck in a loop if the first word is too long
        	 if(currentLine.length() == 0) {
        		 currentLine.append(word);
        		 continue;
        	 }
        	 
             //Check the width of the current line with the next word
        	 int previousCharLen = currentLine.length();
        	 currentLine.append(" ");
        	 currentLine.append(word);
            
        	 currentLineString = currentLine.toString();
             lineWidth = Utils.measureText(measurePaint, currentLineString);
             if (lineWidth < maxWidth) continue; //More room is left
             
             if (lineWidth == maxWidth) {
            	 titleLines.add(new AbstractMap.SimpleEntry<String, Entry<Integer, Integer>>(currentLineString,  new AbstractMap.SimpleEntry<Integer, Integer>(lineWidth, fontHeight)));
            	 currentLine.setLength(0);
            	 continue;
             }
             
             //We have exceeded our maximum. We need to roll back the string builder.
             currentLine.setLength(previousCharLen);
             currentLineString = currentLine.toString();
             lineWidth = Utils.measureText(measurePaint, currentLineString);
             titleLines.add(new AbstractMap.SimpleEntry<String, Entry<Integer, Integer>>(currentLineString,  new AbstractMap.SimpleEntry<Integer, Integer>(lineWidth, fontHeight)));
             
             //Reset the string builder for the next line
             currentLine.setLength(0);
             currentLine.append(word);
         }

         // Add the last line
         if (currentLine.length() > 0) {
        	 currentLineString = currentLine.toString();
             lineWidth = Utils.measureText(measurePaint, currentLineString);
             titleLines.add(new AbstractMap.SimpleEntry<String, Entry<Integer, Integer>>(currentLineString, new AbstractMap.SimpleEntry<Integer, Integer>(lineWidth, fontHeight)));
         }

        return titleLines;
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
