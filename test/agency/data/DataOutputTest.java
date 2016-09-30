package agency.data;

import org.apache.commons.csv.CSVFormat;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liara on 9/27/16.
 */
public class DataOutputTest extends DataOutput {

@Test
public void testOutput() {

  this.outputFilename = "test.csv";
  this.outputFormat = CSVFormat.DEFAULT;
  this.openFile();

  for (int i = 0; i < 5; i++) {
    for (int j = 0; j < 5; j++) {
      DataClass dc = new DataClass();
      dc.x = i;
      dc.y = j;
      dc.test = "The Product is " + (i * j);
      write(dc);
    }
  }
  close();

}


public class DataClass implements AgencyData {
  public int x;
  public int y;
  String test;

  @Override
  public List<String> getHeaders() {
    List<String> headers = new ArrayList<>();
    headers.add("x");
    headers.add("y");
    headers.add("test");
    return headers;
  }

  @Override
  public List<Object> getValues() {
    List<Object> values = new ArrayList<>();
    values.add(x);
    values.add(y);
    values.add(test);
    return values;
  }
}


}
