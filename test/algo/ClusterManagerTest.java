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
import util.Debugger;
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
		System.out
				.println("*********************************************************");
		System.out
				.println("****************** ClusterManager Test ******************");
		System.out
				.println("*********************************************************");
		System.out.println();
		initHelper();
	}
	
	@Test
	public void testClustering() {
		ClusterManager clusterManager = new ClusterManager(graph, tbs);
		ArrayList<TimeBlock> results = clusterManager.clustering(allActivities);
		Assert.assertNotNull(results);
		Assert.assertEquals(2, results.size());
		for (TimeBlock result : results) {
			switch (result.getIndex()) {
			case 0:
				Assert.assertEquals(7, result.getTimeline()
						.getNumScheduled());
				Debugger.printSchedulables(result);
				
				break;
			case 1:
				Assert.assertEquals(5, result.getTimeline()
						.getNumScheduled());
				Debugger.printSchedulables(result);
				break;
			
			}
		}
	}
	
	@Test
	public void testFindAST() {
		clusters.clear();
		clusters.add(abc);
		clusters.add(de);
		ClusterManager clusterManager = new ClusterManager(graph, tbs);
		Assert.assertEquals(abc, clusterManager.findAST(clusters, activityA));
		Assert.assertEquals(abc, clusterManager.findAST(clusters, activityB));
		Assert.assertEquals(abc, clusterManager.findAST(clusters, activityC));
		Assert.assertEquals(de, clusterManager.findAST(clusters, activityD));
		Assert.assertEquals(de, clusterManager.findAST(clusters, activityE));
	}
	
	@Test
	public void testInBlcklist() {
		ClusterManager clusterManager = new ClusterManager(graph, tbs);
		
		Set<ActivitySpanningTree> blackcluster = new HashSet<ActivitySpanningTree>();
		
		// Cluster: ABC, DE. Blacklist empty => false
		Assert.assertTrue(clusters.add(abc));
		Assert.assertTrue(clusters.add(de));
		Assert.assertFalse(clusterManager.inBlacklist(clusters, blacklist));
		
		// Cluster: ABC, DE. Blacklist ABC => true
		Assert.assertTrue(blackcluster.add(abc));
		Assert.assertTrue(blacklist.add(blackcluster));
		Assert.assertTrue(clusterManager.inBlacklist(clusters, blacklist));
		
		// Cluster: ABC. Blacklist: ABC, DE => false
		clusters.clear();
		Assert.assertTrue(clusters.add(abc));
		blackcluster.clear();
		Assert.assertTrue(blackcluster.add(abc));
		Assert.assertTrue(blackcluster.add(de));
		blacklist.clear();
		Assert.assertTrue(blacklist.add(blackcluster));
		Assert.assertFalse(clusterManager.inBlacklist(clusters, blacklist));
		
		// Cluster: ABC. Blacklist: ABC, DE // ABC => true
		blackcluster = new HashSet<ActivitySpanningTree>();
		Assert.assertTrue(blackcluster.add(abc));
		blacklist.add(blackcluster);
		Assert.assertTrue(clusterManager.inBlacklist(clusters, blacklist));
		
		// Cluster is ABC, DE. Blacklist cluster is ABC, DE => true
		Assert.assertTrue(clusters.add(de));
		Assert.assertTrue(blacklist.remove(blackcluster));
		Assert.assertTrue(clusterManager.inBlacklist(clusters, blacklist));
		
	}
	
	private static void initHelper() {
		LegalTimeline legalTimeline = new LegalTimeline(new Interval(0, 50));
		legalTimeline
				.schedule(new DateTime(0), new LegalTime(new Duration(50)));
		
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
				new Transportation(new Duration(8)));
		graph.addEdge(activityC.location, activityD.location,
				new Transportation(new Duration(8)));
		graph.addEdge(activityC.location, activityE.location,
				new Transportation(new Duration(7)));
		graph.addEdge(activityD.location, activityE.location,
				new Transportation(new Duration(20)));
		
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
		Location start1 = new Location(3, 3);
		Location start2 = new Location(1, 1);
		TimeBlock tb1 = new TimeBlock(0, new Interval(1, 25), start1, start1);
		TimeBlock tb2 = new TimeBlock(1, new Interval(31, 47), start2, start2);
		tbs.add(tb1);
		tbs.add(tb2);
	}
}
