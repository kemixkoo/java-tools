/**
 *  
 */
package xyz.kemix.maven.plugin.java.compiler.reporter;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.jar.JarFile;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import xyz.kemix.java.CompilerVersion;
import xyz.kemix.java.io.FileExts;
import xyz.kemix.java.json.JSONSortedArray;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 *         Created at 2017-10-29
 *
 */
public class GeneralClassReporter extends BaseClassReporter {

	public GeneralClassReporter(CompilerVersion baseJDKVersion, boolean compatibleJDKVersion, int maxClasses,
			boolean innerJar) {
		super(baseJDKVersion, compatibleJDKVersion, maxClasses, innerJar);
	}

	public JSONArray processClass(File classFile) throws IOException {
		return processClasses(classFile, new File[] { classFile });
	}

	public JSONArray processClasses(File[] classesFiles) throws IOException {
		return processClasses(null, classesFiles);
	}

	public JSONArray processClasses(File baseFile, File[] classesFiles) throws IOException {
		if (classesFiles == null || classesFiles.length == 0) {
			throw new IOException("Must provide the class files");
		}
		JSONSortedArray result = new JSONSortedArray();

		Map<String, JSONObject> pathMap = new LinkedHashMap<>();

		for (File classFile : classesFiles) {
			JSONObject detailsJson = new JSONObject();
			if (classFile == null) {
				throw new IOException("Must provide the class file");
			}
			if (!classFile.exists()) {
				throw new FileNotFoundException(classFile.getAbsolutePath());
			}
			if (!classFile.isFile()) {
				throw new IOException("Must be file, not folder: " + classFile.getAbsolutePath());
			}
			if (!FileExts.CLASS.of(classFile)) {
				throw new IOException("invalid file: " + classFile.getAbsolutePath());
			}
			if (classFile.getName().contains(CHAR_INNER_CLASS)) {
				continue; // inner class ignore
			}
			processClass(detailsJson, baseFile, classFile);

			if (detailsJson.length() > 0) {
				String folderPath = getClassFilePath(baseFile, classFile); // relative path
				JSONObject exitedDetailsJson = pathMap.get(folderPath);
				if (exitedDetailsJson == null) { // not found, save current one
					pathMap.put(folderPath, detailsJson);
				} else {
					for (String key : detailsJson.keySet()) {
						if (exitedDetailsJson.has(key)) { // exited same version, merge classes
							final JSONObject jsonObject = exitedDetailsJson.getJSONObject(key);
							final JSONObject curJson = detailsJson.getJSONObject(key);
							final JSONArray curArray = curJson.getJSONArray(KEY_CLASSES);
							if (jsonObject.has(KEY_CLASSES)) { // existed, merge
								final JSONArray jsonArray = jsonObject.getJSONArray(KEY_CLASSES);
								mergeArray(curArray, jsonArray);
							} else { // save current classes
								jsonObject.put(KEY_CLASSES, curArray);
							}
						} else {
							exitedDetailsJson.put(key, detailsJson.get(key));
						}
					}
				}
			}
		}

		for (Map.Entry<String, JSONObject> entry : pathMap.entrySet()) {
			JSONObject bundleJson = createBundleJson(null, entry.getKey(), entry.getValue());
			result.put(bundleJson);
		}
		result.sort();
		return result;
	}

	String getClassFilePath(File baseFile, File file) {
		if (baseFile != null && (baseFile.equals(file) || baseFile.equals(file.getParentFile()))) {
			return ".";
		}
		return getRelativeFilePath(baseFile, file.getParentFile());
	}

	public JSONArray processJar(File classFile) throws IOException {
		return processJars(new File[] { classFile });
	}

	public JSONArray processJars(File[] classesFiles) throws IOException {
		return processJars(null, classesFiles);
	}

	public JSONArray processJars(File baseFile, File[] jarFiles) throws IOException {
		if (jarFiles == null || jarFiles.length == 0) {
			return new JSONArray();
		}
		JSONSortedArray result = new JSONSortedArray();
		Arrays.sort(jarFiles);
		for (File jar : jarFiles) {
			if (jar == null) {
				throw new IOException("Must provide the jar file");
			}
			if (!jar.exists()) {
				throw new FileNotFoundException(jar.getAbsolutePath());
			}
			if (!jar.isFile()) {
				throw new IOException("Must be file, not folder: " + jar.getAbsolutePath());
			}
			if (!FileExts.JAR.of(jar)) {
				throw new IOException("invalid file: " + jar.getAbsolutePath());
			}
			JSONObject jarJson = processJarBundle(baseFile, jar);
			if (jarJson != null) {
				result.put(jarJson);
			}
		}
		result.sort();
		return result;
	}

	public JSONArray processFolder(File folder) throws IOException {
		return processFolder(folder, true);
	}

	public JSONArray processFolder(File folder, boolean recursive) throws IOException {
		if (folder == null) {
			throw new IOException("Must provide the class files");
		}
		if (!folder.exists()) {
			throw new FileNotFoundException(folder.getAbsolutePath());
		}
		if (!folder.isDirectory()) {
			throw new IOException("Must be folder, not file: " + folder.getAbsolutePath());
		}
		return processFolder(folder, folder, 0, recursive);
	}

	@SuppressWarnings("rawtypes")
	JSONArray processFolder(File baseFolder, File folder, int level, boolean recursive) throws IOException {
		JSONSortedArray result = new JSONSortedArray();
		// folder bundle
		File manifestFile = new File(folder, JarFile.MANIFEST_NAME);
		boolean isFolderBundle = manifestFile.exists();

		if (isFolderBundle) {
			final JSONObject folderBundleResult = processFolderBundle(baseFolder, folder);
			if (folderBundleResult != null) {
				result.put(folderBundleResult);
			}
		} else {// only deal current files in one level
			// jar
			final Collection jars = FileUtils.listFiles(folder, new String[] { FileExts.JAR.n() }, false);
			if (!jars.isEmpty()) {
				final JSONArray jarBundlesResult = processJars(baseFolder,
						FileUtils.convertFileCollectionToFileArray(jars));
				mergeArray(jarBundlesResult, result);
			}
			// classes
			final Collection classes = FileUtils.listFiles(folder, new String[] { FileExts.CLASS.n() }, false);
			if (!classes.isEmpty()) {
				final JSONArray jarClassesResult = processClasses(baseFolder,
						FileUtils.convertFileCollectionToFileArray(classes));
				mergeArray(jarClassesResult, result);
			}
			if (recursive) {
				// sub-folders
				File[] folders = folder.listFiles(new FileFilter() {

					@Override
					public boolean accept(File f) {
						return f.isDirectory();
					}
				});
				for (File sub : folders) {
					final JSONArray subResult = processFolder(baseFolder, sub, ++level, recursive);
					// TODO
					mergeArray(subResult, result);
				}
			}
		}
		result.sort(new Comparator() {

			@Override
			public int compare(Object o1, Object o2) {
				if (o1 instanceof JSONObject && o2 instanceof JSONObject) {
					JSONObject json1 = (JSONObject) o1;
					JSONObject json2 = (JSONObject) o2;

					if (json1.has(KEY_BUNDLE_NAME) && json2.has(KEY_BUNDLE_NAME)) {
						int compare = json1.getString(KEY_BUNDLE_NAME)
								.compareToIgnoreCase(json2.getString(KEY_BUNDLE_NAME));
						if (compare != 0) {
							return compare;
						}
					}
					if (json1.has(KEY_FILE_PATH) && json2.has(KEY_FILE_PATH)) {
						int compare = json1.getString(KEY_FILE_PATH)
								.compareToIgnoreCase(json2.getString(KEY_FILE_PATH));
						if (compare != 0) {
							return compare;
						}
					}
				}
				return o1.toString().compareToIgnoreCase(o1.toString());
			}
		});
		return result;
	}
}
