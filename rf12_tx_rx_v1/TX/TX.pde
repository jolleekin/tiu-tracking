/*****************************************************************
 * Example tX Code for RFM12 Module - A Polling Approach         *
 * by Dung Dang Le, original source by Benedikt K. (benedikt)    *
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
#include <global.h>
#include <rf12.h>

#include <avr/io.h>
#include <avr/interrupt.h>
#include <avr/pgmspace.h>
#include <avr/eeprom.h>
#include <stdlib.h>
    
void send(void);

void setup()
{
        // uart_init(UART_BAUD_SELECT(19200, F_CPU));
        Serial.begin(19200);
	rf12_init();				// some register set (for example, CLK to 10MHz)
	rf12_setfreq(RF12FREQ(433.92));	        // Transmit / receive frequency at 433.92 MHz
	rf12_setbandwidth(4, 1, 4);		// 200kHz bandwidth,-6dB reinforcement?, DRSSI threshold:-79dBm
	rf12_setbaud(19200);			// 19200 baud
	rf12_setpower(0, 6);			// 1mW, 120 kHz frequency shift
        pinMode(5,OUTPUT);
	sei();
}

void loop()
{
        digitalWrite(5,HIGH);
        send();
        digitalWrite(5,LOW);
        delay(2000);		
}

void send(void)
{
  	unsigned char test[]="Dies ist ein 433MHz Test !!!  ";  	
	rf12_txdata(test,28);
}

