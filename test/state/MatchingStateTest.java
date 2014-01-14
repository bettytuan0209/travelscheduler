package state;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.joda.time.Interval;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import time.TimeBlock;
import activities.ActivitySpanningTree;
import activities.Location;

public class MatchingStateTest {
	
	public static Set<ActivitySpanningTree> asts;
	private MatchingState state;
	
	@BeforeClass
	public static void init() {
		initHelper();
	}
	
	@Test
	public void testMatchingState() {
		state = new MatchingState(asts);
		Assert.assertTrue(state.getMatches().isEmpty());
	}
	
	@Test
	public void testSuccessorsAndCheckGoal() {
		state = new MatchingState(asts);
		MatchingState child;
		HashMap<TimeBlock, ActivitySpanningTree> pairs;
		Entry<TimeBlock, ActivitySpanningTree> entry;
		ArrayList<SearchState> successors;
		Iterator<Entry<TimeBlock, ActivitySpanningTree>> itr;
		
		successors = state.successors();
		successors.get(0).successors();
		Assert.assertEquals(2, successors.size());
		
		// ast1 - 2
		child = (MatchingState) successors.get(0);
		Assert.assertFalse(child.checkGoal());
		pairs = child.getMatches();
		Assert.assertEquals(1, pairs.size());
		entry = pairs.entrySet().iterator().next();
		Assert.assertEquals(1, entry.getValue().getIndex());
		Assert.assertEquals(2, entry.getKey().getIndex());
		
		// ast1 - 3
		child = (MatchingState) successors.get(1);
		Assert.assertFalse(child.checkGoal());
		pairs = child.getMatches();
		Assert.assertEquals(1, pairs.size());
		entry = pairs.entrySet().iterator().next();
		Assert.assertEquals(1, entry.getValue().getIndex());
		Assert.assertEquals(3, entry.getKey().getIndex());
		
		successors = successors.get(1).successors();
		Assert.assertEquals(2, successors.size());
		
		// ast1 - 3, ast2 - 1
		child = (MatchingState) successors.get(0);
		Assert.assertFalse(child.checkGoal());
		pairs = child.getMatches();
		Assert.assertEquals(2, pairs.size());
		itr = pairs.entrySet().iterator();
		while (itr.hasNext()) {
			entry = itr.next();
			switch (entry.getValue().getIndex()) {
			case 1:
				Assert.assertEquals(3, entry.getKey().getIndex());
				break;
			case 2:
				Assert.assertEquals(1, entry.getKey().getIndex());
				break;
			}
			
		}
		
		// ast1 - 3, ast2 - 2
		child = (MatchingState) successors.get(1);
		Assert.assertFalse(child.checkGoal());
		pairs = child.getMatches();
		Assert.assertEquals(2, pairs.size());
		itr = pairs.entrySet().iterator();
		while (itr.hasNext()) {
			entry = itr.next();
			switch (entry.getValue().getIndex()) {
			case 1:
				Assert.assertEquals(3, entry.getKey().getIndex());
				break;
			case 2:
				Assert.assertEquals(2, entry.getKey().getIndex());
				break;
			}
			
		}
		
		// ast1 - 3, ast2 - 1, ast3 - not valid
		Assert.assertTrue(successors.get(0).successors().isEmpty());
		
		successors = successors.get(1).successors();
		Assert.assertEquals(1, successors.size());
		
		// ast1 - 3, ast2 - 2, ast3 - 1. Solution
		child = (MatchingState) successors.get(0);
		Assert.assertTrue(child.checkGoal());
		pairs = child.getMatches();
		Assert.assertEquals(3, pairs.size());
		itr = pairs.entrySet().iterator();
		while (itr.hasNext()) {
			entry = itr.next();
			switch (entry.getValue().getIndex()) {
			case 1:
				Assert.assertEquals(3, entry.getKey().getIndex());
				break;
			case 2:
				Assert.assertEquals(2, entry.getKey().getIndex());
				break;
			case 3:
				Assert.assertEquals(1, entry.getKey().getIndex());
				break;
			}
			
		}
		
	}
	
	public static void initHelper() {
		ArrayList<TimeBlock> availableTBs;
		asts = new HashSet<ActivitySpanningTree>();
		
		// build TBs
		TimeBlock tb1 = new TimeBlock(1, new Interval(1, 20),
				new Location(0, 0), new Location(0, 0));
		TimeBlock tb2 = new TimeBlock(2, new Interval(30, 40), new Location(0,
				0), new Location(0, 0));
		TimeBlock tb3 = new TimeBlock(3, new Interval(50, 60), new Location(0,
				0), new Location(0, 0));
		
		// build AST1 with TB 2 3
		availableTBs = new ArrayList<TimeBlock>();
		Assert.assertTrue(availableTBs.add(tb2));
		Assert.assertTrue(availableTBs.add(tb3));
		ActivitySpanningTree ast1 = new ActivitySpanningTree(1, availableTBs);
		
		// build AST2 with TB 1 2 3
		availableTBs = new ArrayList<TimeBlock>();
		Assert.assertTrue(availableTBs.add(tb1));
		Assert.assertTrue(availableTBs.add(tb2));
		Assert.assertTrue(availableTBs.add(tb3));
		ActivitySpanningTree ast2 = new ActivitySpanningTree(2, availableTBs);
		
		// build AST3 with TB 1 3
		availableTBs = new ArrayList<TimeBlock>();
		Assert.assertTrue(availableTBs.add(tb1));
		Assert.assertTrue(availableTBs.add(tb3));
		ActivitySpanningTree ast3 = new ActivitySpanningTree(3, availableTBs);
		
		Assert.assertTrue(asts.add(ast1));
		Assert.assertTrue(asts.add(ast2));
		Assert.assertTrue(asts.add(ast3));
	}
	
}
