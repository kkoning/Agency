package agency.vector;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.Range;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import agency.Config;
import agency.IndividualFactory;
import agency.XMLConfigurable;
import agency.reproduce.BreedingPipeline;
import agency.util.RangeMap;
import agency.util.RangeMapEntry;

public class VectorIndividualFactory<T>
        implements IndividualFactory<VectorIndividual<T>> {

int genomeSize;
RangeMap<Integer, ValueInitializer<T>> initializers = new RangeMap<>();

public VectorIndividualFactory() {
  super();
}

public VectorIndividualFactory(int genomeSize) {
  super();
  this.genomeSize = genomeSize;
}

@Override
public void readXMLConfig(Element e) {
  genomeSize = Integer.parseInt(e.getAttribute("genomeSize"));

  NodeList nl = e.getChildNodes();
  for (int i = 0; i < nl.getLength(); i++) {
    Node node = nl.item(i);
    if (node instanceof Element) {
      Element child = (Element) node;
      XMLConfigurable xc = Config.initializeXMLConfigurable(child);

      if (xc instanceof ValueInitializer) {
        Integer start = Integer.parseInt(child.getAttribute("start"));
        Integer end = Integer.parseInt(child.getAttribute("end"));
        addInitializer(start, end, (ValueInitializer) xc);
      } else {
        throw new UnsupportedOperationException("Unrecognized child element in VectorIndividualFactory");
      }
    }
  }

  if (initializers.size() < 1)
    throw new UnsupportedOperationException(
            "VectorMutationPipeline must have at least one mutator");
}

@Override
public void writeXMLConfig(final Element e) {
  Document d = e.getOwnerDocument();
  e.setAttribute("genomeSize", Integer.toString(genomeSize));

  // Initializers
  Iterator<RangeMapEntry<Integer, ValueInitializer<T>>> initializersIterator = initializers
          .entries().iterator();
  while (initializersIterator.hasNext()) {
    RangeMapEntry<Integer, ValueInitializer<T>> rme = initializersIterator
            .next();
    Range<Integer> r = rme.getRange();
    ValueInitializer<?> vi = rme.getObject();
    Element childE = Config.createUnnamedElement(d, vi);
    childE.setAttribute("start", r.getMinimum().toString());
    childE.setAttribute("end", r.getMaximum().toString());
    e.appendChild(childE);
  }

}

@Override
public void resumeFromCheckpoint() {

}

public void addInitializer(Integer start, Integer end,
                           ValueInitializer<T> vi) {
  Range<Integer> r = Range.between(start, end);
  addInitializer(r, vi);

}

public void addInitializer(Range<Integer> r, ValueInitializer<T> vi) {
  // Check for illegal values
  if (r.getMinimum() < 0)
    throw new IllegalArgumentException(
            "Cannot add an initializer that covers a negative index");
  if (r.getMaximum() >= genomeSize)
    throw new IllegalArgumentException(
            "Cannot add an initializer limiter that extends past the genome size");

  initializers.put(r, vi);
}

@Override
public VectorIndividual<T> create() {
  VectorIndividual<T> ind = new VectorIndividual<T>(genomeSize);
  // Initialize
  Iterator<RangeMapEntry<Integer, ValueInitializer<T>>> initializersIterator = initializers
          .entries().iterator();
  while (initializersIterator.hasNext()) {
    RangeMapEntry<Integer, ValueInitializer<T>> rme = initializersIterator
            .next();
    Range<Integer> r = rme.getRange();
    ValueInitializer<T> vi = rme.getObject();
    for (int i = r.getMinimum(); i <= r.getMaximum(); i++) {
      ind.genome[i] = vi.create();
    }
  }

  ind.setFitness(null);
  return ind;
}


}
