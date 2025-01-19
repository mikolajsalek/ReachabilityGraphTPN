# ReachabilityGraphTPN
Reachability Graph for Time Petri Net

This algorithm is major part of my engineering thesis and https://github.com/bszawulak/HolmesPN, tool for analyzing petri net.

The main goal of algorithm, is to create reachability graph for finite Time Petri Net. It's integrated into Holmes, but you can easily use it locally, without Holmes, by setting holmes_on = false. 
Since Holmes is java-written programm and algorithm is written in C++, I've also provided integration between Java and C++ using JSON serializers, to read input from Holmes and output from algorithm itself, additonaly visualization has been made using GraphStream library and GUI to show that visualization.
