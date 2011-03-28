package nnetlocator;

import java.util.ArrayList;

public class Neuron {
	public ArrayList<Double> instar;
	public Vector2D outstar;
	
	public Neuron(ArrayList<Double> instar, Vector2D outstar) {
		this.instar = new ArrayList<Double>(instar);
		this.outstar = new Vector2D(outstar);
		
	}
}
