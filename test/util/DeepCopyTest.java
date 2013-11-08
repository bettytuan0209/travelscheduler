package util;

import org.joda.time.Duration;
import org.joda.time.Interval;
import org.junit.Assert;
import org.junit.Test;

import schedulable.Activity;
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
		
		Assert.assertFalse(activityOrigin.getLegalTimeline() == activityCopy
				.getLegalTimeline());
		Assert.assertTrue(activityOrigin.getLegalTimeline().getInterval()
				.equals(activityCopy.getLegalTimeline().getInterval()));
		
	}
	
}
