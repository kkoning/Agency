package agency.eval;

import java.util.stream.Stream;

import agency.XMLConfigurable;

public interface Evaluator extends XMLConfigurable {
Stream<EvaluationGroup> evaluate(Stream<EvaluationGroup> evaluationGroups);

}
