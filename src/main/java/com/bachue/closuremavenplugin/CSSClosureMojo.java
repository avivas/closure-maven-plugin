package com.bachue.closuremavenplugin;

import java.io.File;
import java.security.Permission;
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
import com.google.common.css.compiler.commandline.ClosureCommandLineCompiler;

/**
 * CSS Mojo class
 * @author Alejandro Vivas
 * @version 05/09/2017 0.0.1-SNAPSHOT
 * @since 19/08/2017 0.0.1-SNAPSHOT
 */
@Mojo(name = "css", defaultPhase = LifecyclePhase.COMPILE)
public class CSSClosureMojo extends AbstractMojo
{
	/** Object to get project path */
	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	private MavenProject project;
	
	/** Map with options to */
	@Parameter(property = "cssOptions", required = true)
	private Map<String, String> cssOptions;
	/** Map with options to */
	@Parameter(property = "cssArgs", required = true)
	private List<String> cssArgs;
	/** CSS source directories */
	@Parameter(property = "cssSourceDirectories", required = false)
	private List<String> cssSourceDirectories;
	/** CSS extension to find sources */
	@Parameter(property = "cssExtensions", required = false)
	private List<String> cssExtensions = new ArrayList<>();
	/** Use default configuration values */
	@Parameter(property = "useCssDefaultValues", required = false, defaultValue = "true")
	private boolean useCssDefaultValues;	
	
	/**
	 * Class to avoid System.exit call of closure Stylesheet
	 * @author Alejandro Vivas
	 * @version 21/08/2017 0.0.1-SNAPSHOT
	 * @since 19/08/2017 0.0.1-SNAPSHOT
	 */
	class InternalSecurityManager extends SecurityManager
	{
		/**
		 * To avoid System.exit call
		 * @author Alejandro Vivas
		 * @version 20/08/2017 0.0.1-SNAPSHOT
		 * @since 19/08/2017 0.0.1-SNAPSHOT
		 * @see java.lang.SecurityManager#checkExit(int)
		 */
		@Override
		public void checkExit(int status)
		{
			throw new SecurityException();
		}

		/**
		 * To avoid error AccessControlException
		 * @author Alejandro Vivas
		 * @version 20/08/2017 0.0.1-SNAPSHOT
		 * @since 19/08/2017 0.0.1-SNAPSHOT
		 * @see java.lang.SecurityManager#checkPermission(java.security.Permission)
		 */
		@Override
		public void checkPermission(Permission perm)
		{
		}
	}

	/**
	 * Execute closure stylesheets
	 * @author Alejandro Vivas
	 * @version 05/09/2017 0.0.1-SNAPSHOT
	 * @since 19/08/2017 0.0.1-SNAPSHOT
	 */
	public void execute() throws MojoExecutionException
	{
		defineDefaultValues();
				
		String[] args = createArgsToCssClosure();
		
		if (args.length == 0)
		{
			getLog().warn("Empty args to css closure. No CSS files found or arguments to CSS closure");
			return;
		}

		getLog().info("Options to css closure:" + StringUtils.join(args, " "));

		if (getCssOptions().containsKey("output-file"))
		{
			try
			{
				new File(getCssOptions().get("output-file")).getParentFile().mkdirs();
			}
			catch (SecurityException e)
			{
				throw new MojoExecutionException("Error to create folder:" + getCssOptions().get("output-file"));
			}
		}

		// Change security manager to avoid System.exit
		SecurityManager securityManagerOriginal = System.getSecurityManager();
		System.setSecurityManager(new InternalSecurityManager());
		try
		{
			ClosureCommandLineCompiler.main(args);// Run closure stylesheet
		}
		catch (SecurityException e)
		{
		}
		finally
		{
			System.setSecurityManager(securityManagerOriginal);// Return original security manager
		}
	}
	
	/**
	 * Define default values
	 * @author Alejandro Vivas
	 * @version 5/09/2017 0.0.1-SNAPSHOT
	 * @since 5/09/2017 0.0.1-SNAPSHOT
	 */
	private void defineDefaultValues()
	{
		if(isUseCssDefaultValues())
		{
			if( (getCssSourceDirectories() == null) || getCssSourceDirectories().isEmpty() )
			{
				setCssSourceDirectories(new ArrayList<String>());
				getCssSourceDirectories().add(getProject().getBasedir() +"/src/main/css" );
			}
			
			if( (getCssExtensions() == null) || getCssExtensions().isEmpty() )
			{
				setCssExtensions(new ArrayList<String>());
				getCssExtensions().add("css");
			}
		}
	}
	
	/**
	 * Create arguments to css closure
	 * @author Alejandro Vivas
	 * @version 5/09/2017 0.0.1-SNAPSHOT
	 * @since 5/09/2017 0.0.1-SNAPSHOT
	 * @return String array with arguments to css closure
	 */
	private String [] createArgsToCssClosure()
	{
		String [] argsArray = getCssArgs().toArray(new String[getCssArgs().size()]);
		String [] cssOptionsArray = OptionsUtil.optionsToStringArray(getCssOptions());
		String [] argsToCssClosure = ArrayUtil.concat(argsArray, cssOptionsArray);

		if( !getCssSourceDirectories().isEmpty() && !getCssExtensions().isEmpty() )
		{
			Collection<File> cssFiles = FileUtil.getFiles(getCssSourceDirectories(), getCssExtensions(),getLog());
			if(!cssFiles.isEmpty())
			{
				String [] arrayCssFiles = new String[cssFiles.size()];
				int i = 0;
				for(File file : cssFiles)
				{
					arrayCssFiles[i++] = file.getAbsolutePath();
				}
				argsToCssClosure = ArrayUtil.concat(arrayCssFiles, argsToCssClosure);
			}
		}
		return argsToCssClosure;
	}

	/**
	 * Map with cssOptions
	 * @param cssOptions
	 * @author Alejandro Vivas
	 * @version 19/08/2017 0.0.1-SNAPSHOT
	 * @since 19/08/2017 0.0.1-SNAPSHOT
	 */
	public void setCssOptions(Map<String, String> cssOptions)
	{
		this.cssOptions = cssOptions;
	}

	/**
	 * Map with cssOptions
	 * @return Map with cssOptions
	 * @author Alejandro Vivas
	 * @version 19/08/2017 0.0.1-SNAPSHOT
	 * @since 19/08/2017 0.0.1-SNAPSHOT
	 */
	public Map<String, String> getCssOptions()
	{
		return cssOptions;
	}

	/**
	 * @param cssArgs the cssArgs to set
	 * @author Alejandro Vivas
	 * @version 21/08/2017 0.0.1-SNAPSHOT
	 * @since 21/08/2017 0.0.1-SNAPSHOT
	 */
	public void setCssArgs(List<String> cssArgs)
	{
		this.cssArgs = cssArgs;
	}

	/**
	 * @return the cssArgs
	 * @author Alejandro Vivas
	 * @version 21/08/2017 0.0.1-SNAPSHOT
	 * @since 21/08/2017 0.0.1-SNAPSHOT
	 */
	public List<String> getCssArgs()
	{
		return cssArgs;
	}

	/**
	 * @author Alejandro Vivas
	 * @version 5/09/2017 0.0.1-SNAPSHOT
	 * @since 5/09/2017 0.0.1-SNAPSHOT
	 * @return the cssSourceDirectories
	 */
	public List<String> getCssSourceDirectories()
	{
		return cssSourceDirectories;
	}

	/**
	 * @author Alejandro Vivas
	 * @version 5/09/2017 0.0.1-SNAPSHOT
	 * @since 5/09/2017 0.0.1-SNAPSHOT
	 * @param cssSourceDirectories the cssSourceDirectories to set
	 */
	public void setCssSourceDirectories(List<String> cssSourceDirectories)
	{
		this.cssSourceDirectories = cssSourceDirectories;
	}

	/**
	 * @author Alejandro Vivas
	 * @version 5/09/2017 0.0.1-SNAPSHOT
	 * @since 5/09/2017 0.0.1-SNAPSHOT
	 * @return the cssExtensions
	 */
	public List<String> getCssExtensions()
	{
		return cssExtensions;
	}

	/**
	 * @author Alejandro Vivas
	 * @version 5/09/2017 0.0.1-SNAPSHOT
	 * @since 5/09/2017 0.0.1-SNAPSHOT
	 * @param cssExtensions the cssExtensions to set
	 */
	public void setCssExtensions(List<String> cssExtensions)
	{
		this.cssExtensions = cssExtensions;
	}

	/**
	 * @author Alejandro Vivas
	 * @version 5/09/2017 0.0.1-SNAPSHOT
	 * @since 5/09/2017 0.0.1-SNAPSHOT
	 * @return the useCssDefaultValues
	 */
	public boolean isUseCssDefaultValues()
	{
		return useCssDefaultValues;
	}

	/**
	 * @author Alejandro Vivas
	 * @version 5/09/2017 0.0.1-SNAPSHOT
	 * @since 5/09/2017 0.0.1-SNAPSHOT
	 * @param useCssDefaultValues the useCssDefaultValues to set
	 */
	public void setUseCssDefaultValues(boolean useCssDefaultValues)
	{
		this.useCssDefaultValues = useCssDefaultValues;
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
