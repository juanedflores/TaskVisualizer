package com.juaned;

import java.io.IOException;
import org.json.JSONException;
import org.junit.Test;

/** Unit test for simple App. */
public class AppTest {
  /**
   * Rigorous Test :-)
   *
   * @throws IOException
   * @throws JSONException
   */
  @Test
  public void testMain() throws JSONException, IOException {
    GUI.main(new String[] {"arg1"});
  }
}
