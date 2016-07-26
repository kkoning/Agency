package agency.eval;

import java.util.stream.Stream;

public class InProcessEvaluator implements Evaluator {

	@Override
	public Stream<EvaluationGroup> evaluate(Stream<EvaluationGroup> evaluationGroups) {
		return evaluationGroups
//				.parallel()
				.map(eg -> {
			eg.run();
			return eg;
		});
	}
	
}
