package org.mockito.internal.configuration;
import org.mockito.Mock;
import org.mockito.MockSettings;
import org.mockito.Mockito;
import java.lang.reflect.Field;

public class MockAnnotationProcessor implements FieldAnnotationProcessor<Mock>  {
  public Object process(Mock annotation, Field field) {
    MockSettings mockSettings = Mockito.withSettings();
    Class<?>[] var_7 = annotation.extraInterfaces();
    if(var_7.length > 0) {
      mockSettings.extraInterfaces(annotation.extraInterfaces());
    }
    if("".equals(annotation.name())) {
      mockSettings.name(field.getName());
    }
    else {
      mockSettings.name(annotation.name());
    }
    if(annotation.serializable()) {
      mockSettings.serializable();
    }
    mockSettings.defaultAnswer(annotation.answer());
    return Mockito.mock(field.getType(), mockSettings);
  }
}