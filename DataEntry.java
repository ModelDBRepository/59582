/* DataEntry.java

General purpose data entry class
  - updated for Java 2

BPG 23-9-99
*/

import java.awt.*;
import java.awt.event.*;


// Data entry class
public class DataEntry extends Dialog {

  Dialog thisDE;
  Button acc_butt, def_butt, can_butt;
  String[][] linfo;  // local info
  int lnEntries;  // local nEntries
  TextField[] data;  // data entry points 


  // Constructor for data entry
  public DataEntry(Frame parent, String title, String[][] info, int nEntries) {

    // Create data entry window
    super(parent, title, true);
    thisDE = this;  // for use by event handlers

    // Local references
    linfo = info;
    lnEntries = nEntries;

    // Layout manager
    this.setLayout(new BorderLayout(15, 15));

    // Panel of data entry fields
    int i;
    data = new TextField[nEntries];
    Panel pd = new Panel();
    pd.setLayout(new GridLayout(nEntries, 2, 1, 5));
    for (i = 0; i < nEntries; i++)  {
      pd.add(new Label(info[i][0]));
      pd.add(data[i] = new TextField(info[i][2], 3));
    };
    this.add("Center", pd);  // put data entry in middle

    // Panel of control buttons
    Panel pb = new Panel();
    pb.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 5));
    pb.add(acc_butt = new Button("Accept"));
    pb.add(def_butt = new Button("Default"));
    pb.add(can_butt = new Button("Cancel"));
    this.add("South", pb);  // put buttons at bottom of data entry

    // Actions for each button
    acc_butt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        for (int j = 0; j < lnEntries; j++)
          linfo[j][2] = data[j].getText();
        thisDE.hide();
        thisDE.dispose();
      }
    });
    def_butt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        for (int j = 0; j < lnEntries; j++)
          data[j].setText(linfo[j][1]);
      }
    });
    can_butt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        thisDE.hide();
        thisDE.dispose();
      }
    });

    // Set window to preferred size
    this.pack();

  }

}
