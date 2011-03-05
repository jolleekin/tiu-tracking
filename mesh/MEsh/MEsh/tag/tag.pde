/*****************************************************************
 * Example tX Code for RFM12 Module                              *
 * by Dung Dang Le, Daniel Ferguson,                                              *
 * This code is licensed under GPL v.2                           *
 *****************************************************************
 * Set up:                                                       *
 * RFM12B (REVISION B) ->  ATMEGA328                             *
 *              SDI    ->  MOSI (PB3)                            *
 *              SDO    ->  MISO (PB4)                            *
 *              SCK    ->  SCK  (PB5)                            *
 *              nSel   ->  SS   (PB2)                            *
 * LED -> PD5                                                    *
 *****************************************************************/
//#include <global.h>
//#include <rf12.h>
//
//#include <avr/io.h>
//#include <avr/interrupt.h>
//#include <avr/pgmspace.h>
//#include <avr/eeprom.h>
//#include <stdlib.h>
    
    
#include <RF12.h>
#include <Ports.h>

#define DETECTOR 1
#define TAG 2
#define BASE 3

// RECEIVING FORMAT 
// FROM TAG       : [TAG][TAG_ID][DATA] // E.G 0x0201 =  TAG#1
// MINUMUM 2 BYTES

// this will be retrieved eeprom in later development:
byte TagID = 1; // 1...30 only, 0 is not allow in this lib
byte MessageID=0;                

//char payload[]= "bumble beeeeeeeeeeeeeeeee...";
char payload[10];

void setup()
{
	pinMode(5,OUTPUT);
	sei();
	rf12_initialize(1, RF12_433MHZ, 33);
}

void loop()
{  
    rf12_recvDone();
    if (rf12_canSend()) 
    {     
		char payload[8];
		payload[0] = '$';			//Start1
		payload[1] = 0;				//Source ID
		payload[2] = 0;				//Detector ID
		payload[3] = 0;				//Reserved - put batter level here
		payload[4] = 0;				//RSSI value - we are the tag, we don't know this value.
		payload[5] = TagID;			//Tag ID
		payload[6] = MessageID;		//Message ID	
		payload[7] = payload[0] ^	//Checksum; omitted Zero's, no affect on checksum.
					 payload[5] ^ 
					 payload[6];	
		
		MessageID++;
		
		rf12_sendStart(0, payload, 8);
		
        delay(200);
    }
	   
}


