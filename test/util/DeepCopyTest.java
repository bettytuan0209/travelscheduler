package util;

import java.util.ArrayList;
import java.util.HashSet;

import org.jgrapht.graph.SimpleWeightedGraph;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.junit.Assert;
import org.junit.Test;

import schedulable.Activity;
import schedulable.LegalTime;
import schedulable.Transportation;
import state.SchedulingState;
import time.LegalTimeline;
import time.TimeBlock;
import time.Timeline;
import activities.Location;

public class DeepCopyTest {
	
	@Test
	public void testCopy() {
		Timeline timelineOrigin = new Timeline(new Interval(5, 10));
		Timeline timelineCopy = (Timeline) DeepCopy.copy(timelineOrigin);
		
		Assert.assertFalse(timelineOrigin == timelineCopy);
		
		// check the interval
		Assert.assertFalse(timelineOrigin.getInterval() == timelineCopy
				.getInterval());
		Assert.assertEquals(timelineOrigin.getInterval().getStartMillis(),
				timelineCopy.getInterval().getStartMillis());
		Assert.assertEquals(timelineOrigin.getInterval().getEndMillis(),
				timelineCopy.getInterval().getEndMillis());
		
		// check the schedule
		Assert.assertFalse(timelineOrigin.getSchedule() == timelineCopy
				.getSchedule());
		
		Activity activityOrigin = new Activity("museum", new Duration(2),
				new Location(5, 10));
		Activity activityCopy = (Activity) DeepCopy.copy(activityOrigin);
		
		Assert.assertFalse(activityOrigin == activityCopy);
		
		Assert.assertFalse(activityOrigin.title == activityCopy.title);
		
		activityOrigin.title = "original";
		Assert.assertEquals("museum", activityCopy.title);
		
		activityOrigin.location = new Location(1, 2);
		Assert.assertEquals(5, activityCopy.location.getLatitude(), 0);
		Assert.assertEquals(10, activityCopy.location.getLongitude(), 0);
		
		Assert.assertFalse(activityOrigin.legalTimeline == activityCopy.legalTimeline);
		Assert.assertTrue(activityOrigin.legalTimeline.getInterval().equals(
				activityCopy.legalTimeline.getInterval()));
		
		TimeBlock tb = new TimeBlock(1, new Interval(1, 20),
				new Location(1, 1), new Location(1, 1));
		ArrayList<TimeBlock> availableTBs = new ArrayList<TimeBlock>();
		availableTBs.add(tb);
		SimpleWeightedGraph<Location, Transportation> graph = new SimpleWeightedGraph<Location, Transportation>(
				Transportation.class);
		LegalTimeline legal1 = new LegalTimeline(new Interval(1, 20));
		Assert.assertTrue(legal1.schedule(new DateTime(1), new LegalTime(
				new Duration(20))));
		Activity museum = new Activity("museum", new Duration(2), legal1);
		Activity concert = new Activity("concert", new Duration(3),
				(LegalTimeline) DeepCopy.copy(legal1));
		Activity park = new Activity("park", new Duration(1),
				(LegalTimeline) DeepCopy.copy(legal1));
		HashSet<Activity> activities = new HashSet<Activity>();
		activities.add(museum);
		activities.add(concert);
		activities.add(park);
		
		graph.addVertex(museum.location);
		graph.addVertex(concert.location);
		graph.addVertex(park.location);
		graph.addEdge(museum.location, concert.location, new Transportation(
				new Duration(3)));
		graph.addEdge(park.location, museum.location, new Transportation(
				new Duration(7)));
		graph.addEdge(park.location, concert.location, new Transportation(
				new Duration(2)));
		
		SchedulingState state = new SchedulingState(tb, graph, activities);
		SchedulingState clone = state.clone();
		Assert.assertTrue(clone.getGraph().containsVertex(museum.location));
		Assert.assertTrue(clone.getGraph().containsVertex(park.location));
		Assert.assertEquals(new Transportation(new Duration(3)), clone
				.getGraph().getEdge(museum.location, concert.location));
		
	}
	
}
