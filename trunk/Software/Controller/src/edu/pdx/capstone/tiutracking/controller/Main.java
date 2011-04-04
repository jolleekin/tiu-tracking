package edu.pdx.capstone.tiutracking.controller;

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
import edu.pdx.capstone.tiutracking.common.*;
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
		
		txtOutput = new JTextArea();
		txtOutput.setBounds(10, 8, 553, 443);
		frame.getContentPane().add(txtOutput);
		
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
		protected void storeCalibrationData(
				Hashtable<Integer, ArrayList<Sample>> rssiData)
				throws ClassNotFoundException, SQLException {
			Connection connect;
			Statement statement;
			//Open a connection to a database					
			// This will load the MySQL driver, each DB has its own driver
			Class.forName("com.mysql.jdbc.Driver");
			// Setup the connection with the DB
			connect = DriverManager.getConnection("jdbc:mysql://db.cecs.pdx.edu/hoangman?" + 
			 "user=hoangman&password=c@p2011$#tT");
			// Statements allow to issue SQL queries to the database
			statement = connect.createStatement();	
			
			//Remove rows from CalibrateBlock and BlockDate where BlockNumber == User Specified BlockNumber 
			String query1 = String.format("delete from CalibrationBlock where BlockNumber=%1$s",txtCalibrateBlockNumber.getText());
			statement.executeUpdate(query1);
			query1 = String.format("delete from BlockData where BlockNumber=%1$s",txtCalibrateBlockNumber.getText());
			statement.executeUpdate(query1);
			
			for (Map.Entry<Integer, ArrayList<Sample>> e: rssiData.entrySet()){
				int detectorID = e.getKey();
				//Insert a new CalibrateBlock
				String query2 = String.format("insert into CalibrationBlock values(%1$d, %2$s, %3$s, %4$s);", detectorID, txtCalibrateBlockNumber.getText(), txtCalibrateX.getText(), txtCalibrateY.getText());
				System.out.println(query2);
				statement.executeUpdate(query2);
				for (Sample sample : e.getValue()){
					String query3 = String.format("insert into BlockData values(%1$d, %2$s, %3$d);",detectorID, txtCalibrateBlockNumber.getText(),sample.rssi );
					System.out.println(query3);
					statement.executeUpdate(query3);
				}
			}
			
			statement.close();
			connect.close();
		}
		protected void saveSample(int key, Hashtable<Integer, ArrayList<Sample>> rssiData,
				Sample newSample) {
			//Hash incoming data based on detector ID
			ArrayList<Sample> samples;
			if (rssiData.containsKey(key)){
				//If samples for this detectorID have already been received
				samples = rssiData.get(key);
			}else{
				//If samples for this detectorID have NOT been received before.
				samples = new ArrayList<Sample>();
			}
			samples.add(newSample);
			rssiData.put(key, samples);
		}
		protected String printSample(Sample newSample) {
			DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
			Date date = new Date();
			String time = dateFormat.format(date);				        
			
			String displayString = String.format("%1$s, D:%2$d, S:%3$d, T:%4$d, RSSI:%5$d, MsgID: %6$d, Reserved: %7$d\n",time, newSample.detectorID, newSample.sourceID, newSample.tagID, newSample.rssi, newSample.messageID, newSample.reserved);
			
			txtOutput.append(displayString);
			txtOutput.setCaretPosition(txtOutput.getDocument().getLength());
			return displayString;
		}
		protected void parseSample(ArrayList<Byte> list, Sample newSample) {
			newSample.sourceID 		= list.get(0) & 0xff; 		list.remove(0);
			newSample.detectorID 	= list.get(0) & 0xff; 		list.remove(0);
			newSample.rssi 			= list.get(0) & 0xff;		list.remove(0);			
			newSample.tagID 		= list.get(0) & 0xff;		list.remove(0);
			newSample.messageID 	= list.get(0) & 0xff;		list.remove(0);
			newSample.reserved 		= list.get(0) & 0xff;		list.remove(0);		

			
			System.out.println("Parsing Sample From "+newSample.detectorID);
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
	
	public class Sample
	{
		public int detectorID;
		public int rssi;
		public int sourceID;
		public int tagID;
		public int messageID;
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
				Hashtable<Integer, ArrayList<Sample>> rssiData = new Hashtable<Integer, ArrayList<Sample>>();				
						
				while (!done){		
					Sample newSample = new Sample();
					int bufferSize = 6;
					readPort(list, bufferSize);					
					if (list.size() >= bufferSize){						
						parseSample(list, newSample);						
						printSample(newSample);		
						int key = newSample.detectorID;
						saveSample(key, rssiData, newSample);
					}					
				}
				
				storeCalibrationData(rssiData);				
			
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
				Sample newSample = new Sample();
				while (!done){				

					int bufferSize = 6;
					readPort(list,bufferSize);
					if (list.size() >= bufferSize){												
						
						parseSample(list, newSample);						
						String printString = printSample(newSample);		
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
	
	public class BlockData{
		int detectorID;
		int blockNumber;
		int rssi;
	}
	
	public class LocatorReader  extends PortReader implements Runnable{

		
		public LocatorReader(InputStream in){
			super(in);
		}
		
		public void run(){
			try{				
				this.done=false;
				ArrayList<Byte> list = new ArrayList<Byte>();
				Hashtable<Integer, ArrayList<Sample>> rssiData = new Hashtable<Integer, ArrayList<Sample>>();
				Hashtable<Integer, Calendar> ttl = new Hashtable<Integer, Calendar>();

				Connection connect;
				//Open a connection to a database					
				// This will load the MySQL driver, each DB has its own driver
				Class.forName("com.mysql.jdbc.Driver");
				// Setup the connection with the DB
				connect = DriverManager.getConnection("jdbc:mysql://db.cecs.pdx.edu/hoangman?" + 
				 "user=hoangman&password=c@p2011$#tT");
				// Statements allow to issue SQL queries to the database
				Statement statementOuter = connect.createStatement();			
				String query = String.format("select * from CalibrationBlock;");
				ResultSet calBlocks = statementOuter.executeQuery(query);

				ArrayList<Transaction> fingerPrintTable = new ArrayList<Transaction>();
				while (calBlocks.next()){
					int detectorID = calBlocks.getInt("DetectorID");
					int blockNumber = calBlocks.getInt("BlockNumber");
					float x = calBlocks.getFloat("X");
					float y = calBlocks.getFloat("Y");
					query = String.format("select * from BlockData where DetectorID=%1$d and BlockNumber=%2$d;",detectorID,blockNumber);
					Statement statementInner = connect.createStatement();
					ResultSet blockData = statementInner.executeQuery(query);
					
					Transaction t = new Transaction();
					t.blockID = blockNumber;
					t.x = x;
					t.y = y;
					ArrayList<Integer> detectorRSSI = new ArrayList<Integer>();
					while (blockData.next()){
						int rssi = blockData.getInt("RSSI");
						detectorRSSI.add(rssi);
					}
					statementInner.close();
					t.rssiLists.put(detectorID, detectorRSSI);
					fingerPrintTable.add(t);
				}
				statementOuter.close();
				
				System.out.println("FingerPrintTable:");
				for(Transaction t: fingerPrintTable){
					System.out.println(String.format("TagID=%1$d, BlockNumber=%2$d",t.tagID, t.blockID));
					for (Map.Entry<Integer, ArrayList<Integer>> e: t.rssiLists.entrySet()){						
						System.out.print(String.format("DetectorID %1$d: ", e.getKey()));
						for (Integer rssi: e.getValue()){
							System.out.print(String.format("%1$d  ", rssi));
						}
					}
					System.out.println();
				}
				
				while (!done){				
					Sample newSample = new Sample();
					int bufferSize = 6;
					readPort(list,bufferSize);
					if (list.size() >= bufferSize){												
						
						parseSample(list, newSample);						
						printSample(newSample);
						
						int key = newSample.tagID + newSample.messageID;
						saveSample(key, rssiData, newSample);
						
						//Start a new time window, and associate with key, if key has not been seen.
						if (!ttl.containsKey(key)){
							Calendar currentMoment = Calendar.getInstance();
							currentMoment.add(Calendar.SECOND, 2);
							ttl.put(key, currentMoment);						
						}
					}					
					
					for (Map.Entry<Integer, Calendar> e: ttl.entrySet()){
						Calendar savedMoment = e.getValue();
						if (savedMoment.before(Calendar.getInstance())){
							ArrayList<Sample> sampleVals = rssiData.get(e.getKey());
							if (sampleVals.size() >= 2)
							{		
								Transaction t = new Transaction();
								t.batteryLevel = sampleVals.get(0).reserved;
								t.tagID = sampleVals.get(0).tagID;
								t.x = -999;
								t.y = -999;
								for (int s = 0;s < sampleVals.size();s++)
								{
									ArrayList<Integer> rssiSingle =  new ArrayList<Integer>();
									rssiSingle.add(sampleVals.get(s).rssi);//Only one
									t.rssiLists.put(sampleVals.get(s).detectorID,rssiSingle );
								}
								System.out.println(t);
								//TODO: pass fingerPrintTable and transaction to locator calculate method
								//locatorObject.locate(fingerPrintTable, transaction);
									
							}
							//Remove entries in samples and ttl
							rssiData.remove(e.getKey());
							ttl.remove(e.getKey());							
						}
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
	}
	
}
