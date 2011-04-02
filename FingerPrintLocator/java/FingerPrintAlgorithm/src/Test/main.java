package Test;
import java.util.ArrayList;
import java.util.Hashtable;

import FingerPrintLocator.*;

public class main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Transaction b0 = new Transaction();
		Transaction b1 = new Transaction();
		Transaction bx = new Transaction();
		
		ArrayList<Integer> testList = new ArrayList<Integer>();
		ArrayList<Integer> testList1 = new ArrayList<Integer>();
		ArrayList<Integer> testListx = new ArrayList<Integer>();
		
		
		
		b0.blockID = 0;
		b0.msgID = 0;
		b0.tagID = 0;
		b0.x = 0;
		b0.y = 0;
		b0.rssiLists = new Hashtable<Integer, ArrayList<Integer>>();
		testList.add(111);
		testList.add(112);
		testList.add(113);
		testList.add(114);
		testList.add(115);
		b0.rssiLists.put(0, testList);
		
		
		b1.blockID = 1;
		b1.msgID = 1;
		b1.tagID = 0;
		b1.x = 1;
		b1.y = 1;
		b1.rssiLists = new Hashtable<Integer, ArrayList<Integer>>();
		testList1.add(121);
		testList1.add(122);
		testList1.add(123);
		testList1.add(124);
		testList1.add(125);
		b1.rssiLists.put(1, testList1);
		
		ArrayList<Transaction> fp = new ArrayList<Transaction>();
		fp.add(b0);
		fp.add(b1);
		
		FingerPrint FP = new FingerPrint(fp);
				
		
		//bx.blockID = 1;
		bx.msgID = 1;
		bx.tagID = 0;
		//bx.x = 1;
		//bx.y = 1;
		bx.rssiLists = new Hashtable<Integer, ArrayList<Integer>>();
		testListx.add(121);
		
		bx.rssiLists.put(0, testListx);
		
		bx.rssiLists.put(1, testListx);
		
		FP.locate(bx,"mean");
		FP.locate(bx,"median");
	}

}
