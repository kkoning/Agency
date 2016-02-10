package agency.eval;

import java.io.Serializable;

public class EvaluationRequest implements Serializable {
	private static final long serialVersionUID = 2596102347817005463L;

	long serverTimeSent;
	long clientTimeReceived;
	long clientTimeReturned;
	long serverTimeReceived;
	
	EvaluationGroup[] evaluationGroups;
	long[] executionTimes;
	
	public EvaluationRequest(EvaluationGroup[] evaluationGroups) {
		this.evaluationGroups = evaluationGroups;
		this.executionTimes = new long[evaluationGroups.length];
	}
	
	
	long executionTime() {
		return clientTimeReturned - clientTimeReceived;
	}
	
	long clockTime() {
		return serverTimeReceived - serverTimeSent;
	}
	
	long networkDelay() {
		return clockTime() - executionTime();
	}
	
}
