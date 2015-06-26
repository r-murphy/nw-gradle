package rm.nw.gradle.descriptor.helpers;

import java.util.ArrayList;
import java.util.List;

public class ManifestStringSplitter {

  private static final int MAX_CHARS = 70;

  /**
   * Splits the string as per the SAP_MANIFEST requirements.
   *   -each line not exceeding MAX_CHARS, including real spaces in the xml
   *   -each new line starting with a space that is not included in the MAX_CHARS count
   *   -there is also a single space at the end. unsure if it counts in the MAX_CHARS.
   *
   *   TODO support input builder and/or writer
   *
   * @param string
   * @return
   */
  public static String splitIt(String string) {
    List<String> parts = getParts(string, MAX_CHARS);
    StringBuilder sb = new StringBuilder(parts.get(0)); //append the first one
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
