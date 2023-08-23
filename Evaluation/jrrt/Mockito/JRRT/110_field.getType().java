package org.mockito.internal.util.reflection;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.internal.util.MockUtil;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FieldInitializer  {
  final private Object fieldOwner;
  final private Field field;
  final private ConstructorInstantiator instantiator;
  public FieldInitializer(Object fieldOwner, Field field) {
    this(fieldOwner, field, new NoArgConstructorInstantiator(fieldOwner, field));
  }
  public FieldInitializer(Object fieldOwner, Field field, ConstructorArgumentResolver argResolver) {
    this(fieldOwner, field, new ParameterizedConstructorInstantiator(fieldOwner, field, argResolver));
  }
  private FieldInitializer(Object fieldOwner, Field field, ConstructorInstantiator instantiator) {
    super();
    if(new FieldReader(fieldOwner, field).isNull()) {
      checkNotLocal(field);
      checkNotInner(field);
      checkNotInterface(field);
      checkNotAbstract(field);
    }
    this.fieldOwner = fieldOwner;
    this.field = field;
    this.instantiator = instantiator;
  }
  private FieldInitializationReport acquireFieldInstance() throws IllegalAccessException {
    Object fieldInstance = field.get(fieldOwner);
    if(fieldInstance != null) {
      return new FieldInitializationReport(fieldInstance, false, false);
    }
    return instantiator.instantiate();
  }
  public FieldInitializationReport initialize() {
    final AccessibilityChanger changer = new AccessibilityChanger();
    changer.enableAccess(field);
    try {
      return acquireFieldInstance();
    }
    catch (IllegalAccessException e) {
      throw new MockitoException("Problems initializing field \'" + field.getName() + "\' of type \'" + field.getType().getSimpleName() + "\'", e);
    }
    finally {
      changer.safelyDisableAccess(field);
    }
  }
  private void checkNotAbstract(Field field) {
    if(Modifier.isAbstract(field.getType().getModifiers())) {
      throw new MockitoException("the type \'" + field.getType().getSimpleName() + " is an abstract class.");
    }
  }
  private void checkNotInner(Field field) {
    if(field.getType().isMemberClass() && !Modifier.isStatic(field.getType().getModifiers())) {
      throw new MockitoException("the type \'" + field.getType().getSimpleName() + "\' is an inner class.");
    }
  }
  private void checkNotInterface(Field field) {
    Class<?> var_110 = field.getType();
    if(var_110.isInterface()) {
      throw new MockitoException("the type \'" + field.getType().getSimpleName() + "\' is an interface.");
    }
  }
  private void checkNotLocal(Field field) {
    if(field.getType().isLocalClass()) {
      throw new MockitoException("the type \'" + field.getType().getSimpleName() + "\' is a local class.");
    }
  }
  
  public interface ConstructorArgumentResolver  {
    Object[] resolveTypeInstances(Class<?> ... argTypes);
  }
  
  private interface ConstructorInstantiator  {
    FieldInitializationReport instantiate();
  }
  
  static class NoArgConstructorInstantiator implements ConstructorInstantiator  {
    final private Object testClass;
    final private Field field;
    NoArgConstructorInstantiator(Object testClass, Field field) {
      super();
      this.testClass = testClass;
      this.field = field;
    }
    public FieldInitializationReport instantiate() {
      final AccessibilityChanger changer = new AccessibilityChanger();
      Constructor<?> constructor = null;
      try {
        constructor = field.getType().getDeclaredConstructor();
        changer.enableAccess(constructor);
        final Object[] noArg = new Object[0];
        Object newFieldInstance = constructor.newInstance(noArg);
        new FieldSetter(testClass, field).set(newFieldInstance);
        return new FieldInitializationReport(field.get(testClass), true, false);
      }
      catch (NoSuchMethodException e) {
        throw new MockitoException("the type \'" + field.getType().getSimpleName() + "\' has no default constructor", e);
      }
      catch (InvocationTargetException e) {
        throw new MockitoException("the default constructor of type \'" + field.getType().getSimpleName() + "\' has raised an exception (see the stack trace for cause): " + e.getTargetException().toString(), e);
      }
      catch (InstantiationException e) {
        throw new MockitoException("InstantiationException (see the stack trace for cause): " + e.toString(), e);
      }
      catch (IllegalAccessException e) {
        throw new MockitoException("IllegalAccessException (see the stack trace for cause): " + e.toString(), e);
      }
      finally {
        if(constructor != null) {
          changer.safelyDisableAccess(constructor);
        }
      }
    }
  }
  
  static class ParameterizedConstructorInstantiator implements ConstructorInstantiator  {
    final private Object testClass;
    final private Field field;
    final private ConstructorArgumentResolver argResolver;
    final private MockUtil mockUtil = new MockUtil();
    final private Comparator<Constructor<?>> byParameterNumber = new Comparator<Constructor<?>>() {
        public int compare(Constructor<?> constructorA, Constructor<?> constructorB) {
          int argLengths = constructorB.getParameterTypes().length - constructorA.getParameterTypes().length;
          if(argLengths == 0) {
            int constructorAMockableParamsSize = countMockableParams(constructorA);
            int constructorBMockableParamsSize = countMockableParams(constructorB);
            return constructorBMockableParamsSize - constructorAMockableParamsSize;
          }
          return argLengths;
        }
        private int countMockableParams(Constructor<?> constructor) {
          int constructorMockableParamsSize = 0;
          for (Class<?> aClass : constructor.getParameterTypes()) {
            if(mockUtil.isTypeMockable(aClass)) {
              constructorMockableParamsSize++;
            }
          }
          return constructorMockableParamsSize;
        }
    };
    ParameterizedConstructorInstantiator(Object testClass, Field field, ConstructorArgumentResolver argumentResolver) {
      super();
      this.testClass = testClass;
      this.field = field;
      this.argResolver = argumentResolver;
    }
    private Constructor<?> biggestConstructor(Class<?> clazz) {
      final List<Constructor<?>> constructors = Arrays.asList(clazz.getDeclaredConstructors());
      Collections.sort(constructors, byParameterNumber);
      Constructor<?> constructor = constructors.get(0);
      checkParameterized(constructor, field);
      return constructor;
    }
    public FieldInitializationReport instantiate() {
      final AccessibilityChanger changer = new AccessibilityChanger();
      Constructor<?> constructor = null;
      try {
        constructor = biggestConstructor(field.getType());
        changer.enableAccess(constructor);
        final Object[] args = argResolver.resolveTypeInstances(constructor.getParameterTypes());
        Object newFieldInstance = constructor.newInstance(args);
        new FieldSetter(testClass, field).set(newFieldInstance);
        return new FieldInitializationReport(field.get(testClass), false, true);
      }
      catch (IllegalArgumentException e) {
        throw new MockitoException("internal error : argResolver provided incorrect types for constructor " + constructor + " of type " + field.getType().getSimpleName(), e);
      }
      catch (InvocationTargetException e) {
        throw new MockitoException("the constructor of type \'" + field.getType().getSimpleName() + "\' has raised an exception (see the stack trace for cause): " + e.getTargetException().toString(), e);
      }
      catch (InstantiationException e) {
        throw new MockitoException("InstantiationException (see the stack trace for cause): " + e.toString(), e);
      }
      catch (IllegalAccessException e) {
        throw new MockitoException("IllegalAccessException (see the stack trace for cause): " + e.toString(), e);
      }
      finally {
        if(constructor != null) {
          changer.safelyDisableAccess(constructor);
        }
      }
    }
    private void checkParameterized(Constructor<?> constructor, Field field) {
      if(constructor.getParameterTypes().length == 0) {
        throw new MockitoException("the field " + field.getName() + " of type " + field.getType() + " has no parameterized constructor");
      }
    }
  }
}