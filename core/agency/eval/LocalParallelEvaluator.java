package agency.eval;

import java.util.stream.Stream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class LocalParallelEvaluator implements Evaluator {

@Override
public Stream<EvaluationGroup> evaluate(Stream<EvaluationGroup> evaluationGroups) {
  return evaluationGroups
          .parallel()
          .map(eg -> {
            eg.run();
            return eg;
          });
}

@Override
public void close() {
  // Unnecessary
}

@Override
public void readXMLConfig(Element e) {
  // No configuration required
}

@Override
public void writeXMLConfig(Element e) {
  // No configuration required
}

@Override
public void resumeFromCheckpoint() {
  // No configuration required
}

}
