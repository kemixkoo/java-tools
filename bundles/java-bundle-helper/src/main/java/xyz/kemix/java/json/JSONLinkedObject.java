/**
 * 
 */
package xyz.kemix.java.json;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;

import org.json.JSONObject;

/**
 * @author ggu
 *
 */
public class JSONLinkedObject extends JSONObject {

	public JSONLinkedObject() {
		super();
		try {
			// change to use linked map
			Field mapField = JSONObject.class.getDeclaredField("map"); //$NON-NLS-1$
			mapField.setAccessible(true);
			mapField.set(this, new LinkedHashMap<String, Object>());
		} catch (Exception e) {
			// e.printStackTrace();
		}
	}

}
