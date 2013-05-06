package osmo.tester.testmodels;

import osmo.tester.annotation.AfterSuite;
import osmo.tester.annotation.AfterTest;
import osmo.tester.annotation.BeforeSuite;
import osmo.tester.annotation.BeforeTest;
import osmo.tester.annotation.Guard;
import osmo.tester.annotation.Post;
import osmo.tester.annotation.Pre;
import osmo.tester.annotation.RequirementsField;
import osmo.tester.annotation.TestStep;
import osmo.tester.model.Requirements;

import java.io.PrintStream;
import java.util.Map;

import static junit.framework.Assert.*;

/**
 * A test model with requirements that can all be covered.
 *
 * @author Teemu Kanstren
 */
public class TestStepModel {
  @RequirementsField
  private final Requirements tags = new Requirements();
  public static final String TAG_HELLO = "hello";
  public static final String TAG_WORLD = "world";
  public static final String TAG_EPIX = "epix";
  private final PrintStream out;

  public TestStepModel(PrintStream out) {
    this.out = out;
  }

  @BeforeTest
  public void reset() {
    tags.clearCoverage();
  }

  @AfterTest
  @BeforeSuite
  @AfterSuite
  public void empty() {

  }

  @Guard("hello")
  public boolean helloCheck() {
    return !tags.isCovered(TAG_HELLO) && !tags.isCovered(TAG_WORLD) && !tags.isCovered(TAG_EPIX);
  }

  @TestStep("hello")
  public void transition1() {
    tags.covered(TAG_HELLO);
    out.print(":hello");
  }

  @Guard("world")
  public boolean worldCheck() {
    return tags.isCovered(TAG_HELLO) && !tags.isCovered(TAG_WORLD) && !tags.isCovered(TAG_EPIX);
  }

  @TestStep("world")
  public void epix() {
    tags.covered(TAG_WORLD);
    out.print(":world");
  }

  @Guard("epixx")
  public boolean kitted() {
    return tags.isCovered(TAG_WORLD);
  }

  @Pre("epixx")
  public void epixxPre(Map<String, Object> p) {
    out.print(":epixx_pre");
    assertEquals("First pre-method should have no parameters.", 0, p.size());
    p.put("test", "hello post");
  }

  @TestStep("epixx")
  public void epixx() {
    tags.covered(TAG_EPIX);
    out.print(":epixx");
  }

  @Post("epixx")
  public void epixxO(Map<String, Object> p) {
    out.print(":epixx_oracle");
    assertEquals("Post should maintain pre-parameters.", 1, p.size());
    assertEquals("Content of parameter should remain from pre- to post.", "hello post", p.get("test"));
  }

  @Post
  public void stateCheck() {
    out.print(":gen_oracle");
  }
}
