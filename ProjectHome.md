# Introduction #
TIU tracking system is a capstone project conducted by Portland State University students and sponsored by Intel Corp. The system were used as a demo to track locations of Test Interface Units (TIUs) in the Intel Validation Lab.

The primary figures of merit that were used in the decision making process include **accuracy, power consumption, size, and cost**.


# Hardware #
### Tag ###

  * Size: 1”x1”x1”
  * ATMega328p MCU
  * RF12B transceiver at 434MHz
  * 20mm coin cell battery
  * Battery life: at least 1 months
  * Cost : $25

![https://tiu-tracking.googlecode.com/svn/trunk/Docs/Images/Tag.jpg](https://tiu-tracking.googlecode.com/svn/trunk/Docs/Images/Tag.jpg)

### Detector ###

  * Size: 3.5”x1”
  * ATMega328p MCU
  * RF12B transceiver at 434MHz
  * 9V battery/adapter
  * Cost: $30

https://tiu-tracking.googlecode.com/svn/trunk/Docs/Images/Detector.JPG

# System Overview #

The main goal of the project aims to achieve a low-cost, yet effective tracking system.


The infrastructure of the tracking system is built upon RF transceivers which communicate wirelessly in a mesh network. Asset tags attached to tracked devices (TIUs) broadcast messages to detectors periodically. Messages that contain received signal strength indication (RSSI) values will be routed though the mesh network to a server. Remote Server is responsible for analyzing the received data, calculates the locations, and stores them in the database. A web application will then be able to access the database and display location information in an interactive 2D map.

![https://tiu-tracking.googlecode.com/svn/trunk/Docs/Images/System.png](https://tiu-tracking.googlecode.com/svn/trunk/Docs/Images/System.png)


# Web App #

![https://tiu-tracking.googlecode.com/svn/trunk/Docs/Images/web-app.png](https://tiu-tracking.googlecode.com/svn/trunk/Docs/Images/web-app.png)

# Demo video #
http://www.youtube.com/embed/sZbXoZNrWNc

# Award #
First Place - 2011 ECE Outstanding Capstone Project
http://www.ece.pdx.edu/NewsCurrentEvents/CapstoneWinners2011.php

# Poster #
https://tiu-tracking.googlecode.com/svn/trunk/Docs/CPC-BestOverall2011.pdf
![https://tiu-tracking.googlecode.com/svn/trunk/Docs/Images/CPC-BestOverall2011_02.png](https://tiu-tracking.googlecode.com/svn/trunk/Docs/Images/CPC-BestOverall2011_02.png)