package edu.caltech.cs2.project07;

import edu.caltech.cs2.datastructures.BeaverMapsGraph;
import edu.caltech.cs2.datastructures.Graph;
import edu.caltech.cs2.datastructures.Location;
import edu.caltech.cs2.interfaces.IGraph;

import static org.junit.jupiter.api.Assertions.*;

public class GraphMaker {
    public static BeaverMapsGraph transformToLocations(IGraph<Integer, Integer> g) {
        BeaverMapsGraph ng = new BeaverMapsGraph();
        for (Integer v : g.vertices()) {
            ng.addVertex(new Location((long)v));
        }

        for (Integer v : g.vertices()) {
            for (Integer v2 : g.neighbors(v)) {
                ng.addEdge((long)v, (long)v2, (double)g.adjacent(v, v2));
            }
        }

        return ng;
    }

    /**
     * Generate a simple graph
     * @return graph
     */
    public static IGraph<Integer, Integer> simpleGraph() {
        IGraph<Integer, Integer> g = new Graph<>();
        g.addVertex(1);
        g.addVertex(2);
        g.addVertex(3);
        g.addEdge(1, 2, 10);
        g.addEdge(2, 3, 20);

        return g;
    }

    /**
     * Generate a directed linear graph
     * @param n - number of vertices in the linear graph
     * @return graph, with edges labelled with the source vertex
     */
    public static IGraph<Integer, Integer> linearGraph(int n) {
        IGraph<Integer, Integer> g = new Graph<>();
        for (int i = 0; i < n; i++) {
            assertTrue(g.addVertex(i), "Adding a new vertex should return true");
        }

        for (int i = 0; i < n - 1; i++) {
            assertTrue(g.addEdge(i, i + 1, i),
                    "Adding a new edge should return true"
            );
        }
        return g;
    }

    public static void vertexTest(IGraph<Integer, Integer> g, int n) {
        for (int i = 0; i < n; i++)
            assertTrue(g.vertices().contains(i), "Graphs should contain added vertices");
    }

    // Verify that a graph we're given looks reasonably like graph 2
    public static void verifyLinearGraph(IGraph<Integer, Integer> g, int n) {
        vertexTest(g, n);
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - 1; j++) {
                Integer e = g.adjacent(i, j);
                if (j == i + 1)
                    assertEquals(i, e, "Retrieved edge weight is not correct");
                else
                    assertNull(e, "An edge that was never added returned a non-null weight");
            }
        }
    }

    /**
     * Construct a tournament graph on n vertices; has an edge from i to j iff j<i
     * @param n - number of vertices
     * @return graph
     */
    public static IGraph<Integer, Integer> tournamentGraph(int n) {
        IGraph<Integer, Integer> g = new Graph<>();
        for (int i = 0; i < n; i++) {
            assertTrue(g.addVertex(i), "Adding a new vertex should return true");
        }
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < i; j++) {
                assertTrue(g.addEdge(i, j, i), "Adding a new edge should return true");
            }
        }
        return g;
    }

    // Verify that a graph we're given looks reasonably like graph 3
    public static void verifyTournamentGraph(IGraph<Integer, Integer> g, int n) {
        vertexTest(g, n);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                Integer e = g.adjacent(i, j);
                if (j < i) {
                    assertEquals(i, e, "Retrieved edge weight is not correct");
                }
                else {
                    assertNull(e, "An edge that was never added returned a non-null weight");
                }
            }
        }
    }

    /**
     * Construct a complete graph on n vertices
     * @param n - number of vertices
     * @return graph
     */
    public static IGraph<Integer, Integer> completeGraph(int n) {
        IGraph<Integer, Integer> g = new Graph<>();
        for (int i = 0; i < n; i++) {
            assertTrue(g.addVertex(i), "Adding a new vertex should return true");
        }
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (j == i) continue;
                assertTrue(g.addEdge(i, j, i),
                        "Adding a new edge should return true"
                );
            }
        }
        return g;
    }

    // Verify that a graph we're given looks reasonably like graph 4
    public static void verifyCompleteGraph(IGraph<Integer, Integer> g, int n) {
        vertexTest(g, n);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                Integer e = g.adjacent(i,
                        j);
                if (i != j) {
                    assertEquals(i, e, "Retrieved edge weight is not correct");
                }
                else {
                    assertNull(e, "An edge that was never added returned a non-null weight");
                }
            }
        }
    }

    /**
     * Construct a graph that consists of 2 disjoint complete graphs, each with n vertices
     * @param n - number of vertices in each complete component
     * @return graph
     */
    public static IGraph<Integer, Integer> disjointCompleteGraphs(int n) {
        IGraph<Integer, Integer> g = new Graph<>();
        for (int i = 0; i < n; i++) {
            assertTrue(g.addVertex(i), "Adding a new vertex should return true");
        }
        for (int i = 0; i < n / 2; i++) {
            for (int j = 0; j < n / 2; j++) {
                if (i != j) {
                    assertTrue(g.addEdge(i, j, i),
                            "Adding a new edge should return true"
                    );
                }
            }
        }
        for (int i = n / 2; i < n; i++) {
            for (int j = n / 2; j < n; j++) {
                if (i != j) {
                    assertTrue(g.addEdge(i, j, i),
                            "Adding a new edge should return true"
                    );
                }
            }
        }

        return g;
    }

    // Verify that a graph we're given looks reasonably like graph 5
    public static void verifyDisjointCompleteGraphs(IGraph<Integer, Integer> g, int n) {
        vertexTest(g, n);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                Integer e = g.adjacent(i, j);
                if ((i < n / 2 && j < n / 2 || i >= n / 2 && j >= n / 2) && i != j) {
                    assertEquals(i, e, "Retrieved edge weight is not correct");
                }
                else {
                    assertNull(e, "An edge that was never added returned a non-null weight");
                }
            }
        }
    }
}