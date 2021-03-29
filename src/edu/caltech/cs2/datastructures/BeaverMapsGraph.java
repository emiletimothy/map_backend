package edu.caltech.cs2.datastructures;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import edu.caltech.cs2.interfaces.*;

import java.io.FileReader;
import java.io.IOException;


public class BeaverMapsGraph extends Graph<Long, Double> {
    private IDictionary<Long, Location> ids;
    private ISet<Location> buildings;

    public BeaverMapsGraph() {
        super();
        this.buildings = new ChainingHashSet<>();
        this.ids = new ChainingHashDictionary<>(MoveToFrontDictionary::new);
    }

    /**
     * Reads in buildings, waypoinnts, and roads file into this graph.
     * Populates the ids, buildings, vertices, and edges of the graph
     * @param buildingsFileName the buildings filename
     * @param waypointsFileName the waypoints filename
     * @param roadsFileName the roads filename
     */
    public BeaverMapsGraph(String buildingsFileName, String waypointsFileName, String roadsFileName) {
        this();
        JsonElement bs = fromFile(buildingsFileName);
        assert bs != null;
        for (JsonElement b : bs.getAsJsonArray()) {
            Location loc = new Location(b.getAsJsonObject());
            this.buildings.add(loc);
            this.ids.put(loc.id, loc);
            this.addVertex(loc.id);
        }

        JsonElement wp = fromFile(waypointsFileName);
        assert wp != null;
        for (JsonElement w : wp.getAsJsonArray()) {
            Location way = new Location(w.getAsJsonObject());
            this.ids.put(way.id, way);
            this.addVertex(way.id);
        }

        JsonElement ro = fromFile(roadsFileName);
        assert ro != null;
        for (JsonElement r : ro.getAsJsonArray()) {
            JsonArray store = r.getAsJsonArray();
            for (int i = 0; i < store.size() - 1; i ++) {
                Location road1 = this.getLocationByID(store.get(i).getAsLong());
                Location road2 = this.getLocationByID(store.get(i + 1).getAsLong());
                this.addUndirectedEdge(road2.id, road1.id, road2.getDistance(road1.lat, road1.lon));
            }
        }
    }

    /**
     * Returns a deque of all the locations with the name locName.
     * @param locName the name of the locations to return
     * @return a deque of all location with the name locName
     */
    public IDeque<Location> getLocationByName(String locName) {
        IDeque<Location> locList = new ArrayDeque<>();
        for (Location l: this.buildings){
            if (l.toString().equals(locName)){
                locList.add(l);
            }
        }
        return locList;
    }

    /**
     * Returns the Location object corresponding to the provided id
     * @param id the id of the object to return
     * @return the location identified by id
     */
    public Location getLocationByID(long id) {
        return this.ids.get(id);
    }

    /**
     * Adds the provided location to this map.
     * @param n the location to add
     * @return true if n is a new location and false otherwise
     */
    public boolean addVertex(Location n) {
        if (n == null) {
            return false;
        }
        if (!this.ids.containsValue(n)) {
            this.ids.put(n.id, n);
            super.addVertex(n.id);
            return true;
        }
        return false;
    }

    /**
     * Returns the closest building to the location (lat, lon)
     * @param lat the latitude of the location to search near
     * @param lon the longitute of the location to search near
     * @return the building closest to (lat, lon)
     */
    public Location getClosestBuilding(double lat, double lon) {
        double dist = 999999999;
        Location building = null;
        for (Location l : this.buildings) {
            if(l.getDistance(lat, lon) < dist){
                dist = l.getDistance(lat, lon);
                building = l;
            }
        }
        return building;
    }


    public ISet<Location> dfs(Location start, double threshold) {
        ISet<Location> location_set = new ChainingHashSet<>();
        helper_dfs(start, start, location_set, threshold);
        return location_set;
    }

    private void helper_dfs(Location start, Location curr, ISet<Location> partials, double threshold) {
        partials.add(curr);
        for (Long child: this.neighbors(curr.id)) {
            if (threshold >= start.getDistance(getLocationByID(child))) {
                if (!partials.contains(getLocationByID(child))) {
                    helper_dfs(start, getLocationByID(child), partials, threshold);
                }
            }
        }
    }

    /**
     * Returns a list of Locations corresponding to
     * buildings in the current map.
     * @return a list of all building locations
     */
    public ISet<Location> getBuildings() {
        return this.buildings;
    }

    /**
     * Returns a shortest path (i.e., a deque of vertices) between the start
     * and target locations (including the start and target locations).
     * @param start the location to start the path from
     * @param target the location to end the path at
     * @return a shortest path between start and target
     */
    public IDeque<Location> dijkstra(Location start, Location target) {
        IPriorityQueue<Long> work_list = new MinFourHeap();
        IDictionary<Long, Double> dist = new ChainingHashDictionary<>(MoveToFrontDictionary::new);
        IDictionary<Long, Long> shortest_path = new ChainingHashDictionary<>(MoveToFrontDictionary::new);
        work_list.enqueue(new IPriorityQueue.PQElement(start.id, 0.0));
        dist.put(start.id, 0.0);
        while (work_list.size() != 0) {
            Long location_id = work_list.dequeue().data;
            if (location_id == target.id) {
                break;
            }
            for (Long neighbor : this.neighbors(location_id)) {
                if (this.buildings.contains(this.getLocationByID(neighbor)) && target.id != neighbor) {
                    continue;
                }
                if (!dist.containsKey(neighbor)) {
                        double e = dist.get(location_id) + this.adjacent(location_id, neighbor);
                        dist.put(neighbor, e);
                        work_list.enqueue(new IPriorityQueue.PQElement(neighbor, e));
                        shortest_path.put(neighbor, location_id);
                }
                else {
                    double e = dist.get(location_id) + this.adjacent(location_id, neighbor);
                    if (e < dist.get(neighbor)) {
                        dist.put(neighbor, e);
                        work_list.decreaseKey(new IPriorityQueue.PQElement(neighbor, e));
                        shortest_path.put(neighbor, location_id);
                    }
                }
            }
        }
        IDeque<Location> ret = new LinkedDeque<>();
        Long end = target.id;

        while (end != null && end != start.id) {
            ret.addFront(this.getLocationByID(end));
            end = shortest_path.get(end);
        }
        if (end == null) {
            return null;
        }
        ret.addFront(start);
        return ret;
    }

    /**
     * Returns a JsonElement corresponding to the data in the file
     * with the filename filename
     * @param filename the name of the file to return the data from
     * @return the JSON data from filename
     */
    private static JsonElement fromFile(String filename) {
        try (FileReader reader = new FileReader(filename)) {
            return JsonParser.parseReader(reader);
        } catch (IOException e) {
            return null;
        }
    }
}
