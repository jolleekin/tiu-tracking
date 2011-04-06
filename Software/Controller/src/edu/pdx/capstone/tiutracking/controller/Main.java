/* Controller
 * This application fills the role as mediator between 
 * the Proxy, LocationEngine, and Database.
 * 
 * Key Aspects:
 * 	Three modes of operation
 * 		1) Collect; Saves data coming from proxy into a csv file
 * 		2) Calibrate; Saves data coming from proxy into Database.
 * 		3) Locate; Provides previously collected calibration data to location engines.
 * 			Formats data coming from proxy into a format suitable for a Location Engine to
 * 			calculate a location.
 * 
 * Architecture:
 * 	The GUI has 3 buttons; Collect, Calibrate, and Locate. Each has their own event handler.
 * 	There are 3 runnable thread objects; CollectReader, CalibrateReader, and LocateReader. They are responsible for
 *  reading data from a Port, and providing the functionality that is associated with each mode of operation. Each of the 3 buttons starts their 
 *  respective Reader. Each Reader inherits from the PortReader class. The PortReader class provides base 
 *  functionality for reading and parsing incoming data from a proxy.
 *  
 *  CalibrateReader, and LocateReader access a Database.
 * 
 *  Authors: Daniel Ferguson,
 *  Version: 0.9
 */

package edu.pdx.capstone.tiutracking.controller;
import edu.pdx.capstone.tiutracking.common.*;
import edu.pdx.capstone.tiutracking.locator.FingerPrint;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import gnu.io.*;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

import javax.swing.JComboBox;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JPanel;
import java.awt.Color;
import javax.swing.border.TitledBorder;
import javax.swing.UIManager;

import java.sql.*;

public class Main {

	private enum AppViewMode{
		CALIBRATION,
		COLLECTION,
		LOCATING
	}
	
	private AppViewMode appViewMode;
	private JFrame frame;
	private JComboBox cbxCOMPorts;
	private JTextArea txtOutput;
	
	private CommPort commPort;
	private SerialPort serialPort;
	
	Thread writerThread;
	Thread readerThread;
	LocatorReader locatorReader;
	CollectorReader collectorReader;
	CalibratorReader calibratorReader;
	Writer writer;
	private JTextField txtLocationDescription;
	private JLabel lblPreviousLocationDescription;
	private JButton btnStartCalibrating;
	private JButton btnStartCollecting;
	private JButton btnStartLocating;
	private JTextField txtCalibrateX;
	private JTextField txtCalibrateY;
	private JTextField txtCalibrateTagID;
	private JTextField txtCalibrateBlockNumber;
	private JLabel lblNewLabel;
	private JLabel lblNewLabel_1;
	private JLabel lblNewLabel_2;
	private JLabel lblNewLabel_3;
	private JPanel pnlLocating;
	private JPanel panel_3;
	private JScrollPane scrollPane;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Main window = new Main();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Main() {
		//GUI init
		initialize();

		//member variable init
		appViewMode = AppViewMode.LOCATING;
		
		//Populate combo box with available serial ports.
		@SuppressWarnings("unchecked")
		Enumeration<CommPortIdentifier> ports = CommPortIdentifier.getPortIdentifiers();
		while (ports.hasMoreElements()){
			CommPortIdentifier port = ports.nextElement();	
			cbxCOMPorts.addItem(port.getName());
		}	
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 794, 520);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 8, 553, 443);
		frame.getContentPane().add(scrollPane);
		
		txtOutput = new JTextArea();
		scrollPane.setViewportView(txtOutput);
		
		JPanel pnlCalibrating = new JPanel();
		pnlCalibrating.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Calibrate", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		pnlCalibrating.setBounds(573, 179, 195, 202);
		frame.getContentPane().add(pnlCalibrating);
		pnlCalibrating.setLayout(null);
		
		lblNewLabel = new JLabel("X");
		lblNewLabel.setBounds(10, 27, 46, 14);
		pnlCalibrating.add(lblNewLabel);
		
		lblNewLabel_1 = new JLabel("Y");
		lblNewLabel_1.setBounds(98, 27, 46, 14);
		pnlCalibrating.add(lblNewLabel_1);
		
		txtCalibrateY = new JTextField();
		txtCalibrateY.setBounds(98, 40, 86, 20);
		pnlCalibrating.add(txtCalibrateY);
		txtCalibrateY.setColumns(10);
		
		txtCalibrateX = new JTextField();
		txtCalibrateX.setBounds(10, 40, 86, 20);
		pnlCalibrating.add(txtCalibrateX);
		txtCalibrateX.setColumns(10);
		
		lblNewLabel_2 = new JLabel("TagID");
		lblNewLabel_2.setBounds(10, 71, 46, 14);
		pnlCalibrating.add(lblNewLabel_2);
		
		txtCalibrateTagID = new JTextField();
		txtCalibrateTagID.setBounds(10, 84, 174, 20);
		pnlCalibrating.add(txtCalibrateTagID);
		txtCalibrateTagID.setColumns(10);
		
		lblNewLabel_3 = new JLabel("Block Number");
		lblNewLabel_3.setBounds(10, 112, 102, 14);
		pnlCalibrating.add(lblNewLabel_3);
		
		txtCalibrateBlockNumber = new JTextField();
		txtCalibrateBlockNumber.setBounds(10, 127, 174, 20);
		pnlCalibrating.add(txtCalibrateBlockNumber);
		txtCalibrateBlockNumber.setColumns(10);
		
		btnStartCalibrating = new JButton("Start Calibrating");
		btnStartCalibrating.setBounds(10, 160, 174, 31);
		pnlCalibrating.add(btnStartCalibrating);
		btnStartCalibrating.setToolTipText("Saves collected data into the database");
		
		JPanel pnlCollecting = new JPanel();
		pnlCollecting.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Collect", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		pnlCollecting.setBounds(573, 59, 195, 123);
		frame.getContentPane().add(pnlCollecting);
		pnlCollecting.setLayout(null);
		
		btnStartCollecting = new JButton("Start Collecting");
		btnStartCollecting.setBounds(11, 82, 173, 31);
		pnlCollecting.add(btnStartCollecting);
		btnStartCollecting.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {					
					if (btnStartCollecting.getText() == "Start Collecting"){
						if (txtLocationDescription.getText().length() < 2){
							JOptionPane.showMessageDialog(null,"Enter Location Description!!");
							return;
						}
						String selectedPortName = cbxCOMPorts.getItemAt(cbxCOMPorts.getSelectedIndex()).toString();
						CommPortIdentifier port = CommPortIdentifier.getPortIdentifier(selectedPortName);
						commPort = port.open("Controller",2000);
						if (commPort instanceof SerialPort){
							serialPort = (SerialPort) commPort;
							serialPort.setSerialPortParams(19200, 
														   SerialPort.DATABITS_8, 
														   SerialPort.STOPBITS_1, 
														   SerialPort.PARITY_NONE);
							InputStream in = serialPort.getInputStream();
							OutputStream out = serialPort.getOutputStream();
							collectorReader = new CollectorReader(in);
							writer = new Writer(out);
							writerThread = new Thread(collectorReader);
							readerThread = new Thread(writer);
							writerThread.start();
							readerThread.start();							
							
							btnStartCollecting.setText("Stop Collecting");
						}
					}else if (btnStartCollecting.getText() == "Stop Collecting"){
						writer.stop();
						collectorReader.requestStop();
						writerThread.join();
						readerThread.join();
						serialPort.close();
						btnStartCollecting.setText("Start Collecting");
					}
				} catch (NoSuchPortException e) {
					//Caused by CommPortIdentifier.getPortIdentifier
					e.printStackTrace();
				} catch (PortInUseException e) {
					//Caused by port.open
					e.printStackTrace();
				} catch (UnsupportedCommOperationException e) {
					//Caused by serialPort.setSerialPortParams
					e.printStackTrace();
				} catch (IOException e) {
					//Caused by serialPort.getInputStream() or serialPort.getOutputStream()
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		btnStartCollecting.setToolTipText("Saves collected data into a file");
		
		JLabel lblLocationDescription = new JLabel("Output File Description");
		lblLocationDescription.setBounds(10, 23, 123, 16);
		pnlCollecting.add(lblLocationDescription);
		
		txtLocationDescription = new JTextField();
		txtLocationDescription.setBounds(10, 38, 173, 20);
		pnlCollecting.add(txtLocationDescription);
		txtLocationDescription.setColumns(10);
		
		lblPreviousLocationDescription = new JLabel("Previous...");
		lblPreviousLocationDescription.setBounds(11, 55, 177, 16);
		pnlCollecting.add(lblPreviousLocationDescription);
		
		pnlLocating = new JPanel();
		pnlLocating.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Locate", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		pnlLocating.setBounds(573, 379, 195, 72);
		frame.getContentPane().add(pnlLocating);
		pnlLocating.setLayout(null);
		
		btnStartLocating = new JButton("Start Locating");
		btnStartLocating.setBounds(10, 30, 173, 31);
		pnlLocating.add(btnStartLocating);
		btnStartLocating.setToolTipText("Passes collected data to the Locating engine");
		
		panel_3 = new JPanel();
		panel_3.setBorder(new TitledBorder(null, "Serial Port", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_3.setBounds(573, 8, 195, 54);
		frame.getContentPane().add(panel_3);
		panel_3.setLayout(null);
		
		cbxCOMPorts = new JComboBox();
		cbxCOMPorts.setBounds(10, 23, 173, 20);
		panel_3.add(cbxCOMPorts);
		btnStartLocating.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {				
				try {					
					if (btnStartLocating.getText() == "Start Locating"){						
						String selectedPortName = cbxCOMPorts.getItemAt(cbxCOMPorts.getSelectedIndex()).toString();
						CommPortIdentifier port = CommPortIdentifier.getPortIdentifier(selectedPortName);
						commPort = port.open("Controller",2000);
						if (commPort instanceof SerialPort){
							serialPort = (SerialPort) commPort;
							serialPort.setSerialPortParams(19200, 
														   SerialPort.DATABITS_8, 
														   SerialPort.STOPBITS_1, 
														   SerialPort.PARITY_NONE);
							InputStream in = serialPort.getInputStream();
							OutputStream out = serialPort.getOutputStream();
							locatorReader = new LocatorReader(in);
							writer = new Writer(out);
							writerThread = new Thread(locatorReader);
							readerThread = new Thread(writer);
							writerThread.start();
							readerThread.start();
							
							
							btnStartLocating.setText("Stop Locating");
						} 
					}else if (btnStartLocating.getText() == "Stop Locating"){
						writer.stop();
						locatorReader.requestStop();
						writerThread.join();
						readerThread.join();
						serialPort.close();
						btnStartLocating.setText("Start Locating");
					}
				} catch (NoSuchPortException e) {
					//Caused by CommPortIdentifier.getPortIdentifier
					e.printStackTrace();
				} catch (PortInUseException e) {
					//Caused by port.open
					e.printStackTrace();
				} catch (UnsupportedCommOperationException e) {
					//Caused by serialPort.setSerialPortParams
					e.printStackTrace();
				} catch (IOException e) {
					//Caused by serialPort.getInputStream() or serialPort.getOutputStream()
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		btnStartCalibrating.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {					
					if (btnStartCalibrating.getText() == "Start Calibrating"){
						
						float x = Float.parseFloat(txtCalibrateX.getText());
						float y = Float.parseFloat(txtCalibrateY.getText());
						int blockNumber = Integer.parseInt(txtCalibrateBlockNumber.getText());
						int tagID = Integer.parseInt(txtCalibrateTagID.getText());
						
						String selectedPortName = cbxCOMPorts.getItemAt(cbxCOMPorts.getSelectedIndex()).toString();
						CommPortIdentifier port = CommPortIdentifier.getPortIdentifier(selectedPortName);
						commPort = port.open("Controller",2000);
						if (commPort instanceof SerialPort){
							serialPort = (SerialPort) commPort;
							serialPort.setSerialPortParams(19200, 
														   SerialPort.DATABITS_8, 
														   SerialPort.STOPBITS_1, 
														   SerialPort.PARITY_NONE);
							InputStream in = serialPort.getInputStream();
							OutputStream out = serialPort.getOutputStream();
							calibratorReader = new CalibratorReader(in);
							writer = new Writer(out);
							writerThread = new Thread(calibratorReader);
							readerThread = new Thread(writer);
							writerThread.start();
							readerThread.start();
							
							
							btnStartCalibrating.setText("Stop Calibrating");
						}
					}else if (btnStartCalibrating.getText() == "Stop Calibrating"){
						writer.stop();
						calibratorReader.requestStop();
						writerThread.join();
						readerThread.join();
						serialPort.close();
						btnStartCalibrating.setText("Start Calibrating");
					}
				} catch (NoSuchPortException e) {
					//Caused by CommPortIdentifier.getPortIdentifier
					e.printStackTrace();
				} catch (PortInUseException e) {
					//Caused by port.open
					e.printStackTrace();
				} catch (UnsupportedCommOperationException e) {
					//Caused by serialPort.setSerialPortParams
					e.printStackTrace();
				} catch (IOException e) {
					//Caused by serialPort.getInputStream() or serialPort.getOutputStream()
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		
		JMenu mnview = new JMenu("View");
		menuBar.add(mnview);
		
		JRadioButtonMenuItem rdbtnmntmNewRadioItem = new JRadioButtonMenuItem("Calibration");
		mnview.add(rdbtnmntmNewRadioItem);
		
		JRadioButtonMenuItem rdbtnmntmCollection = new JRadioButtonMenuItem("Collection");
		mnview.add(rdbtnmntmCollection);
		
		JRadioButtonMenuItem rdbtnmntmLocating = new JRadioButtonMenuItem("Locating");
		mnview.add(rdbtnmntmLocating);
	}
	
	public class Writer implements Runnable{
		private OutputStream out;
		private volatile boolean done;
		public Writer(OutputStream out){
			this.out = out;			
		}
		public void stop(){
			done=true;
		}
		public void run(){
			/*try{
				this.done = false;
				while (!done){
							
				}
			}catch (IOException e){
				e.printStackTrace();
			} catch (InterruptedException e) {
				throw new RuntimeException("Interrupted in writer", e);
			}*/
		}
	}
	
	
	public class PortReader{
		protected InputStream in;
		protected volatile boolean done;
		
		public PortReader(InputStream in){
			this.in = in;
		}
		public void requestStop(){
			done=true;
		}		
		
		
		protected String printRawSample(RawSample rawSample) {
			DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
			Date date = new Date();
			String time = dateFormat.format(date);			
			String displayString = String.format("%1$s, D:%2$d, S:%3$d, T:%4$d, RSSI:%5$d, MsgID: %6$d, Reserved: %7$d\n",time, rawSample.detectorId, rawSample.sourceId, rawSample.tagId, rawSample.rssi, rawSample.messageId, rawSample.reserved);			
			txtOutput.append(displayString);
			txtOutput.setCaretPosition(txtOutput.getDocument().getLength());
			return displayString;
		}
		protected void parseRawSample(ArrayList<Byte> list, RawSample newSample) {
			newSample.sourceId 		= list.get(0) & 0xff; 		list.remove(0);
			newSample.detectorId 	= list.get(0) & 0xff; 		list.remove(0);
			newSample.rssi 			= list.get(0) & 0xff;		list.remove(0);			
			newSample.tagId 		= list.get(0) & 0xff;		list.remove(0);
			newSample.messageId 	= list.get(0) & 0xff;		list.remove(0);
			newSample.reserved 		= list.get(0) & 0xff;		list.remove(0);			
			System.out.println("Parsing Sample From "+newSample.detectorId);
		}
		protected void readPort(ArrayList<Byte> list, int bufferSize)
				throws IOException {
			byte[] buffer = new byte[bufferSize];
			int bytesRead=0;					
			if (in.available() > 0){
				bytesRead = in.read(buffer,0, bufferSize);
				System.out.println("Read: " + bytesRead + " bytes");
				for (int k = 0;k < bytesRead;k++){
					list.add(buffer[k]);
				}
			}
		}
	}
	
	public class RawSample{
		public int detectorId;
		public int rssi;
		public int sourceId;
		public int tagId;
		public int messageId;
		public int reserved;	
	}
	
	public class CalibratorReader extends PortReader implements Runnable {
		Hashtable<Integer, Calendar> ttl = new Hashtable<Integer, Calendar>();
		Hashtable<Integer, ArrayList<String>> samples = new Hashtable<Integer,ArrayList<String>>();
		public CalibratorReader(InputStream in){
			super(in);
		}
		public void run() {
			try{				
				this.done=false;
				ArrayList<Byte> list = new ArrayList<Byte>();				
				DataPacket dataBlock = null;
				boolean firstSample=true;
				while (!done){		
					RawSample rawSample = new RawSample();
					int bufferSize = 6;
					readPort(list, bufferSize);					
					if (list.size() >= bufferSize){						
						parseRawSample(list, rawSample);						
						printRawSample(rawSample);						
						int targetTagId = Integer.parseInt(txtCalibrateTagID.getText());
						if (targetTagId == rawSample.tagId ){//Only calibrate for specified tag
							if (firstSample){
								int blockId = Integer.parseInt(txtCalibrateBlockNumber.getText());
								double x = Double.parseDouble(txtCalibrateX.getText());
								double y = Double.parseDouble(txtCalibrateY.getText());
								dataBlock = new DataPacket(blockId, rawSample.tagId, new Vector2D(x,y));
								firstSample = false;
							}							
							saveSample(dataBlock, rawSample);
						}
					}					
				}				
				storeCalibrationData(dataBlock);			
			}catch (IOException e){
				e.printStackTrace();
			}catch (RuntimeException e){
				e.printStackTrace();			
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		} 
		protected void saveSample(DataPacket dataPacket, RawSample rawSample) {
			//Hash incoming data based on detector ID
			ArrayList<Integer> samples = null;
			if (dataPacket.rssiTable.containsKey(rawSample.detectorId)){
				//If samples for this detectorID have already been received
				samples = dataPacket.rssiTable.get(rawSample.detectorId);
			}else{
				//If samples for this detectorID have NOT been received before.
				samples = new ArrayList<Integer>();
			}
			samples.add(rawSample.rssi);
			dataPacket.rssiTable.put(rawSample.detectorId, samples);
		}
		@SuppressWarnings("unchecked")
		private void storeCalibrationData(DataPacket dataPacket)
				throws ClassNotFoundException, SQLException, IOException {
			//TODO: make cal data directory and data file configurable.
			ArrayList<DataPacket> calibrationData=null;			
			File calFile = new File("CalibrationData\\calibrationdata.dat");
			if (calFile.exists()){				
				calibrationData = (ArrayList<DataPacket>)ObjectFiler.load("CalibrationData\\calibrationdata.dat");
			}else{
				calibrationData = new ArrayList<DataPacket>();
			}
			calibrationData.add(dataPacket);
			ObjectFiler.save("CalibrationData\\calibrationdata.dat", calibrationData);	
		}		
	}
	
	public class CollectorReader extends PortReader implements Runnable{
		Hashtable<Integer, Calendar> ttl = new Hashtable<Integer, Calendar>();
		Hashtable<Integer, ArrayList<String>> samples = new Hashtable<Integer,ArrayList<String>>();
		public CollectorReader(InputStream in){
			super(in);
		}
	
		public void run(){
			try{				
				this.done=false;
				ArrayList<Byte> list = new ArrayList<Byte>();
				FileWriter fw = new FileWriter(txtLocationDescription.getText()+"_data.csv");				
				while (!done){
					int bufferSize = 6;
					readPort(list,bufferSize);
					if (list.size() >= bufferSize){												
						RawSample rawSample = new RawSample();
						parseRawSample(list, rawSample);						
						String printString = printRawSample(rawSample);		
						fw.write(printString);
					}					
				}
				fw.close();
				lblPreviousLocationDescription.setText(txtLocationDescription.getText());
				txtLocationDescription.setText(null);			
			}catch (IOException e){
				e.printStackTrace();
			}catch (RuntimeException e){
				e.printStackTrace();			
			}
		}
	}

	public class LocatorReader  extends PortReader implements Runnable{		
		public LocatorReader(InputStream in){
			super(in);
		}		
		public void run(){
			try{				
				this.done=false;
				ArrayList<Byte> list = new ArrayList<Byte>();
				Hashtable<Integer, ArrayList<RawSample>> rawSampleTable = new Hashtable<Integer, ArrayList<RawSample>>();
				Hashtable<Integer, Calendar> ttl = new Hashtable<Integer, Calendar>();
				ArrayList<DataPacket> fingerPrintTable = new ArrayList<DataPacket>();				
				loadCalibrationData(fingerPrintTable);				
				FingerPrint locator = new FingerPrint(fingerPrintTable); 
				while (!done){				
					RawSample rawSample = new RawSample();
					int bufferSize = 6;
					readPort(list,bufferSize);
					if (list.size() >= bufferSize){						
						parseRawSample(list, rawSample);						
						//printSample(newSample);						
						int key = rawSample.tagId + rawSample.messageId;
						saveRawSample(key, rawSampleTable, rawSample);						
						//Start a new time window, and associate with key, if key has not been seen.
						if (!ttl.containsKey(key)){
							Calendar currentMoment = Calendar.getInstance();
							currentMoment.add(Calendar.SECOND, 2);
							ttl.put(key, currentMoment);						
						}
					}				
					ArrayList<Integer> expiredItems = new ArrayList<Integer>();
					//key = TagId + MsgId;
					for (Map.Entry<Integer, Calendar> e: ttl.entrySet()){
						Calendar savedMoment = e.getValue();
						if (savedMoment.before(Calendar.getInstance())){
							//Getting all RawSamples associated with TagId+MsgId key
							ArrayList<RawSample> rawSamples = rawSampleTable.get(e.getKey());
							//TODO: don't give a DataPacket to the locator, if less then N detectors are participating
							DataPacket dataPacket=null;
							boolean first=true;
							for (int s = 0;s < rawSamples.size();s++){
								if (first){									
									dataPacket = new DataPacket(-1,rawSamples.get(0).tagId,null);
									first =false;
								}
								ArrayList<Integer> rssiSingle =  new ArrayList<Integer>();
								rssiSingle.add(rawSamples.get(s).rssi);//Only one
								dataPacket.rssiTable.put(rawSamples.get(s).detectorId,rssiSingle );
							}

							locator.locate(dataPacket,StatisticMode.MEAN);
							String displayString = String.format("TagId=%1$d at (%2$f, %3$f), Block=%4$d\n", dataPacket.tagId, dataPacket.location.x, dataPacket.location.y, dataPacket.blockId);
							txtOutput.append(displayString);
							txtOutput.setCaretPosition(txtOutput.getDocument().getLength());
							//Expire Transaction
							expiredItems.add(e.getKey());							
						}
					}					
					//Remove expired items.
					for (Integer key:expiredItems){
						rawSampleTable.remove(key);
						ttl.remove(key);
					}
				}
			
			}catch (IOException e){
				e.printStackTrace();
			}catch (RuntimeException e){
				e.printStackTrace();
			
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		///saveRawSample - rawDataTable is index by TagId+MsgId
		protected void saveRawSample(int key, Hashtable<Integer, ArrayList<RawSample>> rawDataTable, RawSample rawSample) {
			//Hash incoming data based on key
			ArrayList<RawSample> samples = null;
			if (rawDataTable.containsKey(key)){
				//If samples for this key have already been received
				samples = rawDataTable.get(key);
			}else{
				//If samples for this key have NOT been received before.
				samples = new ArrayList<RawSample>();
			}
			samples.add(rawSample);
			rawDataTable.put(key, samples);
		}
		/*
		 * Loads an ArrayList<DataPacket> with calibration data.
		 * Returns: True if calibration data exists, False otherwise.
		 */
		@SuppressWarnings("unchecked")
		private boolean loadCalibrationData(ArrayList<DataPacket> fingerPrintTable)
				throws ClassNotFoundException, SQLException, IOException {
			File calFile = new File("CalibrationData\\calibrationdata.dat");
			if (!calFile.exists()){
				System.out.println("Calibration data not found.");
				return false;
			}						
			fingerPrintTable = (ArrayList<DataPacket>)ObjectFiler.load("CalibrationData\\calibrationdata.dat");			
			//Debug printing..
			System.out.println("FingerPrintTable:");
			for(DataPacket t: fingerPrintTable){
				System.out.println(String.format("TagID=%1$d, BlockNumber=%2$d",t.tagId, t.blockId));
				for (Map.Entry<Integer, ArrayList<Integer>> e: t.rssiTable.entrySet()){						
					System.out.print(String.format("DetectorID %1$d: ", e.getKey()));
					for (Integer rssi: e.getValue()){
						System.out.print(String.format("%1$d  ", rssi));
					}
				}
				System.out.println();
			}			
			return true;
		}
	}	
}
