package FingerPrintLocator;
import java.util.ArrayList;
import java.util.Hashtable;


public class Transaction {
	public int       blockID;
	public int       tagID;
	public int       msgID;
	public float     x;
	public float     y;
	public int       batteryLevel;
	public long      time;
	public Hashtable<Integer, ArrayList<Integer>> rssiLists;
	//public ArrayList<ArrayList<Integer>> rssiList;
	public Transaction()
	{
		rssiLists = new Hashtable<Integer, ArrayList<Integer>>();
	
	}
	
}
