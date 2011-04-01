package FingerPrintLocator;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

public class FingerPrint 
{
	public int                                     fingerPrintID;
	private ArrayList<Transaction>                 fingerPrintTable;
	private boolean                                dirty;
	private ArrayList<Hashtable<Integer,Integer>>    statTable;	
	private String                                 statmode;
	
	public FingerPrint()
	{
		//fingerPrintTable = new ArrayList<Transaction>();
		this.statmode = "mean";
		this.dirty    = false;
		this.statTable = new ArrayList<Hashtable<Integer,Integer>>();
		
	}
	/*
	 * create class instance with fingerPrintTable passed in
	 */
	public FingerPrint(ArrayList<Transaction> fp)
	{
		this.statmode = "mean";
		this.dirty    = false;
		fingerPrintTable = fp;
	}
	
	/*
	 * locating engine, receive a transaction with location unknown
	 * return true if success (transaction modified with known location)
	 * 
	 */
	public boolean locate(Transaction t, String mode)	
	{
		if (!this.statmode.equals(mode))
		{
			this.statmode = mode.toString();
			this.fill_stat(mode);
		}
		else
		{
			
		}
		return false;
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
		for(Transaction t : this.fingerPrintTable) 
		{
			Enumeration<Integer> mykeys = t.rssiLists.keys();
			while(mykeys.hasMoreElements())
			{
				int current_key = mykeys.nextElement();
				ArrayList<Integer> current_list = (ArrayList<Integer>)t.rssiLists.get(current_key);
				if(!current_list.isEmpty())
				{
					if (mode.equals("mean"))
					{
						(this.statTable.get(t.hashCode())).put(current_key, Statistics.mean(current_list));
						
					}
					else if(mode.equals("mode"))
					{
						(this.statTable.get(t.hashCode())).put(current_key, Statistics.mode(current_list));
						
					}
					else if(mode.equals("median"))
					{
						(this.statTable.get(t.hashCode())).put(current_key, Statistics.median(current_list));
						
					}
					
				}
			}
		}
	}
	
}
