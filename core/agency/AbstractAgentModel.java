package agency;

import java.io.PrintStream;

import static agency.util.Misc.BUG;

/**
 * Created by liara on 2/9/17.
 */
public abstract class AbstractAgentModel implements AgentModel {

public Integer maxSteps;
public int currentStep = 0;

public PrintStream debugOut;


public void enableDebug(PrintStream out) {
  debugOut = out;
}

public boolean isDebugEnabled() {
  return (debugOut != null);
}

@Override
public void init() {
  if (maxSteps == null)
    BUG("Max Steps is null at initialization; model will run forever");
}


}
