package agency.reproduce;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import org.apache.commons.lang3.SerializationUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import agency.Config;
import agency.Individual;
import agency.Population;
import agency.XMLConfigurable;

public class RandomIndividualSelector implements BreedingPipeline, XMLConfigurable {

Population pop;

@Override
public Individual generate() {
  List<Individual> inds = pop.individuals;
  int index = ThreadLocalRandom.current().nextInt(inds.size());
  return SerializationUtils.clone(inds.get(index));
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
