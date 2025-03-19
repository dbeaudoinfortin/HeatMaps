package com.dbf.heatmaps;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import com.dbf.heatmaps.axis.Axis;
import com.dbf.heatmaps.data.DataRecord;

public class HeatMap {

	private static final Color ALTERNATIVE_BACKGROUND_COLOUR = new Color(210, 210, 210);
	
	private HeatMapOptions options;

	private Axis<?> xAxis;
	private Axis<?> yAxis;
	private String  title = "";

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
		super();
		this.options = options;
		this.xAxis = xAxis;
		this.yAxis = yAxis;
		this.title = title;
	}

	/**
     * Renders the heat map and writes it as a PNG file.
     */
	public void render(File file, Collection<DataRecord> data) throws IOException {
		if(null == file) throw new IllegalArgumentException("Invalid file.");
		
		BufferedImage heatmapImage = render(data);
		ImageIO.write(heatmapImage, "png", file);
	}
	
	/**
     * Renders the heat map data to a bitmap image.
     */
	public BufferedImage render(Collection<DataRecord> data) {
		//Basic sanity checks
		if(null == data || data.isEmpty())
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
		final Entry<Integer, Integer> xAxisLabelMaxSize = getMaxStringSize(options.isShowXAxisLabels() ? xAxis.getEntryLabels().values() : Collections.emptyList(), options.getAxisLabelFont());
		final Entry<Integer, Integer> yAxisLabelMaxSize = getMaxStringSize(options.isShowXAxisLabels() ? yAxis.getEntryLabels().values() : Collections.emptyList(), options.getAxisLabelFont());
		int xAxisLabelHeight = xAxisLabelMaxSize.getKey(); //Assume rotated by default, we'll check this assumption later
		final int yAxisLabelMaxWidth = yAxisLabelMaxSize.getKey();
		final int axisLabelFontHeight = Math.max(xAxisLabelMaxSize.getValue(), yAxisLabelMaxSize.getValue()); //May or may be set by either axis
		
		//Determine the dimensions of grid values
		final DecimalFormat dataValuesDF = new DecimalFormat(options.getGridValuesFormat()); //Not thread safe, don't make static
		final List<String> dataValues = options.isShowGridValues() ? data.stream().map(r-> r.getValue() == null ? "" : dataValuesDF.format(r.getValue())).collect(Collectors.toList()) : Collections.emptyList();
		final Entry<Integer, Integer> gridValuesDimensions = getMaxStringSize(dataValues, options.getGridValuesFont());
		
		//Determine the dimensions of the axis titles
		final Entry<Integer, Integer> xTitleDimensions = getMaxStringSize(Collections.singletonList(xAxis.getTitle()), options.getAxisTitleFont());
		final Entry<Integer, Integer> yTitleDimensions = getMaxStringSize(Collections.singletonList(yAxis.getTitle()), options.getAxisTitleFont());
		
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
		final Entry<Integer, Integer> legendLabelDimensions = getMaxStringSize(legendLabels, options.getLegendLabelFont());
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
        if(!title.isEmpty()) titleLines = getTitleSized(title, chartTitleMaxWidth, options.getHeatMapTitleFont()); //Empty chart titles are supported
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
        
        //We are ready to start the actual drawing, create the image object.
        BufferedImage heatmapImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = heatmapImage.createGraphics();
        
        //Start the actual drawing onto the canvas
		 try {	 
			//Render the text smoothly, always
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			 
			//If the background colour is explicitly set, then use it. Otherwise, automatically determine a good colour.
			if(null != options.getBackgroundColour()) {
				g2d.setColor(options.getBackgroundColour());
			} else {
				//Make the background all white, except if the colour scale goes to white
				Color maxColour = options.getGradient().getColour(1.0);
				if(maxColour.getBlue() > 240 && maxColour.getGreen() > 240 && maxColour.getRed() > 240) {
					g2d.setColor(ALTERNATIVE_BACKGROUND_COLOUR);
				} else {
					g2d.setColor(Color.WHITE);
				}
			}
			//Paint the background
			g2d.fillRect(0, 0, imageWidth, imageHeight);
			
			//Render the chart title
			if(!title.isEmpty()) {
				//Set the title font
				g2d.setFont(options.getHeatMapTitleFont());
				g2d.setColor(options.getHeatMapTitleFontColour());
				for (int i = 0; i < titleLines.size(); i++) {
					Entry<String, Entry<Integer, Integer>> line = titleLines.get(i);
					// Label positions are bottom left so we need to add 1 to the line number
					final int linePosY = chartTitleStartPosY + ((i + 1) * chartTitleLineHeight);
					//Centre each line horizontally
					g2d.drawString(line.getKey(), imageCenterY - (line.getValue().getKey()/2), linePosY);
				}
			}
			
	        //Render the legend if needed
	        if (options.isShowLegend()) {
	        	//Reset to a decent basic font
		        g2d.setFont(options.getLegendLabelFont());
		        g2d.setColor(options.getLegendLabelFontColour());
		        
		        //Render the legend labels
	    		//The number of legend boxes may be greater than the number of labels
		   		g2d.drawString(legendLabels.get(legendLabels.size()-1), legendLabelStartPosX, legendLabelStartPosY); //First
	    		g2d.drawString(legendLabels.get(0), legendLabelStartPosX, legendLabelStartPosY + (cellHeight * (legendBoxes-1)) + (options.isShowGridlines() ? options.getGridLineWidth()*(legendBoxes-1) : 0)); //Last
	    		if(valueRange > 0 ) {
	    			//Only render the rest of the labels if there is a range to the colours
	    			for(int i = 1; i < legendBoxes-1; i++) {
	    				final int legendLabelPosY = legendLabelStartPosY + (cellHeight * i) + (options.isShowGridlines() ? options.getGridLineWidth()*i : 0);
	    				g2d.drawString(legendLabels.get(legendBoxes-i-1), legendLabelStartPosX, legendLabelPosY);
	    			}
	    		}
	    		
	    		//Render the legend boxes, starting with the top (maximum colour value) first
	    		for(int i = 0; i < legendBoxes; i++) {
	    			if(i == 0) {
	    				g2d.setColor(options.getGradient().getColour(1.0));
	    			} else if (i == legendBoxes -1) {
	    				g2d.setColor(options.getGradient().getColour(0.0));
	    			} else {
	    				g2d.setColor(options.getGradient().getColour((1-(legendValues.get(i)-minValue)/valueRange)));
	    			}
	    			final int legendBoxPosX = legendStartPosX + (options.isShowGridlines() ? options.getGridLineWidth() : 0);
	    			final int legendBoxPosY = legendStartPosY + (options.isShowGridlines() ? (options.getGridLineWidth() + i * (cellHeight + options.getGridLineWidth())) : i * cellHeight);
					g2d.fillRect(legendBoxPosX, legendBoxPosY, cellWidth, cellHeight);
					
					//Also render the dividing lines
					//The last box doesn't need a line, that's handled by the outside border
					if(options.isShowGridlines() && (i != legendBoxes -1)) {
						g2d.setColor(options.getGridLineColour()); //Reset back to grid line colour! The last colour was from the legend
						g2d.fillRect(legendBoxPosX, legendBoxPosY + cellHeight, cellWidth, options.getGridLineWidth()); 
					}
	    		}
	    		
	    		//Render the legend grid lines or outside border
	    		g2d.setColor(options.getGridLineColour());
	    		if(options.isShowGridlines()) {
	    			g2d.fillRect(legendStartPosX, legendStartPosY, legendBoxesWidth, options.getGridLineWidth()); //Top
	    			g2d.fillRect(legendStartPosX, legendStartPosY + legendHeight - options.getGridLineWidth(), legendBoxesWidth, options.getGridLineWidth()); //Bottom
	    			g2d.fillRect(legendStartPosX, legendStartPosY, options.getGridLineWidth(), legendHeight); //Left
	    			g2d.fillRect(legendStartPosX + legendBoxesWidth - options.getGridLineWidth(), legendStartPosY, options.getGridLineWidth(), legendHeight); //Right
	    		} else {
	    			//Render the legend border on top of the boxes
	        		g2d.drawRect(legendStartPosX, legendStartPosY, cellWidth-1, (cellHeight*legendBoxes) -1);
	    		}
	        }
    		
    		//Will will need to determine the width of each axis title individually using fontMetrics
    		g2d.setFont(options.getAxisTitleFont());
    		g2d.setColor(options.getAxisTitleFontColour()); //Set to Axis title colour. The last colour was from the legend.
    		
    		//Render the X-axis title
	    	if(!xAxis.getTitle().isEmpty()) {
	    		//TODO: Wrap the title if it's too long
	    		final int xAxisTitleWidth = xTitleDimensions.getKey();
	    		g2d.drawString(xAxis.getTitle(), matrixCentreX - (xAxisTitleWidth/2), xAxisTitleStartPosY);
	    	}
    		
    		//Render the Y-axis title
	    	if(!yAxis.getTitle().isEmpty()) {
	    		//TODO: Wrap the title if it's too long
	    		final int yAxisTitleWidth = yTitleDimensions.getKey();
	    		AffineTransform transform = g2d.getTransform();
	    		g2d.translate(yAxisTitleStartPosX, matrixCentreY + (yAxisTitleWidth/2));
				g2d.rotate(-Math.PI / 2); // Rotate 90 degrees counter-clockwise
	    		g2d.drawString(yAxis.getTitle(), 0, 0);
	    		g2d.setTransform(transform);
	    	}
    		
    		//Will will need to determine the width of each label individually using fontMetrics
    		g2d.setFont(options.getAxisLabelFont());
    		g2d.setColor(options.getAxisLabelFontColour());
	    	FontMetrics labelFontMetrics = g2d.getFontMetrics(); //Font is different between titles and labels
	    	
	    	if(options.isShowXAxisLabels()) {
	    		//Draw all of the x labels, drawn vertically or horizontally
		    	AffineTransform transform;
	    		for (Entry<String, Integer> entry : xAxis.getLabelIndices().entrySet()) {
	    			if(rotateXLabels) {
		    			//Store the current transform
		    			transform = g2d.getTransform();
		    			final int cellOffsetX = xAxisLabelStartPosX + (entry.getValue() * cellWidth) + (options.isShowGridlines() ? entry.getValue()*options.getGridLineWidth() : 0) + halfCellWidth + (int)(axisLabelFontHeight*0.25);
		    			//Need to align vertically at the top if the labels are drawn below the matrix
		    			g2d.translate(cellOffsetX, xAxisLabelStartPosY + (options.isxAxisLabelsBelow() ? labelFontMetrics.stringWidth(entry.getKey()) : 0)); 
		    			g2d.rotate(-Math.PI / 2); // Rotate 90 degrees counter-clockwise
		    			
		    			// Draw the x axis label
		    			g2d.drawString(entry.getKey(), 0, 0);
	    	
		    			//Restore the old transform
		    			g2d.setTransform(transform);
	    			} else {
	    				final int labelWidth = labelFontMetrics.stringWidth(entry.getKey());
	    				final int cellOffsetX = xAxisLabelStartPosX - (labelWidth/2) + (entry.getValue() * cellWidth) + (options.isShowGridlines() ? entry.getValue()*options.getGridLineWidth() : 0) + halfCellWidth;
	    				g2d.drawString(entry.getKey(), cellOffsetX, xAxisLabelStartPosY);
	    			}
	    		}
	    	}
	    	
	    	if(options.isShowYAxisLabels()) {
	    		//Add all of the Y labels, drawn horizontally
	    		final int labelVerticalOffset = (int)(axisLabelFontHeight*0.25);
	    		for (Entry<String, Integer> entry : yAxis.getLabelIndices().entrySet()) {
	    			final int labelWidth = labelFontMetrics.stringWidth(entry.getKey());
	    			final int cellOffsetY = yAxisLabelStartPosY + labelVerticalOffset + (entry.getValue() * cellHeight) + (options.isShowGridlines() ? entry.getValue()*options.getGridLineWidth() : 0) + halfCellHeight;
	    			//Aligned right
	    			g2d.drawString(entry.getKey(), yAxisLabelStartPosX + (yAxisLabelMaxWidth - labelWidth), cellOffsetY);
	    		}
	    	}
    		
    		//Determine if we should smoothly blend the colours
    		if(options.isBlendColours()) {
    			final int scaleFactor = options.getBlendColoursScale();
    			
    			//Blending colours means we are going to first render the graph as a tiny image and then scale it up using interpolation.
    			//Render the whole map using a single pixel per cell
    			final int tinyMatrixWidth = xAxis.getCount();
    			final int tinyMatrixHeight = yAxis.getCount();
    			final int bilinearMatrixWidth = tinyMatrixWidth*scaleFactor;
    			final int bilinearMatrixHeight = tinyMatrixHeight*scaleFactor;
    			
    			BufferedImage tinyMatrixImage = new BufferedImage(tinyMatrixWidth, tinyMatrixHeight, BufferedImage.TYPE_INT_ARGB);
    			BufferedImage bilinearMatrixMask = new BufferedImage(bilinearMatrixWidth, bilinearMatrixHeight, BufferedImage.TYPE_INT_ARGB);
    			
    	        Graphics2D g2dTiny = tinyMatrixImage.createGraphics();
    	        Graphics2D g2dBilinearMask = bilinearMatrixMask.createGraphics();
    	        g2dBilinearMask.setColor(Color.BLACK); //Black means fully opaque
    	        
    	        for (DataRecord record: data) {
    	        	if(null == record.getValue()) continue; //No data, perfectly valid.
        			//Determine the colour for this pixel of the map
    				final double val = minClamped || maxClamped ? Math.max(Math.min(record.getValue(), maxValue), minValue) : record.getValue();
    				g2dTiny.setColor(options.getGradient().getColour((valueRange == 0 ? 1.0 : (val-minValue) / valueRange)));
        			g2dTiny.fillRect(xAxis.getIndex(record.getX()), yAxis.getIndex(record.getY()), 1, 1);
        			g2dBilinearMask.fillRect(xAxis.getIndex(record.getX())*scaleFactor, yAxis.getIndex(record.getY())*scaleFactor, scaleFactor, scaleFactor);
    			}
    	        
    	        g2dBilinearMask.dispose();
    	        g2dTiny.dispose();
    	        
    	        //Now we scale the image up using bilinear scaling
    	        //This will linearly interpolate the colours of each pixel
    	        //We use bilinear interpolation because it will only consider a single neighbouring pixel in each direction
    			BufferedImage bilinearScaledImage = new BufferedImage(bilinearMatrixWidth, bilinearMatrixHeight, BufferedImage.TYPE_INT_ARGB);
    	        Graphics2D g2dbilinearScaled = bilinearScaledImage.createGraphics();
    	        g2dbilinearScaled.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    	        g2dbilinearScaled.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
    	        g2dbilinearScaled.drawRenderedImage(tinyMatrixImage,  AffineTransform.getScaleInstance(scaleFactor, scaleFactor));
    	        
    	        //We need to clean up the transparent sections of the image since they have been blended.
    	        //The mask will give us crisp transparent edges after the scaling is applied
    	        g2dbilinearScaled.setComposite(AlphaComposite.DstIn);
    	        g2dbilinearScaled.drawImage(bilinearMatrixMask, 0, 0, null);
    	        g2dbilinearScaled.dispose();
    			
    	        //Scale the tiny image up to the full size using nearest neighbour interpolation
    	        //We use nearest neighbour interpolation because it will keep the edges sharp
    	        BufferedImage scaledImage = new BufferedImage(matrixWidth, matrixHeight, BufferedImage.TYPE_INT_ARGB);
    	        Graphics2D g2dScaled = scaledImage.createGraphics();
    	        g2dScaled.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
    	        g2dScaled.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
    	        g2dScaled.drawRenderedImage(bilinearScaledImage,  AffineTransform.getScaleInstance(((double)matrixWidth)/bilinearMatrixWidth, ((double)matrixHeight)/bilinearMatrixHeight));
    	        g2dScaled.dispose();
    	        
    	        //Draw the scaled image on top of the main canvas
    	        //Note that the grid lines, if present, will be drawn directly on top
    	        g2d.drawImage(scaledImage, matrixStartPosX, matrixStartPosY, null);
    		} else {
    			//Draw the heat map itself, normally. No scaling trickery, this is much simpler.
        		for (DataRecord record: data) {
        			if(null == record.getValue()) continue; //No data, perfectly valid.
        			
        			final int x = xAxis.getIndex(record.getX());
        			final int y = yAxis.getIndex(record.getY());
        			
        			//Determine the colour for this square of the map
    				final double val = minClamped || maxClamped ? Math.max(Math.min(record.getValue(), maxValue), minValue) : record.getValue();
    				g2d.setColor(options.getGradient().getColour((valueRange == 0 ? 1.0 : (val-minValue) / valueRange)));
    				
    				final int matrixCellOffsetX = options.isShowGridlines() ?  x * (cellWidth  + options.getGridLineWidth()) : x * cellWidth;
    				final int matrixCellOffsetY = options.isShowGridlines() ?  y * (cellHeight + options.getGridLineWidth()) : y * cellHeight;
    				
    				final int matrixBoxPosX = matrixStartPosX + (options.isShowGridlines() ? options.getGridLineWidth() : 0) + matrixCellOffsetX;
        			final int matrixBoxPosY = matrixStartPosY + (options.isShowGridlines() ? options.getGridLineWidth() : 0) + matrixCellOffsetY;
    				g2d.fillRect(matrixBoxPosX, matrixBoxPosY, cellWidth, cellHeight);
    			}
    		}
    		
    		//Draw the grid values, if needed
    		if(options.isShowGridValues()) {
    			g2d.setColor(options.getGridValuesFontColour()); //Reset back to grid line colour! The last colour was from the matrix.
    			g2d.setFont(options.getGridValuesFont());
    			FontMetrics valueFontMetrics = g2d.getFontMetrics();
  
    			final int textVerticalOffset = (int) (valueFontMetrics.getHeight()*0.25);
    			int gridValIndex = 0;
    			for (DataRecord record: data) {
    				final String val = dataValues.get(gridValIndex);
    				if(!"".equals(val)) {
    					final int x = xAxis.getIndex(record.getX());
            			final int y = yAxis.getIndex(record.getY());
        				
        				final int matrixCellOffsetX = options.isShowGridlines() ?  x * (cellWidth  + options.getGridLineWidth()) : x * cellWidth;
        				final int matrixCellOffsetY = options.isShowGridlines() ?  y * (cellHeight + options.getGridLineWidth()) : y * cellHeight;
        				
        				final int matrixBoxPosX = matrixStartPosX + (options.isShowGridlines() ? options.getGridLineWidth() : 0) + matrixCellOffsetX;
            			final int matrixBoxPosY = matrixStartPosY + (options.isShowGridlines() ? options.getGridLineWidth() : 0) + matrixCellOffsetY;
            			
            			final int textWidth = valueFontMetrics.stringWidth(val);
            			final int textPosX = matrixBoxPosX + halfCellWidth - (textWidth/2); // Centre the text in the X dimension
            			final int textPosY = matrixBoxPosY + halfCellHeight + textVerticalOffset;
            					
	    				g2d.drawString(val, textPosX, textPosY);
    				}
        			
    				gridValIndex++;
    			}
    		}
    		
    		//Draw the grid lines
			if(options.isShowGridlines()) {
				g2d.setColor(options.getGridLineColour()); //Reset back to grid line colour! The last colour was from the matrix or gid values.
				for(int y = 0; y <= yAxis.getCount(); y++) { //Y grid lines
					final int matrixOffsetY = y * (cellHeight + options.getGridLineWidth());
					g2d.fillRect(matrixStartPosX, matrixStartPosY + matrixOffsetY , matrixWidth, options.getGridLineWidth()); // Top line of each row
				}
				
				for(int x = 0; x <= xAxis.getCount(); x++) { //X grid lines
					final int matrixOffsetX = x * (cellWidth + options.getGridLineWidth());
					g2d.fillRect(matrixStartPosX + matrixOffsetX, matrixStartPosY , options.getGridLineWidth(), matrixHeight); // Top line of each row
				}
			}
			
			//We are done! 🙂
    		return heatmapImage;
        } finally {
        	g2d.dispose();
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

	/**
	 * Returns the X and Y dimensions of the longest string of text, in a collection of strings, when rendered.
	 * 
	 * Note that when rendered, the longest string is not necessarily the ones with the most characters.
	 * 
	 * @param strings A collection of strings to be measured. 
	 * @param font The Font used for rendering the text.
	 * 
	 * @return The X and Y dimensions of the longest string of text, in the form of <code>Entry&lt;x_dimension, y_dimension&gt;</code>
	 */
	protected Entry<Integer, Integer> getMaxStringSize(Collection<String> strings, Font font) {
		if (null == strings || strings.isEmpty()) return new AbstractMap.SimpleEntry<Integer, Integer>(0, 0);
		
		//Create a temporary image to get Graphics2D context for measuring
        BufferedImage tinyImage = new BufferedImage(1, 1, BufferedImage.BITMASK);
        Graphics2D g2d = tinyImage.createGraphics();
        int maxLabelLength = 0;
        int labelHeight = 0;
        try {
        	 g2d.setFont(font);
        	 g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
             FontMetrics fontMetrics = g2d.getFontMetrics();
             
             //Assume it never changes
             labelHeight = fontMetrics.getHeight();
             
     		//Note: the max label size is not necessarily the one with the most characters.
             maxLabelLength = strings.stream().map(s->fontMetrics.stringWidth(s)).max(Integer::compareTo).orElse(0);
        } finally {
        	g2d.dispose();
        }
        return new AbstractMap.SimpleEntry<Integer, Integer>(maxLabelLength, labelHeight);
	}

	/**
	 * Takes a long title and splits it out into multiple lines of text to make it fit within the <code>maxWidth</code> when rendered.
	 * 
	 * @param title The title in String form.
	 * @param maxWidth The maximum width, in pixel, that each line of the title will be when rendered.
	 * @param font The Font used for rendering the text.
	 * 
	 * @return A list of the individual lines of text of the title, with each entry containing the X and Y dimensions of the line,
	 * in pixels. In the form of <code>List&lt;Entry&lt;line_of_text, Entry&lt;x_dimension, y_dimension&gt;&gt;&gt;</code> 
	 */
	protected List<Entry<String, Entry<Integer, Integer>>> getTitleSized(String title, int maxWidth, Font font) {
		//Create a temporary image to get Graphics2D context for measuring
        BufferedImage tinyImage = new BufferedImage(1, 1, BufferedImage.BITMASK);
        Graphics2D g2d = tinyImage.createGraphics();
        List<Entry<String, Entry<Integer, Integer>>> titleLines = new ArrayList<Entry<String, Entry<Integer, Integer>>>();
        try { //TODO: add support for newline characters to allow the user to add line returns to their titles
        	 g2d.setFont(font);
        	 g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
             FontMetrics fontMetrics = g2d.getFontMetrics();
             
             int fontHeight = fontMetrics.getHeight();
             
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
                 lineWidth = fontMetrics.stringWidth(currentLineString);
                 if (lineWidth < maxWidth) continue; //More room is left
                 
                 if (lineWidth == maxWidth) {
                	 titleLines.add(new AbstractMap.SimpleEntry<String, Entry<Integer, Integer>>(currentLineString,  new AbstractMap.SimpleEntry<Integer, Integer>(lineWidth, fontHeight)));
                	 currentLine.setLength(0);
                	 continue;
                 }
                 
                 //We have exceeded our maximum. We need to roll back the string builder.
                 currentLine.setLength(previousCharLen);
                 currentLineString = currentLine.toString();
                 lineWidth = fontMetrics.stringWidth(currentLineString);
                 titleLines.add(new AbstractMap.SimpleEntry<String, Entry<Integer, Integer>>(currentLineString,  new AbstractMap.SimpleEntry<Integer, Integer>(lineWidth, fontHeight)));
                 
                 //Reset the string builder for the next line
                 currentLine.setLength(0);
                 currentLine.append(word);
             }

             // Add the last line
             if (currentLine.length() > 0) {
            	 currentLineString = currentLine.toString();
                 lineWidth = fontMetrics.stringWidth(currentLineString);
                 titleLines.add(new AbstractMap.SimpleEntry<String, Entry<Integer, Integer>>(currentLineString, new AbstractMap.SimpleEntry<Integer, Integer>(lineWidth, fontHeight)));
             }
        } finally {
        	g2d.dispose();
        }
        return titleLines;
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
		if(null == title) title = "";
		this.title = title;
	}

	public HeatMapOptions getOptions() {
		return options;
	}

	public void setOptions(HeatMapOptions options) {
		this.options = options;
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
