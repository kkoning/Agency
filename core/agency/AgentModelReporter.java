package agency;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import agency.eval.EvaluationGroup;

public class AgentModelReporter implements XMLConfigurable, Serializable {
  private static final long   serialVersionUID   = 5428971414269398005L;

  private static final String SEPARATOR          = "\t";

  private String              summaryFilename;
  transient PrintStream       summaryOut;
  transient Class<?>          summaryDataClass;
  transient ArrayList<Field>  summaryFields;
  transient ArrayList<String> summaryFieldNames;
  transient private boolean   writeSummaryHeader = true;
  transient private boolean   seenSummaryData;

  private String              perStepFilename;
  transient PrintStream       perStepOut;
  transient Class<?>          perStepDataClass;
  transient ArrayList<Field>  perStepFields;
  transient ArrayList<String> perStepFieldNames;
  transient private boolean   writePerStepHeader = true;
  transient private boolean   seenPerStepData;

  private boolean             resume;

  @Override
  public void readXMLConfig(Element e) {
    // TODO Auto-generated method stub
    summaryFilename = e.getAttribute("summaryFile");
    perStepFilename = e.getAttribute("perStepFile");
    start();

  }

  @Override
  public void writeXMLConfig(Document d, Element e) {
    // TODO Auto-generated method stub
    e.setAttribute("summaryFile", summaryFilename);
    e.setAttribute("perStepFile", perStepFilename);
  }

  public void perStepData(int generation, EvaluationGroup eg) {

    try {

      Map<Integer, Object> perStepData = eg.getPerStepData();
      if (perStepData == null)
        return;
      if (perStepData.isEmpty())
        return;

      for (Map.Entry<Integer, Object> bob : perStepData.entrySet()) {

        // The first time
        if (!seenPerStepData) {
          // Determine the fields and the field names
          FH fh = extractFields(bob.getValue());
          this.perStepFieldNames = fh.fieldNames;
          this.perStepFields = fh.fields;

          // Possibly write the header
          if (writePerStepHeader) {
            String[] prefix = new String[3];
            prefix[0] = "Generation";
            prefix[1] = "ModelUUID";
            prefix[2] = "Step";
            printHeader(perStepOut, prefix, perStepFieldNames);
            writePerStepHeader = false;
          }
        }

        // Write a line of data
        String[] prefix = new String[3];
        prefix[0] = Integer.toString(generation);
        prefix[1] = eg.getId().toString();
        prefix[2] = bob.getKey().toString();

        ArrayList<String> data = new ArrayList<>(perStepFields.size());
        for (Field f : perStepFields) {
          data.add(f.get(bob.getValue()).toString());
        }
        perStepOut.println(constructLine(prefix, data));

      }

    } catch (IllegalArgumentException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  public void summaryData(int generation, EvaluationGroup eg) {
    // The first time
    if (!seenSummaryData) {
      // Determine the fields and the field names
      FH fh = extractFields(eg.getSummaryData());
      this.summaryFieldNames = fh.fieldNames;
      this.summaryFields = fh.fields;

      // Possibly write the header
      if (writeSummaryHeader) {
        String[] prefix = new String[2];
        prefix[0] = "Generation";
        prefix[1] = "ModelUUID";
        printHeader(summaryOut, prefix, summaryFieldNames);
        writeSummaryHeader = false;
      }
    }

    try {
      // Write a line of data
      String[] prefix = new String[2];
      prefix[0] = Integer.toString(generation);
      prefix[1] = eg.getId().toString();
      ArrayList<String> data = new ArrayList<>(summaryFields.size());
      for (Field f : summaryFields) {
        Object o = f.get(eg.getSummaryData());
        if (o == null)
          data.add("null");
        else
        data.add(o.toString());
      }
      summaryOut.println(constructLine(prefix, data));
    } catch (IllegalArgumentException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  private final void printHeader(PrintStream out, String[] prefix, ArrayList<?> data) {
    out.println(constructLine(prefix, data));
  }

  private final StringBuffer constructLine(String[] prefix, ArrayList<?> data) {
    StringBuffer toReturn = new StringBuffer();
    for (String s : prefix) {
      toReturn.append(s + SEPARATOR);
    }

    int numCols = data.size();
    for (int i = 0; i < numCols; i++) {
      toReturn.append(data.get(i).toString());
      if (i + 1 < numCols)
        toReturn.append(SEPARATOR);

    }

    return toReturn;
  }

  void start() {
    // Open output files
    // TODO: Make each one optional, at least one required...
    summaryOut = openPrintStream(summaryFilename);
    perStepOut = openPrintStream(perStepFilename);
  }

  void close() {
    perStepOut.flush();
    perStepOut.close();
    summaryOut.flush();
    summaryOut.close();
  }

  private PrintStream openPrintStream(String fileName) {
    PrintStream toReturn = null;
    File f = new File(fileName);
    FileOutputStream fos;
    try {
      fos = new FileOutputStream(f, true);
      toReturn = new PrintStream(fos);
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return toReturn;
  }

  private FH extractFields(Object o) {
    FH toReturn = new FH();

    Class<?> c = o.getClass();
    Set<Field> seenFields = new HashSet<>();

    Field[] declaredFields = c.getDeclaredFields();
    for (Field f : declaredFields) {
      if (f.getName().equals("this$0"))
        continue;

      f.setAccessible(true);
      toReturn.fieldNames.add(f.getName());
      toReturn.fields.add(f);
      seenFields.add(f);
    }

    Field[] otherFields = c.getFields();
    for (Field f : otherFields) {

      if (!seenFields.contains(f)) {
        f.setAccessible(true);
        toReturn.fieldNames.add(f.getName());
        toReturn.fields.add(f);
      }
    }

    return toReturn;
  }

  private class FH {
    ArrayList<String> fieldNames = new ArrayList<>();
    ArrayList<Field>  fields     = new ArrayList<>();
  }

  public static void main(String[] args) {
    AgentModelReporter amr = new AgentModelReporter();

    FH fh = amr.extractFields(amr);
    System.out.println("debug here");

  }

}
