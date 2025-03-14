package com.dbf.heatmap;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class AbstractHeatMapTest {

	protected static final File getTempFile(String name) throws IOException {
		Path directory = Files.createTempDirectory("heatmap_tests");
		File outputFile = directory.resolve(name).toFile();
		outputFile.deleteOnExit();
		return outputFile;
	}
}
