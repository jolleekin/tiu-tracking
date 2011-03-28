package nnetlocator;

import java.util.ArrayList;
import java.util.Hashtable;

public interface LocationEngine {
	public void calculate(
			int tagID,
			Hashtable<Integer, ArrayList<Integer>> rssiTable,
			Hashtable<Integer, Vector2D> detectorLocationTable,
			Vector2D refLocation,
			Vector2D result
			);
	
}
