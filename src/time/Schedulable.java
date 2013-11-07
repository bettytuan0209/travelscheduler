package time;

import java.io.Serializable;

import org.joda.time.Duration;

public class Schedulable implements Serializable {
	private static final long serialVersionUID = -4960273957576761595L;
	protected Duration duration;
	
	protected Schedulable() {
		
	}
	
	public Schedulable(Duration duration) {
		this.duration = duration;
	}
	
	public Duration getDuration() {
		return duration;
	}
	
}
