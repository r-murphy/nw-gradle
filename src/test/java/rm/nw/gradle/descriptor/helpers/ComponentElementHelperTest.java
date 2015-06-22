package rm.nw.gradle.descriptor.helpers;

import org.gradle.api.java.archives.Attributes;
import org.junit.Test;

import rm.nw.gradle.descriptor.SAPManifest;
import rm.nw.gradle.descriptor.SAPManifestTest;


public class ComponentElementHelperTest {

  @Test
  public void testGenerateWithDefaults() {
    Attributes attributes = generateAttributes();
    String generated = ComponentElementHelper.generate(attributes);
    System.out.println(generated);
//    Assert.assertEquals(EXPECTED, generated);
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
    SAPManifest createSAPManifest = SAPManifestTest.createSAPManifest();
    Attributes attributes = createSAPManifest.getAttributes();
    return attributes;
  }

}
