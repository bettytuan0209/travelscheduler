package schedulable;

import java.io.Serializable;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.Duration;

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
	
	/**
	 * Default constructor
	 * 
	 * @param duration
	 *            The duration of this transportation.
	 */
	public Transportation(Duration duration) {
		this.duration = duration;
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
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Transportation) {
			Transportation other = (Transportation) obj;
			if (duration.equals(other.duration)) {
				return true;
			}
			
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(duration).toHashCode();
	}
	
}
