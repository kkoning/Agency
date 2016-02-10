package agency;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public interface XMLConfigurable {
	public void readXMLConfig(Element e);
	public void writeXMLConfig(Document d, Element e);
	
	public default String getTagName() {
		return this.getClass().getSimpleName();
	}
}
