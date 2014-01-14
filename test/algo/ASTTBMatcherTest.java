package algo;

import java.util.ArrayList;
import java.util.Set;

import org.jgrapht.graph.SimpleWeightedGraph;
import org.junit.BeforeClass;
import org.junit.Test;

import schedulable.Transportation;
import state.MatchingStateTest;
import time.TimeBlock;
import util.Debugger;
import activities.ActivitySpanningTree;
import activities.Location;

public class ASTTBMatcherTest {
	
	@BeforeClass
	public static void init() {
		System.out
				.println("**************************************************");
		System.out
				.println("****************** Matcher Test ******************");
		System.out
				.println("**************************************************");
		System.out.println();
		
	}
	
	@Test
	public void testMatchingSuccess() {
		MatchingStateTest.initHelper();
		Set<ActivitySpanningTree> asts = MatchingStateTest.asts;
		
		ArrayList<TimeBlock> schedule = ASTTBMatcher.matching(
				new SimpleWeightedGraph<Location, Transportation>(
						Transportation.class), asts);
		Debugger.printSchedulables(schedule.get(0));
		Debugger.printSchedulables(schedule.get(1));
		Debugger.printSchedulables(schedule.get(2));
		
	}
	
}
