package xyz.kemix.java.bundle;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.ArrayUtils;

import xyz.kemix.java.io.ZipFileUtil;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 *         Created at 2017-10-28
 *
 */
public class BundlesManager {
	public static final String MANIFEST_FILE_NAME = new File(JarFile.MANIFEST_NAME).getName();
	public static final String MANIFEST_FOLDER_NAME = new File(JarFile.MANIFEST_NAME).getParent();

	/**
	 * 
	 * Find the matched bundle names of file.
	 * 
	 * if bundleNames is null, will copy all bundles (jars).
	 * 
	 * if packFolder is true, will pack the folder type of bundle to jar at the same
	 * time.
	 */
	public void copy(final File sourceBundlesFolder, final String[] bundleNames, final File targetFolder,
			final boolean packFolder, final boolean unpackJar) throws IOException {
		if (sourceBundlesFolder == null || targetFolder == null) {
			throw new IOException(new NullPointerException());
		}
		if (!sourceBundlesFolder.exists()) {
			throw new FileNotFoundException(sourceBundlesFolder.toString());
		}
		final Set<String> foundBundles = new HashSet<String>();

		File[] bundlesFiles = null;
		try {
			bundlesFiles = sourceBundlesFolder.listFiles(new FileFilter() {

				@Override
				public boolean accept(File f) {
					try {
						Manifest manifest = null;
						// must be jar
						if (f.isFile() && f.getName().endsWith(ZipFileUtil.EXT_JAR)) {
							manifest = ManifestUtil.getJarManifest(f);

						} else if (f.isDirectory()) { // support folder
							File manifestFile = new File(f, JarFile.MANIFEST_NAME);
							manifest = ManifestUtil.getFileManifest(manifestFile);
						}
						if (manifest != null && valid(manifest, f)) {
							return true;
						}
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
					return false;
				}

				private boolean valid(Manifest manifest, File f) {
					String bundleSymbolicName = getBundleSymbolicName(manifest);
					if (bundleSymbolicName != null //
							&& (bundleNames == null // all
									|| ArrayUtils.contains(bundleNames, // limit
											bundleSymbolicName))) {
						foundBundles.add(bundleSymbolicName);
						return true;
					}
					return false;
				}
			});
		} catch (RuntimeException e) {
			if (e.getCause() instanceof IOException) {
				throw (IOException) e.getCause();
			}
		}

		if (bundlesFiles == null || bundlesFiles.length == 0) {
			throw new FileNotFoundException("Can't find any bundles from " + sourceBundlesFolder);
		}
		// missing some bundles
		if (bundleNames != null && foundBundles.size() < bundleNames.length) {
			List<String> checkedList = new ArrayList<String>(Arrays.asList(bundleNames));
			checkedList.removeAll(foundBundles);
			throw new IOException("The bundles : '" + checkedList.toString() + "' are not existed in sources folder: "
					+ sourceBundlesFolder);
		}

		FileUtils.forceMkdir(targetFolder);

		for (File bundle : bundlesFiles) {
			if (bundle.isDirectory()) {
				if (packFolder) {
					File targetJarFile = new File(targetFolder, bundle.getName() + ZipFileUtil.EXT_JAR);
					ZipFileUtil.zip(bundle, targetJarFile);
				} else {
					FileUtils.copyDirectoryToDirectory(bundle, targetFolder);
				}
			} else if (bundle.isFile()) {
				if (unpackJar) {
					File targetPluginFolder = new File(targetFolder, FilenameUtils.getBaseName(bundle.getName()));
					ZipFileUtil.unzip(bundle, targetPluginFolder);
				} else {
					FileUtils.copyFileToDirectory(bundle, targetFolder, true);
				}
			}
		}
	}

	/**
	 * 
	 * List the bundles from the file or folder
	 */
	public Map<String, String> listBundles(File file) throws IOException {
		if (file == null || !file.exists()) {
			return Collections.emptyMap();
		}
		if (file.isDirectory()) {
			return listBundlesFromFolder(file);
		} else if (file.isFile()) {
			if (file.getName().endsWith(ZipFileUtil.EXT_ZIP)) {
				return listBundlesFromZip(file);
			} else if (file.getName().endsWith(ZipFileUtil.EXT_JAR)) {
				return listBundlesFromJar(file);
			}
		}
		return Collections.emptyMap();
	}

	@SuppressWarnings("rawtypes")
	public Map<String, String> listBundlesFromFolder(File bundlesFolder) throws IOException {
		if (bundlesFolder == null || !bundlesFolder.exists()) {
			return Collections.emptyMap();
		}
		Map<String, String> results = new HashMap<String, String>();
		// jars
		Iterator jarFiles = FileUtils.iterateFiles(bundlesFolder, new String[] { ZipFileUtil.JAR }, false);
		while (jarFiles.hasNext())
			results.putAll(listBundlesFromJar((File) jarFiles.next()));

		// folders
		Iterator folderFiles = FileUtils.iterateFiles(bundlesFolder,
				new WildcardFileFilter("*/" + JarFile.MANIFEST_NAME), null);
		while (folderFiles.hasNext())
			results.putAll(listBundlesFromManifest((File) folderFiles.next()));
		return results;
	}

	public Map<String, String> listBundlesFromZip(File zipFile) throws IOException {
		if (zipFile == null || !zipFile.exists()) {
			return Collections.emptyMap();
		}
		Map<String, String> results = new HashMap<String, String>();

		ZipFile zip = null;
		try {
			zip = new ZipFile(zipFile);
			Enumeration<ZipArchiveEntry> entries = zip.getEntries();
			while (entries.hasMoreElements()) {
				ZipArchiveEntry zipEntry = entries.nextElement();
				if (!zipEntry.isDirectory()) {
					String path = zipEntry.getName();
					Manifest manifest = null;
					if (path.endsWith(ZipFileUtil.EXT_JAR)) {
						manifest = ManifestUtil.getZipJarManifest(zip, zipEntry);

					} else if (path.endsWith(JarFile.MANIFEST_NAME)) {
						manifest = ManifestUtil.getZipFileManifest(zip, zipEntry);

					}
					if (manifest != null)
						results.put(getBundleSymbolicName(manifest), getBundleVersion(manifest));

				}
			}
		} catch (IOException e) {
			throw new IOException(zipFile.toString(), e);
		} finally {
			IOUtils.closeQuietly(zip);
		}

		return results;
	}

	public Map<String, String> listBundlesFromManifest(File manifestFile) throws IOException {
		if (manifestFile == null || !manifestFile.exists()
				|| !manifestFile.getAbsolutePath().endsWith(JarFile.MANIFEST_NAME)) {
			return Collections.emptyMap();
		}
		Map<String, String> results = new HashMap<String, String>();

		try {
			Manifest manifest = ManifestUtil.getFileManifest(manifestFile);
			if (manifest != null)
				results.put(getBundleSymbolicName(manifest), getBundleVersion(manifest));
		} catch (IOException e) {
			throw new IOException(manifestFile.toString(), e);
		}

		return results;
	}

	public Map<String, String> listBundlesFromJar(File jarFile) throws IOException {
		if (jarFile == null || !jarFile.exists()) {
			return Collections.emptyMap();
		}
		Map<String, String> results = new HashMap<String, String>();

		try {
			Manifest jarManifest = ManifestUtil.getJarManifest(jarFile);
			if (jarManifest != null)
				results.put(getBundleSymbolicName(jarManifest), getBundleVersion(jarManifest));

		} catch (IOException e) {
			throw new IOException(jarFile.toString(), e);
		}

		return results;
	}

	public String getBundleSymbolicName(Manifest manifest) {
		String name = manifest.getMainAttributes().getValue(ManifestUtil.BUNDLE_SYMBOLICNAME);
		if (name != null) {
			final int indexOf = name.indexOf(';');
			if (indexOf > 0)
				name = name.substring(0, indexOf);
			return name;
		}
		return null;
	}

	public String getBundleVersion(Manifest manifest) {
		return manifest.getMainAttributes().getValue(ManifestUtil.BUNDLE_VERSION);
	}

}
