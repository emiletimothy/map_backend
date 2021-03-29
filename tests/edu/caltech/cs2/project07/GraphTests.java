package edu.caltech.cs2.project07;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.caltech.cs2.datastructures.Graph;
import edu.caltech.cs2.helpers.Inspection;
import edu.caltech.cs2.interfaces.IGraph;
import org.hamcrest.MatcherAssert;
import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@Tag("C")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GraphTests {
    private static final String GRAPH_SOURCE = "src/edu/caltech/cs2/datastructures/Graph.java";
    private static final int SIMPLE_OP_TIMEOUT_MS = 300;
    private static final int MEDIUM_OP_TIMEOUT_MS = 500;
    private static final int STRESS_OP_TIMEOUT_MS = 2200;

    @Order(0)
    @DisplayName("Does not use or import disallowed classes")
    @Test
    public void testForInvalidClasses() {
        List<String> graphDisallow = List.of("java\\.io", "java\\.lang\\.reflect");
        Inspection.assertNoImportsOf(GRAPH_SOURCE, graphDisallow);
        Inspection.assertNoUsageOf(GRAPH_SOURCE, graphDisallow);
    }

    @Order(1)
    @DisplayName("Does not use or import disallowed classes from java.util")
    @Test
    public void testForInvalidImportsJavaUtil() {
        List<String> allowed = List.of("Iterator");
        Inspection.assertNoImportsOfExcept(GRAPH_SOURCE, "java\\.util", allowed);

        List<String> bannedUsages = List.of("java\\.util\\.(?!" + String.join("|", allowed) + ")");
        Inspection.assertNoUsageOf(GRAPH_SOURCE, bannedUsages);
    }

    @Order(2)
    @DisplayName("Test empty graph")
    @Test
    public void emptyGraphTest() {
        IGraph<String, String> g = new Graph<>();
        assertTimeout(Duration.ofMillis(SIMPLE_OP_TIMEOUT_MS), () ->
                assertEquals(0, g.vertices().size(), "Empty graph should have no vertices")
        );
    }

    @Test
    @Order(3)
    @DisplayName("Test creating various graphs")
    public void creationTest() {
        assertTimeout(Duration.ofMillis(MEDIUM_OP_TIMEOUT_MS), () -> {
            GraphMaker.simpleGraph();
            GraphMaker.verifyLinearGraph(GraphMaker.linearGraph(100), 100);
            GraphMaker.verifyTournamentGraph(GraphMaker.tournamentGraph(100), 100);
            GraphMaker.verifyCompleteGraph(GraphMaker.completeGraph(100), 100);
            GraphMaker.verifyDisjointCompleteGraphs(GraphMaker.disjointCompleteGraphs(100), 100);
        });
    }

    /**
     * Ensure that we can create a small graph with some edges.
     */
    @Order(3)
    @DisplayName("Test creating a small directed graph")
    @Test
    public void secondCreateTest() {
        IGraph<String, Integer> g = new Graph<>();

        assertTimeout(Duration.ofMillis(SIMPLE_OP_TIMEOUT_MS), () -> {
            assertTrue(g.addVertex("0"), "Should be able to add a vertex");
            assertTrue(g.addVertex("1"), "Should be able to add a vertex");
            assertTrue(g.addVertex("2"), "Should be able to add a vertex");
            assertTrue(g.addVertex("3"), "Should be able to add a vertex");

            assertTrue(g.addEdge("0", "1", 2), "Should be able to add an edge");
            assertTrue(g.addEdge("2", "1", 4), "Should be able to add an edge");
            assertTrue(g.addEdge("1", "2", 3), "Should be able to add an edge");
            assertTrue(g.addEdge("1", "3", 1), "Should be able to add an edge");
            assertTrue(g.addEdge("3", "1", -1), "Should be able to add an edge");


            assertEquals(g.vertices().size(), 4, "Graph should have correct number of vertices");

            assertEquals(2, (int) g.adjacent("0", "1"), "Edges added should all be present");
            assertEquals(4, (int) g.adjacent("2", "1"), "Edges added should all be present");
            assertEquals(3, (int) g.adjacent("1", "2"), "Edges added should all be present");
            assertEquals(1, (int) g.adjacent("1", "3"), "Edges added should all be present");
            assertEquals((int) g.adjacent("3", "1"), -1, "Edges added should all be present");
        });
    }

    @Order(4)
    @Test
    @DisplayName("Adding an edge with both endpoints missing should fail")
    public void addIllegalEdgeTest() {
        // Test adding edge where neither src, dest exists
        IGraph<String, Integer> g = new Graph<>();
        assertThrows(IllegalArgumentException.class, () -> g.addEdge("", "", 1));
    }

    @Test
    @Order(4)
    @DisplayName("Adding an edge with dest missing should fail")
    public void addIllegalEdgeTest2() {
        // Test adding edge where dest does not exist
        IGraph<String, Integer> g = new Graph<>();
        assertTrue(g.addVertex("0"));
        assertThrows(IllegalArgumentException.class, () -> g.addEdge("0", "", 1));
    }

    @Test
    @Order(4)
    @DisplayName("Adding an edge with src missing should fail")
    public void addIllegalEdgeTest3() {
        // Test adding edge where src does not exist
        IGraph<String, Integer> g = new Graph<>();
        assertTrue(g.addVertex("0"));
        assertThrows(IllegalArgumentException.class, () -> g.addEdge("", "0", 1));
    }

    @Test
    @Order(5)
    @DisplayName("Test building a simple graph with edges and failures to add edges")
    public void simpleAddTest() {
        assertTimeout(Duration.ofMillis(SIMPLE_OP_TIMEOUT_MS), () -> {
            IGraph<String, Integer> g = new Graph<>();

            assertTrue(g.addVertex("1"), "Should be able to add a vertex");
            assertEquals(1, g.vertices().size(), "Should have correct number of vertices");
            assertTrue(g.addVertex("2"), "Should be able to add a vertex");
            assertEquals(2, g.vertices().size(), "Should have correct number of vertices");
            assertTrue(g.addVertex("3"), "Should be able to add a vertex");
            assertEquals(3, g.vertices().size(), "Should have correct number of vertices");

            assertTrue(g.addEdge("1", "2", 5), "Should be able to add new edge");
            assertFalse(g.addEdge("1", "2", 5), "Should not be able to add an existing edge");
            assertFalse(g.addEdge("1", "2", 3), "Should not be able to add an existing edge");

            assertTrue(g.addEdge("2", "1", 5), "Should be able to add new edge");
            assertFalse(g.addEdge("2", "1", 5), "Should not be able to add an existing edge");
            assertFalse(g.addEdge("2", "1", 3), "Should not be able to add an existing edge");

            assertTrue(g.addEdge("1", "3", 5), "Should be able to add new edge");
            assertFalse(g.addEdge("1", "3", 5), "Should not be able to add an existing edge");
            assertFalse(g.addEdge("1", "3", 3), "Should not be able to add an existing edge");

            assertNotNull(g.adjacent("1", "2"), "Edge should exist in the graph");
            assertNotNull(g.adjacent("2", "1"), "Edge should exist in the graph");
            assertNotNull(g.adjacent("1", "3"), "Edge should exist in the graph");
            assertNull(g.adjacent("2", "3"), "Edge should not exist in graph");
            assertNull(g.adjacent("3", "1"), "Edge should not exist in graph");
        });
    }

    @Test
    @Order(6)
    @DisplayName("Test removing edges from the graph")
    public void simpleRemoveTest() {
        assertTimeout(Duration.ofMillis(SIMPLE_OP_TIMEOUT_MS), () -> {
            IGraph<String, Integer> g = new Graph<>();

            assertTrue(g.addVertex("1"), "Should be able to add vertex");
            assertTrue(g.addVertex("2"), "Should be able to add vertex");
            assertTrue(g.addEdge("1", "2", 5), "Should be able to add edge");
            assertEquals(5, (int) g.adjacent("1", "2"), "Added edge should be present in graph");
            assertTrue(g.removeEdge("1", "2"), "Should be able to remove an edge from the graph");
            assertFalse(g.removeEdge("1", "2"), "Should not be able to remove already-removed edge");
        });
    }

    @Test
    @Order(7)
    @DisplayName("Test removing edges from the graph again")
    public void stringEdgeTest() {
        assertTimeout(Duration.ofMillis(SIMPLE_OP_TIMEOUT_MS), () -> {
            IGraph<Integer, Integer> g = new Graph<>();

            assertTrue(g.addVertex(1), "Should be able to add vertex");
            assertTrue(g.addVertex(2), "Should be able to add vertex");
            assertTrue(g.addVertex(3), "Should be able to add vertex");
            assertTrue(g.addEdge(1, 2, 0), "Should be able to add edge");
            assertTrue(g.addEdge(1, 3, 0), "Should be able to add edge");
            assertTrue(g.addEdge(2, 3, 0), "Should be able to add edge");
            assertNotNull(g.adjacent(1, 2), "Added edge should be present in graph");
            assertNotNull(g.adjacent(1, 3), "Added edge should be present in graph");
            assertNotNull(g.adjacent(2, 3), "Added edge should be present in graph");
            assertTrue(g.removeEdge(1, 2), "Should be able to remove an edge from the graph");
            assertFalse(g.removeEdge(1, 2), "Should not be able to remove already-removed edge");
            assertTrue(g.removeEdge(1, 3), "Should be able to remove an edge from the graph");
            assertFalse(g.removeEdge(1, 3), "Should not be able to remove already-removed edge");
            assertTrue(g.removeEdge(2, 3), "Should be able to remove an edge from the graph");
            assertFalse(g.removeEdge(2, 3), "Should not be able to remove already-removed edge");
        });
    }

    @Test
    @Order(8)
    @DisplayName("Test that all edges in a complete graph are present")
    public void adjacentStressTest() {
        assertTimeout(Duration.ofMillis(STRESS_OP_TIMEOUT_MS), () -> {
            IGraph<Integer, Integer> g = GraphMaker.completeGraph(400);
            for (int i = 0; i < 400; i++)
                for (int j = 0; j < 400; j++)
                    if (i != j)
                        assertNotNull(g.adjacent(i, j), "Edge should be present in the graph");
        });
    }

    @Test
    @Order(9)
    @DisplayName("Test that directed edges in a tournament graph are correct")
    public void testNeighbors() {
        assertTimeout(Duration.ofMillis(SIMPLE_OP_TIMEOUT_MS), () -> {
            IGraph<Integer, Integer> g = GraphMaker.tournamentGraph(10);
            Set<Integer> vertices = new HashSet<Integer>();

            for (int i = 0; i < 10; i++) {
                MatcherAssert.assertThat("neighbors", g.neighbors(i),
                        IsIterableContainingInAnyOrder.containsInAnyOrder(vertices.toArray()));
                // In a tournament graph, vertices have edges to smaller ones
                vertices.add(i);
            }
        });
    }

    @Test
    @Order(10)
    @DisplayName("Test adding undirected edges")
    public void undirectedEdgeTest() {
        assertTimeout(Duration.ofMillis(SIMPLE_OP_TIMEOUT_MS), () -> {
            IGraph<String, Integer> g = new Graph<>();

            assertTrue(g.addVertex("1"), "Should be able to add a vertex");
            assertEquals(1, g.vertices().size(), "Should have correct number of vertices");
            assertTrue(g.addVertex("2"), "Should be able to add a vertex");
            assertEquals(2, g.vertices().size(), "Should have correct number of vertices");
            assertTrue(g.addVertex("3"), "Should be able to add a vertex");
            assertEquals(3, g.vertices().size(), "Should have correct number of vertices");

            assertTrue(g.addUndirectedEdge("1", "2", 1), "Adding undirected edge when neither direction exists should return true");
            assertFalse(g.addEdge("1", "2", 1), "Should not be able to add directed edge");
            assertFalse(g.addEdge("2", "1", 1), "Should not be able to add directed edge");

            assertTrue(g.addEdge("1", "3", 1), "Should be able to add new edge");
            assertFalse(g.addUndirectedEdge("3", "1", 2), "Adding an undirected edge when one direction exists should return false");
            assertEquals(g.adjacent("3", "1"), 2, "Adding an undirected edge when one direction exists should update existing edges");
            assertEquals(g.adjacent("1", "3"), 2, "Adding an undirected edge when one direction exists should update existing edges");

            assertTrue(g.addEdge("2", "3", 1), "Should be able to add new directed edge");
            assertFalse(g.addUndirectedEdge("2", "3", 2), "Adding an undirected edge when one direction exists should return false");
            assertEquals(g.adjacent("3", "2"), 2, "Adding an undirected edge when one direction exists should update existing edges");
            assertEquals(g.adjacent("2", "3"), 2, "Adding an undirected edge when one direction exists should update existing edges");
        });
    }
}