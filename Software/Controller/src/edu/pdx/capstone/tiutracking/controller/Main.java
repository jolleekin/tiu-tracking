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

import edu.pdx.capstone.tiutracking.gammaengine.GammaEngine;
import edu.pdx.capstone.tiutracking.kengine.KEngine;
import edu.pdx.capstone.tiutracking.locator.FingerPrint;

import java.awt.EventQueue;
import java.awt.Font;

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

import javax.swing.ButtonGroup;
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
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.UIManager;

import java.sql.*;
import javax.swing.JTabbedPane;
import javax.swing.JMenuItem;
import javax.swing.JTable;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

public class Main {

	private enum AppViewMode{
		CALIBRATION,
		COLLECTION,
		LOCATING
	}
	
	private enum LocatorChoices{
		FINGERPRINT,
		GAMMA,
		KEWTON
	}
	private LocatorChoices locatorChoice=LocatorChoices.KEWTON;
	private LocationEngine locator;
	private AppViewMode appViewMode;
	private JFrame frmController;
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
	private ToggleButton btnStartCalibrating;
	private ToggleButton btnStartCollecting;
	private ToggleButton btnStartLocating;
	private JTextField txtCalibrateX;
	private JTextField txtCalibrateY;
	private JTextField txtCalibrateTagID;
	private JTextField txtCalibrateBlockNumber;
	private JLabel lblNewLabel;
	private JLabel lblNewLabel_1;
	private JLabel lblNewLabel_2;
	private JLabel lblNewLabel_3;
	private JTabbedPane tabbedPaneMain;
	private JMenu mnFile;
	private JMenuItem mntmExit;
	private JTable tblLocatorConfiguration;
	private JScrollPane scrollPane;
	private JScrollPane scrollPane_1;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Main window = new Main();
					window.frmController.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public static LocationEngine createLocator(LocatorChoices locatorChoice) {
		switch (locatorChoice){
		case FINGERPRINT:
			return (LocationEngine)new FingerPrint();
		case GAMMA:
			return(LocationEngine)new GammaEngine();
		case KEWTON:
			return(LocationEngine)new KEngine();
		}
		return null;
	}
	public static void populateConfigurationTable(LocationEngine locator, JTable tblLocatorConfiguration) {
		ArrayList<ConfigurationParam> parameters = locator.getConfiguration();
		if (parameters != null){
			ConfigurationTableModel ctm = new ConfigurationTableModel();
			ctm.setData(parameters);
			tblLocatorConfiguration.setModel(ctm);
			tblLocatorConfiguration.setFont(new Font(null,Font.PLAIN, 8));
			tblLocatorConfiguration.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);   
			tblLocatorConfiguration.getColumnModel().getColumn(0).setPreferredWidth(55);
			tblLocatorConfiguration.getColumnModel().getColumn(1).setPreferredWidth(400);					
			tblLocatorConfiguration.getColumnModel().getColumn(2).setPreferredWidth(55);
		}else{
			for (int i = 0;i < tblLocatorConfiguration.getColumnModel().getColumnCount();i++){
				tblLocatorConfiguration.getColumnModel().removeColumn(tblLocatorConfiguration.getColumnModel().getColumn(0));
			}
		}
	}
	public boolean openPort() throws NoSuchPortException,
	PortInUseException, UnsupportedCommOperationException {
		if (cbxCOMPorts.getItemCount() < 1 || cbxCOMPorts.getSelectedIndex() == -1){
			JOptionPane.showMessageDialog(null,"A port was no selected. Please select a port.");
			return false;
		}
		String selectedPortName = cbxCOMPorts.getItemAt(cbxCOMPorts.getSelectedIndex()).toString();
		CommPortIdentifier port = CommPortIdentifier.getPortIdentifier(selectedPortName);
		commPort = port.open("Controller",2000);
		if (!(commPort instanceof SerialPort)){
			JOptionPane.showMessageDialog(null,"Only Serial Ports are supported. Please select another port.");
			return false;
		}
		serialPort = (SerialPort) commPort;
		serialPort.setSerialPortParams(19200, 
									   SerialPort.DATABITS_8, 
									   SerialPort.STOPBITS_1, 
									   SerialPort.PARITY_NONE);
		return true;
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
		frmController = new JFrame();
		frmController.setTitle("Controller");
		frmController.setBounds(100, 100, 655, 520);
		frmController.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmController.getContentPane().setLayout(null);
		
		tabbedPaneMain = new JTabbedPane(JTabbedPane.TOP);
		tabbedPaneMain.setBounds(10, 245, 615, 205);
		
		JPanel pnlSettings = new JPanel();		
		JPanel pnlCollect = new JPanel();
		JPanel pnlLocate = new JPanel();
		
		tabbedPaneMain.addTab("Settings",pnlSettings);
		pnlSettings.setLayout(null);
		
		cbxCOMPorts = new JComboBox();
		cbxCOMPorts.setBounds(10, 24, 265, 20);
		pnlSettings.add(cbxCOMPorts);
		
		JLabel lblSerialPort = new JLabel("Serial Port");
		lblSerialPort.setBounds(10, 11, 80, 14);
		pnlSettings.add(lblSerialPort);
		
		JLabel lblLocatorConfiguration = new JLabel("Locator Configuration");
		lblLocatorConfiguration.setBounds(10, 62, 122, 14);
		pnlSettings.add(lblLocatorConfiguration);
		
		scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 87, 590, 79);
		pnlSettings.add(scrollPane);
		
		tblLocatorConfiguration = new JTable();
		scrollPane.setViewportView(tblLocatorConfiguration);
		
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "Locator Engine", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel.setBounds(435, 0, 165, 87);
		pnlSettings.add(panel);
		panel.setLayout(null);
		
		final JRadioButton rdbtnFingerPrintEngine = new JRadioButton("Finger Print Engine");
		rdbtnFingerPrintEngine.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				if (rdbtnFingerPrintEngine.isSelected()){
					locatorChoice = LocatorChoices.FINGERPRINT;
					locator = Main.createLocator(locatorChoice);
					Main.populateConfigurationTable(locator, tblLocatorConfiguration);
				}
			}
		});
		rdbtnFingerPrintEngine.setBounds(6, 21, 145, 23);
		panel.add(rdbtnFingerPrintEngine);
		
		final JRadioButton rdbtnGammaEngine = new JRadioButton("Gamma Engine");
		rdbtnGammaEngine.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				if (rdbtnGammaEngine.isSelected()){
					locatorChoice = LocatorChoices.GAMMA;
					locator = Main.createLocator(locatorChoice);				
					Main.populateConfigurationTable(locator, tblLocatorConfiguration);
				}
			}
		});
		rdbtnGammaEngine.setBounds(6, 40, 145, 23);
		panel.add(rdbtnGammaEngine);
		
		final JRadioButton rdbtnKewtonEngine = new JRadioButton("Kewton Engine");
		rdbtnKewtonEngine.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				if (rdbtnKewtonEngine.isSelected()){
					locatorChoice = LocatorChoices.KEWTON;
					locator = Main.createLocator(locatorChoice);				
					Main.populateConfigurationTable(locator, tblLocatorConfiguration);
				}
			}
		});
		
		ButtonGroup engineBtnGroup = new ButtonGroup(); 
		engineBtnGroup.add(rdbtnFingerPrintEngine);
		engineBtnGroup.add(rdbtnGammaEngine);
		engineBtnGroup.add(rdbtnKewtonEngine);
		rdbtnKewtonEngine.setBounds(6, 59, 109, 23);
		panel.add(rdbtnKewtonEngine);
		tabbedPaneMain.addTab("Collect", pnlCollect);
		pnlCollect.setLayout(null);
		
		JLabel lblLocationDescription = new JLabel("Output File Description");
		lblLocationDescription.setBounds(10, 11, 123, 16);
		pnlCollect.add(lblLocationDescription);
		
		txtLocationDescription = new JTextField();
		txtLocationDescription.setBounds(10, 26, 265, 20);
		pnlCollect.add(txtLocationDescription);
		txtLocationDescription.setColumns(10);
		
		lblPreviousLocationDescription = new JLabel("Previous...");
		lblPreviousLocationDescription.setBounds(11, 43, 177, 16);
		pnlCollect.add(lblPreviousLocationDescription);
		
		btnStartCollecting = new ToggleButton("Start Collecting", "Stop Collecting");
		btnStartCollecting.setBounds(11, 70, 264, 31);
		pnlCollect.add(btnStartCollecting);
		btnStartCollecting.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {					
					if (btnStartCollecting.getText() == btnStartCollecting.getFirstTitle()){
						if (txtLocationDescription.getText().length() < 2){
							JOptionPane.showMessageDialog(null,"Please specify a file description.");
							return;
						}
						openPort();
						InputStream in = serialPort.getInputStream();
						OutputStream out = serialPort.getOutputStream();						
						collectorReader = new CollectorReader(in);
						writer = new Writer(out);
						writerThread = new Thread(collectorReader);
						readerThread = new Thread(writer);
						writerThread.start();
						readerThread.start();						
						btnStartCollecting.toggleTitle();						
					}else if (btnStartCollecting.getText() == btnStartCollecting.getSecondTitle()){
						writer.stop();
						collectorReader.requestStop();
						writerThread.join();
						readerThread.join();
						serialPort.close();
						btnStartCollecting.toggleTitle();
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
		JPanel pnlCalibrate = new JPanel();
		tabbedPaneMain.addTab("Calibrate", pnlCalibrate);
		pnlCalibrate.setLayout(null);
		
		lblNewLabel = new JLabel("X");
		lblNewLabel.setBounds(10, 11, 46, 14);
		pnlCalibrate.add(lblNewLabel);
		
		lblNewLabel_1 = new JLabel("Y");
		lblNewLabel_1.setBounds(98, 11, 46, 14);
		pnlCalibrate.add(lblNewLabel_1);
		
		btnStartCalibrating = new ToggleButton("Start Calibrating", "Stop Calibrating");
		btnStartCalibrating.setBounds(10, 144, 265, 31);
		pnlCalibrate.add(btnStartCalibrating);
		btnStartCalibrating.setToolTipText("Saves collected data into the database");
		
		txtCalibrateX = new JTextField();
		txtCalibrateX.setBounds(10, 24, 86, 20);
		pnlCalibrate.add(txtCalibrateX);
		txtCalibrateX.setColumns(10);
		
		txtCalibrateY = new JTextField();
		txtCalibrateY.setBounds(98, 24, 86, 20);
		pnlCalibrate.add(txtCalibrateY);
		txtCalibrateY.setColumns(10);
		
		txtCalibrateTagID = new JTextField();
		txtCalibrateTagID.setBounds(10, 68, 265, 20);
		pnlCalibrate.add(txtCalibrateTagID);
		txtCalibrateTagID.setColumns(10);
		
		txtCalibrateBlockNumber = new JTextField();
		txtCalibrateBlockNumber.setBounds(10, 111, 265, 20);
		pnlCalibrate.add(txtCalibrateBlockNumber);
		txtCalibrateBlockNumber.setColumns(10);
		
		lblNewLabel_3 = new JLabel("Block Number");
		lblNewLabel_3.setBounds(10, 96, 102, 14);
		pnlCalibrate.add(lblNewLabel_3);
		
		lblNewLabel_2 = new JLabel("TagID");
		lblNewLabel_2.setBounds(10, 55, 46, 14);
		pnlCalibrate.add(lblNewLabel_2);
		btnStartCalibrating.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {					
					if (btnStartCalibrating.getText() == btnStartCalibrating.getFirstTitle()){						
						openPort();
						InputStream in = serialPort.getInputStream();
						OutputStream out = serialPort.getOutputStream();
						calibratorReader = new CalibratorReader(in);
						writer = new Writer(out);
						writerThread = new Thread(calibratorReader);
						readerThread = new Thread(writer);
						writerThread.start();
						readerThread.start();							
						btnStartCalibrating.toggleTitle();						
					}else if (btnStartCalibrating.getText() == btnStartCalibrating.getSecondTitle()){
						writer.stop();
						calibratorReader.requestStop();
						writerThread.join();
						readerThread.join();
						serialPort.close();
						btnStartCalibrating.toggleTitle();
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
		tabbedPaneMain.addTab("Locate", pnlLocate);
		pnlLocate.setLayout(null);
		
		btnStartLocating = new ToggleButton("Start Locating", "Stop Locating");
		btnStartLocating.setBounds(10, 11, 265, 31);
		pnlLocate.add(btnStartLocating);
		btnStartLocating.setToolTipText("Passes collected data to the Locating engine");
		btnStartLocating.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {				
				try {					
					if (btnStartLocating.getText() == btnStartLocating.getFirstTitle()){						
						openPort();
						InputStream in = serialPort.getInputStream();
						OutputStream out = serialPort.getOutputStream();
						locatorReader = new LocatorReader(in);
						writer = new Writer(out);
						writerThread = new Thread(locatorReader);
						readerThread = new Thread(writer);
						writerThread.start();
						readerThread.start();					
						btnStartLocating.toggleTitle();
						
					}else if (btnStartLocating.getText() == btnStartLocating.getSecondTitle()){
						writer.stop();
						locatorReader.requestStop();
						writerThread.join();
						readerThread.join();
						serialPort.close();
						btnStartLocating.toggleTitle();
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
		
		
		
		frmController.getContentPane().add(tabbedPaneMain);
		
		scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(10, 9, 615, 225);
		frmController.getContentPane().add(scrollPane_1);
		
		txtOutput = new JTextArea();
		scrollPane_1.setViewportView(txtOutput);
		/*When the Start/Stop Locating button is hit
		 *Start/Stop the LocatorThread 
		 */
		/*When the Start/Stop Calibrating button is hit
		 *Start/Stop the CalibratorThread 
		 */
		
		JMenuBar menuBar = new JMenuBar();
		frmController.setJMenuBar(menuBar);
		
		mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//TODO: stop everything, close everything, exit application
			}
		});
		mnFile.add(mntmExit);
		
		JMenu mnview = new JMenu("View");
		menuBar.add(mnview);
		
		JMenuItem mntmClear = new JMenuItem("Clear");
		mntmClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				txtOutput.setText(null);
			}
		});
		mnview.add(mntmClear);
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
	
	/*class PortReader
	 * 	Implements basic functionality shared by all reader threads.
	 * 	It provides:
	 * 		Reading raw data and packaging it into a RawSample object.
	 * 		Displaying a RawSample object to an output txt field
	 * 		Ability to stop the reader thread.
	 * 
	 */
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
			System.out.println("Parsing Sample From " + newSample.detectorId);
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
	
	/*class RawSample
	 * 	Represents a sample that is exactly as it comes in from the radio.
	 * 	Used internally. 
	 */
	public class RawSample{
		public int detectorId;
		public int rssi;
		public int sourceId;
		public int tagId;
		public int messageId;
		public int reserved;	
	}
	
	/*class CalibratorReader
	 * 	This class implements a thread that reads raw samples from a (Serial)Port.
	 * 	It ignores all Raw Samples that come from a tag that doesn't match the
	 *  TagId specified in the user interface for calibration mode.
	 * 	If a raw sample is collected with a matching TagId, it is stored into
	 * 	a DataPacket object.
	 * 	When this reader is requested to stop, it first serializes the DataPacket object
	 * 	to a local file.
	 */
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
				//Serialize calibration data
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
			File calFile = new File("calibrationdata.dat");
			if (calFile.exists()){				
				calibrationData = (ArrayList<DataPacket>)ObjectFiler.load("calibrationdata.dat");
			}else{
				calibrationData = new ArrayList<DataPacket>();
			}
			
			//If it exists, replace entry with a matching block Id, else just append it.
			boolean foundExisting=false;
			int blockId = Integer.parseInt(txtCalibrateBlockNumber.getText());
			for (int h = 0;h < calibrationData.size();h++){
				if (calibrationData.get(h).blockId == blockId){
					calibrationData.set(h, dataPacket);
					foundExisting = true;
					break;
				}
			}
			if (!foundExisting){
				calibrationData.add(dataPacket);
			}
			ObjectFiler.save("calibrationdata.dat", calibrationData);
		}		
	}
	
	/*class CollectorReader
	 * 	This class implements a thread that reads raw samples from a (Serial)Port.
	 * 	It writes each collected Raw Sample to a local file in CSV format.
	 * 
	 */
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

	/*class LocatorReader
	 * 	This class implements a thread that reads raw samples from a (Serial)Port.
	 *  It investigates the TagId and MsgId; If it has not seen that combination,
	 *  it waits a period of time to allow other raw samples with the same TagId and MsgId
	 *  combination. When the period of time expires, it takes the Raw samples it has collected
	 *  and puts them into a DataPacket.
	 *  
	 *  1) Deserialize calibration data from local filesystem.
	 *  2) Construct a Locator implementation, and pass in calibration data.
	 *  3) Begin collecting Raw Samples
	 *  	4) If conditions are right, and a DataPacket is formed
	 *  	   invoke the locate method on the Locator instance, and
	 *  	   pass in the DataPacket.
	 *  	5) Save results of locate method into DB	
	 * 
	 */
	public class LocatorReader  extends PortReader implements Runnable{	
		private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		public LocatorReader(InputStream in){
			super(in);
		}		
		public void run(){
			try{				
				this.done=false;
				ArrayList<Byte> list = new ArrayList<Byte>();
				Hashtable<Integer, ArrayList<RawSample>> rawSampleTable = new Hashtable<Integer, ArrayList<RawSample>>();
				Hashtable<Integer, Calendar> ttl = new Hashtable<Integer, Calendar>();
				
				//Get all detector Info from DB
				Hashtable<Integer,Vector2D> detectorLocations = getDetectorInfo();
				//Deserialize calibration data
				ArrayList<DataPacket> calibrationData =loadCalibrationData();				
				//Debug printing..
				System.out.println("FingerPrintTable:");
				for(DataPacket t: calibrationData){
					System.out.println(String.format("TagID=%1$d, BlockNumber=%2$d",t.tagId, t.blockId));
					for (Map.Entry<Integer, ArrayList<Integer>> e: t.rssiTable.entrySet()){						
						System.out.print(String.format("\tDetectorID %1$d: ", e.getKey()));
						for (Integer rssi: e.getValue()){
							System.out.print(String.format("%1$d  ", rssi));
						}
						System.out.println();
					}
					System.out.println();
				}			
				
				//Create locator instance, pass in calibration data, and 
				//information about detector locations
				//FingerPrint locator = new FingerPrint(calibrationData);
				//LocationEngine locator = (LocationEngine)new KEngine();
				LocationEngine locator = Main.createLocator(locatorChoice);
				locator.learn(calibrationData, detectorLocations);				
				
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
							currentMoment.add(Calendar.SECOND, 5);
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

							locator.locate(dataPacket);
							storeResult(dataPacket);
							
							//Print to txtOutput
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
		
		public void storeResult(DataPacket dataPacket){
			try{
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
				String query1 = String.format("insert into TagInfo values(%1$d, '%2$s', %3$f, %4$f, %5$d)",
											  dataPacket.tagId, 
											  sdf.format(dataPacket.timestamp), 
											  dataPacket.location.x, 
											  dataPacket.location.y, 
											  dataPacket.battery);
				System.out.println(query1);
				statement.executeUpdate(query1);
				statement.close();
				connect.close();
			}catch (Exception e){
				e.printStackTrace();
			}
		}
		
		public Hashtable<Integer, Vector2D> getDetectorInfo(){
			try{
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
				String query1 = String.format("select * from Detectors;");
				ResultSet detectorsResult = statement.executeQuery(query1);
				
				//Package results into a Hashtable<Integer, Vector2D>
				Hashtable<Integer,Vector2D> detectorTable = new Hashtable<Integer, Vector2D>();
				System.out.println("Getting Detectors:");
				while (detectorsResult.next()){
					int detectorId = detectorsResult.getInt("DetectorID");
					double x = detectorsResult.getFloat("X");
					double y = detectorsResult.getFloat("Y");
					Vector2D location = new Vector2D(x,y);
					detectorTable.put(detectorId, location);
					System.out.println("\tDetectorId="+detectorId+", (X,Y)=("+x+","+y+")");
				}
				
				return detectorTable;
				
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			return null;			
		}

		/*saveRawSample
		 * 	This function is used to save a RawSample into a hashtable.
		 *  The key may be any integer.
		 *  This function is used to store a RawSample before it is known
		 *  which DataPacket it will be associated with. 		 * 
		 */
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
		/*loadCalibrationData
		 * 	deserializes an ArrayList<DataPacket> from the local filesystem
		 * 	and returns it on success. returns null if there does not exist
		 * 	previously serialized calibration data.
		 */
		@SuppressWarnings("unchecked")
		private ArrayList<DataPacket> loadCalibrationData()
				throws ClassNotFoundException, SQLException, IOException {
			File calFile = new File("calibrationdata.dat");
			if (!calFile.exists()){
				System.out.println("Calibration data not found.");
				return null;
			}									
			return (ArrayList<DataPacket>)ObjectFiler.load("calibrationdata.dat");
		}
	}	
}
