package activities;

import java.util.ArrayList;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.MutableInterval;
import org.junit.Assert;
import org.junit.Test;

public class ActivityTest {
	Activity activity;

	@Test
	public void mergeIntervalsTest() {
		activity = new Activity(new Duration(1));

		MutableInterval a = new MutableInterval(1, 5);
		MutableInterval b = new MutableInterval(5, 10);
		Assert.assertEquals(new MutableInterval(1, 10),
				activity.mergeIntervals(a, b));
		Assert.assertEquals(new MutableInterval(1, 10),
				activity.mergeIntervals(b, a));

		a.setEndMillis(8);
		Assert.assertEquals(new MutableInterval(1, 10),
				activity.mergeIntervals(a, b));
		b.setEndMillis(7);
		Assert.assertEquals(new MutableInterval(1, 8),
				activity.mergeIntervals(a, b));
	}

	@Test
	public void addLegalTimeTest() {
		activity = new Activity(new Duration(1));

		MutableInterval a = new MutableInterval(1, 2);
		activity.addLegalTime(a);
		Assert.assertEquals(a, activity.legalTimes.get(a.getStart()));

		MutableInterval b = new MutableInterval(4, 5);
		activity.addLegalTime(b);
		Assert.assertEquals(a, activity.legalTimes.get(a.getStart()));
		Assert.assertEquals(b, activity.legalTimes.get(b.getStart()));

		MutableInterval c = new MutableInterval(2, 3);
		activity.addLegalTime(c);
		Assert.assertEquals(2, activity.legalTimes.size());
		Assert.assertEquals(3, activity.legalTimes.get(a.getStart())
				.getEndMillis());

		MutableInterval d = new MutableInterval(2, 4);
		activity.addLegalTime(d);
		Assert.assertEquals(1, activity.legalTimes.size());
		Assert.assertEquals(1, activity.legalTimes.get(a.getStart())
				.getStartMillis());
		Assert.assertEquals(5, activity.legalTimes.get(a.getStart())
				.getEndMillis());
	}

	@Test
	public void shouldMergeTest() {
		activity = new Activity(new Duration(1));

		MutableInterval a = new MutableInterval(1, 5);
		MutableInterval b = new MutableInterval(5, 10);
		Assert.assertTrue(activity.shouldMerge(a, b));
		Assert.assertTrue(activity.shouldMerge(b, a));
		b = new MutableInterval(2, 5);
		Assert.assertTrue(activity.shouldMerge(a, b));
		b = new MutableInterval(2, 8);
		Assert.assertTrue(activity.shouldMerge(a, b));
		b = new MutableInterval(0, 3);
		Assert.assertTrue(activity.shouldMerge(a, b));
		b = new MutableInterval(2, 3);
		Assert.assertTrue(activity.shouldMerge(a, b));
		b = new MutableInterval(0, 8);
		Assert.assertTrue(activity.shouldMerge(a, b));
		b = new MutableInterval(6, 10);
		Assert.assertFalse(activity.shouldMerge(a, b));

	}

	@Test
	public void setEarlistStartTimeTest() {
		activity = new Activity(new Duration(2));
		MutableInterval a = new MutableInterval(1, 4);
		activity.addLegalTime(a);
		activity.addLegalTime(new MutableInterval(8, 10));

		activity.setEarlistStartTime(new DateTime(2));
		Assert.assertEquals(2, activity.legalTimes.size());
		Assert.assertEquals(2, activity.legalTimes.get(new DateTime(2))
				.getStartMillis());
		Assert.assertEquals(4, activity.legalTimes.get(new DateTime(2))
				.getEndMillis());

		activity.setEarlistStartTime(new DateTime(4));
		Assert.assertEquals(1, activity.legalTimes.size());
		Assert.assertEquals(8, new ArrayList<MutableInterval>(
				activity.legalTimes.values()).get(0).getStartMillis());

	}

	@Test
	public void enoughLegalTimesTest() {
		activity = new Activity(new Duration(2));
		activity.addLegalTime(new MutableInterval(1, 2));
		Assert.assertFalse(activity.enoughLegalTimes());

		activity.addLegalTime(new MutableInterval(2, 3));
		Assert.assertTrue(activity.enoughLegalTimes());

		activity.addLegalTime(new MutableInterval(3, 4));
		Assert.assertTrue(activity.enoughLegalTimes());
	}

}
