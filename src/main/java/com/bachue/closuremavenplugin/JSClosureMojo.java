package com.bachue.closuremavenplugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.StringUtils;

import com.bachue.closuremavenplugin.util.FileUtil;
import com.google.common.base.Function;
import com.google.javascript.jscomp.CommandLineRunner;

/**
 * JS Mojo class
 * @author Alejandro Vivas
 * @version 26/09/2017 0.0.1-SNAPSHOT
 * @since 19/08/2017 0.0.1-SNAPSHOT
 */
@Mojo(name = "js", defaultPhase = LifecyclePhase.COMPILE)
public class JSClosureMojo extends AbstractMojo
{
	/** Object to get project path */
	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	private MavenProject project;
	
	/** Map with options to */
	@Parameter(property = "jsOptions", required = true)
	private Map<String, String> jsOptions;
	/** Map with options to */
	@Parameter(property = "jsArgs", required = true)
	private List<String> jsArgs;
	/** JavaScript source directories */
	@Parameter(property = "jsSourceDirectories", required = false)
	private List<String> jsSourceDirectories;
	/** JavaScript extension to find sources */
	@Parameter(property = "jsExtensions", required = false)
	private List<String> jsExtensions = new ArrayList<>();
	/** Use default configuration values */
	@Parameter(property = "useJsDefaultValues", required = false, defaultValue = "true")
	private boolean useJsDefaultValues;

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
			this.setExitCodeReceiver(new Function<Integer, Void>()
			{
				@Override
				public Void apply(Integer arg0)
				{
					return null;
				}
			});
		}
	}

	/**
	 * Execute closure
	 * @author Alejandro Vivas
	 * @version 05/09/2017 0.0.1-SNAPSHOT
	 * @since 19/08/2017 0.0.1-SNAPSHOT
	 */
	public void execute() throws MojoExecutionException
	{
		defineDefaultValues();
		String [] argsToJsClosure = createArgsToJsClosure();
		
		if (argsToJsClosure.length == 0)
		{
			getLog().warn("Empty args to js closure. No javascript files found or arguments to javascript closure");
			return;
		}
		
		getLog().info("Options to js closure:" + StringUtils.join(argsToJsClosure, " "));

		InnerCommandLineRunner runner = new InnerCommandLineRunner(argsToJsClosure);
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
	 * Define default values
	 * @author Alejandro Vivas
	 * @version 26/09/2017 0.0.1-SNAPSHOT
	 * @since 5/09/2017 0.0.1-SNAPSHOT
	 */
	private void defineDefaultValues()
	{
		if(isUseJsDefaultValues())
		{
			if( (getJsSourceDirectories() == null) || getJsSourceDirectories().isEmpty() )
			{
				setJsSourceDirectories(new ArrayList<String>());
				getJsSourceDirectories().add(getProject().getBasedir() +"/src/main/javascript" );
			}
			else
			{
				List<String> newList = new ArrayList<>(getJsSourceDirectories().size());
				for(String sourceDirectory : getJsSourceDirectories())
				{
					if( !new File(sourceDirectory).isAbsolute() )
					{
						newList.add(getProject().getBasedir() + File.separator + sourceDirectory);
					}
					else
					{
						newList.add(sourceDirectory);
					}
				}
				setJsSourceDirectories(newList);
			}
			
			if( (getJsExtensions() == null) || getJsExtensions().isEmpty() )
			{
				setJsExtensions(new ArrayList<String>());
				getJsExtensions().add("js");
			}
		}
	}
	
	/**
	 * Create arguments to javascript closure
	 * @author Alejandro Vivas
	 * @version 6/09/2017 0.0.1-SNAPSHOT
	 * @since 5/09/2017 0.0.1-SNAPSHOT
	 * @return String array with arguments to javascript closure
	 */
	private String [] createArgsToJsClosure()
	{
		String [] argsArray = getJsArgs().toArray(new String[getJsArgs().size()]);
		String [] jsOptionsArray = OptionsUtil.optionsToStringArray(getJsOptions());
		String [] argsToJsClosure = ArrayUtil.concat(argsArray, jsOptionsArray);

		if( !getJsSourceDirectories().isEmpty() && !getJsExtensions().isEmpty() )
		{
			Collection<File> jsFiles = FileUtil.getFiles(getJsSourceDirectories(), getJsExtensions(),getLog());
			if(!jsFiles.isEmpty())
			{
				String [] arrayJsFiles = new String[jsFiles.size()];
				int i = 0;
				for(File file : jsFiles)
				{
					arrayJsFiles[i++] = file.getAbsolutePath();
				}
				argsToJsClosure = ArrayUtil.concat(arrayJsFiles, argsToJsClosure);
			}
		}
		
		// Delete backslash, avoid problems when generate source map
		for (int i = 0; i < argsToJsClosure.length; i++)
		{
			argsToJsClosure[i] = argsToJsClosure[i].replaceAll("\\\\", "/");
		}
		
		return argsToJsClosure;
	}

	/**
	 * Map with jsOptions
	 * @param jsOptions
	 * @version 21/08/2017 0.0.1-SNAPSHOT
	 * @since 21/08/2017 0.0.1-SNAPSHOT
	 */
	public void setJsOptions(Map<String, String> jsOptions)
	{
		this.jsOptions = jsOptions;
	}

	/**
	 * Map with jsOptions
	 * @return Map with jsOptions
	 * @version 21/08/2017 0.0.1-SNAPSHOT
	 * @since 21/08/2017 0.0.1-SNAPSHOT
	 */
	public Map<String, String> getJsOptions()
	{
		return jsOptions;
	}

	/**
	 * @param jsArgs the jsArgs to set
	 * @version 21/08/2017 0.0.1-SNAPSHOT
	 * @since 21/08/2017 0.0.1-SNAPSHOT
	 */
	public void setJsArgs(List<String> jsArgs)
	{
		this.jsArgs = jsArgs;
	}

	/**
	 * @return the jsArgs
	 * @author Alejandro Vivas
	 * @version 21/08/2017 0.0.1-SNAPSHOT
	 * @since 21/08/2017 0.0.1-SNAPSHOT
	 */
	public List<String> getJsArgs()
	{
		return jsArgs;
	}

	/**
	 * @author Alejandro Vivas
	 * @version 5/09/2017 0.0.1-SNAPSHOT
	 * @since 5/09/2017 0.0.1-SNAPSHOT
	 * @return the jsSourceDirectories
	 */
	public List<String> getJsSourceDirectories()
	{
		return jsSourceDirectories;
	}

	/**
	 * @author Alejandro Vivas
	 * @version 5/09/2017 0.0.1-SNAPSHOT
	 * @since 5/09/2017 0.0.1-SNAPSHOT
	 * @param jsSourceDirectories the jsSourceDirectories to set
	 */
	public void setJsSourceDirectories(List<String> jsSourceDirectories)
	{
		this.jsSourceDirectories = jsSourceDirectories;
	}

	/**
	 * @author Alejandro Vivas
	 * @version 5/09/2017 0.0.1-SNAPSHOT
	 * @since 5/09/2017 0.0.1-SNAPSHOT
	 * @return the jsExtensions
	 */
	public List<String> getJsExtensions()
	{
		return jsExtensions;
	}

	/**
	 * @author Alejandro Vivas
	 * @version 5/09/2017 0.0.1-SNAPSHOT
	 * @since 5/09/2017 0.0.1-SNAPSHOT
	 * @param jsExtensions the jsExtensions to set
	 */
	public void setJsExtensions(List<String> jsExtensions)
	{
		this.jsExtensions = jsExtensions;
	}

	/**
	 * @author Alejandro Vivas
	 * @version 5/09/2017 0.0.1-SNAPSHOT
	 * @since 5/09/2017 0.0.1-SNAPSHOT
	 * @return the useJsDefaultValues
	 */
	public boolean isUseJsDefaultValues()
	{
		return useJsDefaultValues;
	}

	/**
	 * @author Alejandro Vivas
	 * @version 5/09/2017 0.0.1-SNAPSHOT
	 * @since 5/09/2017 0.0.1-SNAPSHOT
	 * @param useJsDefaultValues the useJsDefaultValues to set
	 */
	public void setUseJsDefaultValues(boolean useJsDefaultValues)
	{
		this.useJsDefaultValues = useJsDefaultValues;
	}
	
	/**
	 * @author Alejandro Vivas
	 * @version 5/09/2017 0.0.1-SNAPSHOT
	 * @since 5/09/2017 0.0.1-SNAPSHOT
	 * @param project the project to set
	 */
	public void setProject(MavenProject project)
	{
		this.project = project;
	}
	
	/**
	 * @author Alejandro Vivas
	 * @version 5/09/2017 0.0.1-SNAPSHOT
	 * @since 5/09/2017 0.0.1-SNAPSHOT
	 * @return the project
	 */
	public MavenProject getProject()
	{
		return project;
	}
}
