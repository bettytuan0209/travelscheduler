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

/**
 * The algorithm that clusters activities into ActivitySpanningTrees. If
 * clustering was successful, pass it to matcher to continue scheduling
 * 
 * @author chiao-yutuan
 * 
 */
public class ClusterManager {
	
	private SimpleWeightedGraph<Location, Transportation> graph;
	private ArrayList<TimeBlock> tbs;
	private ArrayList<Set<ActivitySpanningTree>> blacklist;
	private Set<ActivitySpanningTree> clusters;
	
	/**
	 * Constructor
	 * 
	 * @param graph
	 *            The graph that represents the relationship between the
	 *            locations
	 * @param tbs
	 *            The TBs that can potentially be matched with
	 */
	public ClusterManager(SimpleWeightedGraph<Location, Transportation> graph,
			ArrayList<TimeBlock> tbs) {
		this.graph = graph;
		this.tbs = tbs;
		blacklist = new ArrayList<Set<ActivitySpanningTree>>();
		clusters = new HashSet<ActivitySpanningTree>();
	}
	
	/**
	 * The first module of the scheduling algorithms. Clustering algorithm that
	 * partitions all activities into clusters such that they can potentially
	 * get matched with a tb. If clustering is successful, it will get passed to
	 * the matching module to continue the scheduling
	 * 
	 * 
	 * @param activities
	 *            The set of activities that will get partitioned
	 * 
	 * @return A list of TimeBlocks with the activities scheduled. Null if
	 *         impossible to schedule
	 */
	
	public ArrayList<TimeBlock> clustering(Set<Activity> activities) {
		
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
		
		System.out.println("# BRIDGES: ");
		for (Bridge bridge : bridges) {
			System.out.println(bridge);
		}
		System.out.println();
		
		// call a recursive function to cluster
		return clusterInRange(bridges);
		
	}
	
	/**
	 * Recursive function that try to find the next bridge to join. If looks
	 * promising, recurse with following edges. If it reaches the end of
	 * available bridges to join, it returns fail. Parent would then unjoin the
	 * bridge and continue to try down the list
	 * 
	 * @param searchRangeBridges
	 *            An arraylist of bridges that could get considered to join
	 *            activities in increasing order of duration
	 * @return The resulting list of TBs with scheduled activities, or null if
	 *         schedule was unsuccessful
	 * 
	 */
	
	@SuppressWarnings("unchecked")
	private ArrayList<TimeBlock> clusterInRange(
			ArrayList<Bridge> searchRangeBridges) {
		
		// iterate through this given range, try to find ONE bridge to use
		for (Bridge bridge : searchRangeBridges) {
			
			// Find the ASTs that each side of this bridge belong to
			ActivitySpanningTree setA = findAST(bridge.getActivity1());
			ActivitySpanningTree setB = findAST(bridge.getActivity2());
			
			if (!setA.equals(setB)) {
				
				// Build a union set of the two sets
				ActivitySpanningTree union = setA.joinAST(setB, bridge);
				
				// If the resulting union AST is too big or impossible to
				// schedule on the same TB
				if (union.getAvailableTBs(tbs, false, true).size() == 0) {
					
					// build invalid cluster to the blacklist if not in
					// blacklist
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
					
					// if joined AST is in blacklist
					if (inBlacklist(clusters, blacklist)) {
						// restore previous state (aka undo using this
						// bridge)
						clusters.remove(union);
						clusters.add(setA);
						clusters.add(setB);
						bridge.used = false;
						
					} else { // not found in blacklist
					
						ArrayList<TimeBlock> result;
						
						// if the number of ASTs is same or less than the number
						// of TBs
						// and following modules successfully schedule, return
						// schedule
						if (goalCheck()
								&& (result = ASTTBMatcher.matching(graph,
										(Set<ActivitySpanningTree>) DeepCopy
												.copy(clusters),
										(ArrayList<TimeBlock>) DeepCopy
												.copy(tbs))) != null) {
							return result;
							
						} else if ((result = clusterInRange(new ArrayList<Bridge>(
								searchRangeBridges.subList(
										searchRangeBridges.indexOf(bridge) + 1,
										searchRangeBridges.size())))) != null) {
							// recurse with bridges after the one just joined,
							// if it successfully returns the schedule, return
							// it
							
							return result;
							
						} else {// joining this bridge didn't achieve a result
						
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
	
	/**
	 * Find the AST in the clusters of this cluster manager that this activity
	 * belongs to
	 * 
	 * @param activity
	 *            The activity to look for
	 * @return The AST that the activity belongs to, or null if not found
	 */
	protected ActivitySpanningTree findAST(Activity activity) {
		
		return findAST(clusters, activity);
	}
	
	/**
	 * Find the AST in the given clusters that this activity belongs to
	 * 
	 * @param clusters
	 *            The cluster as a set of ASTs to search in
	 * @param activity
	 *            The activity to look for
	 * @return The AST that the activity belongs to, or null if not found
	 */
	protected ActivitySpanningTree findAST(Set<ActivitySpanningTree> clusters,
			Activity activity) {
		
		// go through each AST
		for (ActivitySpanningTree ast : clusters) {
			if (ast.getActivities().contains(activity)) {
				return ast;
			}
		}
		
		return null;
	}
	
	/**
	 * Whether the cluster is in the blacklist, aka a member of the blacklist is
	 * a subset of the cluster, such as cluster: [ABC, DE], and blacklist:
	 * [[ABC], ...]
	 * 
	 * @param clusters
	 *            The clusters, which is a set of ASTs
	 * @param blacklist
	 *            The blacklist as an arraylist of sets of AST
	 * @return TRUE if the clusters are in the blacklist, FALSE otherwise
	 */
	protected boolean inBlacklist(Set<ActivitySpanningTree> clusters,
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
	
	/**
	 * Check if the cluster has completed its job, which is the number of
	 * clusters is the same or less than the number of TBs
	 * 
	 * 
	 * @return TRUE if goal reached, FALSE otherwise
	 */
	private boolean goalCheck() {
		return clusters.size() <= tbs.size();
	}
	
	/**
	 * A helper function that makes a bridge out of a graph and two activities
	 * 
	 * @param graph
	 *            The graph that represents the relationship between locations
	 * @param activity1
	 *            One of the activities on one side of the bridge
	 * @param activity2
	 *            The other activity on one side of the bridge
	 * @return The bridge built from the transportation between the two
	 *         activities
	 */
	public static Bridge makeBridge(
			SimpleWeightedGraph<Location, Transportation> graph,
			Activity activity1, Activity activity2) {
		return new Bridge(
				graph.getEdge(activity1.location, activity2.location),
				activity1, activity2);
	}
	
}
