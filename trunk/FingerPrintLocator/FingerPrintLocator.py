# +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ #
# Demo code with fingerprint-Euclidean distance method for locating asset tag   #
# by Dung Dang Le('Lee')                                                        #
# This code is licensed under GPLv.3                                            #
# The program does the following things:                                        #
# - get data from serial port                                                   #
# - calibrating: data saved to text                                             #
# - fill fingerprint table and predict tag location                             #
# - the fingerprint table supports several stats mode for easy experimenting    #
# +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ #

# Datatypes for fingerprint
# 
#  D1__ __ __D2
#   |__|__|__|
#   |__|_i|__|
#   |__|__|__|
#  D3        D4
#
# - For scalability requirement, the system devided into cells, normally
#   4 detectors per cells (but actually we do not care about detector locations in the 
#   fingerprint method). Each cell has a number of unit blocks that is 
#   atomic. These blocks are indexed by a location number. This number
#   along with its cell number will be used to index to a real location (x,y)
# - Beside scalability, cells can be useful for hybrid locating approach, that is
#   pin point a tag location by first determine the cell it belongs to and 
#   then the specifc block, which can be done by RSSI-to-distance and fingerprint
#   method. 
# - The prototype can have as simple as one to two cells
# - A block can be anywhere inside the cell, as long as it is matched with the 
#   calibration specs. blocks are numbered and listed {blk1,blk2...}. normally, 
#   cell is divided as block matrix as above, but in general blocks are just region in 
#   cell and can have any shape, again as long as it is matched with the calibrations. 
#   the fingerprint method only tells whether a tag is inside a block, which location is known
# - Block_type content includes 
#   + Block number
#   + A set of its RSSI and corresponding detector {RSSI_x,...}
#   This set have fixed N elements (total number of detectors) defined as
#   RSSI_type (RSSI,D_x).  set elements can be zeros (0,D_i) if detector is 
#   unreachable from a tag inside the block.  
# Note: some of these ideas may be obsoleted, for e.g, the cell is actually a subpart of 
# the fingerprint table


import serial,time,struct,array,sys,math
DETECTOR_NUM = 6
BLOCK_NUM = 16
TAG_NUM = 7
THRESHOLD = 4	# set this to the number of detector we want to use when calculating Eucledean distance
				# This threshold should never be larger number of detector available or the locator will not work

#++++++++++++++++++++++++++++++++++++++++++++ rssi_stat ++++++++++++++++++++++++++++++++++++++++++++++++++++
# This class stores RSSI data in a list, and support stats calculations when need
class rssi_stat:
	def __init__(self):
		self.statlist = []							# list of RSSI values recorded, empty, int type
													# the first element will be replaced with the stats mode
		self.length = 0								# keep track of list length
		self.accumulator = 0						# sum of list values

# method produces mean of current list #	
	def mean(self):							
		#print 'sum', self.accumulator, 'length', len(self.statlist), 'count', self.length
		return self.accumulator/(len(self.statlist))
	def maximum(self):
		return max(self.statlist)
	def median(self):
		print 'hehe'		
# method to add a value to list #
	def add_rssi(self,rssi):
		(self.statlist).append(rssi)
		self.length = self.length + 1
		self.accumulator = self.accumulator + rssi
# method to return a specific RSSI value #
	def get_rssi(self,loc):
		return self.statlist[loc]
# method to return the list of RSSI #
	def get_statlist(self):
		return self.statlist
# method to return the length of the listl
	def get_length(self):
		return self.length
#++++++++++++++++++++++++++++++++++++++++++++ end_rssi_stat ++++++++++++++++++++++++++++++++++++++++++++


#++++++++++++++++++++++++++++++++++++++++++++ Block ++++++++++++++++++++++++++++++++++++++++++++ 
# - Block is basic unit of the system, holding the RSSI pattern at the associated location
# - RSSI pattern is the array of RSSIs from all detectors, measured from a specific tag that
#   placed inside the block. Each element of the array show the RSSI from one detector.
# - The array should have DETECTOR_NUM elements (fixed), but they does not need to be all valid
# - Note that the block only show pattern that is associated with a specific tag only. We are
#   not assumimg all tags should have the same pattern (because of hardware characteristics)
#   but we are trying to achieve that.
# - in locating mode, when the array is full or timeout(some seconds) of meet the threshold
#   the block will be ready to send to locator to determine its location. 
# - this class can be used for both calibrating and locating process : 
#   + in calibrating mode, the block has known location and rssi pattern
#   + in locating mode, the block only holds the pattern getting from incomming request, its location
#     will be determined by the locator engine

class Block:

	def __init__(self):
		self.block_xy = [0,0]					# block coordiates
		self.block_number = 0					# block numeric name
		self.tagID = 0							# associate tag id when calibrate
		self.RSSI_pattern = []				  	# array of RSSIs from all detectors
												# elements of this array are of type rssi_stat
												# index to the array is also the detectorID
		for n in range(DETECTOR_NUM+1):			# init block pattern by rssi_stat type
			(self.RSSI_pattern).append(rssi_stat())
		#self.location_known = False
		
# this method returns the pattern list for accessing RSSI stats			
	def get_pattern_list(self,det_num):
		return self.RSSI_pattern[det_num]
# destructor
	def __del__(self):
		class_name = self.__class__.__name__
		print class_name, 'destroyed'

# this method writes Block pattern to file
	def write_to_file(self,filename):
		fo = open(filename, 'a')			
		# metadata
		fo.write('block_number,' + str(self.block_number) + '\n')	#	',' is dilimiter
		fo.write('block_xy,' + str(self.block_xy[0]) + ',' + str(self.block_xy[1]) + '\n')
		fo.write('calibrate_tagID,' + str(self.tagID) + '\n')

		# pattern list, format: detector_i, rssi, rssi .... 
		for n in range(DETECTOR_NUM+1):
			rssi_list = self.get_pattern_list(n)					# list associate with detector id
			if rssi_list.get_length() > 0 :							# if list is valid (actually has something)
				fo.write('detector,' + str(n))						# write 
				for m in range(rssi_list.get_length()):
					fo.write(',' + str(rssi_list.get_rssi(m)))
				fo.write('\n')
		fo.write('$,end\n')											# block delimitter
		fo.close()		
		
# this method will tell whether the block (which location is unknown, because used in locating mode) is 
# ready to be sent out to locator for determining it location
	def ready(self):
		countlist = 0									
		for n in range(DETECTOR_NUM+1):
			rssi_list1 = self.get_pattern_list(n)		# list associate with detector id
			if rssi_list1.get_length() > 0 :			# if list is valid (actually has something)
				countlist = countlist + 1
				#print countlist				
				if countlist >= THRESHOLD:				# a block needs enough amount of RSSI before sent to locator
					return True			
		return 	False	
#++++++++++++++++++++++++++++++++++++++++++++ end_Block ++++++++++++++++++++++++++++++++++++++++++++ 


#++++++++++++++++++++++++++++++++++++++++++++ Fingerprint ++++++++++++++++++++++++++++++++++++++++++++++
# This is main class of the program that provides storing, retrieving and processing (locating) data
# it is built up from other subclasses: Block, rssi_stat and has the locator engine
# - The Fingerprint Table can be described as
#	  block[1]  block[2]  block[3]  block[4]  block[5]  block[6]
# 	 ------------------------------------------------------------
#  1 |  RSSI  ||  RSSI  ||  RSSI  ||  RSSI  ||  RSSI  ||  RSSI  |
#    ------------------------------------------------------------
#  2 |  RSSI  ||  RSSI  ||  RSSI  ||  RSSI  ||  RSSI  ||  RSSI  |
#    ------------------------------------------------------------
#  3 |  RSSI  ||  RSSI  ||  RSSI  ||  RSSI  ||  RSSI  ||  RSSI  |
#    ------------------------------------------------------------
#  4 |  RSSI  ||  RSSI  ||  RSSI  ||  RSSI  ||  RSSI  ||  RSSI  |
#    ------------------------------------------------------------
# - column is block rssi pattern
# - 1,2,3,4... is the detector id used to index to the RSSI value of block
# - although shown as one value, each cell in the table is an array of RSSIs,
#   the number (RSSI)represented in the table (also the first element of the
#   array) is the stats value 
class FingerPrint:
	def __init__(self,fl,stats_mode):
		self.BlockArray = [] #[BLOCKNUM,Block]		# fingerprint table, of Block type, empty	 
		self.count = 0								# number of block
		#self.stats_mode = 'mean'
		self.TagInUnknownBlock = []					# This is the array thats hold location-to-be-determined
													# block where a tag is inside the block
													# Element indexed by tag ID  
													# After the tag/block location is determine, result will
													# be produced and the block will be removed and replaced		
		for n in range(TAG_NUM+1):	
			(self.TagInUnknownBlock).append(Block())# initialize with Block type
			
		# fill data on file to the fingerprint table #
		fp = open(fl, 'r')							# open fingeprint patterns data on text
		dataReady = False
		for line in fp:								# process line by line
			print line
			if not(dataReady):
				blk = Block()						# prepare temporary block
				dataReady = True	
			alist = line.split(',')					# contents splited by ','
			# start parsing data #
			if alist[0] == 'block_number':			
				blk.block_number = int(alist[1])
			elif alist[0] == 'block_xy':
				blk.block_xy[0] = int(alist[1])
				blk.block_xy[1] = int(alist[2])
			elif alist[0] == 'calibrate_tagID':
				blk.tagID = int(alist[1])
			elif alist[0] == 'detector':
				for p in range(len(alist)-2):		# not include the first two elements
					(blk.get_pattern_list(int(alist[1]))).add_rssi(int(alist[p+2]))				
			elif alist[0] == '$':					# end block
				self.add(blk)						# add block to fingerprint table
				self.count = self.count + 1
				blk.write_to_file('test.cvs')		# test block content after parsing
				del blk
				dataReady = False
		fp.close()
		self.fill_stats(stats_mode)					# fill the table with stats data for locating
		print 'FP created with stats mode:', stats_mode
		# end initialize #

	# add block pattern to fingerprint table #			
	def add(self, block):
		self.BlockArray.append(block)
		self.count = self.count + 1
	#def update(self,block,loc):
	#	self.BlockArray[loc] =  block
	
	# remove element method #
	def remove(self,loc):
		del self.BlockArray[loc]
		self.count =  self.count - 1

	# will return block index of any UnknownBlock that is ready to be processed #
	def ready_to_go(self):					
		for n in range(TAG_NUM+1):
			if(self.TagInUnknownBlock[n].ready()):
				return n
			#else:
			#	print 'block',n,'not ready'
		return -1

	# fill the first element of the statlist with the calculating stats	#
	def fill_stats(self,stm):
		if stm == 'mean':
			for b in self.BlockArray:
				for d in b.RSSI_pattern	:			
					if d.get_length() > 0:
						d.statlist[0] = d.mean()
				#test block content after filled will stat value
				b.write_to_file('test_stat.cvs')	
		elif stm == 'max':
			for b in self.BlockArray:
				for d in b.RSSI_pattern	:			
					if d.get_length() > 0:
						d.statlist[0] = d.max()

# LOCATOR ENGINE #
# input to locator:
# + a unknown Block at a certain time, locator may have FIFO buffer 
#   for handle incomming requests
# + fingerprint table, that must be initialized (calibrated before used)
# algorithm:
#   for each location in FP table
#     EUC_DIS.append(sqrt(sum of(RSSI_request - RSSI_pattern)^2))
#   return location of (min(EUC_DIS))
	def locator(self,tag_id,transac):
		print 'locating...'
		ED_list = []			# Euclidean Distance list, minimum is our prediction
		block_index = []		# index to the EU_list
		for bb in self.BlockArray:
			eu_sum = 0
			for dd in range(DETECTOR_NUM+1):			
				rssi_list3 = (self.TagInUnknownBlock[tag_id]).get_pattern_list(dd)
				if rssi_list3.get_length() > 0 and (bb.get_pattern_list(dd)).get_length()>0:
					eu_sum = eu_sum + math.pow((rssi_list3.get_rssi(0) - (bb.get_pattern_list(dd)).get_rssi(0)),2)
			ED_list.append(math.sqrt(eu_sum))			# do the sqrt and append to euclidean list
			block_index.append(bb.block_number)			# append to block index list

		print '++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++'		
		print ED_list
		print block_index
		location = block_index[ED_list.index(min(ED_list))]
		print 'tag location at block', location		
		print '++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++'		
		toBA = 	block_index.index(location)	
		fout = open('location.txt', 'w')				# for now , one tag location only will be send to file 
		fout.write('9999-99-99 99-99-99' + ',' + str(tag_id) + ',' + str(self.BlockArray[toBA].block_xy[0]) + ',' + str(self.BlockArray[toBA].block_xy[1])+ ','+ str(transac.reserved))	
		fout.close()
		return [block_index,ED_list]
	# destructor	
	def __del__(self):
		class_name = self.__class__.__name__
		print class_name, 'destroyed'
#++++++++++++++++++++++++++++++++++++++++++++ end_FingerPrint ++++++++++++++++++++++++++++++++++++++++++++ 


#++++++++++++++++++++++++++++++++++++++++++++ transaction ++++++++++++++++++++++++++++++++++++++++++++ 
# - this class holds the current transaction, that is 7 bytes comming out from proxy
class transaction:
	def __init__(self):
		self.sourceID = 0
		self.detID = 0
		self.RSSI = 0				
		self.tagID = 0
		self.msgID = 0 
		#self.msgID_last	= 0			
		self.reserved = 0
		#self.ready = Falseif(serial_con.isOpen()):			# check serial port
		#self.valid = False
		
	# destructor
	def __del__(self):
		class_name = self.__class__.__name__
		print class_name, 'destroyed'
		
	# parse the incomming data from proxy to this class
	def get_transaction_data(self,serial_con):
		byte_count = 0				# we need 7 bytes
		OK = False					# check for valid packet (start with a '$')
		valid_data = True			# check for validity of data
		done = False				# check for completeness
		
		while serial_con.inWaiting() or not(done):		# check for incomming data
			byte_count = byte_count + 1;				# count variable, we need 7 bytes total
			tmp = serial_con.read()						# get byte out of buffer
							
			if(tmp == '$'):								# check delimiter
				OK = True	
				print 'data available'								
			if OK:										# collect RSSI pattern from serial port
				if byte_count == 2:
					self.sourceID = struct.unpack('B',tmp)[0]
					print 'sourceID ', self.sourceID,
									
				elif byte_count == 3:		
					self.detID = struct.unpack('B',tmp)[0]						
					print 'detID ', self.detID,
					if self.detID > DETECTOR_NUM or self.detID < 0:
						print 'invalid data'
						valid_data = False
									
				elif byte_count == 4:
					self.RSSI = struct.unpack('B',tmp)[0]
					print 'RSSI ', self.RSSI,
					if self.RSSI <= 0:
						print 'invalid data'
						valid_data = False
								
				elif byte_count == 5:
					self.tagID = struct.unpack('B',tmp)[0]
					print 'TagID ', self.tagID,
					if self.tagID > TAG_NUM or self.tagID < 0:
						print 'invalid data'
						valid_data = False
					
				elif byte_count == 6:
					self.msgID = struct.unpack('B',tmp)[0]
					print 'msgID ', self.msgID,

				elif byte_count == 7:
					self.reserved = struct.unpack('B',tmp)[0]
					print 'reserved ', self.reserved 	
					byte_count = 0									
					done = True		
					#break 			# end reading one packet
		# end while loop
		if done and valid_data: 
			return True 	# transaction read succesfully
		else:
			return False	# should ignore this transaction
#++++++++++++++++++++++++++++++++++++++++++++ end_transaction ++++++++++++++++++++++++++++++++++++++++++++ 


# ++++++++++++++++++++++++++++++++++++++++++++ main section ++++++++++++++++++++++++++++++++++++++++++++
# serial input
while True:
	try:
		ser = serial.Serial('/dev/ttyUSB0',19200)	# change this to COM port in windows
		ser.close()									# temporary close for now
		print 'serial connection okay'
		break
	except serial.SerialException:
		print 'error:', sys.exc_info()[1]
		raw_input()			
#ser.timeout = 4							# default 3 secs
#print 'isOpen:', ser.isOpen()
#ind = 0
#res = ''
#mystat = rssi_stat()

#++++++++++++++++++++++++++++++++++++++++++++ main loop ++++++++++++++++++++++++++++++++++++++++++++ 
while True:
	try:
		print 'cal: calibration, loc: locating, q: quit'
		print '>',
		cmd = raw_input()				
		####################################### CALIBRATION MODE ####################################################
		if cmd == 'cal':								# calibration mode,for now in this mode, only one tag is used
			aBlock = Block()							# creat a block instance
			
			print 'block location number: ',
			aBlock.block_number = int(raw_input())		# init block location number
			
			print 'block x coordinate: ',
			aBlock.block_xy[0] = int(raw_input())		# init block xy
			
			print 'block y coordinate: ',
			aBlock.block_xy[1] = int(raw_input())		# init block xy
						
			#aBlock.location_known = True			
			
			print 'tag ID:',
			aBlock.tagID = int(raw_input())				# get calibrating tag
			print aBlock.tagID			
		
			trans = transaction()						# a transaction for getting incomming data
			ser.open()									# reopen serial port
  			while True:	
				try:				
					if(ser.isOpen()):														# check serial port
						if trans.get_transaction_data(ser) == True:							# read succesfully
							if trans.tagID == aBlock.tagID and trans.RSSI <> 0:				# recording	block pattern
								(aBlock.get_pattern_list(trans.detID)).add_rssi(trans.RSSI)	# concat rssi to list
								print 'added'	
							else:
								print '!warning : tagID doesnot match or zero RSSI'	
						# weird thing used to happens if we put these here: :|						
						else:
							print 'transaction fail '
								
					else:
						print '!no connection'						
						break

				except KeyboardInterrupt:				# stop the running mode by Ctrl-C
					#print 'ocacola^'  , 'blocknum', aBlock.block_number, 'tag' , aBlock.tagID
					#for x in (aBlock.get_pattern_list(1)).get_statlist():
					#	print x
					#print 'mean', (aBlock.get_pattern_list(1)).mean()
					print 'save to file?(y/n)',			# store data to file
					yn = '' 
					while (yn <> 'y' and yn <> 'n'):
						yn = raw_input()
					if yn == 'y':			
						print 'filename: ', 
						fn = ''
						while (len(fn) == 0):
							fn = raw_input()
						aBlock.write_to_file(fn)
						print 'saved!'
					else:
						print '!warning: data not saved '		
					del aBlock
					break
				
				except serial.SerialException:
					print 'error:', sys.exc_info()[1]
					break
			ser.close() 
			del trans
		############################################ LOCATING MODE ################################################### 
		elif cmd == 'loc':
			print 'fingerprint file name?', 		#
			fn2 = ''								
			while (len(fn2) == 0):
				fn2 = raw_input()
			try:
				open(fn2,'r')						# attemp to open the file that contains fingerprint pattern 
			except IOError:
				print 'error: cannot open',fn2
				continue
													# read in finger print table
			FP = FingerPrint(fn2,'mean')			# fingerPrint table that will be used for locator, use (stats) mean data in the table
			trans2 = transaction()					# a transaction for getting incomming data

		# THOUGHT: 1.we could spawn two threads: one collects requests put on FIFO, one reads the FIFO and does locating
		# for now just do it one by one, incomming request can be lost if locator is not available, starvation possible
		# 2. it is possible to look at the fingerprint to tell whether two ( or more) locations can cause aliasing
		# 3.		
			ser.open()														# reopen serial port
			ready_block = -1
			while True:														# repeat until keyboard interrupt
				try:
					ready_block = FP.ready_to_go()
		  			if ready_block <> -1:									# check if a (unknown location) block is ready to go...
						#print ready_block									# if so, send to locator
						FP.locator(ready_block,trans2)						# core algorithm, block after has its location
																			# determined will be removed
						#del FP.TagInUnknownBlock[ready_block]					
						FP.TagInUnknownBlock[ready_block] = Block()			# dereferenced, auto destroy
						#print 'after block destroy'	
						ready_block = -1									# reset ready status, too
																			# else,
					else:													# gathering data until ready to be determined
						if(ser.isOpen()):									# check serial port
							if trans2.get_transaction_data(ser) == True:	# read succesfully							
								if trans2.RSSI <> 0:						# recording	request
									((FP.TagInUnknownBlock[trans2.tagID]).get_pattern_list(trans2.detID)).add_rssi(trans2.RSSI)	# concat rssi to list
									(FP.TagInUnknownBlock[trans2.tagID]).tagID = trans2.tagID									
									print 'added'	
								else:
									print '!warning : zero RSSI'	
							else:
								print 'transaction fail '
								
						else:
							print '!no connection'						
							break

				except KeyboardInterrupt:
					break

				except serial.SerialException:
					print 'error:', sys.exc_info()[1]
					break
			ser.close()
		####### EXIT #################################################################################			
		elif cmd == 'q':
			break	
	except KeyboardInterrupt:
		break

	except serial.SerialException:
		print 'error:', sys.exc_info()[1]
		break
	except:
		print 'error:', sys.exc_info()[1]
		raw_input()	
ser.close()


