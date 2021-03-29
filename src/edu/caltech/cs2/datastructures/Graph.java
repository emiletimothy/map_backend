package edu.caltech.cs2.datastructures;
import edu.caltech.cs2.interfaces.IGraph;
import edu.caltech.cs2.interfaces.ISet;


public class Graph<V, E> implements IGraph<V, E> {

    //chaining hash in chaining hash
    //key nodes
    //value something that determines if key is there. Dictionary of all the destinations and their weights
    //only adding destinations if they key exists at all
    //backing structure: don't initialize chd inside graph just put it as the value for whatever you're putting in

    ChainingHashDictionary<V, ChainingHashDictionary<V, E>> graph  = new ChainingHashDictionary<>(MoveToFrontDictionary::new);

    @Override
    public boolean addVertex(V vertex) {
        if(!graph.containsKey(vertex)){
            graph.put(vertex, new ChainingHashDictionary<>(MoveToFrontDictionary::new));
            return true;
        }
        return false;
    }

    @Override
    public boolean addEdge(V src, V dest, E e) {
        if (graph.containsKey(src) && graph.containsKey(dest)){
            if(graph.get(src).containsKey(dest)){//if it contains the key, it has an edge
                graph.get(src).put(dest, e);
                return false;
            }
            graph.get(src).put(dest, e);
            return true;
        }
        throw new IllegalArgumentException("Not valid!");
    }

    @Override
    public boolean addUndirectedEdge(V n1, V n2, E e) {
        if(graph.containsKey(n1) && graph.containsKey(n2)) {
            if (graph.get(n1).containsKey(n2)) {
                graph.get(n1).put(n2, e);//n1 has edge to n2 dont know if n2 has an edge to n1
                graph.get(n2).put(n1, e);
                return false;
            }
            if(graph.get(n2).containsKey(n1)){
                graph.get(n2).put(n1, e);
                graph.get(n1).put(n2, e);
                return false;
            }
            graph.get(n1).put(n2, e);
            graph.get(n2).put(n1, e);
            return true;
        }
        throw new IllegalArgumentException("Not valid!");
    }

    @Override
    public boolean removeEdge(V src, V dest) {
        if (graph.containsKey(src) && graph.containsKey(dest)) {
            if (graph.get(src).containsKey(dest)) {//if it contains the key, it has an edge
                graph.get(src).remove(dest);
                return true;
            }
            return false;
        }
        throw new IllegalArgumentException("Not valid!");
    }

    @Override
    public ISet<V> vertices() {
        return graph.keySet();
    }

    @Override
    public E adjacent(V i, V j) {
        if(graph.containsKey(i) && graph.containsKey(j)){
            if(graph.get(i).keySet().contains(j)){
                return graph.get(i).get(j);
            }
            return null;
        }
        throw new IllegalArgumentException("Not valid!");
    }

    @Override
    public ISet<V> neighbors(V vertex) {
        if(graph.containsKey(vertex)){
            return graph.get(vertex).keySet();
        }
        throw new IllegalArgumentException("Not valid!");

    }

}