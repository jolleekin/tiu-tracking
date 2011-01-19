/*****************************************************************
 * Example RX Code for RFM12 Module - A Polling Approach         *
 * by Dung Dang Le, original source by Benedikt K. (benedikt)    *
 * This code is licensed under GPL v.2                           *
 *****************************************************************
 * Set up:                                                       *
 * RFM12B (REVISION B) ->  ATMEGA328                             *
 *              SDI    ->  MOSI (PB3)                            *
 *              SDO    ->  MISO (PB4)                            *
 *              SCK    ->  SCK  (PB5)                            *
 *              nSel   ->  SS   (PB2)                            *
 * ARSSI (at resistor) ->  PC0  (analog input 0)                 *
 *****************************************************************/
 
#include <global.h>
#include <rf12.h>

#include <avr/io.h>
#include <avr/interrupt.h>
#include <avr/pgmspace.h>
#include <avr/eeprom.h>
#include <stdlib.h>
    
void receive(void);

void setup()
{
        // uart_init(UART_BAUD_SELECT(19200, F_CPU));
        Serial.begin(19200);
	rf12_init();				// some register set (for example, CLK to 10MHz)
	rf12_setfreq(RF12FREQ(433.92));	        // Transmit / receive frequency at 433.92 MHz
	rf12_setbandwidth(4, 1, 4);		// 200kHz bandwidth,-6dB reinforcement?, DRSSI threshold:-79dBm
	rf12_setbaud(19200);			// 19200 baud
	rf12_setpower(0, 6);			// 1mW, 120 kHz frequency shift
	sei();
}
void loop()
{
        receive(); // hence, polling :)
}

void receive(void)
{	
        unsigned char test[28];	
	rf12_rxdata(test,28);	
        unsigned char i=0;
	for (i; i<27; i++)
            Serial.print(test[i],BYTE);
        Serial.print(" SS =  ");
        long s = readRSSI(); // get RSSI (sampling as receiving, done inside rf12_rxdata())
	Serial.println(s,DEC);
}



