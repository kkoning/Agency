package agency.reproduce;

import agency.Fitness;
import agency.Population;
import org.w3c.dom.Element;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 Balances the sizes of populations within a population group based on
 tournament selection of all individuals in that group.
 */
public class TournamentBalancer implements PopulationBalancer {

int tournamentSize;
int topIndividuals;

@Override
public void readXMLConfig(Element e) {
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
  // Nothing should be necessary.
}

@Override
public double[] balance(List<Population> populations, Random r) {
  int[] numWinners = new int[populations.size()];

  int[] numIndividuals = new int[populations.size()];
  int totalIndividuals = 0;
  for (int i = 0; i < populations.size(); i++) {
    numIndividuals[i] = populations.get(i).individuals.size();
    totalIndividuals += numIndividuals[i];
  }


  TSHelper[] inds = new TSHelper[tournamentSize];

  // Main loop for the selection
  for (int i = 0; i < totalIndividuals; i++) {

    // Randomly determine tournament entrants
    for (int j = 0; j < tournamentSize; j++) {
      int indIndex = r.nextInt(totalIndividuals);
      int popIndex = 0;
      TSHelper ind = null;
      while (ind == null) {
        Population candidatePop = populations.get(popIndex);
        if (indIndex < candidatePop.size()) {
          ind = new TSHelper();
          ind.fitness = candidatePop.individuals.get(indIndex).getFitness();
          ind.popIndex = popIndex;
        } else {
          indIndex -= candidatePop.individuals.size();
          popIndex++;
        }
      }
      inds[j] = ind;
    }

    // Select winners, record a win for the populations.
    Arrays.sort(inds);
    for (int j = 1; j <= topIndividuals; j++) {
      TSHelper ind = inds[inds.length-j];
      numWinners[ind.popIndex]++;
    }
  }

  // Transform the wins into an array of doubles.  The PopulationGroup should
  // normalize these to ratios anyway, multiplied by the desired total
  // population size.
  double[] toReturn = new double[numWinners.length];
  for (int i = 0; i < toReturn.length; i++) {
    toReturn[i] = (double) numWinners[i];
  }

  return toReturn;
}

private static class TSHelper implements Comparable<TSHelper> {
  int popIndex;
  Fitness fitness;

  // Compare just by the fitnesses.
  @Override
  public int compareTo(TSHelper o) {
    return fitness.compareTo(o.fitness);
  }
}

}
