package agency.vector;

import agency.XMLConfigurable;

public interface VectorInitializer<T> extends XMLConfigurable {
/**
 *
 *
 * @param i
 * @return
 */
T initialize(int i);

}
