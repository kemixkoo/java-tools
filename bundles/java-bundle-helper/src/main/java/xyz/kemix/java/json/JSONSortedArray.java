/**
 * 
 */
package xyz.kemix.java.json;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author ggu
 *
 */
public class JSONSortedArray extends JSONArray {

	public JSONSortedArray() {
		super();
	}

	public JSONSortedArray(Collection<?> collection) {
		super(collection);
	}

	public void sort() {
		sort(null);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void sort(Comparator comparator) {
		try {
			Field listField = JSONArray.class.getDeclaredField("myArrayList");//$NON-NLS-1$
			listField.setAccessible(true);
			List list = (List) listField.get(this);
			list.sort(comparator);
			for (Object obj : list) {
				if (obj instanceof JSONSortedArray) {
					((JSONSortedArray) obj).sort(comparator);
				} else if (obj instanceof JSONObject) {
					final JSONObject jsonObject = (JSONObject) obj;
					for (String key : jsonObject.keySet()) {
						final Object object = jsonObject.get(key);
						if (object instanceof JSONSortedArray) {
							((JSONSortedArray) object).sort(comparator);
						}
					}
				}
			}
		} catch (Exception e) {
			// e.printStackTrace();
		}
	}

}
