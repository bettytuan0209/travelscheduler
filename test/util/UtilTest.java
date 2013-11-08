package util;

import java.util.TreeMap;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.junit.Assert;
import org.junit.Test;

import schedulable.LegalTime;
import schedulable.Schedulable;

public class UtilTest {
	
	@Test
	public void testGetEndTime1() {
		TreeMap<DateTime, Schedulable> map = new TreeMap<DateTime, Schedulable>();
		map.put(new DateTime(1), new LegalTime(new Duration(2), true));
		Assert.assertEquals(3, Util.getEndTime(map.lastEntry()).getMillis());
		map.put(new DateTime(5), new LegalTime(new Duration(0), true));
		Assert.assertEquals(5, Util.getEndTime(map.lastEntry()).getMillis());
		
	}
	
	@Test
	public void testGetEndTime2() {
		Assert.assertEquals(
				3,
				Util.getEndTime(new DateTime(1),
						new LegalTime(new Duration(2), true)).getMillis());
		Assert.assertEquals(
				5,
				Util.getEndTime(new DateTime(5),
						new LegalTime(new Duration(0), true)).getMillis());
		
	}
	
}
