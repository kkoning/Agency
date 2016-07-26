package agency.reproduce;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.commons.lang3.Range;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import agency.Config;
import agency.Individual;
import agency.Population;
import agency.util.RangeMap;
import agency.util.RangeMapEntry;
import agency.vector.ValueLimiter;
import agency.vector.ValueMutator;
import agency.vector.VectorIndividual;

public class VectorMutationPipeline<T> implements BreedingPipeline {
  static {
    Config.registerClassXMLTag(VectorMutationPipeline.class);
  }

  BreedingPipeline                   source;
  RangeMap<Integer, ValueMutator<T>> mutators;
  RangeMap<Integer, Double>          mutationProbabilities;

  public VectorMutationPipeline() {
    mutators = new RangeMap<>();
    mutationProbabilities = new RangeMap<>();
  }
  
  @Override
  public void readXMLConfig(Element e) {
    Optional<Element> sourceEOp = Config.getChildElementWithTag(e, "Source");
    if (!sourceEOp.isPresent())
      throw new IllegalArgumentException(
          "A VectorMutationPipeline must have exactly one BreedingPipeline Source");

    source = (BreedingPipeline) Config
        .initializeXMLConfigurable(sourceEOp.get());

    List<Element> mutatorEs = Config.getChildElementsWithTag(e, "Mutator");
    if (mutatorEs.isEmpty())
      throw new IllegalArgumentException(
          "A VectorMutationPipeline must have at least one mutator.");

    for (Element mutatorE : mutatorEs) {
      ValueMutator<T> mutator = (ValueMutator<T>) Config
          .initializeXMLConfigurable(mutatorE);
      Integer start = Integer.parseInt(mutatorE.getAttribute("start"));
      Integer end = Integer.parseInt(mutatorE.getAttribute("end"));
      Double prob = Double
          .parseDouble(mutatorE.getAttribute("mutationProbability"));
      addMutator(start, end, prob, mutator);
    }
  }

  @Override
  public void writeXMLConfig(Document d, Element e) {
    Element sourceE = Config.createNamedElement(d, source, "Source");
    e.appendChild(sourceE);

    Iterator<RangeMapEntry<Integer, ValueMutator<T>>> mutatorsIterator = mutators
        .entries().iterator();
    while (mutatorsIterator.hasNext()) {
      RangeMapEntry<Integer, ValueMutator<T>> rme = mutatorsIterator.next();
      Range<Integer> r = rme.getRange();
      ValueMutator<T> vm = rme.getObject();
      Double prob = mutationProbabilities.get(r.getMinimum()).get();

      Element childE = Config.createNamedElement(d, vm, "Mutator");
      childE.setAttribute("start", r.getMinimum().toString());
      childE.setAttribute("end", r.getMaximum().toString());
      childE.setAttribute("mutationProbability", prob.toString());
      e.appendChild(childE);
    }
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
    return ind;
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
