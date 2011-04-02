package FingerPrintLocator;
import java.util.ArrayList;
import java.util.Hashtable;


public class Transaction 
{
	
	/**
	 * ID of calibrated block
	 */
	public int       blockID;
	
	/**
	 * ID of calibrated/transmitting tag
	 */
	public int       tagID;
	
	/**
	 * ID of incoming message
	 */
	public int       msgID;
	
	/**
	 * x coordinate - Location of block, known when calibrating, unknown during locating 
	 */
	public float     x;
	
	/**
	 * y coordinate - Location of block, known when calibrating, unknown during locating 
	 */
	public float     y;
	
	/**
	 * battery level of tag 
	 */
	public int       batteryLevel;
	
	/**
	 * timestamp when proxy receives this transaction 
	 */
	public long      time;
	
	/**
	 * RSSI data (fingerprint pattern), keys: detectorID, value: list of RSSI (raw) 
	 */
	public Hashtable<Integer, ArrayList<Integer>> rssiLists;// keys: detectorIDs
	
	
	public Transaction()
	{
		this.rssiLists = new Hashtable<Integer, ArrayList<Integer>>();
		this.blockID = -1;
		this.msgID = -1;
		this.tagID = -1;
		this.time = -1;
		this.x = -1;
		this.y = -1;
	}
	
}
