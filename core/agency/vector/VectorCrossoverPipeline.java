package agency.vector;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import agency.XMLConfigurable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import agency.Config;
import agency.Individual;
import agency.Population;
import agency.reproduce.BreedingPipeline;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class VectorCrossoverPipeline implements BreedingPipeline {

BreedingPipeline source;
double crossoverProb = 0.2;
VectorIndividual<?> otherInd = null;

@Override
public void readXMLConfig(Element e) {

  NodeList nl = e.getChildNodes();
  for (int i = 0; i < nl.getLength(); i++) {
    Node node = nl.item(i);
    if (node instanceof Element) {
      Element child = (Element) node;
      XMLConfigurable xc = Config.initializeXMLConfigurable(child);

      if (xc instanceof BreedingPipeline) {
        if (source != null) // Can only have one source of individuals
          throw new UnsupportedOperationException("VectorCrossoverPipeline cannot have more than one source BreedingPipeline");
        source = (BreedingPipeline) xc;
      }

    }
  }

  if (source == null)
    throw new UnsupportedOperationException("VectorCrossoverPipeline must have a source BreedingPipeline");

  try {
    crossoverProb = Double.parseDouble(e.getAttribute("crossoverProb"));
  } catch (Exception ex) {
    // TODO
  }

}

@Override
public void writeXMLConfig(Element e) {
  Document d = e.getOwnerDocument();
  Element sourceE = Config.createNamedElement(d, source, "Source");
  e.appendChild(sourceE);
}

@Override
public void resumeFromCheckpoint() {
  // Should be unnecessary; everything should be serializable.
}

@Override
public Individual generate() {
  if (otherInd != null) {
    Individual toReturn = otherInd;
    otherInd = null;
    return toReturn;
  }

  // Crossover is not gauranteed
  double crossoverDice = ThreadLocalRandom.current().nextDouble();
  boolean doCrossover = (crossoverDice < this.crossoverProb);
  if (!doCrossover)
    return source.generate();

  Random r = ThreadLocalRandom.current();

  VectorIndividual clone1 = (VectorIndividual<?>) source.generate();
  VectorIndividual clone2 = (VectorIndividual<?>) source.generate();

  int genomeSize = clone1.getGenomeLength();
  // Assuming equal size for both parents

  int crossoverPos = r.nextInt(genomeSize);
  int crossoverEndPos = r.nextInt(genomeSize);
  // do the actual crossover
  while (crossoverPos != crossoverEndPos) {
    Object fromClone1 = clone1.getGenomeAt(crossoverPos);
    Object fromClone2 = clone2.getGenomeAt(crossoverPos);
    clone1.set(crossoverPos, fromClone2);
    clone2.set(crossoverPos, fromClone1);

    crossoverPos++;
    if (crossoverPos == genomeSize)
      crossoverPos = 0;
  }

  otherInd = clone2;
  return clone1;
}

@Override
public void setSourcePopulation(Population pop) {
  source.setSourcePopulation(pop);
}

}
