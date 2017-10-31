package xyz.kemix.maven.plugin.java.compiler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.JavaVersion;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.json.JSONArray;

import xyz.kemix.java.CompilerVersion;
import xyz.kemix.java.io.FileExts;
import xyz.kemix.java.io.ZipFileUtil;
import xyz.kemix.maven.plugin.core.AbstractBaseMojo;
import xyz.kemix.maven.plugin.java.compiler.reporter.BaseClassReporter;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 *         Created at 2017-10-27
 *
 */
public abstract class BaseJavaCompilerReportMojo extends AbstractBaseMojo {

	private static final String[] DEFAULT_EXCLUDES = new String[] { "**/package.html" };

	private static final String[] DEFAULT_INCLUDES = new String[] { "**/*." + FileExts.CLASS.ext() };
	/**
	 * Location of the checking file.
	 */
	@Parameter(property = "java.compiler.source.path", required = true)
	private File sourcePath;

	/**
	 * the base Compiler version to classes of plugins.
	 * 
	 * only support 1.5, 1.6, 1.7, 1.8 and 9
	 * 
	 * {@link JavaVersion}
	 */
	@Parameter(defaultValue = "1.8", property = "java.compiler.baseVersion")
	private String baseVersion;
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
	 * 
	 * if -1, will be no limit. currently, 20 by default
	 */
	@Parameter(defaultValue = "20", property = "java.compiler.console.limit")
	private int consoleLimit;

	/**
	 * If provide, will report to one json file.
	 */
	@Parameter(defaultValue = "${project.build.directory}/report.json", property = "java.compiler.report.file")
	private File reportFile;

	/**
	 * List of files to include. Specified as fileset patterns which are relative to
	 * the input directory
	 */
	// @Parameter
	private String[] includes;

	/**
	 * List of files to exclude. Specified as fileset patterns which are relative to
	 * the input directory
	 */
	// @Parameter
	private String[] excludes;

	/**
	 * Location of the temp work directory.
	 */
	@Parameter(defaultValue = "${project.build.directory}/working_report", readonly = true)
	private File tempWorkDir;

	protected File getSourcePath() {
		return sourcePath;
	}

	protected File getTempWorkDir() {
		return tempWorkDir;
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

		if (!sourcePath.exists()) {
			throw new MojoExecutionException("Patch file is not existed: " + sourcePath);
		}

		if (reportFile != null && reportFile.getName().equals(".")) {
			reportFile = new File(sourcePath.getParentFile(),
					FilenameUtils.getBaseName(sourcePath.getName() + FileExts.JSON.ext()));
		}
	}

	protected abstract BaseClassReporter createClassReporter(CompilerVersion baseJDKVersion,
			boolean compatibleJDKVersion, int maxClasses, boolean innerJar);

	@Override
	protected void doExecute() throws MojoExecutionException, MojoFailureException {
		JSONArray result = new JSONArray();
		try {
			startStep("Starting to check compiler version of classes");

			// TODO, Will support it later
			// final PlexusIoFileResourceCollection collection = new
			// PlexusIoFileResourceCollection();
			// collection.setIncludes(getIncludes());
			// collection.setExcludes(getExcludes());
			// collection.setBaseDir(baseBundlesFolder);

			BaseClassReporter helper = createClassReporter(CompilerVersion.get(baseVersion), compatible, classesLimit,
					false);

			if (sourcePath.isFile()) {
				FileExts fileExts = FileExts.get(sourcePath);
				if (fileExts != null) {
					switch (fileExts) {
					case CLASS:
						result = helper.processClass(sourcePath);
						break;
					case JAR:
						result = helper.processJar(sourcePath);
						break;
					case ZIP:
						File workDir = getTempWorkDir();
						ZipFileUtil.unzip(sourcePath, workDir);
						result = helper.processFolder(workDir);
						break;
					case TAR:
					case TAR_GZ:
						// TODO
						break;
					case WAR:
						// TODO
						break;
					default:
						throw new IOException("Don't support this file type:" + sourcePath);
					}
				}

			} else if (sourcePath.isDirectory()) {
				result = helper.processFolder(sourcePath);
			}

			finishStep();
		} catch (Exception e) {
			throw new MojoExecutionException("Can't process the source file before checking :" + sourcePath, e);
		}

		if (result == null || result.length() == 0) {
			getLog().info("");
			getLog().info("##########################################");
			getLog().info("             All compatible!");
			getLog().info("##########################################");
			getLog().info("");
		} else {
			if (console) {
				JSONArray consoleArray = new JSONArray();
				for (int i = 0; i < result.length() && (consoleLimit == -1 || i < consoleLimit); i++) {
					consoleArray.put(consoleArray.get(i));
				}
				System.out.println();
				System.out.println();
				System.out.println("Some classes are different compiler version:");
				System.out.println();
				consoleArray.write(new OutputStreamWriter(System.out), 4, 0);
				System.out.println();
				System.out.println();
			}

			// record to report file
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
