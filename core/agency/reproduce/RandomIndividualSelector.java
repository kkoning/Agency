package agency.reproduce;

import java.util.Iterator;
import java.util.stream.Stream;

import org.apache.commons.lang3.SerializationUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import agency.Config;
import agency.Individual;
import agency.Population;
import agency.XMLConfigurable;

public class RandomIndividualSelector implements BreedingPipeline, XMLConfigurable {

	Population pop;
	
	transient Stream<Individual> popStream;
	transient Iterator<Individual> i;
	
	@Override
	public void setSourcePopulation(Population pop) {
		this.pop = pop;
		popStream = pop.randomIndividuals();
		i = popStream.iterator();
	}
	
	@Override
	public Individual generate() {
		return SerializationUtils.clone(i.next());
	}

	@Override
	public void readXMLConfig(Element e) {
		// No config nencessary
	}

	@Override
	public void writeXMLConfig(Document doc, Element e) {
		// No config nencessary
	}

}
