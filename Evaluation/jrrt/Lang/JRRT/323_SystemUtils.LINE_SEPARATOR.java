package org.apache.commons.lang3.builder;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.SystemUtils;

abstract public class ToStringStyle implements Serializable  {
  final private static long serialVersionUID = -2587890625525655916L;
  final public static ToStringStyle DEFAULT_STYLE = new DefaultToStringStyle();
  final public static ToStringStyle MULTI_LINE_STYLE = new MultiLineToStringStyle();
  final public static ToStringStyle NO_FIELD_NAMES_STYLE = new NoFieldNameToStringStyle();
  final public static ToStringStyle SHORT_PREFIX_STYLE = new ShortPrefixToStringStyle();
  final public static ToStringStyle SIMPLE_STYLE = new SimpleToStringStyle();
  final private static ThreadLocal<WeakHashMap<Object, Object>> REGISTRY = new ThreadLocal<WeakHashMap<Object, Object>>();
  private boolean useFieldNames = true;
  private boolean useClassName = true;
  private boolean useShortClassName = false;
  private boolean useIdentityHashCode = true;
  private String contentStart = "[";
  private String contentEnd = "]";
  private String fieldNameValueSeparator = "=";
  private boolean fieldSeparatorAtStart = false;
  private boolean fieldSeparatorAtEnd = false;
  private String fieldSeparator = ",";
  private String arrayStart = "{";
  private String arraySeparator = ",";
  private boolean arrayContentDetail = true;
  private String arrayEnd = "}";
  private boolean defaultFullDetail = true;
  private String nullText = "<null>";
  private String sizeStartText = "<size=";
  private String sizeEndText = ">";
  private String summaryObjectStartText = "<";
  private String summaryObjectEndText = ">";
  protected ToStringStyle() {
    super();
  }
  static Map<Object, Object> getRegistry() {
    return REGISTRY.get();
  }
  protected String getArrayEnd() {
    return arrayEnd;
  }
  protected String getArraySeparator() {
    return arraySeparator;
  }
  protected String getArrayStart() {
    return arrayStart;
  }
  protected String getContentEnd() {
    return contentEnd;
  }
  protected String getContentStart() {
    return contentStart;
  }
  protected String getFieldNameValueSeparator() {
    return fieldNameValueSeparator;
  }
  protected String getFieldSeparator() {
    return fieldSeparator;
  }
  protected String getNullText() {
    return nullText;
  }
  protected String getShortClassName(final Class<?> cls) {
    return ClassUtils.getShortClassName(cls);
  }
  protected String getSizeEndText() {
    return sizeEndText;
  }
  protected String getSizeStartText() {
    return sizeStartText;
  }
  protected String getSummaryObjectEndText() {
    return summaryObjectEndText;
  }
  protected String getSummaryObjectStartText() {
    return summaryObjectStartText;
  }
  protected boolean isArrayContentDetail() {
    return arrayContentDetail;
  }
  protected boolean isDefaultFullDetail() {
    return defaultFullDetail;
  }
  protected boolean isFieldSeparatorAtEnd() {
    return fieldSeparatorAtEnd;
  }
  protected boolean isFieldSeparatorAtStart() {
    return fieldSeparatorAtStart;
  }
  protected boolean isFullDetail(final Boolean fullDetailRequest) {
    if(fullDetailRequest == null) {
      return defaultFullDetail;
    }
    return fullDetailRequest.booleanValue();
  }
  static boolean isRegistered(final Object value) {
    final Map<Object, Object> m = getRegistry();
    return m != null && m.containsKey(value);
  }
  protected boolean isUseClassName() {
    return useClassName;
  }
  protected boolean isUseFieldNames() {
    return useFieldNames;
  }
  protected boolean isUseIdentityHashCode() {
    return useIdentityHashCode;
  }
  protected boolean isUseShortClassName() {
    return useShortClassName;
  }
  public void append(final StringBuffer buffer, final String fieldName, final boolean value) {
    appendFieldStart(buffer, fieldName);
    appendDetail(buffer, fieldName, value);
    appendFieldEnd(buffer, fieldName);
  }
  public void append(final StringBuffer buffer, final String fieldName, final boolean[] array, final Boolean fullDetail) {
    appendFieldStart(buffer, fieldName);
    if(array == null) {
      appendNullText(buffer, fieldName);
    }
    else 
      if(isFullDetail(fullDetail)) {
        appendDetail(buffer, fieldName, array);
      }
      else {
        appendSummary(buffer, fieldName, array);
      }
    appendFieldEnd(buffer, fieldName);
  }
  public void append(final StringBuffer buffer, final String fieldName, final byte value) {
    appendFieldStart(buffer, fieldName);
    appendDetail(buffer, fieldName, value);
    appendFieldEnd(buffer, fieldName);
  }
  public void append(final StringBuffer buffer, final String fieldName, final byte[] array, final Boolean fullDetail) {
    appendFieldStart(buffer, fieldName);
    if(array == null) {
      appendNullText(buffer, fieldName);
    }
    else 
      if(isFullDetail(fullDetail)) {
        appendDetail(buffer, fieldName, array);
      }
      else {
        appendSummary(buffer, fieldName, array);
      }
    appendFieldEnd(buffer, fieldName);
  }
  public void append(final StringBuffer buffer, final String fieldName, final char value) {
    appendFieldStart(buffer, fieldName);
    appendDetail(buffer, fieldName, value);
    appendFieldEnd(buffer, fieldName);
  }
  public void append(final StringBuffer buffer, final String fieldName, final char[] array, final Boolean fullDetail) {
    appendFieldStart(buffer, fieldName);
    if(array == null) {
      appendNullText(buffer, fieldName);
    }
    else 
      if(isFullDetail(fullDetail)) {
        appendDetail(buffer, fieldName, array);
      }
      else {
        appendSummary(buffer, fieldName, array);
      }
    appendFieldEnd(buffer, fieldName);
  }
  public void append(final StringBuffer buffer, final String fieldName, final double value) {
    appendFieldStart(buffer, fieldName);
    appendDetail(buffer, fieldName, value);
    appendFieldEnd(buffer, fieldName);
  }
  public void append(final StringBuffer buffer, final String fieldName, final double[] array, final Boolean fullDetail) {
    appendFieldStart(buffer, fieldName);
    if(array == null) {
      appendNullText(buffer, fieldName);
    }
    else 
      if(isFullDetail(fullDetail)) {
        appendDetail(buffer, fieldName, array);
      }
      else {
        appendSummary(buffer, fieldName, array);
      }
    appendFieldEnd(buffer, fieldName);
  }
  public void append(final StringBuffer buffer, final String fieldName, final float value) {
    appendFieldStart(buffer, fieldName);
    appendDetail(buffer, fieldName, value);
    appendFieldEnd(buffer, fieldName);
  }
  public void append(final StringBuffer buffer, final String fieldName, final float[] array, final Boolean fullDetail) {
    appendFieldStart(buffer, fieldName);
    if(array == null) {
      appendNullText(buffer, fieldName);
    }
    else 
      if(isFullDetail(fullDetail)) {
        appendDetail(buffer, fieldName, array);
      }
      else {
        appendSummary(buffer, fieldName, array);
      }
    appendFieldEnd(buffer, fieldName);
  }
  public void append(final StringBuffer buffer, final String fieldName, final int value) {
    appendFieldStart(buffer, fieldName);
    appendDetail(buffer, fieldName, value);
    appendFieldEnd(buffer, fieldName);
  }
  public void append(final StringBuffer buffer, final String fieldName, final int[] array, final Boolean fullDetail) {
    appendFieldStart(buffer, fieldName);
    if(array == null) {
      appendNullText(buffer, fieldName);
    }
    else 
      if(isFullDetail(fullDetail)) {
        appendDetail(buffer, fieldName, array);
      }
      else {
        appendSummary(buffer, fieldName, array);
      }
    appendFieldEnd(buffer, fieldName);
  }
  public void append(final StringBuffer buffer, final String fieldName, final Object value, final Boolean fullDetail) {
    appendFieldStart(buffer, fieldName);
    if(value == null) {
      appendNullText(buffer, fieldName);
    }
    else {
      appendInternal(buffer, fieldName, value, isFullDetail(fullDetail));
    }
    appendFieldEnd(buffer, fieldName);
  }
  public void append(final StringBuffer buffer, final String fieldName, final Object[] array, final Boolean fullDetail) {
    appendFieldStart(buffer, fieldName);
    if(array == null) {
      appendNullText(buffer, fieldName);
    }
    else 
      if(isFullDetail(fullDetail)) {
        appendDetail(buffer, fieldName, array);
      }
      else {
        appendSummary(buffer, fieldName, array);
      }
    appendFieldEnd(buffer, fieldName);
  }
  public void append(final StringBuffer buffer, final String fieldName, final long value) {
    appendFieldStart(buffer, fieldName);
    appendDetail(buffer, fieldName, value);
    appendFieldEnd(buffer, fieldName);
  }
  public void append(final StringBuffer buffer, final String fieldName, final long[] array, final Boolean fullDetail) {
    appendFieldStart(buffer, fieldName);
    if(array == null) {
      appendNullText(buffer, fieldName);
    }
    else 
      if(isFullDetail(fullDetail)) {
        appendDetail(buffer, fieldName, array);
      }
      else {
        appendSummary(buffer, fieldName, array);
      }
    appendFieldEnd(buffer, fieldName);
  }
  public void append(final StringBuffer buffer, final String fieldName, final short value) {
    appendFieldStart(buffer, fieldName);
    appendDetail(buffer, fieldName, value);
    appendFieldEnd(buffer, fieldName);
  }
  public void append(final StringBuffer buffer, final String fieldName, final short[] array, final Boolean fullDetail) {
    appendFieldStart(buffer, fieldName);
    if(array == null) {
      appendNullText(buffer, fieldName);
    }
    else 
      if(isFullDetail(fullDetail)) {
        appendDetail(buffer, fieldName, array);
      }
      else {
        appendSummary(buffer, fieldName, array);
      }
    appendFieldEnd(buffer, fieldName);
  }
  protected void appendClassName(final StringBuffer buffer, final Object object) {
    if(useClassName && object != null) {
      register(object);
      if(useShortClassName) {
        buffer.append(getShortClassName(object.getClass()));
      }
      else {
        buffer.append(object.getClass().getName());
      }
    }
  }
  protected void appendContentEnd(final StringBuffer buffer) {
    buffer.append(contentEnd);
  }
  protected void appendContentStart(final StringBuffer buffer) {
    buffer.append(contentStart);
  }
  protected void appendCyclicObject(final StringBuffer buffer, final String fieldName, final Object value) {
    ObjectUtils.identityToString(buffer, value);
  }
  protected void appendDetail(final StringBuffer buffer, final String fieldName, final boolean value) {
    buffer.append(value);
  }
  protected void appendDetail(final StringBuffer buffer, final String fieldName, final boolean[] array) {
    buffer.append(arrayStart);
    for(int i = 0; i < array.length; i++) {
      if(i > 0) {
        buffer.append(arraySeparator);
      }
      appendDetail(buffer, fieldName, array[i]);
    }
    buffer.append(arrayEnd);
  }
  protected void appendDetail(final StringBuffer buffer, final String fieldName, final byte value) {
    buffer.append(value);
  }
  protected void appendDetail(final StringBuffer buffer, final String fieldName, final byte[] array) {
    buffer.append(arrayStart);
    for(int i = 0; i < array.length; i++) {
      if(i > 0) {
        buffer.append(arraySeparator);
      }
      appendDetail(buffer, fieldName, array[i]);
    }
    buffer.append(arrayEnd);
  }
  protected void appendDetail(final StringBuffer buffer, final String fieldName, final char value) {
    buffer.append(value);
  }
  protected void appendDetail(final StringBuffer buffer, final String fieldName, final char[] array) {
    buffer.append(arrayStart);
    for(int i = 0; i < array.length; i++) {
      if(i > 0) {
        buffer.append(arraySeparator);
      }
      appendDetail(buffer, fieldName, array[i]);
    }
    buffer.append(arrayEnd);
  }
  protected void appendDetail(final StringBuffer buffer, final String fieldName, final double value) {
    buffer.append(value);
  }
  protected void appendDetail(final StringBuffer buffer, final String fieldName, final double[] array) {
    buffer.append(arrayStart);
    for(int i = 0; i < array.length; i++) {
      if(i > 0) {
        buffer.append(arraySeparator);
      }
      appendDetail(buffer, fieldName, array[i]);
    }
    buffer.append(arrayEnd);
  }
  protected void appendDetail(final StringBuffer buffer, final String fieldName, final float value) {
    buffer.append(value);
  }
  protected void appendDetail(final StringBuffer buffer, final String fieldName, final float[] array) {
    buffer.append(arrayStart);
    for(int i = 0; i < array.length; i++) {
      if(i > 0) {
        buffer.append(arraySeparator);
      }
      appendDetail(buffer, fieldName, array[i]);
    }
    buffer.append(arrayEnd);
  }
  protected void appendDetail(final StringBuffer buffer, final String fieldName, final int value) {
    buffer.append(value);
  }
  protected void appendDetail(final StringBuffer buffer, final String fieldName, final int[] array) {
    buffer.append(arrayStart);
    for(int i = 0; i < array.length; i++) {
      if(i > 0) {
        buffer.append(arraySeparator);
      }
      appendDetail(buffer, fieldName, array[i]);
    }
    buffer.append(arrayEnd);
  }
  protected void appendDetail(final StringBuffer buffer, final String fieldName, final Object value) {
    buffer.append(value);
  }
  protected void appendDetail(final StringBuffer buffer, final String fieldName, final Object[] array) {
    buffer.append(arrayStart);
    for(int i = 0; i < array.length; i++) {
      final Object item = array[i];
      if(i > 0) {
        buffer.append(arraySeparator);
      }
      if(item == null) {
        appendNullText(buffer, fieldName);
      }
      else {
        appendInternal(buffer, fieldName, item, arrayContentDetail);
      }
    }
    buffer.append(arrayEnd);
  }
  protected void appendDetail(final StringBuffer buffer, final String fieldName, final Collection<?> coll) {
    buffer.append(coll);
  }
  protected void appendDetail(final StringBuffer buffer, final String fieldName, final Map<?, ?> map) {
    buffer.append(map);
  }
  protected void appendDetail(final StringBuffer buffer, final String fieldName, final long value) {
    buffer.append(value);
  }
  protected void appendDetail(final StringBuffer buffer, final String fieldName, final long[] array) {
    buffer.append(arrayStart);
    for(int i = 0; i < array.length; i++) {
      if(i > 0) {
        buffer.append(arraySeparator);
      }
      appendDetail(buffer, fieldName, array[i]);
    }
    buffer.append(arrayEnd);
  }
  protected void appendDetail(final StringBuffer buffer, final String fieldName, final short value) {
    buffer.append(value);
  }
  protected void appendDetail(final StringBuffer buffer, final String fieldName, final short[] array) {
    buffer.append(arrayStart);
    for(int i = 0; i < array.length; i++) {
      if(i > 0) {
        buffer.append(arraySeparator);
      }
      appendDetail(buffer, fieldName, array[i]);
    }
    buffer.append(arrayEnd);
  }
  public void appendEnd(final StringBuffer buffer, final Object object) {
    if(this.fieldSeparatorAtEnd == false) {
      removeLastFieldSeparator(buffer);
    }
    appendContentEnd(buffer);
    unregister(object);
  }
  protected void appendFieldEnd(final StringBuffer buffer, final String fieldName) {
    appendFieldSeparator(buffer);
  }
  protected void appendFieldSeparator(final StringBuffer buffer) {
    buffer.append(fieldSeparator);
  }
  protected void appendFieldStart(final StringBuffer buffer, final String fieldName) {
    if(useFieldNames && fieldName != null) {
      buffer.append(fieldName);
      buffer.append(fieldNameValueSeparator);
    }
  }
  protected void appendIdentityHashCode(final StringBuffer buffer, final Object object) {
    if(this.isUseIdentityHashCode() && object != null) {
      register(object);
      buffer.append('@');
      buffer.append(Integer.toHexString(System.identityHashCode(object)));
    }
  }
  protected void appendInternal(final StringBuffer buffer, final String fieldName, final Object value, final boolean detail) {
    if(isRegistered(value) && !(value instanceof Number || value instanceof Boolean || value instanceof Character)) {
      appendCyclicObject(buffer, fieldName, value);
      return ;
    }
    register(value);
    try {
      if(value instanceof Collection<?>) {
        if(detail) {
          appendDetail(buffer, fieldName, (Collection<?>)value);
        }
        else {
          appendSummarySize(buffer, fieldName, ((Collection<?>)value).size());
        }
      }
      else 
        if(value instanceof Map<?, ?>) {
          if(detail) {
            appendDetail(buffer, fieldName, (Map<?, ?>)value);
          }
          else {
            appendSummarySize(buffer, fieldName, ((Map<?, ?>)value).size());
          }
        }
        else 
          if(value instanceof long[]) {
            if(detail) {
              appendDetail(buffer, fieldName, (long[])value);
            }
            else {
              appendSummary(buffer, fieldName, (long[])value);
            }
          }
          else 
            if(value instanceof int[]) {
              if(detail) {
                appendDetail(buffer, fieldName, (int[])value);
              }
              else {
                appendSummary(buffer, fieldName, (int[])value);
              }
            }
            else 
              if(value instanceof short[]) {
                if(detail) {
                  appendDetail(buffer, fieldName, (short[])value);
                }
                else {
                  appendSummary(buffer, fieldName, (short[])value);
                }
              }
              else 
                if(value instanceof byte[]) {
                  if(detail) {
                    appendDetail(buffer, fieldName, (byte[])value);
                  }
                  else {
                    appendSummary(buffer, fieldName, (byte[])value);
                  }
                }
                else 
                  if(value instanceof char[]) {
                    if(detail) {
                      appendDetail(buffer, fieldName, (char[])value);
                    }
                    else {
                      appendSummary(buffer, fieldName, (char[])value);
                    }
                  }
                  else 
                    if(value instanceof double[]) {
                      if(detail) {
                        appendDetail(buffer, fieldName, (double[])value);
                      }
                      else {
                        appendSummary(buffer, fieldName, (double[])value);
                      }
                    }
                    else 
                      if(value instanceof float[]) {
                        if(detail) {
                          appendDetail(buffer, fieldName, (float[])value);
                        }
                        else {
                          appendSummary(buffer, fieldName, (float[])value);
                        }
                      }
                      else 
                        if(value instanceof boolean[]) {
                          if(detail) {
                            appendDetail(buffer, fieldName, (boolean[])value);
                          }
                          else {
                            appendSummary(buffer, fieldName, (boolean[])value);
                          }
                        }
                        else 
                          if(value.getClass().isArray()) {
                            if(detail) {
                              appendDetail(buffer, fieldName, (Object[])value);
                            }
                            else {
                              appendSummary(buffer, fieldName, (Object[])value);
                            }
                          }
                          else {
                            if(detail) {
                              appendDetail(buffer, fieldName, value);
                            }
                            else {
                              appendSummary(buffer, fieldName, value);
                            }
                          }
    }
    finally {
      unregister(value);
    }
  }
  protected void appendNullText(final StringBuffer buffer, final String fieldName) {
    buffer.append(nullText);
  }
  public void appendStart(final StringBuffer buffer, final Object object) {
    if(object != null) {
      appendClassName(buffer, object);
      appendIdentityHashCode(buffer, object);
      appendContentStart(buffer);
      if(fieldSeparatorAtStart) {
        appendFieldSeparator(buffer);
      }
    }
  }
  protected void appendSummary(final StringBuffer buffer, final String fieldName, final boolean[] array) {
    appendSummarySize(buffer, fieldName, array.length);
  }
  protected void appendSummary(final StringBuffer buffer, final String fieldName, final byte[] array) {
    appendSummarySize(buffer, fieldName, array.length);
  }
  protected void appendSummary(final StringBuffer buffer, final String fieldName, final char[] array) {
    appendSummarySize(buffer, fieldName, array.length);
  }
  protected void appendSummary(final StringBuffer buffer, final String fieldName, final double[] array) {
    appendSummarySize(buffer, fieldName, array.length);
  }
  protected void appendSummary(final StringBuffer buffer, final String fieldName, final float[] array) {
    appendSummarySize(buffer, fieldName, array.length);
  }
  protected void appendSummary(final StringBuffer buffer, final String fieldName, final int[] array) {
    appendSummarySize(buffer, fieldName, array.length);
  }
  protected void appendSummary(final StringBuffer buffer, final String fieldName, final Object value) {
    buffer.append(summaryObjectStartText);
    buffer.append(getShortClassName(value.getClass()));
    buffer.append(summaryObjectEndText);
  }
  protected void appendSummary(final StringBuffer buffer, final String fieldName, final Object[] array) {
    appendSummarySize(buffer, fieldName, array.length);
  }
  protected void appendSummary(final StringBuffer buffer, final String fieldName, final long[] array) {
    appendSummarySize(buffer, fieldName, array.length);
  }
  protected void appendSummary(final StringBuffer buffer, final String fieldName, final short[] array) {
    appendSummarySize(buffer, fieldName, array.length);
  }
  protected void appendSummarySize(final StringBuffer buffer, final String fieldName, final int size) {
    buffer.append(sizeStartText);
    buffer.append(size);
    buffer.append(sizeEndText);
  }
  public void appendSuper(final StringBuffer buffer, final String superToString) {
    appendToString(buffer, superToString);
  }
  public void appendToString(final StringBuffer buffer, final String toString) {
    if(toString != null) {
      final int pos1 = toString.indexOf(contentStart) + contentStart.length();
      final int pos2 = toString.lastIndexOf(contentEnd);
      if(pos1 != pos2 && pos1 >= 0 && pos2 >= 0) {
        final String data = toString.substring(pos1, pos2);
        if(fieldSeparatorAtStart) {
          removeLastFieldSeparator(buffer);
        }
        buffer.append(data);
        appendFieldSeparator(buffer);
      }
    }
  }
  protected void reflectionAppendArrayDetail(final StringBuffer buffer, final String fieldName, final Object array) {
    buffer.append(arrayStart);
    final int length = Array.getLength(array);
    for(int i = 0; i < length; i++) {
      final Object item = Array.get(array, i);
      if(i > 0) {
        buffer.append(arraySeparator);
      }
      if(item == null) {
        appendNullText(buffer, fieldName);
      }
      else {
        appendInternal(buffer, fieldName, item, arrayContentDetail);
      }
    }
    buffer.append(arrayEnd);
  }
  static void register(final Object value) {
    if(value != null) {
      final Map<Object, Object> m = getRegistry();
      if(m == null) {
        REGISTRY.set(new WeakHashMap<Object, Object>());
      }
      getRegistry().put(value, null);
    }
  }
  protected void removeLastFieldSeparator(final StringBuffer buffer) {
    final int len = buffer.length();
    final int sepLen = fieldSeparator.length();
    if(len > 0 && sepLen > 0 && len >= sepLen) {
      boolean match = true;
      for(int i = 0; i < sepLen; i++) {
        if(buffer.charAt(len - 1 - i) != fieldSeparator.charAt(sepLen - 1 - i)) {
          match = false;
          break ;
        }
      }
      if(match) {
        buffer.setLength(len - sepLen);
      }
    }
  }
  protected void setArrayContentDetail(final boolean arrayContentDetail) {
    this.arrayContentDetail = arrayContentDetail;
  }
  protected void setArrayEnd(String arrayEnd) {
    if(arrayEnd == null) {
      arrayEnd = "";
    }
    this.arrayEnd = arrayEnd;
  }
  protected void setArraySeparator(String arraySeparator) {
    if(arraySeparator == null) {
      arraySeparator = "";
    }
    this.arraySeparator = arraySeparator;
  }
  protected void setArrayStart(String arrayStart) {
    if(arrayStart == null) {
      arrayStart = "";
    }
    this.arrayStart = arrayStart;
  }
  protected void setContentEnd(String contentEnd) {
    if(contentEnd == null) {
      contentEnd = "";
    }
    this.contentEnd = contentEnd;
  }
  protected void setContentStart(String contentStart) {
    if(contentStart == null) {
      contentStart = "";
    }
    this.contentStart = contentStart;
  }
  protected void setDefaultFullDetail(final boolean defaultFullDetail) {
    this.defaultFullDetail = defaultFullDetail;
  }
  protected void setFieldNameValueSeparator(String fieldNameValueSeparator) {
    if(fieldNameValueSeparator == null) {
      fieldNameValueSeparator = "";
    }
    this.fieldNameValueSeparator = fieldNameValueSeparator;
  }
  protected void setFieldSeparator(String fieldSeparator) {
    if(fieldSeparator == null) {
      fieldSeparator = "";
    }
    this.fieldSeparator = fieldSeparator;
  }
  protected void setFieldSeparatorAtEnd(final boolean fieldSeparatorAtEnd) {
    this.fieldSeparatorAtEnd = fieldSeparatorAtEnd;
  }
  protected void setFieldSeparatorAtStart(final boolean fieldSeparatorAtStart) {
    this.fieldSeparatorAtStart = fieldSeparatorAtStart;
  }
  protected void setNullText(String nullText) {
    if(nullText == null) {
      nullText = "";
    }
    this.nullText = nullText;
  }
  protected void setSizeEndText(String sizeEndText) {
    if(sizeEndText == null) {
      sizeEndText = "";
    }
    this.sizeEndText = sizeEndText;
  }
  protected void setSizeStartText(String sizeStartText) {
    if(sizeStartText == null) {
      sizeStartText = "";
    }
    this.sizeStartText = sizeStartText;
  }
  protected void setSummaryObjectEndText(String summaryObjectEndText) {
    if(summaryObjectEndText == null) {
      summaryObjectEndText = "";
    }
    this.summaryObjectEndText = summaryObjectEndText;
  }
  protected void setSummaryObjectStartText(String summaryObjectStartText) {
    if(summaryObjectStartText == null) {
      summaryObjectStartText = "";
    }
    this.summaryObjectStartText = summaryObjectStartText;
  }
  protected void setUseClassName(final boolean useClassName) {
    this.useClassName = useClassName;
  }
  protected void setUseFieldNames(final boolean useFieldNames) {
    this.useFieldNames = useFieldNames;
  }
  protected void setUseIdentityHashCode(final boolean useIdentityHashCode) {
    this.useIdentityHashCode = useIdentityHashCode;
  }
  protected void setUseShortClassName(final boolean useShortClassName) {
    this.useShortClassName = useShortClassName;
  }
  static void unregister(final Object value) {
    if(value != null) {
      final Map<Object, Object> m = getRegistry();
      if(m != null) {
        m.remove(value);
        if(m.isEmpty()) {
          REGISTRY.remove();
        }
      }
    }
  }
  
  final private static class DefaultToStringStyle extends ToStringStyle  {
    final private static long serialVersionUID = 1L;
    DefaultToStringStyle() {
      super();
    }
    private Object readResolve() {
      return ToStringStyle.DEFAULT_STYLE;
    }
  }
  
  final private static class MultiLineToStringStyle extends ToStringStyle  {
    final private static long serialVersionUID = 1L;
    MultiLineToStringStyle() {
      super();
      this.setContentStart("[");
      String var_323 = SystemUtils.LINE_SEPARATOR;
      this.setFieldSeparator(var_323 + "  ");
      this.setFieldSeparatorAtStart(true);
      this.setContentEnd(SystemUtils.LINE_SEPARATOR + "]");
    }
    private Object readResolve() {
      return ToStringStyle.MULTI_LINE_STYLE;
    }
  }
  
  final private static class NoFieldNameToStringStyle extends ToStringStyle  {
    final private static long serialVersionUID = 1L;
    NoFieldNameToStringStyle() {
      super();
      this.setUseFieldNames(false);
    }
    private Object readResolve() {
      return ToStringStyle.NO_FIELD_NAMES_STYLE;
    }
  }
  
  final private static class ShortPrefixToStringStyle extends ToStringStyle  {
    final private static long serialVersionUID = 1L;
    ShortPrefixToStringStyle() {
      super();
      this.setUseShortClassName(true);
      this.setUseIdentityHashCode(false);
    }
    private Object readResolve() {
      return ToStringStyle.SHORT_PREFIX_STYLE;
    }
  }
  
  final private static class SimpleToStringStyle extends ToStringStyle  {
    final private static long serialVersionUID = 1L;
    SimpleToStringStyle() {
      super();
      this.setUseClassName(false);
      this.setUseIdentityHashCode(false);
      this.setUseFieldNames(false);
      this.setContentStart("");
      this.setContentEnd("");
    }
    private Object readResolve() {
      return ToStringStyle.SIMPLE_STYLE;
    }
  }
}