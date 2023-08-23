package org.joda.time.tz;
import org.joda.time.DateTimeZone;

final public class FixedDateTimeZone extends DateTimeZone  {
  final private static long serialVersionUID = -3513011772763289092L;
  final private String iNameKey;
  final private int iWallOffset;
  final private int iStandardOffset;
  public FixedDateTimeZone(String id, String nameKey, int wallOffset, int standardOffset) {
    super(id);
    iNameKey = nameKey;
    iWallOffset = wallOffset;
    iStandardOffset = standardOffset;
  }
  public String getNameKey(long instant) {
    return iNameKey;
  }
  public boolean equals(Object obj) {
    if(this == obj) {
      return true;
    }
    if(obj instanceof FixedDateTimeZone) {
      FixedDateTimeZone other = (FixedDateTimeZone)obj;
      return getID().equals(other.getID()) && iStandardOffset == other.iStandardOffset && iWallOffset == other.iWallOffset;
    }
    return false;
  }
  public boolean isFixed() {
    return true;
  }
  public int getOffset(long instant) {
    return iWallOffset;
  }
  public int getOffsetFromLocal(long instantLocal) {
    return iWallOffset;
  }
  public int getStandardOffset(long instant) {
    return iStandardOffset;
  }
  public int hashCode() {
    return getID().hashCode() + 37 * iStandardOffset + 31 * iWallOffset;
  }
  public java.util.TimeZone toTimeZone() {
    String var_714 = getID();
    String id = var_714;
    if(id.length() == 6 && (id.startsWith("+") || id.startsWith("-"))) {
      return java.util.TimeZone.getTimeZone("GMT" + getID());
    }
    return new java.util.SimpleTimeZone(iWallOffset, getID());
  }
  public long nextTransition(long instant) {
    return instant;
  }
  public long previousTransition(long instant) {
    return instant;
  }
}