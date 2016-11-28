package agency.vector;

import agency.Config;
import agency.XMLConfigurable;
import org.junit.Test;

/**
 * Created by liara on 11/5/16.
 */
public class VectorIndividualFactoryTest {

@Test
public void basicTest() {
  String xmlString = String.join("\n",
          "<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
          "<VectorIndividualFactory length=\"5\">",
          " <GaussianRandomVectorRange start=\"0\" end=\"4\">",
          "  <Means type=\"DoubleList\" start=\"0\" end=\"4\">1.1 2.2 3.3 4.4 5.5</Means>",
          "  <Deviations type=\"DoubleList\" start=\"0\" end=\"4\">1.1 2.2 3.3 4.4 5.5</Deviations>",
          " </GaussianRandomVectorRange>",
          "</VectorIndividualFactory>",
          "");


  XMLConfigurable xc = Config.getXMLConfigurableFromString(xmlString);
  System.out.println(xc);

  VectorIndividualFactory vif = (VectorIndividualFactory) xc;
  for (int i = 0; i < 10; i++) {
    System.out.println(vif.create());
  }

}


}
