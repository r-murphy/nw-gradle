package rm.nw.gradle.descriptor;

import java.io.File;
import java.io.StringWriter;

import org.eclipse.jdt.internal.core.Assert;
import org.gradle.api.Project;
import org.gradle.api.internal.file.FileResolver;
import org.gradle.api.internal.file.IdentityFileResolver;
import org.gradle.api.java.archives.ManifestException;
import org.gradle.internal.nativeintegration.services.NativeServices;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Test;

import rm.nw.gradle.descriptor.helpers.ApplicationJ2eeEngineHelperTester;

public class SAPManifestTest {

  static {
    NativeServices.initialize(getUserDirectoryPath());
  }

  //from Commons IO FileUtils 2.x, but gradle api uses 1.4
  public static File getUserDirectoryPath() {
    return new File(System.getProperty("user.home"));
  }

  //used by other helper testers
  public static SAPManifest createSAPManifest() {
    FileResolver fileResolver = new IdentityFileResolver();
    SAPManifest sapManifest = new SAPManifest(fileResolver);
    return sapManifest;
  }

  @Test(expected=ManifestException.class)
  public void testWithDefaults() {
    StringWriter writer = new StringWriter();
    //ManifestException: keyname needs to be set before getting effective manifest
    createSAPManifest().writeTo(writer);
  }

  @Test
  public void testWithProject() {
    SAPManifest sapManifest = createSAPManifest();
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

  @Test
  public void testWithDependencies() {
    SAPManifest sapManifest = createSAPManifest();
    sapManifest.setApplicationJ2eeEngineFile(ApplicationJ2eeEngineHelperTester.FILE);
    sapManifest.setIncludeDependencies(true);

    Project project = ProjectBuilder.builder().withName("MyTestProject").build();
    project.setGroup("example.com");
    sapManifest.updateProjectDetails(project);

    StringWriter writer = new StringWriter();
    sapManifest.writeTo(writer);
    String result = writer.toString();
    //System.out.println(result);

    assertContains("dependencies: <dependency  Implementation-Title=\"engine.security.facade\" Implementat", result);
    assertContains("dependencyList: <dependency  keyname=\"engine.security.facade\" keyvendor=\"sap.com\" /> <", result);
  }

  @Test
  //Fixes Issue#8
  public void testWithNoDependencies() {
    SAPManifest sapManifest = createSAPManifest();
    sapManifest.setApplicationJ2eeEngineFile(ApplicationJ2eeEngineHelperTester.FILE_NO_REFS);
    sapManifest.setIncludeDependencies(true);

    Project project = ProjectBuilder.builder().withName("MyTestProject").build();
    project.setGroup("example.com");
    sapManifest.updateProjectDetails(project);

    StringWriter writer = new StringWriter();
    sapManifest.writeTo(writer);
    String result = writer.toString();

    assertDoesNotContain("dependencies", result);
    assertDoesNotContain("dependencyList", result);
  }

  public void assertContains(String expected, String in) {
    if (!in.contains(expected)) {
      throw new Assert.AssertionFailedException("'" + expected + "' not found in string");
    }
  }

  public void assertDoesNotContain(String substring, String in) {
    if (in.contains(substring)) {
      throw new Assert.AssertionFailedException("'" + substring + "' was found in string");
    }
  }

}
