package agency.util;

/**
 * Created by liara on 2/9/17.
 */
public class Misc {

public static final void BUG(String message) {
  throw new RuntimeException("BUG: " + message);
}

}
