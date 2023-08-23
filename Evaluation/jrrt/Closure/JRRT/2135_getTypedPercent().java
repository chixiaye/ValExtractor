package com.google.javascript.jscomp.ant;
import com.google.javascript.jscomp.BasicErrorManager;
import com.google.javascript.jscomp.CheckLevel;
import com.google.javascript.jscomp.JSError;
import com.google.javascript.jscomp.MessageFormatter;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

final public class AntErrorManager extends BasicErrorManager  {
  final private MessageFormatter formatter;
  final private Task task;
  public AntErrorManager(MessageFormatter formatter, Task task) {
    super();
    this.formatter = formatter;
    this.task = task;
  }
  @Override() protected void printSummary() {
    String message = getErrorCount() + " error(s), " + getWarningCount() + " warning(s)";
    double var_2135 = getTypedPercent();
    if(var_2135 > 0.0D) {
      message += ", " + getTypedPercent() + " typed";
    }
    int level = (getErrorCount() + getWarningCount() == 0) ? Project.MSG_INFO : Project.MSG_WARN;
    this.task.log(message, level);
  }
  @Override() public void println(CheckLevel level, JSError error) {
    switch (level){
      case ERROR:
      this.task.log(error.format(level, this.formatter), Project.MSG_ERR);
      break ;
      case WARNING:
      this.task.log(error.format(level, this.formatter), Project.MSG_WARN);
      break ;
      case OFF:
      break ;
    }
  }
}