package activities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeMap;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;

import schedulable.Activity;
import schedulable.Schedulable;
import time.LegalTimeline;
import time.TimeBlock;

public class ActivitySpanningTree implements Serializable {
	private static final long serialVersionUID = 6797165864508180241L;
	public int index;
	public ArrayList<TimeBlock> availableTBs;
	private SimpleGraph<Activity, DefaultEdge> tree;
	private Duration sumActivitiesTime;
	
	public ActivitySpanningTree(int index, ArrayList<TimeBlock> availableTBs,
			SimpleGraph<Activity, DefaultEdge> tree, Duration sumActivitiesTime) {
		this.index = index;
		this.availableTBs = availableTBs;
		this.tree = tree;
		this.sumActivitiesTime = sumActivitiesTime;
	}
	
	public boolean addActivity(Activity activity) {
		
		// check for commonTBs
		ArrayList<Integer> commonTBs = new ArrayList<Integer>();
		for (TimeBlock tb : availableTBs) {
			TreeMap<DateTime, Schedulable> map = new TreeMap<DateTime, Schedulable>();
			Interval interval = tb.getInterval();
			map.put(new DateTime(interval.getStart()),
					new Activity(interval.toDuration()));
			Activity tester = new Activity("tester", activity.getDuration(),
					activity.legalTimeline.intersect(new LegalTimeline(map)));
			if (tester.forwardChecking()) {
				commonTBs.add(tb.getIndex());
			}
		}
		if (commonTBs.isEmpty()) {
			return false;
		}
		
		tree.addVertex(activity);
		
		// update sumActivitiesTime
		sumActivitiesTime = sumActivitiesTime.plus(activity.getDuration());
		
		return true;
	}
	
	public boolean addEdge(Activity origin, Activity dest, DefaultEdge edge) {
		return tree.addEdge(origin, dest, edge);
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
