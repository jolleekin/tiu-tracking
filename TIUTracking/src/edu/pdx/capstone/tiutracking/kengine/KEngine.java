package edu.pdx.capstone.tiutracking.kengine;

import java.util.ArrayList;
import java.util.Hashtable;

import edu.pdx.capstone.tiutracking.LocationEngine;
import edu.pdx.capstone.tiutracking.shared.DataPacket;
import edu.pdx.capstone.tiutracking.shared.Vector2D;

public class KEngine implements LocationEngine {

	@Override
	public void learn(ArrayList<DataPacket> rawData,
			Hashtable<Integer, Vector2D> detectors) {
		// TODO Auto-generated method stub

	}

	@Override
	public void locate(DataPacket dataPacket) {
		// TODO Auto-generated method stub

	}

	@Override
	public Hashtable<String, String> getConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setConfiguration(Hashtable<String, String> config) {
		// TODO Auto-generated method stub

	}

}
