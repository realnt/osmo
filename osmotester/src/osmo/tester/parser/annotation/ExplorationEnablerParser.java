package osmo.tester.parser.annotation;

import osmo.common.Logger;
import osmo.tester.annotation.ExplorationEnabler;
import osmo.tester.model.InvocationTarget;
import osmo.tester.parser.AnnotationParser;
import osmo.tester.parser.ParserParameters;
import osmo.tester.parser.ParserResult;

import java.lang.reflect.Method;

/**
 * Parses {@link osmo.tester.annotation.ExplorationEnabler} annotations from the given model object.
 *
 * @author Teemu Kanstren
 */
public class ExplorationEnablerParser implements AnnotationParser {
  private static final Logger log = new Logger(ExplorationEnablerParser.class);

  @Override
  public void parse(ParserResult result, ParserParameters parameters, StringBuilder errors) {
    ExplorationEnabler enabler = (ExplorationEnabler) parameters.getAnnotation();
    Method method = parameters.getMethod();
    Class<?> returnType = method.getReturnType();
    String name = "@" + ExplorationEnabler.class.getSimpleName();
    if (returnType != void.class && returnType != Void.class) {
      errors.append("Invalid return type for " + name + " (\"" + method.getName() + "()\"):" + returnType + ".\n");
    }
    Class<?>[] parameterTypes = method.getParameterTypes();
    if (parameterTypes.length > 0) {
      errors.append(name + " methods are not allowed to have parameters: \"" + method.getName() + "()\" has " +
              parameterTypes.length + " parameters.\n");
    }
    result.getFsm().addExplorationEnabler(new InvocationTarget(parameters, ExplorationEnabler.class));
  }
}
