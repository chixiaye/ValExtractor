package org.apache.commons.lang3;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EnumUtils  {
  final private static String NULL_ELEMENTS_NOT_PERMITTED = "null elements not permitted";
  final private static String CANNOT_STORE_S_S_VALUES_IN_S_BITS = "Cannot store %s %s values in %s bits";
  final private static String S_DOES_NOT_SEEM_TO_BE_AN_ENUM_TYPE = "%s does not seem to be an Enum type";
  final private static String ENUM_CLASS_MUST_BE_DEFINED = "EnumClass must be defined.";
  public EnumUtils() {
    super();
  }
  private static  <E extends java.lang.Enum<org.apache.commons.lang3.E>> Class<E> asEnum(final Class<E> enumClass) {
    Validate.notNull(enumClass, ENUM_CLASS_MUST_BE_DEFINED);
    Validate.isTrue(enumClass.isEnum(), S_DOES_NOT_SEEM_TO_BE_AN_ENUM_TYPE, enumClass);
    return enumClass;
  }
  private static  <E extends java.lang.Enum<org.apache.commons.lang3.E>> Class<E> checkBitVectorable(final Class<E> enumClass) {
    final E[] constants = asEnum(enumClass).getEnumConstants();
    int var_282 = constants.length;
    Validate.isTrue(var_282 <= Long.SIZE, CANNOT_STORE_S_S_VALUES_IN_S_BITS, constants.length, enumClass.getSimpleName(), Long.SIZE);
    return enumClass;
  }
  public static  <E extends java.lang.Enum<org.apache.commons.lang3.E>> E getEnum(final Class<E> enumClass, final String enumName) {
    if(enumName == null) {
      return null;
    }
    try {
      return Enum.valueOf(enumClass, enumName);
    }
    catch (final IllegalArgumentException ex) {
      return null;
    }
  }
  public static  <E extends java.lang.Enum<org.apache.commons.lang3.E>> EnumSet<E> processBitVector(final Class<E> enumClass, final long value) {
    checkBitVectorable(enumClass).getEnumConstants();
    return processBitVectors(enumClass, value);
  }
  public static  <E extends java.lang.Enum<org.apache.commons.lang3.E>> EnumSet<E> processBitVectors(final Class<E> enumClass, long ... values) {
    final EnumSet<E> results = EnumSet.noneOf(asEnum(enumClass));
    values = ArrayUtils.clone(Validate.notNull(values));
    ArrayUtils.reverse(values);
    for (final E constant : enumClass.getEnumConstants()) {
      final int block = constant.ordinal() / Long.SIZE;
      if(block < values.length && (values[block] & 1 << (constant.ordinal() % Long.SIZE)) != 0) {
        results.add(constant);
      }
    }
    return results;
  }
  public static  <E extends java.lang.Enum<org.apache.commons.lang3.E>> List<E> getEnumList(final Class<E> enumClass) {
    return new ArrayList<E>(Arrays.asList(enumClass.getEnumConstants()));
  }
  public static  <E extends java.lang.Enum<org.apache.commons.lang3.E>> Map<String, E> getEnumMap(final Class<E> enumClass) {
    final Map<String, E> map = new LinkedHashMap<String, E>();
    for (final E e : enumClass.getEnumConstants()) {
      map.put(e.name(), e);
    }
    return map;
  }
  public static  <E extends java.lang.Enum<org.apache.commons.lang3.E>> boolean isValidEnum(final Class<E> enumClass, final String enumName) {
    if(enumName == null) {
      return false;
    }
    try {
      Enum.valueOf(enumClass, enumName);
      return true;
    }
    catch (final IllegalArgumentException ex) {
      return false;
    }
  }
  public static  <E extends java.lang.Enum<org.apache.commons.lang3.E>> long generateBitVector(final Class<E> enumClass, final E ... values) {
    Validate.noNullElements(values);
    return generateBitVector(enumClass, Arrays.<E>asList(values));
  }
  public static  <E extends java.lang.Enum<org.apache.commons.lang3.E>> long generateBitVector(final Class<E> enumClass, final Iterable<E> values) {
    checkBitVectorable(enumClass);
    Validate.notNull(values);
    long total = 0;
    for (final E constant : values) {
      Validate.isTrue(constant != null, NULL_ELEMENTS_NOT_PERMITTED);
      total |= 1 << constant.ordinal();
    }
    return total;
  }
  public static  <E extends java.lang.Enum<org.apache.commons.lang3.E>> long[] generateBitVectors(final Class<E> enumClass, final E ... values) {
    asEnum(enumClass);
    Validate.noNullElements(values);
    final EnumSet<E> condensed = EnumSet.noneOf(enumClass);
    Collections.addAll(condensed, values);
    final long[] result = new long[(enumClass.getEnumConstants().length - 1) / Long.SIZE + 1];
    for (final E value : condensed) {
      result[value.ordinal() / Long.SIZE] |= 1 << (value.ordinal() % Long.SIZE);
    }
    ArrayUtils.reverse(result);
    return result;
  }
  public static  <E extends java.lang.Enum<org.apache.commons.lang3.E>> long[] generateBitVectors(final Class<E> enumClass, final Iterable<E> values) {
    asEnum(enumClass);
    Validate.notNull(values);
    final EnumSet<E> condensed = EnumSet.noneOf(enumClass);
    for (final E constant : values) {
      Validate.isTrue(constant != null, NULL_ELEMENTS_NOT_PERMITTED);
      condensed.add(constant);
    }
    final long[] result = new long[(enumClass.getEnumConstants().length - 1) / Long.SIZE + 1];
    for (final E value : condensed) {
      result[value.ordinal() / Long.SIZE] |= 1 << (value.ordinal() % Long.SIZE);
    }
    ArrayUtils.reverse(result);
    return result;
  }
}