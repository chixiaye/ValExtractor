package com.google.javascript.jscomp;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

final class Tracer  {
  final static Logger logger = Logger.getLogger(Tracer.class.getName());
  private static volatile boolean defaultPrettyPrint;
  private static List<TracingStatistic> extraTracingStatistics = new CopyOnWriteArrayList<TracingStatistic>();
  private long[] extraTracingValues;
  @Nullable() final private String type;
  final private String comment;
  final private long startTimeMs;
  private long stopTimeMs;
  final Thread startThread;
  final static int MAX_TRACE_SIZE = 1000;
  static InternalClock clock = new InternalClock() {
      @Override() public long currentTimeMillis() {
        return System.currentTimeMillis();
      }
  };
  @Nullable() private static AtomicTracerStatMap typeToCountMap;
  @Nullable() private static AtomicTracerStatMap typeToSilentMap;
  @Nullable() private static AtomicTracerStatMap typeToTimeMap;
  final private static Stat ZERO_STAT = new Stat();
  private static ThreadLocal<ThreadTrace> traces = new ThreadLocal<ThreadTrace>();
  Tracer(@Nullable() String type, @Nullable() String comment) {
    super();
    this.type = type;
    this.comment = comment == null ? "" : comment;
    startTimeMs = clock.currentTimeMillis();
    startThread = Thread.currentThread();
    if(!extraTracingStatistics.isEmpty()) {
      int size = extraTracingStatistics.size();
      extraTracingValues = new long[size];
      int i = 0;
      for (TracingStatistic tracingStatistic : extraTracingStatistics) {
        extraTracingValues[i] = tracingStatistic.start(startThread);
        i++;
      }
    }
    ThreadTrace trace = getThreadTrace();
    if(!trace.isInitialized()) {
      return ;
    }
    if(trace.events.size() >= MAX_TRACE_SIZE) {
      logger.log(Level.WARNING, "Giant thread trace. Too many Tracers created. " + "Clearing to avoid memory leak.", new Throwable(trace.toString()));
      trace.truncateEvents();
    }
    if(trace.outstandingEvents.size() >= MAX_TRACE_SIZE) {
      logger.log(Level.WARNING, "Too many outstanding Tracers. Tracer.stop() is missing " + "or Tracer.stop() is not wrapped in a " + "try/finally block. " + "Clearing to avoid memory leak.", new Throwable(trace.toString()));
      trace.truncateOutstandingEvents();
    }
    trace.startEvent(this);
  }
  Tracer(String comment) {
    this(null, comment);
  }
  @Nullable() static Map<String, Long> getTypeToCountMap() {
    return typeToCountMap != null ? typeToCountMap.getMap() : null;
  }
  @Nullable() static Map<String, Long> getTypeToSilentMap() {
    return typeToSilentMap != null ? typeToSilentMap.getMap() : null;
  }
  @Nullable() static Map<String, Long> getTypeToTimeMap() {
    return typeToTimeMap != null ? typeToTimeMap.getMap() : null;
  }
  static Stat getStatsForType(String type) {
    Stat stat = getThreadTrace().stats.get(type);
    return stat != null ? stat : ZERO_STAT;
  }
  private static String formatTime(long time) {
    int sec = (int)((time / 1000) % 60);
    int ms = (int)(time % 1000);
    return String.format("%02d.%03d", sec, ms);
  }
  static String getCurrentThreadTraceReport() {
    return getThreadTrace().toString();
  }
  private static String longToPaddedString(long v, int digits_column_width) {
    int digit_width = numDigits(v);
    StringBuilder sb = new StringBuilder();
    appendSpaces(sb, digits_column_width - digit_width);
    sb.append(v);
    return sb.toString();
  }
  @Override() public String toString() {
    if(type == null) {
      return comment;
    }
    else {
      return "[" + type + "] " + comment;
    }
  }
  static ThreadTrace getThreadTrace() {
    ThreadTrace t = traces.get();
    if(t == null) {
      t = new ThreadTrace();
      t.prettyPrint = defaultPrettyPrint;
      traces.set(t);
    }
    return t;
  }
  static Tracer shortName(Object object, String comment) {
    if(object == null) {
      return new Tracer(comment);
    }
    return new Tracer(object.getClass().getSimpleName(), comment);
  }
  static int addTracingStatistic(TracingStatistic tracingStatistic) {
    if(tracingStatistic.enable()) {
      extraTracingStatistics.add(tracingStatistic);
      return extraTracingStatistics.lastIndexOf(tracingStatistic);
    }
    else {
      return -1;
    }
  }
  private static int numDigits(long n) {
    int i = 0;
    do {
      i++;
      n = n / 10;
    }while(n > 0);
    return i;
  }
  long stop() {
    return stop(-1);
  }
  long stop(int silence_threshold) {
    Preconditions.checkState(Thread.currentThread() == startThread);
    ThreadTrace trace = getThreadTrace();
    if(!trace.isInitialized()) {
      return 0;
    }
    stopTimeMs = clock.currentTimeMillis();
    if(extraTracingValues != null) {
      for(int i = 0; i < extraTracingValues.length; i++) {
        long value = extraTracingStatistics.get(i).stop(startThread);
        extraTracingValues[i] = value - extraTracingValues[i];
      }
    }
    if(!trace.isInitialized()) {
      return 0;
    }
    trace.endEvent(this, silence_threshold);
    return stopTimeMs - startTimeMs;
  }
  @VisibleForTesting() static void appendSpaces(StringBuilder sb, int numSpaces) {
    if(numSpaces > 16) {
      logger.warning("Tracer.appendSpaces called with large numSpaces");
      numSpaces = 16;
    }
    while(numSpaces >= 5){
      sb.append("     ");
      numSpaces -= 5;
    }
    switch (numSpaces){
      case 1:
      sb.append(" ");
      break ;
      case 2:
      sb.append("  ");
      break ;
      case 3:
      sb.append("   ");
      break ;
      case 4:
      sb.append("    ");
      break ;
    }
  }
  static void clearCurrentThreadTrace() {
    clearThreadTrace();
  }
  static void clearThreadTrace() {
    traces.remove();
  }
  @VisibleForTesting() static void clearTracingStatisticsTestingOnly() {
    extraTracingStatistics.clear();
  }
  static synchronized void enableTypeMaps() {
    if(typeToCountMap == null) {
      typeToCountMap = new AtomicTracerStatMap();
      typeToSilentMap = new AtomicTracerStatMap();
      typeToTimeMap = new AtomicTracerStatMap();
    }
  }
  static void initCurrentThreadTrace() {
    ThreadTrace events = getThreadTrace();
    if(!events.isEmpty()) {
      logger.log(Level.WARNING, "Non-empty timer log:\n" + events, new Throwable());
      clearThreadTrace();
      events = getThreadTrace();
    }
    events.init();
  }
  static void initCurrentThreadTrace(int default_silence_threshold) {
    initCurrentThreadTrace();
    setDefaultSilenceThreshold(default_silence_threshold);
  }
  static void logAndClearCurrentThreadTrace() {
    logCurrentThreadTrace();
    clearThreadTrace();
  }
  static void logCurrentThreadTrace() {
    ThreadTrace trace = getThreadTrace();
    if(!trace.isInitialized()) {
      logger.log(Level.WARNING, "Tracer log requested for this thread but was not " + "initialized using Tracer.initCurrentThreadTrace().", new Throwable());
      return ;
    }
    if(!trace.isEmpty()) {
      logger.log(Level.WARNING, "timers:\n{0}", getCurrentThreadTraceReport());
    }
  }
  static void setDefaultSilenceThreshold(int threshold) {
    getThreadTrace().defaultSilenceThreshold = threshold;
  }
  static void setPrettyPrint(boolean enabled) {
    defaultPrettyPrint = enabled;
  }
  
  final static class AtomicTracerStatMap  {
    private ConcurrentMap<String, Long> map = new ConcurrentHashMap<String, Long>();
    Map<String, Long> getMap() {
      return map;
    }
    @SuppressWarnings(value = {"nullness", }) void incrementBy(String key, long delta) {
      Long oldValue = map.get(key);
      if(oldValue == null) {
        oldValue = map.putIfAbsent(key, delta);
        if(oldValue == null) {
          return ;
        }
        else {
        }
      }
      while(true){
        if(map.replace(key, oldValue, oldValue + delta)) {
          break ;
        }
        oldValue = map.get(key);
      }
    }
  }
  
  final private static class Event  {
    boolean isStart;
    Tracer tracer;
    Event(boolean start, Tracer t) {
      super();
      isStart = start;
      tracer = t;
    }
    String toString(long prevEventTime, String indent, int digitsColWidth) {
      StringBuilder sb = new StringBuilder(120);
      if(prevEventTime == -1) {
        appendSpaces(sb, digitsColWidth);
      }
      else {
        sb.append(longToPaddedString(eventTime() - prevEventTime, digitsColWidth));
      }
      sb.append(' ');
      sb.append(formatTime(eventTime()));
      if(isStart) {
        sb.append(" Start ");
        appendSpaces(sb, digitsColWidth);
        sb.append("   ");
      }
      else {
        sb.append(" Done ");
        long delta = tracer.stopTimeMs - tracer.startTimeMs;
        sb.append(longToPaddedString(delta, digitsColWidth));
        sb.append(" ms ");
        if(tracer.extraTracingValues != null) {
          for(int i = 0; i < tracer.extraTracingValues.length; i++) {
            delta = tracer.extraTracingValues[i];
            sb.append(String.format("%4d", delta));
            sb.append(extraTracingStatistics.get(i).getUnits());
            sb.append(";  ");
          }
        }
      }
      sb.append(indent);
      sb.append(tracer.toString());
      return sb.toString();
    }
    long eventTime() {
      return isStart ? tracer.startTimeMs : tracer.stopTimeMs;
    }
  }
  
  static interface InternalClock  {
    long currentTimeMillis();
  }
  
  final static class Stat  {
    private int count;
    private int silent;
    private int clockTime;
    private int[] extraInfo;
    int getCount() {
      return count;
    }
    @VisibleForTesting() int getExtraInfo(int index) {
      return index >= extraInfo.length ? 0 : extraInfo[index];
    }
    int getSilentCount() {
      return silent;
    }
    int getTotalTime() {
      return clockTime;
    }
  }
  
  final static class ThreadTrace  {
    int defaultSilenceThreshold;
    final ArrayList<Event> events = new ArrayList<Event>();
    final HashSet<Tracer> outstandingEvents = new HashSet<Tracer>();
    final Map<String, Stat> stats = new HashMap<String, Stat>();
    boolean isOutstandingEventsTruncated = false;
    boolean isEventsTruncated = false;
    boolean isInitialized = false;
    boolean prettyPrint = false;
    @SuppressWarnings(value = {"nullness", }) @Override() public String toString() {
      int numDigits = getMaxDigits();
      StringBuilder sb = new StringBuilder();
      long etime = -1;
      LinkedList<String> indent = prettyPrint ? new LinkedList<String>() : null;
      for (Event e : events) {
        if(prettyPrint && !e.isStart && !indent.isEmpty()) {
          indent.pop();
        }
        sb.append(" ");
        if(prettyPrint) {
          sb.append(e.toString(etime, Joiner.on("").join(indent), numDigits));
        }
        else {
          sb.append(e.toString(etime, "", 4));
        }
        etime = e.eventTime();
        sb.append('\n');
        if(prettyPrint && e.isStart) {
          indent.push("|  ");
        }
      }
      if(outstandingEvents.size() != 0) {
        long now = clock.currentTimeMillis();
        sb.append(" Unstopped timers:\n");
        for (Tracer t : outstandingEvents) {
          sb.append("  ").append(t).append(" (").append(now - t.startTimeMs).append(" ms, started at ").append(formatTime(t.startTimeMs)).append(")\n");
        }
      }
      for (String key : stats.keySet()) {
        Stat stat = stats.get(key);
        if(stat.count > 1) {
          sb.append(" TOTAL ").append(key).append(" ").append(stat.count).append(" (").append(stat.clockTime).append(" ms");
          if(stat.extraInfo != null) {
            for(int i = 0; i < stat.extraInfo.length; i++) {
              sb.append("; ");
              sb.append(stat.extraInfo[i]).append(' ').append(extraTracingStatistics.get(i).getUnits());
            }
          }
          sb.append(")\n");
        }
      }
      return sb.toString();
    }
    boolean isEmpty() {
      return events.size() == 0 && outstandingEvents.size() == 0;
    }
    boolean isInitialized() {
      return isInitialized;
    }
    private int getMaxDigits() {
      long etime = -1;
      long max_time = 0;
      for (Event e : events) {
        if(etime != -1) {
          long time = e.eventTime() - etime;
          max_time = Math.max(max_time, time);
        }
        if(!e.isStart) {
          Tracer var_1950 = e.tracer;
          long time = var_1950.stopTimeMs - e.tracer.startTimeMs;
          max_time = Math.max(max_time, time);
        }
        etime = e.eventTime();
      }
      return Math.max(3, numDigits(max_time));
    }
    void endEvent(Tracer t, int silenceThreshold) {
      boolean wasOutstanding = outstandingEvents.remove(t);
      if(!wasOutstanding) {
        if(isOutstandingEventsTruncated) {
          logger.log(Level.WARNING, "event not found, probably because the event stack " + "overflowed and was truncated", new Throwable());
        }
        else {
          throw new IllegalStateException();
        }
      }
      long elapsed = t.stopTimeMs - t.startTimeMs;
      if(silenceThreshold == -1) {
        silenceThreshold = defaultSilenceThreshold;
      }
      if(elapsed < silenceThreshold) {
        boolean removed = false;
        for(int i = 0; i < events.size(); i++) {
          Event e = events.get(i);
          if(e.tracer == t) {
            Preconditions.checkState(e.isStart);
            events.remove(i);
            removed = true;
            break ;
          }
        }
        Preconditions.checkState(removed || isEventsTruncated);
      }
      else {
        events.add(new Event(false, t));
      }
      if(t.type != null) {
        Stat stat = stats.get(t.type);
        if(stat == null) {
          stat = new Stat();
          if(!extraTracingStatistics.isEmpty()) {
            stat.extraInfo = new int[extraTracingStatistics.size()];
          }
          stats.put(t.type, stat);
        }
        stat.count++;
        if(typeToCountMap != null) {
          typeToCountMap.incrementBy(t.type, 1);
        }
        stat.clockTime += elapsed;
        if(typeToTimeMap != null) {
          typeToTimeMap.incrementBy(t.type, elapsed);
        }
        if(stat.extraInfo != null && t.extraTracingValues != null) {
          int overlapLength = Math.min(stat.extraInfo.length, t.extraTracingValues.length);
          for(int i = 0; i < overlapLength; i++) {
            stat.extraInfo[i] += t.extraTracingValues[i];
            AtomicTracerStatMap map = extraTracingStatistics.get(i).getTracingStat();
            if(map != null) {
              map.incrementBy(t.type, t.extraTracingValues[i]);
            }
          }
        }
        if(elapsed < silenceThreshold) {
          stat.silent++;
          if(typeToSilentMap != null) {
            typeToSilentMap.incrementBy(t.type, 1);
          }
        }
      }
    }
    void init() {
      isInitialized = true;
    }
    void startEvent(Tracer t) {
      events.add(new Event(true, t));
      boolean notAlreadyOutstanding = outstandingEvents.add(t);
      Preconditions.checkState(notAlreadyOutstanding);
    }
    void truncateEvents() {
      isEventsTruncated = true;
      events.clear();
    }
    void truncateOutstandingEvents() {
      isOutstandingEventsTruncated = true;
      outstandingEvents.clear();
    }
  }
  
  static interface TracingStatistic  {
    AtomicTracerStatMap getTracingStat();
    String getUnits();
    boolean enable();
    long start(Thread thread);
    long stop(Thread thread);
  }
}