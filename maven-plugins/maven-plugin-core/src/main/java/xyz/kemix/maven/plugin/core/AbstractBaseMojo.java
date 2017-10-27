package xyz.kemix.maven.plugin.core;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.PluginParameterExpressionEvaluator;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.util.FileUtils;

/**
 * @author Kemix Koo <kemix_koo@163.com>
 *
 *         Created at 2017-10-27
 *
 */
public abstract class AbstractBaseMojo extends AbstractMojo {
	private static final String DATE_PATTERN = "yyyyMMdd";
	private static final String DATE_TIME_PATTERN = DATE_PATTERN + "_HHmm";
	protected static final String DOT6 = StringUtils.repeat('.', 6);

	@Parameter(defaultValue = "${session}", readonly = true)
	private MavenSession session;

	@Parameter(defaultValue = "${project}", readonly = true)
	private MavenProject project;

	@Parameter(defaultValue = "${mojoExecution}", readonly = true)
	private MojoExecution mojo;

	@Parameter(defaultValue = "${plugin}", readonly = true)
	private PluginDescriptor plugin;

	@Parameter(defaultValue = "${settings}", readonly = true)
	private Settings settings;

	/**
	 * the date and time pattern, will be use for {@link DateFormat}
	 */
	@Parameter(defaultValue = DATE_TIME_PATTERN, property = "patch.build.datetimePattern")
	private String datetimePattern;

	/**
	 * the date and time pattern, will be use for {@link DateFormat}
	 */
	@Parameter(defaultValue = DATE_PATTERN, property = "patch.build.datePattern")
	private String datePattern;

	/**
	 * enable to add the spent time in log info or not
	 */
	@Parameter(defaultValue = "true", property = "patch.build.infoTime")
	private boolean infoTime;

	protected StopWatch timeWatch;

	protected MavenProject getProject() {
		return project;
	}

	protected MavenSession getSession() {
		return session;
	}

	protected MojoExecution getMojo() {
		return mojo;
	}

	protected PluginDescriptor getPlugin() {
		return plugin;
	}

	protected Settings getSettings() {
		return settings;
	}

	protected String getDatetimePattern() {
		return datetimePattern;
	}

	protected String getDatePattern() {
		return datePattern;
	}

	protected File getTempWorkDir() {
		return null;
	}

	protected File getTheFolder(File folder) throws MojoExecutionException {
		try {
			if (!folder.exists()) {
				FileUtils.forceMkdir(folder);
			}
		} catch (IOException e) {
			throw new MojoExecutionException("Can't create the folder: " + folder, e);
		}
		return folder;
	}

	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			timeWatch = StopWatch.createStarted();

			validateParameters();
			beforeExecute();
			doExecute();

			timeWatch.stop();
		} finally {
			afterExecute();
		}
	}

	protected void validateParameters() throws MojoExecutionException, MojoFailureException {
		// nothing to do
	}

	protected void beforeExecute() throws MojoExecutionException, MojoFailureException {
		resetStringParameters();
		cleanTempWorkDirectory();
	}

	protected abstract void doExecute() throws MojoExecutionException, MojoFailureException;

	protected void afterExecute() throws MojoExecutionException, MojoFailureException {
		// nothing to do
	}

	protected void cleanTempWorkDirectory() throws MojoExecutionException {
		File tempWorkDir = getTempWorkDir();
		if (tempWorkDir != null && tempWorkDir.exists()) {
			getLog().info("Clean temp work directory: " + tempWorkDir);
			try {
				FileUtils.deleteDirectory(tempWorkDir);
			} catch (IOException e) {
				throw new MojoExecutionException("Can't clean the work directory: " + tempWorkDir, e);
			}
		}
	}

	protected void resetStringParameters() {
		final Properties mojoProp = getSession().getCurrentProject().getProperties();
		try {
			List<Field> containVarList = new ArrayList<Field>();

			Field[] declaredFields = this.getClass().getDeclaredFields();
			for (Field f : declaredFields) {
				f.setAccessible(true);
				Object value = f.get(this);
				if (value != null && f.getType() == String.class) {
					String expression = value.toString();
					// still have var.
					if (hasVar(expression)) {
						containVarList.add(f);
					} else {
						mojoProp.put(f.getName(), expression);
					}
				}
			}
			eval(containVarList.iterator(), 5);
		} catch (SecurityException e) {
			//
		} catch (IllegalArgumentException e) {
			//
		} catch (IllegalAccessException e) {
			//
		} catch (Exception e) {
			//
		}
	}

	private void eval(Iterator<Field> containVarIterator, int times) throws Exception {
		if (!containVarIterator.hasNext() || times == 0) {
			return;
		}
		final Properties mojoProp = getSession().getCurrentProject().getProperties();
		// final Properties userProperties = getSession().getUserProperties();

		while (containVarIterator.hasNext()) {
			Field f = containVarIterator.next();
			f.setAccessible(true);
			Object value = f.get(this);
			String expression = value.toString();
			// set the value
			PluginParameterExpressionEvaluator evaluator = new PluginParameterExpressionEvaluator(getSession(),
					getMojo());
			Object evaluate = evaluator.evaluate(expression);
			if (evaluate != null) {
				String newExp = evaluate.toString();
				if (!hasVar(newExp)) {
					mojoProp.put(f.getName(), newExp);
					f.set(this, newExp);

					// Parameter paramAnno = f.getAnnotation(Parameter.class);
					// if (paramAnno != null) {
					// String property = paramAnno.property();
					// mojoProp.put(property, newExp);
					// userProperties.put(property, newExp);
					// }
					containVarIterator.remove(); // only left var one
				}
			}
		}
		eval(containVarIterator, times--);
	}

	private boolean hasVar(String expression) {
		return expression.indexOf("${") >= 0 && expression.indexOf('}') > 0;
	}

	protected void startStep(String message) {
		getLog().info(message + DOT6);
	}

	protected void finishStep() {
		if (infoTime) {
			long seconds = timeWatch.getTime(TimeUnit.SECONDS);
			long mSeconds = timeWatch.getTime(TimeUnit.MILLISECONDS);
			String timeStr = null;
			if (seconds == 0) {
				timeStr = mSeconds + "ms";
			} else {
				// like 1.8s
				timeStr = (mSeconds / 100) / 10f + "s";
			}
			getLog().info("Finished in " + timeStr);
		}
	}
}
