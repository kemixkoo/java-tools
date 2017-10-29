/**
 * 
 */
package xyz.kemix.maven.plugin.java.compiler.reporter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import xyz.kemix.java.ClassCompilerUtil;
import xyz.kemix.java.CompilerVersion;
import xyz.kemix.java.bundle.BundlesManager;
import xyz.kemix.java.bundle.ManifestUtil;
import xyz.kemix.java.io.FileExts;
import xyz.kemix.java.json.JSONLinkedObject;
import xyz.kemix.java.json.JSONSortedArray;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 *         Created at 2017-10-28
 *
 */
public class AbstractClassReporter {
	public static final String CHAR_INNER_CLASS = "$";

	public static final String KEY_FILE_PATH = "FilePath";
	public static final String KEY_BUNDLE_NAME = "BundleName";
	public static final String KEY_BUNDLE_VERSION = "BundleVersion";
	public static final String KEY_DETAILS = "Details";
	public static final String KEY_CLASSES = "Classes";
	public static final String KEY_CLASSES_SUM = "Sum";
	public static final String KEY_JAVA_VERSION = "JavaVersion";

	private final BundlesManager bundlesManager = new BundlesManager();
	private final CompilerVersion baseCompilerVersion;
	private final boolean compatibleCompilerVersion;
	private final int maxClasses;
	private final boolean innerJar;

	private int number;

	AbstractClassReporter(CompilerVersion baseJDKVersion, boolean compatibleJDKVersion, int maxClasses,
			boolean innerJar) {
		super();
		this.baseCompilerVersion = baseJDKVersion;
		this.compatibleCompilerVersion = compatibleJDKVersion;
		this.maxClasses = maxClasses;
		this.innerJar = innerJar;
	}

	private boolean isLimit() {
		if (maxClasses < 1) { // if <=0, no limit
			return false;
		}
		return number > maxClasses; // number <=max, no limit
	}

	JSONArray processFiles(File baseFile, File[] listFiles) throws IOException {
		JSONArray bundlesArrays = new JSONArray();
		if (listFiles == null) {
			return bundlesArrays;
		}

		for (File plugin : listFiles) {
			try {
				JSONObject bundleJson = null;
				if (plugin.isFile() && plugin.getName().endsWith(FileExts.JAR.ext())) {
					bundleJson = processJarBundle(baseFile, plugin);
				} else if (plugin.isDirectory()) {
					bundleJson = processFolderBundle(baseFile, plugin);
				}
				if (bundleJson != null && bundleJson.length() > 0) {
					bundlesArrays.put(bundleJson);
				}
			} catch (IOException e) {
				throw new IOException("Can't process the bundle:" + plugin.getName(), e);
			}
		}
		return bundlesArrays;
	}

	private String getRelativeFilePath(File baseFile, File bundleFile) {
		if (bundleFile == null) {
			return null;
		}
		if (baseFile != null) {
			return bundleFile.getAbsolutePath().replace(baseFile.getAbsolutePath(), "");
		}
		return bundleFile.getName();
	}

	boolean validJarBundle(ZipFile jarFile) {
		return true;
	}

	JSONObject processJarBundle(File baseFile, File bundleFile) throws IOException {
		if (bundleFile == null || !bundleFile.exists()) {
			return null;
		}

		if (isLimit()) {
			return null;
		}
		JSONObject detailsJson = new JSONObject();

		ZipFile jarFile = null;
		try {
			jarFile = new ZipFile(bundleFile);
			if (validJarBundle(jarFile)) { // not jar
				return null;
			}
			Enumeration<ZipArchiveEntry> entries = jarFile.getEntries();
			while (entries.hasMoreElements()) {
				ZipArchiveEntry entry = entries.nextElement();
				if (!entry.isDirectory()) {
					String path = entry.getName();
					if (FileExts.CLASS.of(path)) { // class
						String basename = FilenameUtils.getBaseName(path);
						if (basename.contains(CHAR_INNER_CLASS)) {
							continue;
						}
						if (isLimit()) {
							break;
						}
						number++;

						final InputStream classStream = jarFile.getInputStream(entry);

						final String classPath = FilenameUtils.removeExtension(path);
						final String message = "Can't process the class file:" + path + " for bundle:"
								+ bundleFile.getName();

						processClass(detailsJson, classStream, classPath, message);

					} else if (innerJar && path.endsWith(FileExts.JAR.ext())) {
						// TODO
						// JarInputStream innerJarStream = new JarInputStream(
						// jarFile.getInputStream(entry));
						// try {
						// JarEntry jarEntry = null;
						// while ((jarEntry = innerJarStream.getNextJarEntry())
						// != null) {
						// if(!jarEntry.isDirectory()){
						// String jarPath = jarEntry.getName();
						//
						// }
						// }
						// } finally {
						// IOUtils.closeQuietly(innerJarStream);
						// }
					}
				}
			}
		} finally {
			ZipFile.closeQuietly(jarFile);
		}

		if (detailsJson.length() == 0) { // no classes
			return null;
		}
		Manifest fileManifest = ManifestUtil.getJarManifest(bundleFile);
		// jar path
		String jarPath = getRelativeFilePath(baseFile, bundleFile);
		JSONObject bundleJson = createBundleJson(fileManifest, jarPath, detailsJson);

		return bundleJson;
	}

	boolean validFolderBundle(File bundleFile) {
		if (bundleFile == null || !bundleFile.exists()) {
			return false;
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	JSONObject processFolderBundle(File baseFile, File bundleFile) throws IOException {
		if (validFolderBundle(bundleFile)) {
			return null;
		}
		if (isLimit()) {
			return null;
		}
		JSONObject detailsJson = new JSONObject();

		Iterator<File> classesFiles = FileUtils.iterateFiles(bundleFile, new String[] { FileExts.CLASS.n() }, true);
		while (classesFiles.hasNext()) {
			File classFile = (File) classesFiles.next();
			if (classFile.getName().contains(CHAR_INNER_CLASS)) {
				continue; // ignore the inner classes
			}
			if (isLimit()) {
				break;
			}
			number++;

			processClass(detailsJson, classFile, bundleFile);
		}

		if (innerJar) { //
			Collection<File> jarsFiles = FileUtils.listFiles(bundleFile, new String[] { FileExts.JAR.n() }, true);
			JSONArray libInBundlesMap = processFiles(bundleFile, jarsFiles.toArray(new File[0]));

			for (int i = 0; i < libInBundlesMap.length(); i++) {
				JSONObject jarJson = libInBundlesMap.getJSONObject(i);
				String jarRelativePath = jarJson.getString(KEY_FILE_PATH);
				JSONObject jarDetailsJson = jarJson.getJSONObject(KEY_DETAILS);

				Iterator<String> jarCompilerVersions = jarDetailsJson.keys();
				while (jarCompilerVersions.hasNext()) {
					String jarcv = jarCompilerVersions.next();
					JSONObject cvJson = jarDetailsJson.getJSONObject(jarcv);
					if (!cvJson.has(KEY_CLASSES)) {
						continue;
					}
					JSONArray jarClassesArr = cvJson.getJSONArray(KEY_CLASSES);

					if (!detailsJson.has(jarcv)) {
						detailsJson.put(jarcv, new JSONLinkedObject());
					}
					JSONObject parentcvJson = detailsJson.getJSONObject(jarcv);

					if (!parentcvJson.has(KEY_CLASSES)) {
						parentcvJson.put(KEY_CLASSES, new JSONSortedArray());
					}
					JSONArray parentClassesArr = parentcvJson.getJSONArray(KEY_CLASSES);

					// append to existed with path
					for (int j = 0; j < jarClassesArr.length(); j++) {
						String classPathInJar = jarClassesArr.getString(j);
						parentClassesArr.put(jarRelativePath + '!' + classPathInJar);
					}

				}

			}
		}

		if (detailsJson.length() == 0) { // no classes
			return null;
		}

		Manifest fileManifest = ManifestUtil.getFileManifest(new File(bundleFile, JarFile.MANIFEST_NAME));
		// folder
		String folderPath = getRelativeFilePath(baseFile, bundleFile);
		JSONObject bundleJson = createBundleJson(fileManifest, folderPath, detailsJson);

		return bundleJson;
	}

	void processClass(JSONObject detailsJson, File bundleFile, File classFile) throws IOException {
		final FileInputStream fis = new FileInputStream(classFile);

		String classPath = null;
		String message = null;
		if (bundleFile != null) { // relate to bundle
			classPath = classFile.getAbsolutePath().replace(bundleFile.getAbsolutePath(), "");
			message = "Can't process the class file:" + classFile.getName() + " for bundle:" + bundleFile.getName();

		} else { // only file name
			classPath = classFile.getName();
			message = "Can't process the class file:" + classFile.getName();
			;
		}
		classPath = FilenameUtils.removeExtension(classPath);

		processClass(detailsJson, new FileInputStream(classFile), classPath, message);
	}

	private void processClass(JSONObject detailsJson, InputStream classStream, String classPath, String exceptionMessge)
			throws IOException {
		try {
			CompilerVersion cv = getInvalidCompilerVersion(classStream);
			if (cv != null) {
				String cvKey = cv.toString();
				if (!detailsJson.has(cvKey)) { // not existed
					detailsJson.put(cvKey, new JSONLinkedObject());
				}
				// must existed now
				JSONObject cvJson = detailsJson.getJSONObject(cvKey);

				if (!cvJson.has(KEY_CLASSES)) {
					cvJson.put(KEY_CLASSES, new JSONSortedArray());
				}
				// must existed now
				JSONArray classesArrays = cvJson.getJSONArray(KEY_CLASSES);

				classesArrays.put(classPath);
			}
		} catch (IOException e) {
			throw new IOException(exceptionMessge, e);
		} finally {
			IOUtils.closeQuietly(classStream);
		}
	}

	private JSONObject createBundleJson(Manifest manifest, String bundleFilePath, JSONObject detailsJson) {

		JSONObject bundleJson = new JSONLinkedObject();
		if (manifest != null) {
			String bundleSymbolicName = bundlesManager.getBundleSymbolicName(manifest);
			String bundleVersion = bundlesManager.getBundleVersion(manifest);
			bundleJson.put(KEY_BUNDLE_NAME, bundleSymbolicName);
			bundleJson.put(KEY_BUNDLE_VERSION, bundleVersion);
		}
		bundleJson.put(KEY_FILE_PATH, bundleFilePath);

		// calc the sum of classes
		JSONObject newDetailsJson = new JSONLinkedObject();
		Iterator<String> keys = detailsJson.keys();
		while (keys.hasNext()) {
			String cvKey = keys.next();
			if (detailsJson.has(cvKey)) {
				JSONObject cvJson = detailsJson.getJSONObject(cvKey);
				if (cvJson.has(KEY_CLASSES)) {
					JSONArray classesArr = cvJson.getJSONArray(KEY_CLASSES);
					if (classesArr.length() > 0) {
						if (classesArr instanceof JSONSortedArray) {
							((JSONSortedArray) classesArr).sort();
						}
						JSONObject newcvJson = new JSONLinkedObject();
						newDetailsJson.put(cvKey, newcvJson);

						newcvJson.put(KEY_CLASSES_SUM, classesArr.length());
						newcvJson.put(KEY_CLASSES, classesArr);
					}
				}
			}
		}

		bundleJson.put(KEY_DETAILS, newDetailsJson);

		return bundleJson;
	}

	private CompilerVersion getInvalidCompilerVersion(InputStream input) throws IOException {
		CompilerVersion compilerVersion = ClassCompilerUtil.getJavaVersion(input);
		if (compilerVersion != null && baseCompilerVersion != null && !compilerVersion.equals(baseCompilerVersion)) {
			int compareTo = compilerVersion.compareTo(baseCompilerVersion);
			if (compareTo > 0 // bigger than
					|| !compatibleCompilerVersion && compareTo < 0) {// less
				return compilerVersion; // the not match version
			}

		} // else { // ignore?
		return null; // valid
	}

}