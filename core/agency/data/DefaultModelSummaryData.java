package agency.data;

import org.w3c.dom.Element;

import java.util.UUID;

/**
 * Created by liara on 9/30/16.
 */
public class DefaultModelSummaryData extends DataOutput implements ModelSummaryData {

Integer everyNGenerations;

public DefaultModelSummaryData() {
  super();
  String[] prefixHeaders = {"generation", "model"};
  this.setPrefixHeaders(prefixHeaders);
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

}

@Override
public void writeXMLConfig(Element e) {
  super.writeXMLConfig(e);
  if (everyNGenerations != null) {
    e.setAttribute("generations", everyNGenerations.toString());
  }
}

@Override
public void writeSummaryData(int generation, UUID modelUUID, Object data) {
  if (everyNGenerations != null) {
    if (!((generation % everyNGenerations) == 0)) {
      return;
    }
  }

  if (data != null)
    write(data, generation, modelUUID);
}

}
