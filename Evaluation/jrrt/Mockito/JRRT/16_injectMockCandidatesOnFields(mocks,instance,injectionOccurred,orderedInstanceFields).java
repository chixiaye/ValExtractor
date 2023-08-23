package org.mockito.internal.configuration.injection;
import org.mockito.exceptions.Reporter;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.internal.configuration.injection.filter.FinalMockCandidateFilter;
import org.mockito.internal.configuration.injection.filter.MockCandidateFilter;
import org.mockito.internal.configuration.injection.filter.NameBasedCandidateFilter;
import org.mockito.internal.configuration.injection.filter.TypeBasedCandidateFilter;
import org.mockito.internal.util.collections.ListUtil;
import org.mockito.internal.util.reflection.FieldInitializationReport;
import org.mockito.internal.util.reflection.FieldInitializer;
import org.mockito.internal.util.reflection.SuperTypesLastSorter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;
import static org.mockito.internal.util.collections.Sets.newMockSafeHashSet;

public class PropertyAndSetterInjection extends MockInjectionStrategy  {
  final private MockCandidateFilter mockCandidateFilter = new TypeBasedCandidateFilter(new NameBasedCandidateFilter(new FinalMockCandidateFilter()));
  final private ListUtil.Filter<Field> notFinalOrStatic = new ListUtil.Filter<Field>() {
      public boolean isOut(Field object) {
        return Modifier.isFinal(object.getModifiers()) || Modifier.isStatic(object.getModifiers());
      }
  };
  private FieldInitializationReport initializeInjectMocksField(Field field, Object fieldOwner) {
    FieldInitializationReport report = null;
    try {
      report = new FieldInitializer(fieldOwner, field).initialize();
    }
    catch (MockitoException e) {
      if(e.getCause() instanceof InvocationTargetException) {
        Throwable realCause = e.getCause().getCause();
        new Reporter().fieldInitialisationThrewException(field, realCause);
      }
      new Reporter().cannotInitializeForInjectMocksAnnotation(field.getName(), e);
    }
    return report;
  }
  private List<Field> orderedInstanceFieldsFrom(Class<?> awaitingInjectionClazz) {
    List<Field> declaredFields = Arrays.asList(awaitingInjectionClazz.getDeclaredFields());
    declaredFields = ListUtil.filter(declaredFields, notFinalOrStatic);
    return new SuperTypesLastSorter().sort(declaredFields);
  }
  private boolean injectMockCandidates(Class<?> awaitingInjectionClazz, Set<Object> mocks, Object instance) {
    boolean injectionOccurred = false;
    List<Field> orderedInstanceFields = orderedInstanceFieldsFrom(awaitingInjectionClazz);
    boolean var_16 = injectMockCandidatesOnFields(mocks, instance, injectionOccurred, orderedInstanceFields);
    injectionOccurred |= var_16;
    injectionOccurred |= injectMockCandidatesOnFields(mocks, instance, injectionOccurred, orderedInstanceFields);
    return injectionOccurred;
  }
  private boolean injectMockCandidatesOnFields(Set<Object> mocks, Object instance, boolean injectionOccurred, List<Field> orderedInstanceFields) {
    for(java.util.Iterator<java.lang.reflect.Field> it = orderedInstanceFields.iterator(); it.hasNext(); ) {
      Field field = it.next();
      Object injected = mockCandidateFilter.filterCandidate(mocks, field, instance).thenInject();
      if(injected != null) {
        injectionOccurred |= true;
        mocks.remove(injected);
        it.remove();
      }
    }
    return injectionOccurred;
  }
  public boolean processInjection(Field injectMocksField, Object injectMocksFieldOwner, Set<Object> mockCandidates) {
    FieldInitializationReport report = initializeInjectMocksField(injectMocksField, injectMocksFieldOwner);
    boolean injectionOccurred = false;
    Class<?> fieldClass = report.fieldClass();
    Object fieldInstanceNeedingInjection = report.fieldInstance();
    while(fieldClass != Object.class){
      injectionOccurred |= injectMockCandidates(fieldClass, newMockSafeHashSet(mockCandidates), fieldInstanceNeedingInjection);
      fieldClass = fieldClass.getSuperclass();
    }
    return injectionOccurred;
  }
}