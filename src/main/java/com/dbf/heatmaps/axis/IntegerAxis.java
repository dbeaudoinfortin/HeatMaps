package com.dbf.heatmaps.axis;

import java.util.Collection;

public class IntegerAxis extends Axis<Integer> {

	public static IntegerAxis instance() {
		return new IntegerAxis();
	}
	
	public IntegerAxis() {
		super();
	}
	
	public IntegerAxis(String title) {
		super(title);
	}
	
	public IntegerAxis(String title, Collection<Integer> entries){
		super(title, entries);
	}

	public IntegerAxis(String title, int min, int max) {
		super(title);
		addEntries(min, max);
	}
	
	public IntegerAxis addEntries(int min, int max) {
		for(int i = min; i <= max; i++ ) {
			addEntry(i, "" + i);
		}
		return this;
	}

	@Override
	public String getLabel(Integer entry) {
		return this.entryLabels.get(entry);
	}

	@Override
	public Integer getIndex(Object entry) {
		return this.entryIndices.get((Integer) entry);
	}
	
	@Override
	public IntegerAxis withTitle(String title) {
		return (IntegerAxis) super.withTitle(title);
	}
	
	@Override
	public IntegerAxis addEntry(Integer entry, String label) {
		return (IntegerAxis) super.addEntry(entry, label);
	}
}
