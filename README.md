# Google Closure Maven Plugin

Google closure ( [stylesheets](https://github.com/google/closure-stylesheets) and [compiler](https://github.com/google/closure-compiler) ) maven plugin. 

## Features

* Support all options closure
* Support all closure versions

## Installing

```
git clone https://github.com/avivas/closure-maven-plugin.git
cd closure-maven-plugin
mvn install
```

## Pluging goals

Goal | Description
------------ | -------------
css | Run Google closure stylesheets
js | Run Google closure compiler


## Usage

Add plugin to build section:

```xml
<build>
  <plugins>
    <plugin>
      <groupId>com.bachue</groupId>
        <artifactId>closure-maven-plugin</artifactId>
        <version>0.0.1-SNAPSHOT</version>
        <configuration>
          <jsOptions>
            <!-- Put js options -->
          </jsOptions>
          <cssOptions>
            <!-- Put css options -->					
          </cssOptions>
        </configuration>
        <executions>
          <execution>
            <id>min-css</id>
            <phase>package</phase>
            <goals>
              <goal>css</goal>
              <goal>js</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
   </plugins>
</build>
```

Put in jsOptions and cssOptions where for example if you want to execute:
```
java -jar closure-compiler-v20170806.jar --js file.js --js_output_file file.js
java -jar closure-stylesheets.jar file.css --output-file file.min.css
```
Then you need to configure:

```xml
<build>
  <plugins>
    <plugin>
      <groupId>com.bachue</groupId>
        <artifactId>closure-maven-plugin</artifactId>
        <version>0.0.1-SNAPSHOT</version>
        <configuration>
          <jsOptions>
            <js>${project.basedir}/src/main/javascript/file.js</js>
            <js_output_file>${project.build.directory}/min-resources/javascript/file.min.js</js_output_file>
            <compilation_level>SIMPLE</compilation_level>
          </jsOptions>
          <cssOptions>
            <output-file>${project.basedir}/src/main/css/file.min.css</output-file>
            <arg>${project.build.directory}/min-resources/css/file.css</arg>						
          </cssOptions>
        </configuration>
        <executions>
          <execution>
            <id>min-css</id>
            <phase>package</phase>
            <goals>
              <goal>css</goal>
              <goal>js</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
   </plugins>
</build>
```

### Note:
You need to use ${project.basedir} for example or you need to use abosolute a path.


## Change closure version

This configuration use closure-compiler version v20160822 and closure-stylesheets version 1.4.0

```
<build>
  <plugins>
    <plugin>
      <groupId>com.bachue</groupId>
      <artifactId>closure-maven-plugin</artifactId>
      <version>0.0.1-SNAPSHOT</version>
      <dependencies>
        <dependency>
          <groupId>com.google.javascript</groupId>
          <artifactId>closure-compiler</artifactId>
          <version>v20160822</version>
        </dependency>
	<dependency>
	  <groupId>com.google.closure-stylesheets</groupId>
	  <artifactId>closure-stylesheets</artifactId>
	  <version>1.4.0</version>
	</dependency>
      </dependencies>
    </plugin>
  </plugins>
</build>
```

## Issues

If you feel to add any feature you can open issue in https://github.com/avivas/closure-maven-plugin/issues/ and we will try to address it as soon as possible

## Authors

* **Alejandro Vivas** - *Initial work* - [avivas](https://github.com/avivas)

See also the list of [contributors](https://github.com/avivas/closure-maven-plugin//contributors) who participated in this project.

## License

This project is licensed under the Apache License Version 2.0 MIT License - see the [LICENSE.txt](LICENSE.txt) file for details
