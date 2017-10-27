/**
 * 
 */
package xyz.kemix.java.json;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.json.JSONArray;

/**
 * @author ggu
 *
 */
public class JSONSortedArray extends JSONArray {
	public static boolean console = false;
	public static int consoleRowLimit = -1;

	public JSONSortedArray() {
		super();
	}

	public JSONSortedArray(Collection<?> collection) {
		super(collection);
	}

	@Override
	public int length() {
		int length = super.length();
		if (console && consoleRowLimit > 1 && length > consoleRowLimit) {
			return consoleRowLimit;
		}
		return length;
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
		} catch (Exception e) {
			// e.printStackTrace();
		}
	}

}
