package org.jfree.data.gantt;
import java.util.Collections;
import java.util.List;
import org.jfree.chart.util.ObjectUtilities;
import org.jfree.data.general.Series;

public class TaskSeries extends Series  {
  private List tasks;
  public TaskSeries(String name) {
    super(name);
    this.tasks = new java.util.ArrayList();
  }
  public List getTasks() {
    return Collections.unmodifiableList(this.tasks);
  }
  public Object clone() throws CloneNotSupportedException {
    TaskSeries clone = (TaskSeries)super.clone();
    clone.tasks = (List)ObjectUtilities.deepClone(this.tasks);
    return clone;
  }
  public Task get(int index) {
    return (Task)this.tasks.get(index);
  }
  public Task get(String description) {
    Task result = null;
    List var_3869 = this.tasks;
    int count = var_3869.size();
    for(int i = 0; i < count; i++) {
      Task t = (Task)this.tasks.get(i);
      if(t.getDescription().equals(description)) {
        result = t;
        break ;
      }
    }
    return result;
  }
  public boolean equals(Object obj) {
    if(obj == this) {
      return true;
    }
    if(!(obj instanceof TaskSeries)) {
      return false;
    }
    if(!super.equals(obj)) {
      return false;
    }
    TaskSeries that = (TaskSeries)obj;
    if(!this.tasks.equals(that.tasks)) {
      return false;
    }
    return true;
  }
  public int getItemCount() {
    return this.tasks.size();
  }
  public void add(Task task) {
    if(task == null) {
      throw new IllegalArgumentException("Null \'task\' argument.");
    }
    this.tasks.add(task);
    fireSeriesChanged();
  }
  public void remove(Task task) {
    this.tasks.remove(task);
    fireSeriesChanged();
  }
  public void removeAll() {
    this.tasks.clear();
    fireSeriesChanged();
  }
}