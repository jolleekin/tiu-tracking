package edu.pdx.capstone.tiutracking;

import java.util.Hashtable;

public abstract class LocationEngine {
	
	/**
	 * Abstract constructor. Any class derived from this class must
	 * override this constructor.
	 * 
	 * @param detectors		A table containing the locations of all detectors.
	 */
	public LocationEngine(/* raw data goes here, */Hashtable<Integer, Vector2D> detectors) {
		
	}
	
	/**
	 *	Locate an asset tag based on measured RSSI values.
	 *
	 *	@param	tag			The tag whose location is to be determined.
	 */
	public abstract void locate(Tag tag);

}
