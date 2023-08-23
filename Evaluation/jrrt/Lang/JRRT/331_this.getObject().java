package org.apache.commons.lang3.builder;
import org.apache.commons.lang3.ObjectUtils;

public class ToStringBuilder implements Builder<String>  {
  private static volatile ToStringStyle defaultStyle = ToStringStyle.DEFAULT_STYLE;
  final private StringBuffer buffer;
  final private Object object;
  final private ToStringStyle style;
  public ToStringBuilder(final Object object) {
    this(object, null, null);
  }
  public ToStringBuilder(final Object object, ToStringStyle style, StringBuffer buffer) {
    super();
    if(style == null) {
      style = getDefaultStyle();
    }
    if(buffer == null) {
      buffer = new StringBuffer(512);
    }
    this.buffer = buffer;
    this.style = style;
    this.object = object;
    style.appendStart(buffer, object);
  }
  public ToStringBuilder(final Object object, final ToStringStyle style) {
    this(object, style, null);
  }
  public Object getObject() {
    return object;
  }
  @Override() public String build() {
    return toString();
  }
  public static String reflectionToString(final Object object) {
    return ReflectionToStringBuilder.toString(object);
  }
  public static String reflectionToString(final Object object, final ToStringStyle style) {
    return ReflectionToStringBuilder.toString(object, style);
  }
  public static String reflectionToString(final Object object, final ToStringStyle style, final boolean outputTransients) {
    return ReflectionToStringBuilder.toString(object, style, outputTransients, false, null);
  }
  public static  <T extends java.lang.Object> String reflectionToString(final T object, final ToStringStyle style, final boolean outputTransients, final Class<? super T> reflectUpToClass) {
    return ReflectionToStringBuilder.toString(object, style, outputTransients, false, reflectUpToClass);
  }
  @Override() public String toString() {
    Object var_331 = this.getObject();
    if(var_331 == null) {
      this.getStringBuffer().append(this.getStyle().getNullText());
    }
    else {
      style.appendEnd(this.getStringBuffer(), this.getObject());
    }
    return this.getStringBuffer().toString();
  }
  public StringBuffer getStringBuffer() {
    return buffer;
  }
  public ToStringBuilder append(final boolean value) {
    style.append(buffer, null, value);
    return this;
  }
  public ToStringBuilder append(final boolean[] array) {
    style.append(buffer, null, array, null);
    return this;
  }
  public ToStringBuilder append(final byte value) {
    style.append(buffer, null, value);
    return this;
  }
  public ToStringBuilder append(final byte[] array) {
    style.append(buffer, null, array, null);
    return this;
  }
  public ToStringBuilder append(final char value) {
    style.append(buffer, null, value);
    return this;
  }
  public ToStringBuilder append(final char[] array) {
    style.append(buffer, null, array, null);
    return this;
  }
  public ToStringBuilder append(final double value) {
    style.append(buffer, null, value);
    return this;
  }
  public ToStringBuilder append(final double[] array) {
    style.append(buffer, null, array, null);
    return this;
  }
  public ToStringBuilder append(final float value) {
    style.append(buffer, null, value);
    return this;
  }
  public ToStringBuilder append(final float[] array) {
    style.append(buffer, null, array, null);
    return this;
  }
  public ToStringBuilder append(final int value) {
    style.append(buffer, null, value);
    return this;
  }
  public ToStringBuilder append(final int[] array) {
    style.append(buffer, null, array, null);
    return this;
  }
  public ToStringBuilder append(final Object obj) {
    style.append(buffer, null, obj, null);
    return this;
  }
  public ToStringBuilder append(final Object[] array) {
    style.append(buffer, null, array, null);
    return this;
  }
  public ToStringBuilder append(final String fieldName, final boolean value) {
    style.append(buffer, fieldName, value);
    return this;
  }
  public ToStringBuilder append(final String fieldName, final boolean[] array) {
    style.append(buffer, fieldName, array, null);
    return this;
  }
  public ToStringBuilder append(final String fieldName, final boolean[] array, final boolean fullDetail) {
    style.append(buffer, fieldName, array, Boolean.valueOf(fullDetail));
    return this;
  }
  public ToStringBuilder append(final String fieldName, final byte value) {
    style.append(buffer, fieldName, value);
    return this;
  }
  public ToStringBuilder append(final String fieldName, final byte[] array) {
    style.append(buffer, fieldName, array, null);
    return this;
  }
  public ToStringBuilder append(final String fieldName, final byte[] array, final boolean fullDetail) {
    style.append(buffer, fieldName, array, Boolean.valueOf(fullDetail));
    return this;
  }
  public ToStringBuilder append(final String fieldName, final char value) {
    style.append(buffer, fieldName, value);
    return this;
  }
  public ToStringBuilder append(final String fieldName, final char[] array) {
    style.append(buffer, fieldName, array, null);
    return this;
  }
  public ToStringBuilder append(final String fieldName, final char[] array, final boolean fullDetail) {
    style.append(buffer, fieldName, array, Boolean.valueOf(fullDetail));
    return this;
  }
  public ToStringBuilder append(final String fieldName, final double value) {
    style.append(buffer, fieldName, value);
    return this;
  }
  public ToStringBuilder append(final String fieldName, final double[] array) {
    style.append(buffer, fieldName, array, null);
    return this;
  }
  public ToStringBuilder append(final String fieldName, final double[] array, final boolean fullDetail) {
    style.append(buffer, fieldName, array, Boolean.valueOf(fullDetail));
    return this;
  }
  public ToStringBuilder append(final String fieldName, final float value) {
    style.append(buffer, fieldName, value);
    return this;
  }
  public ToStringBuilder append(final String fieldName, final float[] array) {
    style.append(buffer, fieldName, array, null);
    return this;
  }
  public ToStringBuilder append(final String fieldName, final float[] array, final boolean fullDetail) {
    style.append(buffer, fieldName, array, Boolean.valueOf(fullDetail));
    return this;
  }
  public ToStringBuilder append(final String fieldName, final int value) {
    style.append(buffer, fieldName, value);
    return this;
  }
  public ToStringBuilder append(final String fieldName, final int[] array) {
    style.append(buffer, fieldName, array, null);
    return this;
  }
  public ToStringBuilder append(final String fieldName, final int[] array, final boolean fullDetail) {
    style.append(buffer, fieldName, array, Boolean.valueOf(fullDetail));
    return this;
  }
  public ToStringBuilder append(final String fieldName, final Object obj) {
    style.append(buffer, fieldName, obj, null);
    return this;
  }
  public ToStringBuilder append(final String fieldName, final Object obj, final boolean fullDetail) {
    style.append(buffer, fieldName, obj, Boolean.valueOf(fullDetail));
    return this;
  }
  public ToStringBuilder append(final String fieldName, final Object[] array) {
    style.append(buffer, fieldName, array, null);
    return this;
  }
  public ToStringBuilder append(final String fieldName, final Object[] array, final boolean fullDetail) {
    style.append(buffer, fieldName, array, Boolean.valueOf(fullDetail));
    return this;
  }
  public ToStringBuilder append(final String fieldName, final long value) {
    style.append(buffer, fieldName, value);
    return this;
  }
  public ToStringBuilder append(final String fieldName, final long[] array) {
    style.append(buffer, fieldName, array, null);
    return this;
  }
  public ToStringBuilder append(final String fieldName, final long[] array, final boolean fullDetail) {
    style.append(buffer, fieldName, array, Boolean.valueOf(fullDetail));
    return this;
  }
  public ToStringBuilder append(final String fieldName, final short value) {
    style.append(buffer, fieldName, value);
    return this;
  }
  public ToStringBuilder append(final String fieldName, final short[] array) {
    style.append(buffer, fieldName, array, null);
    return this;
  }
  public ToStringBuilder append(final String fieldName, final short[] array, final boolean fullDetail) {
    style.append(buffer, fieldName, array, Boolean.valueOf(fullDetail));
    return this;
  }
  public ToStringBuilder append(final long value) {
    style.append(buffer, null, value);
    return this;
  }
  public ToStringBuilder append(final long[] array) {
    style.append(buffer, null, array, null);
    return this;
  }
  public ToStringBuilder append(final short value) {
    style.append(buffer, null, value);
    return this;
  }
  public ToStringBuilder append(final short[] array) {
    style.append(buffer, null, array, null);
    return this;
  }
  public ToStringBuilder appendAsObjectToString(final Object object) {
    ObjectUtils.identityToString(this.getStringBuffer(), object);
    return this;
  }
  public ToStringBuilder appendSuper(final String superToString) {
    if(superToString != null) {
      style.appendSuper(buffer, superToString);
    }
    return this;
  }
  public ToStringBuilder appendToString(final String toString) {
    if(toString != null) {
      style.appendToString(buffer, toString);
    }
    return this;
  }
  public static ToStringStyle getDefaultStyle() {
    return defaultStyle;
  }
  public ToStringStyle getStyle() {
    return style;
  }
  public static void setDefaultStyle(final ToStringStyle style) {
    if(style == null) {
      throw new IllegalArgumentException("The style must not be null");
    }
    defaultStyle = style;
  }
}