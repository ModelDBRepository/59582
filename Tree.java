/* Tree.java

Binary tree class
  - Trees consist of any number of objects of class Tree
  - Each object points to its parent (closer to root)
  - Each object may point to a left- and right-tree child
      - object is a continuation of a staight segment if
        ltree point to an object and rtree is null
      - object is a bifurcation point if both ltree and
        rtree point to tree objects.

BPG 4-3-03 time parameters changed to doubles
BPG 7-1-03 compiler errors fixed
BPG 6-6-00 intermediate path length calculation fixed
BPG 26-11-99 updated to use Graphics2D
BPG 28-9-99
*/

import java.awt.*;
import java.lang.Math;
import java.lang.Thread;
import java.io.*;


// Public generic binary tree class
public class Tree {

  // Public class variables
  public static int brkey;	// identifying branch key
                                // normally 1 + no.of branches

  // Public instance variables
  // Basic
  public String name;
  public Tree parent;
  public Tree ltree;
  public Tree rtree;
  public int key;  // identifying key
  public int order;  // centrifugal order
  public float length;  // length
  public float diam;  // diameter
  public float pathlength;  // path length to segment
  // Graphics
  public int locx;  // x-coord for screen drawing
  public int locy;  // y-coord for screen drawing
  // File IO
  public FileOutputStream fout;
  public PrintWriter pout;

  // Class variables for housekeeping and graphics
  public static int currx;  // current x position
  public static int curry;  // current y position
  public static float scalex;  // x-scaling (for drawing)
  public static int incx;  // increment in x per node (for printing)
  public static int incy;  // increment in y per node (for both)
  public static float bangle;  // branch angle (for real drawing)
  public static float badel;  // scale factor on branch angle
  public static long drawdel;  // delay between draw updates
  public static float maxLength;  // max possible path length
  public static float scsoma;  // scale factor for soma
  public static float scdend;  // scale factor for dendrite
  // File IO
  public static String tname;	// tree class name
  public static int fstore=0;  // flag to indicate "store data"


  // Basic tree constructors
  // More complex constructors are contained in subclasses

  // Constructor without parent node (root tree)
  public Tree() {
    key = 0;
    order = 0;
    parent = null;
    ltree = null;
    rtree = null;
  }


  // Constructor with parent node (subtree)
  public Tree(Tree parent) {
    this();  // basic constructor
    this.parent = parent;
  }


  // Constructor with parent node and identifying key and order
  public Tree(Tree parent, int key, int order) {
    this(parent);
    this.key = key;
    this.order = order;
  }


  // Constructor with parent node, key, order, length and diameter
  public Tree(Tree parent, int key, int order, float length, float diam) {
    this(parent, key, order);
    this.length = length;
    this.diam = diam;
  }



  // Tree housekeeping methods

  // Count segments in tree
  public int countSegments() {
    if (ltree == null && rtree == null)
      return 1;
    else if (rtree == null)
      return ltree.countSegments();
    else
      return 1 + ltree.countSegments() + rtree.countSegments();
  }


  // Count terminal nodes in tree
  public int countTerminals() {
    if (ltree == null && rtree == null)
      return 1;
    else if (rtree == null)
      return ltree.countTerminals();
    else
      return ltree.countTerminals() + rtree.countTerminals();
  }


  // Store terminal values (indexed by key)
  public void termValues(float[] termV, String pname) {
    if (parent == null)  // soma
      termV[key] = getValue(pname);
    if (ltree == null && rtree == null) {
      if (termV.length > key)
        termV[key] = getValue(pname);  // store terminal value
    };
    if (ltree != null) ltree.termValues(termV, pname);
    if (rtree != null) rtree.termValues(termV, pname);
 }


  // Calculate total path length in tree
  public float totPathLength() {
    if (parent == null) // soma not included
      return ltree.totPathLength();
    else if (ltree == null && rtree == null)
      return length;
    else if (rtree == null)
      return ltree.totPathLength() + length;
    else
      return length + ltree.totPathLength() + rtree.totPathLength();
  }


  // Find maximum path length in tree
  public float maxPathLength() {
    float lpath, rpath;

    if (parent == null)  // soma not included
      pathlength = 0;
//      pathlength = length;
    else
      pathlength = parent.pathlength + length;
    if (ltree == null && rtree == null) {
      return(pathlength);
    }
    else if (rtree == null)
      return ltree.maxPathLength();
    else {
      lpath = ltree.maxPathLength();
      rpath = rtree.maxPathLength();
    };
    if (lpath > rpath) return lpath;
    else return rpath;
  }


  // Store terminal path lengths
  public int termLengths(float[] termL, int i) {
    if (ltree == null && rtree == null) {
      termL[i] = length;  // store terminal length
      return i+1;
    }
    else if (rtree == null)
      return ltree.termLengths(termL, i);
    else
      return rtree.termLengths(termL, ltree.termLengths(termL, i));
  }


  // Store intermediate path lengths (NOT QUITE RIGHT)
  public int intLengths(float[] intL, int i) {
    if (ltree == null && rtree == null)
      return i;
    else if (ltree != null && rtree != null) {
      intL[i] = length;  // store intermediate length
      return rtree.intLengths(intL, ltree.intLengths(intL, i+1));
    }
    else if (ltree == null && rtree != null)
      return rtree.intLengths(intL, i);
    else
      return ltree.intLengths(intL, i);
  }


  // Store all path lengths
  public int pathLengths(float[] pathL, int i) {
    if (ltree == null && rtree == null) {
      pathL[i] = pathlength;  // store path length
      return i+1;
    }
    else if (ltree == null)
      return rtree.pathLengths(pathL, i);
    else if (rtree == null)
      return ltree.pathLengths(pathL, i);
    else
      return rtree.pathLengths(pathL, ltree.pathLengths(pathL, i));
  }


  // Store centrifugal orders
  public int centOrders(int[] centO, int i) {
    if (ltree == null && rtree == null) {
      centO[i] = order;  // store centrifugal order
      return i+1;
    }
    else if (rtree == null)
      return ltree.centOrders(centO, i);
    else
      return rtree.centOrders(centO, ltree.centOrders(centO, i));
  }


  // Find maximum centrifugal order in tree
  public int maxOrder() {
    if (ltree == null && rtree == null) {
      return order;  // terminal order
    }
    else if (rtree == null)
      return ltree.maxOrder();
    else return Math.max(ltree.maxOrder(), rtree.maxOrder());
  }


  // Calculate asymmetry partition of tree
  public float asymPart() {
    int nTerms, lnTerms, rnTerms;
    float currPart;

    lnTerms = 0;
    rnTerms = 0;
    nTerms = countTerminals();
    if (nTerms == 1) {
      return 0;
    }
    else if (rtree == null)  // continuation of branch
      return(ltree.asymPart());
    else {
      lnTerms = ltree.countTerminals();
      rnTerms = rtree.countTerminals();
    };
    if (lnTerms == 1 && rnTerms == 1)
      return 0;
    currPart = (float)Math.abs(lnTerms-rnTerms) / (float)(lnTerms+rnTerms-2);
    return(currPart + ltree.asymPart() + rtree.asymPart());
  }


  // Calculate asymmetry of tree
  public float asymIndex() {
    int nTerms;

    nTerms = countTerminals();
    if (nTerms == 1) {
      return 0;
    }
    else return(asymPart() / (float)(nTerms-1));
  }



  // Dummy routines for growing trees
  //   - must be defined by subclasses

  // Branch all terminal nodes
  public void branchTree(double t) {
  }

  // Grow terminal nodes
  public void elongateTree(double t) {
  }

  // Set segment diameters
  public void diamTree(double t) {
  }

  // Update previous values
  public void updateTree(double t) {
  }

  // Get variable value
  public float getValue(String vname) {
    return 0f;
  }

  // Get terminal parameter value
  public float termValue() {
    return 0f;
  }



  // Methods for displaying trees

  // Display colour tree with active parameter value
  public void displayTree(Graphics g, int x0, int y0, int dw, int dh, String pname, float pmaxval) {
    // (x0, y0) is the top left-hand corner of display area
    // dh, dw are the height and width of the display area

    // Display tree
    Tree.currx = x0;
    Tree.curry = y0 + (dh / 2);  // middle height
    Tree.scalex = (float)dw / maxLength;
    drawRealTree(g, bangle, pname, pmaxval);
    try {Thread.sleep(drawdel);} catch(InterruptedException eIE){};

  }


  // Draw tree as realistic branching structure in colour
  public void drawRealTree(Graphics g1, float bang, String pname, float pmval) {
    float dl, dh, dx, dy;
    float pval;

    Graphics2D g = (Graphics2D) g1;
    Stroke dendStroke;

    locx = currx;
    locy = curry;

    // soma
    if (parent == null) {
      dl = scsoma * scalex * length;
      dh = scsoma * scalex * diam;
      // Get parameter value and maximum value
      pval = getValue(pname);
      g.setColor(ColScale.ColVal(pval, pmval));
      g.fillOval(locx, locy-(int)(dh/2), (int)dl, (int)dh);
//      g.fillRect(locx, locy-(int)(dh/2), (int)dl, (int)dh);
//    g.drawLine(locx, locy, locx + (int)dl, locy);
      locx = currx + (int)dl;
      // set fixed line thickness for dendrites
//      dendStroke = new BasicStroke(scdend);
//      g.setStroke(dendStroke);
    };
    
    // draw continuing left tree
    if (ltree != null && rtree == null) {
      // dl = scalex * ltree.length;
      // dx = dl * (float)Math.cos((double)bangle*Math.PI/180);
      // dy = dl * (float)Math.sin((double)bangle*Math.PI/180);
      if (parent == null) {
        dx = scalex * ltree.length;
        dy = 0;
      } else {
        dx = (locx - parent.locx) * (ltree.length / length);
        dy = (locy - parent.locy) * (ltree.length / length);
      };
      currx = locx+(int)dx;
      curry = locy+(int)dy;
      // Get parameter value
      pval = ltree.getValue(pname);
      dendStroke = new BasicStroke(ltree.diam*scdend);
      g.setStroke(dendStroke);
      g.setColor(ColScale.ColVal(pval, pmval));
      g.drawLine(locx, locy, currx, curry);
      ltree.drawRealTree(g, bang, pname, pmval);
    }
    else {
    // draw left tree
    if (ltree != null) {
      dl = scalex * ltree.length;
      dx = dl * (float)Math.cos((double)bang*Math.PI/180);
      dy = dl * (float)Math.sin((double)bang*Math.PI/180);
      currx = locx+(int)Math.ceil((double)dx);
      curry = locy-(int)Math.ceil((double)dy);
      // Get parameter value
      pval = ltree.getValue(pname);
      dendStroke = new BasicStroke(ltree.diam*scdend);
      g.setStroke(dendStroke);
      g.setColor(ColScale.ColVal(pval, pmval));
      g.drawLine(locx, locy, currx, curry);
      ltree.drawRealTree(g, bang+(badel*bang), pname, pmval);
    };
    // draw right tree
    if (rtree != null) {
      dl = scalex * rtree.length;
      dx = dl * (float)Math.cos((double)bang*Math.PI/180);
      dy = dl * (float)Math.sin((double)bang*Math.PI/180);
      currx = locx+(int)Math.ceil((double)dx);
      curry = locy+(int)Math.ceil((double)dy);
      // Get parameter value
      pval = rtree.getValue(pname);
      dendStroke = new BasicStroke(rtree.diam*scdend);
      g.setStroke(dendStroke);
      g.setColor(ColScale.ColVal(pval, pmval));
      g.drawLine(locx, locy, currx, curry);
      rtree.drawRealTree(g, bang+(badel*bang), pname, pmval);
    };
    };

  }
  
  
  // Print tree to system output
  public void printTree() {
    System.out.println("Key: "+key+" Order: "+order);
    if (ltree != null) ltree.printTree();
    if (rtree != null) rtree.printTree();
  } 


  // Setup File IO for saving real-time segment data 
  public void setupFileIO() throws IOException {
    fout = new FileOutputStream(name+".pbr");
    pout = new PrintWriter(fout);
  }
  
  
  // Close output files
  public void closeFileIO() throws IOException {
    pout.close();
    if (ltree != null) ltree.closeFileIO();
    if (rtree != null) rtree.closeFileIO();
  }


}
