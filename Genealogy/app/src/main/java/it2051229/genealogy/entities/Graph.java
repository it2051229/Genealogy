package it2051229.genealogy.entities;

import java.util.ArrayList;

public class Graph {
    private ArrayList<Node> nodes;

    /**
     * Create a graph for searching
     */
    public Graph() {
        nodes = new ArrayList<>();
    }

    /**
     * Set all nodes back to default
     */
    private void resetNodes() {
        for(Node node : nodes) {
            node.cost = Integer.MAX_VALUE;
            node.previous = null;
        }
    }

    /**
     * Find the node of a name
     */
    private Node find(String name) {
        for(Node node : nodes) {
            if(node.name.equals(name)) {
                return node;
            }
        }

        return null;
    }

    /**
     * Add a new node
     */
    public void add(String name) {
        nodes.add(new Node(name));
    }

    /**
     * Make a connection between 2 nodes
     */
    public void connect(String fromName, String toName, String relationship) {
        Node from = find(fromName);
        Node to = find(toName);

        from.outBounds.add(new Edge(to, relationship));
    }

    /**
     * Get the shortest path going from one person to another
     */
    public String getShortestPath(String fromName, String toName) {
        resetNodes();

        Node source = find(fromName);
        Node destination = find(toName);

        if (source == null || destination == null) {
            return "";
        }

        // Create a new List copying the old list
        PriorityQueue unvisitedNodes = new PriorityQueue();

        for(Node node : nodes) {
            unvisitedNodes.enqueue(node);
        }

        // Set the starting node as zero
        // Set distance infinite for the rest, and this is done by default once the reset is called
        source.cost = 0;

        // While list is not empty
        while (unvisitedNodes.priorityNodes.contains(destination)) {
            Node lowestCostNode = unvisitedNodes.dequeue();

            // If the lowest cost is infinity, then all remaining vertices is inaccessible
            if (lowestCostNode.cost == Integer.MAX_VALUE) {
                break;
            }

            // Calculate the distance from the chosen node to adjacent ones that haven't
            // yet been removed from the graph
            for (int i = 0; i < lowestCostNode.outBounds.size(); i++) {
                Edge adjacentEdge = lowestCostNode.outBounds.get(i);
                int adjacentCost = lowestCostNode.cost + adjacentEdge.weight;

                // Replace the neighbor with better cost
                if (adjacentCost < adjacentEdge.node.cost) {
                    adjacentEdge.node.cost = adjacentCost;
                    adjacentEdge.node.previous = lowestCostNode;
                    adjacentEdge.node.relationshipWithPrevious = adjacentEdge.relationship;
                }
            }
        }

        // Define the path
        Node currentNode = destination;

        String result = "";

        while (currentNode.previous != null) {
            result += currentNode.name + " (" + currentNode.relationshipWithPrevious + " of) " + currentNode.previous.name + "\n";
            currentNode = currentNode.previous;
        }

        return result;
    }

    /**
     * Node is used to make connections to other nodes
     */
    private class Node {
        public String name;
        public int cost;

        public Node previous;
        public String relationshipWithPrevious;

        public ArrayList<Edge> outBounds;

        /**
         * Create a node
         */
        public Node(String name) {
            this.name = name;
            outBounds = new ArrayList<>();
            cost = Integer.MAX_VALUE;
            relationshipWithPrevious = "";
        }
    }

    /**
     * Connects 2 edges together
     */
    private class Edge {
        public Node node;
        public int weight;
        public String relationship;

        public Edge(Node node, String relationship) {
            this.node = node;
            this.relationship = relationship;
            weight = 1;
        }
    }

    /**
     * Self made priority queue
     */
    private class PriorityQueue {

        private ArrayList<Node> priorityNodes;

        /**
         * Initialize the queue
         */
        public PriorityQueue() {
            priorityNodes = new ArrayList<Node>();
        }

        /**
         * Enqueue a node
         */
        public void enqueue(Node node) {
            priorityNodes.add(node);
        }

        /**
         * Get the next prioritized queue
         */
        public Node dequeue() {
            int lowestCostIndex = 0;

            for (int i = 1; i < priorityNodes.size(); i++) {
                if (priorityNodes.get(i).cost < priorityNodes.get(lowestCostIndex).cost) {
                    lowestCostIndex = i;
                }
            }

            // Remove the lowest cost node from the unvisited nodes to be marked as visited
            return priorityNodes.remove(lowestCostIndex);
        }
    }
}
