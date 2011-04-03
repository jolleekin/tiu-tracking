package edu.pdx.capstone.tiutracking;

import java.util.ArrayList;
import java.util.Hashtable;

public final class Tag {

	public final int id;

	/**
	 * Location of the tag. This field is modified by the location engine.
	 * After calling the locate() method of the location engine, the controller
	 * will save this field in the database.
	 */
	public final Vector2D location;
	
	/**
	 * <p>rssiTable[DID] is a list of RSSI values measured by detector <b>DID</b>
	 * for this tag.</p>
	 * 
	 * <p>To traverse the table, please use rssiTable.entrySet().iterator().</p>
	 */
	public final Hashtable<Integer, ArrayList<Integer>> rssiTable;
	
	public Tag(int id) {
		this.id = id;
		this.location = new Vector2D();
		this.rssiTable = new Hashtable<Integer, ArrayList<Integer>>();
	}
}
