# SAP Jar Dependencies

Although technically out of scope of these plugins, many will wonder how to do a gradle build if your project depends on some SAP (jar) dependencies. There are a few solutions, depending on your preference and project structure.

The first and simplest is to put all the SAP jars into a folder and just use a files or fileTree dependency. I've done this on many occasions and it works pretty well. Although I would recommend against storing the SAP jar files in source control in the same project as your code.

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
