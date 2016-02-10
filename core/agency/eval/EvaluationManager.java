package agency.eval;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import agency.Environment;
import agency.Fitness;
import agency.Individual;

/**
 * This class handles the evaluation of individuals within a given environment.
 * 
 * @author kkoning
 *
 */
public class EvaluationManager implements Serializable {
	private static final long serialVersionUID = 9114978478024111905L;

	// Do something more interesting later, for now just use an in-process
	// evaluator
	private Evaluator evaluator = new InProcessEvaluator();

	// Do something more interesting later, for now just use a simple fitness
	// aggregator
	private FitnessAggregator fa = new MeanSimpleFitnessAggregator();

	public EvaluationManager() {

	}

	/**
	 * Evaluate the individuals in this environment. This involves creating
	 * evaluation groups, running those groups, collecting the results, grouping
	 * by individual, and then flattening to an aggregate (e.g., average)
	 * fitness.
	 * 
	 * @param env
	 *            The environment to evaluate
	 */
	public void evaluate(Environment env) {
		EvaluationGroupFactory egf = env.getEvaluationGroupFactory();
		Stream<EvaluationGroup> egs = egf.createEvaluationGroups(env);
		Stream<EvaluationGroup> evaluatedGroups = evaluator.evaluate(egs);

		Stream<Entry<Individual, Fitness>> resultStream;
		resultStream = evaluatedGroups.flatMap(eg -> eg.getResults());

		Map<Individual, List<Entry<Individual, Fitness>>> groupedResults;
		groupedResults = resultStream.collect(Collectors.groupingByConcurrent(Entry::getKey, Collectors.toList()));

		groupedResults.entrySet().stream().parallel().forEach(ent -> {
			List<Entry<Individual, Fitness>> entries = ent.getValue();
			Stream<Fitness> fitnesses = entries.stream().map(e -> e.getValue());
			Individual ind = ent.getKey();
			Fitness fit = fa.reduce(fitnesses);
			ind.setFitness(fit);
		});
	}

}
