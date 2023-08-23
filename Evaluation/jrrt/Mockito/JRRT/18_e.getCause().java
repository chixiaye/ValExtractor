package org.mockito.internal.configuration.injection;
import org.mockito.exceptions.Reporter;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.internal.util.reflection.FieldInitializationReport;
import org.mockito.internal.util.reflection.FieldInitializer;
import org.mockito.internal.util.reflection.FieldInitializer.ConstructorArgumentResolver;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ConstructorInjection extends MockInjectionStrategy  {
  private ConstructorArgumentResolver argResolver;
  public ConstructorInjection() {
    super();
  }
  ConstructorInjection(ConstructorArgumentResolver argResolver) {
    super();
    this.argResolver = argResolver;
  }
  public boolean processInjection(Field field, Object fieldOwner, Set<Object> mockCandidates) {
    try {
      SimpleArgumentResolver simpleArgumentResolver = new SimpleArgumentResolver(mockCandidates);
      FieldInitializationReport report = new FieldInitializer(fieldOwner, field, simpleArgumentResolver).initialize();
      return report.fieldWasInitializedUsingContructorArgs();
    }
    catch (MockitoException e) {
      Throwable var_18 = e.getCause();
      if(var_18 instanceof InvocationTargetException) {
        Throwable realCause = e.getCause().getCause();
        new Reporter().fieldInitialisationThrewException(field, realCause);
      }
      return false;
    }
  }
  
  static class SimpleArgumentResolver implements ConstructorArgumentResolver  {
    final Set<Object> objects;
    public SimpleArgumentResolver(Set<Object> objects) {
      super();
      this.objects = objects;
    }
    private Object objectThatIsAssignableFrom(Class<?> argType) {
      for (Object object : objects) {
        if(argType.isAssignableFrom(object.getClass())) 
          return object;
      }
      return null;
    }
    public Object[] resolveTypeInstances(Class<?> ... argTypes) {
      List<Object> argumentInstances = new ArrayList<Object>(argTypes.length);
      for (Class<?> argType : argTypes) {
        argumentInstances.add(objectThatIsAssignableFrom(argType));
      }
      return argumentInstances.toArray();
    }
  }
}