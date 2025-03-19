package com.dbf.heatmaps;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import com.dbf.heatmaps.axis.IntegerAxis;
import com.dbf.heatmaps.data.BasicDataRecord;
import com.dbf.heatmaps.data.DataRecord;

class EventsVolumeTest extends AbstractHeatMapTest {

	@Test
	void UnitsHeatMapTest() throws IOException {
		File output = getTempFile("units_chart.png");
		System.out.println("Generating a simple test heat map at file " + output.getAbsolutePath());
		
		ArrayList<DataRecord> records = new ArrayList<DataRecord>(units_data.length);
		int month = 10;
		int year = 2018;
		for (int i = 0; i < units_data.length; i++ ) {
			records.add(new BasicDataRecord(month, year, units_data[i]));
			month++;
			if(month == 13) {
				month = 1;
				year++;
			}
		}
		
		HeatMap.builder()
			.withTitle("User Actions")
			.withXAxis(IntegerAxis.instance()
					.withTitle("Month")
					.addEntries(1, 12))
			.withYAxis(IntegerAxis.instance()
					.withTitle("Year")
					.addEntries(2018,2025))
			.withOptions(HeatMapOptions.builder()
					.withCellWidth(60)
					.withCellHeight(60)
					.withShowGridlines(false)
					.withGridLineColour(Color.WHITE)
					.withGradient(HeatMapGradient.EXTENDED_GRADIENT)
					//.withBackgroundColour(Color.BLACK)
					.withAxisTitleFontColour(Color.WHITE)
					.withHeatMapTitleFontColour(Color.WHITE)
					.withAxisLabelFontColour(Color.WHITE)
					.withGridValuesFontColour(Color.WHITE)
					.withShowGridValues(false)
					.withHeatMapTitlePadding(20)
					.withOutsidePadding(10)
					.withShowLegend(true)
					.withShowGridlines(false)
					.withShowXAxisLabels(true)
					.withShowYAxisLabels(true)
					.withLegendTextFormat("0.#")
					.withAxisLabelFont(new Font("Consolas", Font.PLAIN, 18))
					.withGridValuesFont(new Font("Consolas", Font.PLAIN, 12))
					.withAxisTitleFont(new Font("Consolas", Font.PLAIN, 20))
					.withLegendLabelFontColour(Color.WHITE)
					.withColourScaleLowerBound(4000.0)
					.withColourScaleUpperBound(40000.0)
					.build())
		.build()
		.render(output, records);
		System.out.println("Generated a simple test heat map at file " + output.getAbsolutePath());
	}
	
	@Test
	void EventsHeatMapTest() throws IOException {
		File output = getTempFile("events_chart.png");
		System.out.println("Generating a simple test heat map at file " + output.getAbsolutePath());
		
		ArrayList<DataRecord> records = new ArrayList<DataRecord>(events_data.length);
		int month = 7;
		int year = 2018;
		for (int i = 0; i < events_data.length; i++ ) {
			records.add(new BasicDataRecord(month, year, events_data[i]));
			month++;
			if(month == 13) {
				month = 1;
				year++;
			}
		}
		
		HeatMap.builder()
			.withTitle("Events")
			.withXAxis(IntegerAxis.instance()
					.withTitle("Month")
					.addEntries(1, 12))
			.withYAxis(IntegerAxis.instance()
					.withTitle("Year")
					.addEntries(2018,2025))
			.withOptions(HeatMapOptions.builder()
					.withCellWidth(40)
					.withCellHeight(40)
					.withShowGridlines(false)
					.withGridLineColour(Color.WHITE)
					.withGradient(HeatMapGradient.WHITE_HOT_GRADIENT)
					.withBackgroundColour(Color.BLACK)
					.withAxisTitleFontColour(Color.WHITE)
					.withHeatMapTitleFontColour(Color.WHITE)
					.withAxisLabelFontColour(Color.WHITE)
					.withGridValuesFontColour(Color.WHITE)
					.withShowGridValues(false)
					.withHeatMapTitlePadding(20)
					.withOutsidePadding(10)
					.withShowLegend(true)
					.withLegendSteps(7)
					.withShowGridlines(false)
					.withShowXAxisLabels(true)
					.withShowYAxisLabels(true)
					.withLegendTextFormat("0.#")
					.withAxisLabelFont(new Font("Consolas", Font.PLAIN, 18))
					.withGridValuesFont(new Font("Consolas", Font.PLAIN, 12))
					.withAxisTitleFont(new Font("Consolas", Font.PLAIN, 20))
					.withLegendLabelFontColour(Color.WHITE)
					.withColourScaleLowerBound(500000.0)
					.withColourScaleUpperBound(3500000.0)
					.build())
		.build()
		.render(output, records);
		System.out.println("Generated a simple test heat map at file " + output.getAbsolutePath());
	}
	
	private static final double[] units_data  = { 4211, 9867, 6985, 10892, 10546, 12883, 13796, 17770, 19324, 19056, 17402, 16568, 17509, 17075, 15204, 18806, 20352, 29853, 21627, 20423, 20225, 18467, 17480, 21823, 20778, 16109, 14677, 17805, 17491, 21112, 64947, 92119, 44185, 21593, 22422, 25570, 23005, 25367, 17625, 22170, 21344, 24641, 20811, 21769, 21766, 20090, 22061, 22373, 21245, 19230, 14944, 19464, 17788, 19647, 19647, 17612, 18090, 17433, 17413, 15099, 15954, 14114, 10667, 15404, 14685, 11687, 16022, 12927, 17598, 12462, 11801, 12363, 13390, 9888, 7814, 8340, 6282, 1633};
	private static final double[] events_data = {1997269, 2747232, 3232170, 3602597, 2004515, 1473036, 1884537, 1872856, 2678823, 2682874, 2436868, 2617094, 2167073, 2193231, 2937041, 2606866, 2317699, 2041388, 2328496, 2657240, 3127036, 2034227, 2606262, 2275123, 2536332, 2069885, 2352793, 3098405, 1535582, 1538839, 2062838, 1809181, 2073614, 2338034, 1800786, 2020759, 2249132, 2216177, 2545568, 1955447, 1878634, 1545205, 1802925, 1541259, 1922423, 2015646, 1764064, 1784431, 1398362, 1848952, 1685867, 1603531, 1356006, 1244774, 1582257, 1487250, 1829542, 1522358, 1539169, 1314064, 1531905, 1458561, 1033280, 1394609, 1059326, 919520, 1371079, 1318919, 1064012, 1169972, 1365571, 1054313, 993616, 1010055, 1120432, 1107689, 837722, 804991, 856450, 500112};

}
