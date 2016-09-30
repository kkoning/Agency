package agency.data;

import agency.XMLConfigurable;

import java.util.Map;
import java.util.UUID;

/**
 * Created by liara on 9/30/16.
 */
public interface ModelPerStepData extends XMLConfigurable {
public void writePerStepData(int generation, UUID modelUUID, Map<Integer, AgencyData> data);
}
