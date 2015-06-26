package rm.nw.gradle.descriptor;

import java.io.IOException;
import java.io.Writer;

import org.gradle.api.UncheckedIOException;
import org.gradle.api.internal.file.FileResolver;
import org.gradle.internal.reflect.Instantiator;
import org.gradle.plugins.ear.descriptor.internal.DefaultDeploymentDescriptor;

public class SdaDD extends DefaultDeploymentDescriptor {

  public SdaDD(FileResolver arg0, Instantiator arg1) {
    super(arg0, arg1);
  }

  private String fileName = "sda-dd.xml";

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public String getFileName() {
    return fileName;
  }

  public SdaDD writeTo(Writer writer) {
    //PrintWriter printWriter = new PrintWriter(writer);
    try {
      writer.write(XML_STR);
      writer.flush();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return this;
  }

  private static final String XML_STR = "<SDA>"
                                        +"\n <SoftwareType>J2EE</SoftwareType>"
                                        +"\n <engine-deployment-descriptor version=\"2.0\">"
                                        +"\n    <substitution-variable>"
                                        +"\n      <variable-name>com.sap.dc_name</variable-name>"
                                        +"\n    </substitution-variable>"
                                        +"\n    <substitution-variable>"
                                        +"\n      <variable-name>com.sap.dc_vendor</variable-name>"
                                        +"\n    </substitution-variable>"
                                        +"\n    <substitution-variable>"
                                        +"\n      <variable-name>com.sap.sld.GatewayHost</variable-name>"
                                        +"\n    </substitution-variable>"
                                        +"\n    <substitution-variable>"
                                        +"\n      <variable-name>com.sap.sld.GatewayService</variable-name>"
                                        +"\n    </substitution-variable>"
                                        +"\n </engine-deployment-descriptor>"
                                        +"\n</SDA>";

}
