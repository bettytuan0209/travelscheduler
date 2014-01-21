package schedulable;

import java.io.Serializable;

import org.joda.time.Duration;

import activities.Location;

/**
 * The transportation class is a schedulable so that it can be scheduled on a
 * Timeline. It only has a duration field. In a graph, it is the edge between
 * two locations
 * 
 * @author chiao-yutuan
 * 
 */
public class Transportation extends Schedulable implements Serializable,
		Comparable<Transportation> {
	
	private static final long serialVersionUID = -2891097162573349168L;
	private Location start;
	private Location end;
	
	/**
	 * Default constructor. For an undirected graph, the start and end location
	 * can be reversed
	 * 
	 * @param duration
	 *            The duration of this transportation.
	 * @param start
	 *            The start location
	 * @param end
	 *            The end location.
	 */
	public Transportation(Duration duration, Location start, Location end) {
		this.duration = duration;
		this.start = start;
		this.end = end;
	}
	
	/**
	 * Implements compareTo() for the interface Comparable so that the
	 * transportation with the shorter duration is placed before those with
	 * longer duration
	 * 
	 * @param other
	 *            The other transportation to compare with
	 * 
	 * @return A negative integer, zero, or a positive integer as this object is
	 *         less than, equal to, or greater than the specified object.
	 */
	@Override
	public int compareTo(Transportation other) {
		long result = this.duration.getMillis() - other.duration.getMillis();
		if (result < 0) {
			return -1;
		} else if (result > 0) {
			return 1;
		} else {
			return 0;
		}
	}
	
	public Location getStart() {
		return start;
	}
	
	public Location getEnd() {
		return end;
	}
}
