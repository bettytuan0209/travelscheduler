package schedulable;

import java.io.Serializable;

import org.joda.time.Duration;

public abstract class Schedulable implements Serializable {
	private static final long serialVersionUID = -4960273957576761595L;
	protected Duration duration;
	
	public Duration getDuration() {
		return duration;
	}
	
}
