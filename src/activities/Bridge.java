package activities;

import java.io.Serializable;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import schedulable.Activity;
import schedulable.Transportation;

/**
 * This class is used by the cluster manager as a "richer" version of
 * transportation
 * 
 * @author chiao-yutuan
 * 
 */

public class Bridge implements Serializable {
	
	private static final long serialVersionUID = 7317728904166827226L;
	private Transportation edge;
	private Activity activity1;
	private Activity activity2;
	public boolean used;
	
	/**
	 * Default constructor with the transportation and two activities on each
	 * side. The bridge is automatically marked not used
	 * 
	 * @param edge
	 *            The transportation schedulable
	 * @param activity1
	 *            An activity
	 * @param activity2
	 *            The other activity
	 */
	public Bridge(Transportation edge, Activity activity1, Activity activity2) {
		this.edge = edge;
		this.activity1 = activity1;
		this.activity2 = activity2;
		this.used = false;
	}
	
	/**
	 * Get the duration in the schedulable as in milliseconds
	 * 
	 * @return Duration in milliseconds
	 * 
	 */
	public long getDurationMillis() {
		return edge.getDuration().getMillis();
	}
	
	public Transportation getEdge() {
		return edge;
	}
	
	public Activity getActivity1() {
		return activity1;
	}
	
	public Activity getActivity2() {
		return activity2;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Bridge) {
			Bridge other = (Bridge) obj;
			if (edge.getDuration().equals(other.edge.getDuration())
					&& activity1.equals(other.activity1)
					&& activity2.equals(other.activity2)) {
				return true;
			}
			
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(edge.getDuration())
				.append(activity1).append(activity2).toHashCode();
	}
	
	@Override
	public String toString() {
		return activity1.title + "-" + activity2.title + " "
				+ getDurationMillis();
	}
	
}
