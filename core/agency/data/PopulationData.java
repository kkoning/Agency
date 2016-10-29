package agency.data;

import agency.Population;
import agency.XMLConfigurable;

/**
 * Created by liara on 10/1/16.
 */
public interface PopulationData extends XMLConfigurable {
public void writePopulationData(int generation, Population pop);
}
