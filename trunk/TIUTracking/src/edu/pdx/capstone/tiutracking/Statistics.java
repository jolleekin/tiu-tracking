package edu.pdx.capstone.tiutracking;

import java.util.ArrayList;
import java.util.Collections;

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
			throw new IllegalArgumentException("Invalid value type: " + valueType);
		}
	}

	public static int mean(ArrayList<Integer> data) {
		int sum = 0;
		for (int i = data.size() - 1; i >= 0; i--) {
			sum += data.get(i);
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

}
