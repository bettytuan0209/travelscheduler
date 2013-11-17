package schedulable;

import java.io.Serializable;

import org.joda.time.Duration;

public class Transportation extends Schedulable implements Serializable {
	
	private static final long serialVersionUID = -2891097162573349168L;
	
	// private Location startLocation;
	// private Location endLocation;
	
	public Transportation(Duration duration) {
		this.duration = duration;
	}
	
	// public Location getStartLocation() {
	// return startLocation;
	// }
	//
	// public Location getEndLocation() {
	// return endLocation;
	// }
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Transportation) {
			Transportation other = (Transportation) obj;
			if (duration.equals(other.duration)
			// && startLocation.equals(other.startLocation)
			// && endLocation.equals(other.endLocation)
			) {
				return true;
			}
			
		}
		return false;
	}
	
}
