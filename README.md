# NW Gradle
NetWeaver gradle tasks for generating SDA/EAR files. The main purpose of this is to be able to develop outside of NWDS on modern version of eclipse and on non-windows systems. It's main purpose is to build NetWeaver compliant EAR files (SDA files) with proper sda-dd.xml and SAP_MANIFEST.MF files, and packaging the embedded jars just like NWDS does.

## Requirements
- [Java SE](http://www.oracle.com/technetwork/java/javase/overview)
- [Gradle](http://www.gradle.org)
	- Latest version tested on 2.4
	- Original version tested on 2.3

## Using It

First clone this repo

	git clone git@github.com:r-murphy/nw-gradle.git

Then install to local maven:

	cd nw-gradle
	gradle install

Add it to your gradle.build

```groovy
buildscript {
	repositories {
		mavenLocal()
	}
	dependencies {
		classpath(group: 'rm.tools', name: 'nw-gradle', version: '2.+')
	}
}
```

## Plugins

| Plugin  | Automatically Applies  | Creates Task  | Description |
|---------------|----------------|----------------|----------------|
| nw-ear    |   -   |   nwear   |   Creates a NetWeaver   |
| nw-web    |   nw-ear, war, java   |   nwear, war (via war)  |  Creates NetWeaever Web EAR  |
| nw-ejb    |   nw-ear, java   |   nwear, jar (via java)   |  Creates NetWeaver Modle EAR   |


### NW EAR Plugin
The Ear plugin creates a NetWeaver sda/ear file, with sda-dd.xml and SAP_MANIFEST.MF files.
The plugin is can either be used directly on an ear type project, or it can be used indirectly via the NW WEb or NW EJB plugins on their respective projets (see below).

```groovy
project(:MyEarPrj) {
	apply plugin: 'nw-ear'
	...
}
```

The plugin provides the `nwear` task and a corresponding `nwear` configuration. The configuration inherits from the standard EAR convention so it provides all the same configuration options.

 * [Ear DSL](https://docs.gradle.org/current/dsl/org.gradle.plugins.ear.Ear.html).
 * [EarPluginConvention](https://docs.gradle.org/current/dsl/org.gradle.plugins.ear.EarPluginConvention.html).


The NWEar configuration changes the default value for appDirName from "src/main/application" to "EarContent", to be more in line with NWDS/eclipse folder convention. This can be modified in the project configuration.

Additionally, it provides a `sapManifest` closure on the convention to override the default SAP_MANIFEST.MF values. The sapManifest closure inherits from the standard manifest closure, so the same configuration applies.

**Example**

```groovy
ext.vendor = 'me'
nwear {
	destinationDir = rootProject.file('dist')	# instead of within nested project
	archiveName = project.name + '.ear'			# no version
	manifest {
		attributes("Implementation-Vendor-Id": vendor)
	}
	sapManifest {
		attributes("keyvendor": vendor)
	}
	deploymentDescriptor {
		version = 5
	}
	doLast {
		println "---$archivePath"
	}
}
```

### NW Web Plugin
The NW Web plugin is used on a web project to create a war file wrapped in a NW sda/ear file. There will be a one-to-one relationship between war and ear, when using the nwear task. The NW Web plugin will automatically apply the NW EAR plugin, as well as the standard War and Java Plugins.

```groovy
project(:MyWebPrj) {
	apply plugin: 'nw-web'
	...
}
```

**Tasks**
- nwear (from NW Ear Plugin)
- war (from War Plugin)

**Configuration**

Since it applies the NW EAR plugin, the project has the nwear configuration convention, as described above.

And the plugin applies the standard war and java plugins, so all the same DSL configuration options apply. [https://docs.gradle.org/current/dsl/org.gradle.api.tasks.bundling.War.html](https://docs.gradle.org/current/dsl/org.gradle.api.tasks.bundling.War.html) [https://docs.gradle.org/current/dsl/org.gradle.api.tasks.compile.JavaCompile.html](https://docs.gradle.org/current/dsl/org.gradle.api.tasks.compile.JavaCompile.html)

**Example**

```groovy
//These are all optional configurations, except for webXml, which NetWeaver needs.
war {
	archiveName = project.name + '.war'
	webXml = project.file('WebContent/WEB-INF/web.xml')
	destinationDir = rootProject.file('dist')
	rootSpec.eachFile {
	println("\t$it.name")
	}
	doLast {
	println "---$archivePath"
	}
}
```

### NW EJB Plugin
WARNING: Not working. Doesn't generate a proper SAP_MANIFEST.MF yet.

The NW Web plugin is used on a NW EJB project, such as adapter modules. There will be a one-to-one relationship between ejb project and ear, which mimics NWDS deployment. The NW Web plugin will automatically apply the NW EAR plugin, as well as the standard Java Plugin.

```groovy
project(:MyModulePrj) {
	apply plugin: 'nw-ejb'
	...
}
```

## Deployment
Deployment is not handled by these plugins. Maybe in the future. Either deploy via the antique NWDS, via command line on the NetWeaver server, or through other J2EE deployment tools. Personally, I use a customized version of the deployment folder and tools from the NetWeaver server, but NetWeaver should (might?) support any JSR 88 deployment tool (deploytool, ant, Cargo).

### References
Assembling and Deploying J2EE Applications [http://docs.oracle.com/cd/E19253-01/817-6087/dgdeploy.html#wp76734](http://docs.oracle.com/cd/E19253-01/817-6087/dgdeploy.html#wp76734)

Deploying Applications [http://help.sap.com/saphelp_nw74/helpdata/en/4a/f055d4032832c7e10000000a421937/content.htm](http://help.sap.com/saphelp_nw74/helpdata/en/4a/f055d4032832c7e10000000a421937/content.htm)

Using Ant Scripts to Work with SDAs [http://help.sap.com/saphelp_nw74/helpdata/en/4a/f01de74a5a6d62e10000000a42189c/content.htm](http://help.sap.com/saphelp_nw74/helpdata/en/4a/f01de74a5a6d62e10000000a42189c/content.htm)

Using Shell Scripts to Work with SDAs [http://help.sap.com/saphelp_nw74/helpdata/en/4a/f06fb255332475e10000000a42189c/content.htm](http://help.sap.com/saphelp_nw74/helpdata/en/4a/f06fb255332475e10000000a42189c/content.htm)

## Dealing with SAP Jar Dependencies
Although technically out of scope of these plugins, many will wonder how to do a gradle build if your project depends on some SAP (jar) dependencies. There are a few solutions, depending on your preference and project structure.

The first and simplest is to put all the SAP jars into a folder and just use a files or fileTree dependency. I've done this on many occasions and it works pretty well. Although I would recomend against storing the SAP jar files in source control in the same project as your code.

```groovy
dependencies {
	runtime files('libs/a.jar', 'libs/b.jar')
	runtime fileTree(dir: 'libs', include: '*.jar')
}
```

Another solution is to install your SAP jar files into your local maven repo ($USER_HOME/.m2). Here are some sample scripts to do that.

```sh
#!/usr/bin/env bash
file=$1
filename=$(basename $file .jar)
cleanname=$(echo $filename | sed 's/~/-/g')
mvn install:install-file -Dfile="$file" -DgroupId='com.sap.nw' -DartifactId="$cleanname" -Dversion='7.31' -Dpackaging=jar
```

Or many files at once:

```sh
#!/usr/bin/env bash
for file in `find . -name '*.jar' | sort`; do
	filename=$(basename $file .jar)
	cleanname=$(echo $filename | sed 's/~/-/g')
	cmd="mvn install:install-file -Dfile=\"$file\" -DgroupId=\"com.sap.nw\" -DartifactId=\"$cleanname\" -Dversion=\"7.31\" -Dpackaging=jar"
	echo $cmd >> $outputSh
	cmdBat="call $cmd"
	echo $cmdBat >> $outputBat
done
```

I wish I could set up a public maven repo with all the NetWeaver jar files in it, but I'm not sure of the legality of that. So I play it safe and don't put any SAP provided jar files online. Ideally SAP would do that. Although ideally, they would also take a more open approach with the build tools too.

## Example

Check out the example folder for a more in depth example of a multi-project build with a common utility project, a web project and an ejb module project.

## Future Plans / TODO
- More tests
- Get EJB EARs working properly
- Publishing to Maven Central
- default web.xml path in nw-web
- sda-dd.xml filename configuration
- Login Modules
- Resource Adapters
- Maybe deployment

## Contributing
Contributions are welcome.
- Create a branch
- Write the Code.
- Write the Test, if warranted.
- Run `uncrustify.sh` (or similar on Windows).
	- Alternatively, use the Eclipse Formatter from [https://github.com/google/styleguide/blob/gh-pages/eclipse-java-google-style.xml](https://github.com/google/styleguide/blob/gh-pages/eclipse-java-google-style.xml)

- Submit pull request.

## License
See the [LICENSE](LICENSE.md) file for license rights and limitations (MIT).

--------------------------------------------------------------------------------

Copyright (c) 2015 Ryan Murphy.
