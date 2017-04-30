package agency.util;

/**
 * Created by liara on 2/9/17.
 */
public class Misc {

/**
 TODO: Modify to use a logging package.
 @param message
 */
public static final void BUG(String message) {
  throw new RuntimeException("BUG: " + message);
}

/**
 TODO: Modify to use a logging package.
 @param message
 */
public static final void WARN(String message) {
  System.err.println(message);
}

}
