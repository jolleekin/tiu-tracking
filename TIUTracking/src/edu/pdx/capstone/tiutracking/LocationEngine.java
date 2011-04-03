package edu.pdx.capstone.tiutracking;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * A base class for all location engines. Any class derived from this class must
 * override the constructor and all the abstract methods.
 * 
 * @author Kin
 * 
 */
public abstract class LocationEngine {

	/**
	 * Creates an instance of LocationEngine class. The engine uses the provided
	 * raw calibration data and detectors' locations to "learn" how to locate
	 * the tags.
	 * 
	 * @param rawData
	 *            The data collected during calibration.
	 * @param detectors
	 *            A table containing the locations of all detectors.
	 */
	public LocationEngine(ArrayList<Tag> rawData,
			Hashtable<Integer, Vector2D> detectors) {

	}

	/**
	 * Locates an asset tag based on measured RSSI values.
	 * 
	 * @param tag
	 *            The tag whose location is to be determined.
	 */
	public abstract void locate(Tag tag);

	/**
	 * Retrieves a configuration table for this engine.
	 * 
	 * @return A hash table which contains the engine's configuration.
	 */
	public abstract Hashtable<String, String> getConfiguration();

	/**
	 * Applies configuration to the engine.
	 * 
	 * @param config
	 *            A hash table which contains the engine's configuration.
	 */
	public abstract void setConfiguration(Hashtable<String, String> config);
}
