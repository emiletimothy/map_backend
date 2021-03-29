package edu.caltech.cs2.project07;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.caltech.cs2.datastructures.BeaverMapsGraph;
import edu.caltech.cs2.datastructures.Location;
import edu.caltech.cs2.helpers.Inspection;
import edu.caltech.cs2.helpers.Reflection;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.caltech.cs2.interfaces.IDeque;
import edu.caltech.cs2.interfaces.ISet;
import org.hamcrest.MatcherAssert;
import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.hamcrest.core.IsIterableContaining;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BeavermapTests {
    private static String BEAVERMAP_GRAPH_SOURCE = "src/edu/caltech/cs2/datastructures/BeaverMapsGraph.java";

    private static JsonElement fromFile(String filename) {
        try (FileReader reader = new FileReader(filename)) {
            return JsonParser.parseReader(reader);
        }
        catch (IOException e) {
            return null;
        }
    }

    @Tag("C")
    @Tag("Beavermap")
    @DisplayName("BeaverMapsGraph implements required public methods")
    @Test
    @Order(0)
    public void testMethodsBeaverMapsGraph() {
        SortedSet<String> expected = new TreeSet<>(List.of(
                "getLocationByName", "getBuildings", "getClosestBuilding", "dfs", "dijkstra", "addVertex"
        ));
        SortedSet<String> actual = new TreeSet<>(
                Stream.of(BeaverMapsGraph.class.getDeclaredMethods())
                        .filter(Reflection.hasModifier("public"))
                        .map(x -> x.getName())
                        .collect(Collectors.toList()));
        MatcherAssert.assertThat(new ArrayList<>(actual),
                IsIterableContaining.hasItems((expected.toArray())));
    }

    @Tag("C")
    @Tag("Beavermap")
    @Order(1)
    @DisplayName("Does not use or import disallowed classes")
    @Test
    public void testForInvalidClasses() {
        List<String> graphDisallow = List.of("java\\.lang\\.reflect");
        Inspection.assertNoImportsOf(BEAVERMAP_GRAPH_SOURCE, graphDisallow);
        Inspection.assertNoUsageOf(BEAVERMAP_GRAPH_SOURCE, graphDisallow);
    }

    @Order(2)
    @DisplayName("Does not use or import disallowed classes from java.util")
    @Test
    @Tag("C")
    @Tag("Beavermap")
    public void testForInvalidImportsJavaUtil() {
        List<String> allowed = List.of("Iterator");
        Inspection.assertNoImportsOfExcept(BEAVERMAP_GRAPH_SOURCE, "java\\.util", allowed);

        List<String> bannedUsages = List.of("java\\.util\\.(?!" + String.join("|", allowed) + ")");
        Inspection.assertNoUsageOf(BEAVERMAP_GRAPH_SOURCE, bannedUsages);
    }


    // Only use Caltech map and buildings to test for correctness
    @Tag("C")
    @Tag("Beavermap")
    @Test
    @Order(3)
    @DisplayName("Test getLocationById()")
    public void testGetLocationByID() {
        BeaverMapsGraph bmg = new BeaverMapsGraph(
                "data/caltech/caltech.buildings.json",
                "data/caltech/caltech.waypoints.json",
                "data/caltech/caltech.roads.json");
        JsonElement bs = fromFile("data/caltech/caltech.buildings.json");
        for (JsonElement b : bs.getAsJsonArray()) {
            Location loc = new Location(b.getAsJsonObject());
            assertNotNull(bmg.getLocationByID(loc.id), "Location id " + loc.id + " not found by id");
        }
    }

    @Tag("C")
    @Tag("Beavermap")
    @Test
    @Order(4)
    @DisplayName("Test getLocationByName()")
    public void testGetLocationByName() {
        BeaverMapsGraph bmg = new BeaverMapsGraph(
                "data/caltech/caltech.buildings.json",
                "data/caltech/caltech.waypoints.json",
                "data/caltech/caltech.roads.json");
        JsonElement bs = fromFile("data/caltech/caltech.buildings.json");
        for (JsonElement b : bs.getAsJsonArray()) {
            Location loc = new Location(b.getAsJsonObject());
            if (loc.name != null) {
                assertNotNull(bmg.getLocationByName(loc.name), "Location " + loc.name + " not found by name");
            }
        }
    }

    @Tag("C")
    @Tag("Beavermap")
    @DisplayName("Test getBuildings()")
    @ParameterizedTest(name = "Test getBuildings() on {0}")
    @CsvSource({
            "caltech/caltech.buildings.json, caltech/caltech.waypoints.json, caltech/caltech.roads.json",
            "pasadena/pasadena.buildings.json, pasadena/pasadena.waypoints.json, pasadena/pasadena.roads.json",
    })
    @Order(5)
    public void testGetBuildings(String buildingsFile, String waypointsFile, String roadsFile) {
        BeaverMapsGraph bmg = new BeaverMapsGraph(
                "data/" + buildingsFile, "data/" + waypointsFile, "data/" + roadsFile);
        Set<Location> buildings = new HashSet<>();
        JsonElement bs = fromFile("data/" + buildingsFile);
        for (JsonElement b : bs.getAsJsonArray()) {
            Location loc = new Location(b.getAsJsonObject());
            buildings.add(loc);
        }
        MatcherAssert.assertThat(bmg.getBuildings(),
                IsIterableContainingInAnyOrder.containsInAnyOrder(buildings.toArray()));
    }

    @Tag("C")
    @Tag("Beavermap")
    @DisplayName("Test getClosestBuilding()")
    @ParameterizedTest(name = "Test getClosestBuilding() on {0}")
    @CsvSource({
            "caltech/caltech.buildings.json, caltech/caltech.waypoints.json, caltech/caltech.roads.json, caltech/caltech.closest_trace.json",
            "pasadena/pasadena.buildings.json, pasadena/pasadena.waypoints.json, pasadena/pasadena.roads.json, pasadena/pasadena.closest_trace.json",
    })
    @Order(6)
    public void testGetClosestLocation(String buildingsFile, String waypointsFile, String roadsFile, String traceFile) {
        BeaverMapsGraph bmg = new BeaverMapsGraph(
                "data/" + buildingsFile, "data/" + waypointsFile, "data/" + roadsFile);
        JsonElement bs = fromFile("data/" + traceFile);
        for (JsonElement b : bs.getAsJsonArray()) {
            JsonObject curr = b.getAsJsonObject();
            Location center = bmg.getLocationByID(curr.get("center").getAsLong());
            Location closestExpected = bmg.getLocationByID(curr.get("closest").getAsLong());
            Location closestActual = bmg.getClosestBuilding(center.lat, center.lon);
            assertEquals(closestExpected.lat, closestActual.lat, "Latitudes differ");
            assertEquals(closestExpected.lon, closestActual.lon, "Longitudes differ");
        }
    }

    @Tag("C")
    @Tag("Beavermap")
    @DisplayName("Test addVertex() updates BeaverMapsGraph and underlying Graph properly")
    @Test
    @Order(7)
    public void testAddVertexBeaverMapsGraph() {
        BeaverMapsGraph bmg = new BeaverMapsGraph(
                "data/caltech/caltech.buildings.json",
                "data/caltech/caltech.waypoints.json",
                "data/caltech/caltech.roads.json");
        JsonElement bs = fromFile("data/pasadena/pasadena.buildings.json");
        for (JsonElement b : bs.getAsJsonArray()) {
            Location loc = new Location(b.getAsJsonObject());
            bmg.addVertex(loc);
            assertNotNull(bmg.getLocationByID(loc.id), "Location id " + loc.id + " not found by id");
            // Test that the vertex was actually added to the graph
            bmg.neighbors(loc.id);
        }
    }

    // Note: Pasadena map is WAY TOO LARGE to test all edges, don't try
    @Tag("C")
    @Tag("Beavermap")
    @DisplayName("Completely check all nodes and edges in BeaverMapsGraph loaded from files")
    @ParameterizedTest(name = "Test nodes in file {0}")
    @CsvSource({
            "caltech/caltech.buildings.json, caltech/caltech.waypoints.json, caltech/caltech.roads.json, caltech/caltech.neighbors_trace.json"
    })
    @Order(8)
    public void testNodesEdgesInMap(String bFile, String wFile, String roadsFile, String traceFile) {
        BeaverMapsGraph bmg = new BeaverMapsGraph("data/" + bFile, "data/" + wFile, "data/" + roadsFile);

        List<Long> actualNodeIDs = new ArrayList<>();
        for (long nid : bmg.vertices()) {
            actualNodeIDs.add(nid);
        }

        JsonElement s = fromFile("data/" + traceFile);
        for (JsonElement b : s.getAsJsonArray()) {
            JsonObject curr = b.getAsJsonObject();
            long locID = curr.get("id").getAsLong();
            Location loc = bmg.getLocationByID(locID);
            assertNotNull(loc, locID + " should be in graph, but is not");
            actualNodeIDs.remove(locID);

            JsonArray neighbors = curr.get("neighbors").getAsJsonArray();
            ISet<Long> actualNeighbors = bmg.neighbors(locID);
            List<Long> missingNeighbors = new ArrayList<>();
            for (JsonElement e : neighbors) {
                long neighborID = e.getAsLong();
                if (!actualNeighbors.remove(neighborID)) {
                    missingNeighbors.add(neighborID);
                }
            }

            // Use this instead of MatcherAssert to provide better errors (though I doubt they'll be needed)
            // Should use Truth instead... but not this year.
            if (missingNeighbors.size() > 0) {
                fail(locID + " missing neighbors " + missingNeighbors);
            } else if (actualNeighbors.size() != 0) {
                fail(locID + " has extra neighbors " + actualNeighbors);
            }
        }

        assertEquals(0, actualNodeIDs.size(), "Graph has extra nodes: " + actualNodeIDs);
    }

    @Tag("B")
    @DisplayName("Test DFS radius search")
    @ParameterizedTest(name = "Test DFS on graph {0}")
    @CsvSource({
            "caltech/caltech.buildings.json, caltech/caltech.waypoints.json, caltech/caltech.roads.json, caltech/caltech.radius_trace.json",
            "pasadena/pasadena.buildings.json, pasadena/pasadena.waypoints.json, pasadena/pasadena.roads.json, pasadena/pasadena.radius_trace.json",
    })
    @Order(9)
    public void testDFSRadius(String buildingsFile, String waypointsFile, String roadsFile, String traceFile) {

        BeaverMapsGraph bmg = new BeaverMapsGraph(
                "data/" + buildingsFile, "data/" + waypointsFile, "data/" + roadsFile);

        JsonElement s = fromFile("data/" + traceFile);
        for (JsonElement b : s.getAsJsonArray()) {
            JsonObject curr = b.getAsJsonObject();
            long locID = curr.get("center").getAsLong();
            Location loc = bmg.getLocationByID(locID);
            double dist = curr.get("radius").getAsDouble();

            JsonArray locList = curr.get("locations").getAsJsonArray();
            Set<Long> expectedLocIDs = new HashSet<>();
            for (JsonElement e : locList) {
                expectedLocIDs.add(e.getAsLong());
            }

            ISet<Location> actualLoc = bmg.dfs(loc, dist);
            Set<Long> locIDs = new HashSet<>(actualLoc.size());
            for (Location l : actualLoc) {
                locIDs.add(l.id);
            }
            MatcherAssert.assertThat(locIDs,
                    IsIterableContainingInAnyOrder.containsInAnyOrder(expectedLocIDs.toArray()));
        }
    }

    @Tag("A")
    @DisplayName("Test buildings are ignored in dijkstra path")
    @Test
    @Order(10)
    public void testDijkstraIgnoreBuildings() {
        BeaverMapsGraph bmg = new BeaverMapsGraph(
                "data/caltech/caltech.buildings.json",
                "data/caltech/caltech.waypoints.json",
                "data/caltech/caltech.roads.json");
        JsonElement s = fromFile("data/caltech/caltech.paths_trace.json");
        for (JsonElement b : s.getAsJsonArray()) {
            JsonObject curr = b.getAsJsonObject();
            Location start = bmg.getLocationByID(curr.get("start").getAsLong());
            Location target = bmg.getLocationByID(curr.get("target").getAsLong());

            IDeque<Location> actualPath = bmg.dijkstra(start, target);
            if (actualPath == null) {
                continue;
            }
            for (Location loc : actualPath){
                if (loc.id == start.id || loc.id == target.id) {
                    continue;
                }
                ISet<Location> buildings = Reflection.getFieldValue(BeaverMapsGraph.class, "buildings", bmg);
                assertFalse(buildings.contains(loc), "Location " + loc.id + " in path is a building");
            }
        }
    }

    @Tag("A")
    @DisplayName("Test Dijkstra")
    @ParameterizedTest(name = "Test Dijkstra on graph {0}")
    @CsvSource({
            "caltech/caltech.buildings.json, caltech/caltech.waypoints.json, caltech/caltech.roads.json, caltech/caltech.paths_trace.json",
            "pasadena/pasadena.buildings.json, pasadena/pasadena.waypoints.json, pasadena/pasadena.roads.json, pasadena/pasadena.paths_trace.json",
    })
    @Order(11)
    public void testDijkstraBeaverMap(String buildingsFile, String waypointsFile, String roadsFile, String traceFile) throws FileNotFoundException {
        BeaverMapsGraph bmg = new BeaverMapsGraph(
                "data/" + buildingsFile, "data/" + waypointsFile, "data/" + roadsFile);
        JsonElement s = fromFile("data/" + traceFile);
        for (JsonElement b : s.getAsJsonArray()) {
            JsonObject curr = b.getAsJsonObject();
            Location start = bmg.getLocationByID(curr.get("start").getAsLong());
            Location target = bmg.getLocationByID(curr.get("target").getAsLong());

            // Build expected list
            JsonArray pathList = curr.get("path").getAsJsonArray();
            List<Long> expectedPathIDs = new ArrayList<>();
            for (JsonElement e : pathList) {
                expectedPathIDs.add(e.getAsLong());
            }

            IDeque<Location> actualPath = bmg.dijkstra(start, target);
            List<Long> actualPathIDs = new ArrayList<>();

            if (expectedPathIDs.size() == 0) {
                assertNull(actualPath, "Path does not exist from " + start.id + " to " + target.id + " but was found");
            }
            else {
                assertNotNull(actualPath, "Path exists from " + start.id + " to " + target.id + " but was not found");
                for (Location l : actualPath) {
                    actualPathIDs.add(l.id);
                }
                // Check that path is *exactly* equivalent
                MatcherAssert.assertThat(actualPathIDs,
                        IsIterableContainingInOrder.contains(expectedPathIDs.toArray()));
            }

        }
    }
}
