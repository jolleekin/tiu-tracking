/*****************************************************************
 * Example RX Code for RFM12 Module - An interrupt Approach      *
 *                                                               *
 * Test encrypted communication, receiver side                   *
 * 2010-02-21 <jcw@equi4.com>                                    *
 * http://opensource.org/licenses/mit-license.php                *
 * $Id: crypRecv.pde 4833 2010-02-21 21:44:24Z jcw $             *
 *                                                               *
 * DDL: modified with readRSSI(), see RF12.cpp for details       * 
 *                                                               *
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

byte recvCount;

void setup () {
    Serial.begin(57600);
    Serial.println("\n[crypRecv]");
    rf12_initialize(1, RF12_868MHZ, 33);
    rf12_encrypt(RF12_EEPROM_EKEY);
}

// this test turns encryption on or off after every 10 received packets
long temp = 0;
void loop () {
    if (rf12_recvDone() && rf12_crc == 0) {
        // good packet received
        if (recvCount < 10)
            Serial.print(' ');
        Serial.print((int) recvCount);
        // report whether incoming was treated as encoded
        Serial.print(recvCount < 10 ? " (enc)" : "      ");
        Serial.print(" seq ");
        Serial.print(rf12_seq);
        Serial.print(" =");
        for (byte i = 0; i < rf12_len; ++i) {
            Serial.print(' ');
            Serial.print(rf12_data[i], HEX);
        }
        Serial.println();
        temp = readRSSI();
        Serial.print("SS = ");
        Serial.println(temp,DEC);
        recvCount = (recvCount + 1) % 20;
        // set encryption for receiving (0..9 encrypted, 10..19 plaintext)
        rf12_encrypt(recvCount < 10 ? RF12_EEPROM_EKEY : 0);
    }
}
