package com.google.javascript.jscomp.parsing;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Set;

public class Config  {
  final boolean parseJsDocDocumentation;
  final boolean isIdeMode;
  final Map<String, Annotation> annotationNames;
  final Set<String> suppressionNames;
  final LanguageMode languageMode;
  final boolean acceptConstKeyword;
  Config(Set<String> annotationWhitelist, Set<String> suppressionNames, boolean isIdeMode, LanguageMode languageMode, boolean acceptConstKeyword) {
    super();
    this.annotationNames = buildAnnotationNames(annotationWhitelist);
    this.parseJsDocDocumentation = isIdeMode;
    this.suppressionNames = suppressionNames;
    this.isIdeMode = isIdeMode;
    this.languageMode = languageMode;
    this.acceptConstKeyword = acceptConstKeyword;
  }
  private static Map<String, Annotation> buildAnnotationNames(Set<String> annotationWhitelist) {
    ImmutableMap.Builder<String, Annotation> annotationBuilder = ImmutableMap.builder();
    Map<String, Annotation> var_2283 = Annotation.recognizedAnnotations;
    annotationBuilder.putAll(var_2283);
    for (String unrecognizedAnnotation : annotationWhitelist) {
      if(!Annotation.recognizedAnnotations.containsKey(unrecognizedAnnotation)) {
        annotationBuilder.put(unrecognizedAnnotation, Annotation.NOT_IMPLEMENTED);
      }
    }
    return annotationBuilder.build();
  }
  public enum LanguageMode {
    ECMASCRIPT3(),

    ECMASCRIPT5(),

    ECMASCRIPT5_STRICT(),

  ;
  private LanguageMode() {
  }
  }
}