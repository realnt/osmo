package osmo.tester.unittests.reporting.jenkins;

import org.junit.Before;
import org.junit.Test;
import osmo.tester.reporting.jenkins.JenkinsStep;
import osmo.tester.reporting.jenkins.JenkinsTest;

import static junit.framework.Assert.*;

/** @author Teemu Kanstren */
public class StepTests {
  private JenkinsStep step;

  @Before
  public void setUp() throws Exception {
    JenkinsTest test = new JenkinsTest(true);
    step = new JenkinsStep(StepTests.class.getName(), test, "duration");
  }

  @Test
  public void durationSeconds() {
    step.setStartTime(1000);
    step.setEndTime(5000);
    assertEquals("Duration formatting for 4000 ms", "4.00", step.getDuration());
  }

  @Test
  public void durationSecondFractions() {
    step.setStartTime(1000);
    step.setEndTime(5111);
    assertEquals("Duration formatting for 4111 ms", "4.11", step.getDuration());
  }

  @Test
  public void classNameFormatting() {
    assertEquals("Classname formatted", "osmo.tester.unittests.reporting.jenkins.StepTests", step.getClassName());
  }

  @Test
  public void errorFormatting() {
    try {
      throw new IllegalArgumentException("Test e");
    } catch (IllegalArgumentException e) {
      step.setError(e);
      String expected = "java.lang.IllegalArgumentException: Test e";
      assertTrue("Error formatted", step.getError().startsWith(expected));
    }
  }
}
