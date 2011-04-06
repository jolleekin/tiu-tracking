package edu.pdx.capstone.tiutracking;

import java.util.ArrayList;

import edu.pdx.capstone.tiutracking.common.ConfigurationParam;

public class Main {

	public static void main(String[] args) {

		ArrayList<ConfigurationParam> params = new ArrayList<ConfigurationParam>();
		params.add(new ConfigurationParam("", null, 0.0));
		params.add(new ConfigurationParam("A Double", null, 10));

		System.out.println(params.get(0).getTypeName());
	}

}
