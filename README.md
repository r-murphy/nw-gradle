# NW Gradle

NetWeaver gradle tasks for generating SDA/EAR files.

The main purpose of this is to be able to develop outside of NWDS on modern version of eclipse and on non-windows systems.

## System Requirements

- [Java SE](http://www.oracle.com/technetwork/java/javase/overview)
- [Gradle](http://www.gradle.org)
	- Latest version tested on 2.4
	- Original version tested on 2.3

## Using It

### NW EAR Plugin

The Ear plugin creates a NetWeaver sda/ear file, with sda-dd.xml and SAP_MANIFEST.MF files.

The plugin is typically not used directly. Instead, it's typically used via the NW WEb or NW EJB plugins.

The plugin provides the `nwear` task and a corresponding `nwear` configuration.
The configuration inherits from the standard EAR convention so it provides all the same configuration options (https://docs.gradle.org/current/dsl/org.gradle.plugins.ear.Ear.html).

Additionally, it provides a `sapManifest` closure on the convention to override the default SAP_MANIFEST.MF values. The sapManifest closure inherits from the standard manifest closure of the ear and jar configurations, so the same configuration applies.

** Example **

```groovy
ext.vendor = 'me'
nwear {
    destinationDir = rootProject.file('dist')	# instead of within nested project
    archiveName = project.name + '.ear'      	# no version
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

The NW Web plugin is used on a web project to create a war file wrapped in a NW sda/ear file.
There will be a one-to-one relationship between war and ear, which mimics NWDS deployment.

```groovy
project(:MyWebPrj) {
	apply plugin: 'rm.nw.gradle.web'
	...
}
```

The NW Web plugin will automatically apply the NW EAR plugin, as well as the standard War Plugin, and also the Java Plugin indirectly.

** Key Tasks **

 * war (from War Plugin)
 * nwear (from NW Ear Plugin)

** Configuration **

Since it applies the NW EAR plugin, the project has the nwear configuration convention, as described above.

And the plugin applies the standard war and java plugins, so all the same DSL configuration options apply.
https://docs.gradle.org/current/dsl/org.gradle.api.tasks.bundling.War.html
https://docs.gradle.org/current/dsl/org.gradle.api.tasks.compile.JavaCompile.html

Example:

```groovy
//These are all optional configurations.
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

The NW Web plugin is used on a NW EJB project, such as adapter modules.
There will be a one-to-one relationship between ejb project and ear, which mimics NWDS deployment.

```groovy
project(:MyModulePrj) {
	apply plugin: 'rm.nw.gradle.ejb'
	...
}
```

The NW Web plugin will automatically apply the NW EAR plugin, as well as the standard War Plugin, and also the Java Plugin indirectly.

## Deployment

Deployment is not handled by these plugins. Maybe in the future. Either deploy via the antique NWDS, via command line on the NetWeaver server, or through other J2EE deployment tools.
Personally, I use a customized version of the deployment folder and tools from the NetWeaver server, but NetWeaver should (might?) support any JSR 88 deployment tool (deploytool, ant, Cargo).

### References

Assembling and Deploying J2EE Applications
http://docs.oracle.com/cd/E19253-01/817-6087/dgdeploy.html#wp76734

Deploying Applications
http://help.sap.com/saphelp_nw74/helpdata/en/4a/f055d4032832c7e10000000a421937/content.htm

Using Ant Scripts to Work with SDAs
http://help.sap.com/saphelp_nw74/helpdata/en/4a/f01de74a5a6d62e10000000a42189c/content.htm

Using Shell Scripts to Work with SDAs
http://help.sap.com/saphelp_nw74/helpdata/en/4a/f06fb255332475e10000000a42189c/content.htm


## Future Plans

* Get EJB EARs working properly
* Login Modules
* Resource Adapters
* Maybe deployment

## Contributing

Contributions are welcome.

* Create a branch
* Write the Code.
* Write the Test, if warranted.
* Run `uncrustify.sh` (or similar on Windows).
	* Alternatively, use the Eclipse Formatter from https://github.com/google/styleguide/blob/gh-pages/eclipse-java-google-style.xml
* Submit pull request.

## License

See the [LICENSE](LICENSE.md) file for license rights and limitations (MIT).

---

Copyright (c) 2015 Ryan Murphy.
