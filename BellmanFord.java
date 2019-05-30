/**************************************************************************
 * File: BellmanFord2.java
 * Author: Keith Schwarz (htiek@cs.stanford.edu)
 *
 * An implementation of the Bellman-Ford algorithm for single-source shortest
 * paths.  Like Dijkstra's algorithm, the Bellman-Ford algorithm computes the
 * shortest paths from a source node to all other nodes in the graph.  However,
 * unlike Dijkstra's algorithm, the Bellman-Ford algorithm works correctly in
 * graphs containing negative-cost edges, so long as the graph does not contain
 * a negative cycle (in which case the cost of a shortest path may not be well-
 * defined).
 *
 * The Bellman-Ford algorithm, like the Floyd-Warshall algorithm, is a classic
 * example of a dynamic programming algorithm.  The idea behind the algorithm
 * is to consider what the shortest path is from the source node s to each
 * other node in the graph, subject to the constraint that each path uses no
 * more than k edges.  When k = n, this contains all acyclic paths in the graph
 * and consequently, assuming that there are no negative cycles in the graph,
 * must contain the shortest paths in the graph.  The rationale behind phrasing
 * shortest paths this way is that if we denote by D(t, k) the length of the
 * shortest path from the source node s to node t using paths of length at most
 * k, it's possible to compute D(t, k) using the following intution.  First,
 * if we consider the case where k = 0 and all paths must have no edges, then
 * we get that
 *
 *    D(t, 0) = +infinity     (for t != s)
 *    D(s, 0) = 0
 *
 * Since there is no shortest path from s any other node using no edges, while
 * the path from s to itself using no edges has length zero.
 *
 * If we consider the case where k != 0, then there are two ways we could get a
 * shortest path from s to some node t using at most k edges.  First, it's
 * possible that the best way to do this is just to use some path using at most
 * k - 1 edges, either since no other paths exist (as would be the case in a
 * tree).  Alternatively, we could end up with a path of length at most k by 
 * taking any path of length at most k - 1, then extending it by an edge.  For
 * each node in the graph, this means that we can compute the value of D(t, k)
 * as
 *
 *    D(t, k) = min{ D(t, k - 1), min ((u, t) in E){ D(u, k - 1) + l(u, v)}}
 *
 * Where l(u, v) denotes the length of the edge (u, v).
 *
 * This implementation computes this recurrence from the bottom up by beginning
 * with k = 0 and progressively increasing it until it reaches |V|.
 *
 * The runtime of this algorithm is easily determined.  We need to compute the
 * value of D(t, |V|) for each t, so O(|V|) iterations of the recurrence
 * computation are required.  On each iteration, for each node, we'll scan the
 * outgoing edges from that node, and so each iteration visits each edge
 * exactly once.  This takes time O(|E|), so the overall runtime is O(|V||E|).
 * An interesting detail about this algorithm is that since |E| = O(|V|^2), in
 * the worst case this algorithm runs in time O(|V|^3), which is the same
 * runtime as the Floyd-Warshall algorithm.  However, Floyd-Warshall actually
 * produces the lengths of all paths between all pairs of points in this time,
 * and so it makes more sense to run Floyd-Warshall rather than Bellman-Ford!
 * That said, on sparse graphs (|E| = Theta(|V|)) the runtime is O(|V|^2),
 * which is much faster.
 *
 * The Bellman-Ford algorithm is interesting and useful for several reasons,
 * excluding historical relevance.  First, the update rule of the Bellman-Ford
 * algorithm can be computed in a distributed fashion, making it possible for
 * independent computers to each determine their distance from some predefined
 * computer using the algorithm.  This allows the algorithm to be used in
 * network routing protocols.  Second, the Bellman-Ford algorithm can be used
 * as a subroutine of Johnson's algorithm, an all-pairs shortest paths
 * algorithm that is asymptotically faster than the Floyd-Warshall algorithm on
 * sparse graphs.
 */

import java.io.*;
import java.util.*; // For HashMap
import java.util.concurrent.TimeUnit;

public final class BellmanFord {
    /**
     * Given a directed, weighted graph G and a source node s, produces the
     * distances from s to each other node in the graph.  If any nodes in
     * the graph are unreachable from s, they will be reported at infinite
     * distance.
     *
     * @param graph The graph upon which to run Dijkstra's algorithm.
     * @param source The source node in the graph.
     * @return A map from nodes in the graph to their distances from the source.
     */
    public static <T> Map<T, Double> shortestPaths(DirectedGraph<T> graph, T source) {
        /* Construct a map from the nodes to their distances, then populate it
         * with the initial value of the recurrence (the source is at distance
         * zero from itself; all other nodes are at infinite distance).
         */
        Map<T, Double> result = new HashMap<T, Double>();
        for (T node: graph)
            result.put(node, Double.POSITIVE_INFINITY);
        result.put(source, 0.0);

        /* Create a new map that acts as scratch space.  We'll flip back and
         * forth between the result map and this map during each iteration of
         * the algortihm so that we avoid needlessly reallocating maps.
         */
        Map<T, Double> scratch = new HashMap<T, Double>();

        /* Starting with k = 1, compute the new values for the distances by
         * evaluating the recurrence.
         */
        for (int k = 1; k <= graph.size(); ++k) {
            /* Begin by guessing that each node in this new iteration will have
             * a cost equal to its cost on the previous iteration.
             */
            scratch.putAll(result);

            /* Scan across all the edges in the graph, updating the costs of
             * the paths of the nodes at their endpoints.
             */
            for (T node: graph) {
                for (Map.Entry<T, Double> edge: graph.edgesFrom(node).entrySet()) {
                    /* The new cost of the shortest path to this node is no
                     * greater than the cost of the shortest path to the nodes'
                     * neighbor plus the cost of the edge from that neighbor
                     * into this node.
                     */
                    scratch.put(edge.getKey(), // The node being updated
                            Math.min(scratch.get(edge.getKey()),
                                    edge.getValue() + result.get(node)));
                }
            }

            /* Finally, exchange the scratch buffer holding the new result with
             * the result map holding last iteration's results.
             */
            Map<T, Double> temp = result;
            result = scratch;
            scratch = temp;
        }

        /* Finally, report the distances. */
        return result;
    }

    public static void main(String[] args) {
        DirectedGraph<String> myGraph = new DirectedGraph();


        List<Integer> leftEdges = new ArrayList<>(10000);
        List<Integer> rightEdges = new ArrayList<>(10000);
        List<Integer> distances = new ArrayList<>(10000);


        for(int j = 1; j < 20; j++) {

            File file = new File("/Users/yakuphanbilgic/Desktop/İTÜ/2018-2019 | Summer/techicalCommunication/techComProject/evenLarger" + j + ".txt");
            try {
                Scanner sc = new Scanner(file);

                int numberOfEdges = sc.nextInt();
                int numberOfLines = sc.nextInt();

                while (sc.hasNextInt()) {
                    leftEdges.add(sc.nextInt());
                    rightEdges.add(sc.nextInt());
                    distances.add(sc.nextInt());
                }

                List<Integer> uniqueOnes = new ArrayList<>(Collections.nCopies(10000, 0));

                for (int i = 0; i < numberOfEdges; i++) {
                    uniqueOnes.set(leftEdges.get(i), 1);
                    uniqueOnes.set(rightEdges.get(i), 1);
                }

                for (int i = 0; i < 10000; i++) {
                    if (uniqueOnes.get(i) == 1) {
                        myGraph.addNode(Integer.toString(i));
                    }
                }

                for (int i = 0; i < numberOfLines; i++) {
                    myGraph.addEdge(leftEdges.get(i).toString(), rightEdges.get(i).toString(), distances.get(i));
                }

            } catch (FileNotFoundException f) {
                f.printStackTrace();
            }

            BellmanFord myFord = new BellmanFord();
            Dijkstra myDijkstra = new Dijkstra();
            johnsons myJohnson = new johnsons();

            long startTime = System.nanoTime();
            Map<String, Double> result1 = myDijkstra.shortestPaths(myGraph, "4");
            long endTime = System.nanoTime();
            double totalTime = (endTime - startTime) / 1000000.0;
            System.out.println("Dijkstra took " + totalTime + " milliseconds.");

            long startTime2 = System.nanoTime();
            long endTime2 = System.nanoTime();
            Map<String, Double> result2 = myFord.shortestPaths(myGraph, "4");
            double totalTime2 = (endTime2 - startTime2) / 1000000.0;
            System.out.println("Bellman Ford took this " + totalTime2 + " milliseconds.");

            long startTime3 = System.nanoTime();
            DirectedGraph<String> result3 = myJohnson.shortestPaths(myGraph);
            long endTime3 = System.nanoTime();
            double totalTime3 = (endTime3 - startTime3) / 1000000.0;
            System.out.println("Johnson's algorithm took this " + totalTime3 + " milliseconds.");


            System.out.println(result1.get(leftEdges.get(0).toString()));
            System.out.println(result2.get(leftEdges.get(0).toString()));
            System.out.println(result3.edgesFrom(leftEdges.get(0).toString()));

            String fileContent = totalTime + " " + totalTime2 + " " + totalTime3;

            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter("/Users/yakuphanbilgic/Desktop/İTÜ/2018-2019 | Summer/techicalCommunication/techComProject/evenLargerOutput" + j +".txt"));
                writer.write(fileContent);
                writer.close();
            } catch (IOException i) {
                i.printStackTrace();
            }
        }

    }
};

