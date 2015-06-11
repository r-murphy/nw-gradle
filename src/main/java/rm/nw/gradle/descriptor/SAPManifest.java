package rm.nw.gradle.descriptor;

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

public class SAPManifest extends DefaultManifest {

  public static final String MANIFEST_DATE_FORMAT = "yyyy.MM.dd.HH.mm.ss";

  private String fileName = "SAP_MANIFEST.MF";

  @Inject
  public SAPManifest(FileResolver fileResolver) {
    super(fileResolver);
    addSapDefaults(getAttributes());
  }

  @Inject
  public SAPManifest(FileResolver fileResolver, Project project) {
    super(fileResolver);
    addSapDefaults(getAttributes());
    updateProjectDetails(project);
  }

  @Inject
  public SAPManifest(Object manifestPath, FileResolver fileResolver) {
    super(manifestPath, fileResolver);
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  protected static void addSapDefaults(Attributes attributes) {
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
