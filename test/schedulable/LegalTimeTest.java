package schedulable;

import org.joda.time.Duration;
import org.junit.Assert;
import org.junit.Test;

import util.DeepCopy;

public class LegalTimeTest {
	LegalTime legalTime;
	
	@Test
	public void testLegalTime() {
		legalTime = new LegalTime(new Duration(2), true);
		Assert.assertEquals(new Duration(2), legalTime.getDuration());
		Assert.assertTrue(legalTime.available);
		
		LegalTime copy = (LegalTime) DeepCopy.copy(legalTime);
		Assert.assertEquals(copy, legalTime);
		Assert.assertFalse(copy == legalTime);
		
		legalTime.available = false;
		Assert.assertFalse(legalTime.available);
		Assert.assertNotEquals(copy, legalTime);
	}
	
}
