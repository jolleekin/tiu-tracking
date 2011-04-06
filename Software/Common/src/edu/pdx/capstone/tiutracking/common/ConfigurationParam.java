package edu.pdx.capstone.tiutracking.common;

import java.io.Serializable;

public final class ConfigurationParam implements Serializable {

	private static final long serialVersionUID = -7916728150923528984L;

	public final String name;
	public final String description;
	private Object value;

	/**
	 * Creates a new configuration param.
	 * 
	 * @param name
	 *            Name of this param. Name cannot be null.
	 * @param desc
	 *            The description for this element.
	 * @param value
	 *            The value assigned to this element. The value's type should be
	 *            explicitly defined, e.g. 1.0d for Double.
	 */
	public ConfigurationParam(String name, String desc, Object value) {

		if (name == null) {
			throw new IllegalArgumentException("Name cannot be null.");
		}
		this.name = name;
		this.description = desc;
		this.value = value;
	}

	/**
	 * Returns a string representing the value type of this param.
	 * 
	 * @return A string representing the value type of this param.
	 */
	public String getTypeName() {
		return value.getClass().getSimpleName();
	}

	public Object getValue() {
		return value;
	}

	/**
	 * Sets value of this param from a string. This provides a consistent and
	 * simple interface for the controller to change the param's value since the
	 * controller does not care about the param's type.
	 * 
	 * @param valueStr
	 *            A string representing the value to be assigned to this param.
	 */
	public void setValue(String valueStr) {

		Class<?> c = value.getClass();
		if (c == Double.class) {
			value = Double.valueOf(valueStr);

		} else if (c == Integer.class) {
			value = Integer.valueOf(valueStr);

		} else if (c == StatisticMode.class) {
			value = StatisticMode.valueOf(valueStr);

		} else if (c == String.class) {
			value = valueStr;
		} else {
			throw new IllegalArgumentException("Unsupported value type.");
		}
	}

}
