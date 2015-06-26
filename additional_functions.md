Additional Functions
=========

Original Version
----

3.32

Java Version
----

1.8

Required Library
----

* jfreechart-1.0.19.jar
* jcommon-1.0.23.jar
* commons-lang3-3.3.2.jar

You can get one-packege version at releases/studio_ny2.jar.

Bug Fix
-----------

* timestamp chart
* (Not Bug) add syntax highlight for new functions(sdev, svar, scov)

New Functions
--------------

* Create multi-axis chart.
* Show dictionary with table style.
* Completion enhancement
   - begins-with match completion
   - column completion before "from + [tablename]" or after "[tablename] + where"
   - variable completion
* Syntax highlight enhancement
   - variable highlight with dark green, variables will be updated when use completion(ctrl+space)
* Copy String (char list) into clipboard.


Multi-axis Chart Function
--------------

This version is able to create multi-axis chart. 

* Multi-Axis -  X-axis is only one, Y-axes are 1-5 and left-right axis is enabled.
* Chart Type - Line, Line-Marker, Bar, Scatter and OHLC.
* Show corss-hair for first item for each Y-axis.
* Show horizontal and vertical line for X-Y axis.
* Scroll with zoom-in.

![alt tag](https://raw.githubusercontent.com/Naoki-Yatsu/studio/master/meta/console_guide.png)

![alt tag](https://raw.githubusercontent.com/Naoki-Yatsu/studio/master/meta/multi-chart.png)



