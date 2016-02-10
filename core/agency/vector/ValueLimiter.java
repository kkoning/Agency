package agency.vector;

import agency.XMLConfigurable;

public interface ValueLimiter<T> extends XMLConfigurable {
	T limit(T value);
}
