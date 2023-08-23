package org.mockito.internal.configuration;
import org.mockito.Mock;
import org.mockito.MockSettings;
import org.mockito.Mockito;
import java.lang.reflect.Field;

public class MockAnnotationProcessor implements FieldAnnotationProcessor<Mock>  {
  public Object process(Mock annotation, Field field) {
    MockSettings mockSettings = Mockito.withSettings();
    if(annotation.extraInterfaces().length > 0) {
      mockSettings.extraInterfaces(annotation.extraInterfaces());
    }
    String var_8 = annotation.name();
    if("".equals(var_8)) {
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