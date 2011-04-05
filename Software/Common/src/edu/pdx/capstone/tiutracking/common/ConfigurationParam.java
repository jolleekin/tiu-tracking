package edu.pdx.capstone.tiutracking.common;

import java.io.Serializable;

public final class ConfigurationParam implements Serializable {

	private static final long serialVersionUID = 954522706171093537L;

	public final String description;
	public final ValueType type;
	private Object value;

	/**
	 * Creates an instance of ConfigurationParam class.
	 * 
	 * @param desc
	 *            The description for this element.
	 * @param type
	 *            The value type of this element.
	 * @param value
	 *            The value assigned to this element.
	 */
	public ConfigurationParam(String desc, ValueType type, Object value) {

		if (type.getJavaClass() != value.getClass()) {
			throw new IllegalArgumentException("Arguments unmatched: type = "
					+ type + ", value = " + value);
		}
		this.description = desc;
		this.type = type;
		this.value = value;
	}

	public Object getValue() {
		return value;
	}
	
	/**
	 * Sets the value from a string. This method shoudl ease the life of the
	 * controller writer ;)
	 * 
	 * @param valueStr
	 *            The string representing the value
	 */
	public void setValue(String valueStr) {

		switch (type) {
		case DOUBLE:
			value = Double.parseDouble(valueStr);
		case INTEGER:
			value = Integer.parseInt(valueStr);
		case STATISTIC_MODE:
			value = StatisticMode.valueOf(valueStr);
		default:
			value = valueStr;
		}
	}
}
