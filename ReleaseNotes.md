
# Release Notes

## 1.0.0

* Working web ear and ejb ear builds.

## 1.1.0

* For EJB projects, finds META-INF and adds it to the jar spec.
	* No need to manually configure ejb-j2ee-engine.xml path
* For EJB projects, finds MANIFEST.MF and adds it to the jar.
	* Note that the found one has a higher priority than manifest config in gradle.
* Enhanced examples with META-INF/ejb-j2ee-engine.xml 
