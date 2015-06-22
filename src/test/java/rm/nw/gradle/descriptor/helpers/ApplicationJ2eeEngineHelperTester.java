package rm.nw.gradle.descriptor.helpers;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import rm.nw.gradle.TestUtil;
import rm.nw.gradle.descriptor.helpers.ApplicationJ2eeEngineHelper.Reference;

@RunWith(JUnit4.class)
public class ApplicationJ2eeEngineHelperTester {
  
  public static final File FILE;
  static {
    FILE = new File(ApplicationJ2eeEngineHelperTester.class.getClassLoader().getResource("application-j2ee-engine.xml").getFile());
  }
  
  @Test
  public void testParse() {
    ApplicationJ2eeEngineHelper helper = new ApplicationJ2eeEngineHelper();
    helper.setSourceFile(FILE).parse();
    Assert.assertEquals(8, helper.references.size());
    Assert.assertEquals("engine.security.facade", helper.references.get(0).referenceTarget);
  }
  
  @Test
  public void testParseAndWrite() {
    ApplicationJ2eeEngineHelper helper = new ApplicationJ2eeEngineHelper();
    helper.setSourceFile(FILE).parse();
    final String DEPS = TestUtil.getClasspathFileContent("output/dependencies.txt");
    final String DEPL = TestUtil.getClasspathFileContent("output/dependencyList.txt");
    Assert.assertEquals(DEPS, helper.toDepenencies(null).toString());
    Assert.assertEquals(DEPL, helper.toDepenencyList(null).toString());
  }
  
  @Test
  public void testReferenceToDependenciesItem() {
    Reference reference = new ApplicationJ2eeEngineHelper.Reference();
    reference.providerName = "sap.com";
    reference.referenceTarget = "engine.security.facade";
    //reference.referenceType = ReferenceType.hard;
    //reference.targetType = ReferenceTargetType.service;
    Assert.assertEquals(
        "<dependency  Implementation-Title=\"engine.security.facade\" Implementation-Vendor-Id=\"sap.com\" />", 
        reference.toDependenciesItem(null).toString());
  }
  
  @Test
  public void testReferenceToDependencyListItem() {
    Reference reference = new ApplicationJ2eeEngineHelper.Reference();
    reference.providerName = "sap.com";
    reference.referenceTarget = "engine.security.facade";
    //reference.referenceType = ReferenceType.hard;
    //reference.targetType = ReferenceTargetType.service;
    Assert.assertEquals(
        "<dependency  keyname=\"engine.security.facade\" keyvendor=\"sap.com\" />", 
        reference.toDependencyListItem(null).toString());
  }
  
}
