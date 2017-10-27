package xyz.kemix.maven.plugin.java.compiler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.jar.JarFile;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.JavaVersion;
import org.json.JSONArray;
import org.json.JSONObject;

import xyz.kemix.java.bundle.BundlesManager;
import xyz.kemix.java.io.FileExts;
import xyz.kemix.java.io.ZipFileUtil;
import xyz.kemix.java.json.JSONLinkedObject;
import xyz.kemix.java.json.JSONSortedArray;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 *         Created at 2017-10-27
 *
 */
public class ClassReporterHelper {
	public static final String EXT_CLASS = FileExts.CLASS.ext();

	public static final String CHAR_INNER_CLASS = "$";

	public static final String KEY_FILE_PATH = "FilePath";
	public static final String KEY_BUNDLE_NAME = "BundleName";
	public static final String KEY_BUNDLE_VERSION = "BundleVersion";
	public static final String KEY_DETAILS = "Details";
	public static final String KEY_CLASSES = "Classes";
	public static final String KEY_CLASSES_SUM = "Sum";
	public static final String KEY_JAVA_VERSION = "JavaVersion";
	private final BundlesManager bundlesManager = new BundlesManager();
	private final JavaVersion baseCompilerVersion;
	private final boolean compatibleCompilerVersion;
	private final int maxClasses;
	private final boolean innerJar;

	private int number;

	public ClassReporterHelper(JavaVersion baseJDKVersion, boolean compatibleJDKVersion, int maxClasses,
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

	public JSONArray processFiles(File baseFile, File[] listFiles) throws IOException {
		JSONArray bundlesArrays = new JSONArray();
		if (listFiles == null) {
			return bundlesArrays;
		}

		for (File file : listFiles) {
			try {
				JSONObject bundleJson = null;
				if (file.isFile() && file.getName().endsWith(ZipFileUtil.EXT_JAR)) {
					bundleJson = processJarBundle(baseFile, file);
				} else if (file.isDirectory()) {
					bundleJson = processFolderBundle(baseFile, file);
				}
				if (bundleJson != null && bundleJson.length() > 0) {
					bundlesArrays.put(bundleJson);
				}
			} catch (IOException e) {
				throw new IOException("Can't process the bundle:" + file.getName(), e);
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

	private JSONObject processJarBundle(File baseFile, File bundleFile) throws IOException {
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
			ZipArchiveEntry manifestEntry = jarFile.getEntry(JarFile.MANIFEST_NAME);
			if (manifestEntry == null) { // not jar
				return null;
			}
			Enumeration<ZipArchiveEntry> entries = jarFile.getEntries();
			while (entries.hasMoreElements()) {
				ZipArchiveEntry entry = entries.nextElement();
				if (!entry.isDirectory()) {
					String path = entry.getName();
					if (path.endsWith(EXT_CLASS)) { // class
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

					} else if (innerJar && path.endsWith(ZipFileUtil.EXT_JAR)) {
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

		Map<String, String> bundleMap = bundlesManager.listBundlesFromJar(bundleFile);
		if (bundleMap.isEmpty()) {
			return null;
		}
		String bundleName = bundleMap.keySet().iterator().next();
		String bundleVersion = bundleMap.get(bundleName);

		// jar path
		String jarPath = getRelativeFilePath(baseFile, bundleFile);
		JSONObject bundleJson = createBundleJson(bundleName, bundleVersion, jarPath, detailsJson);

		return bundleJson;
	}

	@SuppressWarnings("unchecked")
	private JSONObject processFolderBundle(File baseFile, File bundleFile) throws IOException {
		if (bundleFile == null || !bundleFile.exists()) {
			return null;
		}
		File manifestFile = new File(bundleFile, JarFile.MANIFEST_NAME);
		if (!manifestFile.exists()) {
			return null;
		}
		if (isLimit()) {
			return null;
		}
		JSONObject detailsJson = new JSONObject();

		Iterator<File> classesFiles = FileUtils.iterateFiles(bundleFile, new String[] { EXT_CLASS }, true);
		while (classesFiles.hasNext()) {
			File classFile = (File) classesFiles.next();
			if (classFile.getName().contains(CHAR_INNER_CLASS)) {
				continue; // ignore the inner classes
			}
			if (isLimit()) {
				break;
			}
			number++;

			final FileInputStream fis = new FileInputStream(classFile);

			String classPath = classFile.getAbsolutePath().replace(bundleFile.getAbsolutePath(), "");
			classPath = FilenameUtils.removeExtension(classPath);

			final String message = "Can't process the class file:" + classFile.getName() + " for bundle:"
					+ bundleFile.getName();

			processClass(detailsJson, fis, classPath, message);
		}

		if (innerJar) { //
			Collection<File> jarsFiles = FileUtils.listFiles(bundleFile, new String[] { ZipFileUtil.JAR }, true);
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

		Map<String, String> bundleMap = bundlesManager.listBundlesFromManifest(manifestFile);
		if (bundleMap.isEmpty()) {
			return null;
		}
		String bundleName = bundleMap.keySet().iterator().next();
		String bundleVersion = bundleMap.get(bundleName);

		// folder
		String folderPath = getRelativeFilePath(baseFile, bundleFile);
		JSONObject bundleJson = createBundleJson(bundleName, bundleVersion, folderPath, detailsJson);

		return bundleJson;
	}

	private void processClass(JSONObject detailsJson, InputStream classStream, String classPath, String exceptionMessge)
			throws IOException {
		try {
			JavaVersion cv = getInvalidCompilerVersion(classStream);
			if (cv != null) {
				String cvKey = cv.name();
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

	private JSONObject createBundleJson(String bundleName, String bundleVersion, String bundleFilePath,
			JSONObject detailsJson) {

		JSONObject bundleJson = new JSONLinkedObject();
		bundleJson.put(KEY_BUNDLE_NAME, bundleName);
		bundleJson.put(KEY_BUNDLE_VERSION, bundleVersion);
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

	private JavaVersion getInvalidCompilerVersion(InputStream input) throws IOException {
		JavaVersion compilerVersion = getCompilerVersion(input);
		if (compilerVersion != null && baseCompilerVersion != null && !compilerVersion.equals(baseCompilerVersion)) {
			int compareTo = compilerVersion.compareTo(baseCompilerVersion);
			if (compareTo > 0 // bigger than
					|| !compatibleCompilerVersion && compareTo < 0) {// less
				return compilerVersion; // the not match version
			}

		} // else { // ignore?
		return null; // valid
	}

	private JavaVersion getCompilerVersion(InputStream input) throws IOException {
		if (input != null) {
			// [-54, -2, -70, -66, 0, 0, 0, 52]
			byte[] data = new byte[8];
			input.read(data, 0, 8);

			// also need check the class flag
			String flag = bytesToHexString(Arrays.copyOf(data, 4));
			if (!"CAFEBABE".equals(flag.toUpperCase())) {
				return null;
			}
			// int minor_version = (((int) data[4]) << 8) + data[5];
			int major_version = (((int) data[6]) << 8) + data[7];

			final int baseMajor = 48; // JavaVersion 1.4
			final int baseversion = 14;// JavaVersion 1.4
			int version = baseversion + (major_version - baseMajor);
			if (version < baseversion) {
				// should compatible always, so don't care.
			} else if (version == 15) {
				return JavaVersion.JAVA_1_5;
			} else if (version == 16) {
				return JavaVersion.JAVA_1_6;
			} else if (version == 17) {
				return JavaVersion.JAVA_1_7;
			} else if (version == 18) {
				return JavaVersion.JAVA_1_8;
			} else if (version == 19) {
				return JavaVersion.JAVA_9;
			} else {
				throw new RuntimeException("Don't support the latest compile verion :" + major_version);
			}
		}
		return null;
	}

	private String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder();
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}
}
