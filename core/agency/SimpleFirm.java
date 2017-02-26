package agency;

public abstract class SimpleFirm<N extends Individual, M extends AgentModel>
        implements Agent<N, M> {

N manager;
M model;

public boolean bankrupt = false;

public Account account = new Account();

@Override
public N getManager() {
  return manager;
}

@Override
public void setManager(N manager) {
  this.manager = manager;
}

public M getModel() {
  return model;
}

public void setModel(M model) {
  this.model = model;
}

/**
 * @return a SimpleFitness equal to the firm's balance.
 */
public SimpleFitness getFitness() {
  SimpleFitness fit = new SimpleFitness(account.balance);
  return fit;
}

public Account getAccount() {
  return account;
}

public boolean isBankrupt() {
  return bankrupt;
}

public void goBankrupt() {
  bankrupt = true;
}

}
