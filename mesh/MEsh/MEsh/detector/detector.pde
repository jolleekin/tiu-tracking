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
#define MyID			4

#define FASTADC 1

// defines for setting and clearing register bits
#ifndef cbi
#define cbi(sfr, bit) (_SFR_BYTE(sfr) &= ~_BV(bit))
#endif
#ifndef sbi
#define sbi(sfr, bit) (_SFR_BYTE(sfr) |= _BV(bit))
#endif



unsigned char payload[PAYLOAD_SIZE];
int tagMessageReceived=0;
int detMessageReceived=0;
unsigned char rssi=0;

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

  rf12_initialize(2, RF12_433MHZ,33);
}




void loop () 
{    
  
  if (rf12_recvDone() && rf12_crc==0) 
  {

    //TODO: check to see if incoming message checksum is valid.
    //      and skip this if it is invalid.

    //TODO: if source ID doesn't have an entry in my routing table
    //		then ignore message.
    //foreach entry in routingTable
    //	if rf12_data[1] == entry then
    //		accept=true
    //endfor

    //if (rf12_len >= 8){
    if (rf12_data[1] != FROM_TAG)//From Detector
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


      detMessageReceived=1;               

    }
    else if (rf12_data[1] == FROM_TAG){//From Tag			

      // get RSSI        
      //int rssi = readRSSI();      
      payload[0] = rf12_data[0];		//Start Delimiter
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

      tagMessageReceived=1;
    }
    //}
  }


  if ( ( tagMessageReceived  ) ){
    while (!rf12_canSend()){rf12_recvDone();}
    digitalWrite(5,HIGH);
    delay(random(300));
    rf12_sendStart(0, payload, PAYLOAD_SIZE);
         
    Serial.print("From Tag\r\n");        
    tagMessageReceived=0;
    

    //for (int y = 0;y < 8;y++){
     Serial.print(payload[4],DEC);
     //Serial.print(", "); 
     //}
     Serial.println();		
    digitalWrite(5,LOW);
  }
}







