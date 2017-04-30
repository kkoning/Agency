package agency.reproduce;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.SerializationUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import agency.Config;
import agency.Individual;
import agency.Population;
import agency.XMLConfigurable;

public class ElitismSelector implements BreedingPipeline, XMLConfigurable {

Integer numElites;
Float   proportionElites;

List<Individual> elites;
int currentPosition = 0;

public ElitismSelector() {
}

@Override
public void readXMLConfig(Element e) {
  String numElitesText = e.getAttribute("numElites");
  String proportionElitesText = e.getAttribute("proportionElites");
  boolean useNumElites = false;
  boolean useProportionElites = false;
  if (numElitesText != null && !numElitesText.isEmpty())
    useNumElites = true;
  if (proportionElitesText != null && !proportionElitesText.isEmpty())
    useProportionElites = true;
  if (useNumElites && useProportionElites)
    throw new RuntimeException("Elitism selector cannot simultaneously specify the number AND proportion of elites");
  if (useNumElites)
    numElites = Integer.parseInt(numElitesText);
  if (useProportionElites)
    proportionElites = Float.parseFloat(proportionElitesText);
}

@Override
public void writeXMLConfig(Element e) {
  if (numElites != null)
    e.setAttribute("numElites", Integer.toString(numElites));
  else
    e.setAttribute("proportionElites", Float.toString(proportionElites));
}

@Override
public void resumeFromCheckpoint() {

}

public Optional<Integer> getNumElites() {
  return Optional.of(numElites);
}

public void setNumElites(Integer numElites) {
  this.numElites = numElites;
  proportionElites = null;
}

public Optional<Float> getProportionElites() {
  return Optional.of(proportionElites);
}

public void setProportionElites(Float proportionElites) {
  this.proportionElites = proportionElites;
  numElites = null;
}


@Override
public Individual generate() {
  // If we need more, just cycle
  if (currentPosition >= elites.size())
    currentPosition = 0;

  Individual toReturn = elites.get(currentPosition);
  return SerializationUtils.clone(toReturn);
}

@Override
public void setSourcePopulation(Population pop) {
  // Function local #numElites, so it gets reset every generation
  Integer numElites = this.numElites;
  if (numElites == null)
    numElites = (int) (pop.size() * proportionElites);

  // Always
  if (numElites <= 0)
    numElites = 1;

  List<Individual> allIndividuals = pop.individuals.stream().collect(Collectors.toList());
  Collections.sort(allIndividuals, new FitnessComparator());
  elites = allIndividuals.stream().limit(numElites).collect(Collectors.toList());
  currentPosition = 0;
}


}
