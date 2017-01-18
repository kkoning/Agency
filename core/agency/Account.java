package agency;

public class Account {

public static final long serialVersionUID = 1L;

double balance;
boolean balanceRestricted;

/**
 * Creates an account with a zero balance which can be negative.
 */
public Account() {
  balance = 0;
  balanceRestricted = false;
}

/**
 * Creates an account with the specified starting balance.
 *
 * @param startingBalance
 */
public Account(double startingBalance) {
  balance = startingBalance;
}

/**
 * Convenience method for processing payments between accounts which is
 * automatically balanced.
 *
 * @param source
 *         The source of the funds.
 * @param destination
 *         The account where the funds are deposited
 * @param amount
 *         The amount of the transaction
 */
public static void payment(
        Account source,
        Account destination,
        double amount) throws PaymentException {
  source.pay(amount);
  destination.receive(amount);
}

public void receive(double amount) {
  balance += amount;
}

/**
 * Pays the specified amount from this account.
 *
 * @param amount
 *         The amount this account is decreased
 * @throws PaymentException
 *         if balanceRestricted is true and the payment would cause the account
 *         to go negative.
 */
public void pay(double amount) throws PaymentException {
  if (balanceRestricted) {
    if (balance > amount) {
      balance -= amount;
      return;
    } else {
      throw new PaymentException(balance, amount);
    }
  } else {
    balance -= amount;
  }
}

public boolean isBalanceRestricted() {
  return balanceRestricted;
}

public void setBalanceRestricted(boolean balanceRestricted) {
  this.balanceRestricted = balanceRestricted;
}

public double getBalance() {
  return balance;
}

@Override
public String toString() {
  return super.toString() +
         "[balance=" +
         balance +
         ", balanceRestricted=" +
         balanceRestricted +
         "]";
}

/**
 * Thrown when an attempt is made to make a payment on an account where
 * balanceRestricted is true and the the payment would make the balance of the
 * account go negative.
 *
 * @author kkoning
 */
public static class PaymentException
        extends RuntimeException {
  private static final long serialVersionUID = 1L;

  double balance;
  double amount;

  public PaymentException(double balance, double amount) {
    super("Insufficient balance (" + balance + ") to pay " + amount);
    this.balance = balance;
    this.amount = amount;
  }

  /**
   * @return The additional balance necessary to complete the transaction
   */
  public double getShortfall() {
    return amount - balance;
  }

  public double getBalance() {
    return balance;
  }

  public double getAmount() {
    return amount;
  }

}


}
