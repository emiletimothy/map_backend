package edu.caltech.cs2.project07;

import edu.caltech.cs2.datastructures.Graph;
import org.hamcrest.MatcherAssert;
import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.Duration;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@Tag("C")
@DisplayName("Test graph of shared appearances in Marvel comic books (MarvelTests)")
public class MarvelTests {
    private static Graph<String, Integer> MARVEL_GRAPH;
    private static final int COMMON_TIMEOUT_MS = 1500;

    @BeforeAll
    public static void populateMarvelGraph() {
        MARVEL_GRAPH = new Graph<>();

        Set<String> characters = new HashSet<>();
        Map<String, List<String>> books = new HashMap<>();

        try {
            MarvelParser.parseData("data/marvel/marvel.tsv", characters, books);
        } catch (Exception e) {
            fail("Could not read input file.");
        }

        for (String c : characters) {
            MARVEL_GRAPH.addVertex(c);
        }

        for (String book : books.keySet()) {
            // This will add symmetric edges automatically - no need to add undirected edge
            for (String c1 : books.get(book)) {
                for (String c2 : books.get(book)) {
                    if (!c1.equals(c2)) {
                        Integer num = MARVEL_GRAPH.adjacent(c1, c2);
                        if (num == null) {
                            num = 0;
                        }
                        MARVEL_GRAPH.addEdge(c1, c2, num + 1);
                    }
                }
            }
        }
    }

    @Test
    @Order(0)
    @DisplayName("Test that characters with no co-appearances do not have neighbors - neighbor isEmpty")
    public void testSingletons() {
        List<String> singletons = new ArrayList<>();
        for (String c : MARVEL_GRAPH.vertices()) {
            if (MARVEL_GRAPH.neighbors(c).isEmpty()) {
                singletons.add(c);
            }
        }
        loadAndMatch(singletons, "data/marvel/singletons_output");
    }

    @Test
    @Order(1)
    @DisplayName("Look for characters that appear with many other characters - neighbor size")
    public void testPopular() {
        List<String> populars = new ArrayList<>();
        for (String c : MARVEL_GRAPH.vertices()) {
            if (MARVEL_GRAPH.neighbors(c).size() > 20) {
                populars.add(c);
            }
        }
        loadAndMatch(populars, "data/marvel/popular_output");
    }

    @Test
    @Order(2)
    @DisplayName("Test that there are no loops (self-edges) in the graph")
    public void testNoLoops() {
        for (String c : MARVEL_GRAPH.vertices()) {
            assertNull(MARVEL_GRAPH.adjacent(c, c),
                    "There is a loop (self-edge) in the graph, when there should be none.");
        }
    }


    // THIS TEST MUST BE RUN LAST - IT HAS SIDE EFFECTS ON THE STATIC GRAPH
    @Test
    @Order(3)
    @DisplayName("Look for co-appearances that appear in many issues together - edge weights")
    public void testCommon() {
        Assertions.assertTimeout(Duration.ofMillis(COMMON_TIMEOUT_MS), this::runCommon);
    }

    public void runCommon() {
        List<String> common = new ArrayList<>();
        for (String c1 : MARVEL_GRAPH.vertices()) {
            Set<String> toRemove = new HashSet<>();
            for (String c2 : MARVEL_GRAPH.neighbors(c1)) {
                Integer edge = MARVEL_GRAPH.adjacent(c1, c2);
                Integer symedge = MARVEL_GRAPH.adjacent(c2, c1);
                assertNotNull(edge, "An existing edge is null");
                assertNotNull(symedge, "An existing edge is not symmetric in the graph");
                toRemove.add(c2);

                // Sort here to ignore dependency on hashing order
                if (edge > 80) {
                    if (c1.compareTo(c2) < 0) {
                        common.add(c1.strip() + " --" + edge + "-- " + c2.strip());
                    }
                    else {
                        common.add(c2.strip() + " --" + edge + "-- " + c1.strip());
                    }
                }
            }
            // Process removals separately to prevent modifying neighbors while iterating on it
            for (String c2 : toRemove) {
                Integer symedge = MARVEL_GRAPH.adjacent(c2, c1);
                MARVEL_GRAPH.removeEdge(c1, c2);
                assertNull(MARVEL_GRAPH.adjacent(c1, c2), "An edge is not null after removal");
                assertEquals(symedge, MARVEL_GRAPH.adjacent(c2, c1), "An edge is null after its symmetric edge is removed");

                MARVEL_GRAPH.removeEdge(c2, c1);
                assertNull(MARVEL_GRAPH.adjacent(c2, c1), "An edge is not null after removal");
            }
            assertTrue(MARVEL_GRAPH.neighbors(c1).isEmpty(), "After removing all of a vertex's neighbors, neighbors() is non-empty");
        }

        loadAndMatch(common, "data/marvel/common_output");


    }

    private void loadAndMatch(List<String> actual, String filename) {
        List<String> expected = new ArrayList<>();
        Scanner fr = null;
        try {
            fr = new Scanner(new File(filename));
        } catch (FileNotFoundException e) {
            fail("Could not open test results file.");
        }

        while (fr.hasNextLine()) {
            String l = fr.nextLine();
            // is edge - sort to remove dependence on directionality for correctness
            if (l.contains("--")) {
                String[] spl = l.split("--");
                String v1 = spl[0].strip();
                String v2 = spl[2].strip();
                String e = spl[1];
                if (v1.compareTo(v2) < 0) {
                    l = v1 + " --" + e + "-- " + v2;
                }
                else {
                    l = v2 + " --" + e + "-- " + v1;
                }
            }
            expected.add(l);
        }

        MatcherAssert.assertThat(actual,
                IsIterableContainingInAnyOrder.containsInAnyOrder(expected.toArray()));
    }
}