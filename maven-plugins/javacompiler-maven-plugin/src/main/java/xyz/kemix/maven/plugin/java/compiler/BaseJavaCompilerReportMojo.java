package xyz.kemix.maven.plugin.java.compiler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.JavaVersion;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.components.io.resources.PlexusIoFileResourceCollection;
import org.json.JSONArray;

import xyz.kemix.java.io.FileExts;
import xyz.kemix.java.json.JSONSortedArray;
import xyz.kemix.maven.plugin.core.AbstractBaseMojo;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 *         Created at 2017-10-27
 *
 */
public abstract class BaseJavaCompilerReportMojo extends AbstractBaseMojo {

	private static final String[] DEFAULT_EXCLUDES = new String[] { "**/package.html" };

	private static final String[] DEFAULT_INCLUDES = new String[] { "**/*." + ClassReporterHelper.EXT_CLASS };
	/**
	 * Location of the checking file.
	 */
	@Parameter(property = "java.compiler.checkingFile", required = true)
	private File checkingFile;

	/**
	 * the base Compiler version to classes of plugins.
	 * 
	 * only support 1.5, 1.6, 1.7, 1.8 and 9
	 * 
	 * {@link JavaVersion}
	 */
	@Parameter(defaultValue = "1.7", property = "java.compiler.version")
	private String javaVersion;
	/**
	 * enable to compatible the Compiler version,
	 * 
	 * means if baseVersion is 1.7, but the class is 1.6, will be compatibility, and
	 * don't report, else if class is 1.8, will report the class.
	 */
	@Parameter(defaultValue = "true", property = "java.compiler.compatible")
	private boolean compatible;

	/**
	 * the number of classes to check and report
	 * 
	 * if <=0, means, no limit
	 */
	@Parameter(defaultValue = "-1", property = "java.compiler.classes.limit")
	private int classesLimit;

	/**
	 * If true, will report in console.
	 */
	@Parameter(defaultValue = "false", property = "java.compiler.console")
	private boolean console;

	/**
	 * enable to limit the result in console for array
	 */
	@Parameter(defaultValue = "20", property = "java.compiler.console.limit")
	private int consoleLimit;

	/**
	 * If provide, will report to one json file.
	 */
	@Parameter(property = "java.compiler.report.file")
	private File reportFile;

	/**
	 * List of files to include. Specified as fileset patterns which are relative to
	 * the input directory
	 */
	@Parameter
	private String[] includes;

	/**
	 * List of files to exclude. Specified as fileset patterns which are relative to
	 * the input directory
	 */
	@Parameter
	private String[] excludes;

	/**
	 * Location of the temp work directory.
	 */
	@Parameter(defaultValue = "${project.build.directory}/working_report", readonly = true)
	private File tempWorkDir;

	protected static final Map<String, JavaVersion> JAVA_VERSIONS = new HashMap<>();

	protected File getTempWorkDir() {
		return tempWorkDir;
	}

	protected JavaVersion getBaseJavaVersion() {
		if (JAVA_VERSIONS.isEmpty()) {
			JAVA_VERSIONS.put("1.6", JavaVersion.JAVA_1_6);
			JAVA_VERSIONS.put("1.7", JavaVersion.JAVA_1_7);
			JAVA_VERSIONS.put("1.8", JavaVersion.JAVA_1_8);
			JAVA_VERSIONS.put("9", JavaVersion.JAVA_9);
		}
		return JAVA_VERSIONS.get(javaVersion);
	}

	private String[] getIncludes() {
		if (includes != null && includes.length > 0) {
			return includes;
		}
		return DEFAULT_INCLUDES;
	}

	private String[] getExcludes() {
		if (excludes != null && excludes.length > 0) {
			return excludes;
		}
		return DEFAULT_EXCLUDES;
	}

	@Override
	protected void validateParameters() throws MojoExecutionException, MojoFailureException {
		super.validateParameters();

		if (!checkingFile.exists()) {
			throw new MojoExecutionException("Patch file is not existed: " + checkingFile);
		}

		if (reportFile != null && reportFile.getName().equals(".")) {
			reportFile = new File(checkingFile.getParentFile(),
					FilenameUtils.getBaseName(checkingFile.getName() + FileExts.JSON.ext()));
		}
	}

	@Override
	protected void beforeExecute() throws MojoExecutionException, MojoFailureException {
		super.beforeExecute();

		try {
			if (checkingFile.isFile()) {
				String name = checkingFile.getName();
				if (FileExts.ZIP.of(name)) {

				} else if (FileExts.WAR.of(name)) {

				} else if (FileExts.TAR.of(name) || FileExts.TAR_GZ.of(name)) {

				} else if (FileExts.JAR.of(name)) {

				} else if (FileExts.CLASS.of(name)) {
					FileUtils.copyFileToDirectory(checkingFile, tempWorkDir);
				} else {
					throw new MojoExecutionException("Invalid file: " + checkingFile);
				}
			} else if (checkingFile.isDirectory()) {

			}
		} catch (IOException e) {
			throw new MojoExecutionException("Can't process the source file before checking :" + checkingFile, e);
		}

	}

	@Override
	protected void doExecute() throws MojoExecutionException, MojoFailureException {
		File baseBundlesFolder = getTempWorkDir();
		File[] listFiles = baseBundlesFolder.listFiles();
		if (listFiles == null) {
			throw new MojoExecutionException("Can't find any files to check.");
		}

		final PlexusIoFileResourceCollection collection = new PlexusIoFileResourceCollection();
		collection.setIncludes(getIncludes());
		collection.setExcludes(getExcludes());
		collection.setBaseDir(baseBundlesFolder);

		startStep("Starting to check compiler version of classes");
		JSONArray result = null;
		try {
			ClassReporterHelper helper = new ClassReporterHelper(getBaseJavaVersion(), compatible, classesLimit, false);
			result = helper.processFiles(null, listFiles);
			finishStep();
		} catch (IOException e) {
			throw new MojoExecutionException("Can't process the plugins", e);
		}
		if (result.length() == 0) {
			getLog().info("");
			getLog().info("##########################################");
			getLog().info("             All compatible!");
			getLog().info("##########################################");
			getLog().info("");
		} else {
			if (console) {
				try {
					JSONSortedArray.console = true;
					JSONSortedArray.consoleRowLimit = consoleLimit;

					System.out.println();
					System.out.println();
					System.out.println("Some classes are different compiler version:");
					System.out.println();
					result.write(new OutputStreamWriter(System.out), 4, 0);
					System.out.println();
					System.out.println();
				} finally {
					JSONSortedArray.console = false;
					JSONSortedArray.consoleRowLimit = -1;
				}
			}
			if (reportFile != null) {
				try {
					FileUtils.forceMkdir(reportFile.getParentFile());

					if (reportFile.exists()) {
						FileUtils.forceDelete(reportFile);
					}

					FileWriter fw = new FileWriter(reportFile);
					try {
						result.write(fw, 4, 0);
					} finally {
						try {
							fw.close();
						} catch (IOException e) {
							//
						}
					}
				} catch (IOException e) {
					throw new MojoExecutionException("Can't write the report to file: " + reportFile, e);
				}
			}
		}

	}
}
