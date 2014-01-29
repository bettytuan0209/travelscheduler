package algo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import org.jgrapht.graph.SimpleWeightedGraph;
import org.joda.time.Duration;

import schedulable.Activity;
import schedulable.Transportation;
import time.TimeBlock;
import util.DeepCopy;
import activities.ActivitySpanningTree;
import activities.Bridge;
import activities.Location;

public class ClusterManager {
	
	public static ArrayList<TimeBlock> clustering(
			SimpleWeightedGraph<Location, Transportation> graph,
			Set<Activity> activities, ArrayList<TimeBlock> tbs) {
		Set<ActivitySpanningTree> clusters = new HashSet<ActivitySpanningTree>();
		ArrayList<Set<ActivitySpanningTree>> blacklist = new ArrayList<Set<ActivitySpanningTree>>();
		ArrayList<Bridge> bridges = new ArrayList<Bridge>();
		
		// create an AST for each activity
		int astIndex = 0;
		for (Activity activity : activities) {
			ActivitySpanningTree ast = new ActivitySpanningTree(astIndex,
					activity);
			clusters.add(ast);
			
		}
		
		// Build an arraylist of bridges
		for (Activity activity1 : activities) {
			for (Activity activity2 : activities) {
				
				if (!activity1.equals(activity2)) {
					
					Bridge bridge;
					if (activity1.location.equals(activity2.location)) {
						bridge = new Bridge(
								new Transportation(new Duration(0)), activity1,
								activity2);
					} else {
						bridge = new Bridge(graph.getEdge(activity1.location,
								activity2.location), activity1, activity2);
					}
					
					Bridge reverse = new Bridge(bridge.getEdge(),
							bridge.getActivity2(), bridge.getActivity1());
					
					if (!bridges.contains(bridge) && !bridges.contains(reverse)) {
						bridges.add(bridge);
						
					}
				}
			}
		}
		
		// sort the bridges
		Collections.sort(bridges, new Comparator<Bridge>() {
			public int compare(Bridge a, Bridge b) {
				long result = a.getDurationMillis() - b.getDurationMillis();
				if (result < 0) {
					return -1;
				} else if (result > 0) {
					return 1;
				} else {
					return 0;
				}
			}
		});
		
		return clusterInRange(graph, clusters, bridges, tbs, blacklist);
		
		// int bridgeIndex = 0;
		// // attempt to cluster
		// // iterate until the last edge
		// for (; bridgeIndex < bridges.size(); bridgeIndex++) {
		// if (searchCluster(clusters, bridges, tbs, blacklist, bridgeIndex)) {
		// ArrayList<TimeBlock> result;
		// if ((result = ASTTBMatcher.matching(graph, clusters, tbs)) != null) {
		// return result;
		// } else {
		// blacklist.add(clusters);
		// if ((bridgeIndex = findHeaviestBridge(bridges, clusters)) < 0) {
		// throw new IllegalStateException(
		// "Inconsistency in bridges in ASTs");
		// }
		//
		// }
		// } else {
		// blacklist.add(clusters);
		// bridgeIndex = popHeaviestBridge(bridges, clusters);
		// }
		//
		// }
		
	}
	
	@SuppressWarnings("unchecked")
	private static ArrayList<TimeBlock> clusterInRange(
			SimpleWeightedGraph<Location, Transportation> graph,
			Set<ActivitySpanningTree> clusters,
			ArrayList<Bridge> searchRangeBridges, ArrayList<TimeBlock> tbs,
			ArrayList<Set<ActivitySpanningTree>> blacklist) {
		
		// iterate through this given range, try to find a bridge to use
		for (Bridge bridge : searchRangeBridges) {
			ActivitySpanningTree setA = findAST(clusters, bridge.getActivity1());
			ActivitySpanningTree setB = findAST(clusters, bridge.getActivity2());
			
			if (!setA.equals(setB)) {
				
				ActivitySpanningTree union = setA.joinAST(setB, bridge);
				
				if (union.getAvailableTBs(tbs, false, true).size() == 0) {
					
					Set<ActivitySpanningTree> invalid = new HashSet<ActivitySpanningTree>();
					invalid.add(union);
					
					if (!inBlacklist(invalid, blacklist)) {
						
						blacklist.add(invalid);
					}
					
				} else {
					// actually join the two asts and update the bridges
					clusters.remove(setA);
					clusters.remove(setB);
					clusters.add(union);
					bridge.used = true;
					
					if (inBlacklist(clusters, blacklist)) {
						// restore previous state (aka undo using this
						// bridge)
						clusters.remove(union);
						clusters.add(setA);
						clusters.add(setB);
						bridge.used = false;
					} else {
						
						ArrayList<TimeBlock> result;
						if (goalCheck(clusters, tbs)
								&& (result = ASTTBMatcher.matching(graph,
										(Set<ActivitySpanningTree>) DeepCopy
												.copy(clusters),
										(ArrayList<TimeBlock>) DeepCopy
												.copy(tbs))) != null) {
							return result;
						} else if ((result = clusterInRange(
								graph,
								clusters,
								new ArrayList<Bridge>(searchRangeBridges
										.subList(searchRangeBridges
												.indexOf(bridge) + 1,
												searchRangeBridges.size())),
								tbs, blacklist)) != null) {
							
							return result;
							
						} else {
							
							if (!inBlacklist(clusters, blacklist)) {
								
								// note that this config doesn't work
								blacklist
										.add((Set<ActivitySpanningTree>) DeepCopy
												.copy(clusters));
							}
							// restore previous state (aka undo using this
							// bridge)
							
							clusters.remove(union);
							clusters.add(setA);
							clusters.add(setB);
							bridge.used = false;
							
						}
					}
				}
			}
			
		}
		return null;
	}
	
	protected static ActivitySpanningTree findAST(
			Set<ActivitySpanningTree> clusters, Activity activity) {
		
		// go through each AST
		for (ActivitySpanningTree ast : clusters) {
			if (ast.getActivities().contains(activity)) {
				return ast;
			}
		}
		
		return null;
	}
	
	protected static boolean inBlacklist(Set<ActivitySpanningTree> clusters,
			ArrayList<Set<ActivitySpanningTree>> blacklist) {
		
		// check each cluster
		for (Set<ActivitySpanningTree> blackSet : blacklist) {
			boolean isSubSet = true;
			
			// check if blackSet is a subset of clusters
			for (ActivitySpanningTree blackAST : blackSet) {
				if (!blackAST.getActivities().isEmpty()) {
					
					// find the first activity in this blackAST in the current
					// AST
					ActivitySpanningTree clusterAST = findAST(clusters,
							blackAST.getActivities().iterator().next());
					
					// check if the whole set is the same
					if (clusterAST == null
							|| !blackAST.getActivities().equals(
									clusterAST.getActivities())) {
						isSubSet = false;
						break;
					}
				}
				
			}
			if (isSubSet) {
				return true;
			}
		}
		
		return false;
	}
	
	private static boolean goalCheck(Set<ActivitySpanningTree> clusters,
			ArrayList<TimeBlock> tbs) {
		return clusters.size() <= tbs.size();
	}
	
	public static Bridge makeBridge(
			SimpleWeightedGraph<Location, Transportation> graph,
			Activity activity1, Activity activity2) {
		return new Bridge(
				graph.getEdge(activity1.location, activity2.location),
				activity1, activity2);
	}
	
}
