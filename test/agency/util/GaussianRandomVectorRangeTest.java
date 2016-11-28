package agency.util;

import agency.Config;
import agency.XMLConfigurable;
import org.junit.Test;

import java.util.Arrays;

/**
 * Created by liara on 11/5/16.
 */
public class GaussianRandomVectorRangeTest {

@Test
public void basicTest() {
  String xmlString = String.join("\n",
          "<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
          "<RangedVector length=\"5\">",
          "<GaussianRandomVectorRange start=\"0\" end=\"4\">",
          "<Means type=\"DoubleList\" start=\"0\" end=\"4\">1.1 2.2 3.3 4.4 5.5</Means>",
          "<Deviations type=\"DoubleList\" start=\"0\" end=\"4\">1.1 2.2 3.3 4.4 5.5</Deviations>",
          "</GaussianRandomVectorRange>",
          "</RangedVector>",
          "");


  XMLConfigurable xc = Config.getXMLConfigurableFromString(xmlString);
  System.out.println(xc);

  RangedVector ov = (RangedVector) xc;
  System.out.println(Arrays.toString(ov.createVector()));
  System.out.println(Arrays.toString(ov.createVector()));
  System.out.println(Arrays.toString(ov.createVector()));
  System.out.println(Arrays.toString(ov.createVector()));
  System.out.println(Arrays.toString(ov.createVector()));

}


}
