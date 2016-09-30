package agency;

public class SimpleFitness implements Fitness {

Double fitness;

public SimpleFitness(double fitness) {
  this.fitness = fitness;
}

public Double getFitness() {
  return fitness;
}

public void setFitness(Double fitness) {
  this.fitness = fitness;
}

@Override
public int compareTo(Fitness o) {
  return fitness.compareTo(((SimpleFitness) o).fitness);
}

@Override
public String toString() {
  return "SimpleFitness [" + fitness + "]";
}

}
