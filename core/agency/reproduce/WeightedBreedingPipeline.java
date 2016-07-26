package agency.reproduce;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import agency.Config;
import agency.Individual;
import agency.Population;
import agency.XMLConfigurable;
import agency.XMLConfigurable;

public class WeightedBreedingPipeline implements BreedingPipeline, XMLConfigurable {
	
	Map<BreedingPipeline, Float> componentStreams;

	TreeMap<Float, BreedingPipeline> weightMap;

	Float totalWeight;

	public WeightedBreedingPipeline() {
		componentStreams = new HashMap<>();
		weightMap = new TreeMap<>();
		totalWeight = 0f;
	}

	@Override
	public void readXMLConfig(Element e) {
		NodeList nl = e.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node instanceof Element) {
				// Don't care about node labels, everything should be a breedingpipeline
				// with a weight
				XMLConfigurable xc = Config.initializeXMLConfigurable((Element) node);
				if (!(xc instanceof BreedingPipeline))
					// TODO better error msg?
					throw new RuntimeException("Invalid child: " + xc);

				BreedingPipeline bp = (BreedingPipeline) xc;
				String weightString = ((Element) node).getAttribute("weight");
				Float weight = Float.parseFloat(weightString);
				componentStreams.put(bp, weight);
			}
		}
		rebuildWeightMap();
	}

	@Override
	public void writeXMLConfig(Document doc, Element e) {
		for (Map.Entry<BreedingPipeline, Float> entry : componentStreams.entrySet()) {
			// Element representing the component stream
			Element child = Config.createUnnamedElement(doc, entry.getKey());
			// but add a weight attribute
			child.setAttribute("weight", Float.toString(entry.getValue()));
			e.appendChild(child);
		}
	}

	@Override
	public void setSourcePopulation(Population pop) {
		for (BreedingPipeline bp : componentStreams.keySet())
			bp.setSourcePopulation(pop);
	}

	public void addPipeline(BreedingPipeline gen, Float weight) {
		componentStreams.put(gen, weight);
		rebuildWeightMap();
	}

	public void removePipeline(BreedingPipeline gen) {
		componentStreams.remove(gen);
		rebuildWeightMap();
	}

	private void rebuildWeightMap() {
		totalWeight = 0f;
		weightMap.clear();
		for (Entry<BreedingPipeline, Float> entry : componentStreams.entrySet()) {
			totalWeight += entry.getValue();
			weightMap.put(totalWeight, entry.getKey());
		}
	}

	@Override
	public Individual generate() {
		Random r = ThreadLocalRandom.current();
		Float index = r.nextFloat() * totalWeight;
		BreedingPipeline gen = weightMap.higherEntry(index).getValue();
		return gen.generate();
	}

	@Override
	public String toString() {
		return "WeightedBreedingPipeline [componentStreams=" + componentStreams + ", weightMap=" + weightMap
				+ ", totalWeight=" + totalWeight + "]";
	}

}
