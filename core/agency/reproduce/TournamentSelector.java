package agency.reproduce;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.apache.commons.lang3.SerializationUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import agency.Config;
import agency.Individual;
import agency.Population;
import agency.XMLConfigurable;

public class TournamentSelector implements BreedingPipeline, XMLConfigurable {
	int touramentSize = 2;  // default to 2 individuals, weakest selection pressure
	int topIndividuals = 1;
	
	private List<Individual> individuals;
	
	private Queue<Individual> inPipeline = new LinkedBlockingQueue<>();

	public TournamentSelector() {
	}

	public TournamentSelector(int tournamentSize) {
		this.touramentSize = tournamentSize;
	}

	public TournamentSelector(int tournamentSize, int topIndividuals) {
		this.touramentSize = tournamentSize;
		this.topIndividuals = topIndividuals;
	}


	@Override
	public void readXMLConfig(Element e) {
		touramentSize = Integer.parseInt(e.getAttribute("touramentSize"));
		topIndividuals = Integer.parseInt(e.getAttribute("topIndividuals"));
	}

	@Override
	public void writeXMLConfig(Document doc, Element e) {
		e.setAttribute("touramentSize", Integer.toString(touramentSize));
		e.setAttribute("topIndividuals", Integer.toString(topIndividuals));
	}
	
	@Override
	public void setSourcePopulation(Population pop) {
		individuals = pop.allIndividuals().map((i) -> SerializationUtils.clone(i)).collect(Collectors.toList());
	}
	
	@Override
	public Individual generate() {
		if (!inPipeline.isEmpty()) {
			Individual toReturn = inPipeline.remove();
			return SerializationUtils.clone(toReturn);
		}
		
		List<Individual> participants = new ArrayList<>(touramentSize);
		Random r = ThreadLocalRandom.current();
		while (participants.size() < touramentSize) {
			participants.add(individuals.get(r.nextInt(touramentSize)));
		}
		Collections.sort(participants, new FitnessComparator());
		
		for (int j = 1; j < topIndividuals; j++) {
			inPipeline.add(participants.get(j));
		}
		Individual toReturn = participants.get(0);
		
		return SerializationUtils.clone(toReturn);
	}
	
	

	public int getTouramentSize() {
		return touramentSize;
	}

	public void setTouramentSize(int touramentSize) {
		this.touramentSize = touramentSize;
	}

	public int getTopIndividuals() {
		return topIndividuals;
	}

	public void setTopIndividuals(int topIndividuals) {
		this.topIndividuals = topIndividuals;
	}

}
