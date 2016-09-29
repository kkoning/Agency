package agency.data;

import agency.data.ReflectionDataOutput;
import org.junit.Test;

/**
 * Created by liara on 9/27/16.
 */
public class ReflectionDataOutputTest {

@Test
public void testOutput() {

  ReflectionDataOutput rdo = new ReflectionDataOutput();
  rdo.outputFileName = "test.tsv";
  for (int i = 0; i < 5; i++) {
    for (int j = 0; j < 5; j++) {
      DataClass dc = new DataClass();
      dc.x = i;
      dc.y = j;
      dc.test = "The Product is " + (i * j);
      rdo.write(dc);
    }
  }
  rdo.close();

}


public class DataClass {
  public int x;
  public int y;
  String test;
}


}
