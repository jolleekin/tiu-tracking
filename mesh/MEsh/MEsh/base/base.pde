/*****************************************************************
 * Example detector Code using RFM12 Module                      *
 *****************************************************************
 * Set up:                                                       *
 * RFM12B (REVISION B) ->  ATMEGA328                             *
 *              SDI    ->  MOSI (PB3)                            *
 *              SDO    ->  MISO (PB4)                            *
 *              SCK    ->  SCK  (PB5)                            *
 *              nSel   ->  SS   (PB2)                            *
 *              IRQ    ->  INT0 (PD2)                            *
 * ARSSI (at resistor) ->  PC0  (analog input 0)                 *
 *****************************************************************/
#include <RF12.h>
#include <Ports.h>


// RECEIVING FORMAT 
// FROM TAG       : [TAG][TAG_ID][DATA] // E.G 0x0201 =  TAG#1
// MINUMUM 2 BYTES
//                           |======== FROM =======| |========== DATA 1 =========|  |========|
// FROM (OTHERS) DETECTOR  : [DETECTOR][DETECTOR_ID] [DETECTOR_ID] [TAG_ID] [RSSI]  [RESERVED]
// MINIMUM 5 BYTES
// E.G 0x010100FF = FROM DETECTOR#1 "DETECTOR#1 SAID, TAG#0 HAS RSSI 255"


#define FROM_TAG 		0
#define PAYLOAD_SIZE 	        8
#define MyID			4
unsigned char payload[PAYLOAD_SIZE];

unsigned char routing_table[] = {
	4
};


void setup () 
{
    Serial.begin(19200);   
    pinMode(5,OUTPUT);
    rf12_initialize(1, RF12_433MHZ,33);
}

void loop () 
{    
	if (rf12_recvDone()) 
	{
		//TODO: check to see if incoming message checksum is valid.
		//      and skip this if it is invalid.
		
		//TODO: if source ID doesn't have an entry in my routing table
		//		then ignore message.
		//foreach entry in routingTable
		//	if rf12_data[1] == entry then
		//		accept=true
		//endfor
		
		if (rf12_data[1] != 0)//From Detector
		{
			payload[0] = rf12_data[0];		//Start Delimiter
			payload[1] = MyID;				//Source ID
			payload[2] = rf12_data[2];		//Detector ID
			payload[3] = rf12_data[3];		//Reserved
			payload[4] = rf12_data[4];		//RSSI value 
			payload[5] = rf12_data[5];		//Tag ID
			payload[6] = rf12_data[6];		//Message ID 
			payload[7] = payload[0] ^ 		//Checksum - Changes because of updated DID
				     payload[1] ^ 
				     payload[2] ^ 
			             payload[3] ^ 
				     payload[4] ^ 
				     payload[5] ^ 
				     payload[6];
	   
		}else if (rf12_data[1] == 0){//From Tag			
			
			// get RSSI        
			int rssi = readRSSI();      
			payload[0] = rf12_data[0];		//Start Delimiter
			payload[1] = MyID;				//Source ID
			payload[2] = MyID;				//Detector ID
			payload[3] = 0;					//Reserved - put batter level here
			payload[4] = rssi;				//RSSI value - we are the tag, we don't know this value.
			payload[5] = rf12_data[5];		//Tag ID
			payload[6] = rf12_data[6];		//Message ID 
			payload[7] = payload[0] ^ 		//Checksum
				     payload[1] ^ 
				     payload[2] ^ 
				     payload[3] ^ 
				     payload[4] ^ 
				     payload[5] ^ 
				     payload[6];
		}
		
		
		//If From Detector, then just relay received data.
		//If from Tag, then relay payload that was prepared above..
		Serial.print(payload[0],BYTE);//Start delimiter
		Serial.print(payload[1],BYTE);//DID
		Serial.print(payload[2],BYTE);//SID
		Serial.print(payload[3],BYTE);//Reserved   
		Serial.print(payload[4],BYTE);//RSSI
		Serial.print(payload[5],BYTE);//TID
		Serial.print(payload[6],BYTE);//MID
		Serial.print(payload[7],BYTE);//Checksum    

    
	}
}

  



