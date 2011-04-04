
package edu.pdx.capstone.tiutracking.locator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * @author Le Dang Dung
 * FingerPrint class, implements FingerPrint engine: matching tag 
 * location to sets of calibrated locations. Data used in calibrating are 
 * RSSIs (received signal strength indicator) from a tag placed inside the 
 * location. These signals measured by the surrounding detector nodes.
 */

public class FingerPrint 
{	
	/**
	 * ID of this FingerPrint pattern, associated with the tag that used for calibration 
	 */
	public int                                               fingerPrintID;
	
	/**
	 * FingerPrint table, ArrayList of Block patterns (Transaction type)
	 */
	private ArrayList<Transaction>                           fingerPrintTable;
	
	private boolean                                          dirty;
	
	/**
	 * This table holds stats mode values of the fingerPrintTable.
	 * First key(blockID) index to a block, second key(detectorID)
	 * indexes to the rssi value (stats).
	 */
	private Hashtable<Integer,Hashtable<Integer,Integer>>    statTable;	 
	
	/** 
	 * current stats mode used in locating engine
	 */
	private String                                           statmode;
	
	/**
	 * Create class instance with fingerPrintTable passed in
	 * This must be done before calling the locate() method.  
	 * @param table: ArrayList of Transactions
	 */
	public FingerPrint(ArrayList<Transaction> table)
	{
		this.statmode = "mean";
		this.dirty    = false;
		fingerPrintTable = table;
		this.fill_stat(this.statmode);
	}
	
	/**
	 * Locating engine, receives a transaction with unknown location. 
	 * The engine calculates Eucledean distance between current RSSI set
	 * and the calibrated RSSI sets (matching pattern).
	 * After calculation, the transaction will be modified with known location. 
	 *
	 * @param t - transaction with unknown location
	 * @param mode - choose the stats mode for processing raw data
	 *               : "mean", "median", "mode"
	 * @return true if success, false otherwise
	 */
	public boolean locate(Transaction t, String mode)	
	{
		if (!this.statmode.equals(mode))
		{
			this.statmode = mode.toString();
			this.fill_stat(mode);
		}
		
		Hashtable<Double,Integer> ED_list = new Hashtable<Double,Integer>(); // list of Eucledean distance
		ArrayList<Double> ED_mirror = new ArrayList<Double>(); // mirror list of the EU_list
		Enumeration<Integer> blockIDs = this.statTable.keys(); // get block id list
		while(blockIDs.hasMoreElements())
		{
			int currentBlockID = blockIDs.nextElement();       // get a block ID
			double eu_sum = 0;
			Enumeration<Integer> detIDs = t.rssiLists.keys();  // get detector id list, from the transaction, not the block
			while(detIDs.hasMoreElements())
			{
				int currentDetID = detIDs.nextElement();       // detectorID
				if(this.statTable.get(currentBlockID).get(currentDetID) != null )
				{
					eu_sum = (eu_sum + Math.pow(
							(this.statTable.get(currentBlockID)).get(currentDetID)
							- t.rssiLists.get(currentDetID).get(0)
							, 2));                            // do the sum first
				}
			}	
			eu_sum = Math.sqrt(eu_sum);                       // take sqrt() of sum
			ED_list.put(eu_sum,currentBlockID);               // put eucledean results on the list
			ED_mirror.add(eu_sum);                            // put sum in mirror list for sorting
		}
		Collections.sort(ED_mirror);                          // sort list to find the min value
		                                                      // ascending order, according to the natural ordering of its elements
		t.blockID = ED_list.get(ED_mirror.get(0));            // get the first element after sort, this will be the key to hash table
		t.x = this.fingerPrintTable.get(t.blockID).x;         // map to its x and y coordinates
		t.y = this.fingerPrintTable.get(t.blockID).y;
		return true;
		
		//return false;
	}
	
	/**
	 * commit new data to FingerPrint table
	 * @return
	 */
	public ArrayList<Transaction> commit()
	{
		return null;
	}
	/**
	 * tells whether data in FingerPrint table are changed
	 * @return
	 */
	public boolean isDirty()
	{
		return this.dirty;
	}
	/**
	 * Fill the internal statsTable with stats value processed from raw data table
	 * @param mode - stats mode : "mean", "median", "mode"
	 */
	private void fill_stat(String mode)
	{
		this.statTable = new Hashtable<Integer, Hashtable<Integer,Integer>>();
		for(Transaction block : this.fingerPrintTable)                            // the table is a list of blocks
		{
			this.statTable.put(block.blockID, new Hashtable<Integer, Integer>()); // create block pattern, with ID from input Block
			Enumeration<Integer> mykeys = block.rssiLists.keys();                 // mykeys = detector id list
			while(mykeys.hasMoreElements())
			{
				int current_key = mykeys.nextElement();                           // current_key ; detectorID
				ArrayList<Integer> current_list = (ArrayList<Integer>)block.rssiLists.get(current_key);
				if(!current_list.isEmpty())
				{
					if (mode.equals("mean"))
					{
						this.statTable.get(block.blockID).put(current_key, Statistics.mean(current_list));
					}
					else if(mode.equals("mode"))
					{
						this.statTable.get(block.blockID).put(current_key, Statistics.mode(current_list));
						
					}
					else if(mode.equals("median"))
					{
						this.statTable.get(block.blockID).put(current_key, Statistics.median(current_list));
						
					}
					
				}
			}
		}
	}
	
}
