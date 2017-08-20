package com.bachue.closuremavenplugin;

import java.security.Permission;

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

import com.google.common.css.compiler.commandline.ClosureCommandLineCompiler;

/**
 * CSS Mojo class
 * @author Alejandro Vivas
 * @version 20/08/2017 0.0.1-SNAPSHOT
 * @since 19/08/2017 0.0.1-SNAPSHOT
 */
@Mojo(name = "css", defaultPhase = LifecyclePhase.COMPILE)
public class CSSClosureMojo extends AbstractMojo
{
	/** Map with options to */
	@Parameter(property = "cssOptions", required = true)
	private Map<String, String> cssOptions;

	/**
	 * Class to avoid System.exit call of closure Stylesheet
	 * @author Alejandro Vivas
	 * @version 20/08/2017 0.0.1-SNAPSHOT
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
	 * @version 19/08/2017 0.0.1-SNAPSHOT
	 * @since 19/08/2017 0.0.1-SNAPSHOT
	 */
	public void execute() throws MojoExecutionException
	{
		if (getCssOptions().size() == 0)
		{
			throw new MojoExecutionException("Empty css-options");
		}

		String[] args = OptionsUtil.optionsToStringArray(getCssOptions());

		getLog().info("Options to css closure:" + StringUtils.join(args, " "));

		// Change security manager to avoid System.exit
		SecurityManager securityManagerOriginal = System.getSecurityManager();
		System.setSecurityManager(new InternalSecurityManager());
		try
		{
			// Run closure stylesheet
			ClosureCommandLineCompiler.main(args);
		}
		catch (SecurityException e) 
		{
			// Return original security manager
			System.setSecurityManager(securityManagerOriginal);
		}
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
}
