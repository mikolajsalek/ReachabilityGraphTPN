# ReachabilityGraphTPN
Reachability Graph for Time Petri Net

This algorithm is major part of my engineering thesis. It's part of Holmes https://academic.oup.com/bioinformatics/article/33/23/3822/4076052?login=false tool, for analyzing petri net models.

The main goal of algorithm, is to create reachability graph for finite Time Petri Net. It's integrated into Holmes, but you can easily use it locally, without Holmes, by setting holmes_on = false. 
Since Holmes is java-written programm and algorithm is written in C++, I've also provided integration between Java and C++ using JSON serializers, to read input from Holmes and output from algorithm itself, additonaly visualization has been made using GraphStream library and GUI to show that visualization.

As an input, algorithm takes: 2D vector representing incidency matrix of petri net, vector representing p-marking of petri net and vector representing time intervals for the net. 

Files in this repository:
RG_TPN.cpp -> It's implementation of algorithm for creating reduced reachability graph from "Time and Petri Nets" Louchka Popova-Zeugmann.
HolmesStSpRGtpn.java -> It class that's responsible for integrating previous algorithm and java written Holmes. It's also creating not so complex GUI and visualization using GraphStream.
Graph_Click.java -> Java class with implemented functions regarding to interactive elements in GUI and graph visualization.




