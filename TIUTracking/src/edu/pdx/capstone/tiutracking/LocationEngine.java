package edu.pdx.capstone.tiutracking;

import java.util.ArrayList;
import java.util.Hashtable;

public interface LocationEngine {

	/**
	 *	Calculate the location of an asset tag based on RSSI values.
	 *
	 *	@param	rssiTable	A table containing RSSI lists. Each list corresponds to one detector.
	 *
	 *	@param	detectors	A table containing detectors' information.
	 *
	 *	@param	result		Vector which will hold the result. Must not be null.
	 */
	public void locate(
		Hashtable<Integer, ArrayList<Integer>> rssiTable,
		Hashtable<Integer, Detector> detectors,
		Vector2D result);

}
