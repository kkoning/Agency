package agency.vector;

import agency.XMLConfigurable;

public interface VectorValueMutator extends XMLConfigurable {
void mutate(Object[] genome, int pos);
}
