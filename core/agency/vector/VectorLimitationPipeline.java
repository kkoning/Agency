package agency.vector;

import agency.Config;
import agency.Individual;
import agency.Population;
import agency.XMLConfigurable;
import agency.reproduce.BreedingPipeline;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class VectorLimitationPipeline implements BreedingPipeline {

BreedingPipeline source;
VectorLimiter    limiter;

@Override
public void readXMLConfig(Element e) {

  NodeList nl = e.getChildNodes();
  for (int i = 0; i < nl.getLength(); i++) {
    Node node = nl.item(i);
    if (node instanceof Element) {
      Element child = (Element) node;
      XMLConfigurable xc = Config.initializeXMLConfigurable(child);
      if (xc instanceof VectorLimiter) {
        limiter = (VectorLimiter) xc;
      }
      if (xc instanceof BreedingPipeline) {
        source = (BreedingPipeline) xc;
      }
    }
  }

  if (source == null)
    throw new UnsupportedOperationException(
            "VectorLimitationPipeline must have a source BreedingPipeline");
  if (limiter == null)
    throw new UnsupportedOperationException("VectorLimitationPipeline must have a VectorLimiter");

  // TODO: Change this to explicitly requiring a Source tag for the source?


}

@Override
public void writeXMLConfig(Element e) {
  Document d = e.getOwnerDocument();

  Element sourceElement = Config.createUnnamedElement(d, source);
  e.appendChild(sourceElement);

  Element limiterElement = Config.createUnnamedElement(d, limiter);
  e.appendChild(limiterElement);
}

@Override
public void resumeFromCheckpoint() {
  // Should be unnecessary; has no un-serializable state.
}


@Override
public Individual generate() {
  VectorIndividual<Object> soureceInd = (VectorIndividual<Object>) source.generate();
  VectorIndividual<Object> limitedInd = limiter.limit(soureceInd);

  return limitedInd;
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
