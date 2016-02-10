package agency;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public abstract class AbstractIndividual implements Individual {
	private static final long serialVersionUID = 1451854714980257824L;

	private UUID uuid = UUID.randomUUID();
	List<UUID> parentIDs = new ArrayList<>(2);
	Fitness fitness;
	
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

	
}
