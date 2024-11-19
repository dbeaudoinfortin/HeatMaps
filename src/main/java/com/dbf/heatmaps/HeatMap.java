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
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import com.dbf.heatmaps.axis.Axis;
import com.dbf.heatmaps.data.DataRecord;
import com.dbf.heatmaps.options.GradientOptions;
import com.dbf.heatmaps.options.RenderOptions;

public class HeatMap {

	/* OPTIONS */
	private GradientOptions gradientOptions;
	private RenderOptions renderOptions;
	
	//TODO: move this to a legend specific options class
	private static final String legendFormat = "0.####";
	
	//TODO: move this to an axis specific options class
	private Axis<?> xAxis;
	private Axis<?> yAxis;
	
	private String title;
	
	//TODO: move all this junk out of here
	private double minValue; //TODO: calculate automatically if not provided
	private double maxValue;
	private boolean minClamped;
	private boolean maxClamped;
	
	public void render(File dataFile, List<DataRecord> records) throws IOException{		
		//Render all of the X & Y labels first so we can determine the maximum size
		final Entry<Integer, Integer> xAxisLabelMaxSize = getMaxStringSize(xAxis.getEntryLabels().values(), getRenderOptions().getBasicFont());
		final int yAxisLabelMaxWidth  = getMaxStringSize(yAxis.getEntryLabels().values(), getRenderOptions().getBasicFont()).getKey();
		int xAxisLabelHeight = xAxisLabelMaxSize.getKey(); //Assume rotated by default
		
		//Since the font height is the same for all basic text, we can use the axis labels
		final int basicFontHeight = xAxisLabelMaxSize.getValue();
		
		//The cells need to be at least as big as the font height
		//This is true for the x-axis as  well since at a minimum we can rotate the text
		final int cellWidth  = Math.max(getRenderOptions().getCellWidth(), basicFontHeight + getRenderOptions().getLabelPadding());
		final int cellHeight = Math.max(getRenderOptions().getCellHeight(), basicFontHeight + getRenderOptions().getLabelPadding());
		
		//Save a little bit of math later
		final int halfCellWidth  = cellWidth  / 2;
		final int halfCellHeight = cellHeight / 2;
		
		//Only rotate the x-axis labels when they are too big
		final boolean rotateXLabels = (xAxisLabelHeight ) > (cellWidth + (getRenderOptions().isShowGridlines() ? getRenderOptions().getGridWidth() : 0) - getRenderOptions().getLabelPadding());
		if(!rotateXLabels) {
			xAxisLabelHeight = xAxisLabelMaxSize.getValue();
		}
		
		//Calculate the legend values
		final int    legendBoxes = yAxis.getCount() > 5 ? yAxis.getCount() : 5; //Must be at least 5
		final double valueRange  = maxValue - minValue;
		final double legendSteps = valueRange > 0 ? valueRange / (legendBoxes-1) : 0;
		final List<Double> legendvalues = new ArrayList<Double>(legendBoxes);
		legendvalues.add(minValue);
			for(int i = 1; i < legendBoxes -1; i++) {
				legendvalues.add(minValue + (i*legendSteps));
			}
		legendvalues.add(maxValue);
		
		//Calculate the legend labels
		final DecimalFormat legendDF = new DecimalFormat(legendFormat); //Not thread safe, don't make static
		final List<String> legendLabels = legendvalues.stream().map(v->legendDF.format(v)).collect(Collectors.toList());
		//We need to indicate in the legend if the values are being capped/bounded/clamped
		if(minClamped) legendLabels.set(0, "<= " + legendLabels.get(0));
		if(maxClamped) legendLabels.set(legendvalues.size()-1, ">= " + legendLabels.get(legendvalues.size()-1));
		
		//Calculate legend sizes
		final int legendHeight = (cellHeight * legendBoxes) + (getRenderOptions().isShowGridlines() ? (legendBoxes + 1) * getRenderOptions().getGridWidth() : 0) ;
		final int legendLabelMaxWidth = getMaxStringSize(legendLabels, getRenderOptions().getSmallTitleFont()).getKey();
		final int legendBoxesWidth = cellWidth + (getRenderOptions().isShowGridlines() ? 2 * getRenderOptions().getGridWidth() : 0);
		final int legendWidth = legendBoxesWidth + getRenderOptions().getLabelPadding() + legendLabelMaxWidth;
		
		//Calculate the X positional values, first
		final int yAxisTitleStartPosX = getRenderOptions().getOutsidePadding() + basicFontHeight;
		final int yAxisLabelStartPosX = yAxisTitleStartPosX + (getRenderOptions().getLabelPadding()*2);
		final int matrixStartPosX = yAxisLabelStartPosX + yAxisLabelMaxWidth + getRenderOptions().getLabelPadding();
		final int matrixWidth = (xAxis.getCount()  * cellWidth) + (getRenderOptions().isShowGridlines() ? (xAxis.getCount() + 1) * getRenderOptions().getGridWidth() : 0);
		final int matrixCentreX =  matrixStartPosX + (matrixWidth/2);
		final int xAxisLabelStartPosX = matrixStartPosX + (getRenderOptions().isShowGridlines() ? getRenderOptions().getGridWidth() : 0);
		final int legendStartPosX = matrixStartPosX + matrixWidth + getRenderOptions().getLegendPadding();
		final int legendLabelStartPosX = legendStartPosX + legendBoxesWidth + getRenderOptions().getLabelPadding();
		
		//Calculate the overall image width
		//Outside padding + Y Axis Title + label padding + Y Axis Labels + label padding + chart width + legend padding + legend width + outside padding
        final int imageWidth   = legendStartPosX + legendWidth + getRenderOptions().getOutsidePadding();
        final int imageCenterY = imageWidth/2;
        
        //Now that we know the image width we can figure out if we need to wrap the text of the big chart title
        final int chartTitleMaxWidth = imageWidth - (getRenderOptions().getOutsidePadding()*2);
        List<Entry<String, Entry<Integer, Integer>>> titleLines = null;
        if(!title.isEmpty()) titleLines = getTitleSized(title, chartTitleMaxWidth); //Empty chart titles are supported
        final int chartTitleLineHeight = title.isEmpty() ? 0 : titleLines.get(0).getValue().getValue();
        final int chartTitleHeight = title.isEmpty() ? 0 : titleLines.size() * chartTitleLineHeight;
        
		//Now that we know the chart title height, we can calculate the Y positional values
        final int chartTitleStartPosY = getRenderOptions().getOutsidePadding();
		final int xAxisTitleStartPosY = chartTitleStartPosY + (!title.isEmpty() ? chartTitleHeight + getRenderOptions().getChartTitlePadding() : 0) + basicFontHeight; //Label positions are bottom left!!
		final int xAxisLabelStartPosY = xAxisTitleStartPosY + (getRenderOptions().getLabelPadding()*2) + xAxisLabelHeight;
		final int matrixStartPosY = xAxisLabelStartPosY + getRenderOptions().getLabelPadding();
		final int matrixHeight = (yAxis.getCount()  * cellHeight) + (getRenderOptions().isShowGridlines() ? (yAxis.getCount() + 1) * getRenderOptions().getGridWidth() : 0);
		final int matrixCentreY =  matrixStartPosY + (matrixHeight/2);
		final int yAxisLabelStartPosY = matrixStartPosY + (getRenderOptions().isShowGridlines() ? getRenderOptions().getGridWidth() : 0);
		final int legendStartPosY = (matrixHeight>=legendHeight) ? (matrixCentreY - (legendHeight/2)) : matrixStartPosY; //Legend is centred with the Matrix only if the matrix is big enough
		final int legendLabelStartPosY = legendStartPosY + basicFontHeight + (basicFontHeight/4) + (getRenderOptions().isShowGridlines() ? getRenderOptions().getGridWidth() : 0); //Label positions are bottom left!!
		
        //Finally, we can figure out the overall image height
        //Outside padding + big title + title padding + X Axis Title + label padding + X Axis Labels + label padding + chart height + outside padding
        final int imageHeight = matrixStartPosY + Math.max(matrixHeight, legendHeight) + getRenderOptions().getOutsidePadding();
        
        BufferedImage heatmapImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = heatmapImage.createGraphics();
        
        //Start the actual drawing onto the canvas
		 try {
			//Make the background all white, except if the colour scale goes to white
			Color maxColour = getGradientOptions().getGradient().getColour(1.0);
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
				g2d.setFont(getRenderOptions().getBigTitleFont());
				g2d.setColor(Color.BLACK);
				for (int i = 0; i < titleLines.size(); i++) {
					Entry<String, Entry<Integer, Integer>> line = titleLines.get(i);
					// Label positions are bottom left so we need to add 1 to the line number
					final int linePosY = chartTitleStartPosY + ((i + 1) * chartTitleLineHeight);
					//Centre each line horizontally
					g2d.drawString(line.getKey(), imageCenterY - (line.getValue().getKey()/2), linePosY);
				}
			}
			
			//Reset to a decent basic font
	        g2d.setFont(getRenderOptions().getBasicFont());
	        g2d.setColor(Color.BLACK);
	
    		//Render the legend labels
    		//The number of legend boxes may be greater than the number of labels
	   		g2d.drawString(legendLabels.get(legendLabels.size()-1), legendLabelStartPosX, legendLabelStartPosY); //First
    		g2d.drawString(legendLabels.get(0), legendLabelStartPosX, legendLabelStartPosY + (cellHeight * (legendBoxes-1)) + (getRenderOptions().isShowGridlines() ? getRenderOptions().getGridWidth()*(legendBoxes-1) : 0)); //Last
    		if(valueRange > 0 ) {
    			//Only render the rest of the labels if there is a range to the colours
    			for(int i = 1; i < legendBoxes-1; i++) {
    				final int legendLabelPosY = legendLabelStartPosY + (cellHeight * i) + (getRenderOptions().isShowGridlines() ? getRenderOptions().getGridWidth()*i : 0);
    				g2d.drawString(legendLabels.get(legendBoxes-i-1), legendLabelStartPosX, legendLabelPosY);
    			}
    		}
    		
    		//Render the legend boxes, starting with the top (maximum colour value) first
    		for(int i = 0; i < legendBoxes; i++) {
    			if(i == 0) {
    				g2d.setColor(getGradientOptions().getGradient().getColour(1.0));
    			} else if (i == legendBoxes -1) {
    				g2d.setColor(getGradientOptions().getGradient().getColour(0.0));
    			} else {
    				g2d.setColor(getGradientOptions().getGradient().getColour((1-(legendvalues.get(i)-minValue)/valueRange)));
    			}
    			final int legendBoxPosX = legendStartPosX + (getRenderOptions().isShowGridlines() ? getRenderOptions().getGridWidth() : 0);
    			final int legendBoxPosY = legendStartPosY + (getRenderOptions().isShowGridlines() ? (getRenderOptions().getGridWidth() + i * (cellHeight + getRenderOptions().getGridWidth())) : i * cellHeight);
				g2d.fillRect(legendBoxPosX, legendBoxPosY, cellWidth, cellHeight);
				
				//Also render the dividing lines
				//The last box doesn't need a line, that's handled by the outside border
				if(getRenderOptions().isShowGridlines() && (i != legendBoxes -1)) {
					g2d.setColor(Color.BLACK); //Reset back to black! The last colour was from the legend
					g2d.fillRect(legendBoxPosX, legendBoxPosY + cellHeight, cellWidth, getRenderOptions().getGridWidth()); 
				}
    		}
    		
    		//Render the legend grid lines or outside border
    		g2d.setColor(Color.BLACK);
    		if(getRenderOptions().isShowGridlines()) {
    			g2d.fillRect(legendStartPosX, legendStartPosY, legendBoxesWidth, getRenderOptions().getGridWidth()); //Top
    			g2d.fillRect(legendStartPosX, legendStartPosY + legendHeight - getRenderOptions().getGridWidth(), legendBoxesWidth, getRenderOptions().getGridWidth()); //Bottom
    			g2d.fillRect(legendStartPosX, legendStartPosY, getRenderOptions().getGridWidth(), legendHeight); //Left
    			g2d.fillRect(legendStartPosX + legendBoxesWidth - getRenderOptions().getGridWidth(), legendStartPosY, getRenderOptions().getGridWidth(), legendHeight); //Right
    		} else {
    			//Render the legend border on top of the boxes
        		g2d.drawRect(legendStartPosX, legendStartPosY, cellWidth-1, (cellHeight*legendBoxes) -1);
    		}
    		
    		//Will will need to determine the width of each title individually using fontMetrics
    		g2d.setFont(getRenderOptions().getSmallTitleFont());
    		g2d.setColor(Color.BLACK); //Reset back to black! The last colour was from the legend
	    	FontMetrics titleFontMetrics = g2d.getFontMetrics();
    		
    		//Render the X-axis title
    		//TODO: Wrap the title if it's too long
    		final int xAxisTitleWidth = titleFontMetrics.stringWidth(xAxis.getTitle());
    		g2d.drawString(xAxis.getTitle(), matrixCentreX - (xAxisTitleWidth/2), xAxisTitleStartPosY);
    		
    		//Render the Y-axis title
    		//TODO: Wrap the title if it's too long
    		final int yAxisTitleWidth = titleFontMetrics.stringWidth(yAxis.getTitle());
    		AffineTransform transform = g2d.getTransform();
    		g2d.translate(yAxisTitleStartPosX, matrixCentreY + (yAxisTitleWidth/2));
			g2d.rotate(-Math.PI / 2); // Rotate 90 degrees counter-clockwise
    		g2d.drawString(yAxis.getTitle(), 0, 0);
    		g2d.setTransform(transform);
    		
    		//Will will need to determine the width of each label individually using fontMetrics
    		g2d.setFont(getRenderOptions().getBasicFont());
	    	FontMetrics labelFontMetrics = g2d.getFontMetrics(); //Font is different between titles and labels
	    	
    		//Add all of the x labels, drawn vertically or horizontally
    		for (Entry<String, Integer> entry : xAxis.getLabelIndices().entrySet()) {
    			if(rotateXLabels) {
	    			//Store the current transform
	    			transform = g2d.getTransform();
	    			final int cellOffsetX = xAxisLabelStartPosX + (entry.getValue() * cellWidth) + (getRenderOptions().isShowGridlines() ? entry.getValue()*getRenderOptions().getGridWidth() : 0) + halfCellWidth + (basicFontHeight/4);
	    			g2d.translate(cellOffsetX, xAxisLabelStartPosY);
	    			g2d.rotate(-Math.PI / 2); // Rotate 90 degrees counter-clockwise
	    			
	    			// Draw the x axis label
	    			g2d.drawString(entry.getKey(), 0, 0);
    	
	    			//Restore the old transform
	    			g2d.setTransform(transform);
    			} else {
    				final int labelWidth = labelFontMetrics.stringWidth(entry.getKey());
    				final int cellOffsetX = xAxisLabelStartPosX - (labelWidth/2) + (entry.getValue() * cellWidth) + (getRenderOptions().isShowGridlines() ? entry.getValue()*getRenderOptions().getGridWidth() : 0) + halfCellWidth;
    				g2d.drawString(entry.getKey(), cellOffsetX, xAxisLabelStartPosY);
    			}
    		}
    		
    		//Add all of the y labels, drawn horizontally
    		for (Entry<String, Integer> entry : yAxis.getLabelIndices().entrySet()) {
    			final int labelWidth = labelFontMetrics.stringWidth(entry.getKey());
    			final int cellOffsetY = yAxisLabelStartPosY + (basicFontHeight/4) + (entry.getValue() * cellHeight) + (getRenderOptions().isShowGridlines() ? entry.getValue()*getRenderOptions().getGridWidth() : 0) + halfCellHeight;
    			//Aligned right
    			g2d.drawString(entry.getKey(), yAxisLabelStartPosX + (yAxisLabelMaxWidth - labelWidth), cellOffsetY);
    		}
    		
			//Draw the heat map itself
    		for (DataRecord record: records) {
    			final int x = xAxis.getIndex(record.getX());
    			final int y = yAxis.getIndex(record.getY());
    			
    			//Determine the colour for this square of the map
				final double val = minClamped || maxClamped ? Math.max(Math.min(record.getValue().doubleValue(), maxValue), minValue) : record.getValue().doubleValue();
				g2d.setColor(getGradientOptions().getGradient().getColour((valueRange == 0 ? 1.0 : (val-minValue) / valueRange)));
				
				final int matrixCellOffsetX = getRenderOptions().isShowGridlines() ?  x * (cellWidth  + getRenderOptions().getGridWidth()) : x * cellWidth;
				final int matrixCellOffsetY = getRenderOptions().isShowGridlines() ?  y * (cellHeight + getRenderOptions().getGridWidth()) : y * cellHeight;
				
				final int matrixBoxPosX = matrixStartPosX + (getRenderOptions().isShowGridlines() ? getRenderOptions().getGridWidth() : 0) + matrixCellOffsetX;
    			final int matrixBoxPosY = matrixStartPosY + (getRenderOptions().isShowGridlines() ? getRenderOptions().getGridWidth() : 0) + matrixCellOffsetY;
				g2d.fillRect(matrixBoxPosX, matrixBoxPosY, cellWidth, cellHeight);
			}
    		
    		//Draw the grid lines
			if(getRenderOptions().isShowGridlines()) {
				g2d.setColor(Color.BLACK); //Reset back to black! The last colour was from the matrix
				for(int y = 0; y <= yAxis.getCount(); y++) {
					final int matrixOffsetY = y * (cellHeight + getRenderOptions().getGridWidth());
					g2d.fillRect(matrixStartPosX, matrixStartPosY + matrixOffsetY , matrixWidth, getRenderOptions().getGridWidth()); // Top line of each row
				}
				
				for(int x = 0; x <= xAxis.getCount(); x++) {
					final int matrixOffsetX = x * (cellWidth + getRenderOptions().getGridWidth());
					g2d.fillRect(matrixStartPosX + matrixOffsetX, matrixStartPosY , getRenderOptions().getGridWidth(), matrixHeight); // Top line of each row
				}
			}
    		
    		ImageIO.write(heatmapImage, "png", dataFile);
        } finally {
        	g2d.dispose();
        }
	}
	
	private static Entry<Integer, Integer> getMaxStringSize(Collection<String> strings, Font font) {
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
             maxLabelLength = getLongestStringLength(strings, fontMetrics );
        } finally {
        	g2d.dispose();
        }
        return new AbstractMap.SimpleEntry<Integer, Integer>(maxLabelLength, labelHeight);
	}
	
	private static int getLongestStringLength(Collection<String> strings, FontMetrics fontMetrics) {
		return strings.stream().map(s->fontMetrics.stringWidth(s)).max(Integer::compareTo).orElse(0);
	}
	
	private List<Entry<String, Entry<Integer, Integer>>> getTitleSized(String title, int maxWidth) {
		//Create a temporary image to get Graphics2D context for measuring
        BufferedImage tinyImage = new BufferedImage(1, 1, BufferedImage.BITMASK);
        Graphics2D g2d = tinyImage.createGraphics();
        List<Entry<String, Entry<Integer, Integer>>> titleLines = new ArrayList<Entry<String, Entry<Integer, Integer>>>();
        try {
        	 g2d.setFont(getRenderOptions().getBigTitleFont());
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
	
	public GradientOptions getGradientOptions() {
		return gradientOptions;
	}
	public void setGradientOptions(GradientOptions gradientOptions) {
		this.gradientOptions = gradientOptions;
	}
	public RenderOptions getRenderOptions() {
		return renderOptions;
	}
	public void setRenderOptions(RenderOptions renderOptions) {
		this.renderOptions = renderOptions;
	}
}
