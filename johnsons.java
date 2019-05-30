
import java.util.*; // For HashMap

public final class johnsons {
    /* An object to use as the source node which cannot normally appear in an
     * input graph.
     */
    private static final Object SOURCE_NODE = new Object();

    /**
     * Given a directed, weighted graph G, runs Johnson's algorithm on that
     * graph and produces a new graph with an edge (i, j) between each pair of
     * nodes whose cost is the cost of the shortest path from i to j in the
     * input graph.
     *
     * @param graph The graph on which to run Johnson's algorithm.
     * @return A graph containing the all-pairs shortest paths of the input
     *         graph.
     */
    public static <T> DirectedGraph<T> shortestPaths(DirectedGraph<T> graph) {
        /* Construct the augmented graph G' that will be fed into the Bellman-
         * Ford step by copying the graph and adding an extra node.  Because
         * the source node is of type Object (because we can't necessarily
         * create an element of type T that isn't already in the graph), this
         * graph will store plain old Objects.
         */
        DirectedGraph<Object> augmentedGraph = constructAugmentedGraph(graph);

        /* Compute the single-source shortest path from the source node to
         * each other node in the graph to get the potential function h.
         */
        Map<Object, Double> potential = BellmanFord.shortestPaths(augmentedGraph, SOURCE_NODE);

        /* Now, reweight the edges of the input graph by adjusting edge
         * weights based on the potential.
         */
        DirectedGraph<T> reweightedGraph = reweightGraph(graph, potential);

        /* Our return value is a graph with the all-pairs shortest paths
         * values for edges.  We'll begin by initializing it so that it has a
         * copy of each node in the source graph.
         */
        DirectedGraph<T> result = new DirectedGraph<T>();
        for (T node: graph)
            result.addNode(node);

        /* Now, run Dijkstra's algorithm over every node in the updated graph
         * to get the transformed shortest path costs.
         */
        for (T node: graph) {
            Map<T, Double> costs = Dijkstra.shortestPaths(reweightedGraph, node);

            /* We now have the shortest path costs from this node to all other
             * nodes, but the costs are using the new edges rather than the
             * old.  In particular, we have that
             *
             *                  C'(u, v) = C(u, v) + h(u) - h(v)
             *
             * When recording these costs in the new graph, we'll therefore
             * add in h(v) - h(u).
             */
            for (Map.Entry<T, Double> path: costs.entrySet())
                result.addEdge(node, path.getKey(),
                        path.getValue() + potential.get(path.getKey()) - potential.get(node));
        }

        /* Hand back the resulting graph. */
        return result;
    }

    /**
     * Utility function which, given a directed graph, constructs the
     * augmented graph G' by adding an extra source node.
     *
     * @param graph The graph to augment.
     * @return An augmented version of that graph.
     */
    private static <T> DirectedGraph<Object> constructAugmentedGraph(DirectedGraph<T> graph) {
        DirectedGraph<Object> result = new DirectedGraph<Object>();

        /* Copy over the nodes. */
        for (Object node: graph)
            result.addNode(node);

        /* Copy over the edges. */
        for (T node: graph)
            for (Map.Entry<T, Double> edge: graph.edgesFrom(node).entrySet())
                result.addEdge(node, edge.getKey(), edge.getValue());

        /* Add the new node to the graph. */
        result.addNode(SOURCE_NODE);

        /* Connect it to each other node with an edge of cost zero. */
        for (Object node: graph)
            result.addEdge(SOURCE_NODE, node, 0.0);

        return result;
    }

    /**
     * Utility function which, given a graph and a potential function on that
     * graph (encoded as a map from nodes to their potentials), produces a new
     * graph whose edges are weighted by the potential.
     *
     * @param graph The graph to reweight.
     * @param potential The potential function to apply.
     * @return A reweighted version of the graph.
     */
    private static <T> DirectedGraph<T> reweightGraph(DirectedGraph<T> graph,
                                                      Map<Object, Double> potential) {
        /* Begin by copying over all the nodes from the old graph. */
        DirectedGraph<T> result = new DirectedGraph<T>();
        for (T node: graph)
            result.addNode(node);

        /* Now, copy over the edge with new weights; in particular, by using
         * l'(u, v) = l(u, v) - l(v) + l(u).
         */
        for (T node: graph)
            for (Map.Entry<T, Double> edge: graph.edgesFrom(node).entrySet())
                result.addEdge(node, edge.getKey(),
                        edge.getValue() + potential.get(node) - potential.get(edge.getKey()));

        return result;
    }
}