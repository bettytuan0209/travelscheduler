import java.util.ArrayList;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;

public class Activity {
	Duration duration;
	ArrayList<Interval> legalTimes;
	Location location;
	DateTime scheduledTime;

	public Activity(Duration duration) {
		this.duration = duration;
	}

	public void setLegalTimes() {

	}

}
