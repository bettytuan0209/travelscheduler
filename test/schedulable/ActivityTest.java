package schedulable;

import java.util.TreeMap;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.junit.Assert;
import org.junit.Test;

public class ActivityTest {
	Activity activity;
	
	@Test
	public void setEarlistStartTimeTest() {
		TreeMap<DateTime, Schedulable> legalTimes = new TreeMap<DateTime, Schedulable>();
		
		// 1-4, 8-10
		legalTimes.put(new DateTime(1), new LegalTime(new Duration(3), true));
		legalTimes.put(new DateTime(8), new LegalTime(new Duration(2), true));
		
		activity = new Activity("", new Duration(2), legalTimes);
		
		activity.getLegalTimeline().setEarliestAvailable(new DateTime(2));
		Assert.assertEquals(2, activity.getLegalTimeline().getSchedule().size());
		Assert.assertTrue(activity.getLegalTimeline().hasScheduleStart(
				new DateTime(1)));
		Assert.assertTrue(activity.getLegalTimeline().hasScheduleStart(
				new DateTime(8)));
		
		activity.getLegalTimeline().setEarliestAvailable(new DateTime(4));
		Assert.assertEquals(1, activity.getLegalTimeline().getSchedule().size());
		Assert.assertEquals(8, activity.getLegalTimeline().getSchedule()
				.firstKey());
		
	}
	
	@Test
	public void enoughLegalTimesTest() {
		activity = new Activity(new Duration(2));
		Assert.assertTrue(activity.addLegalTime(new Interval(1, 2)));
		Assert.assertFalse(activity.forwardChecking());
		
		Assert.assertTrue(activity.addLegalTime(new Interval(2, 3)));
		Assert.assertTrue(activity.forwardChecking());
		
		Assert.assertTrue(activity.addLegalTime(new Interval(3, 4)));
		Assert.assertTrue(activity.forwardChecking());
	}
	
}
