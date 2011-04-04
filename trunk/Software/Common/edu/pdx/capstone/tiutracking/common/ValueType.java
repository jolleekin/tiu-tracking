package edu.pdx.capstone.tiutracking.common;

public enum ValueType {
	
	DOUBLE,
	INTEGER,
	STATISTIC_VALUE,
	STRING;
	
	public Class<?> getJavaClass() {
		switch (this) {
		case DOUBLE:
			return Double.class;
		case INTEGER:
			return Integer.class;
		case STATISTIC_VALUE:
			return StatisticValue.class;
		default:
			return String.class;
		}
	}

}
