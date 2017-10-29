/**
 * 
 */
package xyz.kemix.maven.plugin.java.compiler.reporter;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import xyz.kemix.java.CompilerVersion;
import xyz.kemix.java.io.FileExts;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 *         Created at 2017-10-28
 *
 */
@SuppressWarnings("rawtypes")
public abstract class BaseClassReporter extends AbstractClassReporter {

	public BaseClassReporter(CompilerVersion baseJDKVersion, boolean compatibleJDKVersion, int maxClasses,
			boolean innerJar) {
		super(baseJDKVersion, compatibleJDKVersion, maxClasses, innerJar);
	}

	public JSONArray processClass(File classFile) throws IOException {
		return processClasses(new File[] { classFile });
	}

	public JSONArray processClasses(File[] classesFiles) throws IOException {
		return processClasses(null, classesFiles);
	}

	public JSONArray processClasses(File baseFile, File[] classesFiles) throws IOException {
		JSONArray result = new JSONArray();
		JSONObject detailsJson = new JSONObject();

		for (File classFile : classesFiles) {
			processClass(detailsJson, baseFile, classFile);
		}
		result.put(detailsJson);
		return result;
	}

	public JSONArray processJar(File classFile) throws IOException {
		return processJars(new File[] { classFile });
	}

	public JSONArray processJars(File[] classesFiles) throws IOException {
		return processJars(null, classesFiles);
	}

	public JSONArray processJars(File baseFile, File[] classesFiles) throws IOException {
		JSONArray result = new JSONArray();
		for (File classFile : classesFiles) {
			JSONObject jarJson = processJarBundle(baseFile, classFile);
			result.put(jarJson);
		}
		return result;
	}

	public JSONArray processFolder(File folder) throws IOException {
		Collection listFiles = FileUtils.listFiles(folder, new String[] { FileExts.JAR.n(), FileExts.CLASS.n() }, true);
		return processFiles(folder, (File[]) listFiles.toArray());
	}
}
