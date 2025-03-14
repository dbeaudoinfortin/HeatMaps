package com.dbf.heatmaps.data;

public class BasicDataRecord implements DataRecord {
	
	private Object x;
	private Object y;
	private Double value;

	/**
	 * 
	 * @return a new empty instance of the BasicDataRecord class
	 */
	public static BasicDataRecord instance() {
		return new BasicDataRecord();
	}
	
	public BasicDataRecord() {}

	public BasicDataRecord(Object x, Object y, Double value) {
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

	public BasicDataRecord withX(Object x) {
		this.x = x;
		return this;
	}

	public BasicDataRecord withY(Object y) {
		this.y = y;
		return this;
	}

	public BasicDataRecord withValue(Double value) {
		this.value = value;
		return this;
	}
}
