package agency;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.lang3.SerializationUtils;
import agency.eval.FitnessAggregator;

/**
 * Individuals may be evaluated out-of-process, and object identity must
 * still be maintained.  Because there are cases where two separate individuals
 * may have the same genome/data, the equality of that data is insufficient to 
 * uniquely identify them.  Therefore a UUID is used for this purpose.
 * 
 * @author kkoning
 *
 */
public interface Individual extends Serializable, Cloneable {
	UUID getUUID();
	void setUUID(UUID uuid);

	void setFitness(Fitness fitness);
	Optional<Fitness> getFitness();
	
	void addFitnessSample(Fitness fitness);
	void aggregateFitness(FitnessAggregator fitnessAggregator);
	
	void setParentIDs(List<UUID> parentIDs);
	List<UUID> getParentIDs();
	
	public static Individual clone(Individual ind) {
		Individual toReturn = SerializationUtils.clone(ind);
		toReturn.getParentIDs().clear();
		toReturn.getParentIDs().add(ind.getUUID());
		
		return null;
	}
	
}
