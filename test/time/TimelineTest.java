package time;

import java.util.TreeMap;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.junit.Assert;
import org.junit.Test;

import schedulable.Activity;
import schedulable.LegalTime;
import schedulable.Schedulable;
import util.DeepCopy;

public class TimelineTest {
	Timeline timeline;
	
	@Test
	public void testTimelineInterval() {
		Interval interval = new Interval(1, 5);
		timeline = new Timeline(interval);
		Assert.assertTrue(timeline.getInterval().equals(new Interval(1, 6)));
		Assert.assertTrue(timeline.schedule.isEmpty());
		Timeline copy = (Timeline) DeepCopy.copy(timeline);
		Assert.assertTrue(timeline.equals(copy));
		copy.interval = new Interval(3, 5);
		Assert.assertFalse(timeline.equals(copy));
		
	}
	
	@Test
	public void testTimelineTreeMapOfDateTimeSchedulable() {
		TreeMap<DateTime, Schedulable> schedule = new TreeMap<DateTime, Schedulable>();
		timeline = new Timeline(schedule);
		Assert.assertTrue(timeline.getInterval().equals(new Interval(0, 0)));
		Assert.assertTrue(timeline.schedule.isEmpty());
		
		// valid entry
		schedule.put(new DateTime(2), new Activity(new Duration(2)));
		schedule.put(new DateTime(5), new Activity(new Duration(0)));
		Assert.assertTrue(timeline.schedule.isEmpty()); // check no reference
		timeline = new Timeline(schedule);
		Assert.assertTrue(timeline.getInterval().equals(new Interval(2, 6))); // check
																				// no
																				// reference
		Assert.assertEquals(schedule, timeline.schedule);
		Assert.assertFalse(schedule == timeline.schedule);
		
		// test getSchedule()
		TreeMap<DateTime, Schedulable> getterResult = timeline.getSchedule();
		Assert.assertEquals(schedule, getterResult);
		Assert.assertFalse(schedule == getterResult);
		getterResult.put(new DateTime(4), new Activity(new Duration(1)));
		Assert.assertEquals(2, timeline.getNumScheduled());
		
		// test equals()
		Timeline copy = (Timeline) DeepCopy.copy(timeline);
		Assert.assertTrue(timeline.equals(copy));
		copy.unschedule(new DateTime(2));
		Assert.assertFalse(timeline.equals(copy));
		
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testTimelineConstructorOverlap() {
		// invalid entry - overlap
		TreeMap<DateTime, Schedulable> schedule = new TreeMap<DateTime, Schedulable>();
		schedule.put(new DateTime(2), new LegalTime(new Duration(2), true));
		schedule.put(new DateTime(1), new LegalTime(new Duration(3), true));
		
		timeline = new Timeline(schedule);
		
		// test equals()
		Timeline copy = (Timeline) DeepCopy.copy(timeline);
		Assert.assertTrue(timeline.equals(copy));
	}
	
	@Test
	public void testTimelineIntervalTreepMap() {
		TreeMap<DateTime, Schedulable> schedule = new TreeMap<DateTime, Schedulable>();
		schedule.put(new DateTime(2), new LegalTime(new Duration(2), true));
		schedule.put(new DateTime(5), new LegalTime(new Duration(0), true));
		timeline = new Timeline(new Interval(1, 20), schedule);
		Assert.assertTrue(timeline.getInterval().equals(new Interval(1, 21)));
		
	}
	
	@Test
	public void testScheduleAfter() {
		LegalTimeline legalTimeline = new LegalTimeline(new Interval(1, 20));
		timeline = new Timeline(new Interval(1, 20));
		DateTime bound = new DateTime(3);
		Activity schedulable = new Activity(new Duration(2));
		
		// valid schedule
		// Legal: 2 - 9 (false), 10 - 12
		// Schedule: 4 - 9, 10 - 12
		Assert.assertTrue(legalTimeline.schedule(new DateTime(2),
				new LegalTime(new Duration(7), false)));
		Assert.assertTrue(legalTimeline.schedule(new DateTime(10),
				new LegalTime(new Duration(2), true)));
		Assert.assertTrue(timeline.schedule(new DateTime(4), new Activity(
				new Duration(5))));
		Assert.assertTrue(timeline.scheduleAfter(bound, legalTimeline,
				schedulable));
		Assert.assertEquals(schedulable, timeline.getSchedule().lastEntry()
				.getValue());
		
		// invalid schedule
		// Legal: 2 - 9, 10 - 12
		// Schedule: 4 - 9, 9 - 13
		Assert.assertNotNull(legalTimeline.unschedule(new DateTime(2)));
		Assert.assertTrue(legalTimeline.schedule(new DateTime(2),
				new LegalTime(new Duration(7), true)));
		Assert.assertNotNull(timeline.unschedule(new DateTime(10)));
		Assert.assertTrue(timeline.schedule(new DateTime(9), new Activity(
				new Duration(4))));
		bound = new DateTime(10);
		Assert.assertFalse(timeline.scheduleAfter(bound, legalTimeline,
				schedulable));
		Assert.assertNotEquals(schedulable, timeline.getSchedule().lastEntry()
				.getValue());
		
	}
	
	@Test
	public void testEarliestSchedulableLegalAfter() {
		LegalTimeline legalTimeline;
		TreeMap<DateTime, Schedulable> scheduleMap = new TreeMap<DateTime, Schedulable>();
		DateTime bound;
		Activity schedulable = new Activity(new Duration(2));
		
		// test no legaltime
		// Legal: (empty)
		// Schedule: 1 - 5
		scheduleMap.put(new DateTime(1), new Activity(new Duration(4)));
		timeline = new Timeline(new Interval(1, 20), scheduleMap);
		bound = new DateTime(1);
		legalTimeline = new LegalTimeline(new Interval(1, 20));
		Assert.assertNull(timeline.earliestSchedulableLegalAfter(bound,
				legalTimeline, schedulable));
		
		// first legal available (right)
		// Legal: 2 - 9, 10 - 12
		// Schedule: 1 - 5
		Assert.assertTrue(legalTimeline.schedule(new DateTime(2),
				new LegalTime(new Duration(7), true)));
		Assert.assertTrue(legalTimeline.schedule(new DateTime(10),
				new LegalTime(new Duration(2), true)));
		Assert.assertEquals(new DateTime(5), timeline
				.earliestSchedulableLegalAfter(bound, legalTimeline,
						schedulable));
		
		// first legal available (left)
		// Legal: 2 - 9, 10 - 12
		// Schedule: 4 - 9
		Assert.assertNotNull(timeline.unschedule(new DateTime(1)));
		Assert.assertTrue(timeline.schedule(new DateTime(4), new Activity(
				new Duration(5))));
		Assert.assertEquals(new DateTime(2), timeline
				.earliestSchedulableLegalAfter(bound, legalTimeline,
						schedulable));
		
		// first legal not available
		// Legal: 2 - 9 (false), 10 - 12
		// Schedule: 4 - 9
		Assert.assertNotNull(legalTimeline.unschedule(new DateTime(2)));
		Assert.assertTrue(legalTimeline.schedule(new DateTime(2),
				new LegalTime(new Duration(7), false)));
		Assert.assertEquals(new DateTime(10), timeline
				.earliestSchedulableLegalAfter(bound, legalTimeline,
						schedulable));
		
		// first legal not long enough,
		// second long enough after trim for schedulable
		// Legal: 2 - 3, 10 - 12
		// Schedule: 4 - 9
		Assert.assertNotNull(legalTimeline.unschedule(new DateTime(2)));
		Assert.assertTrue(legalTimeline.schedule(new DateTime(2),
				new LegalTime(new Duration(1), true)));
		Assert.assertEquals(new DateTime(10), timeline
				.earliestSchedulableLegalAfter(bound, legalTimeline,
						schedulable));
		
		// change first legal same length
		// but schedule something that conflicts
		// Legal: 2 - 4, 10 - 12
		// Schedule: 3 - 9
		Assert.assertNotNull(legalTimeline.unschedule(new DateTime(2)));
		Assert.assertTrue(legalTimeline.schedule(new DateTime(2),
				new LegalTime(new Duration(2), true)));
		Assert.assertNotNull(timeline.unschedule(new DateTime(4)));
		Assert.assertTrue(timeline.schedule(new DateTime(3), new Activity(
				new Duration(6))));
		Assert.assertEquals(new DateTime(10), timeline
				.earliestSchedulableLegalAfter(bound, legalTimeline,
						schedulable));
		
		// change schedulable to 0 secs, still conflicts
		// Legal: 2 - 4, 10 - 12
		// Schedule: 3 - 3
		Assert.assertNotNull(timeline.unschedule(new DateTime(3)));
		Assert.assertTrue(timeline.schedule(new DateTime(3), new Activity(
				new Duration(0))));
		Assert.assertEquals(new DateTime(10), timeline
				.earliestSchedulableLegalAfter(bound, legalTimeline,
						schedulable));
		
		// first schedulable before first legal,
		// second schedulable overlaps with first legal,
		// and first legal duration left not enough
		// second legal has conflicts
		// third legal not long enough
		// fourth available
		// Legal: 2 - 4, 10 - 12, 13 - 14, 17 - 19
		// Schedule: 1 - 2, 3 - 5, 8 - 15
		Assert.assertTrue(legalTimeline.schedule(new DateTime(13),
				new LegalTime(new Duration(1), true)));
		Assert.assertTrue(legalTimeline.schedule(new DateTime(17),
				new LegalTime(new Duration(2), true)));
		Assert.assertNotNull(timeline.unschedule(new DateTime(3)));
		Assert.assertTrue(timeline.schedule(new DateTime(1), new Activity(
				new Duration(1))));
		Assert.assertTrue(timeline.schedule(new DateTime(3), new Activity(
				new Duration(2))));
		Assert.assertTrue(timeline.schedule(new DateTime(8), new Activity(
				new Duration(7))));
		Assert.assertEquals(new DateTime(17), timeline
				.earliestSchedulableLegalAfter(bound, legalTimeline,
						schedulable));
		
		// 2 legal and schedulables before bound
		// Legal: 2 - 4, 10 - 12, 13 - 16, 17 - 19
		// Schedule: 1 - 2, 3 - 5
		Assert.assertNotNull(legalTimeline.unschedule(new DateTime(13)));
		Assert.assertTrue(legalTimeline.schedule(new DateTime(13),
				new LegalTime(new Duration(3), true)));
		Assert.assertNotNull(timeline.unschedule(new DateTime(8)));
		bound = new DateTime(11);
		Assert.assertEquals(new DateTime(13), timeline
				.earliestSchedulableLegalAfter(bound, legalTimeline,
						schedulable));
		
		// bound is the same as start of a scheduled with 0 duration
		// Legal: 2 - 4, 9 - 12, 13 - 16, 17 - 19
		// Schedule: 1 - 2, 3 - 5, 10 - 10, 13 - 13
		Assert.assertNotNull(legalTimeline.unschedule(new DateTime(10)));
		Assert.assertTrue(legalTimeline.schedule(new DateTime(9),
				new LegalTime(new Duration(3), true)));
		Assert.assertTrue(timeline.schedule(new DateTime(10), new Activity(
				new Duration(0))));
		Assert.assertTrue(timeline.schedule(new DateTime(13), new Activity(
				new Duration(0))));
		bound = new DateTime(5);
		Assert.assertEquals(new DateTime(14), timeline
				.earliestSchedulableLegalAfter(bound, legalTimeline,
						schedulable));
		
		// bound is after the last legal time
		// Legal: 2 - 4, 9 - 12, 13 - 16
		// Schedule: 1 - 2, 3 - 5, 10 - 10, 13 - 13
		Assert.assertNotNull(legalTimeline.unschedule(new DateTime(17)));
		bound = new DateTime(17);
		Assert.assertNull(timeline.earliestSchedulableLegalAfter(bound,
				legalTimeline, schedulable));
		
		// bound is after the timeline
		// Legal: 2 - 4, 9 - 12, 13 - 16, 17 - 19
		// Schedule: 1 - 2, 3 - 5, 10 - 10, 13 - 13
		bound = new DateTime(30);
		Assert.assertNull(timeline.earliestSchedulableLegalAfter(bound,
				legalTimeline, schedulable));
		
		// ran out of legals
		// Legal: 2 - 4
		// Schedule: 1 - 2, 3 - 5
		Assert.assertNotNull(legalTimeline.unschedule(new DateTime(9)));
		Assert.assertNotNull(legalTimeline.unschedule(new DateTime(13)));
		Assert.assertNotNull(timeline.unschedule(new DateTime(10)));
		Assert.assertNotNull(timeline.unschedule(new DateTime(13)));
		bound = new DateTime(1);
		Assert.assertNull(timeline.earliestSchedulableLegalAfter(bound,
				legalTimeline, schedulable));
		
	}
	
	@Test
	public void testSchedule() {
		
		timeline = new Timeline(new Interval(1, 10));
		
		// out of range
		Assert.assertFalse(timeline.schedule(new DateTime(8), new Activity(
				new Duration(11))));
		Assert.assertFalse(timeline.schedule(new DateTime(0), new Activity(
				new Duration(0))));
		
		// legal 1-1, 5-8, 8-10, 10-10
		Assert.assertTrue(timeline.schedule(new DateTime(1), new Activity(
				new Duration(0))));
		Assert.assertTrue(timeline.schedule(new DateTime(8), new Activity(
				new Duration(2))));
		Assert.assertTrue(timeline.schedule(new DateTime(5), new Activity(
				new Duration(3))));
		Assert.assertTrue(timeline.schedule(new DateTime(10), new Activity(
				new Duration(0))));
		
		// overlap
		Assert.assertFalse(timeline.schedule(new DateTime(1), new Activity(
				new Duration(2))));
		Assert.assertFalse(timeline.schedule(new DateTime(3), new Activity(
				new Duration(6))));
		Assert.assertFalse(timeline.schedule(new DateTime(3), new Activity(
				new Duration(3))));
		Assert.assertFalse(timeline.schedule(new DateTime(6), new Activity(
				new Duration(3))));
		
		// fill the timeline
		Assert.assertTrue(timeline.schedule(new DateTime(3), new Activity(
				new Duration(2))));
		
	}
	
	@Test
	public void testUnschedule() {
		timeline = new Timeline(new Interval(1, 10));
		Activity activity = new Activity(new Duration(0));
		Assert.assertTrue(timeline.schedule(new DateTime(1), activity));
		Activity activity2 = new Activity(new Duration(3));
		Assert.assertTrue(timeline.schedule(new DateTime(5), activity2));
		Assert.assertEquals(new DateTime(1), timeline.schedule.firstKey());
		Assert.assertEquals(activity,
				(Activity) timeline.unschedule(new DateTime(1)));
		Assert.assertEquals(activity2, (Activity) timeline.schedule.lastEntry()
				.getValue());
		Assert.assertEquals(activity2,
				(Activity) timeline.unschedule(new DateTime(5)));
		Assert.assertTrue(timeline.schedule.isEmpty());
		
	}
	
	@Test
	public void testHasScheduleStart() {
		timeline = new Timeline(new Interval(1, 10));
		Activity activity = new Activity(new Duration(0));
		Assert.assertTrue(timeline.schedule(new DateTime(1), activity));
		Assert.assertTrue(timeline.hasScheduleStart(new DateTime(1)));
	}
	
	@Test
	public void testLastEndTime() {
		timeline = new Timeline(new Interval(1, 10));
		Assert.assertEquals(new DateTime(1), timeline.lastEndTime());
		Activity activity = new Activity(new Duration(5));
		Assert.assertTrue(timeline.schedule(new DateTime(1), activity));
		Assert.assertEquals(new DateTime(6), timeline.lastEndTime());
		
	}
	
	@Test
	public void testGetNumActivities() {
		timeline = new Timeline(new Interval(1, 10));
		Assert.assertTrue(timeline.isEmpty());
		Activity activity = new Activity(new Duration(0));
		Assert.assertTrue(timeline.schedule(new DateTime(1), activity));
		Activity activity2 = new Activity(new Duration(3));
		Assert.assertTrue(timeline.schedule(new DateTime(5), activity2));
		Assert.assertEquals(2, timeline.getNumScheduled());
		Assert.assertFalse(timeline.isEmpty());
		Assert.assertEquals(activity,
				(Activity) timeline.unschedule(new DateTime(1)));
		
		Assert.assertEquals(activity2,
				(Activity) timeline.unschedule(new DateTime(5)));
		Assert.assertTrue(timeline.isEmpty());
	}
	
}
