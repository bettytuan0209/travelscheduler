package schedulable;

import java.io.Serializable;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.Duration;

/**
 * A Schedulable that represents a time segment legal for some activity to
 * schedule. It consists of a boolean field called available that indicates
 * whether this LegalTime segment is free or blacked out
 * 
 * @author chiao-yutuan
 * 
 */
public class LegalTime extends Schedulable implements Serializable {
	
	private static final long serialVersionUID = 8702078358315508289L;
	public boolean available;
	
	/**
	 * Constructor that takes the duration of this LegalTime and defaults to
	 * available = true
	 * 
	 * @param duration
	 *            The duration of this segment
	 */
	public LegalTime(Duration duration) {
		this.duration = duration;
		this.available = true;
	}
	
	/**
	 * Constructor that takes all fields
	 * 
	 * @param duration
	 *            The duration of this LegalTime segment
	 * @param available
	 *            Whether this is available
	 */
	public LegalTime(Duration duration, boolean available) {
		this.duration = duration;
		this.available = available;
	}
	
	/**
	 * Overrides the object equals() method. Checks all fields to see if
	 * equals(). Consistent with hashCode()
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof LegalTime) {
			LegalTime other = (LegalTime) obj;
			if (duration.equals(other.duration) && available == other.available) {
				return true;
			}
			
		}
		return false;
	}
	
	/**
	 * Overrides the object hashCode() method. Creates a hash using all fields
	 * in the class. Consistent with equals()
	 */
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(duration).append(available)
				.toHashCode();
		
	}
}
