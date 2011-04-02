package FingerPrintLocator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;

public class FingerPrint 
{
	public int                                               fingerPrintID;
	private ArrayList<Transaction>                           fingerPrintTable;
	private boolean                                          dirty;
	
	private Hashtable<Integer,Hashtable<Integer,Integer>>    statTable;	// this table holds stats mode values of the fingerPrintTable
                                                                        // first key(blockID) index to a block, second key(detectorID)
	                                                                    // indexes to the rssi value (stats) 
	private String                                           statmode;
	
	public FingerPrint()
	{
		//fingerPrintTable = new ArrayList<Transaction>();
		this.statmode = "mean";
		this.dirty    = false;
		this.statTable = new Hashtable<Integer,Hashtable<Integer,Integer>>();
		
	}
	/*
	 * create class instance with fingerPrintTable passed in
	 */
	public FingerPrint(ArrayList<Transaction> fp)
	{
		this.statmode = "mean";
		this.dirty    = false;
		fingerPrintTable = fp;
	 
		this.fill_stat(this.statmode);
	}
	
	/*
	 * locating engine, receive a transaction with location unknown
	 * - input : transaction t with unknown location, this.statTable
	 * - return true if success (transaction modified with known location)
	 * 
	 */
	public boolean locate(Transaction t, String mode)	
	{
		if (!this.statmode.equals(mode))
		{
			this.statmode = mode.toString();
			this.fill_stat(mode);
		}
		
		Hashtable<Double,Integer> ED_list = new Hashtable<Double,Integer>(); // list of Eucledean distance
		ArrayList<Double> ED_mirror = new ArrayList<Double>();
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
							, 2));
				}
			}	
			eu_sum = Math.sqrt(eu_sum);
			ED_list.put(eu_sum,currentBlockID);    // put eucledean results on the list
			ED_mirror.add(eu_sum);
		}
		Collections.sort(ED_mirror);               // sort list to find the min value
		t.blockID = ED_list.get(ED_mirror.get(0)); // get the first element after sort, this will be the key to hash table
		t.x = this.fingerPrintTable.get(t.blockID).x;
		t.y = this.fingerPrintTable.get(t.blockID).y;
		return true;
		
		//return false;
	}
	public ArrayList<Transaction> commit()
	{
		return null;
	}
	public boolean isDirty()
	{
		return this.dirty;
	}
	private void fill_stat(String mode)
	{
		this.statTable = new Hashtable<Integer, Hashtable<Integer,Integer>>();
		for(Transaction block : this.fingerPrintTable) // the table is a list of blocks
		{
			this.statTable.put(block.blockID, new Hashtable<Integer, Integer>()); // create block pattern
			Enumeration<Integer> mykeys = block.rssiLists.keys(); // mykeys = detector id list
			while(mykeys.hasMoreElements())
			{
				int current_key = mykeys.nextElement();  // current_key ; detectorID
				ArrayList<Integer> current_list = (ArrayList<Integer>)block.rssiLists.get(current_key);
				if(!current_list.isEmpty())
				{
					if (mode.equals("mean"))
					{
						//(this.statTable.get(t.hashCode())).put(current_key, Statistics.mean(current_list));
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
