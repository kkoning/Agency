package agency;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import agency.reproduce.ElitismSelector;
import agency.reproduce.FitnessProportionalSelector;
import agency.reproduce.RandomIndividualSelector;
import agency.reproduce.TournamentSelector;

public class Config {
	static DocumentBuilderFactory docFactory;
	static DocumentBuilder docBuilder;

	public static Map<String, Class<? extends XMLConfigurable>> classXMLTagNames;
	public static Map<Class<? extends XMLConfigurable>, String> xmlTagNameClass;

	static {
		classXMLTagNames = new HashMap<>();
		xmlTagNameClass = new HashMap<>();

		try {
			docFactory = DocumentBuilderFactory.newInstance();
			docBuilder = docFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException("Could not initialize configuration system. Check classpath");
		}
	}

	public static void registerClassXMLTag(Class<? extends XMLConfigurable> c) {
		try {
			XMLConfigurable xc = c.newInstance();
			String s = xc.getTagName();
			// TODO Check for collisions and throw error
			classXMLTagNames.put(s, c);
			xmlTagNameClass.put(c, s);
		} catch (Exception e) {
			throw new RuntimeException("Could not register class " + c.getName(), e);
		}
	}

	private static Class<? extends XMLConfigurable> getClassFromTagName(String tn) {
		return classXMLTagNames.get(tn);
	}

	private static String getStringForClassName(Class<? extends XMLConfigurable> c) {
		return xmlTagNameClass.get(c);
	}

	public static XMLConfigurable initializeXMLConfigurable(Element e) {
		try {
			String typeName = e.getAttribute("type");
			if (typeName == null || typeName.isEmpty()) {
				typeName = e.getTagName();
			}
			// TODO better error handling
			Class<? extends XMLConfigurable> c = getClassFromTagName(typeName);
			XMLConfigurable xc = c.newInstance();
			xc.readXMLConfig(e);
			return xc;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static Element createUnnamedElement(Document doc, XMLConfigurable xc) {
		String tagName = getStringForClassName(xc.getClass());
		if (tagName == null)
			throw new RuntimeException("No configuration exists for: " + xc.getClass().getName());
		Element e = doc.createElement(tagName);
		xc.writeXMLConfig(doc, e);
		return e;
	}
	
	public static Element createNamedElement(Document doc, XMLConfigurable xc, String name) {
		String typeName = getStringForClassName(xc.getClass());
		if (typeName == null)
			throw new RuntimeException("No configuration exists for: " + xc.getClass().getName());
		Element e = doc.createElement(name);
		if (!typeName.equals(name))
		  e.setAttribute("type", typeName);
		xc.writeXMLConfig(doc, e);
		return e;
	}

	/**
	 * @param e  The parent element
	 * @param tag The tag name, i.e., &lt;foo&gt;
	 * @return A (possibly empty) list of all child elements of the specified tag.
	 */
	public static List<Element> getChildElementsWithTag(Element e, String tag) {
		List<Element> toReturn = new ArrayList<>();
		NodeList nl = e.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node instanceof Element) {
				Element childElement = (Element) node;
				if (childElement.getTagName().equals(tag))
					toReturn.add(childElement);
			}
		}
		return toReturn;
	}
	
	/**
	 * @param e The parent element
	 * @param tag The tag name, i.e., &lt;foo&gt;
	 * @return Possibly a single child element with the specified tag.  Throws an
	 * exception if there are multiple tags.
	 * 
	 */
	public static Optional<Element> getChildElementWithTag(Element e, String tag) {
		List<Element> elements = getChildElementsWithTag(e, tag);
		if (elements.size() > 1)
			throw new RuntimeException("Cannot have multiple items of tag: " + tag);
		if (elements.size() == 0)
			return Optional.empty();
		else
			return Optional.of(elements.get(0));
	}
	
	
	public static Document newDocument() {
		return docBuilder.newDocument();
	}

	public static DocumentBuilder getDocBuilder() {
		return docBuilder;
	}
	
}
