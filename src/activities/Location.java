package activities;

import java.io.Serializable;

import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Represents a geographical location in terms of latitude and longitude
 * 
 * @author chiao-yutuan
 * 
 */
public class Location implements Serializable {
	private static final long serialVersionUID = -7278148360349733343L;
	private float latitude;
	private float longitude;
	
	/**
	 * Constructor with all fields.
	 * 
	 * @param latitude
	 *            Latitude of the location
	 * @param longitude
	 *            Longitude of the location
	 */
	public Location(float latitude, float longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	/**
	 * Overrides the object equals() method. Checks all fields to see if
	 * equals(). Consistent with hashCode()
	 */
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
	
	/**
	 * Overrides the object hashCode() method. Creates a hash using all fields
	 * in the class. Consistent with equals()
	 */
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(latitude).append(longitude)
				.toHashCode();
	}
	
	/***************************** Getters *******************************/
	
	public float getLatitude() {
		return latitude;
	}
	
	public float getLongitude() {
		return longitude;
	}
	
}
