package agency.vector;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang3.Range;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import agency.Config;
import agency.Individual;
import agency.Population;
import agency.XMLConfigurable;
import agency.reproduce.BreedingPipeline;
import agency.util.RangeMapEntry;

public class VectorMutationPipeline implements BreedingPipeline {

BreedingPipeline source;
VectorMutator    mutator;

@Override
public void readXMLConfig(Element e) {

  NodeList nl = e.getChildNodes();
  for (int i = 0; i < nl.getLength(); i++) {
    Node node = nl.item(i);
    if (node instanceof Element) {
      Element child = (Element) node;
      XMLConfigurable xc = Config.initializeXMLConfigurable(child);
      if (xc instanceof VectorMutator) {
        mutator = (VectorMutator) xc;
      }
      if (xc instanceof BreedingPipeline) {
        source = (BreedingPipeline) xc;
      }
    }
  }

  if (source == null)
    throw new UnsupportedOperationException(
            "VectorMutationPipeline must have a source BreedingPipeline");
  if (mutator == null)
    throw new UnsupportedOperationException("VectorMutationPipeline must have a VectorMutator");

  // TODO: Change this to explicitly requiring a Source tag for the source?


}

@Override
public void writeXMLConfig(Element e) {
  Document d = e.getOwnerDocument();

  Element sourceE = Config.createUnnamedElement(d, source);
  e.appendChild(sourceE);

  Element mutatorE = Config.createUnnamedElement(d, mutator);
  e.appendChild(mutatorE);
}

@Override
public void resumeFromCheckpoint() {
  // Should be unnecessary; has no un-serializable state.
}


@Override
public Individual generate() {
  Random r = ThreadLocalRandom.current();
  VectorIndividual<Object> soureceInd = (VectorIndividual<Object>) source.generate();
  VectorIndividual<Object> mutatedInd = mutator.mutate(soureceInd);

  return mutatedInd;


}


@Override
public void setSourcePopulation(Population pop) {
  source.setSourcePopulation(pop);
}

public BreedingPipeline getSource() {
  return source;
}

public void setSource(BreedingPipeline source) {
  this.source = source;
}

}
