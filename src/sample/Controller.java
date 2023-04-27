package sample;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class Controller implements Initializable {
    @FXML   AnchorPane map;
    @FXML   ComboBox source;
    @FXML   ComboBox destination;
    @FXML   ComboBox algorithm;
    @FXML   Button btngo;
    @FXML   TextArea Pathfinal;
    @FXML   TextArea time;
    @FXML   TextArea space;

    // initialize required variable.
    City[] palestineCities = new City[29]; // To store all cities.
    City[] Citiesselected= new City[2];   // user input.
    LinkedList<road>[] adjacencyList;
    ArrayList<City> visited = new ArrayList<>(); //to store visited countries
    PriorityQueue<City> priorityqueueq; //to store all City in this priority queue
    //UI Handling
    ArrayList<String> Algorithms;
    int algorithmToUse = 0;   // 1 : A* , 2 : IDS , 3 : BFS

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        try
        {
            this.readFile();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    public void readFile() throws IOException
    {
        //initialize adjacency list
        adjacencyList = new LinkedList[29];
        priorityqueueq = new PriorityQueue<>();
        for (int i = 0; i < adjacencyList.length; i++)
        {
            adjacencyList[i] = new LinkedList<>();
        }
        //Read city file,parse it and fill the cities array
        File cityFile = new File("towns.txt");
        BufferedReader br = new BufferedReader(new FileReader(cityFile));
        String line;
        int counter = 0;

        while ((line = br.readLine()) != null)
        {
            String[] info = line.split("#");
            String cityName = info[0];

            double cityYAxis = Double.parseDouble(info[2]);
            double cityXAxis = Double.parseDouble(info[1]);
            String arabicname = info[3];
            City currentCity = new City(counter, cityName, cityXAxis, cityYAxis,arabicname);
            palestineCities[counter] = currentCity;
            priorityqueueq.add(currentCity);
            counter++;
        }

        File adjFile = new File("road.txt");
        BufferedReader adjBr = new BufferedReader(new FileReader(adjFile));
        String adjLine;
        while ((adjLine = adjBr.readLine()) != null)
        {
            String[] info = adjLine.split("#");
            System.out.println(info[0]);
            City init = find(info[0]);
            City adj = find(info[1]);
            double distance = Double.parseDouble(info[2]);
            road r = new road(adj, distance);
            adjacencyList[init.getId()].add(r);
        }
        this.fillMap();
    }
    public boolean DLS(City source, City target, int limit)
    {
        visited.add(source);
        if(source.equals(target))
            return true;
        if(limit <= 0)
            return false;
        for(int i = 0; i < adjacencyList[source.getId()].size(); i++)
        {
            City newCity = adjacencyList[source.getId()].get(i).c;
            if(!visited.contains(newCity))
            {
                newCity.predecessor = source;
                if (DLS(adjacencyList[source.getId()].get(i).c, target, limit - 1) == true)
                    return true;
            }
        }
        return false;
    }
    public void IDS(City source, City goal, int max_depth)
    {
        for(int i = 0; i <= max_depth; i++)
        {
            visited.clear();
            if(DLS(source, goal, i) == true) {
                this.printIDS_SearchPath(goal, source, i);
                return;
            }
        }
        System.out.println("Not Found");
        Pathfinal.setText("Not Found!");
    }
    public void BFS_Search(City source, City goal) throws Exception {
        priorityqueueq = new PriorityQueue<City>();
        visited.clear();
        priorityqueueq.add(source);
        visited.add(source);
        if(source.equals(goal))
        {
            this.Pathfinal.setText("Source and goal are equal");
            return;
        }
        this.space.setText("");
        this.time.setText("");
        while(!priorityqueueq.isEmpty())
        {
            City newCity = priorityqueueq.remove();
            if(newCity.equals(goal))
            {
                this.printBFS_SearchPath(goal, source);
                return;
            }
            for(int i = 0; i < adjacencyList[newCity.getId()].size(); i++) {
                City adj = adjacencyList[newCity.getId()].get(i).c;
                if (!visited.contains(adj))
                {
                    priorityqueueq.add(adj);
                    visited.add(adj);
                    adj.predecessor = newCity;
                }
            }
        }
        throw new Exception("UnExpected error occurred!");
    }
    public void AStartSearch(City source, City goal) {
        priorityqueueq = new PriorityQueue<City>();
        visited.clear();
        source.setfN(0.0 + this.WalkingHeuristicValueCalculator(source, goal));
        source.setgN(0.0);
        priorityqueueq.add(source);
        int time=0;
        int fringeSize=priorityqueueq.size();
        while (!priorityqueueq.isEmpty()) {
            City newMin = priorityqueueq.remove();
            visited.add(newMin);
            time++;
            if (newMin.getArabicName().equals(goal.getArabicName())) {
                this.printAStartSearchPath(newMin, source,time,fringeSize);
                return;
            }
            for (int i = 0; i < adjacencyList[newMin.getId()].size(); i++) {
                City adj = adjacencyList[newMin.getId()].get(i).c;
                adj.setgN(0.0);
                double gN = adjacencyList[newMin.getId()].get(i).distance + newMin.getgN();
                adj.setgN(gN);
                double h1= this.AerialHeuristicValueCalculator(adj, goal);
                double h2 = this.WalkingHeuristicValueCalculator(adj, goal);
                double hN = h1 + h2;
                double fN = gN + hN;
                adj.setfN(fN);
                adj.sethN(hN);
                time++;
                if (!visited.contains(adj)) {
                    priorityqueueq.add(adj);
                    adj.predecessor = newMin;
                    fringeSize=Integer.max(fringeSize,priorityqueueq.size());
                }
            }
        }
    }

    public double WalkingHeuristicValueCalculator(City c1, City c2) {
        double x1 = c1.getCityXAxis();
        double y1 = c1.getCityYAxis();
        double x2 = c2.getCityXAxis();
        double y2 = c2.getCityYAxis();
        double straightDistance = Math.sqrt(((x2 - x1) * (x2 - x1)) + ((y2 - y1) * (y2 - y1)));
        double gN = c1.getgN()+c2.getgN(); //the Travel distance
        double h2 = (gN+straightDistance)/2; //the walking distance(heuristic 2)
        return h2*0.55; // to be underestimate and be admissible method else it would not be effective heuristic method
    }
    public double AerialHeuristicValueCalculator(City c1, City c2) {
        double x1 = c1.getCityXAxis();
        double y1 = c1.getCityYAxis();
        double x2 = c2.getCityXAxis();
        double y2 = c2.getCityYAxis();
        double h1 = Math.sqrt(((x2 - x1) * (x2 - x1)) + ((y2 - y1) * (y2 - y1)));
        return h1; // to be underestimate and be admissible method else it would not be effective heuristic method
    }

    public void printIDS_SearchPath(City goal, City source, int depthReached)
    {
        LinkedList<City> path = new LinkedList<>();
        path.add(goal);
        double distance = 0.0;
        while(!goal.getArabicName().equals(source.getArabicName()))
        {
            path.add(goal.predecessor);
            LinkedList<road> r = adjacencyList[goal.getId()];
            for(int i = 0; i < r.size(); i++)
            {
                if(r.get(i).c.getArabicName().equals(goal.predecessor.getArabicName()))
                {
                    distance+=r.get(i).distance;
                    break;
                }
            }
            goal = goal.predecessor;
        }
        int time = path.size();
        for (int i = 0; i < path.size() - 1; i++) {
            Line partialPath = new Line();
            partialPath.setStartX(path.get(i).getCityXAxis());
            partialPath.setStartY(path.get(i).getCityYAxis());
            partialPath.setEndX(path.get(i + 1).getCityXAxis());
            partialPath.setEndY(path.get(i + 1).getCityYAxis());
            partialPath.setStrokeWidth(3);
            partialPath.setStroke(Color.rgb(0,0,0));
            partialPath.setOpacity(0.8);
            partialPath.setId("path");
            Circle c = new Circle(path.get(i).getCityXAxis(), path.get(i).getCityYAxis(), 4);
            c.setFill(Color.INDIANRED);
            c.setId("pathCircle");
            map.getChildren().addAll(partialPath, c);
        }
        Pathfinal.appendText("Total distance ==> " +distance + " KM\n");
        this.time.setText(String.valueOf(time));
        this.space.setText("Path found after depth: "+String.valueOf(depthReached));
        for (int i = path.size() - 1; i >= 0; i--) {
            Pathfinal.appendText(path.get(i).getCityName() + " - " + path.get(i).getArabicName() + " - " + "\n");
        }
    }
    public void printBFS_SearchPath(City goal, City source)
    {
        LinkedList<City> path = new LinkedList<>();
        path.add(goal);
        double distance = 0.0;
        while(!goal.getArabicName().equals(source.getArabicName()))
        {
            path.add(goal.predecessor);
            LinkedList<road> r = adjacencyList[goal.getId()];
            for(int i = 0; i < r.size(); i++)
            {
                if(r.get(i).c.getArabicName().equals(goal.predecessor.getArabicName()))
                {
                    distance+=r.get(i).distance;
                    break;
                }
            }
            goal = goal.predecessor;
        }
        int time = path.size();
        for (int i = 0; i < path.size() - 1; i++) {
            Line partialPath = new Line();
            partialPath.setStartX(path.get(i).getCityXAxis());
            partialPath.setStartY(path.get(i).getCityYAxis());
            partialPath.setEndX(path.get(i + 1).getCityXAxis());
            partialPath.setEndY(path.get(i + 1).getCityYAxis());
            partialPath.setStrokeWidth(3);
            partialPath.setStroke(Color.rgb(0,0,0));
            partialPath.setOpacity(0.8);
            partialPath.setId("path");
            Circle c = new Circle(path.get(i).getCityXAxis(), path.get(i).getCityYAxis(), 4);
            c.setFill(Color.INDIANRED);
            c.setId("pathCircle");
            map.getChildren().addAll(partialPath, c);
        }
        Pathfinal.appendText("Total distance ==> " +distance + " KM\n");
        this.time.setText(String.valueOf(time));
        for (int i = path.size() - 1; i >= 0; i--) {
            Pathfinal.appendText(path.get(i).getCityName() + " - " + path.get(i).getArabicName() + " - " + "\n");
        }
    }
    public void printAStartSearchPath(City goal, City source,int timeValue,int spaceValue)
    {
        double cost = Math.round(goal.getgN() * 100.0) / 100.0;
        Pathfinal.appendText("total distance ==> " + cost + " KM\n");
        this.time.setText(String.valueOf(timeValue));
        this.space.setText(String.valueOf(spaceValue));
        LinkedList<City> path = new LinkedList<City>();
        path.add(goal);
        while (!goal.getArabicName().equals(source.getArabicName())) {
            path.add(goal.predecessor);
            goal = goal.predecessor;
        }
        for (int i = 0; i < path.size() - 1; i++) {

            Line partialPath = new Line();
            partialPath.setStartX(path.get(i).getCityXAxis());
            partialPath.setStartY(path.get(i).getCityYAxis());
            partialPath.setEndX(path.get(i + 1).getCityXAxis());
            partialPath.setEndY(path.get(i + 1).getCityYAxis());
            partialPath.setStrokeWidth(3);
            partialPath.setStroke(Color.rgb(0,0,0));
            partialPath.setOpacity(0.8);
            partialPath.setId("path");
            Circle c = new Circle(path.get(i).getCityXAxis(), path.get(i).getCityYAxis(), 4);
            c.setFill(Color.INDIANRED);
            c.setId("pathCircle");
            map.getChildren().addAll(partialPath, c);
        }
        for (int i = path.size() - 1; i >= 0; i--) {
            Pathfinal.appendText(path.get(i).getCityName() + " - " + path.get(i).getArabicName() + " - " + "\n");
        }

    }
    public void pathAlgorithmRequested()
    {
        if(Citiesselected[0]==null||Citiesselected[1]==null)
        {
            this.Pathfinal.setText("Please choose cities first");
            return;
        }
        if(algorithmToUse == 0)
        {
            this.Pathfinal.setText("Please choose an algorithm first");
            return;
        }
        if(Citiesselected[0].equals(Citiesselected[1]))
        {
            this.Pathfinal.setText("Goal and Target Must be Different!");
            return;
        }
        this.Pathfinal.setText("");
        for (int i = 0; i < palestineCities.length; i++)
        {
            palestineCities[i].setCost(Double.POSITIVE_INFINITY);
        }
        Set<Node> prevPath = map.lookupAll("#path");
        for (Node p : prevPath)
        {
            Line path = (Line) p;
            map.getChildren().remove(path);
        }
        Set<Node> prevPathCircle = map.lookupAll("#pathCircle");
        for (Node p : prevPathCircle)
        {
            Circle path = (Circle) p;
            map.getChildren().remove(path);
        }
        if(algorithmToUse == 1)
            this.AStartSearch(Citiesselected[0], Citiesselected[1]);
        if(algorithmToUse == 2)
            this.IDS(Citiesselected[0], Citiesselected[1], 10);
        if(algorithmToUse == 3) {
            try {
                this.BFS_Search(Citiesselected[0], Citiesselected[1]);
            } catch (Exception e) {}
        }
    }

    public void clear()
    {
        Pathfinal.setText("");
        time.clear();
        space.clear();
        source.setValue(null);
        destination.setValue(null);
        algorithm.setValue(null);
        algorithmToUse = 0;
        Citiesselected[0] = null;
        Citiesselected[1] = null;
        Set<Node> prevPath = map.lookupAll("#path");
        for (Node p : prevPath) {
            Line path = (Line) p;
            map.getChildren().remove(path);
        }
        Set<Node> prevPathCircle = map.lookupAll("#pathCircle");
        for (Node p : prevPathCircle) {
            Circle path = (Circle) p;
            map.getChildren().remove(path);
        }
        clearCircles();
    }
    public void exit()
    {
        System.exit(0);
    }
    public void clearCircles()
    {
        for (int i = 0; i < map.getChildren().size(); i++)
        {
            if(map.getChildren().get(i) instanceof Circle)
            {
                Circle dot = (Circle) map.getChildren().get(i);
                dot.setFill(Color.DARKSEAGREEN);
                dot.setRadius(5);
            }
        }
    }
    public void fillMap()
    {
        for(int i =0 ; i<palestineCities.length;i++)
        {
            Circle dot = new Circle(palestineCities[i].getCityXAxis(), (palestineCities[i].getCityYAxis()), 5);
            dot.setFill(Color.DARKSEAGREEN);
            dot.setId(palestineCities[i].getCityName().replaceAll(" ", ""));
            map.getChildren().add(dot);

            destination.getItems().addAll(palestineCities[i].getArabicName());
            source.getItems().addAll(palestineCities[i].getArabicName());
        }
        source.setOnAction(e -> {
            City firstSelection = find(String.valueOf(source.getValue()));
            this.findSelected(firstSelection, e);
        });

        destination.setOnAction(e -> {
            City firstSelection = find(String.valueOf(destination.getValue()));
            this.findSelected(firstSelection, e);
        });

        Algorithms = new ArrayList<>();
        Algorithms.add("A*");
        Algorithms.add("IDS");
        Algorithms.add("BFS");
        algorithm.getItems().addAll(Algorithms);

        algorithm.setOnAction(e -> {
            this.choiceAlgorithm();
        });
    }

    public void choiceAlgorithm()
    {
        if(algorithm.getValue() == null)
        {
            algorithmToUse = 0;
            return;
        }
        if(algorithm.getValue().equals("A*"))
        {
            algorithmToUse = 1;
            return;
        }
        if(algorithm.getValue().equals("IDS"))
        {
            algorithmToUse = 2;
            return;
        }
        if(algorithm.getValue().equals("BFS"))
        {
            algorithmToUse = 3;
            return;
        }

    }
    public void findSelected(City city, Event e)
    {
        if (((ComboBox) e.getSource()).getId().equals("source"))
        {
            Set<Node> selectedCircle = map.lookupAll("#selectedSource");
            for (Node c : selectedCircle) {
                Circle circle = (Circle) c;
                circle.setFill(Color.DARKSEAGREEN);
                circle.setRadius(5);
                circle.setId(Citiesselected[0].getCityName().replaceAll(" ", ""));
            }
            if (city == null)
                return;
            Set<Node> selected = map.lookupAll("#" + city.getCityName().replaceAll(" ", ""));
            for (Node c : selected) {
                Circle circle = (Circle) c;
                circle.setFill(Color.INDIANRED);
                circle.setRadius(7);
                circle.setId("selectedSource");
            }
            Citiesselected[0] = city;
        } else if (((ComboBox) e.getSource()).getId().equals("destination"))
        {
            Set<Node> selectedCircle = map.lookupAll("#selectedDestination");
            for (Node c : selectedCircle)
            {
                Circle circle = (Circle) c;
                circle.setFill(Color.DARKSEAGREEN);
                circle.setRadius(5);
                circle.setId(Citiesselected[1].getCityName().replaceAll(" ", ""));
            }
            if (city == null)
                return;

            Set<Node> selected = map.lookupAll("#" + city.getCityName().replaceAll(" ", ""));
            for (Node c : selected)
            {
                Circle circle = (Circle) c;
                circle.setFill(Color.INDIANRED);
                circle.setRadius(5);
                circle.setId("selectedDestination");
            }
            Citiesselected[1] = city;
        }
    }
    public City find(String cityName) {
        for(City palestincity : palestineCities){
            if (palestincity.getArabicName().equals(cityName.trim())) {
                return palestincity;
            }
        }
        return null;
    }
}