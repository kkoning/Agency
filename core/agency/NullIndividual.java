package agency;

import java.util.UUID;

/**
 * A NullIndividual has no properties that can evolve. It is useful primarily
 * when the behavior of an entire species is hard-coded without evolvable
 * parameters, e.g., an agent that always mimics the behavior of other agents.
 *
 * @author liara
 */
public class NullIndividual
        extends AbstractIndividual {
public static final long serialVersionUID = 1L;

@Override
public Individual copy() {
  Individual toReturn = new NullIndividual();
  toReturn.setUUID(UUID.randomUUID()); // TODO: psuedo-random issue
  return toReturn;
}

}
