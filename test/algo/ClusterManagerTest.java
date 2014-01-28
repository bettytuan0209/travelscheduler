package algo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.jgrapht.graph.SimpleWeightedGraph;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import schedulable.Activity;
import schedulable.LegalTime;
import schedulable.Transportation;
import time.LegalTimeline;
import time.TimeBlock;
import activities.ActivitySpanningTree;
import activities.Bridge;
import activities.Location;

public class ClusterManagerTest {
	private static Activity activityA, activityB, activityC, activityD,
			activityE;
	private static ActivitySpanningTree abc, de;
	private static SimpleWeightedGraph<Location, Transportation> graph;
	private static Set<ActivitySpanningTree> clusters = new HashSet<ActivitySpanningTree>();
	private static ArrayList<Set<ActivitySpanningTree>> blacklist = new ArrayList<Set<ActivitySpanningTree>>();
	private static Set<Activity> allActivities = new HashSet<Activity>();
	private static ArrayList<TimeBlock> tbs = new ArrayList<TimeBlock>();
	
	@BeforeClass
	public static void init() {
		initHelper();
	}
	
	@Test
	public void testClustering() {
		Assert.assertNotNull(ClusterManager.clustering(graph, allActivities,
				tbs));
	}
	
	// @Test
	public void testFindAST() {
		clusters.clear();
		clusters.add(abc);
		clusters.add(de);
		Assert.assertEquals(abc, ClusterManager.findAST(clusters, activityA));
		Assert.assertEquals(abc, ClusterManager.findAST(clusters, activityB));
		Assert.assertEquals(abc, ClusterManager.findAST(clusters, activityC));
		Assert.assertEquals(de, ClusterManager.findAST(clusters, activityD));
		Assert.assertEquals(de, ClusterManager.findAST(clusters, activityE));
	}
	
	@Test
	public void testInBlcklist() {
		
		Set<ActivitySpanningTree> blackcluster = new HashSet<ActivitySpanningTree>();
		
		// Cluster: ABC, DE. Blacklist empty => false
		Assert.assertTrue(clusters.add(abc));
		Assert.assertTrue(clusters.add(de));
		Assert.assertFalse(ClusterManager.inBlacklist(clusters, blacklist));
		
		// Cluster: ABC, DE. Blacklist ABC => true
		Assert.assertTrue(blackcluster.add(abc));
		Assert.assertTrue(blacklist.add(blackcluster));
		Assert.assertTrue(ClusterManager.inBlacklist(clusters, blacklist));
		
		// Cluster: ABC. Blacklist: ABC, DE => false
		clusters.clear();
		Assert.assertTrue(clusters.add(abc));
		blackcluster.clear();
		Assert.assertTrue(blackcluster.add(abc));
		Assert.assertTrue(blackcluster.add(de));
		blacklist.clear();
		Assert.assertTrue(blacklist.add(blackcluster));
		Assert.assertFalse(ClusterManager.inBlacklist(clusters, blacklist));
		
		// Cluster: ABC. Blacklist: ABC, DE // ABC => true
		blackcluster = new HashSet<ActivitySpanningTree>();
		Assert.assertTrue(blackcluster.add(abc));
		blacklist.add(blackcluster);
		Assert.assertTrue(ClusterManager.inBlacklist(clusters, blacklist));
		
		// Cluster is ABC, DE. Blacklist cluster is ABC, DE => true
		Assert.assertTrue(clusters.add(de));
		Assert.assertTrue(blacklist.remove(blackcluster));
		Assert.assertTrue(ClusterManager.inBlacklist(clusters, blacklist));
		
	}
	
	private static void initHelper() {
		LegalTimeline legalTimeline = new LegalTimeline(new Interval(0, 35));
		legalTimeline
				.schedule(new DateTime(0), new LegalTime(new Duration(35)));
		
		// build activities
		activityA = new Activity("A", new Duration(2), new Location(1, 1),
				legalTimeline);
		activityB = new Activity("B", new Duration(3), new Location(2, 2),
				legalTimeline);
		activityC = new Activity("C", new Duration(4), new Location(3, 3),
				legalTimeline);
		activityD = new Activity("D", new Duration(5), new Location(4, 4),
				legalTimeline);
		activityE = new Activity("E", new Duration(1), new Location(5, 5),
				legalTimeline);
		
		allActivities.add(activityA);
		allActivities.add(activityB);
		allActivities.add(activityC);
		allActivities.add(activityD);
		allActivities.add(activityE);
		
		graph = new SimpleWeightedGraph<Location, Transportation>(
				Transportation.class);
		
		// add vertices to the graph
		graph.addVertex(activityA.location);
		graph.addVertex(activityB.location);
		graph.addVertex(activityC.location);
		graph.addVertex(activityD.location);
		graph.addVertex(activityE.location);
		
		// add edges between vertices
		graph.addEdge(activityA.location, activityB.location,
				new Transportation(new Duration(1)));
		graph.addEdge(activityA.location, activityC.location,
				new Transportation(new Duration(2)));
		graph.addEdge(activityA.location, activityD.location,
				new Transportation(new Duration(5)));
		graph.addEdge(activityA.location, activityE.location,
				new Transportation(new Duration(9)));
		graph.addEdge(activityB.location, activityC.location,
				new Transportation(new Duration(1)));
		graph.addEdge(activityB.location, activityD.location,
				new Transportation(new Duration(6)));
		graph.addEdge(activityB.location, activityE.location,
				new Transportation(new Duration(9)));
		graph.addEdge(activityC.location, activityD.location,
				new Transportation(new Duration(8)));
		graph.addEdge(activityC.location, activityE.location,
				new Transportation(new Duration(7)));
		graph.addEdge(activityD.location, activityE.location,
				new Transportation(new Duration(20)));
		
		// build the bridges
		// Map<Bridge, Boolean> bridges = new TreeMap<Bridge, Boolean>(
		// new Comparator<Bridge>() {
		// public int compare(Bridge a, Bridge b) {
		// long result = a.getDurationMillis()
		// - b.getDurationMillis();
		// if (result < 0) {
		// return -1;
		// } else if (result > 0) {
		// return 1;
		// } else {
		// return 0;
		// }
		// }
		// });
		// for (Activity activity1 : allActivities) {
		// for (Activity activity2 : allActivities) {
		// if (!activity1.equals(activity2)) {
		// bridges.put(ClusterManager.makeBridge(graph, activity1,
		// activity2), new Boolean(false));
		// }
		// }
		// }
		
		// build AST with ABC
		abc = new ActivitySpanningTree(0, activityA);
		abc = abc.joinAST(new ActivitySpanningTree(0, activityB),
				ClusterManager.makeBridge(graph, activityA, activityB));
		abc = abc.joinAST(new ActivitySpanningTree(0, activityC),
				ClusterManager.makeBridge(graph, activityB, activityC));
		
		// build AST with DE
		de = new ActivitySpanningTree(0, activityD);
		de = de.joinAST(new ActivitySpanningTree(0, activityE),
				ClusterManager.makeBridge(graph, activityD, activityE));
		
		ActivitySpanningTree ast = new ActivitySpanningTree(0, activityA);
		ast = ast.joinAST(new ActivitySpanningTree(0, activityB), new Bridge(
				new Transportation(new Duration(1)), activityA, activityB));
		ast = ast.joinAST(new ActivitySpanningTree(0, activityC), new Bridge(
				new Transportation(new Duration(1)), activityB, activityC));
		
		Set<ActivitySpanningTree> clusters = new HashSet<ActivitySpanningTree>();
		clusters.add(ast);
		
		// tbs
		Location start = new Location(1, 1);
		TimeBlock tb1 = new TimeBlock(0, new Interval(0, 16), start, start);
		TimeBlock tb2 = new TimeBlock(0, new Interval(18, 30), start, start);
		tbs.add(tb1);
		tbs.add(tb2);
	}
}
