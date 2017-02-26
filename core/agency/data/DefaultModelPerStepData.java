package agency.data;

import agency.eval.EvaluationGroup;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by liara on 9/30/16.
 */
public class DefaultModelPerStepData extends DataOutput implements ModelPerStepData {

public DefaultModelPerStepData() {
  super();
  String[] prefixHeaders = {"Generation", "Model", "Step"};
  this.setPrefixHeaders(prefixHeaders);
}

@Override
public void writePerStepData(int generation,
                             UUID modelUUID,
                             List<EvaluationGroup.PerStepData> allStepsData) {
  if (allStepsData != null) {
    for (EvaluationGroup.PerStepData psd : allStepsData) {
      if (psd != null) {
        write(psd.data, generation, modelUUID, psd.step);
      }
    }
  }
}

}
