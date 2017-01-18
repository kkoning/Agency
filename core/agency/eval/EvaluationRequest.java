package agency.eval;

import java.io.Serializable;

public class EvaluationRequest implements Serializable {
public static final long serialVersionUID = 1L;

long serverTimeSent;
long clientTimeReceived;
long clientTimeReturned;
long serverTimeReceived;

EvaluationGroup[] evaluationGroups;
long[]            executionTimes;

public EvaluationRequest(EvaluationGroup[] evaluationGroups) {
  this.evaluationGroups = evaluationGroups;
  this.executionTimes = new long[evaluationGroups.length];
}

long networkDelay() {
  return clockTime() - executionTime();
}

long executionTime() {
  return clientTimeReturned - clientTimeReceived;
}

long clockTime() {
  return serverTimeReceived - serverTimeSent;
}

}
