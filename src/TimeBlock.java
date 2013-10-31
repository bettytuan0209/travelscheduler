import java.util.ArrayList;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;

public class TimeBlock {
	int index;
	Interval whole;
	Location startLocation;
	Location endLocation;
	ActivitySpanningTree pairedAST;
	ArrayList<Activity> scheduledActivities; // map from begin time to
												// activity

	public TimeBlock() {
	}

	public boolean scheduleActivity(DateTime startTime, Activity activity) {
		// if timeframe available on this timeblock, insert
		int index;
		if ((index = timeSegmentFree(startTime, activity.duration)) >= 0) {
			scheduledActivities.add(index, activity);
			return true;
		}
		return false;
	}

	private int timeSegmentFree(DateTime startTime, Duration duration) {
		// try to find the index to insert this activity

		// if can't find one, return -1
		return -1;
	}

}