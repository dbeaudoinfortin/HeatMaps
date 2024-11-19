package com.dbf.heatmaps.data;

public class DataRecord {
	private Object x;
	private Object y;
	private Double value;

	public DataRecord() {}

	public DataRecord(Object x, Object y, Double value) {
		super();
		this.x = x;
		this.y = y;
		this.value = value;
	}

	public Double getValue() {
		return value;
	}
	public void setValue(Double value) {
		this.value = value;
	}

	public Object getX() {
		return x;
	}

	public void setX(Object x) {
		this.x = x;
	}

	public Object getY() {
		return y;
	}

	public void setY(Object y) {
		this.y = y;
	}
}
