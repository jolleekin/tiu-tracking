# Power Management #

### 1. Battery ###

![http://andrew.svp.co.uk/custom/images/products/CR2025_300.png](http://andrew.svp.co.uk/custom/images/products/CR2025_300.png)

Using this battery from digikey (0.96" Diameter,3V-1000mAh)

http://search.digikey.com/scripts/DkSearch/dksus.dll?Detail&name=P126-ND

Assume:
  * transmission time: 100ms every 10s period
  * current 100mA (RF + uC)
  * ignore sleep current

> _=> battery life = (1000mAh) / (100mA x 100ms/10s) = 1000 hrs_

In general:
  * C = capacity `[`mAh`]`
  * I = active current `[`mA`]`
  * T = transmit ratio

> |=> battery life (hours) = C/(I x T)|
|:----------------------------------|
```
    _ _                       _ _   
   |   | transmission time   |   |
 ->|   |<-                   |   |
_ _|   |_ _ _ _ _ _ _ _ _ _ _|   |_ _ _ _
       |<--- sleep time  --->|
   
```
We have this on the assumption that we _ignore the sleep current_. This is reasonable if RF modules and uC support sleep mode that leak current is only a couple of uA.

Example :


|Pico Power ATtiny13A|HOPE RFM12 |
|:-------------------|:----------|
|3.9-10 μA|0.3μA|

http://www.atmel.com/dyn/resources/prod_documents/doc8126_105.pdf

http://www.hoperf.com/upfile/RFM12.pdf


### 2. Transmission time ###
It is good to know the transmission time of the module so that we can calculate the transmission ratio.

Also, transmission time place an important role in collision avoidance (the shorter transmission time, the smaller chance of collision).

Tested:

  * atmega328p + RFM12
  * baud rate 19200, 434MHz
  * source TX.pde version 2 with a small modification :
```
long t;

t = micros(); // get time stamp 
send();
t = micros() - t; // transmission time

Serial.println(t,DEC); // serial monitor

delay(10); // 'll be replaced with sleep mode later


```

Theoretically, at 19200 baud rate, no start/stop bit, we have 19200/8 = 2400 byte per second, or **416us/byte**.

Test Results:
|sent|transmission time|
|:---|:----------------|
|10 bytes|6220 us|
|20 bytes|10164 us|
|30 bytes|14108 us|
|40 bytes|18060 us|

=> **~400us/byte** , overhead ~2000us. This confirms the theory.

in conclusion:
> |transmission time = overhead + (number of transmitted byte x 8/baudrate)|
|:-----------------------------------------------------------------------|

Now taking this result and applying to calculate battery life, e.g send 20 bytes will take about 10ms. Assuming 100mA active current, 10s of transmit interval:

> _=> battery life = 1000mAh /(100mA x 10ms/10s) = 10.000 hours_


### 3. ATmega328P - Bed Time ###
As said, the ATMega328P is Pico Power (hence, the P?) version. Other chip such as ATTiny13A also has Pico Power capability. While the ATTiny13A obviously consumes less power than ATMega328P in active mode (at 8MHz-5V, 6mA v.s 9mA; 4MHz-3V, 1.8mA v.s 2.5mA), at power down mode (in sleep mode) both chips consume approximately the same amount of current (3.2-10uA). We 'll discuss implementation power-down mode in ATMega328P with watchdog timer(I haven't get to the ATTiny yet, need a programmer).

Here's the DC characteristic from datasheet:

![https://tiu-tracking.googlecode.com/svn/trunk/power/New%20Picture.png](https://tiu-tracking.googlecode.com/svn/trunk/power/New%20Picture.png)

Quote:
```
The Power-down mode saves the register contents but freezes the Oscillator, disabling all other chip functions until the next interrupt or hardware reset. 

In Power-save mode, the asynchronous timer continues to run, allowing the user to maintain a timer base while the rest of the device is sleeping. 
The ADC Noise Reduction mode stops the CPU and all I/O modules except asynchronous timer and ADC, to minimize switching noise during ADC conversions. 

In Standby mode, the crystal/resonator Oscillator is running while the rest of the device is sleeping. This allows very fast start-up combined with low power consumption.
```

(It is interesting that power-save mode consumes even less power).

At this moment, I haven't implemented RF sleep mode yet, but the basic scheme usually is: transmit data -> MCU and RF go to sleep -> WDT on MCU wake-up -> so on... However if we allow the tag to be waked up by external sources (e.g when tag receives a report call from system), the MCU will need interrupt from RF module. This will be discussed later.

Below is the reference for sleep mode and wake up source, using WDT seems to be a good choice.

![https://tiu-tracking.googlecode.com/svn/trunk/power/New%20Picture_1.png](https://tiu-tracking.googlecode.com/svn/trunk/power/New%20Picture_1.png)

Below is the register bits for setting up power-down mode:

![https://tiu-tracking.googlecode.com/svn/trunk/power/New%20Picture_2.png](https://tiu-tracking.googlecode.com/svn/trunk/power/New%20Picture_2.png)

This is an example from Martin Nawrath.
Full description can be found at http://interface.khm.de/index.php/lab/experiments/sleep_watchdog_battery/
```

setup()
{
  // CPU Sleep Modes 
  // SM2 SM1 SM0 Sleep Mode
  // 0    0  0 Idle
  // 0    0  1 ADC Noise Reduction
  // 0    1  0 Power-down
  // 0    1  1 Power-save
  // 1    0  0 Reserved
  // 1    0  1 Reserved
  // 1    1  0 Standby(1)

  cbi( SMCR,SE );      // sleep enable, power down mode
  cbi( SMCR,SM0 );     // power down mode
  sbi( SMCR,SM1 );     // power down mode
  cbi( SMCR,SM2 );     // power down mode

  setup_watchdog(7);
}


void loop()
{
    // do something
    system_sleep();
}


//****************************************************************  
// set system into the sleep state 
// system wakes up when wtchdog is timed out
void system_sleep() 
{

  cbi(ADCSRA,ADEN);                    // switch Analog to Digitalconverter OFF

  set_sleep_mode(SLEEP_MODE_PWR_DOWN); // sleep mode is set here
  sleep_enable();

  sleep_mode();                        // System sleeps here

  sleep_disable();                     // System continues execution here when watchdog timed out 
  sbi(ADCSRA,ADEN);                    // switch Analog to Digitalconverter ON

}

//****************************************************************
// 0=16ms, 1=32ms,2=64ms,3=128ms,4=250ms,5=500ms
// 6=1 sec,7=2 sec, 8=4 sec, 9= 8sec
void setup_watchdog(int ii) 
{

  byte bb;
  int ww;
  if (ii > 9 ) ii=9;
    bb=ii & 7;
  if (ii > 7) 
    bb|= (1<<5);
  bb|= (1<<WDCE);
  ww=bb;
 
  MCUSR &= ~(1<<WDRF);
  // start timed sequence
  WDTCSR |= (1<<WDCE) | (1<<WDE);
  // set new watchdog timeout value
  WDTCSR = bb;
  WDTCSR |= _BV(WDIE);


}
//****************************************************************  
// Watchdog Interrupt Service / is executed when  watchdog timed out
ISR(WDT_vect) 
{
  // do something
}

```

(Test data will be updated later).