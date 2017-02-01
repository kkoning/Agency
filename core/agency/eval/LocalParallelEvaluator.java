package agency.eval;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.FutureTask;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class LocalParallelEvaluator
        implements Evaluator {
public static final long serialVersionUID = 1L;

@Override
public Stream<EvaluationGroup> evaluate(Stream<EvaluationGroup> evaluationGroups) {

  ExecutorService executorService = ForkJoinPool.commonPool();
  List<FutureTask> evaluationGroupTasks = new ArrayList<>();

  List<EvaluationGroup> evaluatedGroups =
          evaluationGroups.collect(Collectors.toList());

  for (EvaluationGroup eg : evaluatedGroups) {
    FutureTask ft = new FutureTask(eg,true);
    executorService.submit(ft);
    evaluationGroupTasks.add(ft);
  }

  // Wait to make sure they're all finished executing
  for (FutureTask ft : evaluationGroupTasks) {
    try {
      // Don't care about result, but get() blocks until complete
      ft.get();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    }
  }

  return evaluatedGroups.stream();
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
