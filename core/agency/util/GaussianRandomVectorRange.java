package agency.util;

import agency.Config;
import agency.XMLConfigurable;
import org.w3c.dom.Element;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by liara on 11/5/16.
 */
public class GaussianRandomVectorRange extends VectorRange<Number> {

VectorRange<Number> means;
VectorRange<Number> deviations;

@Override
public void readXMLConfig(Element e) {
  super.readXMLConfig(e);

  Optional<Element> oe = Config.getChildElementWithTag(e,"Means");
  if (oe.isPresent()) {
    Element meansElement = oe.get();
    XMLConfigurable xc = Config.initializeXMLConfigurable(meansElement);
    means = (VectorRange<Number>) xc;
  } else {
    throw new RuntimeException("A GaussianRandomVectorRange is missing child VectorRange specifying means");
  }

  oe = Config.getChildElementWithTag(e,"Deviations");
  if (oe.isPresent()) {
    Element deviationsElement = oe.get();
    XMLConfigurable xc = Config.initializeXMLConfigurable(deviationsElement);
    deviations = (VectorRange<Number>) xc;
  } else {
    throw new RuntimeException("A GaussianRandomVectorRange is missing child VectorRange specifying deviations");
  }

}

@Override
public Number get(int position) {
  Number mean = means.get(position);
  Number deviation = deviations.get(position);

  Random random = ThreadLocalRandom.current();
  double variance = random.nextGaussian();
  double scaledVariance = variance * deviation.doubleValue();
  Double value = mean.doubleValue();
  value += scaledVariance;

  if (mean instanceof Integer)
    return value.intValue();
  if (mean instanceof Float)
    return value.floatValue();

  return value;
}

}
