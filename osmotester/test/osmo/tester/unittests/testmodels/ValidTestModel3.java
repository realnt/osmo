package osmo.tester.unittests.testmodels;

import osmo.tester.annotation.AfterSuite;
import osmo.tester.annotation.AfterTest;
import osmo.tester.annotation.BeforeSuite;
import osmo.tester.annotation.BeforeTest;
import osmo.tester.annotation.Guard;
import osmo.tester.annotation.Post;
import osmo.tester.annotation.Pre;
import osmo.tester.annotation.TestStep;
import osmo.tester.model.Requirements;

import java.io.PrintStream;

/**
 * A test model with reqs that can all be covered.
 *
 * @author Teemu Kanstren
 */
public class ValidTestModel3 {
  private final Requirements req = new Requirements();
  public static final String REQ_HELLO = "hello";
  public static final String REQ_WORLD = "world";
  public static final String REQ_EPIX = "epix";
  private final PrintStream out;

  public ValidTestModel3(PrintStream out) {
    this.out = out;
  }

  @BeforeTest
  public void reset() {
    req.clearCoverage();
  }

  @AfterTest
  @BeforeSuite
  @AfterSuite
  public void empty() {
  }

  @Guard("hello")
  public boolean helloCheck() {
    return !req.isCovered(REQ_HELLO) && !req.isCovered(REQ_WORLD) && !req.isCovered(REQ_EPIX);
  }

  @TestStep("hello")
  public void transition1() {
    req.covered(REQ_HELLO);
    out.print(":hello");
  }

  @Guard("world")
  public boolean worldCheck() {
    return req.isCovered(REQ_HELLO) && !req.isCovered(REQ_WORLD) && !req.isCovered(REQ_EPIX);
  }

  @TestStep("world")
  public void epix() {
    req.covered(REQ_WORLD);
    out.print(":world");
  }

  @Guard("epixx")
  public boolean kitted() {
    return req.isCovered(REQ_WORLD);
  }

  @Pre("epixx")
  public void epixxPre() {
    out.print(":epixx_pre");
  }

  @TestStep("epixx")
  public void epixx() {
    req.covered(REQ_EPIX);
    out.print(":epixx");
  }

  @Post("epixx")
  public void epixxO() {
    out.print(":epixx_oracle");
  }

  @Post("all")
  public void stateCheck() {
    out.print(":gen_oracle");
  }
}
