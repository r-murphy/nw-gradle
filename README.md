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
		classpath(group: 'rm.tools', name: 'nw-gradle', version: '1.+')
	}
}
```

## Plugins

| Plugin  | Automatically Applies  | Creates Task  | Description |
|---------------|----------------|----------------|----------------|
| nw-ear    |   -   |   nwear   |   Creates a NetWeaver EAR  |
| nw-web    |   nw-ear, war, java   |   nwear, war (via war)  |  Creates NetWeaver Web EAR  |
| nw-ejb    |   nw-ear, java   |   nwear, jar (via java)   |  Creates NetWeaver Modle EAR   |


### NW EAR Plugin
The Ear plugin creates a NetWeaver sda/ear file, with sda-dd.xml and SAP_MANIFEST.MF files.
The plugin can either be used directly on an ear type project, or it can be used indirectly via the NW Web or NW EJB plugins on their respective projects (see below).

```groovy
project(:MyEarPrj) {
	apply plugin: 'nw-ear'
	...
}
```

The plugin provides the `nwear` task and a corresponding `nwear` configuration. The configuration inherits from the standard EAR convention so it provides all the same configuration options.

References:

 * [Ear DSL](https://docs.gradle.org/current/dsl/org.gradle.plugins.ear.Ear.html)
 * [EarPluginConvention](https://docs.gradle.org/current/dsl/org.gradle.plugins.ear.EarPluginConvention.html)


The NWEar configuration changes the default value for appDirName from "src/main/application" to "EarContent", to be more in line with NWDS/eclipse folder convention. This can be modified in the project configuration.

Additionally, it provides some additional configuration methods (closures).

	* `sapManifest` closure on the convention to override the default SAP_MANIFEST.MF values. The sapManifest closure inherits from the standard manifest closure, so the same configuration applies (manifest is incubating in gradle so it's not document yet)
	* 'sdaDD' closure to configure the SDA Deployment Descriptor file. Currently the only option is override the fileName value.

**Example**

```groovy
ext.vendor = 'me'
nwear {
	//standard ear configuration options
	destinationDir = rootProject.file('dist')	# instead of within nested project
	archiveName = project.name + '.ear'			# no version
	manifest {
		attributes("Implementation-Vendor-Id": vendor)
	}
	deploymentDescriptor {
		version = 5
	}
	//custom nw ear configuration options
	sapManifest {
		attributes("keyvendor": vendor)
	}
	sdaDD {
		fileName = 'sda-dd.xml' //this is the default
	}
}
```

### NW Web Plugin
The NW Web plugin is used on a web project to create a war file wrapped in a NW sda/ear file. The NW Web plugin will automatically apply the NW EAR plugin, as well as the standard War and Java Plugins. There is one-to-one relationship between war and ear, when using the nwear task.

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

And the plugin applies the standard war and java plugins, so all the same DSL configuration options apply.

 * [War DSL](https://docs.gradle.org/current/dsl/org.gradle.api.tasks.bundling.War.html)
 * [JavaCompile DSL](https://docs.gradle.org/current/dsl/org.gradle.api.tasks.compile.JavaCompile.html)

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
The NW Web plugin is used on a NW EJB project, such as adapter modules. The NW EJB plugin will automatically apply the NW EAR plugin, as well as the standard Java Plugin. There will be a one-to-one relationship between ejb project and ear, which mimics NWDS deployment.

The EJB Plugin relies on the standard jar task to create the ejb jar file.
The plugin will search the source files to discover the META-INF folder to add it to the jar file as well. This typically contains the ejb-j2ee-engine.xml file and the MANIFEST.MF file. Note that normally, gradle will exclude any found MANIFEST.MF files and build its own MANIFEST.MF file, allowing its parameters to be configured in the build.gradle script. However this plugin reverses that priority in order since source files should have a higher priority. This enables better cross-compatibility with NWDS, if needed.

```groovy
project(:MyModulePrj) {
	apply plugin: 'nw-ejb'
	...
}
```

## Example

Check out the example folder for a more in depth example of a multi-project build with a common utility project, a web project and an ejb module project.

## Deployment

See [docs/deployment.md](docs/deployment.md)

## Dealing with SAP Jar Dependencies

See [docs/sap-jar-dependencies.md](docs/sap-jar-dependencies.md)

## Future Plans / TODO
- More tests
- Publishing to Maven Central
- See Issues list for more
- Maybe deployment

## Contributing
Contributions are welcome.
- Create a branch
- Write the Code.
- Write the Test, if warranted.
- Run `uncrustify.sh` (or similar on Windows).
	- Alternatively, use the Eclipse Formatter from [https://github.com/google/styleguide/blob/gh-pages/eclipse-java-google-style.xml](https://github.com/google/styleguide/blob/gh-pages/eclipse-java-google-style.xml)

- Submit pull request.

## Release Notes

See [ReleaseNotes.md](ReleaseNotes.md)

## License
See the [LICENSE](LICENSE.md) file for license rights and limitations (MIT).

--------------------------------------------------------------------------------

Copyright (c) 2015 Ryan Murphy.
