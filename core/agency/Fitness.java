package agency;

import java.io.Serializable;

public interface Fitness extends Comparable<Fitness>, Serializable {
/**
 * Aggregates two fitness measurements associated with a single individual.
 * This function will be used in a map/reduce to aggregate the fitness
 * samples from each evaluation group.  For an example, see the implementation
 * in SimpleFitness.  This combination must be associative, that is, the order
 * in which these combinations occur should not change the final outcome.
 *
 * E.g., with a + b + c + d, it doesn't matter how you order the terms or
 * where you put parenthesis, the result will still be the same.
 *
 * @param other
 * @return
 */
void combine(final Fitness other);

}
