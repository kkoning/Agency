package agency;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import agency.eval.FitnessAggregator;

public abstract class AbstractIndividual implements Individual {
	private static final long serialVersionUID = 1451854714980257824L;

	private UUID uuid = UUID.randomUUID();
	List<UUID> parentIDs = new ArrayList<>(2);
	Fitness fitness;
	List<Fitness> fitnessSamples = new ArrayList<>(5);
	
	@Override
	public UUID getUUID() {
		return uuid;
	}

	@Override
	public void setUUID(UUID uuid) {
		this.uuid = uuid;
	}
	
	@Override
	public void setParentIDs(List<UUID> parentIDs) {
		this.parentIDs = parentIDs;
	}

	@Override
	public List<UUID> getParentIDs() {
		return parentIDs;
	}

	public Optional<Fitness> getFitness() {
		return Optional.ofNullable(fitness);
	}

	public void setFitness(Fitness fitness) {
		this.fitness = fitness;
	}

  @Override
  public void addFitnessSample(Fitness fitness) {
    fitnessSamples.add(fitness);
  }

  @Override
  public void aggregateFitness(FitnessAggregator fitnessAggregator) {
    fitness = fitnessAggregator.reduce(fitnessSamples.stream());
    fitnessSamples.clear();
  }

	
	
}
