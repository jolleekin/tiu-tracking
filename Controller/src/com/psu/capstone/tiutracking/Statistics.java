package com.psu.capstone.tiutracking;

import java.util.ArrayList;

public class Statistics {
	public static int mean(ArrayList<Integer> data) {
		int sum = 0;
		for (int i = 0; i < data.size(); i++) {
			sum += data.get(i);
		}
		return sum / data.size();
	}
	
	public static int mode(ArrayList<Integer> data) {
		int[] count = new int[526];
		for (int i = 0; i < count.length; i++) {
			count[i] = 0;
		}
		
		for (int i = 0; i < data.size(); i++) {
			count[data.get(i)]++;
		}
		
		int result = 0;
		int maxCount = count[0];
		for (int i = 1; i < count.length; i++) {
			if (count[i] > maxCount) {
				maxCount = count[i];
				result = i;
			}
		}
		return result;
	}
	
	public static int median(ArrayList<Integer> data) {
		Collections.sort(data);
		int i = data.size() / 2;
		if ((data.size() & 1) == 1) {
			return data.get(i);
		} else {
			return (data.get(i) + data.get(i - 1)) / 2;
		}
	}
}
