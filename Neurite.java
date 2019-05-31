/* Neurite.java

Applet and application (combined)
Package for generating neuritic trees.
Written for Java 2.

version JTB1 BPG 22-10-05
  - basic version containing code used for JTB paper:
    Graham and van Ooyen, Journal of Theoretical Biology 230:421-432, 2004
  

*/

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;


public class Neurite extends Applet implements Runnable {

  // Tree types
  public static final int BES_type = 0;
  public static final int AD_type = 1;
  public static final int ADcm_type = 2;

  // Tree structure
  Tree[] branches;

  // Tree parameters
  int tree_type=-1;
  int nTrees=1;  // number of trees to generate
  int ibr=0;  // tree index
  int nTerms, nInts;  // number of terminals and intermediates

  // Generated tree data
  float[] termV;   // values in terminal nodes
  int[][] timeTerm; // no. of terminals over time
  float[] mtimeTerm; // mean no. of terminals over time

  // Tree statistics
  int[] totSegments;  // number of tree segments generated
  float mSegs, sdSegs;  // mean and standard deviation
  int[] totTerms;  // number of terminal nodes (degree)
  float mTerms, sdTerms;  // mean and standard deviation
  float[] totLength;  // total tree length
  float mLength, sdLength;  // mean and standard deviation
  float[] termLength;  // terminal node length
  float mTermL, sdTermL;  // mean and standard deviation
  float[] intLength;  // intermediate node length
  float mIntL, sdIntL;  // mean and standard deviation
  float[] maxPath;  // maximum path length of a tree
  float[] pathLength;  // path length of all paths
  float mPathL, sdPathL;  // mean and standard deviation
  float[] asym;  // tree asymmetry index
  float mAsym, sdAsym;  // mean and standard deviation
  int[] maxCO;  // maximum centrifugal order of a tree
  int[] centorder;  // centrifugal order of all paths
  float mCO, sdCO;  // mean and standard deviation

  // Menu display
  public static int mainw=600;
  public static int mainh=220;

  // Graphics display
  private boolean refresh=true;  // refresh display
  public static int dispw=600;  // display width
  public static int disph=350;  // display height
  public static int dispoffx=10;  // x offset
  public static int dispoffy=10;  // y offset
  public static int dtw=580;
  public static int dth=330;
  public static String dname="";  // parameter to display
  public static float dmval=1.0f;  // maximum parameter value
  public static int dtdisp=1;  // display every dtdisp time steps
  public static boolean dispfl=false;  // turn display on or off
  private TreeDisplay treeDisp;	// graphic display window

  // Plots
  public static int pw=300;  // plot width
  public static int ph=150;  // plot height
  public static String pname="";  // plot variable
  public static float pmval=1.0f;  // maximum plot value
  public static int dtplot=1;  // plot every dtplot time steps
  public static int somafl=0;  // include soma
  public static final int NPMAX = 10;
  private TermPlot[] tp;
  public static int nplots=0;  // number of plots

  // Simulation parameters
  private Thread construct_thread=null;  // tree constructor
  float dt=1;   // time step
  double Tstop=0;  // total simulation time
  double tsim=0;  // elapsed simulation time
  double Tstep=0;  // time to step through
  double Tend=0;  // current stop time
  boolean dostep=false;  // step through growth
  boolean stepping=false;  // in process of stepping
  boolean new_type=true;  // flag new tree type

  // File I/O
  int sSflag=0;  // store statistics data flag
  int sCflag=0;  // store concentration data flag
  int sLflag=0;  // store length data flag
  String fSstem="test";
  String fCstem="test";
  String fLstem="test";
  FileOutputStream flen;
  PrintWriter plen;
  FileOutputStream fC;
  PrintWriter pC;
  int sTflag=0;  // store terminal data flag
  double Tterm=0;  // terminal number collection time
  int sPflag=0;  // store plot data flag
  String fPstem="test";
  static final int maxfP=20;
  FileOutputStream[] fP;
  PrintWriter[] pP;
  int sDflag=0;  // store tree display flag
  String fDstem="test";
  static final int maxfD=100;  // currently not used
  FileOutputStream fD;
  // Reading and saving simulation parameters (BPG 3-4-01)
  String fMstem="model";
  FileInputStream fMi;
  FileOutputStream fMo;
  PrintWriter pM;

  // GUI stuff
  Button load_button;  // load simulation parameters from file
  Button save_button;  // save simulation parameters to file
  Button quit_button;
  Button sim_button;
  Choice tree_choice;
  Button tree_button;
  Button stop_button;
  Button display_button;
  Button dispfl_button;
  Button plot_button;
  Button draw_button;
  Button help_button;
  TextField ibr_text;
  TextArea message_text;
  StringBuffer message;
  String de_title;
  DataEntry d;
  Graphics g;
  Rectangle r;


  // Data entry
    
  String SIM_data[][] = {
    {"Number of trees:", "1", "1"},
    {"Time step (dt):", "1", "1"},
    {"Simulation time (Tstop):", "200", "200"},
    {"Store statistics (1=yes):", "0", "0"},
    {"Statistics file stem:", "AD", "AD"},
    {"Store concentrations (1=yes):", "0", "0"},
    {"Concentration file stem:", "AD", "AD"},
    {"Store lengths (1=yes):", "0", "0"},
    {"Length file stem:", "AD", "AD"},
    {"Store no. terminals over time (1=yes):", "0", "0"},
    {"Storage interval (time):", "1", "1"}};
  private final int Nsim = 11;

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

  String PLOT_data[][] = {
    {"Plot width:", "300", "300"},
    {"Plot height:", "150", "150"},
    {"Plot parameter:", "Cbr", "Cbr"},
    {"Maximum plot value:", "1.0", "1.0"},
    {"Plot interval (steps):", "100", "100"},
    {"Include soma (1=yes):", "0", "0"},
    {"Save plot values (1=yes):", "0", "0"},
    {"Save file stem:", "AD", "AD"}};
  private final int Nplot = 8;



  // Main program for application
  public static void main(String args[]) {
    NeuriteFrame app = new NeuriteFrame();
    app.setSize(mainw, mainh);
    app.show();
  }



  // Initialisation
  public void init() {

    // No trees initially
    branches = null;
    nTerms = 0;

    // Initial simulation things
    nTrees = Integer.parseInt(SIM_data[0][2]);
    dt = Float.valueOf(SIM_data[1][2]).floatValue();
    Tstop = Double.valueOf(SIM_data[2][2]).floatValue();
    
    // Initial graphics things
    g = this.getGraphics();
    r = this.getBounds();
    dispw = Integer.parseInt(GRAPHIC_data[0][2]);
    disph = Integer.parseInt(GRAPHIC_data[1][2]);
    dname = GRAPHIC_data[2][2];
    dmval = Float.valueOf(GRAPHIC_data[3][2]).floatValue();
    dtdisp = Integer.parseInt(GRAPHIC_data[4][2]);
    Tree.drawdel = (long)Integer.parseInt(GRAPHIC_data[5][2]);
    Tree.maxLength = Float.valueOf(GRAPHIC_data[6][2]).floatValue();
    Tree.bangle = Float.valueOf(GRAPHIC_data[7][2]).floatValue();
    Tree.badel = Float.valueOf(GRAPHIC_data[8][2]).floatValue();
    Tree.scsoma = Float.valueOf(GRAPHIC_data[9][2]).floatValue();
    Tree.scdend = Float.valueOf(GRAPHIC_data[10][2]).floatValue();

    tp = new TermPlot[NPMAX];
    nplots = 0;
    pw = Integer.parseInt(PLOT_data[0][2]);
    ph = Integer.parseInt(PLOT_data[1][2]);
    pname = PLOT_data[2][2];
    pmval = Float.valueOf(PLOT_data[3][2]).floatValue();
    dtplot = Integer.parseInt(PLOT_data[4][2]);
    somafl = Integer.parseInt(PLOT_data[5][2]);
    sPflag = Integer.parseInt(PLOT_data[6][2]);
    fPstem = PLOT_data[7][2];

    fP = new FileOutputStream[maxfP];
    pP = new PrintWriter[maxfP];


    // Add useful menus and buttons
    
    // quit
    quit_button = new Button("Quit");
    quit_button.setForeground(Color.black);
    quit_button.setBackground(Color.lightGray);
    add(quit_button);
    quit_button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        System.exit(0);
      }
    });
    
    // read parameters from a file
    load_button = new Button("Load");
    load_button.setForeground(Color.black);
    load_button.setBackground(Color.lightGray);
    add(load_button);
    load_button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        String currl;   // current line
        FileDialog get_fn = new FileDialog(new Frame(),"Load File",FileDialog.LOAD);
        get_fn.setDirectory("Params");
        get_fn.show();
        String fname = get_fn.getFile();
        get_fn.hide();
        System.out.println(fname);
        
        try {
        FileInputStream fi = new FileInputStream("Params/"+fname);
        BufferedReader r = new BufferedReader(new InputStreamReader(fi));
        
        // model parameters
        String[][] MOD_data = BESTree.DEparams;
        int Nmod = BESTree.Nparams;
        currl = r.readLine();
        currl = r.readLine();
        currl = r.readLine();
        currl = r.readLine();
        currl = r.readLine();
        tree_type = Integer.parseInt(currl);   // model type number
        tree_choice.select(tree_type);   // set model in menu selection
        switch (tree_type) {
        case BES_type:
          default:
          MOD_data = BESTree.DEparams;
          Nmod = BESTree.Nparams;
          break;
        case AD_type:
          MOD_data = ADTree.DEparams;
          Nmod = ADTree.Nparams;
          break;
        case ADcm_type:
            MOD_data = ADcmTree.DEparams;
            Nmod = ADcmTree.Nparams;
            break;
         };
        for (int i = 0; i < Nmod; i++) {
          currl = r.readLine();
          currl = r.readLine();
          MOD_data[i][2] = currl;
        };
        
        // simulation parameters
        currl = r.readLine();
        currl = r.readLine();
        for (int i = 0; i < Nsim; i++) {
          currl = r.readLine();
          currl = r.readLine();
          SIM_data[i][2] = currl;
        };
        nTrees = Integer.parseInt(SIM_data[0][2]);
        dt = Float.valueOf(SIM_data[1][2]).floatValue();
        Tstop = Double.valueOf(SIM_data[2][2]).floatValue();
        sSflag = Integer.parseInt(SIM_data[3][2]);
        fSstem = SIM_data[4][2];
        sCflag = Integer.parseInt(SIM_data[5][2]);
        fCstem = SIM_data[6][2];
        sLflag = Integer.parseInt(SIM_data[7][2]);
        fLstem = SIM_data[8][2];
        sTflag = Integer.parseInt(SIM_data[9][2]);
        Tterm = Double.valueOf(SIM_data[10][2]).floatValue();
	    timeTerm = new int[nTrees][(int)(Tstop/Tterm)+1];
	    for (int i = 0; i < nTrees; i++) timeTerm[i][0] = 1;

        // graphic display parameters
        currl = r.readLine();
        currl = r.readLine();
        for (int i = 0; i < Ngraphic; i++) {
          currl = r.readLine();
          currl = r.readLine();
          GRAPHIC_data[i][2] = currl;
        };
        dispw = Integer.parseInt(GRAPHIC_data[0][2]);
        disph = Integer.parseInt(GRAPHIC_data[1][2]);
        dname = GRAPHIC_data[2][2];
        dmval = Float.valueOf(GRAPHIC_data[3][2]).floatValue();
        dtdisp = Integer.parseInt(GRAPHIC_data[4][2]);
        Tree.drawdel = (long)Integer.parseInt(GRAPHIC_data[5][2]);
        Tree.maxLength = Float.valueOf(GRAPHIC_data[6][2]).floatValue();
        Tree.bangle = Float.valueOf(GRAPHIC_data[7][2]).floatValue();
        Tree.badel = Float.valueOf(GRAPHIC_data[8][2]).floatValue();
        Tree.scsoma = Float.valueOf(GRAPHIC_data[9][2]).floatValue();
        Tree.scdend = Float.valueOf(GRAPHIC_data[10][2]).floatValue();        
        fi.close();
        }
        catch(IOException e) {
          System.out.println("Something went wrong loading the data!");
        };
        
      }
    });
    
    // save parameters to a file
    save_button = new Button("Save");
    save_button.setForeground(Color.black);
    save_button.setBackground(Color.lightGray);
    add(save_button);
    save_button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        FileDialog get_fn = new FileDialog(new Frame(),"Save File",FileDialog.SAVE);
        get_fn.setDirectory("Params");
        get_fn.show();
        String fname = get_fn.getFile();
        get_fn.hide();
        System.out.println(fname);
        
        try {
        FileOutputStream fo = new FileOutputStream("Params/"+fname);
        PrintStream po = new PrintStream(fo);
        
        // version number
        po.println("# Neurite version JTB1");
        
        // model parameters
        String[][] MOD_data = BESTree.DEparams;
        int Nmod = BESTree.Nparams;
        po.println("");   // blank line
        po.println("# MODEL PARAMETERS");
        switch (tree_type) {
        case BES_type:
          default:
          po.println("# MODEL: BES");
          MOD_data = BESTree.DEparams;
          Nmod = BESTree.Nparams;
          break;
        case AD_type:
          po.println("# MODEL: AD");
          MOD_data = ADTree.DEparams;
          Nmod = ADTree.Nparams;
          break;
        case ADcm_type:
            po.println("# MODEL: ADcm");
            MOD_data = ADcmTree.DEparams;
            Nmod = ADcmTree.Nparams;
            break;
        };
        po.println(tree_type);   // model type number
        for (int i = 0; i < Nmod; i++) {
          po.println("# "+MOD_data[i][0]);
          po.println(MOD_data[i][2]);
        };
        
        // simulation parameters
        po.println("");   // blank line
        po.println("# SIMULATION PARAMETERS");
        for (int i = 0; i < Nsim; i++) {
          po.println("# "+SIM_data[i][0]);
          po.println(SIM_data[i][2]);
        };
        
        // graphic display parameters
        po.println("");   // blank line
        po.println("# DISPLAY PARAMETERS");
        for (int i = 0; i < Ngraphic; i++) {
          po.println("# "+GRAPHIC_data[i][0]);
          po.println(GRAPHIC_data[i][2]);
        };
        
        fo.close();
        }
        catch(IOException e) {
          System.out.println("Something went wrong saving the data!");
        };
        
      }
    });

    //   choices of tree type
    tree_choice = new Choice();
    tree_choice.addItem("BESTL");
    tree_choice.addItem("AD");
    tree_choice.addItem("ADcm");
    add(tree_choice);
    tree_choice.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent event) {
      de_title = tree_choice.getSelectedItem();
      if (tree_type != tree_choice.getSelectedIndex())
        new_type = true;
      else
        new_type = false;
      tree_type = tree_choice.getSelectedIndex();
      switch (tree_type) {
        case BES_type:
        default:
          d = new DataEntry(new Frame(), de_title, BESTree.DEparams, BESTree.Nparams);
          break;
        case AD_type:
          d = new DataEntry(new Frame(), de_title, ADTree.DEparams, ADTree.Nparams);
          break;
        case ADcm_type:
          d = new DataEntry(new Frame(), de_title, ADcmTree.DEparams, ADcmTree.Nparams);
          break;
      };
      d.show();
      }
    });
    
    // button for editing simulation parameters
    sim_button = new Button("Simulation");
    sim_button.setForeground(Color.black);
    sim_button.setBackground(Color.lightGray);
    add(sim_button);
    sim_button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        d = new DataEntry(new Frame(), "Simulation", SIM_data, Nsim);
        d.show();
        nTrees = Integer.parseInt(SIM_data[0][2]);
        dt = Float.valueOf(SIM_data[1][2]).floatValue();
        Tstop = Double.valueOf(SIM_data[2][2]).floatValue();
        sSflag = Integer.parseInt(SIM_data[3][2]);
        fSstem = SIM_data[4][2];
        sCflag = Integer.parseInt(SIM_data[5][2]);
        fCstem = SIM_data[6][2];
        sLflag = Integer.parseInt(SIM_data[7][2]);
        fLstem = SIM_data[8][2];
        sTflag = Integer.parseInt(SIM_data[9][2]);
        Tterm = Double.valueOf(SIM_data[10][2]).floatValue();
        timeTerm = new int[nTrees][(int)(Tstop/Tterm)+1];
        for (int i = 0; i < nTrees; i++) timeTerm[i][0] = 1;
	  }
    });

    //   button for editing display parameters
    display_button = new Button("Display");
    display_button.setForeground(Color.black);
    display_button.setBackground(Color.lightGray);
    add(display_button);
    display_button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        d = new DataEntry(new Frame(), "Display", GRAPHIC_data, Ngraphic);
        d.show();
        dispw = Integer.parseInt(GRAPHIC_data[0][2]);
        disph = Integer.parseInt(GRAPHIC_data[1][2]);
        dname = GRAPHIC_data[2][2];
        dmval = Float.valueOf(GRAPHIC_data[3][2]).floatValue();
        dtdisp = Integer.parseInt(GRAPHIC_data[4][2]);
        Tree.drawdel = (long)Integer.parseInt(GRAPHIC_data[5][2]);
        Tree.maxLength = Float.valueOf(GRAPHIC_data[6][2]).floatValue();
        Tree.bangle = Float.valueOf(GRAPHIC_data[7][2]).floatValue();
        Tree.badel = Float.valueOf(GRAPHIC_data[8][2]).floatValue();
        Tree.scsoma = Float.valueOf(GRAPHIC_data[9][2]).floatValue();
        Tree.scdend = Float.valueOf(GRAPHIC_data[10][2]).floatValue();
      }
    });

    //   button for turning display on or off
    if (dispfl)
      dispfl_button = new Button("On");
    else
      dispfl_button = new Button("Off");
    dispfl_button.setForeground(Color.black);
    dispfl_button.setBackground(Color.lightGray);
    add(dispfl_button);
    dispfl_button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        if (dispfl) {
          dispfl = false;
          dispfl_button.setLabel("Off");
          treeDisp.dispose();
        } else {
          dispfl = true;
          dispfl_button.setLabel("On");
          treeDisp = new TreeDisplay(dname, dispw, disph, dname, dmval, dtplot);
        };
      }
    });

    //   button for editing plot parameters
    plot_button = new Button("Plot");
    plot_button.setForeground(Color.black);
    plot_button.setBackground(Color.lightGray);
    add(plot_button);
    plot_button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        if (nplots >= NPMAX)  {  // too many plots
          message_text.setText("Too many plots!");
        } else {
          d = new DataEntry(new Frame(), "Plot", PLOT_data, Nplot);
          d.show();
          pw = Integer.parseInt(PLOT_data[0][2]);
          ph = Integer.parseInt(PLOT_data[1][2]);
          pname = PLOT_data[2][2];
          pmval = Float.valueOf(PLOT_data[3][2]).floatValue();
          dtplot = Integer.parseInt(PLOT_data[4][2]);
          somafl = Integer.parseInt(PLOT_data[5][2]);
          sPflag = Integer.parseInt(PLOT_data[6][2]);
          fPstem = PLOT_data[7][2];
          // create new plot
          tp[nplots++] = new TermPlot(pname, pw, ph, pname, pmval, dtplot, somafl, sPflag, fPstem);
        };
      }
    });

    //   button for constructing trees
    tree_button = new Button("Construct");
    tree_button.setForeground(Color.black);
    tree_button.setBackground(Color.lightGray);
    add(tree_button);
    tree_button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        if (dostep)  {  // already constructing
          message_text.setText("Still constructing!");
        } else {
          if (!stepping) refresh_plots();
          stepping = false;
          constructTree(Tstop);
        };
      }
    });

    //   button for stopping construction
    stop_button = new Button("Stop");
    stop_button.setForeground(Color.black);
    stop_button.setBackground(Color.lightGray);
    add(stop_button);
    stop_button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        dostep = false;
      }
    });

    //   button for drawing trees
    draw_button = new Button("Draw");
    draw_button.setForeground(Color.black);
    draw_button.setBackground(Color.lightGray);
    add(draw_button);
    draw_button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        if (getTreeIndex() == true) {
          if (branches != null && branches[ibr] != null) {
            indTreeStats(ibr);  // display statistics
            refresh = true;
            nTerms = 0;
            paint(g);
          } else
            message_text.setText("Tree not constructed!");
        }; 
      }
    });

    //   index of tree to display
    add(new Label("Display tree:"));
    ibr_text = new TextField("0", 5);
    add(ibr_text);

    //   button for online "help"
    help_button = new Button("Help");
    help_button.setForeground(Color.black);
    help_button.setBackground(Color.lightGray);
    add(help_button);
    help_button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        HelpWindow help = new HelpWindow();
      }
    });

    // Messages
    message_text = new TextArea(6, 80);
    message_text.setEditable(false);
    message_text.setForeground(Color.black);
    message_text.setBackground(Color.lightGray);
    add(message_text);

  }


  // Graphics
  public void paint(Graphics g) {

    Graphics2D gt, gp;
    Stroke treeStroke;


    if (branches != null && branches[ibr] != null) {
      g = treeDisp.dc.getGraphics();
      // get drawing area size
      Rectangle r = treeDisp.dc.getBounds();  // applet size
      // refresh if new tree, or window has changed size
      if (dispfl && (refresh || dispw != r.width || disph != r.height)) {
        dispw = r.width;
        disph = r.height;
        dth = disph - (2*dispoffy);
        dtw = dispw - (2*dispoffx);
        // offscreen image for tree
        treeDisp.dc.imageTree = treeDisp.dc.createImage(dispw, disph);
        gt = (Graphics2D) treeDisp.dc.imageTree.getGraphics();
        treeStroke = new BasicStroke(5.0f);
        gt.setStroke(treeStroke);
        // colour scale for drawing
        ColScale.drawCS(gt, 0, dth/10, dtw/2);
        g.drawImage(treeDisp.dc.imageTree,dispoffx,dispoffy,treeDisp.dc);
        refresh = false;
      };
      // draw current tree
      if (dispfl && ((long)(tsim/(double)dt) % dtdisp == 0)) {
        gt = (Graphics2D) treeDisp.dc.imageTree.getGraphics();
        gt.clearRect(0, 0, dtw, dth);
        treeStroke = new BasicStroke(5.0f);
        gt.setStroke(treeStroke);
        ColScale.drawCS(gt, 0, dth/10, dtw/2);
        branches[ibr].displayTree(gt, 0, 0, dtw, dth, dname, dmval);
        g.drawImage(treeDisp.dc.imageTree,dispoffx,dispoffy,treeDisp.dc);
        treeDisp.dc.getToolkit().sync();  // draw now
      };
      // plot terminal values
      for (int i = 0; i < nplots; i++) {
        if (dostep && ((long)(tsim/(double)dt) % tp[i].dtplot == 0)) {
          gp = (Graphics2D) tp[i].gp.imagePlot.getGraphics();
          tp[i].plotTerms(gp, branches[ibr], tsim, Tstop, tp[i].pname, tp[i].pmval);
          tp[i].repaint();
          if (tp[i].sPflag == 1)
           tp[i].saveTerms(branches[ibr], tsim, tp[i].pname);
        };
      };
    };  

  }



  // Refresh plot windows if new simulation
  private void refresh_plots() {
    for (int i = 0; i < nplots; i++) tp[i].refresh = true;
  }
    


  // Construct required number of trees
  public void constructTree(double t) {

    switch (tree_type) {
      case BES_type:
      default:
        BESTree.updateParams();
        BESTree.setTimeStep(dt);
        break;
      case AD_type:
        ADTree.updateParams();
        ADTree.setTimeStep(dt);
        break;
      case ADcm_type:
        ADcmTree.updateParams();
        ADcmTree.setTimeStep(dt);
        break;
    };

    // Set up for new construction or continuation
    if (!stepping) {  // new construction
      branches = new Tree[nTrees];
      // construct cell bodies (with initial neurites)
      for (ibr = 0; ibr < nTrees; ibr++) {
        switch (tree_type) {
     case BES_type:
          default:
            branches[ibr] = new BESTree(0, 0);
            break;
     case AD_type:
            branches[ibr] = new ADTree(0, 0);
            break;
     case ADcm_type:
         branches[ibr] = new ADcmTree(0, 0);
         break;
        };
      };
      ibr = 0;
      tsim = 0;
      refresh = true;
      stepping = true;
      nTerms = 0;
    } else {  // continuation of single tree
      refresh = false;
    };
    Tend = tsim + t;
    if (Tend > Tstop) Tend = Tstop;          
    dostep = true;

    // Construct trees
    construct_thread = new Thread(this);
    construct_thread.start();

  }



  // Thread to construct trees
  public void run() {

    for (ibr = 0; ibr < nTrees && dostep; ibr++) {
      message_text.setText("Tree "+(ibr+1));
      growTree(ibr, Tend);  // construct tree
      if (tsim >= Tstop) {
        tsim = 0;  // finished tree
        for (int i = 0; i < nplots; i++) {
          if (tp[i].sPflag == 1)
           tp[i].flushTerms();
        };
      };
    };

    if (!dostep || Tend >= Tstop) {
      stepping = false;  // stopped or finished
      message_text.setText("Finished!");
      if (Tend >= Tstop) {  // completed normally
        calcTreeStats();
        if (nTrees == 1)
          indTreeStats(0);
        else {
          multTreeStats();
          if (sSflag == 1) storeTreeData(fSstem);
        };
      };
    };
    dostep = false;
    ibr = 0;
    
    // close any data files
    int i = 0;
    while (pP[i] != null) {
      pP[i].flush();
      pP[i].close();
      i++;
    };

  }
  


  // Grow a tree and display growth
  public void growTree(int ibr, double Tend) {

    while (dostep && tsim < Tend) {
      tsim += (double)dt;   // update simulation time
      branches[ibr].branchTree(tsim);
      branches[ibr].elongateTree(tsim);
      branches[ibr].diamTree(tsim);
      branches[ibr].updateTree(tsim);
      if (sTflag == 1 && (Math.round(tsim/(double)dt) % Math.round(Tterm/(double)dt) == 0)) {
        timeTerm[ibr][(int)(tsim/Tterm)] = branches[ibr].countTerminals();
      };
      if (nTrees == 1 && dispfl && ((long)(tsim/(double)dt) % dtdisp == 0)) {
        paint(g);
      };
    };

  }



  // Get and validate tree index
  boolean getTreeIndex() {
    ibr = Integer.parseInt(ibr_text.getText())-1;  // tree index
    if (0 <= ibr && ibr < nTrees)  {  // valid index
      return true;
    }
    else { // bad index
      message_text.setText("Index should be between 1 and "+nTrees);
    };
    return false;
  }



  // Calculate tree statistics
  void calcTreeStats() {

    // data structures for one point per tree
    totSegments = new int[nTrees];
    totTerms = new int[nTrees];
    totLength = new float[nTrees];
    asym = new float[nTrees];
    maxCO = new int[nTrees];
    maxPath = new float[nTrees];

    // calculate data (one data point per tree)
    for (ibr = 0; ibr < nTrees; ibr++) {
      totSegments[ibr] = branches[ibr].countSegments();
      totTerms[ibr] = branches[ibr].countTerminals();
      totLength[ibr] = branches[ibr].totPathLength();
      asym[ibr] = branches[ibr].asymIndex();
      maxCO[ibr] = branches[ibr].maxOrder();
      maxPath[ibr] = branches[ibr].maxPathLength();
    };

    // calculate numbers of data points for multiple points per tree
    nTerms = 0; nInts = 0;
    for (ibr = 0; ibr < nTrees; ibr++) {
      nTerms = nTerms + totTerms[ibr];  // number of terminals
      nInts = nInts + totSegments[ibr] - totTerms[ibr];  // intermediates
    };

    // data structures for multiple points per tree
    centorder = new int[nTerms];
    pathLength = new float[nTerms];
    termLength = new float[nTerms];
    intLength = new float[nInts];

    // calculate and store this data
    int i = 0;
    int j = 0;
    branches[0].pathLengths(pathLength, 0);
    branches[0].termLengths(termLength, 0);
    branches[0].intLengths(intLength, 0);
    branches[0].centOrders(centorder, 0);
    for (ibr = 1; ibr < nTrees; ibr++) {
      i = i + totTerms[ibr-1];
      branches[ibr].pathLengths(pathLength, i);
      branches[ibr].termLengths(termLength, i);
      branches[ibr].centOrders(centorder, i);
      j = j + totSegments[ibr-1] - totTerms[ibr-1];
      branches[ibr].intLengths(intLength, j);
    };

    // stats on data points
    mSegs = (float)ArrayStats.mean(totSegments, nTrees);
    sdSegs = (float)ArrayStats.std(totSegments, nTrees);
    mTerms = (float)ArrayStats.mean(totTerms, nTrees);
    sdTerms = (float)ArrayStats.std(totTerms, nTrees);
    mLength = (float)ArrayStats.mean(totLength, nTrees);
    sdLength = (float)ArrayStats.std(totLength, nTrees);
    mAsym = (float)ArrayStats.mean(asym, nTrees);
    sdAsym = (float)ArrayStats.std(asym, nTrees);
    mCO = (float)ArrayStats.mean(centorder, nTerms);
    sdCO = (float)ArrayStats.std(centorder, nTerms);
    mPathL = (float)ArrayStats.mean(pathLength, nTerms);
    sdPathL = (float)ArrayStats.std(pathLength, nTerms);
    mTermL = (float)ArrayStats.mean(termLength, nTerms);
    sdTermL = (float)ArrayStats.std(termLength, nTerms);
    mIntL = (float)ArrayStats.mean(intLength, nInts);
    sdIntL = (float)ArrayStats.std(intLength, nInts);

    // calculate average number of terminals over time
    mtimeTerm = new float[(int)(Tstop/Tterm)+1];
    for (int it = 0; it <= (int)(Tstop/Tterm); it++) {
      int numT = 0;
      for (ibr = 0; ibr < nTrees; ibr++) numT += timeTerm[ibr][it];
      mtimeTerm[it] = (float)numT / (float)nTrees;
    };

  }



  // Show individual tree statistics
  void indTreeStats(int ibr) {
    message = new StringBuffer();
    message.append("Segments = " + Integer.toString(totSegments[ibr]));
    message.append("; Degree=" + Integer.toString(totTerms[ibr]));
    message.append("; Asymmetry=" + Float.toString(asym[ibr]));
    message.append("; Order=" + Integer.toString(maxCO[ibr]));
    message.append("\nTotal Length = " + Float.toString(totLength[ibr]));
    message.append("; Max Path Length = " + Float.toString(maxPath[ibr]));
    message.append("\nPath length: m=" + Float.toString(mPathL));
    message.append(", std=" + Float.toString(sdPathL));
    message.append("\nIntermediate length: m=" + Float.toString(mIntL));
    message.append(", std=" + Float.toString(sdIntL));
    message.append("\nTerminal length: m=" + Float.toString(mTermL));
    message.append(", std=" + Float.toString(sdTermL));
    message_text.setText(message.toString());
  }



  // Show multiple tree statistics
  void multTreeStats() {
    message = new StringBuffer();     
    message.append("Degree: mean=" + Float.toString(mTerms));
    message.append(", std=" + Float.toString(sdTerms));
    message.append("\nAsymmetry: mean=" + Float.toString(mAsym));
    message.append(", std=" + Float.toString(sdAsym));
    message.append("\nCentrifugal order: mean=" + Float.toString(mCO));
    message.append(", std=" + Float.toString(sdCO));
    message.append("\nTotal length: mean=" + Float.toString(mLength));
    message.append(", std=" + Float.toString(sdLength));
    message.append("\nPath length: m=" + Float.toString(mPathL));
    message.append(", std=" + Float.toString(sdPathL));
    message.append("\nIntermediate length: m=" + Float.toString(mIntL));
    message.append(", std=" + Float.toString(sdIntL));
    message.append("\nTerminal length: m=" + Float.toString(mTermL));
    message.append(", std=" + Float.toString(sdTermL));
    message_text.setText(message.toString());
  }



  // Store tree data in files
  void storeTreeData(String fstem) {
    try {
      switch(tree_type) {
        case BES_type:
        default:
          storeInfo(fstem+".inf", BESTree.DEparams, BESTree.Nparams);
          break;
        case AD_type:
          storeInfo(fstem+".inf", ADTree.DEparams, ADTree.Nparams);
          break;
        case ADcm_type:
            storeInfo(fstem+".inf", ADcmTree.DEparams, ADcmTree.Nparams);
            break;
      };
      storeArray(fstem+"_deg.dat", totTerms, nTrees);
      storeArray(fstem+"_co.dat", centorder, nTerms);
      storeArray(fstem+"_asym.dat", asym, nTrees);
      storeArray(fstem+"_totL.dat", totLength, nTrees);
      storeArray(fstem+"_termL.dat", termLength, nTerms);
      storeArray(fstem+"_intL.dat", intLength, nInts);
      storeArray(fstem+"_pathL.dat", pathLength, nTerms);
	  if (sTflag == 1)
          storeArray(fstem+"_termT.dat", mtimeTerm, (int)(Tstop/Tterm)+1);
    }
    catch(IOException e){};
  }



  // Store model parameter information in a file
  void storeInfo(String fname, String[][] pinfo, int ndata)
      throws IOException {
    int i;
    FileOutputStream fout = null;
    PrintWriter pout = null;

    fout = new FileOutputStream(fname);
    pout = new PrintWriter(fout);
    for (i = 0; i < ndata; i++)
      pout.println(pinfo[i][0]+" "+pinfo[i][2]);
    //    if (tree_type != Br1_type) {
      pout.println("\nResults:");
      multTreeStats();
      pout.println(message.toString());
      //      };
    pout.close();
  }



  // Store an array of float data in a file
  void storeArray(String fname, float[] adata, int ndata)
      throws IOException {
    int i;
    FileOutputStream fout = null;
    PrintWriter pout = null;

    fout = new FileOutputStream(fname);
    pout = new PrintWriter(fout);
    for (i = 0; i < ndata; i++)
      pout.println(adata[i]);
    pout.close();
  }



  // Store an array of int data in a file
  void storeArray(String fname, int[] adata, int ndata)
      throws IOException {
    int i;
    FileOutputStream fout = null;
    PrintWriter pout = null;

    fout = new FileOutputStream(fname);
    pout = new PrintWriter(fout);
    for (i = 0; i < ndata; i++)
      pout.println(adata[i]);
    pout.close();
  }


}



// Frame class for application
class NeuriteFrame extends Frame {
    public NeuriteFrame() {
      super("Neurite");

      Neurite applet = new Neurite();

      add(applet, "Center");
      applet.init();

      addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent event) {
          dispose();
          System.exit(0);
        }
      });
    }
}
