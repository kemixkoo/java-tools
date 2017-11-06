/**
 *  
 */
package xyz.kemix.maven.plugin.java.compiler.reporter;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.jar.JarFile;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import xyz.kemix.java.CompilerVersion;
import xyz.kemix.java.eclipse.EclipsePluginsManager;
import xyz.kemix.java.io.FileExts;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 *         Created at 2017-10-29
 *
 */
public class EclipsePluginsClassReporter extends BaseClassReporter {
	private final EclipsePluginsManager pluginsManager = new EclipsePluginsManager();

	public EclipsePluginsClassReporter(CompilerVersion baseJDKVersion, boolean compatibleJDKVersion, int maxClasses,
			boolean innerJar) {
		super(baseJDKVersion, compatibleJDKVersion, maxClasses, innerJar);
	}

	boolean validFolderBundle(File bundleFile) {
		if (super.validFolderBundle(bundleFile)) {
			// must have MANIFEST.MF
			File manifestFile = new File(bundleFile, JarFile.MANIFEST_NAME);
			if (manifestFile.exists()) {
				return true;
			}
		}
		return false;
	}

	boolean validJarBundle(ZipFile jarFile) {
		if (super.validJarBundle(jarFile)) {
			ZipArchiveEntry manifestEntry = jarFile.getEntry(JarFile.MANIFEST_NAME);
			if (manifestEntry == null) { // not jar
				return false;
			}
			return true;
		}
		return false;
	}

	/**
	 * Only support zip file or folder with sub-folders "plugins", so only need deal
	 * with folder way.
	 */
	public JSONArray processFolder(File folder) throws IOException {
		Collection<File> pluginsFiles = pluginsManager.listPluginsFilesFromProduct(folder);

		File[] listFiles = FileUtils.convertFileCollectionToFileArray(pluginsFiles);
		Arrays.sort(listFiles);

		JSONArray bundlesArrays = new JSONArray();
		for (File file : listFiles) {
			try {
				JSONObject bundleJson = null;
				if (file.isFile() && file.getName().endsWith(FileExts.JAR.ext())) {
					bundleJson = processJarBundle(folder, file);
				} else if (file.isDirectory()) {
					bundleJson = processFolderBundle(folder, file);
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

}
