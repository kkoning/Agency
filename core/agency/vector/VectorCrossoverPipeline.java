package agency.vector;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import agency.Config;
import agency.Individual;
import agency.Population;
import agency.XMLConfigurable;
import agency.reproduce.BreedingPipeline;

public class VectorCrossoverPipeline implements BreedingPipeline {
private static final long serialVersionUID = 1L;

BreedingPipeline    source;
double              crossoverProb = 0.2;
VectorIndividual<?> otherInd      = null;

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
@SuppressWarnings("unchecked")
public Individual generate() {

  /*
   * Crossover produces two individuals, but this function only returns one
   * individual at a time. The other is saved, and so can be returned
   * immediately when the next call to generate() is made.
   */
  if (otherInd != null) {
    Individual toReturn = otherInd;
    otherInd = null;
    return toReturn;
  }

  // Crossover is not guaranteed
  double crossoverDice = ThreadLocalRandom.current().nextDouble();
  boolean doCrossover = (crossoverDice < this.crossoverProb);
  if (!doCrossover)
    return source.generate();

  Random r = ThreadLocalRandom.current();

  /*
   * The two individuals that we are going to do crossover on come from the
   * source breeding pipeline. They must be VectorIndividuals. However, the
   * breeding pipeline is configured with XML, and so it is possible that the
   * user has chosen an incorrect configuration where VectorCrossoverPipeline is
   * expected to operate on other types of agents. This next section of code
   * checks for that error and returns a RuntimeException with what is hopefully
   * a useful error message.
   * 
   * @SuppressWarnings("unchecked") has been used to suppress the compiler
   * warning for this issue.
   */
  VectorIndividual<Object> clone1;
  VectorIndividual<Object> clone2;
  try {
    clone1 = (VectorIndividual<Object>) source.generate();
    clone2 = (VectorIndividual<Object>) source.generate();
  } catch (ClassCastException cce) {
    throw new RuntimeException("VectorCrossoverPipeline requires only VectorIndividuals");
  }

  /*
   * Determine the positions for the crossover. Rather than fixing one of the
   * end points at the end of the genome, both are determined randomly.
   */
  int genomeSize = clone1.getGenomeLength();
  int crossoverPos = r.nextInt(genomeSize);
  int crossoverEndPos = r.nextInt(genomeSize);

  /*
   * Actually perform the crossover.
   */
  while (crossoverPos != crossoverEndPos) {
    
    Object[] clone1genome = clone1.getGenome();
    Object[] clone2genome = clone2.getGenome();
    
    Object fromClone1 = clone1genome[crossoverPos];
    Object fromClone2 = clone2genome[crossoverPos];
    clone1genome[crossoverPos] = fromClone2;
    clone2genome[crossoverPos] = fromClone1;
    crossoverPos++;

    // Crossover wraps around the genome; the end condition is
    // crossoverPos != crossoverEndPos
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
