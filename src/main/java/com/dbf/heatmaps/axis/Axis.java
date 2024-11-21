package com.dbf.heatmaps.axis;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class Axis<T> {
	
	private String title = "";
	protected int count;
	
	/* LOOKUP TABLES */
	protected final Map<T, String>       entryLabels  = new HashMap<T, String>();
	protected final Map<T, Integer>      entryIndices = new HashMap<T, Integer>();
	protected final Map<String, Integer> labelIndices = new HashMap<String, Integer>();
	
	public Axis(){}
	
	public Axis(String title){
		setTitle(title);
	}
	
	public Axis(String title, Collection<T> entries){
		this(title);
		for(T entry : entries) {
			addEntry(entry, entry.toString());
		}
	}
	
	protected Axis(String title, int count){
		this(title);
		this.count = count;
	}
	
	public int getCount() {
		return count;
	}

	public Map<T, String> getEntryLabels() {
		return entryLabels;
	}

	public Map<T, Integer> getEntryIndices() {
		return entryIndices;
	}
	
	public Map<String, Integer> getLabelIndices() {
		return labelIndices;
	}
	
	public abstract String getLabel(T entry);
	
	public abstract Integer getIndex(Object entry);
	
	public Axis<T> addEntry(T entry, String label) {
		if(!entryLabels.containsKey(entry)) {
			entryLabels.put(entry, label);
			entryIndices.put(entry, count);
			labelIndices.put(label, count);
			count++;
		}
		return this;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		if(null == title) title = "";
		this.title = title;
	}
	
	public Axis<T> withTitle(String title) {
		setTitle(title);
		return this;
	}
}
