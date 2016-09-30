package agency.data;

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
public void writePerStepData(int generation, UUID modelUUID, Map<Integer, AgencyData> allStepsData) {
  if (allStepsData != null) {
    for (Map.Entry<Integer, AgencyData> perStepData : allStepsData.entrySet()) {
      Integer step = perStepData.getKey();
      AgencyData stepData = perStepData.getValue();
      write(stepData, generation, modelUUID, step);
    }
  }
}

}
