package agency.data;

import agency.XMLConfigurable;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Element;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 Created by liara on 9/30/16.

 TODO: Write documentation for this class.

 */
public class DataOutput implements XMLConfigurable {

private static final String separatorChar = ",";
protected String                   outputFilename;
protected boolean                  headersOutput;
protected List<String>             prefixHeaders;
protected DefaultDataObjectManager ddom;

transient private Lock                 writeLock;
transient private BufferedOutputStream out;

public DataOutput(String outputFilename) {
  this();
  this.outputFilename = outputFilename;
  openFile();
}

public DataOutput() {
  writeLock = new ReentrantLock();
  prefixHeaders = new ArrayList<>();
}

public void setPrefixHeaders(String[] headers) {
  if (prefixHeaders.size() != 0)
    throw new RuntimeException("Can only set prefix headers once");
  if (headersOutput)
    throw new RuntimeException("Cannot set prefix headers after headers have already been output");

  for (String header : headers)
    prefixHeaders.add(header);
}

public void write(Object pojoObject, Object... prefixes) {
  if (ddom == null)
    ddom = new DefaultDataObjectManager();

  AgencyData ad = ddom.process(pojoObject);
  write(ad,prefixes);
}


public void write(AgencyData ad, Object... prefixes) {
  /*
   * Write the header if it has not yet been written.
   */
  if (!headersOutput) {
    // First (redundant) check improves performance by avoiding the use of the lock
    // after the headers have been clearly output.
    try {
      writeLock.lock();
      // Check again, otherwise the check is outside the lock.
      if (!headersOutput) {
        writeHeaders(ad);
        headersOutput = true;
      }
    } finally {
      writeLock.unlock();
    }
  }

  // Check to make sure prefixes sizes match, throw error if not.
  if (prefixHeaders.size() != prefixes.length)
    throw new RuntimeException(outputFilename + " tried to output " + prefixes.length + " prefixes " +
            "but the number of prefix headers was " + prefixHeaders.size());

  ArrayList<Object> data = new ArrayList<>();
  for (Object prefix : prefixes)
    data.add(prefix);
  data.addAll(ad.getValues());
  writeLine(data);
}

private void writeHeaders(AgencyData ad) {
  List<Object> headers = new ArrayList<>();
  headers.addAll(prefixHeaders);
  List<String> dataHeaders = ad.getHeaders();
  if (dataHeaders == null)
    throw new RuntimeException(ad.getClass().getCanonicalName() + " returned getHeaders()==null");
  else
    headers.addAll(dataHeaders);
  writeLine(headers);
}

public void writeLine(Iterable<Object> values) {
  String line = constructLine(values);
  byte[] lineBytes = line.getBytes(StandardCharsets.UTF_8);
  try {
    writeLock.lock();
    out.write(lineBytes);
  } catch (IOException ioe) {
    throw new RuntimeException("IO Error writing " + getOutputFilename());
  } finally {
    writeLock.unlock();
  }
}

private static String constructLine(Iterable<Object> values) {
  StringBuffer sb = new StringBuffer();
  Iterator<Object> i = values.iterator();
  while (i.hasNext()) {
    Object toOutput = i.next();
    if (toOutput != null)
      sb.append(toOutput.toString());
    if (i.hasNext())
      sb.append(separatorChar);
  }
  sb.append("\n");
  return sb.toString();
}


protected String getOutputFilename() {
  return this.outputFilename;
}

@Override
public void readXMLConfig(Element e) {
  this.outputFilename = e.getAttribute("file");
  if (this.outputFilename == null)
    throw new RuntimeException(this.getClass().getCanonicalName() +
            " requires an output file.  Specify with file=\"filename\"");
  openFile();
}

@Override
public void writeXMLConfig(Element e) {
  e.setAttribute("file", outputFilename);
}

@Override
public void resumeFromCheckpoint() {
  writeLock = new ReentrantLock();
  openFile();
}

private void openFile() {
  try {
    String normalizedFilename = FilenameUtils.normalize(outputFilename);
    File file = new File(normalizedFilename);
    if (file.exists())
      if (file.length() > 4)
        headersOutput = true;
    FileOutputStream fos = new FileOutputStream(file, true);
    this.out = new BufferedOutputStream(fos);
  } catch (Exception e) {
    throw new RuntimeException("Error opening file " + this.outputFilename, e);
  }
}

public void close() {
  writeLock.lock();
  try {
    out.flush();
    out.close();
  } catch (IOException e) {
    throw new RuntimeException("IO Error closing " + outputFilename, e);
  }
  writeLock.unlock();
}


}
