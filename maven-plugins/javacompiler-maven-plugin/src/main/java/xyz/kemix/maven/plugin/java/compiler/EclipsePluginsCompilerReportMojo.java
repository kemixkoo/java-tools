/**
 *  
 */
package xyz.kemix.maven.plugin.java.compiler;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import xyz.kemix.java.CompilerVersion;
import xyz.kemix.java.eclipse.EclipsePluginsManager;
import xyz.kemix.java.io.FileExts;
import xyz.kemix.maven.plugin.java.compiler.reporter.BaseClassReporter;
import xyz.kemix.maven.plugin.java.compiler.reporter.EclipsePluginsClassReporter;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 *         Created at 2017-10-28
 *
 */
@Mojo(name = "plugins-report", defaultPhase = LifecyclePhase.PROCESS_CLASSES)
public class EclipsePluginsCompilerReportMojo extends BaseJavaCompilerReportMojo {

	@Override
	protected BaseClassReporter createClassReporter(CompilerVersion baseJDKVersion, boolean compatibleJDKVersion,
			int maxClasses, boolean innerJar) {
		return new EclipsePluginsClassReporter(baseJDKVersion, compatibleJDKVersion, maxClasses, innerJar);
	}

	@Override
	protected void validateParameters() throws MojoExecutionException, MojoFailureException {
		super.validateParameters();

		if (getSourcePath().isFile()) {
			if (!FileExts.ZIP.of(getSourcePath())) {
				throw new MojoExecutionException("Must be zip file: " + getSourcePath());
			}
		} else if (getSourcePath().isDirectory()
				&& !new File(getSourcePath(), EclipsePluginsManager.FOLDER_PLUGINS).exists()) {
			throw new MojoExecutionException(
					"The source path must be the parent of plugins folder, so it's invalid path :" + getSourcePath());
		}

	}

}
