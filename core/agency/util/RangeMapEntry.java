package agency.util;

import org.apache.commons.lang3.Range;

import java.io.Serializable;

public class RangeMapEntry<M, N> implements Serializable {
Range<M> range;
N        object;

RangeMapEntry(Range<M> range, N object) {
  this.range = range;
  this.object = object;
}

@Override
public String toString() {
  return "RangeMapHelper[range=" + range + ", object=" + object + "]";
}

public Range<M> getRange() {
  return range;
}

public void setRange(Range<M> range) {
  this.range = range;
}

public N getObject() {
  return object;
}

public void setObject(N object) {
  this.object = object;
}

}
