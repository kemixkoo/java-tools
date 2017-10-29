/**
 *  
 */
package xyz.kemix.maven.plugin.java.compiler;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import xyz.kemix.java.CompilerVersion;
import xyz.kemix.maven.plugin.java.compiler.reporter.BaseClassReporter;
import xyz.kemix.maven.plugin.java.compiler.reporter.GeneralClassReporter;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 *         Created at 2017-10-28
 *
 */
@Mojo(name = "report", defaultPhase = LifecyclePhase.PROCESS_CLASSES)
public class GeneralCompilerReportMojo extends BaseJavaCompilerReportMojo {

	@Override
	protected BaseClassReporter createClassReporter(CompilerVersion baseJDKVersion, boolean compatibleJDKVersion,
			int maxClasses, boolean innerJar) {
		return new GeneralClassReporter(baseJDKVersion, compatibleJDKVersion, maxClasses, innerJar);
	}

}
