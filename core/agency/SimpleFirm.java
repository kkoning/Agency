package agency;

public abstract class SimpleFirm<T extends Individual> implements Agent<T> {
  protected T    manager;
  public Account account;
  AgentModel     model;

  protected SimpleFirm() {
    account = new Account();
  }

  @Override
  public T getManager() {
    return manager;
  }

  @Override
  public void setManager(T manager) {
    this.manager = manager;
  }
  
  public AgentModel getModel() {
    return model;
  }

  public void setModel(AgentModel model) {
    this.model = model;
  }

  /**
   * @return a SimpleFitness equal to the firm's balance.
   */
  public SimpleFitness getFitness() {
    SimpleFitness fit = new SimpleFitness(account.balance);
    return fit;
  }

}
