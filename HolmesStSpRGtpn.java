package holmes.windows.statespace;

import com.google.gson.JsonObject;
import holmes.graphpanel.GraphPanel;
import holmes.darkgui.GUIManager;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.data.PetriNetMethods;
import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.Node;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import holmes.utilities.ColorPalette;
import holmes.utilities.Tools;
import holmes.windows.HolmesNotepad;
import holmes.petrinet.elements.extensions.TransitionTimeExtention;
import javax.swing.*;
import javax.swing.plaf.nimbus.State;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.*;
import holmes.windows.statespace.Graph_Click;


import org.graphstream.algorithm.Toolkit;
import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;
import org.graphstream.ui.geom.Point2;
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.graphicGraph.*;
import org.graphstream.ui.spriteManager.*;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.graphstream.ui.swing_viewer.DefaultView;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerListener;
import org.graphstream.ui.view.ViewerPipe;
import org.graphstream.ui.view.camera.Camera;
import org.graphstream.ui.view.util.GraphMetrics;
import org.graphstream.ui.view.util.InteractiveElement;
import org.graphstream.ui.view.util.MouseManager;

class StateData {
    List<Double> pMarking;
    double stateName;
    List<Double> toClock;

    public StateData(List<Double> pMarking, double stateName, List<Double> toClock){
        this.pMarking = pMarking;
        this.stateName = stateName;
        this.toClock = toClock;
    }

    @Override
    public String toString() {
        return "StateData{" +
                "pMarking=" + pMarking +
                ", stateName=" + stateName +
                ", toClock=" + toClock +
                '}';
    }
}

public class HolmesStSpRGtpn extends JFrame {
    private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
    private static JTextArea logField1stTab = null;
    
    //komponenty:
    private JPanel mainPanel;
    private JPanel upperPanel;
    private JPanel lowerPanel;

    public HolmesStSpRGtpn() {
        this.setTitle("State space analysis");
        try {
            setIconImage(Tools.getImageFromIcon("/icons/holmesicon.png"));
        } catch (Exception ex) {
            GUIManager.getDefaultGUIManager().log("Error (533315487) | Exception:  "+ex.getMessage(), "error", true);
        }

        //oblokowuje główne okno po zamknięciu tego
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                overlord.getFrame().setEnabled(true);
            }
        });
        overlord.getFrame().setEnabled(false); //blokuj główne okno
        setResizable(false);
        initializeComponents();
        setVisible(true);
    }

    /**
     * Metoda tworząca główne sekcje okna.
     */
    private void initializeComponents() {
        this.setLocation(20, 20);

        setLayout(new BorderLayout());
        setSize(new Dimension(1024, 768));
        setLocation(50, 50);
        setResizable(false);

        mainPanel = new JPanel();
        mainPanel.setLayout(null);
        mainPanel.setBounds(0, 0, 1024, 768);
        mainPanel.setLocation(0, 0);
        mainPanel.add(uppedPanel());
        mainPanel.add(lowerPanel());
        add(mainPanel, BorderLayout.CENTER);
    }

    private JTextArea ShortestPathDetails = new JTextArea("Displaying shortest path details.");

    private JPanel uppedPanel() {
        upperPanel = new JPanel();
        upperPanel.setLayout(null);
        upperPanel.setBounds(0, 0, mainPanel.getWidth()-20, 200);
        upperPanel.setLocation(0, 0);
        upperPanel.setBorder(BorderFactory.createTitledBorder("First panel:"));

        int panX = 20;
        int panY = 20;

        JButton button1 = new JButton("Display states details");
        button1.setText("<html><center>Display states<br />details<center></html>");
        button1.setBounds(panX, panY, 150, 40);
        button1.setMargin(new Insets(0, 0, 0, 0));
        button1.setFocusPainted(false);
        upperPanel.add(button1);

        JButton button2 = new JButton("RG for TPN");
        button2.setText("<html><center>Create<br />RG for TPN<center></html>");
        button2.setBounds(panX+160, panY, 150, 40);
        button2.setMargin(new Insets(0, 0, 0, 0));
        //button2.setIcon(Tools.getResIcon32("/icons/stateSim/simpleSimTab.png"));
        button2.addActionListener(actionEvent -> {
            manage_RGtpn();
        });
        button2.setFocusPainted(false);
        upperPanel.add(button2);

        JButton button3 = new JButton("Show involved transitions.");
        button3.setText("<html><center>Show involved<br />transitions<center></html>");
        button3.setBounds(panX+320, panY, 150, 40);
        button3.setMargin(new Insets(0, 0, 0, 0));
        button3.addActionListener(actionEvent -> {
            show_involved_transiton_on_main_net();
        });
        button3.setFocusPainted(false);
        upperPanel.add(button3);

        JTextArea StateDetails = new JTextArea("Displaying states details.");
        StateDetails.setEditable(false); // Make it non-editable
        StateDetails.setLineWrap(true);
        StateDetails.setWrapStyleWord(true);

        JScrollPane scrollPaneStateDetails = new JScrollPane(StateDetails);
        scrollPaneStateDetails.setBounds(panX, panY + 50, 500, 120);
        upperPanel.add(scrollPaneStateDetails);

        button1.addActionListener(actionEvent -> {

            display_states_details(are_added_to_display, StateDetails);


        });

        ShortestPathDetails.setEditable(false);
        ShortestPathDetails.setLineWrap(true);
        ShortestPathDetails.setWrapStyleWord(true);

        JScrollPane scrollPaneShortestPathDetails = new JScrollPane(ShortestPathDetails);
        scrollPaneShortestPathDetails.setBounds(panX + 525, panY + 50, 425, 120);
        upperPanel.add(scrollPaneShortestPathDetails);


        return upperPanel;
    }



    private JPanel lowerPanel() {
        lowerPanel = new JPanel(new BorderLayout());
        lowerPanel.setLayout(null);
        lowerPanel.setBounds(0, 0, mainPanel.getWidth()-20, 550);
        lowerPanel.setLocation(0, upperPanel.getHeight());

        lowerPanel.setBorder(BorderFactory.createTitledBorder("Second panel:"));


        logField1stTab = new JTextArea();
        logField1stTab.setLineWrap(true);
        logField1stTab.setEditable(true);
        logField1stTab.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        DefaultCaret caret = (DefaultCaret)logField1stTab.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        JPanel logFieldPanel = new JPanel();
        logFieldPanel.setLayout(new BorderLayout());
        logFieldPanel.add(new JScrollPane(logField1stTab), BorderLayout.CENTER);
        logFieldPanel.setBounds(10, 20, lowerPanel.getWidth()-35, lowerPanel.getHeight()-50);
        lowerPanel.add(logFieldPanel);

        return lowerPanel;
    }

    public JPanel getLowerPanel() {
        return lowerPanel;
    }

    public JPanel getUpperPanel() {
        return upperPanel;
    }

    public JTextArea getShortestPathDetails() {
        return ShortestPathDetails;
    }

    private static void show_involved_transiton_on_main_net(){
        //powinno to dzialac tak, ze dostanie liste tranzycji, ktore biora udzial w grafie, potem
        //przeleci przez wszystkie tranzycje w grafie i zmieni kolor tym, ktore matchuja :)

        ArrayList<Transition> transitions = overlord.getWorkspace().getProject().getTransitions();

        System.out.println(transitions_involved_in_graph);
        
        for(String transition : transitions_involved_in_graph){

            int transition_ID = Integer.parseInt(transition.replace("t", ""));

            transitions.get(transition_ID).setGlowedSub(true);

        }

    }

    private static void display_states_details(List<StateData> states, JTextArea StateDetails) {

        StringBuilder detailsBuilder = new StringBuilder();
        HolmesNotepad notePad = new HolmesNotepad(800, 500);
        notePad.setVisible(true);

        for (StateData state : states) {
            detailsBuilder.append("State Name: ").append(state.stateName).append("\n");
            notePad.addTextLine("State Name: " + state.stateName + "\n", "text");
            detailsBuilder.append("P Marking: ").append(state.pMarking).append("\n");
            notePad.addTextLine("P Marking: " + state.pMarking + "\n", "text");
            detailsBuilder.append("Transitions Clocks: ").append(state.toClock).append("\n");
            notePad.addTextLine("Transitions Clocks: " + state.toClock + "\n", "text");
            detailsBuilder.append("\n"); // Separate states
            notePad.addTextLine("\n", "text");

        }

        StateDetails.setText(detailsBuilder.toString());


    }

    private void manage_RGtpn() {

        Map<String, Object> Data_From_Holmes = GatherDataFromHolmes();

        StringBuilder Input_To_Graph = ProcessingCPP(Data_From_Holmes);

        Graph graph = GraphVisualization(Input_To_Graph);

        new Graph_Click(graph, getLowerPanel(), getShortestPathDetails());

    }

    private static Map<String, Object> GatherDataFromHolmes() {

        Gson gson = new Gson();
        ArrayList<Transition> transitions = overlord.getWorkspace().getProject().getTransitions();
        ArrayList<Place> places = overlord.getWorkspace().getProject().getPlaces();
        ArrayList<Arc> arcs = overlord.getWorkspace().getProject().getArcs();

        // Tworzymy macierz incydencji
        int numPlaces = places.size(); // Liczba miejsc
        int numTransitions = transitions.size(); // Liczba tranzycji
        int[][] incidenceMatrix = new int[numPlaces][numTransitions]; // Macierz incydencji

        // Inicjalizujemy macierz incydencji zerami
        for (int i = 0; i < numPlaces; i++) {
            for (int j = 0; j < numTransitions; j++) {
                incidenceMatrix[i][j] = 0;
            }
        }

        for (Arc arc : arcs) {
            Node startNode = arc.getStartNode();
            Node endNode = arc.getEndNode();

            if(startNode instanceof Place && endNode instanceof Transition) {
                //luk z miejsca do tranzycji
                int placeIndex = places.indexOf(startNode);
                int transitionIndex = transitions.indexOf(endNode);

                if (placeIndex != -1 && transitionIndex != -1) {
                    incidenceMatrix[placeIndex][transitionIndex] -= arc.getWeight();
                }
            }else if (startNode instanceof Transition && endNode instanceof Place) {
                int placeIndex = places.indexOf(endNode);
                int transitionIndex = transitions.indexOf(startNode);

                if(placeIndex != -1 && transitionIndex != -1) {
                    incidenceMatrix[placeIndex][transitionIndex] += arc.getWeight();
                }


            }
        }

        System.out.println("Incidence Matrix:");
        for (int i = 0; i < numPlaces; i++) {
            for (int j = 0; j < numTransitions; j++) {
                System.out.print(incidenceMatrix[i][j] + ", ");
            }
            System.out.println();
        }

        double[][] timeIntervals = new double[numTransitions][2];
        int[] p_marking = new int[numPlaces];
        int x = 0;
        // Wyświetlamy szczegóły tranzycji i miejsc
        if (!transitions.isEmpty()) {
            logField1stTab.append("Transitions in the project:\n");

            for (Transition t : transitions) {

                logField1stTab.append("Transition: " + t.getName() + "\n");

                if (t.getTransType() == Transition.TransitionType.TPN){
                    double eft = t.timeExtension.getEFT();
                    double lft = t.timeExtension.getLFT();

                    timeIntervals[x][0] = eft;
                    timeIntervals[x][1] = lft;


                }else{
                    //If transition type != TPN
                    timeIntervals[x][0] = 0;
                    timeIntervals[x][1] = 0;
                }
                x++;


                // Pobierz miejsca wejściowe dla każdej tranzycji
                ArrayList<Place> inputPlaces = t.getInputPlaces();
                for (Place p : inputPlaces) {
                    logField1stTab.append("  Input place: " + p.getName() + "\n");
                }

                // Pobierz miejsca wyjściowe dla każdej tranzycji
                ArrayList<Place> outputPlaces = t.getOutputPlaces();
                for (Place p : outputPlaces) {
                    logField1stTab.append("  Output place: " + p.getName() + "\n");
                }

                if (t.getTransType() == Transition.TransitionType.TPN) {
                    double eft = t.timeExtension.getEFT();
                    double lft = t.timeExtension.getLFT();
                    logField1stTab.append("  EFT: " + eft + "\n");
                    logField1stTab.append("  LFT: " + lft + "\n");
                }
                else
                {
                    t.setTransType(Transition.TransitionType.TPN);
                    double eft = 0.0;
                    double lft = 10.0;
                    logField1stTab.append("  EFT: " + eft + "\n");
                    logField1stTab.append("  LFT: " + lft + "\n");
                }

            }
        } else {
            logField1stTab.append("No transitions in the project.\n");
        }

        int index = 0;
        if (!places.isEmpty()) {
            logField1stTab.append("P-marking poczatkowy: \n");
            for (Place p : places) {
                p_marking[index++] = p.getTokensNumber();
            }

            logField1stTab.append("Places in the project:\n");
            for (Place p : places) {
                logField1stTab.append("Place: " + p.getName() + "\n");

                // Pobierz tranzycje wejściowe dla każdego miejsca
                ArrayList<Transition> inputTransitions = p.getInputTransitions();
                for (Transition t : inputTransitions) {
                    logField1stTab.append("  Input transition: " + t.getName() + "\n");
                }

                // Pobierz tranzycje wyjściowe dla każdego miejsca
                ArrayList<Transition> outputTransitions = p.getOutputTransitions();
                for (Transition t : outputTransitions) {
                    logField1stTab.append("  Output transition: " + t.getName() + "\n");
                }
            }

        } else {
            logField1stTab.append("No places in the project.\n");
        }

        Map<String, Object> data_to_cpp = new HashMap<>();
        data_to_cpp.put("incidenceMatrix", incidenceMatrix);
        data_to_cpp.put("timeIntervals", timeIntervals);
        data_to_cpp.put("pmarking", p_marking);

        return data_to_cpp;

    }

    private static StringBuilder ProcessingCPP(Map<String, Object> data_to_cpp) {

        Gson gson = new Gson();

        String json = gson.toJson(data_to_cpp);

        logField1stTab.append("\n\n\n\n");
        logField1stTab.append(json);

        System.out.println(json);
        StringBuilder jsonBuilder = new StringBuilder();

        try {
            String workingDirectory = System.getProperty("user.dir");
            System.out.println(workingDirectory);


            String cppSourcePath = "C:/Users/T14/OneDrive/Pulpit/Inzynierka";
            String compileCommand = "g++ -o algorytm.exe";

            Process compileProcess = Runtime.getRuntime().exec(compileCommand);

            int compileExitCode = compileProcess.waitFor();
            if (compileExitCode == 0) {
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(compileProcess.getErrorStream()));
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    System.err.println(errorLine);
                }
            }
            System.out.println("C++ compiled successfully");

            //String cppProgramPath = "C:/Users/T14/OneDrive/Pulpit/Inzynierka/algorytm.exe";
            String cppProgramPath = workingDirectory + File.separator + "Holmes" + File.separator + "scripts" + File.separator + "RG_TPN" + File.separator + "src" + File.separator + "algorytm.exe";

            //String cppProgramPath = "C:/Users/T14/IdeaProjects/HolmesPN/Holmes/scripts/RG_TPN/algorytm.exe";
            System.out.println("Checking the path to RG_TPN.exe: " + cppProgramPath);

            //System.out.println("C++ program path: " + cppProgramPath);
            //System.out.println(cppProgramPath);

            ProcessBuilder processBuilder = new ProcessBuilder(cppProgramPath);
            Process process = processBuilder.start();

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            writer.write(json);
            writer.newLine();
            writer.flush();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String response;

            System.out.println("Response from C++ programm:");
            while ((response = reader.readLine()) != null) {
                System.out.println(response);
                jsonBuilder.append(response);
            }
            process.waitFor();


        }catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }


        return jsonBuilder;

    }

    public static List<StateData> are_added_to_display = new ArrayList<StateData>();
    public static Set<String> transitions_involved_in_graph = new HashSet<>();
    private static Graph GraphVisualization(StringBuilder jsonBuilder) {

        String jsonInput = jsonBuilder.toString();
        System.setProperty("org.graphstream.ui", "swing"); // or "j2d" for a 2D viewer

        Graph graph = new MultiGraph("RG_TPN");
        graph.setStrict(false);

        Gson gson_RG_tpn = new Gson();
        java.lang.reflect.Type listType = new TypeToken<List<Map<String, Object>>>() {}.getType();
        List<Map<String, Object>> edges = gson_RG_tpn.fromJson(jsonInput, listType);

        for (Map<String, Object> edge : edges) {
            Map<String, Object> oldState = (Map<String, Object>) edge.get("old_state");
            Map<String, Object> newState = (Map<String, Object>) edge.get("new_state");

            int oldStateName = ((Double) oldState.get("state_name")).intValue();
            int newStateName = ((Double) newState.get("state_name")).intValue();
            String transitionName = (String) edge.get("transition");

            transitions_involved_in_graph.add(transitionName);

            String time = String.valueOf(edge.get("time"));

            String oldStateNameString = String.valueOf(oldStateName);
            String newStateNameString = String.valueOf(newStateName);

            if(graph.getNode(oldStateName) == null){

                graph.addNode(oldStateNameString);
                List<Double> pMarking = (List<Double>) oldState.get("p_marking");
                List<Double> toClock = (List<Double>) oldState.get("to_clock");
                Double stateName = (Double) oldState.get("state_name");

                StateData stateData = new StateData(pMarking, stateName, toClock);
                are_added_to_display.add(stateData);

                org.graphstream.graph.Node node_old = graph.getNode(oldStateNameString);
                node_old.setAttribute("ui.label", oldStateNameString);
                node_old.setAttribute("ui.style", "text-offset: -10");

            }
            if(graph.getNode(newStateName) == null){
                graph.addNode(newStateNameString);
                List<Double> pMarking = (List<Double>) newState.get("p_marking");
                List<Double> toClock = (List<Double>) newState.get("to_clock");
                Double stateName = (Double) newState.get("state_name");

                StateData stateData = new StateData(pMarking, stateName, toClock);
                are_added_to_display.add(stateData);

                org.graphstream.graph.Node node_new = graph.getNode(newStateNameString);
                node_new.setAttribute("ui.label", newStateNameString);
                node_new.setAttribute("ui.style", "text-offset: -10");

            }
            String edgeId = oldStateNameString + "-" + newStateNameString + "-" + transitionName + "-" + time;
            System.out.println(edgeId);

            if (edgeId != null){
                graph.addEdge(edgeId, oldStateNameString, newStateNameString, true);
                graph.getEdge(edgeId).setAttribute("ui.label", transitionName + ", " + time);
                graph.getEdge(edgeId).setAttribute("text-alignment", "along");

            }

        }

        // Set Graph Styling
        graph.setAttribute("ui.quality");
        graph.setAttribute("ui.antialias");
        graph.setAttribute("ui.stylesheet", "edge { text-alignment: along; text-background-mode: rounded-box; text-background-color: white; }"); //jest along

        graph.nodes().forEach(n -> n.setAttribute("ui.style", "fill-color: red; size: 10px;"));
        graph.edges().forEach(e -> e.setAttribute("ui.style", "fill-color: black; size: 2px;"));

        graph.nodes().forEach(n1 -> {
            graph.nodes().forEach(n2 -> {

                if(!n1.equals(n2)){
                    long edgeCount = graph.edges().filter(e->e.getSourceNode().equals(n1) && e.getTargetNode().equals(n2)).count();

                    if(edgeCount > 1) {

                        int[] offSet = {10};

                        graph.edges().filter(e->e.getSourceNode().equals(n1) && e.getTargetNode().equals(n2)).forEach(e -> {
                            e.setAttribute("ui.style", "text-offset: " + offSet[0] + ";");

                            offSet[0] += -20;
                        });

                    }

                }

            });
        });

        return graph;

    }


}
