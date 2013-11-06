package time;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.junit.Assert;
import org.junit.Test;

import activities.Activity;

public class TimelineTest {
	Timeline timeline;
	
	@Test
	public void earliestSchedulableLegalAfterTest() {
		timeline = new Timeline(new Interval(1, 10));
		
		// out of range
		Activity activity = new Activity(new Duration(20));
		Assert.assertNull(timeline.earliestSchedulableLegalAfter(
				new DateTime(0), activity.getLegalTimes(), activity));
		activity = new Activity(new Duration(0));
		Assert.assertNull(timeline.earliestSchedulableLegalAfter(new DateTime(
				11), activity.getLegalTimes(), activity));
	}
	
	@Test
	public void scheduleTest() {
		timeline = new Timeline(new Interval(1, 10));
		
		// out of range
		Assert.assertFalse(timeline.schedule(new DateTime(8), new Activity(
				new Duration(11))));
		Assert.assertFalse(timeline.schedule(new DateTime(0), new Activity(
				new Duration(0))));
		
		// legal
		Assert.assertTrue(timeline.schedule(new DateTime(1), new Activity(
				new Duration(0))));
		Assert.assertTrue(timeline.schedule(new DateTime(8), new Activity(
				new Duration(2))));
		Assert.assertTrue(timeline.schedule(new DateTime(5), new Activity(
				new Duration(3))));
		Assert.assertTrue(timeline.schedule(new DateTime(1), new Activity(
				new Duration(2))));
		Assert.assertTrue(timeline.schedule(new DateTime(10), new Activity(
				new Duration(0))));
		
		// overlap
		Assert.assertFalse(timeline.schedule(new DateTime(3), new Activity(
				new Duration(5))));
		Assert.assertFalse(timeline.schedule(new DateTime(1), new Activity(
				new Duration(5))));
		
		// fill the timeline
		Assert.assertTrue(timeline.schedule(new DateTime(3), new Activity(
				new Duration(2))));
		
	}
	
	@Test
	public void lastEndTimeTest() {
		timeline = new Timeline(new Interval(1, 10));
		
		Assert.assertTrue(timeline.schedule(new DateTime(2), new Activity(
				new Duration(2))));
		Assert.assertTrue(timeline.schedule(new DateTime(7), new Activity(
				new Duration(1))));
		Assert.assertEquals(new DateTime(8), timeline.lastEndTime());
	}
	
}
