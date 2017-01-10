package agency.data;

import agency.XMLConfigurable;

import java.util.Map;
import java.util.UUID;

/**
 Created by liara on 9/30/16.
 */
public interface ModelPerStepData extends XMLConfigurable {
void writePerStepData(int generation, UUID modelUUID, Map<Integer, Object> data);
}
