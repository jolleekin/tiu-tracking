package edu.pdx.capstone.tiutracking.shared;

public enum ValueType {
	
	DOUBLE,
	INTEGER,
	STATISTIC_MODE,
	STRING;
	
	public Class<?> getJavaClass() {
		switch (this) {
		case DOUBLE:
			return Double.class;
		case INTEGER:
			return Integer.class;
		case STATISTIC_MODE:
			return StatisticMode.class;
		default:
			return String.class;
		}
	}

}
