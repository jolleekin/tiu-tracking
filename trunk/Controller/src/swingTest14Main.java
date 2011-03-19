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
import javax.swing.JComboBox;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JLabel;

public class swingTest14Main {

	private JFrame frame;
	private JButton btnConnect;
	private JComboBox cbxCOMPorts;
	private JTextArea txtOutput;
	JTextArea txtAreaNotes;
	
	private CommPort commPort;
	private SerialPort serialPort;
	private boolean readerDone;
	private boolean writerDone;
	Thread writerThread;
	Thread readerThread;
	Reader reader;
	Writer writer;
	private JTextField txtLocationDescription;
	private JLabel lblPreviousLocationDescription;
	private JScrollPane scrollPane;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					swingTest14Main window = new swingTest14Main();
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
	public swingTest14Main() {
		//GUI init
		initialize();

		//member variable init
		readerDone=false;
		writerDone=false;
		
		//Populate combo box with available serial ports.
		@SuppressWarnings("unchecked")
		Enumeration<CommPortIdentifier> ports = CommPortIdentifier.getPortIdentifiers();
		while (ports.hasMoreElements())
		{
			CommPortIdentifier port = ports.nextElement();	
			cbxCOMPorts.addItem(port.getName());
		}	
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 794, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		btnConnect = new JButton("Start");
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {				
				try {					
					if (btnConnect.getText() == "Start"){
						if (txtLocationDescription.getText().length() < 2){
							JOptionPane.showMessageDialog(null,"Enter Location Description!!");
							return;
						}
						String selectedPortName = cbxCOMPorts.getItemAt(cbxCOMPorts.getSelectedIndex()).toString();
						CommPortIdentifier port = CommPortIdentifier.getPortIdentifier(selectedPortName);
						commPort = port.open("swingTest14Main",2000);
						if (commPort instanceof SerialPort){
							serialPort = (SerialPort) commPort;
							serialPort.setSerialPortParams(19200, 
														   SerialPort.DATABITS_8, 
														   SerialPort.STOPBITS_1, 
														   SerialPort.PARITY_NONE);
							InputStream in = serialPort.getInputStream();
							OutputStream out = serialPort.getOutputStream();
							reader = new Reader(in);
							writer = new Writer(out);
							writerThread = new Thread(reader);
							readerThread = new Thread(writer);
							writerThread.start();
							readerThread.start();
							
							
							btnConnect.setText("Stop");
						}
					}else if (btnConnect.getText() == "Stop"){
						writer.stop();
						reader.requestStop();
						writerThread.join();
						readerThread.join();
						serialPort.close();
						btnConnect.setText("Start");
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
		btnConnect.setBounds(594, 221, 173, 31);
		frame.getContentPane().add(btnConnect);
		
		cbxCOMPorts = new JComboBox();
		cbxCOMPorts.setBounds(594, 10, 173, 20);
		frame.getContentPane().add(cbxCOMPorts);
		
		txtLocationDescription = new JTextField();
		txtLocationDescription.setBounds(594, 45, 173, 20);
		frame.getContentPane().add(txtLocationDescription);
		txtLocationDescription.setColumns(10);
		
		JLabel lblLocationDescription = new JLabel("Location Description:");
		lblLocationDescription.setBounds(594, 30, 123, 16);
		frame.getContentPane().add(lblLocationDescription);
		
		JLabel lblNotes = new JLabel("Notes:");
		lblNotes.setBounds(594, 91, 46, 16);
		frame.getContentPane().add(lblNotes);
		
		lblPreviousLocationDescription = new JLabel("previous...");
		lblPreviousLocationDescription.setBounds(595, 62, 177, 16);
		frame.getContentPane().add(lblPreviousLocationDescription);
		
		txtAreaNotes = new JTextArea();
		txtAreaNotes.setBounds(594, 105, 173, 104);
		frame.getContentPane().add(txtAreaNotes);
		txtAreaNotes.setWrapStyleWord(true);
		txtAreaNotes.setLineWrap(true);
		
		scrollPane = new JScrollPane();
		scrollPane.setBounds(8, 10, 574, 236);
		frame.getContentPane().add(scrollPane);
		
		txtOutput = new JTextArea();
		scrollPane.setViewportView(txtOutput);
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
				int toggle=1;
				while (!done){
					if (toggle == 1)
					{					
						out.write(97);
						Thread.sleep(500);
						
					}else{					
						out.write(98);
						Thread.sleep(500);				
					}
					toggle ^=1;				
				}
			}catch (IOException e){
				e.printStackTrace();
			} catch (InterruptedException e) {
				throw new RuntimeException("Interrupted in writer", e);
			}*/
		}
	}
	
	public class Tag{
		public Tag(){
			
		}
		
		public int getDetectorID(){return detectorID;}
		public int getTagID(){return tagID;}
		public int getBatteryLevel(){return batteryLevel;}
		public int getRSSI(){return rssi;}
		
		public void setDetectorID(int did){detectorID=did;}
		public void setTagID(int tid){tagID=tid;}
		public void setBatteryLevel(int batt){batteryLevel=batt;}
		public void setRSSI(int rssi){this.rssi = rssi;}
		
		private int detectorID;
		private int tagID;
		private int batteryLevel;
		private int rssi;		
	}
	
	public class Location{
		private Hashtable<Integer, Tag> tags;
		public Location()
		{
			tags = new Hashtable<Integer,Tag>();
		}
		
	}
	public class Reader implements Runnable{
		private InputStream in;
		private volatile boolean done;
		public Reader(InputStream in){
			this.in = in;
		}
		public void requestStop(){
			done=true;
		}
		public void run(){
			try{				
				this.done=false;
				ArrayList<Byte> list = new ArrayList<Byte>();
				FileWriter fw = new FileWriter(txtLocationDescription.getText()+"_data.csv");
				
				
				while (!done){				
					
					//int bufferSize = 8;
					int bufferSize = 6;
					byte[] buffer = new byte[bufferSize];
					int bytesRead=0;
					
					System.out.println("bufferSize" + bufferSize);

					bytesRead = in.read(buffer,0, bufferSize);
					System.out.println("byteRead: " + bytesRead);
					System.out.println("sourceID: " + buffer[0]);
					for (int k = 0;k < bytesRead;k++){
						list.add(buffer[k]);
					}
					
					if (list.size() > bufferSize){
						
						/*
						int startDelimiter 		= list.get(0);			list.remove(0);
						int sourceID 			= list.get(0) & 0xff;	list.remove(0);
						int detectorID 			= list.get(0) & 0xff;	list.remove(0);
						int reserved 			= list.get(0);			list.remove(0);
						int rssi 				= list.get(0) & 0xff;	list.remove(0);
						int tagID 				= list.get(0) & 0xff;	list.remove(0);
						int messageID 			= list.get(0) & 0xff;	list.remove(0);
						int checkSum 			= list.get(0) & 0xff;	list.remove(0);
						*/
						
						
						int sourceID 	= list.get(0) & 0xff; 		list.remove(0);
						int detectorID 	= list.get(0) & 0xff; 		list.remove(0);
						int rssi 		= list.get(0) & 0xff;		list.remove(0);
						int tagID 		= list.get(0) & 0xff;		list.remove(0);
						int messageID 	= list.get(0) & 0xff;		list.remove(0);
						int reserved 	= list.get(0) & 0xff;		list.remove(0);
						
						
						
						//System.out.println(sourceID);
						
						DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
				        Date date = new Date();
				        String time = dateFormat.format(date);
						
						//int calculatedCheckSum =0;
						//calculatedCheckSum = startDelimiter ^ sourceID ^ detectorID ^ reserved ^ rssi ^ tagID ^ messageID;
						
						//String formattedString1 = String.format("%1$s, D:%2$d, S:%3$d, T:%4$d, R:%5$d, RSSI:%6$d, MsgID: %7$d, ChkSm: %8$d\n",time, detectorID, sourceID, tagID, reserved, rssi, messageID, checkSum);
						//String formattedString2 = String.format("%1$s,%2$d,%3$d,%4$d,%5$d,%6$d,%7$d,%8$d\n",time,detectorID, sourceID, tagID, reserved, rssi, messageID, checkSum);
						String formattedString1 = String.format("%1$s, D:%2$d, S:%3$d, T:%4$d, RSSI:%5$d, MsgID: %6$d, Reserved: %7$d\n",time, detectorID, sourceID, tagID, rssi, messageID, reserved);
						String formattedString2 = String.format("%1$s,%2$d,%3$d,%4$d,%5$d,%6$d,%7$d\n", time, detectorID, sourceID, tagID, rssi, messageID, reserved);
						txtOutput.append(formattedString1);
						txtOutput.setCaretPosition(txtOutput.getDocument().getLength());
						fw.write(formattedString2);
					}					
				}
				fw.close();
				lblPreviousLocationDescription.setText(txtLocationDescription.getText());
				txtLocationDescription.setText(null);
			
			/*	while (!done)
				{
					int bufferSize = 128;
					byte[] buffer = new byte[bufferSize];
					int bytesRead=0;

					bytesRead = in.read(buffer,0, bufferSize);
					String formattedString1="";
					for (int k = 0;k < bytesRead;k++){
						formattedString1 += String.
					}
					
					txtOutput.append(formattedString1);
					txtOutput.setCaretPosition(txtOutput.getDocument().getLength());
				}*/
			}catch (IOException e){
				e.printStackTrace();
			}catch (RuntimeException e){
				e.printStackTrace();
			
			}
		}
	}
}
