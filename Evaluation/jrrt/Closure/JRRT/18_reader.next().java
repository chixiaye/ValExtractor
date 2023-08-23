package com.google.debugging.sourcemap;
import com.google.common.collect.Lists;
import java.util.List;

class SourceMapLineDecoder  {
  private SourceMapLineDecoder() {
    super();
  }
  private static LineEntry decodeLineEntry(StringParser reader, int lastId) {
    int repDigits = 0;
    for(char peek = reader.peek(); peek == '!'; peek = reader.peek()) {
      repDigits++;
      reader.next();
    }
    int idDigits = 0;
    int reps = 0;
    if(repDigits == 0) {
      char digit = reader.next();
      int value = addBase64Digit(digit, 0);
      reps = (value >> 2);
      idDigits = (value & 3);
    }
    else {
      char var_18 = reader.next();
      char digit = var_18;
      idDigits = addBase64Digit(digit, 0);
      int value = 0;
      for(int i = 0; i < repDigits; i++) {
        digit = reader.next();
        value = addBase64Digit(digit, value);
      }
      reps = value;
    }
    reps += 1;
    idDigits += 1;
    int value = 0;
    for(int i = 0; i < idDigits; i++) {
      char digit = reader.next();
      value = addBase64Digit(digit, value);
    }
    int mappingId = getIdFromRelativeId(value, idDigits, lastId);
    return new LineEntry(mappingId, reps);
  }
  static LineEntry decodeLineEntry(String in, int lastId) {
    return decodeLineEntry(new StringParser(in), lastId);
  }
  private static List<Integer> decodeLine(StringParser reader) {
    List<Integer> result = Lists.newArrayListWithCapacity(512);
    int lastId = 0;
    while(reader.hasNext()){
      LineEntry entry = decodeLineEntry(reader, lastId);
      lastId = entry.id;
      for(int i = 0; i < entry.reps; i++) {
        result.add(entry.id);
      }
    }
    return result;
  }
  static List<Integer> decodeLine(String lineSource) {
    return decodeLine(new StringParser(lineSource));
  }
  private static int addBase64Digit(char digit, int previousValue) {
    return (previousValue * 64) + Base64.fromBase64(digit);
  }
  static int getIdFromRelativeId(int rawId, int digits, int lastId) {
    int base = 1 << (digits * 6);
    return ((rawId >= base / 2) ? rawId - base : rawId) + lastId;
  }
  
  static class LineEntry  {
    final int id;
    final int reps;
    public LineEntry(int id, int reps) {
      super();
      this.id = id;
      this.reps = reps;
    }
  }
  
  static class StringParser  {
    final String content;
    int current = 0;
    StringParser(String content) {
      super();
      this.content = content;
    }
    boolean hasNext() {
      return current < content.length() - 1;
    }
    char next() {
      return content.charAt(current++);
    }
    char peek() {
      return content.charAt(current);
    }
  }
}