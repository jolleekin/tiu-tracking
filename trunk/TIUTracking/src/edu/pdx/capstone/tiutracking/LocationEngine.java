package edu.pdx.capstone.tiutracking;

import java.util.ArrayList;
import java.util.Hashtable;

import edu.pdx.capstone.tiutracking.shared.DataPacket;
import edu.pdx.capstone.tiutracking.shared.Vector2D;

/**
 * An interface for all location engines.
 * 
 * @author Kin
 * 
 */
public interface LocationEngine {

	/**
	 * Learns how to locate the tags given raw calibration data and detectors'
	 * locations.
	 * 
	 * @param rawData
	 *            The data collected during calibration.
	 * @param detectors
	 *            A table containing the locations of all detectors.
	 */
	public void learn(ArrayList<DataPacket> rawData,
			Hashtable<Integer, Vector2D> detectors);

	/**
	 * Locates an asset tag based on measured RSSI values.
	 * 
	 * @param dataPacket
	 *            A data packet which contains various information about the tag
	 *            whose location is to be determined.
	 */
	public void locate(DataPacket dataPacket);

	/**
	 * Retrieves a configuration table for this engine.
	 * 
	 * @return A hash table which contains the engine's configuration.
	 */
	public Hashtable<String, String> getConfiguration();

	/**
	 * Applies configuration to the engine.
	 * 
	 * @param config
	 *            A hash table which contains the engine's configuration.
	 */
	public void setConfiguration(Hashtable<String, String> config);
}
