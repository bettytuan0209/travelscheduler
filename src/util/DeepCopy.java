package util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Util class that makes a deep copy of any object using serialization
 * 
 * @author chiao-yutuan
 * 
 */
public class DeepCopy {
	
	/**
	 * Makes a deep copy of the object by serializing the object and unserialize
	 * it back
	 * 
	 * @param orig
	 *            Original object
	 * @return copied object
	 */
	public static Object copy(Object orig) {
		
		Object obj = null;
		try {
			// Write the object out to a byte array
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bos);
			out.writeObject(orig);
			out.flush();
			out.close();
			// Make an input stream from the byte array and read
			// a copy of the object back in.
			ObjectInputStream in = new ObjectInputStream(
					new ByteArrayInputStream(bos.toByteArray()));
			obj = in.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException cnfe) {
			cnfe.printStackTrace();
		}
		return obj;
		
	}
	
}
