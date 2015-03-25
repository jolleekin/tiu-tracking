## Demo package: transmit/receive/Analog-RSSI using RFM12 module - version 1.0 ##

source https://tiu-tracking.googlecode.com/svn/trunk/

This package demonstrates wireless communication on hobby frequency
band (i.e 433MHz...) using RFM12B transceiver modules and ATMega328 uC.

![https://tiu-tracking.googlecode.com/svn/trunk/rf12_tx_rx_v1/schematic/3296687815_007f613b6b_o.png](https://tiu-tracking.googlecode.com/svn/trunk/rf12_tx_rx_v1/schematic/3296687815_007f613b6b_o.png)

Our purpose is testing these RFM12B module functions: transmit/receive data and Analog-RSSI, which could be considered applying for our project later.

There are two different approaches will be introduced:
  * polling, no checksum based on Benedikt's library
  * interrupt with checksum based on RF12 arduino library by Jeelabs


### Notes: ###
  * No collision avoidance is considered yet in this version
  * To get ARSSI output, you may have to manually solder on a wire to a resistor to access this pad (location shown in red circle)(thanks to Stephen Eaton).
  * Schematic provided by Jeelabs, used for both transmitting receiving modules
  * Antennas required for best performance, Ideally at 433MHz ~ 17cm length
  * Yeah, I used arduino board to program the chips and monitor the serial outputs

![https://tiu-tracking.googlecode.com/svn/trunk/rf12_tx_rx_v1/schematic/arssi.png](https://tiu-tracking.googlecode.com/svn/trunk/rf12_tx_rx_v1/schematic/arssi.png)


_**update:**_
  * 1. code version 2 posted: https://tiu-tracking.googlecode.com/svn/trunk/rf12_tx_rx_v2
  * 2. fuse setting for chips that are off-arduino and 3.3V rail: (you need a programmer, and probably avrStudio)

![https://tiu-tracking.googlecode.com/svn/trunk/power/fuse.png](https://tiu-tracking.googlecode.com/svn/trunk/power/fuse.png)