/*****************************************************************
 * Example base Code using RFM12 Module                          *
 * by Dung Dang Le, Daniel Ferguson,                             *
 * This code is licensed under GPL v.2                           *
 *****************************************************************
 * Set up:                                                       *
 * RFM12B (REVISION B) ->  ATMEGA328                             *
 *              SDI    ->  MOSI (PB3)                            *
 *              SDO    ->  MISO (PB4)                            *
 *              SCK    ->  SCK  (PB5)                            *
 *              nSel   ->  SS   (PB2)                            *
 *              IRQ    ->  INT0 (PD2)                            *
 * ARSSI (at resistor) ->  PC0  (analog input 0)                 *
 *              VCC    ->  AVCC                                  *
 *****************************************************************/
#include <RF12.h>

#define FROM_TAG 		0
#define PAYLOAD_SIZE 	        8
#define MyID			255

#define FASTADC 1
#define CACHE_SIZE              100
#define CACHE_ENTRY_EMPTY        -1
// defines for setting and clearing register bits
#ifndef cbi
#define cbi(sfr, bit) (_SFR_BYTE(sfr) &= ~_BV(bit))
#endif
#ifndef sbi
#define sbi(sfr, bit) (_SFR_BYTE(sfr) |= _BV(bit))
#endif

unsigned char payload[PAYLOAD_SIZE+1];
class CacheEntry{
  public:
  int did;
  int tid;
  int mid;
};

CacheEntry cache[CACHE_SIZE];


int key(int did, int tid){
 return ((tid & 0xff)<<8)+(did & 0xff); 
}


//Returns:
//  -1 is entryExists
//  otherwise, return the index of the slot in the cache we can use.
int entryExists(int did, int tid, int mid){
  for (int index = 0;index <  CACHE_SIZE;++index){
   if (key(did,tid) == key(cache[index].did,cache[index].tid)){
     //This detector has sent us a message before    
     if (abs(cache[index].mid - mid)>20){
       // there is a large gap between message Id's, assume we need to broadcast.
       return index;
     }
     else{
      if (mid > cache[index].mid){
        //but we have not sent a message with this mid before(Do broadcast)

       return index; 
      }else if (mid <= cache[index].mid){
        //but we've already sent a message with this mid(don't broadcast)
       return -1; 
      }
     }
   } 
  }
  //We have never recieved a message from this detector before.
  //so, find first empty slot, denoted by all -1's
  for (int index = 0;index <  CACHE_SIZE;++index){
    if (cache[index].did ==-1){
        return index;//is the next available slot.
    }
  }
}
void insertCache(int index, int did, int tid, int mid){
    cache[index].did = did;
    cache[index].tid = tid;
    cache[index].mid = mid; 
}
//
void setup () 
{
  for (int i = 0;i < CACHE_SIZE;++i){
    cache[i].did = cache[i].tid = cache[i].mid = -1;
  }
#if FASTADC
  // set prescale to 16
  sbi(ADCSRA,ADPS2) ;
  cbi(ADCSRA,ADPS1) ;
  cbi(ADCSRA,ADPS0) ;
#endif
  Serial.begin(19200);   
  delay(3000);
  Serial.print("$$$");//Get back into command mode
  delay(2000);
  Serial.println("reboot");//reboot so we're in a known state
  delay(3000);
  Serial.print("$$$");//get back into command mode
  delay(2000);
  Serial.println("set wlan auth 4");//WPA2-PSK
  delay(2000);
//  Serial.println("set wlan phrase balibear");
  Serial.println("set wlan phrase 022-*VfIHF*Xj~FvLozvCEeAdMS9#46GCd9x4FnqRgoC+YZy092OIPnypDNpTDb");
  delay(2000);
//  Serial.println("join Giggles");
  Serial.println("join Engine9AP");
  delay(8000);
  Serial.println("exit");
  delay(1000);
  pinMode(5,OUTPUT);
  rf12_initialize(3, RF12_433MHZ,33);
}

void loop () 
{    
  if (rf12_recvDone() && rf12_crc == 0) 
  {
    //TODO: if source ID doesn't have an entry in my routing table
    //		then ignore message.
    //foreach entry in routingTable
    //	if rf12_data[1] == entry then
    //		accept=true
    //endfor
    
    //If From Detector, then relay received data to wifi, else ignore.
    
      if (rf12_data[0] != FROM_TAG){//From Detector
        digitalWrite(5,HIGH);
       
        payload[0] = MyID;			//Source ID
        payload[1] = rf12_data[1];		//Detector ID        
        payload[2] = rf12_data[2];		//HIGH BYTE - RSSI value 
        payload[3] = rf12_data[3];		//LOW BYTE - RSSI value 
        payload[4] = rf12_data[4];		//Tag ID
        payload[5] = rf12_data[5];		//Message ID 
        payload[6] = rf12_data[6];		//Tag battery level
        payload[7] = rf12_data[7];              //Detector battery level
      
        //Base wants to not duplicate messages from detectors..
        int index = entryExists(payload[1], payload[4],payload[5]);
        if (index != -1){
          insertCache(index,payload[1], payload[4],payload[5]);
          Serial.print("$");
          Serial.print(payload[0],BYTE);
          Serial.print(payload[1],BYTE);
          Serial.print(payload[2],BYTE);
          Serial.print(payload[3],BYTE);
          Serial.print(payload[4],BYTE);
          Serial.print(payload[5],BYTE);
          Serial.print(payload[6],BYTE);
          Serial.print(payload[7],BYTE);
        }
      
        digitalWrite(5,LOW);
      }
      // to Wifi
//      Serial.print(payload[0],DEC);Serial.print(", ");
//      Serial.print(payload[1],DEC);Serial.print(", ");
//      Serial.print(payload[2],DEC);Serial.print(", ");
//      Serial.print(payload[3],DEC);Serial.print(", ");
//      Serial.print(payload[4],DEC);Serial.print(", ");
//      Serial.println(payload[5],DEC);
  }
}






