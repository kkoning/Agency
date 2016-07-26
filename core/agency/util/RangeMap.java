package agency.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import org.apache.commons.lang3.Range;

public class RangeMap<K, V> implements Serializable {
  private static final long serialVersionUID = 4945585013577014093L;

  TreeMap<K, RangeMapEntry<K, V>> treeMap = new TreeMap<>();

  public void put(Range<K> range, V object) {

    // Does the new range overlap with a portion already accounted for?
    // If so, throw exception.
    Map.Entry<K, RangeMapEntry<K, V>> possibleCollisionEntry = treeMap
        .floorEntry(range.getMaximum());
    if (possibleCollisionEntry != null) {
      Range<K> possibleCollisionRange = possibleCollisionEntry.getValue().range;
      if (possibleCollisionRange.isOverlappedBy(range)) {
        throw new UnsupportedOperationException(
            "Attempted to insert an overlapping entry into "
                + "a RangeMap.  Existing range '"
                + possibleCollisionRange.toString()
                + "' overlaps with candidate range '" + range.toString()
                + "'.  Overlapping region is "
                + possibleCollisionRange.intersectionWith(range));
      }
    }

    // Otherwise, go ahead and insert
    treeMap.put(range.getMinimum(), new RangeMapEntry<K,V>(range,object));
  }
  
  public void put(V object) {
    if (!(object instanceof HasRange))
      throw new RuntimeException("Cannot use RageMap.put(V) unless V extends HasRange");
    
    @SuppressWarnings("unchecked")
    HasRange<K> hr = (HasRange<K>) object;
    Range<K> r = hr.getRange();
    put(r,object);
  }

  public Optional<V> get(K location) {
    RangeMapEntry<K,V> rmh = treeMap.floorEntry(location).getValue();
    Range<K> r = rmh.range;
    if (r.contains(location))
      return Optional.of(rmh.object);
    return Optional.empty();
  }

  public Collection<RangeMapEntry<K, V>> entries() {
    return treeMap.values();
  }
  
  @Override
  public String toString() {
    return "RangeMap[" + treeMap + "]";
  }
  
  public int size() {
    return treeMap.size();
  }

}
