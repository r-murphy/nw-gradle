package rm.nw.gradle;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.io.FileUtils;


public class TestUtil {

  public static String getClasspathFileContent(String path) {
    try {
      URL resource = TestUtil.class.getClassLoader().getResource(path);
      //String file = TestUtil.class.getResource(path).getFile();
      return FileUtils.readFileToString(new File(resource.toURI()));
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
    catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

}
