package agency.eval;

import java.util.stream.Stream;

public interface Evaluator {
	Stream<EvaluationGroup> evaluate(Stream<EvaluationGroup> evaluationGroups);
	
}
