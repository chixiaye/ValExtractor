package com.google.javascript.jscomp;
import com.google.javascript.rhino.TokenStream;
import javax.annotation.Nullable;
import com.google.common.collect.Sets;
import com.google.common.primitives.Chars;
import java.util.*;

final class NameGenerator  {
  final static char[] FIRST_CHAR = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ$".toCharArray();
  final static char[] NONFIRST_CHAR = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_0123456789$".toCharArray();
  final private Set<String> reservedNames;
  final private String prefix;
  private int nameCount;
  final private char[] firstChars;
  final private char[] nonFirstChars;
  NameGenerator(Set<String> reservedNames, String prefix, @Nullable() char[] reservedCharacters) {
    super();
    this.reservedNames = reservedNames;
    this.prefix = prefix;
    this.firstChars = reserveCharacters(FIRST_CHAR, reservedCharacters);
    this.nonFirstChars = reserveCharacters(NONFIRST_CHAR, reservedCharacters);
    checkPrefix(prefix);
  }
  String generateNextName() {
    while(true){
      String name = prefix;
      int i = nameCount;
      if(name.isEmpty()) {
        int pos = i % firstChars.length;
        name += firstChars[pos];
        i /= firstChars.length;
      }
      while(i > 0){
        i--;
        int var_474 = nonFirstChars.length;
        int pos = i % var_474;
        name += nonFirstChars[pos];
        i /= nonFirstChars.length;
      }
      nameCount++;
      if(TokenStream.isKeyword(name) || reservedNames.contains(name)) {
        continue ;
      }
      return name;
    }
  }
  private boolean contains(char[] arr, char c) {
    for(int i = 0; i < arr.length; i++) {
      if(arr[i] == c) {
        return true;
      }
    }
    return false;
  }
  static char[] reserveCharacters(char[] chars, char[] reservedCharacters) {
    if(reservedCharacters == null || reservedCharacters.length == 0) {
      return chars;
    }
    Set<Character> charSet = Sets.newLinkedHashSet(Chars.asList(chars));
    for (char reservedCharacter : reservedCharacters) {
      charSet.remove(reservedCharacter);
    }
    return Chars.toArray(charSet);
  }
  private void checkPrefix(String prefix) {
    if(prefix.length() > 0) {
      if(!contains(firstChars, prefix.charAt(0))) {
        throw new IllegalArgumentException("prefix must start with one of: " + Arrays.toString(firstChars));
      }
      for(int pos = 1; pos < prefix.length(); ++pos) {
        if(!contains(nonFirstChars, prefix.charAt(pos))) {
          throw new IllegalArgumentException("prefix has invalid characters, " + "must be one of: " + Arrays.toString(nonFirstChars));
        }
      }
    }
  }
}