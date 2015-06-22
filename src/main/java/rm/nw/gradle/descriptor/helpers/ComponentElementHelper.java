package rm.nw.gradle.descriptor.helpers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.gradle.api.java.archives.Attributes;
import org.gradle.api.java.archives.ManifestException;

import rm.nw.gradle.descriptor.SAPManifest;

/**
 * Helper to create the componentelement string in SAP_MANIFEST
 *
 */
public class ComponentElementHelper {

  public static final String UPDATEVERSION_DATE_FORMAT = "yyyyMMddHHmmss";
  
  /*
     componentelement: <componentelement  name="MTestEAR" vendor="sap.com" componenttype="DC"
     subsystem="NO_SUBSYS" location="localhost" counter="2015.02.18.21.56.
     34" deltaversion="F" updateversion="LB-20150218215635" componentprovid
     er="SAP AG" archivetype="DC"/>
   */

  public static String generate(Attributes attributes) {
    String singleLine = generateSingleLine(attributes);
    return ManifestStringSplitter.splitIt(singleLine);
  }

  private static String generateSingleLine(Attributes attributes) {
    StringBuilder sb = new StringBuilder("<componentelement ");     //intentional extra space
    appendAttribute(sb, "name", attributes.get("keyname"));
    appendAttribute(sb, "vendor", attributes.get("keyvendor"));
    appendAttribute(sb, "componenttype", attributes.get("archivetype"));
    appendAttribute(sb, "subsystem", "NO_SUBSYS");
    appendAttribute(sb, "location", attributes.get("keylocation"));
    appendAttribute(sb, "counter", attributes.get("keycounter"));
    appendAttribute(sb, "deltaversion", "F");
    appendAttribute(sb, "updateversion", generateUpdateVersion(attributes));
    appendAttribute(sb, "componentprovider", "SAP AG");
    appendAttribute(sb, "archivetype", attributes.get("archivetype"));
    sb.append("/>");

    return sb.toString();
  }

  private static final String generateUpdateVersion(Attributes attributes) {
    try {
      String manifestDate = (String)attributes.get("keycounter");
      if (manifestDate==null) {
        throw new ManifestException("keycounter not found in SAP_MANIFEST attributes");
      }
      Date date = new SimpleDateFormat(SAPManifest.MANIFEST_DATE_FORMAT).parse(manifestDate);
      return "LB-" + (new SimpleDateFormat(UPDATEVERSION_DATE_FORMAT)).format(date);
    } catch (ParseException e) {
      throw new ManifestException(e.getMessage(), e);
    }
  }

  private static final void appendAttribute(StringBuilder sb, String name, Object value) {
    //i.e. '  vendor="sap.com"'
    sb.append(" ").append(name).append("=\"").append(value).append("\"");
  }

}
