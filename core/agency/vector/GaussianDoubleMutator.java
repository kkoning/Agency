package agency.vector;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import agency.Config;


/**
 * TODO: Better error handing, more documentation
 *
 * @author kkoning
 */
public class GaussianDoubleMutator implements ValueMutator<Double> {

double sd;

public GaussianDoubleMutator() {
  super();
}

public GaussianDoubleMutator(double sd) {
  super();
  this.sd = sd;
}

@Override
public void readXMLConfig(Element e) {
  sd = Double.parseDouble(e.getAttribute("sd"));
}

@Override
public void writeXMLConfig(Element e) {
  e.setAttribute("sd", Double.toString(sd));
}

@Override
public void resumeFromCheckpoint() {
}

@Override
public Double mutate(Double value) {
  Random r = ThreadLocalRandom.current();
  double z = r.nextGaussian();
  return (double) (value + (z * sd));
}


}
