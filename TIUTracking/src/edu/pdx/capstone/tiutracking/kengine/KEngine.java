package edu.pdx.capstone.tiutracking.kengine;

import java.util.ArrayList;
import java.util.Hashtable;

import edu.pdx.capstone.tiutracking.DataPacket;
import edu.pdx.capstone.tiutracking.LocationEngine;
import edu.pdx.capstone.tiutracking.Vector2D;

public class KEngine extends LocationEngine {

	public KEngine(ArrayList<DataPacket> rawData,
			Hashtable<Integer, Vector2D> detectors) {
		super(rawData, detectors);

		
	}

	@Override
	public void locate(DataPacket dataPacket) {


	}

	@Override
	public Hashtable<String, String> getConfiguration() {

		return null;
	}

	@Override
	public void setConfiguration(Hashtable<String, String> config) {


	}

}
