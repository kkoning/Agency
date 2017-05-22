package agency;

public class Account {

public static final long serialVersionUID = 1L;

private double balance;
private boolean balanceRestricted;

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
  this();
  verifySaneAmount(startingBalance);
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
  verifySaneAmount(amount);
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
  // Infinity/NaN check
  verifySaneAmount(amount);
  // But this must also be positive.
  if (amount < 0)
    throw new PaymentException("Cannot pay a negative amount.");
  
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

public void payTo(Account other, double amount) throws PaymentException {
  verifySaneAmount(amount);
  // Amount must be positive
  if (amount < 0)
    throw new PaymentException("Cannot pay a negative amount.");
  
  if (other == this)
    return; // No point in paying ourselves...
  pay(amount);
  other.receive(amount);
}

public void receiveFrom(Account other, double amount) throws PaymentException {
  verifySaneAmount(amount);
  // Amount must be positive
  if (amount < 0)
    throw new PaymentException("Cannot receive a negative amount.");
  
  other.pay(amount);
  receive(amount);
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

/**
 * Forcibly sets the account balance to this amount.  Generally, this
 * function should not be used except in cases where some hard-coded fitness
 * adjustment is necessary, e.g., bankruptcy.  It is marked as deprecated
 * for this reason; to help make sure it is not used unless necessary.
 *
 * @param balance
 */
@Deprecated
public void forceBalance(double balance) {
  verifySaneAmount(balance);
  this.balance = balance;
}

private void verifySaneAmount(double amount) {
  if (Double.isNaN(amount) || Double.isInfinite(amount))
    throw new RuntimeException();
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
public static class PaymentException extends Exception {
  private static final long serialVersionUID = 1L;

  double balance;
  double amount;

  public PaymentException(String message) {
    super(message);
  }
  
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
