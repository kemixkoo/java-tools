/**
 *  
 */
package xyz.kemix.maven.plugin.java.compiler.reporter;

import xyz.kemix.java.CompilerVersion;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 *         Created at 2017-10-29
 *
 */
public class GeneralClassReporter extends BaseClassReporter {

	public GeneralClassReporter(CompilerVersion baseJDKVersion, boolean compatibleJDKVersion, int maxClasses,
			boolean innerJar) {
		super(baseJDKVersion, compatibleJDKVersion, maxClasses, innerJar);
	}

}
