package edu.pdx.capstone.tiutracking.kengine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.Set;

import edu.pdx.capstone.tiutracking.LocationEngine;
import edu.pdx.capstone.tiutracking.shared.ConfigurationElement;
import edu.pdx.capstone.tiutracking.shared.DataPacket;
import edu.pdx.capstone.tiutracking.shared.ObjectFiler;
import edu.pdx.capstone.tiutracking.shared.StatisticValue;
import edu.pdx.capstone.tiutracking.shared.Statistics;
import edu.pdx.capstone.tiutracking.shared.ValueType;
import edu.pdx.capstone.tiutracking.shared.Vector2D;

public class KEngine implements LocationEngine {

	private static final String DATA_FILE_NAME = "KEngine.dat";
	private static final String CONFIG_FILE_NAME = "KEngine.cfg";

	private static final String CFG_LEARNING_RATE = "Learning Rate";
	private static final String CFG_LEARNING_CYCLE = "Learning Cycle";
	private static final String CFG_STATISTIC_VALUE = "Statistic Value";

	private Hashtable<String, ConfigurationElement> configuration;
	private Hashtable<Integer, PathLossModel> pathLossModels;

	private int cycleCount = 10;
	private double learningRate = 0.3;
	private StatisticValue statValue = StatisticValue.MEDIAN;

	@SuppressWarnings("unchecked")
	public KEngine() {

		try {
			// Load configuration file if exists.
			configuration = (Hashtable<String, ConfigurationElement>) ObjectFiler
					.load(CONFIG_FILE_NAME);
			
			// If file does not exist, intialized default configuration.
			if (configuration == null) {

				configuration.put(CFG_LEARNING_RATE, new ConfigurationElement(
						"Learning rate used to adjust the path loss models.",
						ValueType.DOUBLE, 0.3));

				configuration.put(CFG_LEARNING_CYCLE, new ConfigurationElement(
						"Number of cyles that a list of raw data packets is used to adjust the models.",
						ValueType.INTEGER, 10));

				configuration.put(CFG_STATISTIC_VALUE, new ConfigurationElement(
						"Statistic value used to calculate the 'average' RSSI.",
						ValueType.STATISTIC_VALUE,
						StatisticValue.MEDIAN));
			}

			// Load data file if exists
			pathLossModels = (Hashtable<Integer, PathLossModel>) ObjectFiler
					.load(DATA_FILE_NAME);
			
			if (pathLossModels == null) {
				pathLossModels = new Hashtable<Integer, PathLossModel>();
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void learn(ArrayList<DataPacket> rawData,
			Hashtable<Integer, Vector2D> detectors) throws IOException {

		for (int cycle = cycleCount - 1; cycle >= 0; cycle--) {

			for (DataPacket packet : rawData) {

				Set<Entry<Integer, ArrayList<Integer>>> set = packet.rssiTable.entrySet();

				for (Entry<Integer, ArrayList<Integer>> entry : set) {

					int detectorId = entry.getKey();
					double distance = packet.location.distanceTo(detectors
							.get(detectorId));
					int rssi = Statistics
							.calculate(entry.getValue(), statValue);

					PathLossModel model;

					if (pathLossModels.containsKey(detectorId) == false) {
						model = pathLossModels.put(detectorId,
								new PathLossModel());
					} else {
						model = pathLossModels.get(detectorId);
					}

					model.learn(rssi, distance, learningRate);
				}
			}
		}

		ObjectFiler.save(DATA_FILE_NAME, pathLossModels);
	}

	@Override
	public void locate(DataPacket dataPacket) {
		// To be done soon :D

	}

	@Override
	public Hashtable<String, ConfigurationElement> getConfiguration() {
		return configuration;
	}

	@Override
	public void onConfigurationChanged() throws IOException {

		cycleCount = (Integer) configuration.get(CFG_LEARNING_RATE).value;
		learningRate = (Double) configuration.get(CFG_LEARNING_RATE).value;
		statValue = (StatisticValue) configuration.get(CFG_STATISTIC_VALUE).value;

		ObjectFiler.save(CONFIG_FILE_NAME, configuration);
	}

}
