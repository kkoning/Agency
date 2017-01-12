package agency.data;

import agency.Environment;
import agency.XMLConfigurable;

/**
 * Created by liara on 9/26/16.
 */
public interface EnvironmentStatistics extends XMLConfigurable {
public void calculate(Environment env);

void close();
}
