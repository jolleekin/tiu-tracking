package edu.pdx.capstone.tiutracking.shared;

import java.io.Serializable;

public final class ConfigurationElement implements Serializable {

	private static final long serialVersionUID = 954522706171093537L;

	public final String description;
	public final ValueType type;
	public Object value;

	public ConfigurationElement(String desc, ValueType type,
			Object value) {

		if (type.getJavaClass() != value.getClass()) {
			throw new IllegalArgumentException("Arguments unmatched: type = "
					+ type + ", value = " + value);
		}
		this.description = desc;
		this.type = type;
		this.value = value;
	}

}
