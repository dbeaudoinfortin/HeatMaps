package com.dbf.heatmaps.axis;

import java.util.Collection;

public class StringAxis extends Axis<String> {
	
	public StringAxis() {
		super();
	}
	
	public StringAxis(String title) {
		super(title);
	}
	
	public StringAxis(String title, Collection<String> entries){
		super(title, entries);
	}
	
	public StringAxis(String title, String... strings) {
		super(title, strings.length);
		for(int i = 0; i < strings.length; i++ ) {
			entryLabels.put(strings[i], strings[i]);
			entryIndices.put(strings[i], i);
			labelIndices.put(strings[i], i);
		}
	}
	
	public void addEntry(String entry) {
		super.addEntry(entry, entry);
	}
	
	@Override
	public String getLabel(String entry) {
		return entry;
	}

	@Override
	public Integer getIndex(Object entry) {
		return this.entryIndices.get((String) entry);
	}

}
