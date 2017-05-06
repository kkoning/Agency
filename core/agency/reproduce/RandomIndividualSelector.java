package agency.reproduce;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.w3c.dom.Element;

import agency.Individual;
import agency.Population;
import agency.XMLConfigurable;

public class RandomIndividualSelector implements BreedingPipeline, XMLConfigurable {
private static final long serialVersionUID = 1L;

Population pop;

@Override
public Individual generate() {
  List<Individual> inds = pop.individuals;
  int index = ThreadLocalRandom.current().nextInt(inds.size());
  Individual parent = inds.get(index);
  Individual clone = parent.copy();
  return clone;
}

@Override
public void setSourcePopulation(Population pop) {
  this.pop = pop;
}

@Override
public void readXMLConfig(Element e) {
  // No config nencessary
}

@Override
public void writeXMLConfig(Element e) {
  // No config nencessary
}

@Override
public void resumeFromCheckpoint() {

}

}
