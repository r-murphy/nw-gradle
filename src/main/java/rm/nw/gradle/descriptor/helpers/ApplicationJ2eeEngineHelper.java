package rm.nw.gradle.descriptor.helpers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

//<application-j2ee-engine xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="application-j2ee-engine.xsd">
//  <reference 
//    reference-type="hard">
//    <reference-target 
//      provider-name="sap.com" 
//      target-type="library">com.sap.aii.af.lib.facade</reference-target>
//  </reference>
//  ...
//</application-j2ee-engine>

public class ApplicationJ2eeEngineHelper {
  
  List<Reference> references = new ArrayList<Reference>();
  File sourceFile;
  
  public List<Reference> getReferences() {
    return references;
  }
  
  public ApplicationJ2eeEngineHelper setSourceFile(File sourceFile) {
    this.sourceFile = sourceFile;
    return this;
  }
  
  /**
   * Parse the xml doc from source file.
   */
  public ApplicationJ2eeEngineHelper parse() {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.parse(sourceFile);
      NodeList nodeList = document.getElementsByTagName("reference");
      for (int i = 0; i < nodeList.getLength(); i++) {
        Reference reference = parseReference((Element)nodeList.item(i));
        references.add(reference);
      }
    }
    catch (Exception e) {
      if (e instanceof RuntimeException) {
        throw (RuntimeException)e;
      }
      throw new RuntimeException(e);
    }
    return this;
  }
  
  /*
   * Parse individual reference xml element to Reference class 
   */
  private static Reference parseReference(Element referenceEl) {
    Reference reference = new Reference();
    NodeList targets = referenceEl.getElementsByTagName("reference-target");
    if (targets.getLength() != 1) {
      throw new RuntimeException("Expected 1 reference-target in reference, but instead found " + targets.getLength());
    }
    Element refTarget = (Element)targets.item(0);
    reference.providerName = refTarget.getAttribute("provider-name");
    reference.referenceTarget = refTarget.getTextContent();
    return reference;
  }
  
  /**
   * Create the 'dependencies' string.
   * No splitting or trailing space at the end. Both done later if needed. 
   * @param builder - optional build to append to
   * @return StringBuilder
   */
  public StringBuilder toDepenencies(StringBuilder builder) {
    if (builder == null) {
      builder = new StringBuilder();
    }
    boolean first = true;
    for (Reference reference : references) {
      if (first) {
        first = false;
      }
      else {
        builder.append(" ");
      }
      reference.toDependenciesItem(builder);
    }
    return builder;
  }
  
  /**
   * Create the 'dependencyList' string.
   * No splitting or trailing space at the end. Both done later if needed. 
   * @param builder - optional build to append to
   * @return StringBuilder
   */
  public StringBuilder toDepenencyList(StringBuilder builder) {
    if (builder == null) {
      builder = new StringBuilder();
    }
    boolean first = true;
    for (Reference reference : references) {
      if (first) {
        first = false;
      }
      else {
        builder.append(" ");
      }
      reference.toDependencyListItem(builder);
    }
    return builder;
  }
  
//  public static enum ReferenceType {
//    hard, soft;
//  }
//  
//  public static enum ReferenceTargetType {
//    service, library; //Other?
//  }
  
  public static class Reference {
//    ReferenceType referenceType;
    String providerName;
//    ReferenceTargetType targetType;
    String referenceTarget;
    
    @Override
    public String toString() {
      //return referenceType + ":" + targetType + ":" + providerName + ":" + referenceTarget; 
      return providerName + ":" + referenceTarget;
    }
    
    public StringBuilder toDependenciesItem(StringBuilder builder) {
      //<dependency  Implementation-Title="engine.security.facade" Implementation-Vendor-Id="sap.com" />
      if (builder==null) {
        builder = new StringBuilder();
      }
      builder.append("<dependency  Implementation-Title=\"").append(referenceTarget)
        .append("\" Implementation-Vendor-Id=\"").append(providerName)
        .append("\" />");
      return builder;
    }
    
    public StringBuilder toDependencyListItem(StringBuilder builder) {
      //<dependency  keyname="engine.security.facade" keyvendor="sap.com" />
      if (builder==null) {
        builder = new StringBuilder();
      }
      builder.append("<dependency  keyname=\"").append(referenceTarget)
        .append("\" keyvendor=\"").append(providerName)
        .append("\" />");
      return builder;
    }
    
  }
  
}
