package rm.nw.gradle.descriptor.helpers;

import java.util.ArrayList;
import java.util.List;

public class ManifestStringSplitter {

  private static final int MAX_CHARS_PER_LINE = 70;
  private static final String LINE_TRAILING = " ";
  private static final String LINE_DELIM = "\n" + LINE_TRAILING;

  /**
   * Splits the string as per the SAP_MANIFEST requirements.
   *   -each line not exceeding MAX_CHARS, including real spaces in the xml
   *   -each new line starting with a space that is not included in the MAX_CHARS count
   *   -there is also a single space at the end. unsure if it counts in the MAX_CHARS
   *
   *   TODO support input builder and/or writer
   *
   * @param string
   * @return
   */
  public static String splitIt(String string) {
    List<String> lines = splitStringBySize(string, MAX_CHARS_PER_LINE);
    return join(lines, LINE_DELIM) + LINE_TRAILING;
  }

  /*
   * Same as StringUtils.join(..)
   */
  private static String join(List<String> list, String delim) {
    if (list.isEmpty()) {
      return "";
    }
    //add the first then loop through the rest starting at i 1
    StringBuilder sb = new StringBuilder(list.get(0));
    for (int i = 1; i < list.size(); i++) {
      sb.append(delim).append(list.get(i));
    }
    return sb.toString();
  }

  //  private static Collection<String> splitStringBySize(String str, int size) {
  //	    ArrayList<String> split = new ArrayList<String>();
  //	    for (int i = 0; i <= str.length() / size; i++) {
  //	        split.add(str.substring(i * size, Math.min((i + 1) * size, str.length())));
  //	    }
  //	    return split;
  //	}

  private static List<String> splitStringBySize(String string, int partitionSize) {
    List<String> parts = new ArrayList<String>();
    int len = string.length();
    for (int i=0; i<len; i+=partitionSize) {
      parts.add(string.substring(i, Math.min(len, i + partitionSize)));
    }
    return parts;
  }

}
