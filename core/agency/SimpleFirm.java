package agency;

public abstract class SimpleFirm<T extends Individual> implements Agent<T> {
  protected T    manager;
  public Account account;

  @Override
  public T getManager() {
    return manager;
  }

  @Override
  public void setManager(T manager) {
    this.manager = manager;
  }

  /**
   * @return a SimpleFitness equal to the firm's balance.
   */
  public SimpleFitness getFitness() {
    SimpleFitness fit = new SimpleFitness(account.balance);
    return fit;
  }

}
