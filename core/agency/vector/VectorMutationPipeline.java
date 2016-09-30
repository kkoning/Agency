package agency.vector;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
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
import agency.util.RangeMap;
import agency.util.RangeMapEntry;

public class VectorMutationPipeline<T> implements BreedingPipeline {

BreedingPipeline                   source;
RangeMap<Integer, ValueMutator<T>> mutators;
RangeMap<Integer, Double>          mutationProbabilities;
RangeMap<Integer, ValueLimiter<T>> limiters;

public VectorMutationPipeline() {
  mutators = new RangeMap<>();
  mutationProbabilities = new RangeMap<>();
  limiters = new RangeMap<>();
}

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
          throw new UnsupportedOperationException(
                  "VectorMutationPipeline cannot have more than one source BreedingPipeline");
        source = (BreedingPipeline) xc;
      } else if (xc instanceof ValueMutator) {
        Integer start = Integer.parseInt(child.getAttribute("start"));
        Integer end = Integer.parseInt(child.getAttribute("end"));
        Double prob = Double
                .parseDouble(child.getAttribute("mutationProbability"));

        addMutator(start, end, prob, (ValueMutator) xc);
      } else if (xc instanceof ValueLimiter) {
        Integer start = Integer.parseInt(child.getAttribute("start"));
        Integer end = Integer.parseInt(child.getAttribute("end"));
        addLimiter(start, end, (ValueLimiter) xc);
      }

    }
  }

  if (source == null)
    throw new UnsupportedOperationException(
            "VectorMutationPipeline must have a source BreedingPipeline");

  if (mutators.size() < 1)
    throw new UnsupportedOperationException(
            "VectorMutationPipeline must have at least one mutator");

}

@Override
public void writeXMLConfig(Element e) {
  Document d = e.getOwnerDocument();
  Element sourceE = Config.createUnnamedElement(d, source);
  e.appendChild(sourceE);

  Iterator<RangeMapEntry<Integer, ValueMutator<T>>> mutatorsIterator = mutators
          .entries().iterator();
  while (mutatorsIterator.hasNext()) {
    RangeMapEntry<Integer, ValueMutator<T>> rme = mutatorsIterator.next();
    Range<Integer> r = rme.getRange();
    ValueMutator<T> vm = rme.getObject();
    Double prob = mutationProbabilities.get(r.getMinimum()).get();

    Element childE = Config.createUnnamedElement(d, vm);
    childE.setAttribute("start", r.getMinimum().toString());
    childE.setAttribute("end", r.getMaximum().toString());
    childE.setAttribute("mutationProbability", prob.toString());
    e.appendChild(childE);
  }

  Iterator<RangeMapEntry<Integer, ValueLimiter<T>>> limitersIterator = limiters
          .entries().iterator();
  while (limitersIterator.hasNext()) {
    RangeMapEntry<Integer, ValueLimiter<T>> rme = limitersIterator.next();
    Range<Integer> r = rme.getRange();
    ValueLimiter<T> vl = rme.getObject();

    Element childE = Config.createUnnamedElement(d, vl);
    childE.setAttribute("start", r.getMinimum().toString());
    childE.setAttribute("end", r.getMaximum().toString());

    e.appendChild(childE);
  }


}

@Override
public void resumeFromCheckpoint() {

}

public void addMutator(int start, int end, double probability,
                       ValueMutator<T> mutator) {
  Range<Integer> r = Range.between(start, end);
  addMutator(r, probability, mutator);
}

public void addMutator(Range<Integer> range, double probability,
                       ValueMutator<T> mutator) {
  mutators.put(range, mutator);
  mutationProbabilities.put(range, probability);
}

public void addLimiter(Integer start, Integer end, ValueLimiter<T> vl) {
  Range<Integer> r = Range.between(start, end);
  addLimiter(r, vl);
}

public void addLimiter(Range<Integer> r, ValueLimiter<T> vl) {
  // Check for illegal values
  if (r.getMinimum() < 0)
    throw new IllegalArgumentException(
            "Cannot add a range limiter that covers a negative index");
  limiters.put(r, vl);
}

@Override
public Individual generate() {
  Random r = ThreadLocalRandom.current();
  VectorIndividual<T> ind = (VectorIndividual<T>) source.generate();

  for (RangeMapEntry<Integer, ValueMutator<T>> mutatorRME : mutators
          .entries()) {
    Range<Integer> range = mutatorRME.getRange();
    ValueMutator<T> mutator = mutatorRME.getObject();
    Double prob = mutationProbabilities.get(range.getMinimum()).get();

    int pos = range.getMinimum();
    int last = range.getMaximum();
    while (pos <= last) {
      if (r.nextDouble() < prob)
        ind.set(pos, mutator.mutate(ind.get(pos)));
      pos++;
    }
  }
  limit(ind);
  return ind;
}

public void limit(VectorIndividual<T> ind) {
  // Initialize
  Iterator<RangeMapEntry<Integer, ValueLimiter<T>>> initializersIterator = limiters
          .entries().iterator();
  while (initializersIterator.hasNext()) {
    RangeMapEntry<Integer, ValueLimiter<T>> rme = initializersIterator.next();
    Range<Integer> r = rme.getRange();
    ValueLimiter<T> vl = rme.getObject();
    for (int i = r.getMinimum(); i <= r.getMaximum(); i++) {
      ind.genome[i] = vl.limit(ind.genome[i]);
    }
  }
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
