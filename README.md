# Reachability Graph for Time Petri Net
This algorithm is a major component of my engineering thesis and is integrated into the Holmes tool https://doi.org/10.1093/bioinformatics/btx492, designed for analyzing Petri net models.

The primary goal of the algorithm is to generate a reachability graph for finite Time Petri Nets. While it is integrated into Holmes, it can also be run locally by setting holmes_on = false. Since Holmes is written in Java and the algorithm is implemented in C++, I provided an integration layer using JSON serialization to handle input from Holmes and output from the algorithm. Additionally, the visualization was implemented using the GraphStream library, and a GUI was developed to display the visualization interactively.

## Input:
The algorithm requires the following inputs:

* A 2D vector representing the incidence matrix of the Petri net.
* A vector representing the P-marking of the Petri net.
* A vector representing the time intervals of the net.
## Repository Files:
* RG_TPN.cpp: Implements the algorithm for creating a reduced reachability graph, based on "Time and Petri Nets" by Louchka Popova-Zeugmann.
* HolmesStSpRGtpn.java: Integrates the algorithm with Holmes, provides a simple GUI, and enables visualization using GraphStream.
* Graph_Click.java: Implements functions for interactive elements in the GUI and graph visualization.
