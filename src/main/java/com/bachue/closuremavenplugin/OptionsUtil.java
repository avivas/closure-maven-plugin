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

/**
 * Utility class
 * @author Alejandro Vivas
 * @version 19/08/2017 0.0.1-SNAPSHOT
 * @since 19/08/2017 0.0.1-SNAPSHOT
 */
public final class OptionsUtil
{
	/**
	 * To avoid instances
	 * @author Alejandro Vivas
	 * @version 19/08/2017 0.0.1-SNAPSHOT
	 * @since 19/08/2017 0.0.1-SNAPSHOT
	 */
	private OptionsUtil()
	{
	}

	/**
	 * Convert a map to String array
	 * @param options Map with options
	 * @return String array with options
	 */
	public static String[] optionsToStringArray(Map<String, String> options)
	{
		StringBuilder stringBuilder = new StringBuilder();
		for (String option : options.keySet())
		{
			String value = options.get(option);
			if(!option.equals("arg"))
			{
				stringBuilder.append("--");
				stringBuilder.append(option);
			}
			stringBuilder.append(" ");
			if(value != null)
			{
				stringBuilder.append(value);
			}
			stringBuilder.append(" ");
		}
		return stringBuilder.toString().trim().replace('\n',' ').split(" ");
	}
}
