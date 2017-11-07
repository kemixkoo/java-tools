/**
 *  
 */
package xyz.kemix.java.eclipse;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang3.StringUtils;

import xyz.kemix.java.bundle.BundleVersion;
import xyz.kemix.java.bundle.BundlesManager;
import xyz.kemix.java.bundle.ManifestUtil;
import xyz.kemix.java.io.FileExts;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 *         Created at 2017-10-28
 *
 */
public class EclipsePluginsManager extends BundlesManager {
	public static final String NAME_ARTIFACTS = "artifacts";
	public static final String NAME_CONTENT = "content";
	public static final String FOLDER_PLUGINS = "plugins";

	/**
	 * must have "plugins" sub-folder.
	 * 
	 * @return the plugins jar files or the plugin folders.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Collection<File> listPluginsFilesFromProduct(File productFolder) throws IOException {
		if (productFolder == null || !productFolder.exists()) {
			return Collections.emptyList();
		}
		Set<File> results = new HashSet<File>();

		File pluginsFolder = new File(productFolder, FOLDER_PLUGINS);
		if (!pluginsFolder.exists()) {
			return Collections.emptyList();
		}
		// jars
		Collection jarFiles = FileUtils.listFiles(pluginsFolder, new String[] { FileExts.JAR.n() }, false);
		if (jarFiles != null)
			results.addAll(jarFiles);

		// folders
		File[] folderBundles = pluginsFolder.listFiles(new FileFilter() {

			@Override
			public boolean accept(File f) {
				return f.isDirectory() && new File(f, JarFile.MANIFEST_NAME).exists();
			}
		});
		if (folderBundles != null) {
			results.addAll(Arrays.asList(folderBundles));
		}
		return results;
	}

	/**
	 * 
	 * must have "plugins" sub-folder.
	 * 
	 * @return the bundle name with version map
	 */
	public Map<String, String> listPluginsFromProduct(File productFolder) throws IOException {
		if (productFolder == null || !productFolder.exists()) {
			return Collections.emptyMap();
		}
		Map<String, String> results = new HashMap<String, String>();

		File pluginsFolder = new File(productFolder, FOLDER_PLUGINS);
		if (!pluginsFolder.exists()) {
			return Collections.emptyMap();
		}

		Collection<File> pluginsFiles = listPluginsFilesFromProduct(productFolder);
		for (File plugin : pluginsFiles) {
			if (FileExts.JAR.of(plugin)) {
				results.putAll(listBundlesFromJar(plugin));
			} else if (plugin.isDirectory()) {
				File menifestFile = new File(plugin, JarFile.MANIFEST_NAME);
				if (menifestFile.exists()) {
					results.putAll(listBundlesFromManifest(menifestFile));
				}
			}
		}

		return results;
	}

	/**
	 * return the bundle version.
	 */
	@SuppressWarnings("rawtypes")
	public String retrieveBundleVersion(File pluginsFolder, IOFileFilter bundleFilter) throws Exception {
		String version = null;
		// set default patch version
		final int max = 10;
		int num = 0;
		Map<String, String> bundlesMap = listBundles(pluginsFolder);
		// talend plugins jars
		Iterator talendPluginsIterator = bundlesMap.keySet().iterator();

		while (talendPluginsIterator.hasNext() && num < max) {
			String bundleName = talendPluginsIterator.next().toString();

			if (bundleFilter == null || bundleFilter.accept(null, bundleName)) {
				String bundleVersion = bundlesMap.get(bundleName);

				String bundelV = new BundleVersion(bundleVersion).clone(false).toString();
				if (version == null) {
					version = bundelV;
				} else if (!bundelV.equals(version)) {
					// getLog().warn(
					// "Some bundle with defferent version: "
					// + version + " and " + bundelV);
					break; // will use the first one
				}
				num++;
			}
		}
		if (StringUtils.isBlank(version)) {
			throw new Exception("Must provide the patch version if need.");
		}
		return version;
	}

	public String retrieveBundleVersion(File zipFile, boolean ignoreBrokenPlugin, IOFileFilter bundleFilter)
			throws IOException {
		if (zipFile == null) {
			throw new IOException("Must provide the zip file");
		}
		if (!zipFile.exists()) {
			throw new FileNotFoundException(zipFile.toString());
		}

		String bundleVersion = null;
		ZipFile patchZipFile = null;
		try {
			patchZipFile = new ZipFile(zipFile);
			Enumeration<ZipArchiveEntry> patchZipFileEntries = patchZipFile.getEntries();
			int tryNum = 0;
			while (patchZipFileEntries.hasMoreElements()) {
				ZipArchiveEntry zipEntry = patchZipFileEntries.nextElement();
				// talend plugin
				if (bundleFilter == null || bundleFilter.accept(null, FilenameUtils.getBaseName(zipEntry.getName()))) {
					tryNum++;

					try {
						Manifest manifest = ManifestUtil.getZipJarManifest(patchZipFile, zipEntry);
						if (manifest != null) {
							bundleVersion = getBundleVersion(manifest);
							if (bundleVersion == null) {
								throw new IOException("Can't find bundle version in plugin " + zipEntry.getName()
										+ " of  " + zipFile.getName());
							}
						}

					} catch (Throwable t) {
						if (!ignoreBrokenPlugin) {
							throw t;
						}
					}
					if (bundleVersion != null || tryNum > 10) {
						break;
					}
				}
			}
		} catch (IOException e) {
			throw e;
		} finally {
			ZipFile.closeQuietly(patchZipFile);
		}
		return bundleVersion;
	}
}
