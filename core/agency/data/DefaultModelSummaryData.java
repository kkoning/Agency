package agency.data;

import java.util.UUID;

/**
 * Created by liara on 9/30/16.
 */
public class DefaultModelSummaryData extends DataOutput implements ModelSummaryData {

public DefaultModelSummaryData() {
  super();
  String[] prefixHeaders = {"Generation", "Model"};
  this.setPrefixHeaders(prefixHeaders);
}

@Override
public void writeSummaryData(int generation, UUID modelUUID, AgencyData data) {
  if (data != null)
    write(data,generation,modelUUID);
}

}
