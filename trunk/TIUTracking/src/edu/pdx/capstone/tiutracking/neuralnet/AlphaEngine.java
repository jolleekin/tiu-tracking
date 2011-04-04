package edu.pdx.capstone.tiutracking.neuralnet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;


import edu.pdx.capstone.tiutracking.LocationEngine;
import edu.pdx.capstone.tiutracking.shared.*;

public class AlphaEngine implements LocationEngine {
	
	private double learningRate;
	private ArrayList<Neuron> neurons;
	private double sigma;
	
	
	/** 
	 * Creates an instance of AlphaEngine class which is implemented as a neural network
	 * 
	 * @param rawData
	 * 			An array of data packets passed by the controller
	 * @param detectors
	 * 			A hash table which contains setup locations of detectors
	 */
	
	@SuppressWarnings("unchecked")
	public AlphaEngine(ArrayList<DataPacket> rawData,
			Hashtable<Integer, Vector2D> detectors) {
		
		//Initialize parameters of the neural network
		learningRate = 0.2;
		sigma = 1.0;
		
		/* Load the configuration from file
		 * Create an ArrayList of neurons with instar and outstar weights
		 */
		try {
			neurons = (ArrayList<Neuron>) ObjectFiler.load("neurons.txt");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		
		/*
		 * Learning process The neural network processes rawData and generates
		 * the training set The learning process is supervised by using the
		 * known locations of asset tags provided in rawData
		 */
		
		
	}
	
	public void learn(ArrayList<DataPacket> rawData,
			Hashtable<Integer, Vector2D> detectors) {
		
		System.out.println("Learning mode...");
		for (int index = 0; index < rawData.size(); index++) {
			Hashtable<Integer, ArrayList<Integer>> rssiTable = rawData.get(index).rssiTable;
			Vector2D refLocation = rawData.get(index).location;
			learnRefined(rssiTable, refLocation);
		}
		System.out.println("Learning successful!");
	}

	
	/** 
	 * Locates asset tags base on measured RSSI values
	 * 
	 * @param dataPacket
	 * 			A data packet which contains RSSI values measured by detectors from specified tag
	 * @return 
	 * 			Modify dataPacket.location
	 */
	public void locate(DataPacket dataPacket) {
		Pattern pattern = new Pattern();
		preprocess(dataPacket.rssiTable, pattern);
		
	}

	
	/** 
	 * Gets configuration of the neural net
	 * 
	 * @return
	 * 			A hash table which contains configuration's info
	 */
	public Hashtable<String, String> getConfiguration() {

		return null;
	}


	/** 
	 * Load configuration of the neural network
	 * 
	 * <p>
	 * Process the given hash table and update new configuration of the neural network
	 * 
	 * @param config
	 * 			A hash table which contains configuration's info 
	 */
	public void setConfiguration(Hashtable<String, String> config) {

	}

	
	private void learnRefined(Hashtable<Integer, ArrayList<Integer>> rssiTable, Vector2D refLocation) {
		Pattern pattern = new Pattern();
		preprocess(rssiTable, pattern);
		
		//Learns pattern.max
		for (Neuron neuron : neurons) {
			//if the reference location is around one existed outstar Vector2D
			if (neuron.outstar.equals(refLocation)) {
				linearInterpolate(neuron.instar, neuron.instar, pattern.max, learningRate);
				return;
			}
		}
			
		// There is no such neuron, then add a new neuron which has outstar weights specified by refLocation
		neurons.add(new Neuron(pattern.max, refLocation));
	}
	
	private void preprocess(Hashtable<Integer, ArrayList<Integer>> rssiTable, Pattern pattern) {
		int index = 0;
		
		for (ArrayList<Integer> v: rssiTable.values()) {
			pattern.max.add(index, (double) Collections.max(v));
			pattern.min.add(index, (double) Collections.min(v));
			pattern.mean.add(index, (double) Statistics.mean(v));
			pattern.median.add(index, (double) Statistics.median(v));
			
		}
	}
	
	private void linearInterpolate(ArrayList<Double> result, ArrayList<Double> a, ArrayList<Double> b, double factor) {

		assert (result.size() == a.size()) && (a.size() == b.size());

		for (int i = result.size() - 1; i >= 0; i--) {
			double s = a.get(i);
			double d = b.get(i);
			result.set(i, s + (d - s) * factor);
		}
	}
	
	//Calculate distance between input vector and instar vector
	private double distance(ArrayList<Double> input, ArrayList<Double> instar) {
		assert (input.size() == instar.size());
		
		double result = 0;
		
		for (int i = 0; i < input.size(); i++) {
			result += Math.pow((input.get(i) - instar.get(i)), 2);
		}
		
		return Math.sqrt(result);
	}
	
	private double gaussian(double distance) {
		return Math.exp(-(distance * distance)/(2 * sigma * sigma));
	}
	
	private void recall(Vector2D result, ArrayList<Double> input) {
		
	}
	
}
