package com.google.javascript.jscomp;
import com.google.javascript.jscomp.CheckLevel;
import java.util.logging.Logger;
import java.util.logging.Level;

public class LoggerErrorManager extends BasicErrorManager  {
  final private MessageFormatter formatter;
  final private Logger logger;
  public LoggerErrorManager(Logger logger) {
    this(ErrorFormat.SOURCELESS.toFormatter(null, false), logger);
  }
  public LoggerErrorManager(MessageFormatter formatter, Logger logger) {
    super();
    this.formatter = formatter;
    this.logger = logger;
  }
  @Override() protected void printSummary() {
    Level level = (getErrorCount() + getWarningCount() == 0) ? Level.INFO : Level.WARNING;
    double var_1186 = getTypedPercent();
    if(var_1186 > 0.0D) {
      logger.log(level, "{0} error(s), {1} warning(s), {2,number,#.#}% typed", new Object[]{ getErrorCount(), getWarningCount(), getTypedPercent() } );
    }
    else {
      if(getErrorCount() + getWarningCount() > 0) {
        logger.log(level, "{0} error(s), {1} warning(s)", new Object[]{ getErrorCount(), getWarningCount() } );
      }
    }
  }
  @Override() public void println(CheckLevel level, JSError error) {
    switch (level){
      case ERROR:
      logger.severe(error.format(level, formatter));
      break ;
      case WARNING:
      logger.warning(error.format(level, formatter));
      break ;
      case OFF:
      break ;
    }
  }
}