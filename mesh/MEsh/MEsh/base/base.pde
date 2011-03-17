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

#define FROM_TAG 		0
#define PAYLOAD_SIZE 	        8
#define MyID			6

#define FASTADC 1

// defines for setting and clearing register bits
#ifndef cbi
#define cbi(sfr, bit) (_SFR_BYTE(sfr) &= ~_BV(bit))
#endif
#ifndef sbi
#define sbi(sfr, bit) (_SFR_BYTE(sfr) |= _BV(bit))
#endif


unsigned char payload[PAYLOAD_SIZE];
unsigned char rssi=0;
//
void setup () 
{
#if FASTADC
  // set prescale to 16
  sbi(ADCSRA,ADPS2) ;
  cbi(ADCSRA,ADPS1) ;
  cbi(ADCSRA,ADPS0) ;
#endif
  Serial.begin(19200);   
  pinMode(5,OUTPUT);
  rf12_initialize(3, RF12_433MHZ,33);
}

void loop () 
{    

  if (rf12_recvDone() && rf12_crc == 0) 
  {
    //Serial.print("rf12_len=");
    //Serial.print(rf12_len,DEC);
    //Serial.println();
    //TODO: check to see if incoming message checksum is valid.
    //      and skip this if it is invalid.

    //TODO: if source ID doesn't have an entry in my routing table
    //		then ignore message.
    //foreach entry in routingTable
    //	if rf12_data[1] == entry then
    //		accept=true
    //endfor
      if (rf12_data[1] != FROM_TAG)//From Detector
      {
        payload[0] = 13;		//Start Delimiter
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

      }
      else if (rf12_data[1] == FROM_TAG){//From Tag			

        // get RSSI              
        payload[0] = 14;		//Start Delimiter
        payload[1] = MyID;				//Source ID
        payload[2] = MyID;				//Detector ID
        payload[3] = 0;					//Reserved - put batter level here
        payload[4] = readRSSI();				//RSSI value - we are the tag, we don't know this value.
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






