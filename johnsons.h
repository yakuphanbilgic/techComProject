//
// Created by Yakuphan Bilgiç on 2019-04-14.
//

#ifndef UNTITLED1_JOHNSONS_H
#define UNTITLED1_JOHNSONS_H

#include <iostream>
#include <fstream>
#include <vector>
#include <climits>
#include <exception>
#include <set>

using namespace std;

struct Edge {
    int head;
    long cost;
};

class johnson {
    using Graph = vector<vector<Edge>>;
    using SingleSP = vector<long>;
    using AllSP = vector<vector<long>>;
    const long inf = LONG_MAX;
public:
    // first line is in the format <N> <M> where N is the number of vertices, M is the number of edges that follow
    // each following line represents a directed edge in the form <Source> <Dest> <Cost>

    Graph addZeroEdge(Graph g) {
        // add a zero-cost edge from vertex 0 to all other edges
        for (int i = 1; i < g.size(); i++) {
            g[0].push_back({i, 0});
        }

        return g;
    }

    SingleSP bellmanford(Graph &g, int s) {
        vector<vector<long>> memo(g.size()+2, vector<long>(g.size(), inf));

        // initialise base case
        memo[0][s] = 0;

        for (int i = 1; i < memo.size(); i++) {
            // compute shortest paths from s to all vertices, with max hop-count i
            for (int n = 0; n < g.size(); n++) {
                if (memo[i-1][n] < memo[i][n]) {
                    memo[i][n] = memo[i-1][n];
                }
                for (auto& e : g[n]) {
                    if (memo[i-1][n] != inf) {
                        if (memo[i-1][n] + e.cost < memo[i][e.head]) {
                            memo[i][e.head] = memo[i-1][n] + e.cost;
                        }
                    }
                }
            }
        }

        // check if the last iteration differed from the 2nd-last
        for (int j = 0; j < g.size(); j++) {
            if (memo[g.size()+1][j] != memo[g.size()][j]) {
                throw string{"negative cycle found"};
            }
        }

        return memo[g.size()];
    }

    SingleSP djikstra(const Graph& g, int s) {
        SingleSP dist(g.size(), inf);
        set<pair<int,long>> frontier;

        frontier.insert({0,s});

        while (!frontier.empty()) {
            pair<int,long> p = *frontier.begin();
            frontier.erase(frontier.begin());

            int d = p.first;
            int n = p.second;

            // this is our shortest path to n
            dist[n] = d;

            // now look at all edges out from n to update the frontier
            for (auto e : g[n]) {
                // update this node in the frontier if we have a shorter path
                if (dist[n]+e.cost < dist[e.head]) {
                    if (dist[e.head] != inf) {
                        // we've seen this node before, so erase it from the set in order to update it
                        frontier.erase(frontier.find({dist[e.head], e.head}));
                    }
                    frontier.insert({dist[n]+e.cost, e.head});
                    dist[e.head] = dist[n]+e.cost;
                }
            }
        }

        return dist;
    }

    long doJohnson(Graph &g, int src, int dest) {
        // now build "g prime" which is g with a new edge added from vertex 0 to all other edges, with cost 0
        Graph gprime = addZeroEdge(g);

        // now run Bellman-Ford to get single-source shortest paths from s to all other vertices
        SingleSP ssp;
        try {
            ssp = bellmanford(gprime, 0);
        } catch (string e) {
            cout << "Negative cycles found in graph. Cannot compute shortest paths." << endl;
            throw e;
        }

        // no re-weight each edge (u,v) in g to be: cost + ssp[u] - ssp[v].
        for (int i = 1; i < g.size(); i++) {
            for (auto &e : g[i]) {
                e.cost = e.cost + ssp[i] - ssp[e.head];
            }
        }

        // now that we've re-weighted our graph, we can invoke N iterations of Dijkstra to find
        // all pairs shortest paths
        AllSP allsp(g.size());
        for (int i = 1; i < g.size(); i++) {
            allsp[i] = djikstra(g, i);
        }

        // now re-weight the path costs back to their original weightings
        for (int u = 1; u < g.size(); u++) {
            for (int v = 1; v < g.size(); v++) {
                if (allsp[u][v] != inf) {
                    allsp[u][v] += ssp[v] - ssp[u];
                }
            }
        }

        // find the "shortest shortest path", ie. the path with lowest cost,
        // amongst all shortest path pairs.
        long shortest = allsp[src][dest];
        return shortest;
    }
};

#endif //UNTITLED1_JOHNSONS_H
