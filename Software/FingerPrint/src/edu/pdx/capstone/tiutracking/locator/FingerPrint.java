
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
	 * if distance gap between one block to other is greater than the aliasThreshold,
	 * the engine has clue that there is no aliasing.
	 */
	private double                                           aliasThreshold;
	/**
	 * threshold for determine adjacent blocks, unit: meter
	 */
	private double                                           maxBlockSize;
	
    /**
     * threshold for finding closest detector, uses rssi unit
     */
	private int                                              rssiThreshold;
	
	
	/**
	 * threshold to determine good prediction, uses euclidean unit
	 * 
	 * best prediction has distance  = 0
	 * 10-20 is good or normal	  
	 * 
	 */
	private double                                           goodPredictionThreshold;
	
	/**
	 * reference for an approximate number of adjacent block, normally a block should has 2-3 adjacent blocks 
	 */
	private int                                              adjacentBlocks;
	
	/**
	 * rate for (n-1) vote model, 75% is default
	 */
	private double                                           NMOvoteRate;
	
	/**
	 * keep track of 1000 predictions
	 */
	private Hashtable<Integer,Hashtable<Integer,Integer>>    predictions;
	
	/**
	 * save last block where tag is at
	 */
	private  Hashtable<Integer,Integer>                      lastPrediction_block;
	
	/**
	 * save last location where tag is at
	 */
	private  Hashtable<Integer,Vector2D>                     lastPrediction_location;
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
		this.aliasThreshold = 15; // 
		this.maxBlockSize = 2;    // 2m
		this.rssiThreshold = 10;  // 
		this.blockLocations = new Hashtable<Integer, Vector2D>();
		this.goodPredictionThreshold = 15;
		this.adjacentBlocks = 2;  
		this.NMOvoteRate = 1;
		this.lastPrediction_block = new Hashtable<Integer, Integer>();
		this.lastPrediction_location = new Hashtable<Integer, Vector2D>();
		
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
	/**
	 * set the maximum number of adjacent blocks
	 * @param t
	 */
	public void setAdjacentBlocks(int t)
	{
		this.adjacentBlocks = t;
	}  
	/**
	 * set the minimum rate for (n-1) vote model
	 * @param t
	 */
	public void setNMOVoteRate(double t)
	{
		this.NMOvoteRate = t;
	} 
	/**
	 * find out if blk_1 is adjacent to blk_2
	 * @param blk_1
	 * @param blk_2
	 * @return
	 */
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
		int count = this.adjacentBlocks;
		
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
	public void learn(ArrayList<DataPacket> table, Hashtable<Integer, Vector2D> detLocs)
	{	
		fingerPrintTable = table;		
		this.fill_stat(this.statmode);
	
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
		
		// check for aliasing, 
		// this.locateNMO(t);
		
		int blk_0 = ED_hashlist.get(ED_mirror.get(0));        // get the first element after sort, then get block ID from the hash table
		int blk_1 = ED_hashlist.get(ED_mirror.get(1));
		double adjustedAliasThreshold = this.aliasThreshold;
		
		// first sign  : large distance ( if a block has small distance to tag, this is the sign of good a prediction ) 
		// second sign : similar signal strength
		if (ED_mirror.get(0) > this.goodPredictionThreshold || (Math.abs(ED_mirror.get(0) - ED_mirror.get(1))) < adjustedAliasThreshold)
		{			
			// if so, check if the two blocks are adjacent
			System.out.println(":suspect aliasing.........\n");
			if( this.isAdjacent(blk_0, blk_1) )
			{
				// if so,not aliasing but adjacent -> interpolate
				System.out.println(":adjacent blocks " + blk_0 + "," + blk_1);
				System.out.println(":adjacent blocks, interpolate. : x = "+ this.blockLocations.get(blk_0).x + ","+ this.blockLocations.get(blk_1).x );
				System.out.println(":adjacent blocks, interpolate. : y = "+ this.blockLocations.get(blk_0).y + ","+ this.blockLocations.get(blk_1).y );
				t.location.x = (this.blockLocations.get(blk_0).x + this.blockLocations.get(blk_1).x) / 2;
				t.location.y = (this.blockLocations.get(blk_0).y + this.blockLocations.get(blk_1).y) / 2;
				// warning! not exact block, 0 or 1 ?
				t.blockId = blk_0;	
				this.lastPrediction_block.put(t.tagId, blk_0);
				//this.lastPrediction_location 
				
				this.locateNMO(t, true);
			}
			else // not adjacent, => aliasing, need to determine which is the "chosen one"
			{			
				Enumeration<Integer> dk = t.rssiTable.keys();  // get detector id list, from the transaction, not the block
				int rssi_strongest = 0;  // for the strongest 
				int of_detector = 0;

				// get the strongest signal strength from a detector
				while(dk.hasMoreElements())
				{
					int cdk = dk.nextElement();       // detectorID
					if ( (t.rssiTable.get(cdk)).get(0) > rssi_strongest)
					{
						rssi_strongest = (t.rssiTable.get(cdk)).get(0);
						of_detector = cdk;
					}
				}
				if (of_detector != 0 && this.statTable.get(blk_0).get(of_detector)!=null) // valid detector
				{
					// the non-aliasing block should has similar rssi 
					if (Math.abs(this.statTable.get(blk_0).get(of_detector) - rssi_strongest) < this.rssiThreshold)
					{
						t.blockId = blk_0;
						this.lastPrediction_block.put(t.tagId, blk_0);
						t.location.set(this.blockLocations.get(blk_0));
						System.out.println(":not adjacent blocks, determine by closest rssi... : " + blk_0);
					}
					else
					{
						t.blockId = blk_1;
						this.lastPrediction_block.put(t.tagId, blk_1);
						t.location.set(this.blockLocations.get(blk_1));
						System.out.println(":not adjacent blocks, determine by closest rssi..." + blk_1);
					}
				}
				else
				{
					t.blockId = blk_1;
					t.location.set(this.blockLocations.get(blk_1));
					this.lastPrediction_block.put(t.tagId, blk_1);
					System.out.println(":not adjacent blocks, determine by closest rssi..." + blk_1);
				}
				this.locateNMO(t,false);
			}
			
			
		}		
		else // no aliasing 
		{
			t.blockId = blk_0;
			t.location.set(this.blockLocations.get(blk_0));
			this.lastPrediction_block.put(t.tagId, blk_0);
		}
		System.out.println("--------------------end locating ----------------------------\n");
		
		

	}
	/**
	 * locating algorithm using n-1 model:
	 * + there are n-1 detectors in use for the euclidean distance calculation
	 * + since there are less detector used but circulated, prediction from this model will likely to:
	 *    - support the main prediction when it is unsure about a prediction( use goodPrediction and aliasing Threshold) 
	 *    - detector bad data from single detector
	 * + prediction from this model can be used to interpolate block location (double interpolate possible) 
	 * + if the main prediction is solid, the NMO might not be helpful anymore, hence avoid 
	 * 
	 * @param t
	 */
	private void locateNMO(DataPacket t, boolean interpolated)
	{
		if(t.rssiTable.size() < 4 )
		{
			System.out.println(": need more detector, n = "+ t.rssiTable.size() );
			return;
		}
		System.out.println("\n: (n-1) mode........................." );
		Enumeration<Integer> nDetIDs = t.rssiTable.keys();  // get detector id list, from the transaction, not the block
		Hashtable<Double,Integer> nResult = new Hashtable<Double,Integer>(); // list of Euclidean distance
		while(nDetIDs.hasMoreElements())
		{
			int currentNDetID = nDetIDs.nextElement();       // detectorID
			System.out.println(": without value from det:" + currentNDetID );	
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
				System.out.println("NMO: Distance From Block " + currentBlockID + ": "+ eu_sum);
				ED_hashlist.put(eu_sum,currentBlockID);           // put eucledean results on the list
				ED_mirror.add(eu_sum);                            // put sum in mirror list for sorting
			}
			Collections.sort(ED_mirror);                          // sort list to find the min value
			                                                      // ascending order, according to the natural ordering of its elements
			nResult.put( ED_mirror.get(0),ED_hashlist.get(ED_mirror.get(0)) );
		}
		Enumeration<Double> temp_key = nResult.keys(); // get distance keys
		System.out.println(".............nresults ");
		
		ArrayList<Integer> candidateList = new ArrayList<Integer>();
		ArrayList<Double> distList = new ArrayList<Double>();
		ArrayList<Double> distListMirror = new ArrayList<Double>();


		while(temp_key.hasMoreElements())
		{
			double dist = temp_key.nextElement();       // get a block ID
			System.out.println("candidate : " + nResult.get(dist) + "  dist :" + dist );
			candidateList.add(nResult.get(dist));
			distList.add(dist);
			distListMirror.add(dist);
			
		}
		ArrayList<Integer> modeResult = this.findMode(candidateList);
		
		int candidateBlock = modeResult.get(0);
		int candidateBlock_freq = modeResult.get(1);
		double currentRate= (double)candidateBlock_freq/(double)candidateList.size();
		System.out.println("most vote candidate : " + candidateBlock + "    freq : "+ candidateBlock_freq + "    rate : "+ currentRate + "");
		
		
		/*
		 * Analyzing : n results of (n-1) model
		 * 1.	if all the smallest distances (n results) refers to one detector and the distance is less than the goodPredictionThreshold
		 *      	-> location is confirmed at that block
		 * 		else
		 * 			if ( vote rate < 100% ) 
		 * 				if there are gap between one distance and the rest (difference b/w the smallest to the second > aliasingThreshold)
		 * 					if distances are exceptionally high  ( > 5*goodPredictionThreshold ) 
		 * 						-> bad data, the smallest of n results should be the best guess //or previous good result is recommended
		 * 					else if distances are NOT exceptionally high but NO good distance either ( smallest value > goodPredictionThreshold) 
		 * 						-> interpolate adjacent blocks or previous good result is recommended 
		 * 	            	else (there is one good distance)
		 * 						-> good data, interpolate adjacent block is recommended but tag should be closer to block with smallest distance
		 * 				else if there are NO obvious gap between one distance and the rest
		 * 					-> no best candidate, the most voted with smallest distance is recommended 
		 * 			if (vote rate == 100 % but its distance > 5*goodPredictionThreshold)
		 * 				-> bad data, interpolate adjacent block or previous good result is recommended 
		 * 
		 * 2. a recommend result means the engine should double-interpolate if possible
		 *    a confirm means the engine should not interpolate or double-interpolate 
		 */    
		
		Collections.sort(distListMirror);
		double d0 = distListMirror.get(0);
		double d1 = distListMirror.get(1);
		if(this.blockLocations.get(candidateBlock) != null)
		{
			if(currentRate >= this.NMOvoteRate && d0 < this.goodPredictionThreshold )
			{
				//t.blockId = candidateBlock;
				//t.location.set(this.blockLocations.get(candidateBlock));
				System.out.println("confirm block " + nResult.get(d0));	
				if (interpolated == true && this.blockLocations.get(candidateBlock)!=null) // pull the location back a little bit
				{
					t.location.x = (this.blockLocations.get(candidateBlock).x + t.location.x) / 2;
					t.location.y = (this.blockLocations.get(candidateBlock).y + t.location.y) / 2;
				}
				else
				{
					t.blockId = candidateBlock;
					t.location.set(this.blockLocations.get(candidateBlock));
				}
			}
			else
			{			
				if (currentRate < this.NMOvoteRate )
				{
					if (Math.abs(d0 - d1) > this.aliasThreshold)
					{
						if(d0 > 5 * this.goodPredictionThreshold)
						{
							System.out.println("bad data, prediction: best distance recommendation, block " + nResult.get(d0));	
							t.location.x = (this.blockLocations.get(candidateBlock).x + 2*t.location.x) / 3;
							t.location.y = (this.blockLocations.get(candidateBlock).y + 2*t.location.y) / 3;
						}					
						else if(d0 > this.goodPredictionThreshold)
						{
							System.out.println("neutral distance: best distance recommendation, block " + nResult.get(d0));							
							System.out.println("compare to last prediction:" + this.lastPrediction_block.get(t.tagId));
							t.location.x = (this.blockLocations.get(candidateBlock).x + t.location.x) / 2;
							t.location.y = (this.blockLocations.get(candidateBlock).y + t.location.y) / 2;
						}
						else if( d0 <= this.goodPredictionThreshold)
						{
							System.out.println("good data:  recommendation block " + nResult.get(d0));
							t.location.x = (2*this.blockLocations.get(candidateBlock).x + t.location.x) / 3;
							t.location.y = (2*this.blockLocations.get(candidateBlock).y + t.location.y) / 3;

						}						
					}
					else
					{
						System.out.println("no best candidate: best distance recommendation, block " + nResult.get(d0));
						t.location.x = (this.blockLocations.get(nResult.get(d0)).x + t.location.x) / 2;
						t.location.y = (this.blockLocations.get(nResult.get(d0)).y + t.location.y) / 2;
						System.out.println("no best candidate: most vote recommendation, block " + candidateBlock);	
						t.location.x = (this.blockLocations.get(candidateBlock).x + t.location.x) / 2;
						t.location.y = (this.blockLocations.get(candidateBlock).y + t.location.y) / 2;
					}
				}
				else 
				{
					System.out.println("bad data, prediction: 3rd recommendation, block " + nResult.get(d0));	
					t.location.x = (this.blockLocations.get(candidateBlock).x + 2*t.location.x) / 3;
					t.location.y = (this.blockLocations.get(candidateBlock).y + 2*t.location.y) / 3;
					System.out.println("compare to last prediction:" + this.lastPrediction_block.get(t.tagId));
				}
			}
		}
		
		System.out.println("--------------------end NMO analysis----------------------------\n");						

		
	}
	/**
	 * simple stats:mode
	 * @param list : raw data 
	 * @return a 2 elements array: [0]-mode in the array and [1]-frequency 
	 * 
	 */
	public ArrayList<Integer> findMode(ArrayList<Integer> list)
	{
		int mode = 0;
		
		Hashtable<Integer,Integer> ht = new Hashtable<Integer, Integer>(); // key == element, value == frequency
		ArrayList<Integer> ret = new ArrayList<Integer>();
		for (int e:list)
		{
			if (ht.containsKey(e)) // check if the element presents
			{
				// yes, increase frequency
				int temp = ht.get(e) + 1 ; // increase
				ht.remove(e);
				ht.put(e, temp);
			}
			else
			{
				ht.put(e, 1);
			}
		
		}
		Enumeration<Integer> k = ht.keys(); // get keys : elements
		int maxf = 1;
		while(k.hasMoreElements())
		{
			int e = k.nextElement();       // get an element
			if (maxf < ht.get(e))
			{
				maxf = ht.get(e);          // get the higher f
				mode = e;                  // and the mode
			}
		}
		ret.add(0,mode);		
		ret.add(1,maxf);
		return ret;		
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
