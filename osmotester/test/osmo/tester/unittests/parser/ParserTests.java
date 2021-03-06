package osmo.tester.unittests.parser;

import org.junit.Before;
import org.junit.Test;
import osmo.tester.OSMOConfiguration;
import osmo.tester.annotation.Variable;
import osmo.tester.generator.SingleInstanceModelFactory;
import osmo.tester.generator.testsuite.TestSuite;
import osmo.tester.model.*;
import osmo.tester.parser.MainParser;
import osmo.tester.parser.ParserResult;
import osmo.tester.unittests.testmodels.EmptyTestModel1;
import osmo.tester.unittests.testmodels.EmptyTestModel2;
import osmo.tester.unittests.testmodels.EmptyTestModel3;
import osmo.tester.unittests.testmodels.EmptyTestModel4;
import osmo.tester.unittests.testmodels.EmptyTestModel5;
import osmo.tester.unittests.testmodels.EmptyTestModel6;
import osmo.tester.unittests.testmodels.PartialModel1;
import osmo.tester.unittests.testmodels.PartialModel2;
import osmo.tester.unittests.testmodels.StepAndTransitionModel;
import osmo.tester.unittests.testmodels.TestStepModel;
import osmo.tester.unittests.testmodels.ValidTestModel3;
import osmo.tester.unittests.testmodels.VariableModel1;
import osmo.tester.unittests.testmodels.VariableModel2;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.*;

import static junit.framework.Assert.*;

/** @author Teemu Kanstren */
public class ParserTests {
  private MainParser parser = null;
  private OSMOConfiguration config;

  @Before
  public void setup() {
    config = new OSMOConfiguration();
    config.setMethodBasedNaming(true);
    parser = new MainParser(config);
  }

  private OSMOConfiguration conf(Object... modelObjects) {
    SingleInstanceModelFactory factory = new SingleInstanceModelFactory();
    config.setFactory(factory);
    for (Object mo : modelObjects) {
      factory.add(mo);
    }
    return config;
  }

  @Test
  public void testModel1WithMethodNaming() throws Exception {
    EmptyTestModel1 model = new EmptyTestModel1();
    ParserResult result = parser.parse(1, conf(model), new TestSuite());
    FSM fsm = result.getFsm();
    assertEquals("Number of @Before methods", 2, fsm.getBeforeTests().size());
    assertEquals("Number of @BeforeSuite methods", 1, fsm.getBeforeSuites().size());
    assertEquals("Number of @After methods", 1, fsm.getAfterTests().size());
    assertEquals("Number of @AfterSuite methods", 1, fsm.getAfterSuites().size());
    //these also test for the correct number of guards
    assertTransitionPresent(fsm, "hello", 1, 2);
    assertTransitionPresent(fsm, "world", 3, 1);
    assertTransitionPresent(fsm, "epixx", 3, 3);
    assertEquals("Number of end conditions", 2, fsm.getEndConditions().size());
    assertEquals("Number of generation enablers", 1, fsm.getGenerationEnablers().size());
    assertEquals("Number of exploration enablers", 1, fsm.getExplorationEnablers().size());
    assertNotNull("Should have TestSuite set", model.getHistory());
  }

  @Test
  public void testModel1WithoutMethodNaming() throws Exception {
    config.setMethodBasedNaming(false);
    parser = new MainParser(config);
    EmptyTestModel1 model = new EmptyTestModel1();
    ParserResult result = parser.parse(1, conf(model), new TestSuite());
    FSM fsm = result.getFsm();
    assertEquals("Number of @Before methods", 2, fsm.getBeforeTests().size());
    assertEquals("Number of @BeforeSuite methods", 1, fsm.getBeforeSuites().size());
    assertEquals("Number of @After methods", 1, fsm.getAfterTests().size());
    assertEquals("Number of @AfterSuite methods", 1, fsm.getAfterSuites().size());
    //these also test for the correct number of guards
    assertTransitionPresent(fsm, "hello", 3, 2);
    assertTransitionPresent(fsm, "world", 3, 1);
    assertTransitionPresent(fsm, "epixx", 5, 3);
    assertEquals("Number of end conditions", 2, fsm.getEndConditions().size());
    assertEquals("Number of generation enablers", 1, fsm.getGenerationEnablers().size());
    assertEquals("Number of exploration enablers", 1, fsm.getExplorationEnablers().size());
    assertNotNull("Should have TestSuite set", model.getHistory());
  }

  @Test
  public void testModel2() {
    try {
      ParserResult result = parser.parse(1, conf(new EmptyTestModel2()), new TestSuite());
      fail("Should throw exception");
    } catch (Exception e) {
      String msg = e.getMessage();
      String expected = "Invalid test model:\n" +
              "Only one Requirements object instance allowed in the model.\n" +
              "No test steps found in given model object. Model cannot be processed.\n" +
              "@Guard without matching step:foo.\n";
      assertEquals(expected, msg);
    }
  }

  @Test
  public void testModel3() {
    try {
      ParserResult result = parser.parse(1, conf(new EmptyTestModel3()), new TestSuite());
      fail("Should throw exception");
    } catch (Exception e) {
      String msg = e.getMessage();
      msg = sortErrors(msg);
      String expected = "Invalid test model:\n" +
              "@AfterSuite methods are not allowed to have parameters: \"badAS()\" has 1 parameters.\n" +
              "@AfterTest methods are not allowed to have parameters: \"badAT()\" has 1 parameters.\n" +
              "@BeforeSuite methods are not allowed to have parameters: \"badBS()\" has 1 parameters.\n" +
              "@BeforeTest methods are not allowed to have parameters: \"badBT()\" has 1 parameters.\n" +
              "@CoverageValue methods must have 1 parameter (class osmo.tester.generator.testsuite.TestCaseStep): \"badArgument()\" has 2 parameters.\n" +
              "@CoverageValue parameter must be of type class osmo.tester.generator.testsuite.TestCaseStep: \"badArgument()\" has type class java.lang.String\n" +
              "@ExplorationEnabler methods are not allowed to have parameters: \"enableExploration()\" has 1 parameters.\n" +
              "@GenerationEnabler methods are not allowed to have parameters: \"enableGeneration()\" has 1 parameters.\n" +
              "@Post methods are not allowed to have any parameters: \"wrong()\" has 1.\n" +
              "Invalid return type for @CoverageValue in (\"badArgument()\"):void. Should be String.\n" +
              "Invalid return type for @EndCondition (\"end()\"):void. Should be boolean.\n" +
              "Invalid return type for @ExplorationEnabler (\"enableExploration()\"):class java.lang.String.\n" +
              "Invalid return type for @ExplorationEnabler (\"enableExploration()\"):int.\n" +
              "Invalid return type for @GenerationEnabler (\"enableGeneration()\"):int.\n" +
              "Invalid return type for guard (\"hello()\"):class java.lang.String.\n" +
              "Test step name must be unique. 'foo' given several times.\n"+
              "";
      assertEquals(expected, msg);
    }
  }

  /**
   * This is used to sort the e messages in a predictable way for test oracle assertions.
   * It seems the JDK7 does not iterate reflected methods predictably so this is needed as a workaround.
   *
   * @param errors The set of errors to be sorted, with lines separated by "\n"
   * @return The given e lines sorted by Collections.sort, exlucing the header which remains on top.
   */
  private String sortErrors(String errors) {
    String[] split = errors.split("\n");
    List<String> sorted = new ArrayList<>();
    Collections.addAll(sorted, split);
    String header = sorted.remove(0);
    Collections.sort(sorted);
    StringBuilder bob = new StringBuilder();
    bob.append(header).append("\n");
    for (String line : sorted) {
      bob.append(line).append("\n");
    }
    return bob.toString();
  }

  @Test
  public void testModel4() {
    try {
      ParserResult result = parser.parse(1, conf(new EmptyTestModel4()), new TestSuite());
      fail("Should throw exception");
    } catch (Exception e) {
      //note that this exception checking will swallow real errors so it can be useful to print them..
//      e.printStackTrace();
      String msg = e.getMessage();
      msg = sortErrors(msg);
      String expected = "Invalid test model:\n" +
              "@CoverageValue methods must have 1 parameter (class osmo.tester.generator.testsuite.TestCaseStep): \"noArgument()\" has 0 parameters.\n" +
              "@EndCondition methods are not allowed to have parameters: \"ending()\" has 1 parameters.\n" +
              "@LastStep methods are not allowed to have parameters: \"last()\" has 1 parameters.\n"+
              "Guard methods are not allowed to have parameters: \"hello()\" has 1 parameters.\n"+
              "Requirements object was null, which is not allowed.\n" +
              "";
      assertEquals(expected, msg);
    }
  }

  @Test
  public void testModel5() {
    try {
      ParserResult result = parser.parse(1, conf(new EmptyTestModel5()), new TestSuite());
      fail("Should throw exception");
    } catch (Exception e) {
      String msg = e.getMessage();
      String expected = "Invalid test model:\n" +
              "@TestStep methods are not allowed to have parameters: \"epixx()\" has 1 parameters.\n" +
              "Invalid return type for @EndCondition (\"hello()\"):class java.lang.String. Should be boolean.\n" +
              "@EndCondition methods are not allowed to have parameters: \"hello()\" has 1 parameters.\n";
      assertEquals(expected, msg);
    }
  }

  @Test
  public void testModel6() {
    try {
      ParserResult result = parser.parse(1, conf(new EmptyTestModel6()), new TestSuite());
      fail("Should throw exception");
    } catch (Exception e) {
      String msg = e.getMessage();
      String expected = "Invalid test model:\n" +
              "@TestStep methods are not allowed to have parameters: \"epix()\" has 1 parameters.\n" +
              "Invalid return type for guard (\"listCheck()\"):class java.lang.String.\n" +
              "@TestStep methods are not allowed to have parameters: \"transition1()\" has 1 parameters.\n" +
              "@Guard without matching step:world.\n";
      assertEquals(expected, msg);
    }
  }
  
  @Test
  public void testPartialModelsWithMethodNaming() {
    Requirements req = new Requirements();
    PartialModel1 model1 = new PartialModel1(req, null);
    PartialModel2 model2 = new PartialModel2(req, null);
    ParserResult result = parser.parse(1, conf(model1, model2), new TestSuite());
    FSM fsm = result.getFsm();
    assertEquals("Number of @Before methods", 2, fsm.getBeforeTests().size());
    assertEquals("Number of @BeforeSuite methods", 2, fsm.getBeforeSuites().size());
    assertEquals("Number of @After methods", 2, fsm.getAfterTests().size());
    assertEquals("Number of @AfterSuite methods", 1, fsm.getAfterSuites().size());
    assertEquals("Number of @ExplorationEnabler methods", 2, fsm.getExplorationEnablers().size());
    assertEquals("Number of @GenerationEnabler methods", 1, fsm.getGenerationEnablers().size());
    assertNotNull("@StateDescription method", fsm.getCoverageMethods());
    //these also test for the correct number of guards
    assertTransitionPresent(fsm, "Hello", 1, 3);
    assertTransitionPresent(fsm, "world", 3, 3);
    assertTransitionPresent(fsm, "epixx", 2, 3);
    assertEquals("Number of end conditions", 2, fsm.getEndConditions().size());
    assertNotNull("Should have TestSuite set", model1.getHistory());
  }

  @Test
  public void testPartialModelsWithoutMethodNaming() {
    config.setMethodBasedNaming(false);
    parser = new MainParser(config);
    Requirements req = new Requirements();
    PartialModel1 model1 = new PartialModel1(req, null);
    PartialModel2 model2 = new PartialModel2(req, null);
    ParserResult result = parser.parse(1, conf(model1, model2), new TestSuite());
    FSM fsm = result.getFsm();
    assertEquals("Number of @BeforeTest methods", 2, fsm.getBeforeTests().size());
    assertEquals("Number of @BeforeSuite methods", 2, fsm.getBeforeSuites().size());
    assertEquals("Number of @AfterTest methods", 2, fsm.getAfterTests().size());
    assertEquals("Number of @AfterSuite methods", 1, fsm.getAfterSuites().size());
    assertEquals("Number of @ExplorationEnabler methods", 2, fsm.getExplorationEnablers().size());
    assertEquals("Number of @GenerationEnabler methods", 1, fsm.getGenerationEnablers().size());
    assertNotNull("@StateDescription method", fsm.getCoverageMethods());
    //these also test for the correct number of guards
    assertTransitionPresent(fsm, "Hello", 1, 3);
    assertTransitionPresent(fsm, "world", 4, 3);
    assertTransitionPresent(fsm, "epixx", 3, 3);
    assertEquals("Number of end conditions", 2, fsm.getEndConditions().size());
    assertNotNull("Should have TestSuite set", model1.getHistory());
  }

  @Test
  public void noMethods() {
    try {
      ParserResult result = parser.parse(1, conf(new Object()), new TestSuite());
      FSM fsm = result.getFsm();
      fsm.checkFSM(new StringBuilder());
      fail("Should throw exception when no transition methods are available.");
    } catch (Exception e) {
      String msg = e.getMessage();
      String expected = "Invalid test model:\n" +
              "No test steps found in given model object. Model cannot be processed.\n";
      assertEquals(expected, msg);
    }
  }

  private void assertTransitionPresent(FSM fsm, String name, int guardCount, int oracleCount) {
    FSMTransition transition = fsm.getTransition(name);
    assertNotNull("Transition '" + name + "' should be generated.", transition);
    assertNotNull("Transition '" + name + "' should have valid transition content.", transition.getTransition());
    assertEquals("Transition '" + name + "' should have " + guardCount + " guards.", guardCount, transition.getGuards().size());
    assertEquals("Transition '" + name + "' should have " + oracleCount + " post methods.", oracleCount, transition.getPostMethods().size());
  }

  @Test
  public void variableParsing() {
    VariableModel1 model = new VariableModel1();
    ParserResult result = parser.parse(1, conf(model), new TestSuite());
    FSM fsm = result.getFsm();
    Collection<VariableField> variables = fsm.getModelVariables();
    assertEquals("All @" + Variable.class.getSimpleName() + " items should be parsed.", 10, variables.size());
    assertVariablePresent(variables, "i1");
    assertVariablePresent(variables, "f1");
    assertVariablePresent(variables, "d1");
    assertVariablePresent(variables, "b1");
    assertVariablePresent(variables, "i2");
    assertVariablePresent(variables, "f2");
    assertVariablePresent(variables, "d2");
    assertVariablePresent(variables, "b2");
    assertVariablePresent(variables, "testVariable");
    assertVariablePresent(variables, "stringVariable");
  }

  private void assertVariablePresent(Collection<VariableField> variables, String name) {
    for (VariableField variable : variables) {
      if (variable.getName().equals(name)) {
        return;
      }
    }
    fail("Variable " + name + " should be present in the model.");
  }

  private void assertVariableNotPresent(Collection<VariableField> variables, String name) {
    for (VariableField variable : variables) {
      if (variable.getName().equals(name)) {
        fail("Variable " + name + " should not be present in the model.");
      }
    }
  }

  @Test
  public void searchableInputParsing() {
    VariableModel2 model = new VariableModel2();
    OSMOConfiguration config = conf(model);
    ParserResult result = parser.parse(1, config, new TestSuite());
    FSM fsm = result.getFsm();
    Collection<VariableField> variables = fsm.getModelVariables();
    assertEquals("Number of inputs", 6, variables.size());
    assertVariablePresent(variables, "range");
    assertVariablePresent(variables, "named-set");
    assertVariablePresent(variables, "Hello There");
    assertVariableNotPresent(variables, "set");
    assertVariableNotPresent(variables, "toStringVariable");
  }

  @Test
  public void prefixOnlyParsing() {
    ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
    PrintStream ps = new PrintStream(out);
    ValidTestModel3 model = new ValidTestModel3(ps);
    OSMOConfiguration config = new OSMOConfiguration();
    SingleInstanceModelFactory factory = new SingleInstanceModelFactory();
    factory.add("ap_", model);
    factory.add("ip_", model);
    config.setFactory(factory);
    ParserResult result = parser.parse(1, config, new TestSuite());
    FSM fsm = result.getFsm();
    assertTransitionPresent(fsm, "ap_hello", 1, 2);
    assertTransitionPresent(fsm, "ip_hello", 1, 2);
    assertTransitionPresent(fsm, "ap_world", 1, 2);
    assertTransitionPresent(fsm, "ip_world", 1, 2);
    assertTransitionPresent(fsm, "ap_epixx", 1, 3);
    assertTransitionPresent(fsm, "ip_epixx", 1, 3);
    assertEquals("Number of @BeforeTest elements", 2, fsm.getBeforeTests().size());
    assertEquals("Number of @BeforeSuite elements", 2, fsm.getBeforeSuites().size());
    assertEquals("Number of @AfterTest elements", 2, fsm.getAfterTests().size());
    assertEquals("Number of @AfterSuite elements", 2, fsm.getAfterSuites().size());
  }

  @Test
  public void prefixAndNoPrefixParsing() {
    ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
    PrintStream ps = new PrintStream(out);
    ValidTestModel3 model = new ValidTestModel3(ps);
    OSMOConfiguration config = new OSMOConfiguration();
    SingleInstanceModelFactory factory = new SingleInstanceModelFactory();
    factory.add("ap_", model);
    factory.add("ip_", model);
    factory.add(model);
    config.setFactory(factory);
    ParserResult result = parser.parse(1, config, new TestSuite());
    FSM fsm = result.getFsm();
    assertTransitionPresent(fsm, "hello", 1, 3);
    assertTransitionPresent(fsm, "ap_hello", 1, 3);
    assertTransitionPresent(fsm, "ip_hello", 1, 3);
    assertTransitionPresent(fsm, "world", 1, 3);
    assertTransitionPresent(fsm, "ap_world", 1, 3);
    assertTransitionPresent(fsm, "ip_world", 1, 3);
    assertTransitionPresent(fsm, "epixx", 1, 4);
    assertTransitionPresent(fsm, "ap_epixx", 1, 4);
    assertTransitionPresent(fsm, "ip_epixx", 1, 4);
    assertEquals("Number of @BeforeTest elements", 3, fsm.getBeforeTests().size());
    assertEquals("Number of @BeforeSuite elements", 3, fsm.getBeforeSuites().size());
    assertEquals("Number of @AfterTest elements", 3, fsm.getAfterTests().size());
    assertEquals("Number of @AfterSuite elements", 3, fsm.getAfterSuites().size());
  }

  @Test
  public void testStepsOnly() {
    ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
    PrintStream ps = new PrintStream(out);
    TestStepModel model = new TestStepModel(ps);
    OSMOConfiguration config = new OSMOConfiguration();
    SingleInstanceModelFactory factory = new SingleInstanceModelFactory();
    factory.add("ap_", model);
    factory.add("ip_", model);
    factory.add(model);
    config.setFactory(factory);
    ParserResult result = parser.parse(1, config, new TestSuite());
    FSM fsm = result.getFsm();
    assertTransitionPresent(fsm, "hello", 1, 3);
    assertTransitionPresent(fsm, "ap_hello", 1, 3);
    assertTransitionPresent(fsm, "ip_hello", 1, 3);
    assertTransitionPresent(fsm, "world", 1, 3);
    assertTransitionPresent(fsm, "ap_world", 1, 3);
    assertTransitionPresent(fsm, "ip_world", 1, 3);
    assertTransitionPresent(fsm, "epixx", 1, 4);
    assertTransitionPresent(fsm, "ap_epixx", 1, 4);
    assertTransitionPresent(fsm, "ip_epixx", 1, 4);
    assertEquals("Number of @BeforeTest elements", 3, fsm.getBeforeTests().size());
    assertEquals("Number of @BeforeSuite elements", 3, fsm.getBeforeSuites().size());
    assertEquals("Number of @AfterTest elements", 3, fsm.getAfterTests().size());
    assertEquals("Number of @AfterSuite elements", 3, fsm.getAfterSuites().size());
  }

  @Test
  public void hybridWithStepsAndTransitions() {
    ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
    PrintStream ps = new PrintStream(out);
    StepAndTransitionModel model = new StepAndTransitionModel(ps);
    OSMOConfiguration config = new OSMOConfiguration();
    SingleInstanceModelFactory factory = new SingleInstanceModelFactory();
    factory.add("ap_", model);
    factory.add("ip_", model);
    factory.add(model);
    config.setFactory(factory);
    ParserResult result = parser.parse(1, config, new TestSuite());
    FSM fsm = result.getFsm();
    assertTransitionPresent(fsm, "hello", 1, 3);
    assertTransitionPresent(fsm, "ap_hello", 1, 3);
    assertTransitionPresent(fsm, "ip_hello", 1, 3);
    assertTransitionPresent(fsm, "world", 1, 3);
    assertTransitionPresent(fsm, "ap_world", 1, 3);
    assertTransitionPresent(fsm, "ip_world", 1, 3);
    assertTransitionPresent(fsm, "epixx", 1, 4);
    assertTransitionPresent(fsm, "ap_epixx", 1, 4);
    assertTransitionPresent(fsm, "ip_epixx", 1, 4);
    assertEquals("Number of @BeforeTest elements", 3, fsm.getBeforeTests().size());
    assertEquals("Number of @BeforeSuite elements", 3, fsm.getBeforeSuites().size());
    assertEquals("Number of @AfterTest elements", 3, fsm.getAfterTests().size());
    assertEquals("Number of @AfterSuite elements", 3, fsm.getAfterSuites().size());
  }
  
  @Test
  public void descriptions() {
    EmptyTestModel1 model = new EmptyTestModel1();
    ParserResult result = parser.parse(1, conf(model), new TestSuite());
    FSM fsm = result.getFsm();
    Collection<InvocationTarget> afterSuites = fsm.getAfterSuites();
    Collection<InvocationTarget> beforeSuites = fsm.getBeforeSuites();
    Collection<InvocationTarget> beforeTests = fsm.getBeforeTests();
    Collection<InvocationTarget> afterTests = fsm.getAfterTests();
    Collection<FSMTransition> transitions = fsm.getTransitions();
    Collection<CoverageMethod> coverageMethods = fsm.getCoverageMethods();
    Collection<InvocationTarget> endConditions = fsm.getEndConditions();
    Collection<InvocationTarget> explorationEnablers = fsm.getExplorationEnablers();
    Collection<InvocationTarget> generationEnablers = fsm.getGenerationEnablers();
    Collection<InvocationTarget> lastSteps = fsm.getLastSteps();

    assertDescriptions(afterSuites, "After the suite looks like this");
    assertDescriptions(beforeSuites, "Before the suite looks like this");
    assertDescriptions(beforeTests, "Start here", "Before test we do this");
    assertDescriptions(afterTests, "After test we do this");
    assertDescriptions(endConditions, "Extra end condition one", "Extra end condition two");
    assertDescriptions(explorationEnablers, "");
    assertDescriptions(generationEnablers, "");
    assertDescriptions(lastSteps);

    Collection<InvocationTarget> guards = new LinkedHashSet<>();
    Collection<InvocationTarget> posts = new LinkedHashSet<>();
    Collection<InvocationTarget> pres = new LinkedHashSet<>();
    Collection<InvocationTarget> tts = new LinkedHashSet<>();
    for (FSMTransition transition : transitions) {
      guards.addAll(transition.getGuards());
      posts.addAll(transition.getPostMethods());
      pres.addAll(transition.getPreMethods());
      tts.add(transition.getTransition());
    }
    assertDescriptions(guards, "Negative guard looks like this", "World is guarded here", "", "", "");
    assertDescriptions(posts, "", "", "Post one");
    assertDescriptions(pres, "", "Pre one");
    assertDescriptions(tts, "", "", "");

    Collection<InvocationTarget> cvs = new LinkedHashSet<>();
    for (CoverageMethod method : coverageMethods) {
      cvs.add(method.getInvocationTarget());
    }
    assertDescriptions(cvs);
  }
  
  private void assertDescriptions(Collection<InvocationTarget> targets, String... descs) {
    List<String> found = new ArrayList<>();
    for (InvocationTarget target : targets) {
      found.add(target.getDescription());
    }
    List<String> expected = new ArrayList<>();
    expected.addAll(Arrays.asList(descs));
    assertEquals("Number of descriptions to find", expected.size(), found.size());
    for (String name : expected) {
      assertTrue("Target not found:"+name, found.contains(name));
    }
  }
}
