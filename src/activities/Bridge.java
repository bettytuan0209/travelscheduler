package activities;

import java.io.Serializable;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import schedulable.Activity;
import schedulable.Transportation;

public class Bridge implements Comparable<Bridge>, Serializable {
	
	private static final long serialVersionUID = 7317728904166827226L;
	private Transportation edge;
	private Activity activity1;
	private Activity activity2;
	
	public Bridge(Transportation edge, Activity activity1, Activity activity2) {
		this.edge = edge;
		this.activity1 = activity1;
		this.activity2 = activity2;
	}
	
	public long getDurationMillis() {
		return edge.getDuration().getMillis();
	}
	
	@Override
	public int compareTo(Bridge other) {
		long result = this.edge.getDuration().getMillis()
				- other.edge.getDuration().getMillis();
		if (result < 0) {
			return -1;
		} else if (result > 0) {
			return 1;
		} else {
			return 0;
		}
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
			if (edge.equals(other.edge) && activity1.equals(other.activity1)
					&& activity2.equals(other.activity2)) {
				return true;
			}
			
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(edge).append(activity1)
				.append(activity2).toHashCode();
	}
	
}
