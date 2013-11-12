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

public class LegalTimelineTest {
	LegalTimeline legalTimeline;
	
	@Test
	public void testLegalTimelineInterval() {
		legalTimeline = new LegalTimeline(new Interval(1, 20));
		Assert.assertEquals(new Interval(1, 21), legalTimeline.getInterval());
		Assert.assertNotNull(legalTimeline.getSchedule());
		Assert.assertTrue(legalTimeline.getSchedule().isEmpty());
		
		LegalTimeline copy = (LegalTimeline) DeepCopy.copy(legalTimeline);
		Assert.assertEquals(copy, legalTimeline);
		Assert.assertFalse(copy == legalTimeline);
	}
	
	@Test
	public void testLegalTimelineTreeMapOfDateTimeSchedulable() {
		TreeMap<DateTime, Schedulable> schedule = new TreeMap<DateTime, Schedulable>();
		legalTimeline = new LegalTimeline(schedule);
		Assert.assertTrue(legalTimeline.getInterval()
				.equals(new Interval(0, 0)));
		Assert.assertTrue(legalTimeline.schedule.isEmpty());
		
		// valid entry
		schedule.put(new DateTime(2), new LegalTime(new Duration(2), true));
		schedule.put(new DateTime(5), new LegalTime(new Duration(0), true));
		Assert.assertTrue(legalTimeline.schedule.isEmpty()); // check no
																// reference
		
		legalTimeline = new LegalTimeline(schedule);
		Assert.assertTrue(legalTimeline.getInterval()
				.equals(new Interval(2, 6))); // check no reference
		Assert.assertEquals(schedule, legalTimeline.schedule);
		Assert.assertFalse(schedule == legalTimeline.schedule);
		
	}
	
	@Test
	public void testLegalTimelineIntervalTreeMapOfDateTimeSchedulable() {
		TreeMap<DateTime, Schedulable> schedule = new TreeMap<DateTime, Schedulable>();
		schedule.put(new DateTime(2), new LegalTime(new Duration(2), true));
		schedule.put(new DateTime(5), new LegalTime(new Duration(0), false));
		legalTimeline = new LegalTimeline(new Interval(1, 20), schedule);
		Assert.assertEquals(new Interval(1, 21), legalTimeline.getInterval());
		Assert.assertEquals(schedule, legalTimeline.getSchedule());
	}
	
	@Test(expected = ClassCastException.class)
	public void testTimelineConstructorNotLegalTime() {
		TreeMap<DateTime, Schedulable> schedule = new TreeMap<DateTime, Schedulable>();
		schedule.put(new DateTime(2), new LegalTime(new Duration(2), true));
		schedule.put(new DateTime(5), new Activity(new Duration(0)));
		
		legalTimeline = new LegalTimeline(schedule);
		
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testTimelineConstructorOverlap() {
		TreeMap<DateTime, Schedulable> schedule = new TreeMap<DateTime, Schedulable>();
		schedule.put(new DateTime(2), new LegalTime(new Duration(2), true));
		schedule.put(new DateTime(3), new LegalTime(new Duration(0), true));
		
		legalTimeline = new LegalTimeline(schedule);
		
	}
	
	@Test
	public void testSchedule() {
		
		legalTimeline = new LegalTimeline(new Interval(1, 20));
		Assert.assertTrue(legalTimeline.schedule(new DateTime(2),
				new LegalTime(new Duration(2), true)));
		Assert.assertFalse(legalTimeline.schedule(new DateTime(2),
				new LegalTime(new Duration(0), true)));
		Assert.assertFalse(legalTimeline.schedule(new DateTime(25),
				new LegalTime(new Duration(2), true)));
		
	}
	
	@Test
	public void testSetEarliestAvailable() {
		legalTimeline = new LegalTimeline(new Interval(1, 20));
		
		// 2 - 4, 6 - 10
		Assert.assertTrue(legalTimeline.schedule(new DateTime(2),
				new LegalTime(new Duration(2), true)));
		Assert.assertTrue(legalTimeline.schedule(new DateTime(6),
				new LegalTime(new Duration(4), true)));
		
		// 2 - 4 (false), 6 - 10 (true)
		Assert.assertTrue(legalTimeline.setEarliestAvailable(new DateTime(4)));
		Assert.assertEquals(2, legalTimeline.getNumScheduled());
		Assert.assertEquals(new LegalTime(new Duration(2), false),
				(LegalTime) legalTimeline.getSchedule().get(new DateTime(2)));
		Assert.assertEquals(new LegalTime(new Duration(4), true),
				(LegalTime) legalTimeline.getSchedule().get(new DateTime(6)));
		
		// 2 - 4 (false), 6 - 7 (false), 8 - 10 (true)
		Assert.assertTrue(legalTimeline.setEarliestAvailable(new DateTime(8)));
		Assert.assertEquals(3, legalTimeline.getNumScheduled());
		Assert.assertEquals(new LegalTime(new Duration(2), false),
				(LegalTime) legalTimeline.getSchedule().get(new DateTime(2)));
		Assert.assertEquals(new LegalTime(new Duration(2), false),
				(LegalTime) legalTimeline.getSchedule().get(new DateTime(6)));
		Assert.assertEquals(new LegalTime(new Duration(2), true),
				(LegalTime) legalTimeline.getSchedule().get(new DateTime(8)));
		
	}
	
	@Test
	public void testEnoughLegalTimes() {
		legalTimeline = new LegalTimeline(new Interval(1, 20));
		Duration duration = new Duration(2);
		
		Assert.assertFalse(legalTimeline.enoughLegalTimes(duration));
		
		// enough
		Assert.assertTrue(legalTimeline.schedule(new DateTime(1),
				new LegalTime(new Duration(3), true)));
		Assert.assertTrue(legalTimeline.enoughLegalTimes(duration));
		
		// setEarliestAvailable
		Assert.assertTrue(legalTimeline.setEarliestAvailable(new DateTime(3)));
		Assert.assertFalse(legalTimeline.enoughLegalTimes(duration));
		
		// add another
		Assert.assertTrue(legalTimeline.schedule(new DateTime(5),
				new LegalTime(new Duration(3), true)));
		Assert.assertTrue(legalTimeline.enoughLegalTimes(duration));
		
	}
	
}
