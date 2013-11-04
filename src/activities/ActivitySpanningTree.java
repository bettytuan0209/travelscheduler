package activities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;

import org.jgraph.graph.Edge;
import org.jgrapht.graph.SimpleGraph;
import org.joda.time.Duration;

public class ActivitySpanningTree implements Serializable {
	private static final long serialVersionUID = 6797165864508180241L;
	public int index;
	public ArrayList<Integer> availableTBs;
	private SimpleGraph<Activity, Edge> tree;
	private Duration sumActivitiesTime;

	public ActivitySpanningTree(int index) {
		this.index = index;
	}

	public boolean addActivity() {
		// update sumActivitiesTime and check if possible to add
		return true;
	}

	public Set<Activity> getActivities() {
		return tree.vertexSet();

	}

	public Duration getSumActivitiesTime() {
		return sumActivitiesTime;
	}

	public boolean removeActivity(Activity activity) {
		if (tree.removeVertex(activity)) {
			sumActivitiesTime = sumActivitiesTime.minus(activity.getDuration());
			return true;
		}
		return false;
	}

}
