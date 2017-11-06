/**
 *  
 */
package xyz.kemix.maven.plugin.java.compiler;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.json.JSONArray;

import xyz.kemix.java.CompilerVersion;
import xyz.kemix.java.io.FileExts;
import xyz.kemix.java.io.ZipFileUtil;
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

	@Override
	protected JSONArray retrieveResult() throws IOException {
		GeneralClassReporter reporter = new GeneralClassReporter(getBaseVersion(), isCompatible(), getClassesLimit(),
				needInner());
		final File sourcePath = getSourcePath();

		if (sourcePath.isFile()) {
			FileExts fileExts = FileExts.get(sourcePath);
			if (fileExts != null) {
				switch (fileExts) {
				case CLASS:
					return reporter.processClass(sourcePath);
				case JAR:
					return reporter.processJar(sourcePath);
				case ZIP:
					File workDir = getTempWorkDir();
					ZipFileUtil.unzip(sourcePath, workDir);
					return reporter.processFolder(workDir);
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
			return reporter.processFolder(sourcePath);
		}

		return null;
	}

}
