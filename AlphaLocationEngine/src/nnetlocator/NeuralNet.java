package nnetlocator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;


public class NeuralNet implements LocationEngine {
	
	public double learningRate; //learning rate, which can be modified by the controller
	private ArrayList<Neuron> neurons;
	private final int NEURON_NUMBER = 30;
	private final int INPUT_LENGTH = 4;
	private static double sigma; //sigma is an empirical value
	public Hashtable<Integer, Weights> myWeightTable;
	
	//Constructors
	
	//Construct a new neural network
	public NeuralNet() {
		learningRate = 0.2;
		sigma = 1.0;
		neurons = new ArrayList<Neuron>(NEURON_NUMBER);
		
	}
	
	
	/* Load an existed neural network
	 * which is specified by instar weight vectors 
	 * and outstar weight vectors
	 */

	public NeuralNet(Hashtable<Integer, Weights> weightTable) {
		learningRate = 0.2;
		sigma = 1.0;
		neurons = new ArrayList<Neuron>();
		//Load configuration of the neural net
		//Get each element
		myWeightTable = weightTable;
		for (Weights w: myWeightTable.values()) {
			neurons.add(new Neuron(w.instarList, w.location));
		}
		
	}
	
	//Load an existed neural network by name (extension: *.nnet)
	public NeuralNet (String fileName) {
		
		learningRate = 0.2;
		sigma = 1.0;
		//Read file's content
		
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
		 * Variants of input patterns from raw data are MAX, MIN, MEAN, MEDIAN
		 * Each input pattern is an integer ArrayList which has fixed length
		 */
		ArrayList<Double> patternMax = new ArrayList<Double>();
		ArrayList<Double> patternMin = new ArrayList<Double>();
		ArrayList<Double> patternMean = new ArrayList<Double>();
		ArrayList<Double> patternMedian = new ArrayList<Double>();
		
		int index = 0;
		
		for (ArrayList<Integer> v: rssiTable.values()) {
			patternMax.add(index, (double)Collections.max(v)); 		//MAX pattern
			patternMin.add(index, (double)Collections.min(v)); 		//MIN pattern
			patternMean.add(index, (double)Statistics.mean(v)); 	//MEAN pattern
			patternMedian.add(index, (double)Statistics.median(v)); //MEDIAN pattern
		}
		
		
		//Serve the request from controller
		
		//If reference location is null, the neural network operates in locating mode
		if (refLocation == null) {
			System.out.println("Locating mode...");
			
			//Initialize result vector
			result.x = 0;
			result.y = 0;
			
			//Try with different input patterns
			Vector2D resultMax = new Vector2D(0, 0);
			Vector2D resultMin = new Vector2D(0, 0);
			Vector2D resultMean = new Vector2D(0 ,0);
			Vector2D resultMedian = new Vector2D(0, 0);
			
			locating(resultMax, patternMax);
			locating(resultMin, patternMin);
			locating(resultMean, patternMean);
			locating(resultMedian, patternMedian);
			
			/* Decision making process
			 * At the level of location engine
			 * The returned result for location of the asset tag will be averaged
			 */
			result.x = (resultMax.x + resultMin.x + resultMean.x + resultMedian.x)/4;
			result.y = (resultMax.y + resultMin.y + resultMean.y + resultMedian.y)/4;

			
		}
		
		/* Else, switch the neural network to learning mode
		 * Choose patternMax as input for learning
		 * Note that we can choose other patterns to teach the neural network
		 */
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
				Weights extraNeuron = new Weights(patternMax, refLocation);
				int neuronID = myWeightTable.size();
				myWeightTable.put(neuronID + 1, extraNeuron);
		}
		
		
		
	}
	
	/* Calculate the location of asset tag corresponding to the applied input
	 * and return result
	 * for later use in decision making process
	 */
	public void locating(Vector2D result, ArrayList<Double> pattern) {
		double hsum = 0;
		double h;
		for (Neuron neuron : neurons) {
			h = gaussian(distance(neuron.instar, pattern));
			hsum += h;
			result.x += h * neuron.outstar.x;
			result.y += h * neuron.outstar.y;
		}
		result.mult(1 / hsum);
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
		return Math.exp(-(distance * distance)/(2 * sigma * sigma));
	}
	
}
