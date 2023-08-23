package org.mockito.internal.configuration;
import org.mockito.*;
import org.mockito.configuration.AnnotationEngine;
import org.mockito.internal.configuration.injection.scanner.InjectMocksScanner;
import org.mockito.internal.configuration.injection.scanner.MockScanner;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import static org.mockito.internal.util.collections.Sets.newMockSafeHashSet;

@SuppressWarnings(value = {"deprecation", "unchecked", }) public class InjectingAnnotationEngine implements AnnotationEngine  {
  final private AnnotationEngine delegate = new DefaultAnnotationEngine();
  final private AnnotationEngine spyAnnotationEngine = new SpyAnnotationEngine();
  @Deprecated() public Object createMockFor(Annotation annotation, Field field) {
    return delegate.createMockFor(annotation, field);
  }
  public void injectMocks(final Object testClassInstance) {
    Class<?> clazz = testClassInstance.getClass();
    Set<Field> mockDependentFields = new HashSet<Field>();
    Set<Object> mocks = newMockSafeHashSet();
    while(clazz != Object.class){
      new InjectMocksScanner(clazz).addTo(mockDependentFields);
      new MockScanner(testClassInstance, clazz).addPreparedMocks(mocks);
      clazz = clazz.getSuperclass();
    }
    new DefaultInjectionEngine().injectMocksOnFields(mockDependentFields, mocks, testClassInstance);
  }
  public void process(Class<?> clazz, Object testInstance) {
    Class<? extends Object> var_13 = testInstance.getClass();
    processIndependentAnnotations(var_13, testInstance);
    processInjectMocks(testInstance.getClass(), testInstance);
  }
  private void processIndependentAnnotations(final Class<?> clazz, final Object testInstance) {
    Class<?> classContext = clazz;
    while(classContext != Object.class){
      delegate.process(classContext, testInstance);
      spyAnnotationEngine.process(classContext, testInstance);
      classContext = classContext.getSuperclass();
    }
  }
  private void processInjectMocks(final Class<?> clazz, final Object testInstance) {
    Class<?> classContext = clazz;
    while(classContext != Object.class){
      injectMocks(testInstance);
      classContext = classContext.getSuperclass();
    }
  }
}