package edu.pdx.capstone.tiutracking;

import java.util.ArrayList;
import java.util.Arrays;

public class Controller {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ArrayList<Integer> data = new ArrayList<Integer>(Arrays.asList(1, 3, 5));
		System.out.println("Mean = " + Statistics.calculate(data, Statistics.svMean));
		System.out.println("Median = " + Statistics.calculate(data, Statistics.svMedian));
		System.out.println("Max = " + Statistics.calculate(data, Statistics.svMax));
		System.out.println("Std Dev = " + Statistics.stdDev(data));
	}

}
