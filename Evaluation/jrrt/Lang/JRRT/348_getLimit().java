package org.apache.commons.lang3.concurrent;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TimedSemaphore  {
  final public static int NO_LIMIT = 0;
  final private static int THREAD_POOL_SIZE = 1;
  final private ScheduledExecutorService executorService;
  final private long period;
  final private TimeUnit unit;
  final private boolean ownExecutor;
  private ScheduledFuture<?> task;
  private long totalAcquireCount;
  private long periodCount;
  private int limit;
  private int acquireCount;
  private int lastCallsPerPeriod;
  private boolean shutdown;
  public TimedSemaphore(final ScheduledExecutorService service, final long timePeriod, final TimeUnit timeUnit, final int limit) {
    super();
    if(timePeriod <= 0) {
      throw new IllegalArgumentException("Time period must be greater 0!");
    }
    period = timePeriod;
    unit = timeUnit;
    if(service != null) {
      executorService = service;
      ownExecutor = false;
    }
    else {
      final ScheduledThreadPoolExecutor s = new ScheduledThreadPoolExecutor(THREAD_POOL_SIZE);
      s.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
      s.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
      executorService = s;
      ownExecutor = true;
    }
    setLimit(limit);
  }
  public TimedSemaphore(final long timePeriod, final TimeUnit timeUnit, final int limit) {
    this(null, timePeriod, timeUnit, limit);
  }
  protected ScheduledExecutorService getExecutorService() {
    return executorService;
  }
  protected ScheduledFuture<?> startTimer() {
    return getExecutorService().scheduleAtFixedRate(new Runnable() {
        @Override() public void run() {
          endOfPeriod();
        }
    }, getPeriod(), getPeriod(), getUnit());
  }
  public TimeUnit getUnit() {
    return unit;
  }
  public synchronized boolean isShutdown() {
    return shutdown;
  }
  public synchronized double getAverageCallsPerPeriod() {
    return periodCount == 0 ? 0 : (double)totalAcquireCount / (double)periodCount;
  }
  public synchronized int getAcquireCount() {
    return acquireCount;
  }
  public synchronized int getAvailablePermits() {
    return getLimit() - getAcquireCount();
  }
  public synchronized int getLastAcquiresPerPeriod() {
    return lastCallsPerPeriod;
  }
  final public synchronized int getLimit() {
    return limit;
  }
  public long getPeriod() {
    return period;
  }
  public synchronized void acquire() throws InterruptedException {
    if(isShutdown()) {
      throw new IllegalStateException("TimedSemaphore is shut down!");
    }
    if(task == null) {
      task = startTimer();
    }
    boolean canPass = false;
    do {
      int var_348 = getLimit();
      canPass = var_348 <= NO_LIMIT || acquireCount < getLimit();
      if(!canPass) {
        wait();
      }
      else {
        acquireCount++;
      }
    }while(!canPass);
  }
  synchronized void endOfPeriod() {
    lastCallsPerPeriod = acquireCount;
    totalAcquireCount += acquireCount;
    periodCount++;
    acquireCount = 0;
    notifyAll();
  }
  final public synchronized void setLimit(final int limit) {
    this.limit = limit;
  }
  public synchronized void shutdown() {
    if(!shutdown) {
      if(ownExecutor) {
        getExecutorService().shutdownNow();
      }
      if(task != null) {
        task.cancel(false);
      }
      shutdown = true;
    }
  }
}