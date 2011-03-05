/*****************************************************************
 * Example base Code for RFM12 Module                            *
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

//                  |======== FROM =======| |========== DATA 1 =========|  |========|
// FROM DETECTOR  : [DETECTOR][DETECTOR_ID] [DETECTOR_ID] [TAG_ID] [RSSI]  [RESERVED]
// MINIMUM 5 BYTES
// E.G 0x010100FF = FROM DETECTOR#1 "DETECTOR#1 SAID, TAG#0 HAS RSSI 255"

// this will be retrieved eeprom in later development:
byte ID = 0; // 1...30 only, 0 is not allow in this lib                

// routing "table", store detector IDs which this device could receive messages
// this can be re-programmed from external source
// NOTE : NOT IMPLEMENTED YET
int canReceiveFrom[3];
int receiveNumber = 3;
	
//unsigned char sndbuff[20];	

void setup () 
{
    Serial.begin(19200);   
    //sndbuff[0] = DETECTOR;
    //sndbuff[1] = ID;
//    canReceiveFrom[0] = 0; // always listens to base node
//    // for testing this sketch only, change this value for an appropriate route
//    canReceiveFrom[1] = 4; // will listens to this detector node 
//    canReceiveFrom[2] = 0; // will listens to this detector node 
    rf12_initialize(1, RF12_433MHZ,33);
}

int temp = 0;
void loop () 
{    
   if (rf12_recvDone()) 
   {
  
        if (rf12_data[0] == DETECTOR)
        {
          
                  Serial.print(" from Detector:");
                  Serial.print(rf12_data[2],DEC);
                  Serial.print(" Tag:");
                  Serial.print(rf12_data[3],DEC);
                  Serial.print(" has RSSI:");                  
                  Serial.print((unsigned char)rf12_data[4],DEC);                                
                  Serial.println();
        
        }
        else if (rf12_data[0] == TAG)
        {
                  // calibrate this after setup detectors
                  int temp =  readRSSI()/2;                  
                  Serial.print(" Tag:");
                  Serial.print(rf12_data[1],DEC);
                  Serial.print(" has RSSI:");
                  Serial.print(temp,DEC);
                  Serial.println();
        }
   }
}

   
