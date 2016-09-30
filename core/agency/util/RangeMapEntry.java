package agency.util;

import org.apache.commons.lang3.Range;

public class RangeMapEntry<M, N> {
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
