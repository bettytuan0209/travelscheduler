package time;

import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.junit.Assert;
import org.junit.Test;

import schedulable.Activity;
import schedulable.LegalTime;
import schedulable.Schedulable;
import schedulable.Transportation;
import util.DeepCopy;
import activities.Location;

public class TimeBlockTest {
	TimeBlock tb;
	
	@Test
	public void testTimeBlock() {
		int index = 1;
		Interval interval = new Interval(1, 20);
		Location startLocation = new Location(1, 2);
		Location endLocation = new Location(3, 5);
		tb = new TimeBlock(index, interval, startLocation, endLocation);
		
		Assert.assertEquals(index, tb.getIndex());
		Assert.assertEquals(new Interval(1, 21), tb.getInterval());
		Assert.assertEquals(startLocation, tb.getStartLocation());
		Assert.assertEquals(endLocation, tb.getEndLocation());
		
		interval = new Interval(5, 10);
		Assert.assertEquals(new Interval(1, 21), tb.getInterval());
		
		TimeBlock copy = (TimeBlock) DeepCopy.copy(tb);
		Assert.assertEquals(copy, tb);
		Assert.assertFalse(copy == tb);
		
	}
	
	@Test
	public void testSchedule() {
		tb = new TimeBlock(1, new Interval(1, 10), new Location(1, 1),
				new Location(2, 2));
		
		// out of range
		Assert.assertFalse(tb.schedule(new DateTime(8), new Activity(
				new Duration(11))));
		
		// legal 1-1, 5-8
		Assert.assertTrue(tb.schedule(new DateTime(1), new Activity(
				new Duration(0))));
		Assert.assertTrue(tb.schedule(new DateTime(5), new Activity(
				new Duration(3))));
		
		Assert.assertEquals(new DateTime(5), tb.getLastScheduled().getKey());
		Assert.assertEquals(new Activity(new Duration(3)), tb
				.getLastScheduled().getValue());
		
		// overlap
		Assert.assertFalse(tb.schedule(new DateTime(1), new Activity(
				new Duration(2))));
		Assert.assertFalse(tb.schedule(new DateTime(3), new Activity(
				new Duration(6))));
		
	}
	
	@Test
	public void testScheduleAfterActivity() {
		tb = new TimeBlock(1, new Interval(1, 30), new Location(1, 1),
				new Location(2, 2));
		// Legal: 1 - 5, 10 - 15, 20 - 30
		// Schedule: 2 - 4, 8 - 12, 18 - 21
		LegalTimeline legalTimeline = new LegalTimeline(new Interval(1, 30));
		Assert.assertTrue(legalTimeline.schedule(new DateTime(1),
				new LegalTime(new Duration(4), true)));
		Assert.assertTrue(legalTimeline.schedule(new DateTime(10),
				new LegalTime(new Duration(5), true)));
		Assert.assertTrue(legalTimeline.schedule(new DateTime(20),
				new LegalTime(new Duration(10), true)));
		Assert.assertTrue(tb.getScheduledActivities().schedule(new DateTime(2),
				new Activity(new Duration(2))));
		Assert.assertTrue(tb.getScheduledActivities().schedule(new DateTime(8),
				new Activity(new Duration(4))));
		Assert.assertTrue(tb.getScheduledActivities().schedule(
				new DateTime(18), new Activity(new Duration(3))));
		Assert.assertEquals(new DateTime(18), tb.getLastScheduled().getKey());
		Assert.assertEquals(new Activity(new Duration(3)), tb
				.getLastScheduled().getValue());
		
		Activity activity = new Activity("", new Duration(2), legalTimeline);
		// no bound
		Assert.assertTrue(tb.scheduleAfter(activity));
		Assert.assertEquals(activity,
				(Activity) tb.getScheduledActivities().schedule
						.get(new DateTime(12)));
		
		// with bound
		Assert.assertTrue(tb.scheduleAfter(new DateTime(13), activity));
		Assert.assertEquals(activity,
				(Activity) tb.getScheduledActivities().schedule
						.get(new DateTime(21)));
		Assert.assertTrue(tb.scheduleAfter(new DateTime(28), activity));
		Assert.assertEquals(activity,
				(Activity) tb.getScheduledActivities().schedule
						.get(new DateTime(28)));
		
		// no legalTime
		// schedule: 3 - 5
		Transportation transport = new Transportation(new Duration(2),
				new Location(0, 0), new Location(0, 0));
		tb = new TimeBlock(1, new Interval(1, 30), new Location(1, 1),
				new Location(2, 2));
		Assert.assertTrue(tb.getScheduledActivities().schedule(new DateTime(3),
				new Activity(new Duration(2))));
		Assert.assertTrue(tb.scheduleAfter(new DateTime(3), transport));
		Assert.assertEquals(transport, (Transportation) tb
				.getScheduledActivities().schedule.get(new DateTime(5)));
		Assert.assertTrue(tb.scheduleAfter(transport));
		Assert.assertEquals(transport, (Transportation) tb
				.getScheduledActivities().schedule.get(new DateTime(1)));
		
	}
	
	@Test
	public void testScheduleBeforeTb() {
		tb = new TimeBlock(1, new Interval(1, 30), new Location(1, 1),
				new Location(2, 2));
		
		// too long
		Activity activity = new Activity("", new Duration(2),
				new Location(1, 2));
		Assert.assertFalse(tb.scheduleBeforeTb(activity));
		
		// legal
		activity = new Activity("", new Duration(1), new Location(1, 2));
		Assert.assertTrue(tb.scheduleBeforeTb(activity));
		Map.Entry<DateTime, Schedulable> entry = tb.getScheduledActivities().schedule
				.firstEntry();
		Assert.assertEquals(new DateTime(0), entry.getKey());
		Assert.assertEquals(activity, entry.getValue());
		
		// already scheduled
		Activity activity2 = new Activity("", new Duration(0), new Location(1,
				2));
		Assert.assertFalse(tb.scheduleBeforeTb(activity2));
		entry = tb.getScheduledActivities().schedule.firstEntry();
		Assert.assertEquals(new DateTime(0), entry.getKey());
		Assert.assertEquals(activity, entry.getValue());
		
	}
	
	@Test
	public void testScheduleAfterTb() {
		tb = new TimeBlock(1, new Interval(1, 30), new Location(1, 1),
				new Location(2, 2));
		Activity activity = new Activity("", new Duration(5),
				new Location(3, 5));
		Assert.assertTrue(tb.schedule(new DateTime(2), activity));
		
		// too long
		activity = new Activity("", new Duration(2), new Location(1, 2));
		Assert.assertFalse(tb.scheduleAfterTb(activity));
		
		// legal
		activity = new Activity("", new Duration(1), new Location(1, 2));
		Assert.assertTrue(tb.scheduleAfterTb(activity));
		Map.Entry<DateTime, Schedulable> entry = tb.getScheduledActivities().schedule
				.lastEntry();
		Assert.assertEquals(new DateTime(7), entry.getKey());
		Assert.assertEquals(activity, entry.getValue());
		
	}
	
	@Test
	public void testLastEndTime() {
		tb = new TimeBlock(1, new Interval(1, 30), new Location(1, 1),
				new Location(2, 2));
		
		Assert.assertEquals(new DateTime(1), tb.lastEndTime());
		
		// 3 - 3, 6 - 8
		Assert.assertTrue(tb.getScheduledActivities().schedule(new DateTime(3),
				new Activity(new Duration(3))));
		Assert.assertTrue(tb.getScheduledActivities().schedule(new DateTime(6),
				new Activity(new Duration(2))));
		
		Assert.assertEquals(new DateTime(8), tb.lastEndTime());
		
	}
	
}
