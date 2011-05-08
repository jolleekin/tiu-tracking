
package edu.pdx.capstone.tiutracking.controller;
import edu.pdx.capstone.tiutracking.common.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * @author Le Dang Dung
 * FingerPrint class - version 2.0
 * implements FingerPrint engine: matching tag 
 * location to sets of calibrated locations. Data used in calibrating are 
 * RSSIs (received signal strength indicator) from a tag placed inside the 
 * location. These signals measured by the surrounding detector nodes.
 * 
 * changes:
 * 1. support detect aliasing blocks with threshold
 */

public class FingerPrint implements LocationEngine
{	
	/**
	 * ID of this FingerPrint pattern, associated with the tag that used for calibration 
	 */
	public int                                               fingerPrintID;
	
	/**
	 * FingerPrint table, ArrayList of Block patterns (Transaction type)
	 */
	private ArrayList<DataPacket>                            fingerPrintTable;
	
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
	private StatisticMode                                    statmode;
	
	/**
	 * threshold for determine aliasing, used to compare among euclidean distances
	 */
	private double                                           aliasThreshold;
	/**
	 * threshold for determine adjacent blocks, unit: meter
	 */
	private double                                           maxBlockSize;
    /**
     * threshold for finding closest detector, unit : rssi unit
     */
	private int                                              rssiThreshold;
	
	private double                                           goodPredictionThreshold;
	
	/**
	 * detector location 
	 * key - ID
	 * value - location
	 */
	//private Hashtable<Integer, Vector2D>                     detectorLocations;
	/**
	 * block location 
	 * key - ID
	 * value - location
	 */
	 
	private Hashtable<Integer, Vector2D>                     blockLocations;
	
	/**
	 * Create class instance with fingerPrintTable passed in
	 * This must be done before calling the locate() method.  
	 * @param table: ArrayList of Transactions
	 */
	public FingerPrint()
	{
		this.statmode = StatisticMode.MEDIAN;
		this.dirty    = false;
		this.aliasThreshold = 10;  // 5 euclidean units
		this.maxBlockSize = 2;    // 2m
		this.rssiThreshold = 10;  // 10 rssi units
		this.blockLocations = new Hashtable<Integer, Vector2D>();
		this.goodPredictionThreshold = 15;
		
	}
	
	/**
	 * set the threshold of aliasing
	 * @param t
	 */
	public void setAliasThreshold(double t)
	{
		this.aliasThreshold = t;
	}  
	
	public void setRssiThreshold(int t)
	{
		this.rssiThreshold = t;
	} 
	/**
	 * set the maximum size of blocks
	 * @param t
	 */
	public void setMaxBlockSize(double t)
	{
		this.maxBlockSize = t;
	}  
	
	public boolean isAdjacent(Integer blk_1, Integer blk_2)
	{
		ArrayList <Double> refDistance = new ArrayList<Double>();		
		Enumeration<Integer> blkIDs = this.blockLocations.keys();  
		double dis = this.blockLocations.get(blk_1).distanceTo(this.blockLocations.get(blk_2));
		
		// check 1 : max block size
		if (dis <= this.maxBlockSize)
			return true;
		// check 2 : a block is one of the closest blocks of other 
		while(blkIDs.hasMoreElements())                    // calculate distance from blk1 to the rest, exclude blk 2
		{
			int currentBlkID = blkIDs.nextElement();       // get blockID
			if(currentBlkID != blk_1 && currentBlkID != blk_2)                      
			{
				refDistance.add(this.blockLocations.get(currentBlkID).distanceTo(this.blockLocations.get(blk_1)));
			}
		}	
		
		Collections.sort(refDistance);
		// blk 2 is adjacent to blk 1 if it is one of the closest blocks (count == 2) to blk 1
		int count = 0;
		
		while (true)
		{
			
			if (dis <= refDistance.get(count))
			{
				return true;
			}
			count++;
			if(count >= refDistance.size() || count == 2)
				break;
		}
		
		return false;

	}
	
	
	/**
	 * 
	 */
	public void learn(ArrayList<DataPacket> table, Hashtable<Integer, Vector2D> detLocs){
		//this.statmode = StatisticMode.MEDIAN;
		//this.dirty    = false;
		//this.aliasThreshold = 10;  // 10 euclidean units
		//this.maxBlockSize = 2;    // 2m
		//this.rssiThreshold = 10;  // 10 rssi units
		
		
		fingerPrintTable = table;
		
		
		this.fill_stat(this.statmode);
		//this.detectorLocations = detLocs;
		
		// get block locations
		for(DataPacket block : this.fingerPrintTable)                            // the table is a list of blocks
		{
			this.blockLocations.put(block.blockId, block.location);
		}
		
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
	public void locate(DataPacket t)	
	{
		/*if (this.statmode != mode)
		{
			this.statmode = mode;
			this.fill_stat(mode);
		}*/
		if(t.rssiTable.size() <= 2 )
		{
			System.out.println(": need more detector, n = "+ t.rssiTable.size() );
			return;
		}
		Hashtable<Double,Integer> ED_hashlist = new Hashtable<Double,Integer>(); // list of Euclidean distance
		ArrayList<Double> ED_mirror = new ArrayList<Double>(); // mirror list of the EU_list
		Enumeration<Integer> blockIDs = this.statTable.keys(); // get block id list
		while(blockIDs.hasMoreElements())
		{
			int currentBlockID = blockIDs.nextElement();       // get a block ID
			double eu_sum = 0;
			Enumeration<Integer> detIDs = t.rssiTable.keys();  // get detector id list, from the transaction, not the block
			while(detIDs.hasMoreElements())
			{
				int currentDetID = detIDs.nextElement();       // detectorID
				if(this.statTable.get(currentBlockID).get(currentDetID) != null )
				{
					double calibrationValue = this.statTable.get(currentBlockID).get(currentDetID);
					double currentValue = t.rssiTable.get(currentDetID).get(0);
					double squaredDifference = Math.pow(calibrationValue - currentValue	, 2);
					//double squaredDifference = calibrationValue - currentValue;//change 
					eu_sum = (eu_sum + squaredDifference);    // do the sum first
				}
			}	
			eu_sum = Math.sqrt(eu_sum);                       // take sqrt() of sum
			//eu_sum = Math.abs(eu_sum);// change
			System.out.println("Distance From Block " + currentBlockID + ": "+ eu_sum);
			ED_hashlist.put(eu_sum,currentBlockID);           // put eucledean results on the list
			ED_mirror.add(eu_sum);                            // put sum in mirror list for sorting
		}
		Collections.sort(ED_mirror);                          // sort list to find the min value
		                                                      // ascending order, according to the natural ordering of its elements
		
		this.locateNMO(t);
		// check for aliasing, 
		int blk_0 = ED_hashlist.get(ED_mirror.get(0));        // get the first element after sort, then get block ID from the hash table
		int blk_1 = ED_hashlist.get(ED_mirror.get(1));
		double adjustedAliasThreshold = this.aliasThreshold;
		if (ED_mirror.get(0) > this.goodPredictionThreshold)
		{
			adjustedAliasThreshold  = adjustedAliasThreshold + 5; 
		}
		
		if(Math.abs(ED_mirror.get(0) - ED_mirror.get(1)) < adjustedAliasThreshold)
		{
			// if so, check if the two block is adjacent
			System.out.println(":suspect aliasing.........\n");
			if( this.isAdjacent(blk_0, blk_1) )
			{
				System.out.println(":adjacent blocks, interpolate.........\n");	
				// if so, interpolate
				t.location.x = (this.blockLocations.get(blk_0).x + this.blockLocations.get(blk_1).x) / 2;
				t.location.y = (this.blockLocations.get(blk_0).y + this.blockLocations.get(blk_1).y) / 2;
				// warning! not exact block, 0 or 1 ?
				t.blockId = blk_0;

				
			}
			else
			{
				// no => aliasing, need to determine which is the "chosen one"
				
				Enumeration<Integer> dk = t.rssiTable.keys();  // get detector id list, from the transaction, not the block
				int rssi_temp = 0;
				int of_detector = 0;
				// get the strongest signal strength from a detector
				while(dk.hasMoreElements())
				{
					int cdk = dk.nextElement();       // detectorID
					if ( (t.rssiTable.get(cdk)).get(0) > rssi_temp)
					{
						rssi_temp = (t.rssiTable.get(cdk)).get(0);
						of_detector = cdk;
					}
					

						
				}
				
				if (of_detector != 0) // valid detector
				{
					// the non-aliasing block should has similar rssi 
					if (Math.abs(this.statTable.get(blk_0).get(of_detector) - rssi_temp) < this.rssiThreshold)
					{
						t.blockId = blk_0;
						t.location.set(this.blockLocations.get(blk_0));
						System.out.println(":not adjacent blocks, determine by closest rssi... : " + blk_0);
					}
					else
					{
						t.blockId = blk_1;
						t.location.set(this.blockLocations.get(blk_1));
						System.out.println(":not adjacent blocks, determine by closest rssi..." + blk_1);

					}
						
				}
				
				
				
			}
		}
		else // no aliasing 
		{
			t.blockId = blk_0;
			t.location.set(this.blockLocations.get(blk_0));
		}
		
		      
		//t.x = this.fingerPrintTable.get(t.blockId).x;       // map to its x and y coordinates
		//t.y = this.fingerPrintTable.get(t.blockId).y;
		
		//t.location.set(this.fingerPrintTable.get(t.blockId).location);
		//return true;
		
		//return false;
	}
	/**
	 * locate algorithm using n-1 model
	 * @param t
	 */
	public void locateNMO(DataPacket t)
	{
		
		System.out.println(": (n-1) mode........................." );
		Enumeration<Integer> nDetIDs = t.rssiTable.keys();  // get detector id list, from the transaction, not the block
		Hashtable<Integer,Double> nResult = new Hashtable<Integer,Double>(); // list of Euclidean distance
		while(nDetIDs.hasMoreElements())
		{
			int currentNDetID = nDetIDs.nextElement();       // detectorID
				
			Hashtable<Double,Integer> ED_hashlist = new Hashtable<Double,Integer>(); // list of Euclidean distance
			ArrayList<Double> ED_mirror = new ArrayList<Double>(); // mirror list of the EU_list
			Enumeration<Integer> blockIDs = this.statTable.keys(); // get block id list
			while(blockIDs.hasMoreElements())
			{
				int currentBlockID = blockIDs.nextElement();       // get a block ID
				double eu_sum = 0;
				Enumeration<Integer> detIDs = t.rssiTable.keys();  // get detector id list, from the transaction, not the block
				while(detIDs.hasMoreElements())
				{
					int currentDetID = detIDs.nextElement();       // detectorID
					if (currentDetID == currentNDetID)
						continue;
					if(this.statTable.get(currentBlockID).get(currentDetID) != null )
					{
						double calibrationValue = this.statTable.get(currentBlockID).get(currentDetID);
						double currentValue = t.rssiTable.get(currentDetID).get(0);
						double squaredDifference = Math.pow(calibrationValue - currentValue	, 2);
						//double squaredDifference = calibrationValue - currentValue;//change 
						eu_sum = (eu_sum + squaredDifference);    // do the sum first
					}
				}	
				eu_sum = Math.sqrt(eu_sum);                       // take sqrt() of sum
				//eu_sum = Math.abs(eu_sum);// change
				System.out.println("Distance From Block " + currentBlockID + ": "+ eu_sum);
				ED_hashlist.put(eu_sum,currentBlockID);           // put eucledean results on the list
				ED_mirror.add(eu_sum);                            // put sum in mirror list for sorting
			}
			Collections.sort(ED_mirror);                          // sort list to find the min value
			                                                      // ascending order, according to the natural ordering of its elements
			nResult.put( ED_hashlist.get(ED_mirror.get(0)), ED_mirror.get(0));
		}
		Enumeration<Integer> temp_key = nResult.keys(); // get block id list
		System.out.println(".............nresults ");
		while(temp_key.hasMoreElements())
		{
			int blk = temp_key.nextElement();       // get a block ID
			System.out.println("block " + blk + ":" + nResult.get(blk));
		}
	}
	
	/**
	 * commit new data to FingerPrint table
	 * @return
	 */
	public ArrayList<DataPacket> commit()
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
	private void fill_stat(StatisticMode mode)
	{
		this.statTable = new Hashtable<Integer, Hashtable<Integer,Integer>>();
		for(DataPacket block : this.fingerPrintTable)                            // the table is a list of blocks
		{
			this.statTable.put(block.blockId, new Hashtable<Integer, Integer>()); // create block pattern, with ID from input Block
			Enumeration<Integer> mykeys = block.rssiTable.keys();                 // mykeys = detector id list
			while(mykeys.hasMoreElements())
			{
				int current_key = mykeys.nextElement();                           // current_key ; detectorID
				ArrayList<Integer> current_list = (ArrayList<Integer>)block.rssiTable.get(current_key);
				if(!current_list.isEmpty())
				{			
					this.statTable.get(block.blockId).put(current_key, Statistics.calculate(current_list,mode));
				}
			}
		}
	}

	@Override
	public ArrayList<ConfigurationParam> getConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onConfigurationChanged() {
		// TODO Auto-generated method stub
		
	}
	
}
