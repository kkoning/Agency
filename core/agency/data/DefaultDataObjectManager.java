package agency.data;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;

/**
 A utility class used to convert POJOs to AgencyData objects for output.
 */
public class DefaultDataObjectManager implements Serializable {

private Class expectedClass;

transient private Field[]      fields;
transient private List<String> fieldNames;

private boolean passThrough = false;


/**
 Expects a POJO, which it converts into an AgencyData object with reflection.
 However, if passed an AgencyData object, it will pass it straight through.

 @param o The data object, which must be the same every time process() is called
 on a given instance of this class.
 @return  */
public AgencyData process(Object o) {
  if (passThrough)
    return (AgencyData) o;
  if (fields == null)
    initialize(o);

  List<Object> data = new ArrayList<>();

  // Do the reflection to get values
  for (int i = 0; i < fields.length; i++) {
    try {
      data.add(fields[i].get(o));
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  return new SimpleAgencyData(fieldNames, data);

}

private void initialize(Object o) {

  if (o instanceof AgencyData) {
    passThrough = true;
    return;
  }

  expectedClass = o.getClass();

  // Fields including all parents that are public
  Set<Field> allFields = new HashSet<>();
  Field[] localFields = expectedClass.getDeclaredFields();
  for (Field f : localFields) {
    allFields.add(f);
    f.setAccessible(true);
  }
  Field[] parentPublicFields = expectedClass.getFields();
  for (Field f : parentPublicFields) allFields.add(f);

  fields = allFields.toArray(new Field[0]);

  // Sort fields alphabetically by name.
  Arrays.sort(fields, (o1, o2) -> o1.getName().compareTo(o2.getName()));

  // Create a fieldNames list, so it can always be reused in the created
  // AgencyData object.
  fieldNames = new ArrayList<>();
  for (int i = 0; i < fields.length; i++) {
    fieldNames.add(fields[i].getName());
  }

}

private class SimpleAgencyData implements AgencyData {

  List<String> headers;
  List<Object> data;

  SimpleAgencyData(List<String> headers, List<Object> data) {
    this.headers = headers;
    this.data = data;
  }

  @Override
  public List<String> getHeaders() {
    return headers;
  }

  @Override
  public List<Object> getValues() {
    return data;
  }
}


}
