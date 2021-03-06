package agency.reproduce;

import static agency.util.Misc.WARN;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

import org.w3c.dom.Element;

import agency.Individual;
import agency.Population;
import agency.XMLConfigurable;

public class TournamentSelector implements BreedingPipeline, XMLConfigurable {
private static final long serialVersionUID = 1L;

int tournamentSize = 2; // default to 2 individuals, weakest selection pressure
int topIndividuals = 1;

private List<Individual>  individuals;
private Queue<Individual> inPipeline = new LinkedBlockingQueue<>();

public TournamentSelector() {
}

public TournamentSelector(int tournamentSize) {
  this.tournamentSize = tournamentSize;
}

public TournamentSelector(int tournamentSize, int topIndividuals) {
  this.tournamentSize = tournamentSize;
  this.topIndividuals = topIndividuals;
}

@Override
public void readXMLConfig(Element e) {
  // TODO: Better error messages and checking.
  tournamentSize = Integer.parseInt(e.getAttribute("tournamentSize"));
  topIndividuals = Integer.parseInt(e.getAttribute("topIndividuals"));
}

@Override
public void writeXMLConfig(Element e) {
  e.setAttribute("tournamentSize", Integer.toString(tournamentSize));
  e.setAttribute("topIndividuals", Integer.toString(topIndividuals));
}

@Override
public void resumeFromCheckpoint() {

}

@Override
public Individual generate() {

  if (!inPipeline.isEmpty()) {
    Individual ancestor = inPipeline.remove();
    Individual clone = ancestor.copy();
    return clone;
  }

  List<Individual> participants = new ArrayList<>(tournamentSize);
  Random r = ThreadLocalRandom.current();
  while (participants.size() < tournamentSize) {
    if (tournamentSize > individuals.size()) {
      tournamentSize = individuals.size();
      WARN("Not enough individuals for full tournament selection");
    }
    participants.add(individuals.get(r.nextInt(tournamentSize)));
  }
  Collections.sort(participants, new FitnessComparator());

  for (int j = 1; j < topIndividuals; j++) {
    inPipeline.add(participants.get(j));
  }
  Individual ancestor = participants.get(0);
  Individual clone = ancestor.copy();
  return clone;
}

@Override
public void setSourcePopulation(Population pop) {
  individuals = pop.individuals;
}

public int getTournamentSize() {
  return tournamentSize;
}

public void setTournamentSize(int tournamentSize) {
  this.tournamentSize = tournamentSize;
}

public int getTopIndividuals() {
  return topIndividuals;
}

public void setTopIndividuals(int topIndividuals) {
  this.topIndividuals = topIndividuals;
}

}
