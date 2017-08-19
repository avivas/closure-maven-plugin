package com.bachue.closuremavenplugin;

/*-
 * #%L
 * closure-maven-plugin Maven Plugin
 * %%
 * Copyright (C) 2017 Bachue
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.Map;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.StringUtils;

import com.google.common.base.Function;
import com.google.javascript.jscomp.CommandLineRunner;

/**
 * JS Mojo class
 * @author Alejandro Vivas
 * @version 19/08/2017 0.0.1-SNAPSHOT
 * @since 19/08/2017 0.0.1-SNAPSHOT
 */
@Mojo(name = "js", defaultPhase = LifecyclePhase.COMPILE)
public class JSClosureMojo extends AbstractMojo
{
	/** Map with options to */
	@Parameter(property = "jsOptions", required = true)
	private Map<String, String> jsOptions;

	/**
	 * Class to create instance of CommandLineRunner
	 * @author Alejandro Vivas
	 * @version 19/08/2017 0.0.1-SNAPSHOT
	 * @since 19/08/2017 0.0.1-SNAPSHOT
	 */
	private static class InnerCommandLineRunner extends CommandLineRunner
	{
		/**
		 * Create a InnerCommandLineRunner
		 * @param args Arguments to command line runner
		 * @author Alejandro Vivas
		 * @version 19/08/2017 0.0.1-SNAPSHOT
		 * @since 19/08/2017 0.0.1-SNAPSHOT
		 */
		protected InnerCommandLineRunner(String[] args)
		{
			super(args);
			// To avoid exit
			this.setExitCodeReceiver(new Function<Integer, Void>() {@Override	public Void apply(Integer arg0){return null;}});
		}
	}

	/**
	 * Execute closure
	 * @author Alejandro Vivas
	 * @version 19/08/2017 0.0.1-SNAPSHOT
	 * @since 19/08/2017 0.0.1-SNAPSHOT
	 */
	public void execute() throws MojoExecutionException
	{
		if (getJsOptions().size() == 0)
		{
			throw new MojoExecutionException("Empty js-options");
		}

		String[] args = OptionsUtil.optionsToStringArray(getJsOptions());

		getLog().info("Options to js closure:" + StringUtils.join(args, " "));

		InnerCommandLineRunner runner = new InnerCommandLineRunner(args);
		if (runner.shouldRunCompiler())
		{
			runner.run();
		}
		if (runner.hasErrors())
		{
			throw new MojoExecutionException("Error to run closure");
		}
	}

	/**
	 * Map with jsOptions
	 * @param jsOptions
	 */
	public void setJsOptions(Map<String, String> jsOptions)
	{
		this.jsOptions = jsOptions;
	}

	/**
	 * Map with jsOptions
	 * @return Map with jsOptions
	 */
	public Map<String, String> getJsOptions()
	{
		return jsOptions;
	}
}
