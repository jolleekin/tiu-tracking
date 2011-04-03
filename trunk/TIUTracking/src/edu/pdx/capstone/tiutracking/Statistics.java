package edu.pdx.capstone.tiutracking;

import java.util.ArrayList;
import java.util.Collections;

/**
 * A utility class that calculates various statistic values of an integer array.
 * All functions in this class assume that the data given are valid, i.e.
 * containing at least one element.
 * 
 * @version 1.0
 * 
 * @author Kin
 * 
 */
public class Statistics {

	/**
	 * Statistic Value Type Enumeration
	 */
	public static final int svMin = 0, svMean = 1, svMedian = 2, svMax = 3;

	public static int calculate(ArrayList<Integer> data, int valueType) {
		switch (valueType) {
		case svMax:
			return Collections.max(data);
		case svMedian:
			return median(data);
		case svMean:
			return mean(data);
		case svMin:
			return Collections.min(data);
		default:
			throw new IllegalArgumentException("Invalid value type: "
					+ valueType);
		}
	}

	public static int mean(ArrayList<Integer> data) {
		int sum = 0;
		for (int x : data) {
			sum += x;
		}
		return sum / data.size();
	}

	public static int median(ArrayList<Integer> data) {
		Collections.sort(data);
		int i = data.size() >> 1;
		if ((data.size() & 1) == 1) {
			return data.get(i);
		}
		return (data.get(i) + data.get(i - 1)) >> 1;
	}

	public static double stdDev(ArrayList<Integer> data) {
		int n = data.size();
		if (n == 1) {
			return 0;
		}
		int sum = 0;
		int sumOfSquares = 0;
		for (int x : data) {
			sum += x;
			sumOfSquares += x * x;
		}

		return Math.sqrt((double) (n * sumOfSquares - sum * sum)
				/ (n * (n - 1)));
	}

}
