package schedulable;

import java.io.Serializable;

import org.joda.time.Duration;

/**
 * An abstract class that represents all classes that can be scheduled on a
 * Timeline.
 * 
 * @author chiao-yutuan
 * 
 */
public abstract class Schedulable implements Serializable {
	private static final long serialVersionUID = -4960273957576761595L;
	
	protected Duration duration;
	
	public Duration getDuration() {
		return duration;
	}
	
}
