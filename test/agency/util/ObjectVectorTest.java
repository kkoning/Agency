package agency.util;

import agency.Config;
import agency.XMLConfigurable;
import org.junit.Test;

/**
 * Created by liara on 11/4/16.
 */
public class ObjectVectorTest {

@Test
public void basicTest() {
  String xmlString = String.join("\n",
          "<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
          "<RangedVector length=\"20\">",
          "<DoubleList start=\"0\" end=\"4\">1.1 2.2 3.3 4.4 5.5</DoubleList>",
          "<IntegerList start=\"5\" end=\"9\">1 2 3 4 5</IntegerList>",
          "<RepeatingDouble start=\"10\" end=\"14\" value=\"3.14159\"/>",
          "<RepeatingInteger start=\"15\" end=\"19\" value=\"42\"/>",
          "</RangedVector>",
          "");


  XMLConfigurable xc = Config.getXMLConfigurableFromString(xmlString);
  System.out.println(xc);
}


}
