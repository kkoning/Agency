package agency.vector;

import agency.XMLConfigurable;

public interface ValueInitializer<T> extends XMLConfigurable {
	T create();
}
