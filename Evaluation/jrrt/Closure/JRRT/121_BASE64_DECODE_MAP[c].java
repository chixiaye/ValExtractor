package com.google.debugging.sourcemap;
import java.util.Arrays;

final public class Base64  {
  final private static String BASE64_MAP = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "abcdefghijklmnopqrstuvwxyz" + "0123456789+/";
  final private static int[] BASE64_DECODE_MAP = new int[256];
  static {
    Arrays.fill(BASE64_DECODE_MAP, -1);
    for(int i = 0; i < BASE64_MAP.length(); i++) {
      BASE64_DECODE_MAP[BASE64_MAP.charAt(i)] = i;
    }
  }
  private Base64() {
    super();
  }
  public static String base64EncodeInt(int value) {
    char[] c = new char[6];
    for(int i = 0; i < 5; i++) {
      c[i] = Base64.toBase64((value >> (26 - i * 6)) & 0x3f);
    }
    c[5] = Base64.toBase64((value << 4) & 0x3f);
    return new String(c);
  }
  public static char toBase64(int value) {
    assert (value <= 63 && value >= 0) : "value out of range:" + value;
    return BASE64_MAP.charAt(value);
  }
  public static int fromBase64(char c) {
    int var_121 = BASE64_DECODE_MAP[c];
    int result = var_121;
    assert (result != -1) : "invalid char";
    return BASE64_DECODE_MAP[c];
  }
}