#include <iostream>
#include "bellmanFord.h"
#include "dijkstra.h"
#include "johnsons.h"
#include <fstream>

using jGraph = vector<vector<Edge>>;

int main(int argc, char **argv) //Driver Function for Dijkstra SSSP
{
    dijkstra Dijkstra;
    bellmanFord bellmanFord;
    Graph bellmanFordGraph;
    johnson myJohnson;
    jGraph johnsonsGraph;

    ifstream myFile(argv[1]);

    int n, m, x, y, w, source, destination;

    source = atoi(argv[3]);
    destination = atoi(argv[4]);

    myFile >> n;
    myFile >> m;

    bellmanFordGraph.createGraph(n, m);
    johnsonsGraph.resize(n+1);

    for(int i = 0; i < m; i++) {
        myFile >> x;
        myFile >> y;
        myFile >> w;
        Dijkstra.a[x].push_back(make_pair(y,w));
        Dijkstra.a[y].push_back(make_pair(x,w));

        bellmanFordGraph.edge[i].src = x;
        bellmanFordGraph.edge[i].dest = y;
        bellmanFordGraph.edge[i].weight = w;
        johnsonsGraph[x].push_back({y, w});
    }

    ofstream output;
    output.open(argv[2], std::ios_base::app);

    auto start = chrono::steady_clock::now();
    bellmanFord.BellmanFord(&bellmanFordGraph, source, destination);
    auto end = chrono::steady_clock::now();

    auto diff = end - start;

    cout << "Bellman Ford took: " << chrono::duration<double, milli>(diff).count() << " ms" << endl << endl << endl;

    // Dijkstra now

    auto start2 = chrono::steady_clock::now();
    Dijkstra.Dijkstra(source, n);
    auto end2 = chrono::steady_clock::now();
    auto diff2 = end2 - start2;

    cout << "I am Dijkstra, Vertex: " << source << " destination: " << destination << ", distance: "
         << Dijkstra.dis[destination] << endl;
    cout << "Dijkstra took: " << chrono::duration<double, milli>(diff2).count() << " ms" << endl << endl << endl;

    //Johnsons now

    auto start3 = chrono::steady_clock::now();
    // run Johnson's algorithm to get all pairs shortest paths
    long shortestJohn = myJohnson.doJohnson(johnsonsGraph, source, destination);
    auto end3 = chrono::steady_clock::now();
    auto diff3 = end3 - start3;

    cout << "I am Johnson's, Vertex: " << source << " destination: " << destination << ", distance: "
         << shortestJohn << endl;
    cout << "Johnson's took: " << chrono::duration<double, milli>(diff3).count() / 8 << " ms" << endl << endl << endl;

    output << chrono::duration<double, milli>(diff2).count() << " " << chrono::duration<double, milli>(diff).count()
           << " " << chrono::duration<double, milli>(diff3).count() <<  "    " << Dijkstra.dis[destination] <<  "\n";

    output.close();

    return 0;
}