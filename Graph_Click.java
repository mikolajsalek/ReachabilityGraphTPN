package holmes.windows.statespace;

import org.graphstream.algorithm.AStar;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.stream.ProxyPipe;
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerListener;
import org.graphstream.ui.view.ViewerPipe;
import org.graphstream.ui.view.camera.Camera;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Graph_Click implements ViewerListener {

    protected boolean loop = true;
    private Graph graph;
    private JTextArea ShortestPathDetails;


    public static void main(String args[]) {

        System.setProperty("org.graphstream.ui", "swing");
        Graph graph = new MultiGraph("RG");
        JPanel lowerPanel = new JPanel();
        JTextArea ShortestPathDetails = new JTextArea("Displaying shortest path details.");
        new Graph_Click(graph, lowerPanel, ShortestPathDetails);

    }

    Viewer viewer;
    public Graph_Click(Graph graph, JPanel lowerPanel, JTextArea ShortestPathDetails) {
        // We do as usual to display a graph. This
        // connect the graph outputs to the viewer.
        // The viewer is a sink of the graph.
        this.graph = graph;
        this.ShortestPathDetails = ShortestPathDetails;

        viewer = graph.display();
        System.out.println("Graph is being displayed.");

        ProxyPipe pipe = viewer.newViewerPipe();
        pipe.addAttributeSink(graph);
        // The default action when closing the view is to quit
        // the program.
        viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.HIDE_ONLY);

        // We connect back the viewer to the graph,
        // the graph becomes a sink for the viewer.
        // We also install us as a viewer listener to
        // intercept the graphic events.
        ViewerPipe fromViewer = viewer.newViewerPipe();
        fromViewer.addViewerListener(this);
        fromViewer.addSink(graph);

        View view = viewer.getDefaultView();
        Camera camera = view.getCamera();
        view.enableMouseOptions();

        JComponent swingComponent = (JComponent) view;

        initializeCheckboxPanel();

        // Set up the graph viewer
        // Set up graph viewer as before
        setupViewer();

        JComponent graphComponent = (JComponent) view;

        lowerPanel.removeAll();
        lowerPanel.setLayout(new BorderLayout());

        lowerPanel.add(graphComponent, BorderLayout.CENTER);

        lowerPanel.revalidate();
        lowerPanel.repaint();


        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(100); // Prevent 100% CPU usage
                    pipe.pump();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();


        swingComponent.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                handleMouseWheel(e, camera);
            }
        });

        swingComponent.addMouseListener(new MouseListener() {
           @Override
            public void mouseClicked(MouseEvent e) {

           }

           @Override
            public void mousePressed(MouseEvent e) {
               if (SwingUtilities.isLeftMouseButton(e)){
                   handleMouseLPM(e, camera);
               }

           }

           @Override
            public void mouseReleased(MouseEvent e) {
               if (SwingUtilities.isLeftMouseButton(e)){
                   handleMouseLPM(e, camera);
               }
           }

           @Override
            public void mouseEntered(MouseEvent e) {

           }

           @Override
            public void mouseExited(MouseEvent e) {

           }


        });

        swingComponent.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
               handleMouseLPM(e, camera);
            }
        });




    }
    private JPanel checkboxPanel;
    private JButton applyButton;

    private void initializeCheckboxPanel() {
        // Create the left panel to hold checkboxes

        checkboxPanel = new JPanel();
        checkboxPanel.setLayout(new BoxLayout(checkboxPanel, BoxLayout.Y_AXIS)); // Stack components vertically
        checkboxPanel.setPreferredSize(new Dimension(150, 0)); // Set width of the panel

        List<JCheckBox> nodeCheckboxes = new ArrayList<>();
        AtomicInteger selectedCount = new AtomicInteger(0);
        String[] selectedNodes = new String[2];

        // Add checkboxes for each node
        for (Node node : graph) {
            JCheckBox nodeCheckbox = new JCheckBox("State " + node.getId());
            nodeCheckbox.setSelected(false); // Initial state (unchecked)

            nodeCheckboxes.add(nodeCheckbox);

            // When the checkbox is clicked, show the state information for the node
            nodeCheckbox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (nodeCheckbox.isSelected()) {
                        // Show marking and clocks for the selected node
                        displayNodeStateInfo(node);

                    } else {
                        // Hide marking and clocks if checkbox is unchecked
                        hideNodeStateInfo(node);
                    }

                    if(nodeCheckbox.isSelected()){
                        if(selectedCount.get() < 2){
                            selectedCount.incrementAndGet();

                        } else{
                            nodeCheckbox.setSelected(false);
                            JOptionPane.showMessageDialog(
                                    null,
                                    "You can only select up to 2 states.",
                                    "Limit Reached",
                                    JOptionPane.WARNING_MESSAGE
                            );
                        }
                    } else {
                        selectedCount.decrementAndGet();

                    }
                    nodeCheckboxes.forEach(cb -> cb.setEnabled(selectedCount.get() < 2 || cb.isSelected()));


                }
            });

            checkboxPanel.add(nodeCheckbox);
        }

        applyButton = new JButton("Apply");
        applyButton.addActionListener(e -> {

            int selectedIndex = 0;

            for(JCheckBox checkBox : nodeCheckboxes) {
                if (checkBox.isSelected()){
                    Node node = graph.getNode(checkBox.getText().replace("State ", ""));

                    if (selectedIndex < 2){
                        selectedNodes[selectedIndex] = node.getId();
                        selectedIndex++;
                    }
                }

            }

            if (selectedNodes[0] != null && selectedNodes[1] != null) {
                String source = selectedNodes[0];
                String destination = selectedNodes[1];
                System.out.println("Source: " + source + " Destination: " + destination);
                shortest_path(source, destination); // Call the shortest path method
            } else {
                JOptionPane.showMessageDialog(
                        null,
                        "Please select exactly two states before applying.",
                        "Selection Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }


        });

        // Add checkbox panel to the left side of the graph viewer
        JComponent swingComponent = (JComponent) viewer.getDefaultView();
        swingComponent.setLayout(new BorderLayout());
        swingComponent.add(checkboxPanel, BorderLayout.WEST); // Add panel to the left side

        checkboxPanel.add(applyButton);

    }

    private void shortest_path(String source, String destination){
        //System.out.println(source + destination);
        AStar astar = new AStar(graph);
        astar.setCosts(new AStar.DistanceCosts());
        astar.setSource(source);
        astar.setTarget(destination);
        astar.compute();

        List<Edge> shortest_path_edges = astar.getShortestPath().getEdgePath();

        Set<String> transitions_in_path = new HashSet<>();
        for (Edge edge : shortest_path_edges) {
            String edgeId = edge.getId();
            String transition = edgeId.split("-")[2];

            transitions_in_path.add(transition);
        }
        System.out.println("Used transition in path: " + transitions_in_path);


        System.out.println(shortest_path_edges);

        StringBuilder shortest_path_builder = new StringBuilder();
        shortest_path_builder.append("Shortest path between states: ").append(source).append("->").append(destination).append("\n");
        shortest_path_builder.append("Transitions involved in path: ").append(transitions_in_path).append("\n");
        if(astar.getShortestPath() != null){
            String ShortestPath = astar.getShortestPath().toString();
            shortest_path_builder.append("Shortest Path: ").append(ShortestPath).append("\n");
            ShortestPathDetails.setText(shortest_path_builder.toString());
        }else{
            ShortestPathDetails.setText("There is no connection between states: " + source + " and " + destination);
        }









        //shortest path daje nam info o najkrotszej sciezce miedzy stanami, teraz chcialbym, zeby poza wierzcholkami, zostaly
        //wypisane tranzycje, ktore biora udzial w najkrotszym mozliwym przejsciu stany od stanu i chcialbym zeby one byly
        //jakos zaznaczone na tej OG sieci :)



    }


    private void displayNodeStateInfo(Node node) {
        // Retrieve and display the marking and clocks of the node
        String nodeId = node.getId();
        // Here you can extract the state marking and clocks from your node or external data
        System.out.println("Displaying state info for Node " + nodeId);
        // Example: Add state info to the UI (customize as needed)

    }

    private void hideNodeStateInfo(Node node) {
        // Hide the state information when checkbox is unchecked
        //node.removeAttribute("ui.label");
    }

    private void setupViewer() {
        viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.HIDE_ONLY);
        viewer.getDefaultView().enableMouseOptions();
    }

    private void scaling_labels(double zoom){
        final double BaseLabelSize_node = 10.0;
        final double BaseIconSize_node = 10.0;
        final double scaledSizeLabel_node = Math.max(10.0, Math.min(BaseLabelSize_node / zoom, 20.0));
        final double scaledIconSize_node = BaseIconSize_node/zoom;

        //scaledSizeLabel_node = Math.max(10.0, Math.min(scaledSizeLabel_node, 40.0));

        for (Node node : graph) {
            String style = String.format(Locale.US, "text-size: %.1fpx;", scaledSizeLabel_node);

            node.setAttribute("ui.style", style);

            String node_icon = String.format(Locale.US, "size: %.1fpx;", scaledIconSize_node);
            node.setAttribute("ui.style", node_icon);

        }

        graph.edges().forEach(edge -> {
            String edge_label = String.format(Locale.US, "text-size: %.1fpx;", scaledSizeLabel_node);
            System.out.println(edge_label);
            edge.setAttribute("ui.style", edge_label);
        });

    }

    //dziala przyblizanie/oddalanie
    private void handleMouseWheel(MouseWheelEvent e, Camera camera) {

        int wheelRotation = e.getWheelRotation();

        double x = e.getX();
        double y = e.getY();

        Point3 mouse_coords = camera.transformPxToGu(x, y);


        // Get the current zoom factor (view percent)
        double zoomFactor = camera.getViewPercent();

        // Adjust zoom factor based on scroll direction
        if (wheelRotation < 0) { // Zoom in (scroll up)
            zoomFactor *= 1.1; // Zoom in by 10%
            scaling_labels(zoomFactor);

        } else { // Zoom out (scroll down)
            zoomFactor /= 1.1; // Zoom out by 10%
            scaling_labels(zoomFactor);
        }

        // Limit the zoom factor to prevent extreme zoom levels
        zoomFactor = Math.max(0.1, Math.min(zoomFactor, 5.0)); // Limit zoom range

        // Apply zoom to the camera
        camera.setViewPercent(zoomFactor);

    }


    //bullshit nie dziala
    private static final double THRESHOLD = 20.0; // 20px for the node icon size
    private Point3 lastMousePosition = null;

    private void handleMouseLPM(MouseEvent e, Camera camera) {
        if (e.getID() == MouseEvent.MOUSE_PRESSED) {
            lastMousePosition = camera.transformPxToGu(e.getX(), e.getY());
        } else if (e.getID() == MouseEvent.MOUSE_DRAGGED && lastMousePosition != null) {

            Point3 currentMousePosition = camera.transformPxToGu(e.getX(), e.getY());

            // Calculate the difference between the current and last positions
            double dx = currentMousePosition.x - lastMousePosition.x;
            double dy = currentMousePosition.y - lastMousePosition.y;

            // Move the camera by adjusting its view center
            camera.setViewCenter(camera.getViewCenter().x - dx, camera.getViewCenter().y - dy, camera.getViewCenter().z);

            // Update the last position for the next drag event
            lastMousePosition = currentMousePosition;
        } else if (e.getID() == MouseEvent.MOUSE_RELEASED) {
            lastMousePosition = null;
        }
    }


    @Override
    public void viewClosed(String id) {
        loop = false;
    }

    @Override
    public void buttonPushed(String id) {
        System.out.println("Node clicked: " + id);
    }

    public void buttonReleased(String id) {
        System.out.println("Button released on node "+id);
    }

    public void mouseOver(String id) {
        System.out.println("Need the Mouse Options to be activated");
    }

    public void mouseLeft(String id) {
        System.out.println("Need the Mouse Options to be activated");
    }


}
