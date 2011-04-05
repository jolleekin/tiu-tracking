package edu.pdx.capstone.tiutracking;

import edu.pdx.capstone.tiutracking.common.ConfigurationParam;
import edu.pdx.capstone.tiutracking.common.ValueType;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ConfigurationParam p = new ConfigurationParam(
				"Learning rate used to adjust the path loss models. It is also the interpolation " +
				"factor used to update the location of a tag during the locating process.",
				ValueType.DOUBLE,
				3);
	}

}
