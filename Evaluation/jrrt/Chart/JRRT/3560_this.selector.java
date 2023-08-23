package org.jfree.chart.ui;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComboBox;
import javax.swing.JPanel;

public class StrokeChooserPanel extends JPanel  {
  private JComboBox selector;
  public StrokeChooserPanel(StrokeSample current, StrokeSample[] available) {
    super();
    setLayout(new BorderLayout());
    this.selector = new JComboBox(available);
    JComboBox var_3560 = this.selector;
    var_3560.setSelectedItem(current);
    this.selector.setRenderer(new StrokeSample(new BasicStroke(1)));
    add(this.selector);
    this.selector.addActionListener(new ActionListener() {
        public void actionPerformed(final ActionEvent evt) {
          getSelector().transferFocus();
        }
    });
  }
  final protected JComboBox getSelector() {
    return this.selector;
  }
  public Stroke getSelectedStroke() {
    StrokeSample sample = (StrokeSample)this.selector.getSelectedItem();
    return sample.getStroke();
  }
}