package edu.pdx.capstone.tiutracking.neuralnet;

import java.util.ArrayList;
import edu.pdx.capstone.tiutracking.shared.Vector2D;

public class Weight {
	ArrayList<Double> instarList;
	Vector2D location;
	public Weight() {
		instarList = new ArrayList<Double>();
		location = new Vector2D();
	}
	
	public Weight(ArrayList<Double> instarList, Vector2D location) {
		this.instarList = instarList;
		this.location = location;
	}
}
