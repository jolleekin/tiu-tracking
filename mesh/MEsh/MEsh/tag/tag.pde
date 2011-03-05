/*****************************************************************
 * Example tX Code for RFM12 Module                              *
 * by Dung Dang Le,                                              *
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

unsigned char test[]="bumble beeeeeeeee..."; 

// RECEIVING FORMAT 
// FROM TAG       : [TAG][TAG_ID][DATA] // E.G 0x0201 =  TAG#1
// MINUMUM 2 BYTES

// this will be retrieved eeprom in later development:
byte ID = 1; // 1...30 only, 0 is not allow in this lib
                

char payload[]= "bumble beeeeeeeeeeeeeeeee...";
void setup()
{
        pinMode(5,OUTPUT);
        test[0] = TAG;
        test[1] = ID;
	sei();
        rf12_initialize(1, RF12_433MHZ, 33);
        payload[0] = TAG; 
        payload[1]= ID;
}

void loop()
{
  
    rf12_recvDone();

    if (rf12_canSend()) 
    {     
        //digitalWrite(5,HIGH);
        rf12_sendStart(0, payload, 25);
        //sendSize = (sendSize + 1) % 11;
        delay(100);
        // digitalWrite(5,LOW);
        delay(100);
    }
	   
}


