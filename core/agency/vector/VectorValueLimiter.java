package agency.vector;

import agency.XMLConfigurable;

public interface VectorValueLimiter extends XMLConfigurable {
void limit(Object[] genome, int pos);
}
