package nnetlocator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;


public class NeuralNet implements LocationEngine {
	
	public double learningRate; //learning rate, which can be modified by the controller
	private ArrayList<Neuron> neurons;
	private final int NEURON_NUMBER = 30;
	private final int INPUT_SIZE = 4;
	private final double SIGMA = 1.0;
	
	//Constructors
	
	//Construct a new neural network
	public NeuralNet() {
		learningRate = 0.2;
		neurons = new ArrayList<Neuron>(NEURON_NUMBER);
		
	}
	
	
	/* Load an existed neural network
	 * which is specified by instar weight vectors 
	 * and outstar weight vectors
	 */
	public NeuralNet(Hashtable<Integer, ArrayList<Double>> instarTable, Hashtable<Integer, ArrayList<Vector2D>> outstarTable) {
		learningRate = 0.2;
	}
	
	//Load an existed neural network by name (extension: *.nnet)
	public NeuralNet (String fileName) {
		
		learningRate = 0.2;
	}
	
	
	/**
	 * Main function
	 */
	public void calculate(
			int tagID,
			Hashtable<Integer, ArrayList<Integer>> rssiTable,
			Hashtable<Integer, Vector2D> detectorLocationTable,
			Vector2D refLocation,
			Vector2D result
	) {
		
		/** Process raw RSSI data
		 * Generate different input patterns which will be applied to neural net
		 * An input pattern is an ordered list of RSSI values corresponding to detector 1, 2, 3, and so on
		 * The fixed number of detectors is four
		 * Variants of input patterns from raw data are MAX, MIN, MODE, MEDIAN
		 * Each input pattern is an integer ArrayList which has fixed length
		 */
		ArrayList<Double> patternMax = new ArrayList<Double>();
		ArrayList<Double> patternMin;
		ArrayList<Double> patternMode;
		ArrayList<Double> patternMedian;
		
		int index = 0;
		for (Map.Entry<Integer, ArrayList<Integer>> e: rssiTable.entrySet()) {
			patternMax.add(index, (double)Collections.max(e.getValue()));
		}
		
		//Serve the request from controller
		if (refLocation == null) {
			System.out.println("Locating mode...");
			result.x = 0;
			result.y = 0;

			double h;
			for (Neuron neuron : neurons) {
				h = gaussian(distance(neuron.instar, patternMax));
				result.x += h * neuron.outstar.x;
				result.y += h * neuron.outstar.y;
			}
		}
		else {
			System.out.println("Learning mode...");
			for (Neuron neuron : neurons) {
				if (neuron.outstar.equals(refLocation)) {
					linearInterpolate(neuron.instar, neuron.instar, patternMax, learningRate);
					return;
				}
				}
		
				// Oops, there’s no such neuron :( So, we add one :)
				neurons.add(new Neuron(patternMax, refLocation));
		}
		
		/* Save the modified configuration into file
		 * 
		 */
		
		
		System.out.println(tagID);
		
	}
	
	
	// result = a + (b - a) * factor;
	public void linearInterpolate(ArrayList<Double> result, ArrayList<Double> a, ArrayList<Double> b, double factor) {

		assert (result.size() == a.size()) && (a.size() == b.size());

		for (int i = result.size() - 1; i >= 0; i--) {
			double s = a.get(i);
			double d = b.get(i);
			result.set(i, s + (d - s) * factor);
		}
	}
	
	//Calculate distance between input vector and instar vector
	public double distance(ArrayList<Double> input, ArrayList<Double> instar) {
		assert (input.size() == instar.size());
		
		double result = 0;
		
		for (int i = 0; i < input.size(); i++) {
			result += Math.pow((input.get(i) - instar.get(i)), 2);
		}
		
		return Math.sqrt(result);
	}
	
	public double gaussian(double distance) {
		return Math.exp(-(distance * distance)/(2 * SIGMA * SIGMA));
	}
	
}
