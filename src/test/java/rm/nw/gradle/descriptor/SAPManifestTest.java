package rm.nw.gradle.descriptor;

import java.io.File;
import java.io.StringWriter;

import org.eclipse.jdt.internal.core.Assert;
import org.gradle.api.Project;
import org.gradle.api.internal.file.FileResolver;
import org.gradle.api.internal.file.IdentityFileResolver;
import org.gradle.api.java.archives.ManifestException;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Test;
import org.gradle.internal.nativeintegration.services.NativeServices;

import rm.nw.gradle.descriptor.SAPManifest;

public class SAPManifestTest {

  static {
    NativeServices.initialize(getUserDirectoryPath());
  }

  //from Commons IO FileUtils 2.x, but gradle api uses 1.4
  public static File getUserDirectoryPath() {
    return new File(System.getProperty("user.home"));
  }

  @Test(expected=ManifestException.class)
  public void testWithDefaults() {
    FileResolver fileResolver = new IdentityFileResolver();
    SAPManifest sapManifest = new SAPManifest(fileResolver);
    StringWriter writer = new StringWriter();
    sapManifest.writeTo(writer);
  }

  @Test
  public void testWithProject() {
    FileResolver fileResolver = new IdentityFileResolver();
    SAPManifest sapManifest = new SAPManifest(fileResolver);
    Project project = ProjectBuilder.builder().withName("MyTestProject").build();
    project.setGroup("example.com");
    sapManifest.updateProjectDetails(project);
    StringWriter writer = new StringWriter();
    sapManifest.writeTo(writer);
    String result = writer.toString();
    //System.out.println(result);
    assertContains("keyname: MyTestProject", result);
    assertContains("keyvendor: example.com", result);
    assertContains("deployfile: sda-dd.xml", result);
  }

  public void assertContains(String expected, String in) {
    if (!in.contains(expected)) {
      throw new Assert.AssertionFailedException(expected + "not found in string");
    }
  }

}
