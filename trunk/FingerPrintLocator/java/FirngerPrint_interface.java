// interface for FingerPrint locator
// class : FingerPrint
// dependencies classes : Block, rssi_list, statistics...

// underconstruction note:  if you guys have any idea on this interface please add your comments 
// at bottom and commit, thanks.

class rssi_list
{
//++++++++++++++++++++++++++++++++++++++++++++ rssi_list ++++++++++++++++++++++++++++++++++++++++++++++++++++
// This class stores RSSI data in a list, and support stats calculations when needed
	type? list[];						// list of RSSI values recorded,
									
	type? length;						// keep track of list length
	void init()
// getset methods 			
	type? get_();
	type? set_();
// stats methods on the current list //	
	type? mean();		
	type? max();
	type? median();
	type? mode();
// method to add a value to list //
	type? add(rssi);
// method to remove a value from list //
	type? remove(element);
// destructor
//++++++++++++++++++++++++++++++++++++++++++++ end_rssi_list ++++++++++++++++++++++++++++++++++++++++++++
}

class Block
{
//++++++++++++++++++++++++++++++++++++++++++++ Block ++++++++++++++++++++++++++++++++++++++++++++ 
// - Block holdsthe RSSI pattern at the associated location
// - RSSI pattern is the array of RSSIs from all detectors, measured from a specific tag that
//   placed inside the block. Each element of the array show the RSSI list from one detector.
// - The array should have DETECTOR_NUM elements (fixed), but they does not need to be all valid
// - Note that the block only show pattern that is associated with a specific tag only. We are
//   not assumimg all tags should have the same pattern (because of hardware characteristics)
//   but we are trying to achieve that.

// - this class can be used for both calibrating and locating process : 
//   + in calibrating mode, the block has known location and rssi pattern
//   + in locating mode, the block only holds the pattern getting from incomming request, its location
//     will be determined by the locator engine

	type? block_xy;						// block coordiates
	type? block_number; 				// block numeric name
	type? tagID;						// associate tag id in the block
	rssi_list RSSI_pattern[];			// array of RSSIs from all detectors
										// elements of this array are of arrays of RSSIs
										// index to the array is the detectorID
										
	void init();						// init block pattern by rssi_stat type
			
		
// getset methods 			
	type? get_();
	type? set_();
// destructor
//
//++++++++++++++++++++++++++++++++++++++++++++ end_Block ++++++++++++++++++++++++++++++++++++++++++++ 

}

class FingerPrint
{
//++++++++++++++++++++++++++++++++++++++++++++ Fingerprint ++++++++++++++++++++++++++++++++++++++++++++++
//
// This class built up from subclasses: Block
// - The Fingerprint Table can be described as
//	    block[1]  block[2]  block[3]  block[4]  block[5]  block[6] ...
// 	  ------------------------------------------------------------
//  1 |  RSSI  ||  RSSI  ||  RSSI  ||  RSSI  ||  RSSI  ||  RSSI  |
//    ------------------------------------------------------------
//  2 |  RSSI  ||  RSSI  ||  RSSI  ||  RSSI  ||  RSSI  ||  RSSI  |
//    ------------------------------------------------------------
//  3 |  RSSI  ||  RSSI  ||  RSSI  ||  RSSI  ||  RSSI  ||  RSSI  |
//    ------------------------------------------------------------
//  4 |  RSSI  ||  RSSI  ||  RSSI  ||  RSSI  ||  RSSI  ||  RSSI  |
//    ------------------------------------------------------------

// - a column is a block 
// - 1,2,3,4... is the detector id used to index to the RSSI value of block
// - each cell in the table is the stats value of the RSSI LIST
//

	Block FingerPrintTable[];					// fingerprint table, of Block type
	type? length;								// number of block
	type? stats_mode;
	
// getset methods 			
	type? get_();
	type? set_();
// destructor			
	void init(stats_mode);						// fill the table with stats data

// add block pattern to fingerprint table //			
	type? add();

// remove element method //
	type? remove(element);
	
// LOCATOR ENGINE //
// input to locator:
// + an unDeterminedBlock at a certain time
//	 unDeterminedBlock: block where a location-to-be-determined tag is inside.
// 	 This block has block_xy undetermined
// + fingerprint table, that must be initialized (calibrated before used)

// algorithm:
//   	for each location in FP table
//    		 EUC_DIS.append(sqrt(sum of(RSSI_request - RSSI_pattern)^2))
// 		return location of (min(EUC_DIS))
// example usage: 
//	+ init the finger print table
//  + call locator when you have an unDeterminedBlock to pass
			
	type? locator(Block unDeterminedBlock);	
	
// update/recalibrate fingerprint with the new rssi list
// This method use block properties to update database
// example usage: 
//	+ init the finger print table
//  + call locator when you have a unDeterminedBlock to pass
//	+ pass the justDeterminedBlock to this function
	update(Block justDeterminedBlock);
	
// destructor	
//++++++++++++++++++++++++++++++++++++++++++++ end_FingerPrint ++++++++++++++++++++++++++++++++++++++++++++ 
}
// underconstruction notes
// comments go here:
// 1.