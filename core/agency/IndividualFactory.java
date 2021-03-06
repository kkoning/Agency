package agency;

public interface IndividualFactory<T extends Individual> extends XMLConfigurable {
/**
 * Creates a new individual, not based on any parent individuals. Typically,
 * the values will be based on how the IndividualFactory was configured at
 * program start.
 * <p>
 * When a population is first initialized, its individuals will have been
 * generated by this function.
 *
 * @return A new individual.
 */
T create();

}
