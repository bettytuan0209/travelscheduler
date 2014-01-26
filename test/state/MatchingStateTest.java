package state;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.joda.time.Duration;
import org.joda.time.Interval;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import schedulable.Activity;
import time.LegalTimeline;
import time.TimeBlock;
import activities.ActivitySpanningTree;
import activities.Location;

public class MatchingStateTest {
	
	public static Set<ActivitySpanningTree> asts;
	public static ArrayList<TimeBlock> tbs;
	private MatchingState state;
	
	@BeforeClass
	public static void init() {
		initHelper();
	}
	
	@Test
	public void testMatchingState() {
		state = new MatchingState(asts, tbs);
		Assert.assertEquals(tbs.size(), state.getMatches().size());
		for (ActivitySpanningTree ast : state.getMatches().values()) {
			Assert.assertTrue(ast == null);
		}
	}
	
	@Test
	public void testSuccessorsAndCheckGoal() {
		state = new MatchingState(asts, tbs);
		MatchingState child;
		HashMap<TimeBlock, ActivitySpanningTree> pairs;
		Entry<TimeBlock, ActivitySpanningTree> entry;
		ArrayList<SearchState> successors;
		Iterator<Entry<TimeBlock, ActivitySpanningTree>> itr;
		
		successors = state.successors();
		Assert.assertEquals(2, successors.size());
		
		// ast1 - 2
		// 1 - null
		// 2 - ast1
		// 3 - null
		child = (MatchingState) successors.get(0);
		Assert.assertFalse(child.checkGoal());
		pairs = child.getMatches();
		Assert.assertEquals(3, pairs.size());
		itr = pairs.entrySet().iterator();
		while (itr.hasNext()) {
			entry = itr.next();
			switch (entry.getKey().getIndex()) {
			case 1:
				Assert.assertNull(entry.getValue());
				break;
			case 2:
				Assert.assertEquals(1, entry.getValue().getIndex());
				break;
			case 3:
				Assert.assertNull(entry.getValue());
				break;
			}
			
		}
		
		MatchingState left = (MatchingState) successors.get(0);
		
		// ast1 - 3
		// 1 - null
		// 2 - null
		// 3 - ast1
		child = (MatchingState) successors.get(1);
		Assert.assertFalse(child.checkGoal());
		pairs = child.getMatches();
		itr = pairs.entrySet().iterator();
		while (itr.hasNext()) {
			entry = itr.next();
			switch (entry.getKey().getIndex()) {
			case 1:
				Assert.assertNull(entry.getValue());
				break;
			case 2:
				Assert.assertNull(entry.getValue());
				break;
			case 3:
				Assert.assertEquals(1, entry.getValue().getIndex());
				
				break;
			}
			
		}
		
		successors = child.successors();
		Assert.assertEquals(1, successors.size());
		
		// ast1 - 3, ast3 - 1
		// 1 - ast3
		// 2 - null
		// 3 - ast1
		child = (MatchingState) successors.get(0);
		Assert.assertFalse(child.checkGoal());
		pairs = child.getMatches();
		itr = pairs.entrySet().iterator();
		while (itr.hasNext()) {
			entry = itr.next();
			switch (entry.getKey().getIndex()) {
			case 1:
				Assert.assertEquals(3, entry.getValue().getIndex());
				break;
			case 2:
				Assert.assertNull(entry.getValue());
				break;
			case 3:
				Assert.assertEquals(1, entry.getValue().getIndex());
				
				break;
			}
			
		}
		
		successors = successors.get(0).successors();
		Assert.assertEquals(1, successors.size());
		
		// ast1 - 3, ast3 - 1, ast2 - 2. Solution
		// 1 - ast3
		// 2 - ast2
		// 3 - ast1
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
		
		// Continue with left branch
		successors = left.successors();
		Assert.assertEquals(2, successors.size());
		
		// ast1 - 2, ast3 - 3
		// 1 - null
		// 2 - ast1
		// 3 - ast3
		child = (MatchingState) successors.get(1);
		Assert.assertFalse(child.checkGoal());
		pairs = child.getMatches();
		itr = pairs.entrySet().iterator();
		while (itr.hasNext()) {
			entry = itr.next();
			switch (entry.getKey().getIndex()) {
			case 1:
				Assert.assertNull(entry.getValue());
				break;
			case 2:
				Assert.assertEquals(1, entry.getValue().getIndex());
				break;
			case 3:
				Assert.assertEquals(3, entry.getValue().getIndex());
				break;
			}
			
		}
		
		successors = child.successors();
		Assert.assertEquals(1, successors.size());
		
		// ast1 - 2, ast2 - 1, ast3 - 3. Solution
		// 1 - ast2
		// 2 - ast1
		// 3 - ast3
		child = (MatchingState) successors.get(0);
		Assert.assertTrue(child.checkGoal());
		pairs = child.getMatches();
		Assert.assertEquals(3, pairs.size());
		itr = pairs.entrySet().iterator();
		while (itr.hasNext()) {
			entry = itr.next();
			switch (entry.getValue().getIndex()) {
			case 1:
				Assert.assertEquals(2, entry.getKey().getIndex());
				break;
			case 2:
				Assert.assertEquals(1, entry.getKey().getIndex());
				break;
			case 3:
				Assert.assertEquals(3, entry.getKey().getIndex());
				break;
			}
			
		}
		
	}
	
	public static void initHelper() {
		asts = new HashSet<ActivitySpanningTree>();
		tbs = new ArrayList<TimeBlock>();
		
		// build TBs
		TimeBlock tb1 = new TimeBlock(1, new Interval(1, 20),
				new Location(1, 1), new Location(1, 1));
		TimeBlock tb2 = new TimeBlock(2, new Interval(30, 40), new Location(1,
				1), new Location(1, 1));
		TimeBlock tb3 = new TimeBlock(3, new Interval(50, 60), new Location(1,
				1), new Location(1, 1));
		Assert.assertTrue(tbs.add(tb1));
		Assert.assertTrue(tbs.add(tb2));
		Assert.assertTrue(tbs.add(tb3));
		
		// build AST1 with TB 2 3
		Activity activity1 = new Activity("activity1", new Duration(1),
				new Location(1, 1), new LegalTimeline(new Interval(35, 55)));
		activity1.legalTimeline.schedule(35, 55);
		ActivitySpanningTree ast1 = new ActivitySpanningTree(1, activity1);
		
		// build AST2 with TB 1 2 3
		Activity activity2 = new Activity("activity2", new Duration(1),
				new Location(1, 1), new LegalTimeline(new Interval(10, 55)));
		activity2.legalTimeline.schedule(10, 55);
		ActivitySpanningTree ast2 = new ActivitySpanningTree(2, activity2);
		
		// build AST3 with TB 1 3
		Activity activity3 = new Activity("activity3", new Duration(1),
				new Location(1, 1), new LegalTimeline(new Interval(10, 55)));
		activity3.legalTimeline.schedule(10, 20);
		activity3.legalTimeline.schedule(50, 55);
		ActivitySpanningTree ast3 = new ActivitySpanningTree(3, activity3);
		
		Assert.assertTrue(asts.add(ast1));
		Assert.assertTrue(asts.add(ast2));
		Assert.assertTrue(asts.add(ast3));
	}
	
}
