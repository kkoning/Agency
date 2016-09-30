package agency.util;

import java.util.IdentityHashMap;
import java.util.Map;

public class ObjectCounter extends IdentityHashMap<Object, Integer> {
private static final long serialVersionUID = -3499538508462895455L;

long totalObservations = 0;
long totalNullObservations;

public void observe(Object o) {
  if (o == null)
    totalNullObservations++;
  else if (!this.containsKey(o)) {
    put(o, 1);
  } else {
    Integer currentObservations = get(o);
    put(o, currentObservations + 1);
  }
  totalObservations++;
}

public int getCount(Object o) {
  return get(o);
}

public long getTotalObservations() {
  return totalNullObservations;
}

public int getNumDifferentObjects() {
  return this.size();
}

@Override
public String toString() {
  StringBuffer sb = new StringBuffer();
  sb.append("ObjectCounter[\n");
  sb.append("null=" + totalNullObservations + "\n");
  for (Map.Entry<Object, Integer> entry : this.entrySet()) {
    sb.append(entry.toString());
    sb.append("\n");
  }
  sb.append("], ");
  sb.append(this.size());
  sb.append(" objects\n");
  return sb.toString();
}
}
