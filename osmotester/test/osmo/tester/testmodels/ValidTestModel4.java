package osmo.tester.testmodels;

import osmo.tester.annotation.BeforeTest;
import osmo.tester.annotation.Guard;
import osmo.tester.annotation.Post;
import osmo.tester.annotation.RequirementsField;
import osmo.tester.annotation.Transition;
import osmo.tester.model.Requirements;

import java.io.PrintStream;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

/**
 * A test model with requirements that can all be covered.
 *
 * @author Teemu Kanstren
 */
public class ValidTestModel4 {
  @RequirementsField
  private final Requirements req = new Requirements();
  public static final String REQ_HELLO = "hello";
  public static final String REQ_WORLD = "world";
  public static final String REQ_EPIX = "epix";
  private final PrintStream out;

  public ValidTestModel4(PrintStream out) {
    this.out = out;
  }

  @BeforeTest
  public void reset() {
    req.clearCoverage();
  }

  @Guard("hello")
  public boolean helloCheck() {
    return !req.isCovered(REQ_HELLO) && !req.isCovered(REQ_WORLD) && !req.isCovered(REQ_EPIX);
  }

  @Transition("hello")
  public void transition1() {
    req.covered(REQ_HELLO);
    out.print(":hello");
  }

  @Guard("world")
  public boolean worldCheck() {
    return req.isCovered(REQ_HELLO) && !req.isCovered(REQ_WORLD) && !req.isCovered(REQ_EPIX);
  }

  @Transition("world")
  public void epix() {
    req.covered(REQ_WORLD);
    out.print(":world");
  }

  @Guard("epixx")
  public boolean kitted() {
    return req.isCovered(REQ_WORLD);
  }

  @Transition("epixx")
  public void epixx() {
    req.covered(REQ_EPIX);
    out.print(":epixx");
  }

  @Post("epixx")
  public void epixxO() {
    out.print(":epixx_oracle");
  }

  @Post
  public void stateCheck(Map<String, Object> p) {
    out.print(":gen_oracle");
    assertEquals("Post should have no parameters without one defined in pre.", 0, p.size());
  }

  @Post({"hello", "world"})
  public void sharedCheck() {
    out.print(":two_oracle");
  }
}
