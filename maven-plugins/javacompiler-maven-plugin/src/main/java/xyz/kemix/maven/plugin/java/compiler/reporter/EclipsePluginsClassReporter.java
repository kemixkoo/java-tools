/**
 *  
 */
package xyz.kemix.maven.plugin.java.compiler.reporter;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.jar.JarFile;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;

import xyz.kemix.java.CompilerVersion;
import xyz.kemix.java.eclipse.EclipsePluginsManager;

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
		}
		return false;
	}

	/**
	 * Only support zip file or folder with sub-folders "plugins", so only need deal
	 * with folder way.
	 */
	@Override
	public JSONArray processFolder(File folder) throws IOException {
		Collection<File> pluginsFiles = pluginsManager.listPluginsFilesFromProduct(folder);
		return processFiles(folder, FileUtils.convertFileCollectionToFileArray(pluginsFiles));
	}

	@Override
	public JSONArray processClasses(File baseFile, File[] classesFiles) throws IOException {
		throw new UnsupportedOperationException(); // don't support
	}

	@Override
	public JSONArray processJars(File baseFile, File[] classesFiles) throws IOException {
		throw new UnsupportedOperationException(); // don't support
	}

}
