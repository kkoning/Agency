package agency.vector;

import agency.XMLConfigurable;

public interface ValueMutator<T> extends XMLConfigurable {
	T mutate(T object);
}
