/* TreeDisplay.java

Window (frame) for graphic display of tree.

Version 1.0 30-3-01 BPG
*/

import java.awt.*;
import java.awt.event.*;


public class TreeDisplay extends Frame {

  // Private instance variables
  public DisplayCanvas dc;
  private Graphics g;
  String GRAPHIC_data[][] = {
    {"Display width:", "600", "600"},
    {"Display height:", "350", "350"},
    {"Display parameter:", "Cbr", "Cbr"},
    {"Maximum display value:", "1.0", "1.0"},
    {"Display interval (steps):", "1", "1"},
    {"Display delay (msecs):", "0", "0"},
    {"Maximum path length (um):", "100", "100"},
    {"Initial branch angle (deg):", "20", "20"},
    {"Angle scaling factor:", "-0.4", "-0.4"},
    {"Soma scaling factor:", "1.0", "1.0"},
    {"Dendrite scaling factor:", "5.0", "5.0"}};
  private final int Ngraphic = 11;
  private int dispw=600;  // display width
  private int disph=350;  // display height

  // Public instance variables
  public boolean refresh=true;  // refresh display
  public String dtitle;
  public String dname="";  // parameter to plot
  public float dmval=1.0f;  // maximum plot value
  public int dtdisp=100;  // time interval


  // Constructor
  public TreeDisplay(String title, int width, int height, String dn, float mval, int dt) {
    // create frame
    super(title);

    // set parameter values
    dtitle = title;  // display title
    dispw = width;  // display width
    disph = height;  // display height
    dname = dn;  // variable name
    dmval = mval;  // maximum data value
    dtdisp = dt;  // refresh time interval

    // save parameter values in menu
    GRAPHIC_data[0][2] = String.valueOf(dispw);
    GRAPHIC_data[1][2] = String.valueOf(disph);
    GRAPHIC_data[2][2] = dname;
    GRAPHIC_data[3][2] = String.valueOf(dmval);
    GRAPHIC_data[4][2] = String.valueOf(dtdisp);

    // add menu bar
    MenuItem item;
    MenuBar mb = new MenuBar();
    Menu wind = new Menu("Window", true);
    mb.add(wind);
    wind.add(item = new MenuItem("Close"));
    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        dispose();
      }
    });
    wind.add(item = new MenuItem("Refresh"));
    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        repaint();
      }
    });
    Menu control = new Menu("Control", true);
    mb.add(control);
    control.add(item = new MenuItem("Params..."));
    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        dispparams();
      }
    });
    this.setMenuBar(mb);

    // add canvas for drawing in
    dc = new DisplayCanvas(width, height);
    this.add("Center", dc);

    // allow closing
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent event) {
        dispose();
      }
    });

    // set frame size and display
    this.setResizable(true);
    this.pack();
    this.show();

    g = this.getGraphics();
     
    // draw display;
    refresh = true;
    paint(g);

  }


  public void update(Graphics g) {
    paint(g);
  }


  // Method for displaying trees
  public void paint(Graphics g)  {

    // check for resizing
    Dimension currd = dc.getSize();
    if (currd.width != dispw || currd.height != disph) {
      dispw = currd.width;
      disph = currd.height;
      this.pack();
      refresh = true;
    };

    if (refresh) {
      // might need drawing code from Neurite here
    };

    dc.repaint();

  }



  // Window for user-setting of display parameters
  private void dispparams() {
    DataEntry d = new DataEntry(new Frame(), "Display Params", GRAPHIC_data, Ngraphic);
    d.show();
    dispw = Integer.parseInt(GRAPHIC_data[0][2]);
    disph = Integer.parseInt(GRAPHIC_data[1][2]);
    dname = GRAPHIC_data[2][2];
    this.setTitle(dname);
    dmval = Float.valueOf(GRAPHIC_data[3][2]).floatValue();
    dtdisp = Integer.parseInt(GRAPHIC_data[4][2]);
  }


}



// Canvas to display tree in
class DisplayCanvas extends Canvas {

  public Image imageTree;
  private int dcw, dch;

  public DisplayCanvas(int width, int height) {
    super();
    dcw = width;
    dch = height;
    this.setSize(dcw, dch);
    imageTree = this.createImage(dcw,dch);
  }


  public void update(Graphics g) {
    paint(g);
  }


  public void paint(Graphics g) {
    if (imageTree != null)
      g.drawImage(imageTree,10,10,this);
    this.getToolkit().sync();  // draw now
  }
    
}
