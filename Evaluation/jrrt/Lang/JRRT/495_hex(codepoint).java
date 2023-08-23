package org.apache.commons.lang3.text.translate;
import java.io.IOException;
import java.io.Writer;

public class UnicodeEscaper extends CodePointTranslator  {
  final private int below;
  final private int above;
  final private boolean between;
  public UnicodeEscaper() {
    this(0, Integer.MAX_VALUE, true);
  }
  protected UnicodeEscaper(final int below, final int above, final boolean between) {
    super();
    this.below = below;
    this.above = above;
    this.between = between;
  }
  protected String toUtf16Escape(final int codepoint) {
    return "\\u" + hex(codepoint);
  }
  public static UnicodeEscaper above(final int codepoint) {
    return outsideOf(0, codepoint);
  }
  public static UnicodeEscaper below(final int codepoint) {
    return outsideOf(codepoint, Integer.MAX_VALUE);
  }
  public static UnicodeEscaper between(final int codepointLow, final int codepointHigh) {
    return new UnicodeEscaper(codepointLow, codepointHigh, true);
  }
  public static UnicodeEscaper outsideOf(final int codepointLow, final int codepointHigh) {
    return new UnicodeEscaper(codepointLow, codepointHigh, false);
  }
  @Override() public boolean translate(final int codepoint, final Writer out) throws IOException {
    if(between) {
      if(codepoint < below || codepoint > above) {
        return false;
      }
    }
    else {
      if(codepoint >= below && codepoint <= above) {
        return false;
      }
    }
    if(codepoint > 0xffff) {
      out.write(toUtf16Escape(codepoint));
    }
    else 
      if(codepoint > 0xfff) {
        out.write("\\u" + hex(codepoint));
      }
      else 
        if(codepoint > 0xff) {
          out.write("\\u0" + hex(codepoint));
        }
        else 
          if(codepoint > 0xf) {
            String var_495 = hex(codepoint);
            out.write("\\u00" + var_495);
          }
          else {
            out.write("\\u000" + hex(codepoint));
          }
    return true;
  }
}