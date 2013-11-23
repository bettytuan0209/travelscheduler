package schedulable;

import java.io.Serializable;

import org.joda.time.Duration;

/**
 * The transportation class is a schedulable so that it can be scheduled on a
 * Timeline. It only has a duration field. In a graph, it is the edge between
 * two locations
 * 
 * @author chiao-yutuan
 * 
 */
public class Transportation extends Schedulable implements Serializable {
	
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
	
}
