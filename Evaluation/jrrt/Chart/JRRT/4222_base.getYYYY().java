package org.jfree.data.time;
import java.io.Serializable;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

abstract public class SerialDate implements Comparable, Serializable, MonthConstants  {
  final private static long serialVersionUID = -293716040467423637L;
  final public static DateFormatSymbols DATE_FORMAT_SYMBOLS = new SimpleDateFormat().getDateFormatSymbols();
  final public static int SERIAL_LOWER_BOUND = 2;
  final public static int SERIAL_UPPER_BOUND = 2958465;
  final public static int MINIMUM_YEAR_SUPPORTED = 1900;
  final public static int MAXIMUM_YEAR_SUPPORTED = 9999;
  final public static int MONDAY = Calendar.MONDAY;
  final public static int TUESDAY = Calendar.TUESDAY;
  final public static int WEDNESDAY = Calendar.WEDNESDAY;
  final public static int THURSDAY = Calendar.THURSDAY;
  final public static int FRIDAY = Calendar.FRIDAY;
  final public static int SATURDAY = Calendar.SATURDAY;
  final public static int SUNDAY = Calendar.SUNDAY;
  final static int[] LAST_DAY_OF_MONTH = { 0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 } ;
  final static int[] AGGREGATE_DAYS_TO_END_OF_MONTH = { 0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334, 365 } ;
  final static int[] AGGREGATE_DAYS_TO_END_OF_PRECEDING_MONTH = { 0, 0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334, 365 } ;
  final static int[] LEAP_YEAR_AGGREGATE_DAYS_TO_END_OF_MONTH = { 0, 31, 60, 91, 121, 152, 182, 213, 244, 274, 305, 335, 366 } ;
  final static int[] LEAP_YEAR_AGGREGATE_DAYS_TO_END_OF_PRECEDING_MONTH = { 0, 0, 31, 60, 91, 121, 152, 182, 213, 244, 274, 305, 335, 366 } ;
  final public static int FIRST_WEEK_IN_MONTH = 1;
  final public static int SECOND_WEEK_IN_MONTH = 2;
  final public static int THIRD_WEEK_IN_MONTH = 3;
  final public static int FOURTH_WEEK_IN_MONTH = 4;
  final public static int LAST_WEEK_IN_MONTH = 0;
  final public static int INCLUDE_NONE = 0;
  final public static int INCLUDE_FIRST = 1;
  final public static int INCLUDE_SECOND = 2;
  final public static int INCLUDE_BOTH = 3;
  final public static int PRECEDING = -1;
  final public static int NEAREST = 0;
  final public static int FOLLOWING = 1;
  private String description;
  protected SerialDate() {
    super();
  }
  public static SerialDate addDays(final int days, final SerialDate base) {
    final int serialDayNumber = base.toSerial() + days;
    return SerialDate.createInstance(serialDayNumber);
  }
  public static SerialDate addMonths(final int months, final SerialDate base) {
    int var_4222 = base.getYYYY();
    final int yy = (12 * var_4222 + base.getMonth() + months - 1) / 12;
    final int mm = (12 * base.getYYYY() + base.getMonth() + months - 1) % 12 + 1;
    final int dd = Math.min(base.getDayOfMonth(), SerialDate.lastDayOfMonth(mm, yy));
    return SerialDate.createInstance(dd, mm, yy);
  }
  public static SerialDate addYears(final int years, final SerialDate base) {
    final int baseY = base.getYYYY();
    final int baseM = base.getMonth();
    final int baseD = base.getDayOfMonth();
    final int targetY = baseY + years;
    final int targetD = Math.min(baseD, SerialDate.lastDayOfMonth(baseM, targetY));
    return SerialDate.createInstance(targetD, baseM, targetY);
  }
  public static SerialDate createInstance(final int serial) {
    return new SpreadsheetDate(serial);
  }
  public static SerialDate createInstance(final int day, final int month, final int yyyy) {
    return new SpreadsheetDate(day, month, yyyy);
  }
  public static SerialDate createInstance(final java.util.Date date) {
    final GregorianCalendar calendar = new GregorianCalendar();
    calendar.setTime(date);
    return new SpreadsheetDate(calendar.get(Calendar.DATE), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR));
  }
  public SerialDate getEndOfCurrentMonth(final SerialDate base) {
    final int last = SerialDate.lastDayOfMonth(base.getMonth(), base.getYYYY());
    return SerialDate.createInstance(last, base.getMonth(), base.getYYYY());
  }
  public SerialDate getFollowingDayOfWeek(final int targetDOW) {
    return getFollowingDayOfWeek(targetDOW, this);
  }
  public static SerialDate getFollowingDayOfWeek(final int targetWeekday, final SerialDate base) {
    if(!SerialDate.isValidWeekdayCode(targetWeekday)) {
      throw new IllegalArgumentException("Invalid day-of-the-week code.");
    }
    final int adjust;
    final int baseDOW = base.getDayOfWeek();
    if(baseDOW > targetWeekday) {
      adjust = 7 + Math.min(0, targetWeekday - baseDOW);
    }
    else {
      adjust = Math.max(0, targetWeekday - baseDOW);
    }
    return SerialDate.addDays(adjust, base);
  }
  public SerialDate getNearestDayOfWeek(final int targetDOW) {
    return getNearestDayOfWeek(targetDOW, this);
  }
  public static SerialDate getNearestDayOfWeek(final int targetDOW, final SerialDate base) {
    if(!SerialDate.isValidWeekdayCode(targetDOW)) {
      throw new IllegalArgumentException("Invalid day-of-the-week code.");
    }
    final int baseDOW = base.getDayOfWeek();
    int adjust = -Math.abs(targetDOW - baseDOW);
    if(adjust >= 4) {
      adjust = 7 - adjust;
    }
    if(adjust <= -4) {
      adjust = 7 + adjust;
    }
    return SerialDate.addDays(adjust, base);
  }
  public SerialDate getPreviousDayOfWeek(final int targetDOW) {
    return getPreviousDayOfWeek(targetDOW, this);
  }
  public static SerialDate getPreviousDayOfWeek(final int targetWeekday, final SerialDate base) {
    if(!SerialDate.isValidWeekdayCode(targetWeekday)) {
      throw new IllegalArgumentException("Invalid day-of-the-week code.");
    }
    final int adjust;
    final int baseDOW = base.getDayOfWeek();
    if(baseDOW > targetWeekday) {
      adjust = Math.min(0, targetWeekday - baseDOW);
    }
    else {
      adjust = -7 + Math.max(0, targetWeekday - baseDOW);
    }
    return SerialDate.addDays(adjust, base);
  }
  public String getDescription() {
    return this.description;
  }
  public static String monthCodeToString(final int month) {
    return monthCodeToString(month, false);
  }
  public static String monthCodeToString(final int month, final boolean shortened) {
    if(!isValidMonthCode(month)) {
      throw new IllegalArgumentException("SerialDate.monthCodeToString: month outside valid range.");
    }
    final String[] months;
    if(shortened) {
      months = DATE_FORMAT_SYMBOLS.getShortMonths();
    }
    else {
      months = DATE_FORMAT_SYMBOLS.getMonths();
    }
    return months[month - 1];
  }
  public static String relativeToString(final int relative) {
    switch (relative){
      case SerialDate.PRECEDING:
      return "Preceding";
      case SerialDate.NEAREST:
      return "Nearest";
      case SerialDate.FOLLOWING:
      return "Following";
      default:
      return "ERROR : Relative To String";
    }
  }
  public String toString() {
    return getDayOfMonth() + "-" + SerialDate.monthCodeToString(getMonth()) + "-" + getYYYY();
  }
  public static String weekInMonthToString(final int count) {
    switch (count){
      case SerialDate.FIRST_WEEK_IN_MONTH:
      return "First";
      case SerialDate.SECOND_WEEK_IN_MONTH:
      return "Second";
      case SerialDate.THIRD_WEEK_IN_MONTH:
      return "Third";
      case SerialDate.FOURTH_WEEK_IN_MONTH:
      return "Fourth";
      case SerialDate.LAST_WEEK_IN_MONTH:
      return "Last";
      default:
      return "SerialDate.weekInMonthToString(): invalid code.";
    }
  }
  public static String weekdayCodeToString(final int weekday) {
    final String[] weekdays = DATE_FORMAT_SYMBOLS.getWeekdays();
    return weekdays[weekday];
  }
  public static String[] getMonths() {
    return getMonths(false);
  }
  public static String[] getMonths(final boolean shortened) {
    if(shortened) {
      return DATE_FORMAT_SYMBOLS.getShortMonths();
    }
    else {
      return DATE_FORMAT_SYMBOLS.getMonths();
    }
  }
  abstract public boolean isAfter(SerialDate other);
  abstract public boolean isBefore(SerialDate other);
  abstract public boolean isInRange(SerialDate d1, SerialDate d2);
  abstract public boolean isInRange(SerialDate d1, SerialDate d2, int include);
  public static boolean isLeapYear(final int yyyy) {
    if((yyyy % 4) != 0) {
      return false;
    }
    else 
      if((yyyy % 400) == 0) {
        return true;
      }
      else 
        if((yyyy % 100) == 0) {
          return false;
        }
        else {
          return true;
        }
  }
  abstract public boolean isOn(SerialDate other);
  abstract public boolean isOnOrAfter(SerialDate other);
  abstract public boolean isOnOrBefore(SerialDate other);
  public static boolean isValidMonthCode(final int code) {
    switch (code){
      case JANUARY:
      case FEBRUARY:
      case MARCH:
      case APRIL:
      case MAY:
      case JUNE:
      case JULY:
      case AUGUST:
      case SEPTEMBER:
      case OCTOBER:
      case NOVEMBER:
      case DECEMBER:
      return true;
      default:
      return false;
    }
  }
  public static boolean isValidWeekInMonthCode(final int code) {
    switch (code){
      case FIRST_WEEK_IN_MONTH:
      case SECOND_WEEK_IN_MONTH:
      case THIRD_WEEK_IN_MONTH:
      case FOURTH_WEEK_IN_MONTH:
      case LAST_WEEK_IN_MONTH:
      return true;
      default:
      return false;
    }
  }
  public static boolean isValidWeekdayCode(final int code) {
    switch (code){
      case SUNDAY:
      case MONDAY:
      case TUESDAY:
      case WEDNESDAY:
      case THURSDAY:
      case FRIDAY:
      case SATURDAY:
      return true;
      default:
      return false;
    }
  }
  abstract public int compare(SerialDate other);
  abstract public int getDayOfMonth();
  abstract public int getDayOfWeek();
  abstract public int getMonth();
  abstract public int getYYYY();
  public static int lastDayOfMonth(final int month, final int yyyy) {
    final int result = LAST_DAY_OF_MONTH[month];
    if(month != FEBRUARY) {
      return result;
    }
    else 
      if(isLeapYear(yyyy)) {
        return result + 1;
      }
      else {
        return result;
      }
  }
  public static int leapYearCount(final int yyyy) {
    final int leap4 = (yyyy - 1896) / 4;
    final int leap100 = (yyyy - 1800) / 100;
    final int leap400 = (yyyy - 1600) / 400;
    return leap4 - leap100 + leap400;
  }
  public static int monthCodeToQuarter(final int code) {
    switch (code){
      case JANUARY:
      case FEBRUARY:
      case MARCH:
      return 1;
      case APRIL:
      case MAY:
      case JUNE:
      return 2;
      case JULY:
      case AUGUST:
      case SEPTEMBER:
      return 3;
      case OCTOBER:
      case NOVEMBER:
      case DECEMBER:
      return 4;
      default:
      throw new IllegalArgumentException("SerialDate.monthCodeToQuarter: invalid month code.");
    }
  }
  public static int stringToMonthCode(String s) {
    final String[] shortMonthNames = DATE_FORMAT_SYMBOLS.getShortMonths();
    final String[] monthNames = DATE_FORMAT_SYMBOLS.getMonths();
    int result = -1;
    s = s.trim();
    try {
      result = Integer.parseInt(s);
    }
    catch (NumberFormatException e) {
    }
    if((result < 1) || (result > 12)) {
      for(int i = 0; i < monthNames.length; i++) {
        if(s.equals(shortMonthNames[i])) {
          result = i + 1;
          break ;
        }
        if(s.equals(monthNames[i])) {
          result = i + 1;
          break ;
        }
      }
    }
    return result;
  }
  public static int stringToWeekdayCode(String s) {
    final String[] shortWeekdayNames = DATE_FORMAT_SYMBOLS.getShortWeekdays();
    final String[] weekDayNames = DATE_FORMAT_SYMBOLS.getWeekdays();
    int result = -1;
    s = s.trim();
    for(int i = 0; i < weekDayNames.length; i++) {
      if(s.equals(shortWeekdayNames[i])) {
        result = i;
        break ;
      }
      if(s.equals(weekDayNames[i])) {
        result = i;
        break ;
      }
    }
    return result;
  }
  abstract public int toSerial();
  abstract public java.util.Date toDate();
  public void setDescription(final String description) {
    this.description = description;
  }
}