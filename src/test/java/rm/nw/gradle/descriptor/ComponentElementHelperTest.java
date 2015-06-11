package rm.nw.gradle.descriptor;

import org.gradle.api.java.archives.Attributes;
import org.gradle.api.java.archives.internal.DefaultAttributes;
import org.junit.Test;

import rm.nw.gradle.descriptor.ComponentElementHelper;
import rm.nw.gradle.descriptor.SAPManifest;


public class ComponentElementHelperTest {

  @Test
  public void testGenerateWithDefaults() {
    Attributes attributes = generateAttributes();
    String generated = ComponentElementHelper.generate(attributes);
    System.out.println(generated);
    //Assert.assertEquals(EXPECTED, generated);
  }

  @Test
  public void testGenerateWithProject() {
    Attributes attributes = generateAttributes();
    attributes.put("keyname", "MyTestProject");
    attributes.put("keyvendor", "example.com");
    String generated = ComponentElementHelper.generate(attributes);
    System.out.println(generated);
    //Assert.assertEquals(EXPECTED, generated);
  }

  private Attributes generateAttributes() {
    Attributes attributes = new DefaultAttributes();
    SAPManifest.addSapDefaults(attributes);
    return attributes;
  }

}
