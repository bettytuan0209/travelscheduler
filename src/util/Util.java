package util;

import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import schedulable.Schedulable;
import schedulable.Transportation;
import activities.Location;

public class Util {
	
	public static DateTime getEndTime(Map.Entry<DateTime, Schedulable> entry) {
		return entry.getKey().plus(entry.getValue().getDuration());
	}
	
	public static DateTime getEndTime(DateTime start, Schedulable schedulable) {
		return start.plus(schedulable.getDuration());
	}
	
	// TO DO: hard coded
	public static Transportation searchTransportation(Location a, Location b) {
		long duration = 0;
		Location toSearch;
		
		if (a.equals(new Location(0, 0))) {
			
			toSearch = b;
		} else if (b.equals(new Location(0, 0))) {
			toSearch = a;
		} else {
			throw new IllegalArgumentException("cannot find tranportation");
		}
		
		if (toSearch.equals(new Location(1, 1))) { // museum
			duration = 4;
		} else if (toSearch.equals(new Location(2, 2))) { // concert
			duration = 5;
		} else if (toSearch.equals(new Location(3, 3))) { // park
			duration = 3;
		} else {
			throw new IllegalArgumentException("cannot find tranportation");
		}
		
		return new Transportation(new Duration(duration));
	}
}
