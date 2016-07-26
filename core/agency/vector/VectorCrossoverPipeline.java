package agency.reproduce;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import agency.Config;
import agency.Individual;
import agency.Population;
import agency.vector.VectorIndividual;

public class VectorCrossoverPipeline implements BreedingPipeline {
  static {
    Config.registerClassXMLTag(VectorCrossoverPipeline.class);
  }

  BreedingPipeline    source;
  VectorIndividual<?> otherInd = null;

  @Override
  public void readXMLConfig(Element e) {
    Optional<Element> sourceEOp = Config.getChildElementWithTag(e, "Source");
    if (!sourceEOp.isPresent())
      throw new IllegalArgumentException(
          "A VectorCrossoverPipeline must have exactly one BreedingPipeline Source");
    
    source = (BreedingPipeline) Config.initializeXMLConfigurable(sourceEOp.get());
  }

  @Override
  public void writeXMLConfig(Document d, Element e) {
    Element sourceE = Config.createNamedElement(d, source, "Source");
    e.appendChild(sourceE);
  }

  @Override
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public Individual generate() {
    if (otherInd != null) {
      Individual toReturn = otherInd;
      otherInd = null;
      return toReturn;
    }

    Random r = ThreadLocalRandom.current();

    VectorIndividual clone1 = (VectorIndividual<?>) source.generate();
    VectorIndividual clone2 = (VectorIndividual<?>) source.generate();

    int genomeSize = clone1.getGenomeLength();
    // Assuming equal size for both parents

    int crossoverPos = r.nextInt(genomeSize);
    int crossoverEndPos = r.nextInt(genomeSize);
    // do the actual crossover
    while (crossoverPos != crossoverEndPos) {
      Object fromClone1 = clone1.get(crossoverPos);
      Object fromClone2 = clone2.get(crossoverPos);
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
