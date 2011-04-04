package edu.pdx.capstone.tiutracking;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import edu.pdx.capstone.tiutracking.shared.ConfigurationElement;
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
	 * locations. After learning, the engine should save its data to a file.
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
	 * Retrieves a reference to the configuration table of this engine. The
	 * string key is the name of a configuration element.
	 * 
	 * @return A table containing the engine's configuration.
	 */
	public Hashtable<String, ConfigurationElement> getConfiguration();

	/**
	 * This method is called by the controller when the configuration of the
	 * engine has been changed. The engine can capture this event to update its
	 * states and save the configuration to a file.
	 * 
	 */
	public void onConfigurationChanged();
}
