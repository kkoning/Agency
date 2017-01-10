package agency.data;

import agency.XMLConfigurable;

import java.util.UUID;

/**
 * Created by liara on 9/30/16.
 */
public interface ModelSummaryData extends XMLConfigurable {

public void writeSummaryData(int generation, UUID modelUUID, Object data);

}
