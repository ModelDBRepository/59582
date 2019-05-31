/* TermPlot.java

Window (frame) for graph plots of terminal values.

Version 1.1 BPG 4-3-03 time parameters changed to doubles
Version 1.0 15-3-00 BPG
*/

import java.awt.*;
import java.awt.event.*;
import java.io.*;


public class TermPlot extends Frame {

  // Private instance variables
  public PlotCanvas gp;
  private Graphics g;
  private String plot_data[][] = {
    {"Plot width:", "300", "300"},
    {"Plot height:", "150", "150"},
    {"Plot parameter:", "Cbr", "Cbr"},
    {"Maximum plot value:", "1.0", "1.0"},
    {"Plot interval (steps):", "10", "10"},
    {"Include soma (1=yes):", "0", "0"},
    {"Save plot values (1=yes):", "0", "0"},
    {"Save file stem:", "Br1", "Br1"}};
  private final int Nplotd = 8;
  private Color collist[] = {Color.black, Color.blue, Color.red,
    Color.cyan, Color.green, Color.darkGray, Color.yellow,
    Color.magenta, Color.gray, Color.orange, Color.pink,
    Color.lightGray};
  private int dispw=300;  // display width
  private int disph=150;  // display height
  private int dispoffx=10;  // x offset
  private int dispoffy=10;  // y offset
  private static final int maxfP=20;
  private FileOutputStream[] fP;
  private PrintWriter[] pP;
  private int nTerms=0;  // number of terminals
  private float[] termV;	// values in terminal nodes

  // Public instance variables
  public boolean refresh=true;  // refresh display
  public String ptitle;
  public String pname="";  // parameter to plot
  public float pmval=0.1f;  // maximum plot value
  public int dtplot=1;  // time interval
  public int somafl=0;  // display soma value
  public int sPflag=0;  // store plot data flag
  public String fPstem="test";


  // Constructor
  public TermPlot(String title, int width, int height, String pn, float mval, int dt, int sfl, int sflag, String fstem) {
    // create frame
    super(title);

    // set parameter values
    ptitle = title;  // plot title
    dispw = width;  // plot width
    disph = height;  // plot height
    pname = pn;  // variable name
    pmval = mval;  // maximum data value
    dtplot = dt;  // refresh time interval
    somafl = sfl;  // include soma or not (1 or 0)
    sPflag = sflag;  // to store or not to store (1 or 0)
    fPstem = fstem;  // file stem for storage

    // save parameter values in menu
    plot_data[0][2] = String.valueOf(dispw);
    plot_data[1][2] = String.valueOf(disph);
    plot_data[2][2] = pname;
    plot_data[3][2] = String.valueOf(pmval);
    plot_data[4][2] = String.valueOf(dtplot);
    plot_data[5][2] = String.valueOf(somafl);
    plot_data[6][2] = String.valueOf(sPflag);
    plot_data[7][2] = fPstem;

    fP = new FileOutputStream[maxfP+1];
    pP = new PrintWriter[maxfP+1];

    // add menu bar
    MenuItem item;
    MenuBar mb = new MenuBar();
    Menu wind = new Menu("Window", true);
    mb.add(wind);
    wind.add(item = new MenuItem("Close"));
    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        Neurite.nplots--;
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
        plotparams();
      }
    });
    this.setMenuBar(mb);

    // add canvas for drawing in
    gp = new PlotCanvas(width, height);
    this.add("Center", gp);

    // allow closing
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent event) {
        Neurite.nplots--;
        dispose();
      }
    });

    // set frame size and display
    this.setResizable(true);
    this.pack();
    this.show();

    g = this.getGraphics();
     
    // draw graph;
    refresh = true;
    paint(g);

  }


  public void update(Graphics g) {
    paint(g);
  }


  // Method for drawing graphs
  public void paint(Graphics g)  {

    // check for resizing
    Dimension currd = gp.getSize();
    if (currd.width != dispw || currd.height != disph) {
      dispw = currd.width;
      disph = currd.height;
      this.pack();
      refresh = true;
    };

    if (refresh) {
      // offscreen image for plot
      gp.imagePlot = this.createImage(dispw, disph);
      Graphics2D g2 = (Graphics2D) gp.imagePlot.getGraphics();
      // axes for plotting
      BasicStroke plotStroke = new BasicStroke(3.0f);
      g2.setStroke(plotStroke);
      g2.setColor(Color.blue);
      g2.drawLine(dispoffx,disph-dispoffy,dispw-dispoffx,disph-dispoffy);
      g2.drawLine(dispoffx,dispoffy,dispoffx,disph-dispoffy);
      refresh = false;
    };

    gp.repaint();

  }



  // Window for user-setting of plot parameters
  private void plotparams() {
    DataEntry d = new DataEntry(new Frame(), "Plot Params", plot_data, Nplotd);
    d.show();
    dispw = Integer.parseInt(plot_data[0][2]);
    disph = Integer.parseInt(plot_data[1][2]);
    pname = plot_data[2][2];
    this.setTitle(pname);
    pmval = Float.valueOf(plot_data[3][2]).floatValue();
    dtplot = Integer.parseInt(plot_data[4][2]);
    somafl = Integer.parseInt(plot_data[5][2]);
    sPflag = Integer.parseInt(plot_data[6][2]);
    fPstem = plot_data[7][2];
  }



  // Plot terminal values
  public void plotTerms(Graphics g, Tree br, double t, double Tstop, String pname, float maxv) {
    int x, y, starti=1;

    // get data
    termV = new float[Tree.brkey];
    for (int i=0; i < termV.length; i++) termV[i]=0;
    br.termValues(termV, pname);

    // plot data 
    x = dispoffx + (int)((dispw-(2*dispoffx))*t/Tstop);
    if (somafl == 1) starti = 0;  // include soma
    for (int i=starti; i < Tree.brkey; i++) {
      if (termV[i] > 0) {
        y = disph - dispoffy - (int)((disph-(2*dispoffy))*termV[i]/maxv);
        g.setColor(collist[i%collist.length]);
        g.fillRect(x-1, y-1, 2, 2);
      };
    };

  }



  // Save terminal values
  public void saveTerms(Tree br, double t, String pname) {

    try {
      // open new files, if necessary
      if (Tree.brkey > nTerms && Tree.brkey <= maxfP) {
        for (int i = nTerms; i < Tree.brkey; i++) {
          fP[i] = new FileOutputStream(fPstem+"_"+i+"_"+pname+".dat");
          pP[i] = new PrintWriter(fP[i]);
        };
        nTerms = Tree.brkey;  // includes soma (key=0)
      };

      // get data
      termV = new float[Tree.brkey];
      for (int i=0; i < termV.length; i++) termV[i]=0;
      br.termValues(termV, pname);

      // save data
      for (int i=0; i < Tree.brkey && i < maxfP; i++) {  // incl. soma
        pP[i].print(t);
        pP[i].print(" ");
        pP[i].println(termV[i]);
      };
    }
    catch(IOException e){
      System.out.println("I/O error has occurred");
      System.exit(0);
    };


  }




  // Flush terminal value files
  public void flushTerms() {

    int i = 0;
    while (pP[i] != null) {
      pP[i].flush();
      pP[i].close();
      i++;
    };
    nTerms = 0;

  }


}



// Canvas to do plotting in
class PlotCanvas extends Canvas {

  public Image imagePlot;
  private int pcw, pch;

  public PlotCanvas(int width, int height) {
    super();
    pcw = width;
    pch = height;
    this.setSize(pcw, pch);
  }


  public void update(Graphics g) {
    paint(g);
  }


  public void paint(Graphics g) {
    g.drawImage(imagePlot,0,0,this);
    this.getToolkit().sync();  // draw now
  }
    
}
