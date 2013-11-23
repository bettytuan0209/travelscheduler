package schedulable;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.junit.Assert;
import org.junit.Test;

import time.LegalTimeline;
import util.DeepCopy;
import activities.Location;

public class ActivityTest {
	Activity activity;
	
	/**
	 * Test constructor
	 */
	@Test
	public void testActivityDuration() {
		Duration duration = new Duration(2);
		
		activity = new Activity(duration);
		Assert.assertEquals(duration, activity.duration);
		Assert.assertNotNull(activity.legalTimeline);
		Assert.assertEquals("", activity.title);
		Assert.assertTrue(activity.legalTimeline.isEmpty());
		
		Activity copy = (Activity) DeepCopy.copy(activity);
		Assert.assertEquals(copy, activity);
		Assert.assertFalse(copy == activity);
	}
	
	/**
	 * Test constructor
	 */
	@Test
	public void testActivityStringDurationLocation() {
		Duration duration = new Duration(2);
		Location location = new Location(1, 2);
		
		activity = new Activity("title", duration, location);
		Assert.assertEquals("title", activity.title);
		Assert.assertEquals(duration, activity.duration);
		Assert.assertEquals(location, activity.location);
		Assert.assertNotNull(activity.legalTimeline);
		Assert.assertTrue(activity.legalTimeline.isEmpty());
		
	}
	
	/**
	 * Test constructor
	 */
	@Test
	public void testActivityStringDurationLegalTimeline() {
		Duration duration = new Duration(2);
		LegalTimeline legalTimeline = new LegalTimeline(new Interval(1, 20));
		Assert.assertTrue(legalTimeline.schedule(new DateTime(2),
				new LegalTime(new Duration(4), true)));
		
		activity = new Activity("title", duration, legalTimeline);
		Assert.assertEquals("title", activity.title);
		Assert.assertEquals(duration, activity.duration);
		Assert.assertNotNull(activity.legalTimeline);
		Assert.assertEquals(legalTimeline, activity.legalTimeline);
		
		Assert.assertTrue(legalTimeline.schedule(new DateTime(10),
				new LegalTime(new Duration(5), true)));
		Assert.assertEquals(new DateTime(15),
				activity.legalTimeline.lastEndTime());
	}
	
	/**
	 * Test constructor
	 */
	@Test
	public void testActivityStringDurationLocationLegalTimeline() {
		Duration duration = new Duration(2);
		Location location = new Location(1, 2);
		LegalTimeline legalTimeline = new LegalTimeline(new Interval(1, 20));
		Assert.assertTrue(legalTimeline.schedule(new DateTime(2),
				new LegalTime(new Duration(4), true)));
		
		activity = new Activity("title", duration, location, legalTimeline);
		Assert.assertEquals("title", activity.title);
		Assert.assertEquals(duration, activity.duration);
		Assert.assertEquals(location, activity.location);
		Assert.assertNotNull(activity.legalTimeline);
		Assert.assertEquals(legalTimeline, activity.legalTimeline);
	}
	
	@Test
	public void testForwardChecking() {
		activity = new Activity("", new Duration(2), new LegalTimeline(
				new Interval(1, 20)));
		Assert.assertTrue(activity.addLegalTime(new Interval(1, 2)));
		Assert.assertFalse(activity.forwardChecking());
		
		Assert.assertTrue(activity.addLegalTime(new Interval(2, 3)));
		Assert.assertFalse(activity.forwardChecking());
		
		Assert.assertTrue(activity.addLegalTime(new Interval(3, 5)));
		Assert.assertTrue(activity.forwardChecking());
	}
	
	@Test
	public void testAddLegalTime() {
		activity = new Activity("", new Duration(2), new LegalTimeline(
				new Interval(1, 20)));
		Assert.assertTrue(activity.addLegalTime(new Interval(1, 2)));
		Assert.assertTrue(activity.addLegalTime(new Interval(4, 6)));
		Assert.assertFalse(activity.addLegalTime(new Interval(1, 1)));
		Assert.assertFalse(activity.addLegalTime(new Interval(3, 5)));
		
	}
	
}
