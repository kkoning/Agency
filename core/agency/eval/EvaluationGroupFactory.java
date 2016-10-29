package agency.eval;

import java.util.stream.Stream;

import agency.Environment;
import agency.XMLConfigurable;

public interface EvaluationGroupFactory extends XMLConfigurable {
/**
 * @param env
 *         The source environment
 * @return A stream of EvaluationGroups, each with a set of agents to evaluate.
 */
Stream<EvaluationGroup> createEvaluationGroups(Environment env);
}
