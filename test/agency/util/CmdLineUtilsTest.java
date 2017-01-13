package agency.util;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.junit.Test;

/**
 * Created by liara on 1/12/17.
 */
public class CmdLineUtilsTest {

@Test
public void test() {
  String[] args = {"-foo", "bar", "-h"};

  Options options = new Options();
    Option foo = Option.builder("foo")
                       .hasArg()
                       .required()
                       .desc("It's a stilly test option.")
                       .build();
  options.addOption(foo);

  CmdLineUtils.parseOrHelpAndExit(CmdLineUtilsTest.class,args,options);


}

}
