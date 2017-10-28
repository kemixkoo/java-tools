/**
 *  
 */
package xyz.kemix.java.eclipse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.jar.Manifest;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
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

	@SuppressWarnings("rawtypes")
	public Map<String, String> listPluginsFromProduct(File productFolder) throws IOException {
		if (productFolder == null || !productFolder.exists()) {
			return Collections.emptyMap();
		}
		Map<String, String> results = new HashMap<String, String>();

		File pluginsFolder = new File(productFolder, FOLDER_PLUGINS);
		if (!pluginsFolder.exists()) {
			return Collections.emptyMap();
		}

		// jars
		Iterator jarFiles = FileUtils.iterateFiles(pluginsFolder, new String[] { FileExts.JAR.n() }, false);
		while (jarFiles.hasNext())
			results.putAll(listBundlesFromJar((File) jarFiles.next()));

		// folders with MANIFEST.MF
		Iterator menifestFiles = FileUtils.iterateFiles(pluginsFolder, new NameFileFilter(MANIFEST_FILE_NAME) {

			@Override
			public boolean accept(File file) {
				boolean valid = super.accept(file);
				if (valid) {
					return file.getParent().equals(MANIFEST_FOLDER_NAME);
				}
				return valid;
			}

			@Override
			public boolean accept(File file, String name) {
				return accept(new File(file, name));
			}

		}, null);
		while (menifestFiles.hasNext())
			results.putAll(listBundlesFromManifest((File) menifestFiles.next()));
		return results;
	}

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

				BundleVersion v = new BundleVersion(bundleVersion);
				BundleVersion withoutQualifier = new BundleVersion(v.getMajor(), v.getMinor(), v.getMicro());
				String bundelV = withoutQualifier.toString();
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
