package org.mockito.internal.configuration;
import org.mockito.*;
import org.mockito.configuration.AnnotationEngine;
import org.mockito.exceptions.Reporter;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.internal.util.MockUtil;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import static org.mockito.Mockito.withSettings;

@SuppressWarnings(value = {"unchecked", }) public class SpyAnnotationEngine implements AnnotationEngine  {
  public Object createMockFor(Annotation annotation, Field field) {
    return null;
  }
  private static Object newSpyInstance(Object testInstance, Field field) throws InstantiationException, IllegalAccessException, InvocationTargetException {
    MockSettings settings = withSettings().defaultAnswer(Mockito.CALLS_REAL_METHODS).name(field.getName());
    Class<?> type = field.getType();
    if(type.isInterface()) {
      MockSettings var_10 = settings.useConstructor();
      return Mockito.mock(type, var_10);
    }
    if(!Modifier.isStatic(type.getModifiers())) {
      Class<?> enclosing = type.getEnclosingClass();
      if(enclosing != null) {
        if(!enclosing.isInstance(testInstance)) {
          throw new MockitoException("@Spy annotation can only initialize inner classes declared in the test. " + "Inner class: \'" + type.getSimpleName() + "\', " + "outer class: \'" + enclosing.getSimpleName() + "\'.");
        }
        return Mockito.mock(type, settings.useConstructor().outerInstance(testInstance));
      }
    }
    Constructor<?> constructor;
    try {
      constructor = type.getDeclaredConstructor();
    }
    catch (NoSuchMethodException e) {
      throw new MockitoException("Please ensure that the type \'" + type.getSimpleName() + "\' has 0-arg constructor.");
    }
    if(Modifier.isPrivate(constructor.getModifiers())) {
      constructor.setAccessible(true);
      return Mockito.mock(type, settings.spiedInstance(constructor.newInstance()));
    }
    else {
      return Mockito.mock(type, settings.useConstructor());
    }
  }
  void assertNoIncompatibleAnnotations(Class annotation, Field field, Class ... undesiredAnnotations) {
    for (Class u : undesiredAnnotations) {
      if(field.isAnnotationPresent(u)) {
        new Reporter().unsupportedCombinationOfAnnotations(annotation.getSimpleName(), annotation.getClass().getSimpleName());
      }
    }
  }
  private static void assertNotInterface(Object testInstance, Class<?> type) {
    type = testInstance != null ? testInstance.getClass() : type;
    if(type.isInterface()) {
      throw new MockitoException("Type \'" + type.getSimpleName() + "\' is an interface and it cannot be spied on.");
    }
  }
  @SuppressWarnings(value = {"deprecation", }) public void process(Class<?> context, Object testInstance) {
    Field[] fields = context.getDeclaredFields();
    for (Field field : fields) {
      if(field.isAnnotationPresent(Spy.class) && !field.isAnnotationPresent(InjectMocks.class)) {
        assertNoIncompatibleAnnotations(Spy.class, field, Mock.class, org.mockito.MockitoAnnotations.Mock.class, Captor.class);
        field.setAccessible(true);
        Object instance;
        try {
          instance = field.get(testInstance);
          assertNotInterface(instance, field.getType());
          if(new MockUtil().isMock(instance)) {
            Mockito.reset(instance);
          }
          else 
            if(instance != null) {
              field.set(testInstance, Mockito.mock(instance.getClass(), withSettings().spiedInstance(instance).defaultAnswer(Mockito.CALLS_REAL_METHODS).name(field.getName())));
            }
            else {
              field.set(testInstance, newSpyInstance(testInstance, field));
            }
        }
        catch (Exception e) {
          throw new MockitoException("Unable to initialize @Spy annotated field \'" + field.getName() + "\'.\n" + e.getMessage(), e);
        }
      }
    }
  }
}