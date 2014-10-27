package com.dooapp.configuration;

import com.dooapp.Spoon;
import com.dooapp.configuration.SpoonConfigurationBuilder;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by gerard on 15/10/2014.
 */
abstract class AbstractSpoonConfigurationBuilder
		implements SpoonConfigurationBuilder {

	protected final List<String> parameters = new LinkedList<String>();
	protected final Spoon spoon;

	protected AbstractSpoonConfigurationBuilder(Spoon spoon) {
		this.spoon = spoon;
		if (this.spoon.getLog().isInfoEnabled()) {
			parameters.add("-v");
		}
		if (this.spoon.getLog().isDebugEnabled()) {
			parameters.add("--vvv");
		}
	}

	@Override
	public SpoonConfigurationBuilder addInputFolder() {
		final String srcDir = spoon.getProject().getBuild()
				.getSourceDirectory();
		final File srcDirFile = new File(srcDir);
		if (srcDirFile.exists()) {
			parameters.add("-i");
			parameters.add(srcDir);
			return this;
		} else if (spoon.getSrcFolder() != null && spoon.getSrcFolder()
				.exists()) {
			parameters.add("-i");
			parameters.add(spoon.getSrcFolder().getAbsolutePath());
			return this;
		}
		throw new RuntimeException(
				"No source directory for " + spoon.getProject().getName()
						+ " project.");
	}

	@Override
	public SpoonConfigurationBuilder addOutputFolder() {
		// Create output folder if it doesn't exist.
		if (!spoon.getOutFolder().exists()) {
			if (spoon.getOutFolder().mkdirs()) {
				throw new RuntimeException("Cannot create ouput directories.");
			}
		}

		parameters.add("-o");
		parameters.add(spoon.getOutFolder().getAbsolutePath());
		return this;
	}

	@Override
	public SpoonConfigurationBuilder addCompliance() {
		parameters.add("--compliance");
		//TODO load it from the project compilation level
		parameters.add("7");
		return this;
	}

	@Override
	public SpoonConfigurationBuilder addSourceClasspath() {
		final MavenProject project = spoon.getProject();
		List compileClasspath = null;
		try {
			compileClasspath = project.getCompileClasspathElements();
		} catch (DependencyResolutionRequiredException e) {
			final String errorMessage = "Cannot get compile classpath elements.";
			spoon.getLog().warn(errorMessage);
			throw new RuntimeException(errorMessage,
					e);
		}
		if (compileClasspath.size() > 1) {
			final StringBuilder classpath = new StringBuilder();
			for (int i = 1; i < compileClasspath.size(); i++) {
				Object dependency = compileClasspath.get(i);
				spoon.getLog().info("current dependency: " + dependency);
				classpath.append(dependency + System.getProperty(
						"path.separator"));
			}
			spoon.getLog().info("Source classpath: " + classpath.toString());
			parameters.add("--source-classpath");
			parameters.add(classpath.toString());
		}
		return this;
	}

	@Override
	public SpoonConfigurationBuilder addPreserveFormatting() {
		if (spoon.isPreserveFormatting()) {
			parameters.add("-f");
		}
		return this;
	}

	@Override
	public String[] build() {
		spoon.getLog().info("Running spoon with parameters : ");
		spoon.getLog().info(parameters.toString());
		return parameters.toArray(new String[parameters.size()]);
	}

	/**
	 * Concatenates a tab in a string with a path separator given.
	 */
	protected String implode(String[] tabToConcatenate, String pathSeparator) {
		final StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < tabToConcatenate.length; i++) {
			buffer.append(tabToConcatenate[i]);
			if (i < tabToConcatenate.length - 1) {
				buffer.append(pathSeparator);
			}
		}
		return buffer.toString();
	}
}
