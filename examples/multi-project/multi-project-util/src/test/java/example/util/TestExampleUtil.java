package example.util;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestExampleUtil {

  @Test
  public void testPositive() {
    Assert.assertEquals(2, ExampleUtil.add(1, 1));
  }

  @Test
  public void testNegative() {
    Assert.assertEquals(-1, ExampleUtil.add(1, -2));
  }

}
