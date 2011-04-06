package edu.pdx.capstone.tiutracking;

import java.util.ArrayList;

import edu.pdx.capstone.tiutracking.common.ConfigurationParam;
import edu.pdx.capstone.tiutracking.common.StatisticMode;

public class Main {

	public static void main(String[] args) {

		ArrayList<ConfigurationParam> params = new ArrayList<ConfigurationParam>();
		params.add(new ConfigurationParam("A", null, 0.0));
		params.add(new ConfigurationParam("B", null, StatisticMode.MAX));

		for (ConfigurationParam p : params) {
			String[] list = p.getValueList();
			if (list != null) {
				for (String item : list)
					System.out.println(item);
			} else {
				System.out.println("Not an enum");
			}
		}
	}

}
