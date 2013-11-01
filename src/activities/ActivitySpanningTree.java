package activities;
import java.util.ArrayList;

import org.jgraph.graph.Edge;
import org.jgrapht.graph.SimpleGraph;

public class ActivitySpanningTree {
	int index;
	SimpleGraph<Activity, Edge> tree;
	ArrayList<Integer> availableTBs;

	public ActivitySpanningTree(int index) {
		this.index = index;
	}

}
