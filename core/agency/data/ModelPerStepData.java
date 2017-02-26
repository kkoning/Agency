package agency.data;

import agency.XMLConfigurable;
import agency.eval.EvaluationGroup;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 Created by liara on 9/30/16.
 */
public interface ModelPerStepData extends XMLConfigurable {
void writePerStepData(int generation, UUID modelUUID, List<EvaluationGroup.PerStepData> data);

void close();
}
