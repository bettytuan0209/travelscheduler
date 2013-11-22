package activities;

import java.io.Serializable;

import org.apache.commons.lang3.builder.HashCodeBuilder;

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
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Location) {
			Location other = (Location) obj;
			if (latitude == other.latitude && longitude == other.longitude) {
				return true;
			}
			
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder()
		// two randomly chosen prime numbers
				.append(latitude).append(longitude).toHashCode();
	}
	
}
