// A C++ program for Bellman-Ford's single source
// shortest path algorithm.
#include <cstdio>
#include <climits>
#include <iostream>

struct Graph
{
    struct Edge
    {
        int src, dest, weight;
    };

    // V-> Number of vertices, E-> Number of edges
    int V, E;

    // graph is represented as an array of edges.
    struct Edge* edge;

    void createGraph(int V, int E)
    {
        this->V = V;
        this->E = E;
        this->edge = new Edge[E];
    }
};

class bellmanFord {
public:
    void BellmanFord(struct Graph* graph, int src, int dest)
    {
        int V = graph->V;
        int E = graph->E;
        int dist[V];
        // Step 1: Initialize distances from src to all other vertices
        // as INFINITE
        for (int i = 0; i < V; i++) {
            dist[i] = INT_MAX;
        }
        dist[src] = 0;

        for (int i = 1; i <= V-1; i++)
        {
            for (int j = 0; j < E; j++)
            {
                int u = graph->edge[j].src;
                int v = graph->edge[j].dest;
                int weight = graph->edge[j].weight;
                if (dist[u] != INT_MAX && dist[u] + weight < dist[v])
                    dist[v] = dist[u] + weight;
            }
        }

        // Step 3: check for negative-weight cycles.  The above step
        // guarantees shortest distances if graph doesn't contain
        // negative weight cycle.  If we get a shorter path, then there
        // is a cycle.
        for (int i = 0; i < E; i++)
        {
            int u = graph->edge[i].src;
            int v = graph->edge[i].dest;
            int weight = graph->edge[i].weight;
            if (dist[u] != INT_MAX && dist[u] + weight < dist[v]) {
                printf("Graph contains negative weight cycle");
                return;
            }

        }

      std::cout << "I am Bellman Ford, Vertex: " << src << " destination: " << dest << ", distance: " << dist[dest] << std::endl;

      return;
    }
};
