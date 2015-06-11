package rm.nw.gradle.descriptor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.gradle.api.java.archives.Attributes;
import org.gradle.api.java.archives.ManifestException;

/**
 * Helper to create the componentelement string in SAP_MANIFEST
 *
 */
public class ComponentElementHelper {

  private static final int MAX_CHARS = 70;
  public static final String UPDATEVERSION_DATE_FORMAT = "yyyyMMddHHmmss";

  /*
     componentelement: <componentelement  name="MTestEAR" vendor="sap.com" componenttype="DC"
     subsystem="NO_SUBSYS" location="localhost" counter="2015.02.18.21.56.
     34" deltaversion="F" updateversion="LB-20150218215635" componentprovid
     er="SAP AG" archivetype="DC"/>
   */

  public static String generate(Attributes attributes) {
    String singleLine = generateSingleLine(attributes);
    return splitIt(singleLine);
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

  /**
   * Splits the componentelement string as per the SAP_MANIFEST requirements.
   *   -each line not exceeding MAX_CHARS, including real spaces in the xml
   *   -each new line starting with a space that is not included in the MAX_CHARS count
   *   -there is also a single space at the end. unsure if it counts in the MAX_CHARS.
   * @param string
   * @return
   */
  private static String splitIt(String string) {
    List<String> parts = getParts(string, MAX_CHARS);
    StringBuilder sb = new StringBuilder(parts.get(0));     //append the first one
    for (int i = 1; i < parts.size(); i++) {
      sb.append("\n ").append(parts.get(i));
    }
    sb.append(" ");
    return sb.toString();
  }

  private static List<String> getParts(String string, int partitionSize) {
    List<String> parts = new ArrayList<String>();
    int len = string.length();
    for (int i=0; i<len; i+=partitionSize) {
      parts.add(string.substring(i, Math.min(len, i + partitionSize)));
    }
    return parts;
  }

}
