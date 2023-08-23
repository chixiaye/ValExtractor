package com.google.javascript.jscomp;
import com.google.common.base.CaseFormat;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.javascript.jscomp.JsMessage.IdGenerator;
import com.google.javascript.jscomp.JsMessage.PlaceholderReference;
import java.util.List;

public class GoogleJsMessageIdGenerator implements IdGenerator  {
  final private String projectId;
  public GoogleJsMessageIdGenerator(String projectId) {
    super();
    this.projectId = projectId;
  }
  @Override() public String generateId(String meaning, List<CharSequence> messageParts) {
    Preconditions.checkState(meaning != null);
    StringBuilder sb = new StringBuilder();
    for (CharSequence part : messageParts) {
      if(part instanceof PlaceholderReference) {
        sb.append(CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, ((PlaceholderReference)part).getName()));
      }
      else {
        sb.append(part);
      }
    }
    String tcValue = sb.toString();
    String projectScopedMeaning = (projectId != null ? (projectId + ": ") : "") + meaning;
    return String.valueOf(MessageId.GenerateId(tcValue, projectScopedMeaning));
  }
  
  final private static class FP  {
    private FP() {
      super();
    }
    @SuppressWarnings(value = {"fallthrough", }) private static int hash32(byte[] str, int start, int limit, int c) {
      int a = 0x9e3779b9;
      int b = 0x9e3779b9;
      int i;
      for(i = start; i + 12 <= limit; i += 12) {
        a += (((str[i + 0] & 0xff) << 0) | ((str[i + 1] & 0xff) << 8) | ((str[i + 2] & 0xff) << 16) | ((str[i + 3] & 0xff) << 24));
        b += (((str[i + 4] & 0xff) << 0) | ((str[i + 5] & 0xff) << 8) | ((str[i + 6] & 0xff) << 16) | ((str[i + 7] & 0xff) << 24));
        byte var_1688 = str[i + 10];
        c += (((str[i + 8] & 0xff) << 0) | ((str[i + 9] & 0xff) << 8) | ((var_1688 & 0xff) << 16) | ((str[i + 11] & 0xff) << 24));
        a -= b;
        a -= c;
        a ^= (c >>> 13);
        b -= c;
        b -= a;
        b ^= (a << 8);
        c -= a;
        c -= b;
        c ^= (b >>> 13);
        a -= b;
        a -= c;
        a ^= (c >>> 12);
        b -= c;
        b -= a;
        b ^= (a << 16);
        c -= a;
        c -= b;
        c ^= (b >>> 5);
        a -= b;
        a -= c;
        a ^= (c >>> 3);
        b -= c;
        b -= a;
        b ^= (a << 10);
        c -= a;
        c -= b;
        c ^= (b >>> 15);
      }
      c += limit - start;
      switch (limit - i){
        case 11:
        c += (str[i + 10] & 0xff) << 24;
        case 10:
        c += (str[i + 9] & 0xff) << 16;
        case 9:
        c += (str[i + 8] & 0xff) << 8;
        case 8:
        b += (str[i + 7] & 0xff) << 24;
        case 7:
        b += (str[i + 6] & 0xff) << 16;
        case 6:
        b += (str[i + 5] & 0xff) << 8;
        case 5:
        b += (str[i + 4] & 0xff);
        case 4:
        a += (str[i + 3] & 0xff) << 24;
        case 3:
        a += (str[i + 2] & 0xff) << 16;
        case 2:
        a += (str[i + 1] & 0xff) << 8;
        case 1:
        a += (str[i + 0] & 0xff);
      }
      a -= b;
      a -= c;
      a ^= (c >>> 13);
      b -= c;
      b -= a;
      b ^= (a << 8);
      c -= a;
      c -= b;
      c ^= (b >>> 13);
      a -= b;
      a -= c;
      a ^= (c >>> 12);
      b -= c;
      b -= a;
      b ^= (a << 16);
      c -= a;
      c -= b;
      c ^= (b >>> 5);
      a -= b;
      a -= c;
      a ^= (c >>> 3);
      b -= c;
      b -= a;
      b ^= (a << 10);
      c -= a;
      c -= b;
      c ^= (b >>> 15);
      return c;
    }
    private static long fingerprint(byte[] str, int start, int limit) {
      int hi = hash32(str, start, limit, 0);
      int lo = hash32(str, start, limit, 102072);
      if((hi == 0) && (lo == 0 || lo == 1)) {
        hi ^= 0x130f9bef;
        lo ^= 0x94a0a928;
      }
      return (((long)hi) << 32) | (lo & 0xffffffffL);
    }
    private static long fingerprint(String str) {
      byte[] tmp = str.getBytes(Charsets.UTF_8);
      return FP.fingerprint(tmp, 0, tmp.length);
    }
  }
  
  private static class MessageId  {
    final private static long GenerateId(String message, String meaning) {
      long fp = FP.fingerprint(message);
      if(null != meaning && meaning.length() > 0) {
        long fp2 = FP.fingerprint(meaning);
        fp = fp2 + (fp << 1) + (fp < 0 ? 1 : 0);
      }
      return fp & 0x7fffffffffffffffL;
    }
  }
}