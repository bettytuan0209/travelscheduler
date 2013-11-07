package schedulable;

import java.io.Serializable;

import activities.Location;

public class Transportation extends Schedulable implements Serializable {
	
	private static final long serialVersionUID = -2891097162573349168L;
	private Location startLocation;
	private Location endLocation;
	
	public Location getStartLocation() {
		return startLocation;
	}
	
	public Location getEndLocation() {
		return endLocation;
	}
	
}
