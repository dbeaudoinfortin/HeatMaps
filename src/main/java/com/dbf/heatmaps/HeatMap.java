package com.dbf.heatmaps;

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

	public void render(File file, Collection<DataRecord> data) throws IOException {
		if(null == file) throw new IllegalArgumentException("Invalid file.");
		
		BufferedImage heatmapImage = render(data);
		ImageIO.write(heatmapImage, "png", file);
	}
	
	public BufferedImage render(Collection<DataRecord> data) {
		//Basic sanity checks
		if(null == data || data.isEmpty()) throw new IllegalArgumentException("Missing data.");
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
		
		//Render all of the X & Y labels first so we can determine the maximum size
		final Entry<Integer, Integer> xAxisLabelMaxSize = getMaxStringSize(xAxis.getEntryLabels().values(), options.getBasicFont());
		final int yAxisLabelMaxWidth  = getMaxStringSize(yAxis.getEntryLabels().values(), options.getBasicFont()).getKey();
		int xAxisLabelHeight = xAxisLabelMaxSize.getKey(); //Assume rotated by default
		
		//Since the font height is the same for all basic text, we can use the axis labels
		final int basicFontHeight = xAxisLabelMaxSize.getValue();
		
		//Determine the dimensions of the axis titles
		final Entry<Integer, Integer> xTitleDimensions = getMaxStringSize(Collections.singletonList(xAxis.getTitle()), options.getAxisTitleFont());
		final Entry<Integer, Integer> yTitleDimensions = getMaxStringSize(Collections.singletonList(yAxis.getTitle()), options.getAxisTitleFont());
		
		//The cells need to be at least as big as the font height
		//This is true for the x-axis as  well since at a minimum we can rotate the text
		final int cellWidth  = Math.max(options.getCellWidth(), basicFontHeight + options.getLabelPadding());
		final int cellHeight = Math.max(options.getCellHeight(), basicFontHeight + options.getLabelPadding());
		
		//Save a little bit of math later
		final int halfCellWidth  = cellWidth / 2;
		final int halfCellHeight = cellHeight / 2;
		
		//Only rotate the x-axis labels when they are too big
		final boolean rotateXLabels = (xAxisLabelHeight ) > (cellWidth + (options.isShowGridlines() ? options.getGridLineWidth() : 0) - options.getLabelPadding());
		if(!rotateXLabels) {
			xAxisLabelHeight = xAxisLabelMaxSize.getValue();
		}
		
		//Calculate the legend values
		final int legendBoxes = options.isShowLegend() ? (yAxis.getCount() > 5 ? yAxis.getCount() : 5) : 0; //If present, must be at least 5
		final double valueRange = maxValue - minValue;
		final double legendSteps = options.isShowLegend()  ? (valueRange > 0 ? valueRange / (legendBoxes-1) : 0) : 0;
		final List<Double> legendvalues = new ArrayList<Double>(legendBoxes);
		if(options.isShowLegend()) {
			legendvalues.add(minValue);
				for(int i = 1; i < legendBoxes -1; i++) {
					legendvalues.add(minValue + (i*legendSteps));
				}
			legendvalues.add(maxValue);
		}
		
		//Calculate the legend labels
		final DecimalFormat legendDF = new DecimalFormat(options.getLegendTextFormat()); //Not thread safe, don't make static
		final List<String> legendLabels = legendvalues.stream().map(v->legendDF.format(v)).collect(Collectors.toList());
		
		if(options.isShowLegend()) {
			//We need to indicate in the legend if the values are being capped/bounded/clamped
			if(minClamped) legendLabels.set(0, "<= " + legendLabels.get(0));
			if(maxClamped) legendLabels.set(legendvalues.size()-1, ">= " + legendLabels.get(legendvalues.size()-1));
		}
		
		//Calculate legend sizes
		final int legendHeight = options.isShowLegend() ? ((cellHeight * legendBoxes) + (options.isShowGridlines() ? (legendBoxes + 1) * options.getGridLineWidth() : 0)) : 0 ;
		final int legendLabelMaxWidth = getMaxStringSize(legendLabels, options.getAxisTitleFont()).getKey();
		final int legendBoxesWidth = options.isShowLegend() ? (cellWidth + (options.isShowGridlines() ? 2 * options.getGridLineWidth() : 0)) : 0;
		final int legendWidth = options.isShowLegend() ? (legendBoxesWidth + options.getLabelPadding() + legendLabelMaxWidth) : 0;
		
		//Calculate the X positional values of all of the elements first
		final int yAxisTitleStartPosX = options.getOutsidePadding() + (!yAxis.getTitle().isEmpty() ? yTitleDimensions.getValue() : 0);
		final int yAxisLabelStartPosX = yAxisTitleStartPosX + (!yAxis.getTitle().isEmpty() ? options.getLabelPadding()*2 : 0);
		final int matrixStartPosX = yAxisLabelStartPosX + yAxisLabelMaxWidth + options.getLabelPadding();
		final int matrixWidth = (xAxis.getCount()  * cellWidth) + (options.isShowGridlines() ? (xAxis.getCount() + 1) * options.getGridLineWidth() : 0);
		final int matrixCentreX =  matrixStartPosX + (matrixWidth/2);
		final int xAxisLabelStartPosX = matrixStartPosX + (options.isShowGridlines() ? options.getGridLineWidth() : 0);
		final int legendStartPosX = matrixStartPosX + matrixWidth + (options.isShowLegend() ? options.getLegendPadding() : 0);
		final int legendLabelStartPosX = legendStartPosX + (options.isShowLegend() ? legendBoxesWidth +  options.getLabelPadding() : 0);
		
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
		final int xAxisTitleStartPosY = chartTitleStartPosY + (!title.isEmpty() ? chartTitleHeight + options.getHeatMapTitlePadding() : 0) + (!xAxis.getTitle().isEmpty() ? xTitleDimensions.getValue() : 0); //Label positions are bottom left!!
		final int xAxisLabelStartPosY = xAxisTitleStartPosY + (!xAxis.getTitle().isEmpty() ? options.getLabelPadding()*2 : 0) + xAxisLabelHeight;
		final int matrixStartPosY = xAxisLabelStartPosY + options.getLabelPadding();
		final int matrixHeight = (yAxis.getCount()  * cellHeight) + (options.isShowGridlines() ? (yAxis.getCount() + 1) * options.getGridLineWidth() : 0);
		final int matrixCentreY =  matrixStartPosY + (matrixHeight/2);
		final int yAxisLabelStartPosY = matrixStartPosY + (options.isShowGridlines() ? options.getGridLineWidth() : 0);
		final int legendStartPosY = (matrixHeight>=legendHeight) ? (matrixCentreY - (legendHeight/2)) : matrixStartPosY; //Legend is centred with the Matrix only if the matrix is big enough
		final int legendLabelStartPosY = legendStartPosY + basicFontHeight + (basicFontHeight/4) + (options.isShowGridlines() ? options.getGridLineWidth() : 0); //Label positions are bottom left!!
		
        //Finally, we can figure out the overall image height
        //Outside padding + big title + title padding + X Axis Title + label padding + X Axis Labels + label padding + chart height + outside padding
        final int imageHeight = matrixStartPosY + Math.max(matrixHeight, legendHeight) + options.getOutsidePadding();
        
        BufferedImage heatmapImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = heatmapImage.createGraphics();
        
        //Start the actual drawing onto the canvas
		 try {
			//Make the background all white, except if the colour scale goes to white
			Color maxColour = options.getGradient().getColour(1.0);
			if(maxColour.getBlue() > 240 && maxColour.getGreen() > 240 && maxColour.getRed() > 240) {
				g2d.setColor(new Color(210, 210, 210));
			} else {
				g2d.setColor(Color.WHITE);
			}
			g2d.fillRect(0, 0, imageWidth, imageHeight);
				
			//Render the text smoothly
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			
			//Render the chart title
			if(!title.isEmpty()) {
				//Set the title font
				g2d.setFont(options.getHeatMapTitleFont());
				g2d.setColor(Color.BLACK);
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
		        g2d.setFont(options.getBasicFont());
		        g2d.setColor(Color.BLACK);
		        
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
	    				g2d.setColor(options.getGradient().getColour((1-(legendvalues.get(i)-minValue)/valueRange)));
	    			}
	    			final int legendBoxPosX = legendStartPosX + (options.isShowGridlines() ? options.getGridLineWidth() : 0);
	    			final int legendBoxPosY = legendStartPosY + (options.isShowGridlines() ? (options.getGridLineWidth() + i * (cellHeight + options.getGridLineWidth())) : i * cellHeight);
					g2d.fillRect(legendBoxPosX, legendBoxPosY, cellWidth, cellHeight);
					
					//Also render the dividing lines
					//The last box doesn't need a line, that's handled by the outside border
					if(options.isShowGridlines() && (i != legendBoxes -1)) {
						g2d.setColor(Color.BLACK); //Reset back to black! The last colour was from the legend
						g2d.fillRect(legendBoxPosX, legendBoxPosY + cellHeight, cellWidth, options.getGridLineWidth()); 
					}
	    		}
	    		
	    		//Render the legend grid lines or outside border
	    		g2d.setColor(Color.BLACK);
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
    		g2d.setColor(Color.BLACK); //Reset back to black! The last colour was from the legend
    		
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
    		g2d.setFont(options.getBasicFont());
	    	FontMetrics labelFontMetrics = g2d.getFontMetrics(); //Font is different between titles and labels
	    	
    		//Add all of the x labels, drawn vertically or horizontally
	    	AffineTransform transform;
    		for (Entry<String, Integer> entry : xAxis.getLabelIndices().entrySet()) {
    			if(rotateXLabels) {
	    			//Store the current transform
	    			transform = g2d.getTransform();
	    			final int cellOffsetX = xAxisLabelStartPosX + (entry.getValue() * cellWidth) + (options.isShowGridlines() ? entry.getValue()*options.getGridLineWidth() : 0) + halfCellWidth + (basicFontHeight/4);
	    			g2d.translate(cellOffsetX, xAxisLabelStartPosY);
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
    		
    		//Add all of the y labels, drawn horizontally
    		for (Entry<String, Integer> entry : yAxis.getLabelIndices().entrySet()) {
    			final int labelWidth = labelFontMetrics.stringWidth(entry.getKey());
    			final int cellOffsetY = yAxisLabelStartPosY + (basicFontHeight/4) + (entry.getValue() * cellHeight) + (options.isShowGridlines() ? entry.getValue()*options.getGridLineWidth() : 0) + halfCellHeight;
    			//Aligned right
    			g2d.drawString(entry.getKey(), yAxisLabelStartPosX + (yAxisLabelMaxWidth - labelWidth), cellOffsetY);
    		}
    		
			//Draw the heat map itself
    		for (DataRecord record: data) {
    			final int x = xAxis.getIndex(record.getX());
    			final int y = yAxis.getIndex(record.getY());
    			
    			//Determine the colour for this square of the map
				final double val = minClamped || maxClamped ? Math.max(Math.min(record.getValue().doubleValue(), maxValue), minValue) : record.getValue().doubleValue();
				g2d.setColor(options.getGradient().getColour((valueRange == 0 ? 1.0 : (val-minValue) / valueRange)));
				
				final int matrixCellOffsetX = options.isShowGridlines() ?  x * (cellWidth  + options.getGridLineWidth()) : x * cellWidth;
				final int matrixCellOffsetY = options.isShowGridlines() ?  y * (cellHeight + options.getGridLineWidth()) : y * cellHeight;
				
				final int matrixBoxPosX = matrixStartPosX + (options.isShowGridlines() ? options.getGridLineWidth() : 0) + matrixCellOffsetX;
    			final int matrixBoxPosY = matrixStartPosY + (options.isShowGridlines() ? options.getGridLineWidth() : 0) + matrixCellOffsetY;
				g2d.fillRect(matrixBoxPosX, matrixBoxPosY, cellWidth, cellHeight);
			}
    		
    		//Draw the grid lines
			if(options.isShowGridlines()) {
				g2d.setColor(Color.BLACK); //Reset back to black! The last colour was from the matrix
				for(int y = 0; y <= yAxis.getCount(); y++) {
					final int matrixOffsetY = y * (cellHeight + options.getGridLineWidth());
					g2d.fillRect(matrixStartPosX, matrixStartPosY + matrixOffsetY , matrixWidth, options.getGridLineWidth()); // Top line of each row
				}
				
				for(int x = 0; x <= xAxis.getCount(); x++) {
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
	 * @return The X & Y dimensions of the longest string of text, in the form of <code>Entry<x_dimension, y_dimension></code>
	 */
	private static Entry<Integer, Integer> getMaxStringSize(Collection<String> strings, Font font) {
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
	 * @return A list of the individual lines of text of the title, with each entry containing the X & Y dimensions of the line,
	 * in pixels. In the form of <code>List<Entry<line_of_text, Entry<x_dimension, y_dimension>>></code> 
	 */
	private List<Entry<String, Entry<Integer, Integer>>> getTitleSized(String title, int maxWidth, Font font) {
		//Create a temporary image to get Graphics2D context for measuring
        BufferedImage tinyImage = new BufferedImage(1, 1, BufferedImage.BITMASK);
        Graphics2D g2d = tinyImage.createGraphics();
        List<Entry<String, Entry<Integer, Integer>>> titleLines = new ArrayList<Entry<String, Entry<Integer, Integer>>>();
        try {
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
