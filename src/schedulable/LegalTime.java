package schedulable;

import java.io.Serializable;

import org.joda.time.Duration;

public class LegalTime extends Schedulable implements Serializable {
	
	private static final long serialVersionUID = 8702078358315508289L;
	public boolean available;
	
	public LegalTime(Duration duration, boolean available) {
		this.duration = duration;
		this.available = available;
	}
	
	public LegalTime(Duration duration) {
		this.duration = duration;
		this.available = true;
	}
	
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
}
