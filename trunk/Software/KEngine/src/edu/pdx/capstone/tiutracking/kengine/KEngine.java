package edu.pdx.capstone.tiutracking.kengine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.Set;

import edu.pdx.capstone.tiutracking.common.*;

/**
 * An implementation of the location engine based on path loss model.
 * <p>
 * The engine creates a path loss model for each detector and uses
 * Delta Rule from neural networks to adjust the models
 * during the learning phase.
 * </p>
 * <p>
 * To locate a tag, the engine uses a simple algorithm in which each
 * detector imposes a force on the tag. The magnitude of the force
 * is proportional to the difference between the distance derived from
 * an RSSI list and the current distance.
 * The net force causes the tag to move. After some amount of time, the
 * tag will be at a location at which the net force is virtually zero.
 * That location is the result of the locating process.
 * </p>
 * <p>
 * In case the algorithm does not converge, a max iteration count is used
 * to ensure that the algorithm will eventually stop.
 * </p>
 *  
 * @author Kin
 *
 */
public class KEngine implements LocationEngine {

	private static final String DATA_FILE_NAME = "KEngine.dat";
	private static final String CONFIG_FILE_NAME = "KEngine.cfg";

	private static final String CFG_LEARNING_RATE = "Learning Rate";
	private static final String CFG_LEARNING_CYCLE = "Learning Cycle";
	private static final String CFG_MAX_ITERATIONS = "Max Iterations";
	private static final String CFG_STATISTIC_VALUE = "Statistic Value";
	private static final String CFG_THRESHOLD = "Threshold";

	private int cfgLearningCycle = 10;
	private double cfgLearningRate = 0.3;
	private int cfgMaxIterations = 20;
	private StatisticMode cfgStatisticMode = StatisticMode.MEDIAN;
	private double cfgThreshold = 1;

	private Hashtable<String, ConfigurationParam> configuration;
	private Hashtable<Integer, PathLossModel> pathLossModels;

	// Table holding the locations of all detectors
	private Hashtable<Integer, Vector2D> detectorLocations;

	// List of detectors involved in the current data packet
	private ArrayList<DetectorInfo> involvedDetectors = new ArrayList<DetectorInfo>();

	@SuppressWarnings("unchecked")
	public KEngine() {

		try {
			// Load configuration file if exists.
			configuration = (Hashtable<String, ConfigurationParam>) ObjectFiler.load(CONFIG_FILE_NAME);

			// If file does not exist, intialized default configuration.
			if (configuration == null) {

				configuration.put(CFG_LEARNING_RATE, new ConfigurationParam(
						"Learning rate used to adjust the path loss models. It is also the interpolation " +
						"factor used to update the location of a tag during the locating process.",
						ValueType.DOUBLE,
						cfgLearningRate));

				configuration.put(CFG_LEARNING_CYCLE, new ConfigurationParam(
						"Number of cyles that a list of raw data packets is used to adjust the models.",
						ValueType.INTEGER,
						cfgLearningCycle));
				
				configuration.put(CFG_MAX_ITERATIONS, new ConfigurationParam(
						"The threshold below which the net force applied on a tag is considered zero.",
						ValueType.DOUBLE,
						cfgMaxIterations));
				
				configuration.put(CFG_STATISTIC_VALUE, new ConfigurationParam(
						"Statistic value used to calculate the 'average' RSSI.",
						ValueType.STATISTIC_MODE,
						cfgStatisticMode));

				configuration.put(CFG_THRESHOLD, new ConfigurationParam(
						"The threshold below which the net force applied on a tag is considered zero.",
						ValueType.DOUBLE,
						cfgThreshold));
			}

			// Load data file if exists
			pathLossModels = (Hashtable<Integer, PathLossModel>) ObjectFiler.load(DATA_FILE_NAME);

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
			Hashtable<Integer, Vector2D> detectorLocations) {

		// Save the detector table for later use.
		this.detectorLocations = detectorLocations;

		// Performs learning algorithm
		for (int cycle = cfgLearningCycle - 1; cycle >= 0; cycle--) {

			for (DataPacket packet : rawData) {

				Set<Entry<Integer, ArrayList<Integer>>> set = packet.rssiTable.entrySet();

				for (Entry<Integer, ArrayList<Integer>> entry : set) {

					// TODO: Implement your learning algorithm here

					int detectorId = entry.getKey();
					double distance = packet.location.distanceTo(detectorLocations.get(detectorId));
					int rssi = Statistics.calculate(entry.getValue(), cfgStatisticMode);

					PathLossModel model;

					if (pathLossModels.containsKey(detectorId) == false) {
						model = pathLossModels.put(detectorId, new PathLossModel());
					} else {
						model = pathLossModels.get(detectorId);
					}

					model.learn(rssi, distance, cfgLearningRate);
				}
			}
		}

		try {
			ObjectFiler.save(DATA_FILE_NAME, pathLossModels);
		} catch (IOException e) {
			System.out.println("Failed to save " + DATA_FILE_NAME);
		}
	}

	@Override
	public void locate(DataPacket dataPacket) {

		Set<Entry<Integer, ArrayList<Integer>>> set = dataPacket.rssiTable.entrySet();

		if (set.size() > 0) {

			Vector2D result = dataPacket.location;
			result.set(0, 0);

			involvedDetectors.clear();

			for (Entry<Integer, ArrayList<Integer>> entry : set) {
				int key = entry.getKey();

				PathLossModel model = pathLossModels.get(key);
				Vector2D location = detectorLocations.get(key);
				int rssi = Statistics.calculate(entry.getValue(), cfgStatisticMode);
				double distance = model.rssiToDistance(rssi);

				involvedDetectors.add(new DetectorInfo(model, location, distance));
				result.add(location);
			}

			// Initialize the tag's location to be at the centroid of the
			// polygon formed by the involved detectors.
			result.mult(1 / involvedDetectors.size());

			int count = 0;

			for (;;) {
				Vector2D netForce = new Vector2D();

				// Calculate individual forces and net force imposed on the tag.
				for (DetectorInfo info : involvedDetectors) {

					// Get the vector pointing from the detector to the tag.
					Vector2D v = Vector2D.sub(info.location, result);

					// Scale it by the percent of difference between the
					// measured distance and the current distance.
					v.mult(info.distance / v.mag() - 1);

					// Add to the net force.
					netForce.add(v);
				}

				count++;
				result.lerp(netForce, cfgLearningRate);

				if ((netForce.magSquared() < cfgThreshold) || (count >= cfgMaxIterations)) {
					return;
				}
			}
		}
	}

	@Override
	public Hashtable<String, ConfigurationParam> getConfiguration() {
		return configuration;
	}

	@Override
	public void onConfigurationChanged() {

		cfgLearningCycle = (Integer) configuration.get(CFG_LEARNING_RATE).getValue();
		cfgLearningRate = (Double) configuration.get(CFG_LEARNING_RATE).getValue();
		cfgMaxIterations = (Integer) configuration.get(CFG_MAX_ITERATIONS).getValue();
		cfgStatisticMode = (StatisticMode) configuration.get(CFG_STATISTIC_VALUE).getValue();
		cfgThreshold = (Double) configuration.get(CFG_THRESHOLD).getValue();

		try {
			ObjectFiler.save(CONFIG_FILE_NAME, configuration);
		} catch (IOException e) {
			System.out.println("Failed to save " + CONFIG_FILE_NAME);
		}
	}

}
