package agency.vector;

import agency.Config;
import agency.XMLConfigurable;
import agency.util.VectorRange;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by liara on 11/7/16.
 */
public class GaussianMutator extends VectorRange<Number> implements VectorValueMutator {

VectorRange<Number> mutationProbabilities;
VectorRange<Number> deviations;

@Override
public void readXMLConfig(Element e) {
  super.readXMLConfig(e);

  Optional<Element> oe = Config.getChildElementWithTag(e, "MutationProbability");
  if (oe.isPresent()) {
    Element mutationProbabilityElement = oe.get();
    XMLConfigurable xc = Config.initializeXMLConfigurable(mutationProbabilityElement);
    mutationProbabilities = (VectorRange<Number>) xc;
  } else {
    throw new RuntimeException("A GaussianMutator is missing child VectorRange specifying MutationProbability");
  }

  oe = Config.getChildElementWithTag(e, "Deviations");
  if (oe.isPresent()) {
    Element deviationsElement = oe.get();
    XMLConfigurable xc = Config.initializeXMLConfigurable(deviationsElement);
    deviations = (VectorRange<Number>) xc;
  } else {
    throw new RuntimeException("A GaussianMutator is missing child VectorRange specifying Deviations");
  }
}

@Override
public void writeXMLConfig(Element e) {
  super.writeXMLConfig(e);

  Document d = e.getOwnerDocument();
  Element mutE = Config.createNamedElement(d, mutationProbabilities, "MutationProbability");
  Element devE = Config.createNamedElement(d, deviations, "Deviations");
  d.appendChild(mutE);
  d.appendChild(devE);

}

@Override
public Number get(int position) {
  return null;
}

@Override
public void mutate(Object[] genome, int pos) {

  Number mutProb = mutationProbabilities.get(pos);
  Random rand = ThreadLocalRandom.current();
  double roll = rand.nextDouble();
  if (roll <= mutProb.doubleValue()) {
    // Then do the mutation
    double stdDev = rand.nextGaussian();
    Number scale = deviations.get(pos);
    double totalChange = stdDev * scale.doubleValue();

    Number genomeValue = (Number) genome[pos];
    // TODO: Catch cast exception & throw better error message?
    Double newValue = genomeValue.doubleValue() + totalChange;

    // Put back the type of value we originally found.
    if (genomeValue instanceof Byte) {
      genome[pos] = newValue.byteValue();
    } else if (genomeValue instanceof Short) {
      genome[pos] = newValue.shortValue();
    } else if (genomeValue instanceof Integer) {
      genome[pos] = newValue.intValue();
    } else if (genomeValue instanceof Long) {
      genome[pos] = newValue.longValue();
    } else if (genomeValue instanceof Float) {
      genome[pos] = newValue.floatValue();
    } else if (genomeValue instanceof Double) {
      genome[pos] = newValue;
    } else {
      throw new RuntimeException("GaussianMutator attempting to mutate unknown Numeric type?");
    }


  }


  return;
}
}
