package com.dbf.heatmap;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import com.dbf.heatmaps.HeatMap;
import com.dbf.heatmaps.HeatMapGradient;
import com.dbf.heatmaps.HeatMapOptions;
import com.dbf.heatmaps.axis.IntegerAxis;
import com.dbf.heatmaps.data.BasicDataRecord;
import com.dbf.heatmaps.data.DataRecord;

class SimpleChartTest extends AbstractHeatMapTest {

	@Test
	void SimpleChartHeatMapTest() throws IOException {
		File output = getTempFile("simple_chart.png");
		System.out.println("Generating the simple chart test heat map at file " + output.getAbsolutePath());
		
		ArrayList<DataRecord> records = new ArrayList<DataRecord>(values.length);
		int arrayIndex = 0;
		for (int x = 1; x <= 11; x++ ) {
			records.add(new BasicDataRecord(x,1, values[arrayIndex]));
			arrayIndex++;
		}
		
		HeatMap.builder()
			.withTitle("")
			.withXAxis(IntegerAxis.instance()
					.withTitle("")
					.addEntries(1, 11))
			.withYAxis(IntegerAxis.instance()
					.withTitle("")
					.addEntries(1,1))
			.withOptions(HeatMapOptions.builder()
					.withCellWidth(100)
					.withCellHeight(50)
					.withShowGridlines(false)
					.withGridLineColour(Color.WHITE)
					.withGradient(HeatMapGradient.EXTENDED_GRADIENT)
					.withBackgroundColour(Color.BLACK)
					.withAxisTitleFontColour(Color.WHITE)
					.withHeatMapTitleFontColour(Color.WHITE)
					.withAxisLabelFontColour(Color.WHITE)
					.withGridValuesFontColour(Color.WHITE)
					.withShowGridValues(false)
					.withHeatMapTitlePadding(0)
					.withOutsidePadding(0)
					.withShowLegend(false)
					.withShowGridlines(false)
					.withShowXAxisLabels(false)
					.withShowYAxisLabels(false)
					.withLegendTextFormat("0.#")
					.withAxisLabelFont(new Font("Consolas", Font.PLAIN, 26))
					.withGridValuesFont(new Font("Consolas", Font.PLAIN, 12))
					.withAxisTitleFont(new Font("Consolas", Font.PLAIN, 30))
					.withLegendLabelFontColour(Color.WHITE)
					.withColourScaleLowerBound(0.0)
					.withColourScaleUpperBound(11.0)
					.build())
		.build()
		.render(output, records);
		System.out.println("Generated the simple chart test heat map at file " + output.getAbsolutePath());
	}

	private static final double[] values = { 1,2,3,4,5,6,7,8,9,10,11 };

}
