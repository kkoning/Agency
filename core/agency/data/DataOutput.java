package agency.data;

import agency.XMLConfigurable;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by liara on 9/30/16.
 */
public class DataOutput implements XMLConfigurable {

protected         String       outputFilename;
protected         CSVFormat    outputFormat;
protected         boolean      headersOutput;
protected         List<String> prefixHeaders;
transient private Lock         writeLock;
transient private CSVPrinter   csvOut;

public DataOutput(String outputFilename) {
  this(outputFilename, CSVFormat.DEFAULT);
}

public DataOutput(String outputFilename, CSVFormat format) {
  this();
  this.outputFilename = outputFilename;
  this.outputFormat = format;
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
  printRecord(data);
}

protected void writeHeaders(AgencyData ad) {
  List<String> headers = new ArrayList<>();
  headers.addAll(prefixHeaders);
  List<String> dataHeaders = ad.getHeaders();
  if (dataHeaders == null)
    throw new RuntimeException(ad.getClass().getCanonicalName() + " returned getHeaders()==null");
  else
    headers.addAll(dataHeaders);
  printRecord(headers);
}

public void printRecord(Iterable<?> values) {
  try {
    writeLock.lock();
    csvOut.printRecord(values);
  } catch (IOException ioe) {
    throw new RuntimeException("IO Error writing " + getOutputFilename());
  } finally {
    writeLock.unlock();
  }
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
  String formatString = e.getAttribute("format");
  if (formatString == null)
    this.outputFormat = CSVFormat.DEFAULT;
  else if (formatString.isEmpty())
    this.outputFormat = CSVFormat.DEFAULT;
  else if (formatString.equalsIgnoreCase("tsv"))
    this.outputFormat = CSVFormat.TDF;
  else if (formatString.equalsIgnoreCase("csv"))
    this.outputFormat = CSVFormat.DEFAULT;
  else if (formatString.equalsIgnoreCase("excel"))
    this.outputFormat = CSVFormat.EXCEL;
  else if (formatString.equalsIgnoreCase("rfc4180"))
    this.outputFormat = CSVFormat.RFC4180;
  else
    throw new RuntimeException("Unrecognized format + \"" + formatString + "\". " +
            this.getClass().getCanonicalName() + " recognized formats are " +
            "tsv, csv, excel, and rfc4180.  These correspond with the predefined formats from " +
            "apache commons csv, CSVFormat.___");

  openFile();
}

@Override
public void writeXMLConfig(Element e) {
  e.setAttribute("file", outputFilename);
  if (outputFormat == CSVFormat.DEFAULT)
    e.setAttribute("format", "csv");
  else if (outputFormat == CSVFormat.TDF)
    e.setAttribute("format", "tsv");
  else if (outputFormat == CSVFormat.EXCEL)
    e.setAttribute("format", "excel");
  else if (outputFormat == CSVFormat.RFC4180)
    e.setAttribute("format", "rfc4180");
}

@Override
public void resumeFromCheckpoint() {
  writeLock = new ReentrantLock();
  openFile();
}

void openFile() {
  try {
    String normalizedFilename = FilenameUtils.normalize(outputFilename);
    File file = new File(normalizedFilename);
    if (file.exists())
      if (file.length() > 4)
        headersOutput = true;
    FileOutputStream fos = new FileOutputStream(file, true);
    PrintStream ps = new PrintStream(fos);
    csvOut = new CSVPrinter(ps, outputFormat);
  } catch (Exception e) {
    throw new RuntimeException("Error opening file " + this.outputFilename, e);
  }
}

public void printComment(String comment) {
  try {
    writeLock.lock();
    csvOut.printComment(comment);
  } catch (IOException ioe) {
    throw new RuntimeException("IO Error writing " + getOutputFilename());
  } finally {
    writeLock.unlock();
  }
}

synchronized public void printRecord(Object... values) {
  try {
    writeLock.lock();
    csvOut.printRecord(values);
  } catch (IOException ioe) {
    throw new RuntimeException("IO Error writing " + getOutputFilename());
  } finally {
    writeLock.unlock();
  }
}

synchronized public void printRecords(Iterable<?> values) {
  try {
    writeLock.lock();
    csvOut.printRecord(values);
  } catch (IOException ioe) {
    throw new RuntimeException("IO Error writing " + getOutputFilename());
  } finally {
    writeLock.unlock();
  }
}

public void printRecords(Object... values) {
  try {
    writeLock.lock();
    csvOut.printRecords(values);
  } catch (IOException ioe) {
    throw new RuntimeException("IO Error writing " + getOutputFilename());
  } finally {
    writeLock.unlock();
  }
}

public void close() {
  writeLock.lock();
  try {
    csvOut.flush();
    csvOut.close();
  } catch (IOException e) {
    throw new RuntimeException("IO Error closing " + outputFilename, e);
  }
  writeLock.unlock();
}


}
