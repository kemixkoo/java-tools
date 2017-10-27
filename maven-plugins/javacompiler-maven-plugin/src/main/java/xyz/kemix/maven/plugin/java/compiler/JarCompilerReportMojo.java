/**
 *  
 */
package xyz.kemix.maven.plugin.java.compiler;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 *         Created at 2017-10-28
 *
 */
@Mojo(name = "jar-report", defaultPhase = LifecyclePhase.PROCESS_CLASSES)
public class JarCompilerReportMojo extends BaseJavaCompilerReportMojo {

}
