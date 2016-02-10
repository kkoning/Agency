package agency.vector;

import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang3.Range;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import agency.Config;
import agency.IndividualFactory;
import agency.util.RangeMap;
import agency.util.RangeMapEntry;

public class VectorIndividualFactory<T>
    implements IndividualFactory<VectorIndividual<T>> {
  static {
    Config.registerClassXMLTag(VectorIndividualFactory.class);
  }

  int                                    genomeSize;
  RangeMap<Integer, ValueInitializer<T>> initializers = new RangeMap<>();
  RangeMap<Integer, ValueLimiter<T>>     limiters     = new RangeMap<>();

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

    // Initializers
    List<Element> initializerElements = Config.getChildElementsWithTag(e,
        "Initializer");
    if (initializerElements.isEmpty())
      throw new RuntimeException("Must have at least one initalizer");
    for (Element initE : initializerElements) {
      Integer start = Integer.parseInt(initE.getAttribute("start"));
      Integer end = Integer.parseInt(initE.getAttribute("end"));
      ValueInitializer<T> vi = (ValueInitializer<T>) Config
          .initializeXMLConfigurable(initE);
      addInitializer(start, end, vi);
    }

    // Limiters
    List<Element> limiterElements = Config.getChildElementsWithTag(e,
        "Limiter");
    if (limiterElements.isEmpty())
      throw new RuntimeException("Must have at least one initalizer");
    for (Element limitE : limiterElements) {
      Integer start = Integer.parseInt(limitE.getAttribute("start"));
      Integer end = Integer.parseInt(limitE.getAttribute("end"));
      ValueLimiter<T> vl = (ValueLimiter<T>) Config
          .initializeXMLConfigurable(limitE);
      addLimiter(start, end, vl);
    }
  }

  @Override
  public void writeXMLConfig(final Document d, final Element e) {
    e.setAttribute("genomeSize", Integer.toString(genomeSize));

    // Initializers
    Iterator<RangeMapEntry<Integer, ValueInitializer<T>>> initializersIterator = initializers
        .entries().iterator();
    while (initializersIterator.hasNext()) {
      RangeMapEntry<Integer, ValueInitializer<T>> rme = initializersIterator
          .next();
      Range<Integer> r = rme.getRange();
      ValueInitializer<?> vi = rme.getObject();
      Element childE = Config.createNamedElement(d, vi, "Initializer");
      childE.setAttribute("start", r.getMinimum().toString());
      childE.setAttribute("end", r.getMaximum().toString());
      e.appendChild(childE);
    }

    // Limiters
    Iterator<RangeMapEntry<Integer, ValueLimiter<T>>> limitersIterator = limiters
        .entries().iterator();
    while (limitersIterator.hasNext()) {
      RangeMapEntry<Integer, ValueLimiter<T>> rme = limitersIterator.next();
      Range<Integer> r = rme.getRange();
      ValueLimiter<?> vl = rme.getObject();
      Element childE = Config.createNamedElement(d, vl, "Limiter");
      childE.setAttribute("start", r.getMinimum().toString());
      childE.setAttribute("end", r.getMaximum().toString());
      e.appendChild(childE);
    }
  }

  public void addInitializer(Integer start, Integer end,
      ValueInitializer<T> vi) {
    Range<Integer> r = Range.between(start, end);
    addInitializer(r, vi);

  }

  public void addLimiter(Range<Integer> r, ValueLimiter<T> vl) {
    // Check for illegal values
    if (r.getMinimum() < 0)
      throw new IllegalArgumentException(
          "Cannot add a range limiter that covers a negative index");
    if (r.getMaximum() >= genomeSize)
      throw new IllegalArgumentException(
          "Cannot add a range limiter that extends past the genome size");

    limiters.put(r, vl);
  }

  public void addLimiter(Integer start, Integer end, ValueLimiter<T> vl) {
    Range<Integer> r = Range.between(start, end);
    addLimiter(r, vl);
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

    // Limit
    limit(ind);

    ind.setFitness(null);
    return ind;
  }

  @Override
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

}
