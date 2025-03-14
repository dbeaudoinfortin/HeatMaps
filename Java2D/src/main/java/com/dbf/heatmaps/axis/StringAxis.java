package com.dbf.heatmaps.axis;

import java.util.Collection;

public class StringAxis extends Axis<String> {
	
	public static StringAxis instance() {
		return new StringAxis();
	}
	
	public StringAxis() {
		super();
	}
	
	public StringAxis(String title) {
		super(title);
	}
	
	public StringAxis(String title, Collection<String> entries){
		super(title, entries);
	}
	
	public StringAxis(String title, String... entries) {
		super(title);
		addEntries(entries);
	}
	
	public StringAxis addEntries(String... entries) {
		for(String entry : entries) {
			addEntry(entry);
		}
		return this;
	}
	
	public StringAxis addEntry(String entry) {
		return (StringAxis) super.addEntry(entry, entry);
	}
	
	@Override
	public String getLabel(String entry) {
		return entry;
	}

	@Override
	public Integer getIndex(Object entry) {
		return this.entryIndices.get((String) entry);
	}
	
	@Override
	public StringAxis withTitle(String title) {
		return (StringAxis) super.withTitle(title);
	}
	
	@Override
	public StringAxis addEntry(String entry, String label) {
		return (StringAxis) super.addEntry(entry, label);
	}
}
