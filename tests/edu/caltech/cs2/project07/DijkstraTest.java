package edu.caltech.cs2.project07;

import edu.caltech.cs2.datastructures.BeaverMapsGraph;
import edu.caltech.cs2.datastructures.Graph;
import edu.caltech.cs2.datastructures.Location;
import edu.caltech.cs2.helpers.Reflection;
import edu.caltech.cs2.interfaces.IDeque;
import edu.caltech.cs2.interfaces.IGraph;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.Random;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

@Tag("A")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DijkstraTest {

    @Order(0)
    @DisplayName("Loop path should be singleton")
    @Test
    public void dijkstraShortTripTest() {
        IDeque<Location> res = GraphMaker.transformToLocations(GraphMaker.completeGraph(10)).dijkstra(new Location(1), new Location(1));
        assertEquals(1, res.size(), "Path from a node to itself should only include the node once");
        assertEquals(res.peek(), new Location(1), "Path from a node to itself should only include the node once");
    }

    @Order(1)
    @DisplayName("Disconnected graph should not have a path")
    @Test
    public void dijkstraDisconnectedTest() {
        IDeque<Location> res = GraphMaker.transformToLocations(GraphMaker.disjointCompleteGraphs(10)).dijkstra(new Location(1), new Location(9));
        assertNull(res, "Disconnected graph should give null path");
    }

    @Order(2)
    @ParameterizedTest(name = "Graph: {0}, start: {1}, end: {2}, trace file: {3}")
    @DisplayName("Tests correctness of Dijkstra implementation")
    @CsvSource({
            "simpleGraph, 1, 3, simple_1_3",
            "linearGraph, 0, 1, line_0_1",
            "linearGraph, 0, 5, line_0_5",
            "linearGraph, 0, 20, line_0_20",
            "linearGraph, 0, 99, line_0_99",
            "linearGraph, 1, 0, line_1_0",
            "tournamentGraph, 0, 1, graph3_0_1",
            "tournamentGraph, 99, 0, graph3_99_0"
    })
    public void dijkstraTestGraph(String graphName, int start, int end, String traceFile)
            throws IllegalAccessException, InvocationTargetException, FileNotFoundException {
        BeaverMapsGraph bmg;
        if (graphName.equals("simpleGraph")) {
            Method graphGen = Reflection.getMethod(GraphMaker.class, graphName);
            bmg = GraphMaker.transformToLocations(
                    (IGraph<Integer, Integer>) graphGen.invoke(null));
        }
        else {
            Method graphGen = Reflection.getMethod(GraphMaker.class, graphName, int.class);
            bmg = GraphMaker.transformToLocations(
                    (IGraph<Integer, Integer>) graphGen.invoke(null, 100));
        }

        IDeque<Location> res = bmg.dijkstra(new Location(start), new Location(end));
        double pathLen = 0;
        Location prev = null;

        Scanner s = new Scanner(new File("data/dijkstra/" + traceFile));

        String line = s.nextLine();
        if (line.equals("null")) {
            assertNull(res, "Path does not exist from " + start + " to " + end + " but a path was found");
        }
        else {
            assertNotNull(res, "Path exists from " + start + " to " + end + " but a path was not found");
            for (Location l : res) {
                if (prev != null) {
                    pathLen += bmg.adjacent(prev.id, l.id);
                }
                prev = l;
            }

            double expectedLen = Double.parseDouble(line);

            assertEquals(expectedLen, pathLen, "Path lengths are not equivalent");
        }
    }

    @Order(3)
    @DisplayName("Tests Dijkstra on random graph and paths")
    @Test
    public void dijkstraStressTest() throws FileNotFoundException {
        final int num_tests = 1000;
        IGraph<Integer, Integer> refg = new Graph<Integer, Integer>();

        Scanner s = new Scanner(new File("data/dijkstra/random_graph"));

        Random r = new Random(69420);

        int num_vertices = r.nextInt(100);

        for (int i = 0; i < num_vertices; i++) {
            refg.addVertex(i);
        }
        for (int i = 0; i < num_tests; i++) {

            // It will be handy to have two unique vertex ids below
            int one = r.nextInt(num_vertices);
            int two = r.nextInt(num_vertices);
            if (one == two)
                two = (two + 1) % num_vertices;

            int dice = r.nextInt(5);
            // TODO: y
            if (dice <= 4) {
                refg.addEdge(one, two, r.nextInt(100));
            } else if (dice <= 5) {
                refg.removeEdge(one, two);
            }

            int startvertex = r.nextInt(num_vertices);
            int endvertex = r.nextInt(num_vertices);

            BeaverMapsGraph bmg = GraphMaker.transformToLocations(refg);
            IDeque<Location> res = bmg.dijkstra(new Location(startvertex), new Location(endvertex));

            String line = s.nextLine();
            if (line.equals("null")) {
                assertNull(res, "Path does not exist from " + startvertex + " to " + endvertex + " but a path was found");
            }
            else {
                assertNotNull(res, "Path exists from " + startvertex + " to " + endvertex + " but a path was not found");
                double pathLen = 0;
                Location prev = null;
                for (Location l : res) {
                    if (prev != null) {
                        pathLen += bmg.adjacent(prev.id, l.id);
                    }
                    prev = l;
                }

                double expectedLen = Double.parseDouble(line);
                assertEquals(expectedLen, pathLen, "Path is not the shortest path by length");
            }
        }
    }
}
