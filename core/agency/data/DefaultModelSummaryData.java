package agency.data;

import org.w3c.dom.Element;

import java.util.UUID;

/**
 * Created by liara on 9/30/16.
 */
public class DefaultModelSummaryData extends DataOutput implements ModelSummaryData {
private static final long serialVersionUID = 1L;

Integer everyNGenerations;
Boolean writeUUIDs;

boolean uuid; // To avoid awkward null check. Default is false.

public DefaultModelSummaryData() {
  super();
}

@Override
public void readXMLConfig(Element e) {
  super.readXMLConfig(e);
  String generationsString = e.getAttribute("generations");
  if (generationsString != null) {
    if (!generationsString.isEmpty()) {
      everyNGenerations = Integer.parseInt(generationsString);
    }
  }

  String uuidString = e.getAttribute("uuid");
  if (uuidString != null) {
    if (!uuidString.isEmpty()) {
      writeUUIDs = Boolean.parseBoolean(uuidString);
      uuid = writeUUIDs;
    }
  }

  String[] prefixHeaders;
  if (uuid) {
    prefixHeaders = new String[2];
    prefixHeaders[0] = "generation";
    prefixHeaders[1] = "model";
  } else {
    prefixHeaders = new String[1];
    prefixHeaders[0] = "generation";
  }

  this.setPrefixHeaders(prefixHeaders);

}

@Override
public void writeXMLConfig(Element e) {
  super.writeXMLConfig(e);
  if (everyNGenerations != null) {
    e.setAttribute("generations", everyNGenerations.toString());
  }

  if (writeUUIDs != null) {
    e.setAttribute("uuid", everyNGenerations.toString());
  }

}

@Override
public void writeSummaryData(int generation, UUID modelUUID, Object data) {
  if (everyNGenerations != null) {
    if (!((generation % everyNGenerations) == 0)) {
      return;
    }
  }

  if (data != null) {
    if (uuid)
      write(data, generation, modelUUID);
    else
      write(data, generation);

  }
}

}
