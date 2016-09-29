package agency.data;

import agency.XMLConfigurable;
import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by kkoning on 9/26/16.
 */
public class ReflectionDataOutput implements Serializable, XMLConfigurable {

String separator = "\t";
String outputFileName;

private           Class<?>          outputClass;
private           String[]          prefixLabels;
transient private ArrayList<String> fieldLabels;
transient private ArrayList<Field>  fields;

transient private PrintStream out;

private boolean typeDetermined  = false;
private boolean fieldsExtracted = false;
private boolean headerWritten   = false;


@Override
public void readXMLConfig(Element e) {

}

@Override
public void writeXMLConfig(Document d, Element e) {

}

public void setPrefixLabels(String... prefixLabels) {
  if (prefixLabels != null)
    this.prefixLabels = prefixLabels;
  else
    throw new RuntimeException("Prefix labels can only be set once, before output");
}

private String constructHeaderLine() {
  StringBuffer sb = new StringBuffer();
  for (String prefixLabel : prefixLabels) {
    sb.append(prefixLabel);
    sb.append(separator);
  }
  for (int i = 0; i < fieldLabels.size(); i++) {
    sb.append(fieldLabels.get(i));
    // separator on all but last column
    if (i < (fieldLabels.size() - 1))
      sb.append(separator);
  }
  return sb.toString();
}

private String constructOutputLine(Object o, Object... prefixes) {
  StringBuffer sb = new StringBuffer();
  if (prefixes.length != prefixLabels.length)
    throw new RuntimeException("Error writing " + this.outputFileName + ", # of output prefixes doesn't match # of prefix headers");
  for (Object prefix : prefixes) {
    sb.append(prefix.toString());
    sb.append(separator);
  }
  try {
    for (int i = 0; i < fields.size(); i++) {
      Field fieldOfProp = fields.get(i);
      Object propToWrite = fieldOfProp.get(o);
      sb.append(propToWrite);
      // separator on all but last column
      if (i < (fields.size() - 1))
        sb.append(separator);
    }
  } catch (IllegalAccessException iae) {
    throw new RuntimeException("Should be impossible?" + iae);
  }
  return sb.toString();
}

private void initializeOutput() {
  // fix prefix headers
  if (prefixLabels == null)
    prefixLabels = new String[0];

  try {
    String fileName = FilenameUtils.normalize(outputFileName);
    File file = new File(fileName);
    // If a new file, write the header
    boolean writeHeader = !file.exists();
    FileOutputStream fos = new FileOutputStream(file, true);
    out = new PrintStream(fos);
    if (writeHeader) {
      String line = constructHeaderLine();
      out.println(line);
    }
  } catch (Exception e) {
    throw new RuntimeException("Error writing file " + this.outputFileName);
  }


}

public void close() {
  try {
    if (out != null)
      out.flush();
  } finally {
    out.close();
  }
}

public void write(Object o, Object... prefixes) {
  if (!typeDetermined) {
    this.outputClass = o.getClass();
    extractFields(this.outputClass);
    initializeOutput();
  } else {
    // if type is determined but fields == null, we're probably resuming from checkpoint.
    if (fields == null) {
      extractFields(o.getClass());
    }
  }

  // Check to make sure the object is of the correct type.
  // E.g., we can only consistently write one type of object.
  if (!o.getClass().equals(outputClass))
    throw new RuntimeException("ReflectionDataOuput started with class " +
            outputClass.getCanonicalName() + " but received and object of type " +
            o.getClass().getCanonicalName());

  // Otherwise, write a line.
  String line = constructOutputLine(o, prefixes);
  out.println(line);


}


private void extractFields(Class<?> c) {
  // Reset these (we might be resuming from a checkpoint?)
  fieldLabels = new ArrayList<>();
  fields = new ArrayList<>();

  Set<Field> allFields = new HashSet<>();

  Field[] declaredFields = c.getDeclaredFields();
  Field[] localFields = c.getFields();
  for (Field f : declaredFields)
    allFields.add(f);
  for (Field f : localFields)
    allFields.add(f);

  for (Field f : allFields) {
    // Don't output anything from the containing class/object
    if (f.getName().equals("this$0"))
      continue;

    // Don't output transient fields
    if (Modifier.isTransient(f.getModifiers()))
      continue;

    // Check to see if it's a basic type
    boolean isBasicType = isBasicType(f);

    // if it is, we can print it out directly.
    if (isBasicType) {
      fieldLabels.add(f.getName());
      fields.add(f);
    } else {
      // Not supported at this time.
      throw new RuntimeException("ReflectionDataOutput can write objects with basic types only (int, double, etc...), not "
        + f.getName() + " of type " + f.getType().getCanonicalName());
    }
  }
}

private boolean isBasicType(Field f) {
  boolean isBasicType = false;
  if (f.getType().equals(java.lang.Boolean.class) | f.getType().equals(Boolean.TYPE))
    isBasicType = true;
  if (f.getType().equals(java.lang.Byte.class) | f.getType().equals(Byte.TYPE))
    isBasicType = true;
  if (f.getType().equals(java.lang.Character.class) | f.getType().equals(Character.TYPE))
    isBasicType = true;
  if (f.getType().equals(java.lang.Double.class) | f.getType().equals(Double.TYPE))
    isBasicType = true;
  if (f.getType().equals(java.lang.Float.class) | f.getType().equals(Float.TYPE))
    isBasicType = true;
  if (f.getType().equals(java.lang.Integer.class) | f.getType().equals(Integer.TYPE))
    isBasicType = true;
  if (f.getType().equals(java.lang.Number.class))
    isBasicType = true;
  if (f.getType().equals(java.lang.Short.class) | f.getType().equals(Short.TYPE))
    isBasicType = true;
  if (f.getType().equals(java.lang.String.class))
    isBasicType = true;


  return isBasicType;
}

}
