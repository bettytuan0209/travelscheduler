package activities;

import java.io.Serializable;

public class Location implements Serializable {
	private static final long serialVersionUID = -7278148360349733343L;
	private float latitude;
	private float longitude;
	
	public Location(float latitude, float longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	public float getLatitude() {
		return latitude;
	}
	
	public float getLongitude() {
		return longitude;
	}
	
}
