package org.mockito;
import org.mockito.configuration.AnnotationEngine;
import org.mockito.configuration.DefaultMockitoConfiguration;
import org.mockito.exceptions.Reporter;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.internal.configuration.GlobalConfiguration;
import org.mockito.internal.util.reflection.FieldSetter;
import org.mockito.runners.MockitoJUnitRunner;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import static java.lang.annotation.ElementType.FIELD;

public class MockitoAnnotations  {
  public static void initMocks(Object testClass) {
    if(testClass == null) {
      throw new MockitoException("testClass cannot be null. For info how to use @Mock annotations see examples in javadoc for MockitoAnnotations class");
    }
    AnnotationEngine annotationEngine = new GlobalConfiguration().getAnnotationEngine();
    Class<? extends Object> var_1 = testClass.getClass();
    Class<?> clazz = var_1;
    if(annotationEngine.getClass() != new DefaultMockitoConfiguration().getAnnotationEngine().getClass()) {
      while(clazz != Object.class){
        scanDeprecatedWay(annotationEngine, testClass, clazz);
        clazz = clazz.getSuperclass();
      }
    }
    annotationEngine.process(testClass.getClass(), testClass);
  }
  @SuppressWarnings(value = {"deprecation", }) static void processAnnotationDeprecatedWay(AnnotationEngine annotationEngine, Object testClass, Field field) {
    boolean alreadyAssigned = false;
    for (Annotation annotation : field.getAnnotations()) {
      Object mock = annotationEngine.createMockFor(annotation, field);
      if(mock != null) {
        throwIfAlreadyAssigned(field, alreadyAssigned);
        alreadyAssigned = true;
        try {
          new FieldSetter(testClass, field).set(mock);
        }
        catch (Exception e) {
          throw new MockitoException("Problems setting field " + field.getName() + " annotated with " + annotation, e);
        }
      }
    }
  }
  static void scanDeprecatedWay(AnnotationEngine annotationEngine, Object testClass, Class<?> clazz) {
    Field[] fields = clazz.getDeclaredFields();
    for (Field field : fields) {
      processAnnotationDeprecatedWay(annotationEngine, testClass, field);
    }
  }
  static void throwIfAlreadyAssigned(Field field, boolean alreadyAssigned) {
    if(alreadyAssigned) {
      new Reporter().moreThanOneAnnotationNotAllowed(field.getName());
    }
  }
  @Target(value = {FIELD, }) @Retention(value = RetentionPolicy.RUNTIME) @Deprecated() public @interface Mock {
  }
}