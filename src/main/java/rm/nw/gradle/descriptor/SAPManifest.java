package rm.nw.gradle.descriptor;

import java.io.File;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.inject.Inject;

import org.gradle.api.Project;
import org.gradle.api.internal.file.FileResolver;
import org.gradle.api.java.archives.Attributes;
import org.gradle.api.java.archives.ManifestException;
import org.gradle.api.java.archives.internal.DefaultManifest;

import rm.nw.gradle.descriptor.helpers.ApplicationJ2eeEngineHelper;
import rm.nw.gradle.descriptor.helpers.ComponentElementHelper;
import rm.nw.gradle.descriptor.helpers.ManifestStringSplitter;

public class SAPManifest extends DefaultManifest {
  
  public static final String MANIFEST_DATE_FORMAT = "yyyy.MM.dd.HH.mm.ss";

  private String fileName = "SAP_MANIFEST.MF";
  
  private File applicationJ2eeEngineFile; //file path
  
  private boolean includeDependencies = false;
  
  @Inject
  public SAPManifest(FileResolver fileResolver) {
    super(fileResolver);
    addSapDefaults();
  }

  @Inject
  public SAPManifest(FileResolver fileResolver, Project project) {
    super(fileResolver);
    addSapDefaults();
    updateProjectDetails(project);
  }

  @Inject
  public SAPManifest(Object manifestPath, FileResolver fileResolver) {
    super(manifestPath, fileResolver);
  }
  
  public void setIncludeDependencies(boolean includeDependencies) {
    this.includeDependencies = includeDependencies;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }
  
  public void setApplicationJ2eeEngineFile(File applicationJ2eeEngineFile) {
    this.applicationJ2eeEngineFile = applicationJ2eeEngineFile;
  }

  public void addSapDefaults() {
    Attributes attributes = getAttributes();
    attributes.put("Ext-SDM-SDA-Comp-Version", "1");
    attributes.put("softwaretype", "J2EE");
    attributes.put("JarSAP-Standalone-Version", "20090803.1000");
    attributes.put("JarSAPProcessing-Version", "20090907.1000");
    attributes.put("deployfile", "sda-dd.xml");
    attributes.put("archivetype", "DC");
    attributes.put("JarSAP-Version", "20090803.1000");
    attributes.put("keyname", "");     //placeholder in (ordered) LinkedHashMap
    attributes.put("keyvendor", "sap.com");
    attributes.put("keylocation", "localhost");
    attributes.put("keycounter", generateManifestDate());
    attributes.put("componentelement", "");     //placeholder in (ordered) LinkedHashMap
    attributes.put("dependencies", ""); //placeholder in (ordered) LinkedHashMap
    attributes.put("dependencylist", ""); //placeholder in (ordered) LinkedHashMap
    attributes.put("JarSL-Version", "20100616.1800");
    attributes.put("compress", "true");
  }

  private static String generateManifestDate() {
    return new SimpleDateFormat(MANIFEST_DATE_FORMAT).format(new Date());
  }

  public void updateProjectDetails(final Project project) {
    Attributes attributes = getAttributes();
    attributes.put("keyname", project.getName());
    attributes.put("keyvendor", project.getGroup().toString());
  }

  @Override
  public DefaultManifest getEffectiveManifest() {
    Attributes attributes = getAttributes();

    String keyname = (String)attributes.get("keyname");
    if (keyname==null || keyname.isEmpty()) {
      throw new ManifestException("keyname needs to be set before getting effective manifest");
    }

    attributes.put("componentelement", ComponentElementHelper.generate(attributes));
    
    System.out.println("SAPManifest.getEffectiveManifest~includeDependencies:" +includeDependencies);
    System.out.println("SAPManifest.getEffectiveManifest~applicationJ2eeEngineFile:" +applicationJ2eeEngineFile);
    
    if (includeDependencies && applicationJ2eeEngineFile!=null) {
      //deferred application-j2ee-engine.xml parse
      final ApplicationJ2eeEngineHelper applicationJ2eeEngineHelper = new ApplicationJ2eeEngineHelper();
      applicationJ2eeEngineHelper.setSourceFile(applicationJ2eeEngineFile).parse();
      
      String dependencies = applicationJ2eeEngineHelper.toDepenencies(null).toString();
      attributes.put("dependencies", ManifestStringSplitter.splitIt(dependencies));
      System.out.println(ManifestStringSplitter.splitIt(dependencies));
      
      String dependencyList = applicationJ2eeEngineHelper.toDepenencyList(null).toString();
      attributes.put("dependencyList", ManifestStringSplitter.splitIt(dependencyList));
      System.out.println(ManifestStringSplitter.splitIt(dependencyList));
    }
    else {
      attributes.remove("dependencies");
      attributes.remove("dependencyList");
    }
    
    //System.out.println("SAPManifest.getEffectiveManifest() - attributes.get("componentelement"));
    return super.getEffectiveManifest();
  }

  @Override
  public DefaultManifest writeTo(Writer writer) {
    Attributes attributes = getEffectiveManifest().getAttributes();
    PrintWriter printWriter = new PrintWriter(writer);
    for (Map.Entry<String, Object> entry : attributes.entrySet()) {
      printWriter.println(entry.getKey() + ": " + entry.getValue());
    }
    printWriter.println();
    printWriter.flush();
    return this;
  }

}
