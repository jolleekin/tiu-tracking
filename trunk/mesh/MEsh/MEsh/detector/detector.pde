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

#define DETECTOR 1
#define TAG 2
#define BASE 3

// RECEIVING FORMAT 
// FROM TAG       : [TAG][TAG_ID][DATA] // E.G 0x0201 =  TAG#1
// MINUMUM 2 BYTES
//                           |======== FROM =======| |========== DATA 1 =========|  |========|
// FROM (OTHERS) DETECTOR  : [DETECTOR][DETECTOR_ID] [DETECTOR_ID] [TAG_ID] [RSSI]  [RESERVED]
// MINIMUM 5 BYTES
// E.G 0x010100FF = FROM DETECTOR#1 "DETECTOR#1 SAID, TAG#0 HAS RSSI 255"

// this will be retrieved eeprom in later development:
byte ID = 4; // 1...30 only, 0 is not allow in this lib
       
// routing "table", store detector IDs which this device could receive messages
// this can be re-programmed from external source
// note: not implemented yet!
int canReceiveFrom[3];
int receiveNumber = 3;
	
unsigned char sndbuff[] = "hello!";	

void setup () 
{
    Serial.begin(19200);   
 
    sndbuff[0] = DETECTOR;
    sndbuff[1] = ID;
    
//    canReceiveFrom[0] = 0; // always listens to base node
//    for testing this sketch only, change this value for an appropriate route
//    canReceiveFrom[1] = 4; // will listens to this detector node 
//    canReceiveFrom[2] = 0; // will listens to this detector node 
//    
    pinMode(5,OUTPUT);
    rf12_initialize(1, RF12_433MHZ,33);
}


int temp = 0;
void loop () 
{    
   if (rf12_recvDone()) 
   {
     if (rf12_data[0] == TAG)
     {
        // get RSSI;        
        temp = readRSSI()/2; // SO THAT IT FITS IN ONE BYTE
        Serial.print(" RSSI");
        Serial.println(temp);
        sndbuff[2] = ID;                   // This Detector ID
        sndbuff[3] = rf12_data[1];         // Tag ID
        sndbuff[4] = (byte)temp;           // RSSI
        //sndbuff[5] = ' ';                   
      
        while(1)
        {
                digitalWrite(5,HIGH);
                // use timerinterrupt instead, later
                delay(random(temp));   
                //Serial.println(sndbuff);
                rf12_sendStart(0, sndbuff, 5);
                digitalWrite(5,LOW);
                Serial.println((unsigned char)sndbuff[4],DEC);
                break;       
        }   
     }
      else if (rf12_data[0] == DETECTOR)
      {
        // not implemented yet
      }
  }
}

  



