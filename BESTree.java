/* BESTree.java

Implements van Pelt's BESTL model

Version 1.0 BPG 4-3-03
*/

import java.lang.Math;
import java.util.Random;


// New tree class
public class BESTree extends Tree {

  // New class variables
  static float B=1.0f;  // overall branching probability
  static float Kbr=1.0f;  // scale factor for branching probability
  static float pbr=1.0f;  // terminal-dependent prob. factor
  static float Cnorm=1.0f;  // order-dependent norm. factor
  static float E;  // dependence on number of terminals
  static float S;  // dependence on centrifugal order
  static float dt=1.0f;  // time step
  static float dL=1.0f;  // length step
  static float somaL=1.0f;  // soma length
  static float somadiam=1.0f;  // soma diameter
  static float termdiam=1.0f;  // terminal node diameter
  static float ebp=1.0f;  // branch power for diameter
  static float Vel=1.0f;  // elongation phase growth rate
  static float Vbr=1.0f;  // branching phase growth rate
  static float crate=1.0f;	// mean elongation rate
  static float rCV=1.0f;  // coefficient of variation of rates
  static double Tbstop=1.0;  // branching stop time

  // data entry
  public static String DEparams[][] = {
    {"Branching probability (B):", "3.85", "3.85"},
    {"Dependence on number of terminals (E):", "0.74", "0.74"},
    {"Dependence on centrifugal order (S):", "0.87", "0.87"},
    {"Branching phase rate (Vbr):", "0.22", "0.22"},
    {"Elongation phase rate (Vel):", "0.51", "0.51"},
    {"Rate coefficient of variation (rCV):", "0.28", "0.28"},
    {"Time branching stops (Tbstop):", "264", "264"},
    {"Soma length:", "10.0", "10.0"},
    {"Soma diameter:", "10.0", "10.0"},
    {"Terminal diameter:", "0.8", "0.8"},
    {"Branch power (e):", "1.6", "1.6"}};
  public static final int Nparams=11;

  // random number generators for branching and elongation
  static Random ranG=null;  // Gaussian random number
  static Random ranp=null;  // Uniform random number
 
  // Instance variables
  float Cbr;	// conc. of branch-producing chemical
  float Cbrp;	// value at previous time step
  float erate; // variation in elongation rate


  // Constructor for cell body
  public BESTree(int key, int order) {
    super(null, key, order, somaL, somadiam);  // basic tree constructor
    
    ranp = new Random();  // randomness for branching
    ranG = new Random();  // randomness for elongation rates
    
    updateParams();  // get parameter values from data entry
    Cbr = 1.0f;
    crate = Vbr;  // branching phase mean elongation rate
    
    // add first neurite segment as left branch
    this.ltree = new BESTree(this, key+1, Cbr);
    Tree.brkey = key+2;  // reset branch key
  }


  // Constructor for first neurite branch
  public BESTree(Tree parent, int key, float Cinit) {
    super(parent, key, 0, 0f, termdiam);  // basic tree constructor
    Cbr = Cinit;  // no branching chemical initially
    erate = (float)ranG.nextGaussian();  // new variation
    // must have positive growth
    while((rCV*crate*erate) + crate <= 0)
      erate = (float)ranG.nextGaussian();  // new variation
  }


  // Constructor for new branch
  public BESTree(Tree parent, int key, int order) {
    super(parent, key, order, 0f, termdiam);  // basic tree constructor
    Cbr = 0f;  // no branching initially
    erate = (float)ranG.nextGaussian();  // new variation
    // must have positive growth
    while((rCV*crate*erate) + crate <= 0)
      erate = (float)ranG.nextGaussian();  // new variation
  }



  // Get parameter values from data entry (class method)
  public static void updateParams() {

    B = Float.valueOf(DEparams[0][2]).floatValue();
    E = Float.valueOf(DEparams[1][2]).floatValue();
    S = Float.valueOf(DEparams[2][2]).floatValue();
    Vbr = Float.valueOf(DEparams[3][2]).floatValue();
    Vel = Float.valueOf(DEparams[4][2]).floatValue();
    rCV = Float.valueOf(DEparams[5][2]).floatValue();
    Tbstop = Double.valueOf(DEparams[6][2]).floatValue();
    somaL = Float.valueOf(DEparams[7][2]).floatValue();
    somadiam = Float.valueOf(DEparams[8][2]).floatValue();
    termdiam = Float.valueOf(DEparams[9][2]).floatValue();
    ebp = Float.valueOf(DEparams[10][2]).floatValue();

    Kbr = B / (float)Tbstop;
    pbr = 1f;
    Cnorm = 1f;

  }


  // Set time step (class method)
  public static void setTimeStep(float dt) {
    BESTree.dt = dt;
  }



  // Randomly branch all terminal nodes
  public void branchTree(double t) {
    int totTerms;

    // Calculate global probability component, if root node
    if (parent == null) {  // root node
      totTerms = countTerminals();
      // assume Cnorm previously set by updateTree()
      Cnorm = (float)totTerms / Cnorm;
      pbr = Cnorm / (float)Math.pow((double)totTerms, (double)E);
    };
    
    // Randomly branch terminal node (during branch phase only)
    if (t <= Tbstop && ltree == null && rtree == null) {
      // randomly branch terminal node
      Cbr = pbr*(float)Math.pow(2.0d,-(double)S*(double)order);
      if (Cbr*Kbr*dt >= ranp.nextDouble()) {
        this.ltree = new BESTree(this, Tree.brkey++, order+1);
        this.rtree = new BESTree(this, Tree.brkey++, order+1);
      };
    }
    else {  // search for terminals to branch
      if (ltree != null) ltree.branchTree(t);
      if (rtree != null) rtree.branchTree(t);
    };
      
  }



  // Elongate terminal nodes
  public void elongateTree(double t) {

    // Check for elongation phase (soma only)
    if (parent == null) {
      if (t > Tbstop) crate = Vel;  // elongation phase
      else crate = Vbr;  // branching phase
    };

    // Elongate terminal nodes
    if (ltree == null && rtree == null) {
      length = length + (((rCV*crate*erate) + crate) * dt);
    }
    else {  // search for terminals to elongate
      if (ltree != null) ltree.elongateTree(t);
      if (rtree != null) rtree.elongateTree(t);
    };
    
  }



  // Update normalisation constant
  public void updateTree(double t) {

    if (parent == null)
      Cnorm = 0f;
    if (ltree == null && rtree == null)
      Cnorm += (float)Math.pow(2.0d, -(double)S*(double)order);
    else {
      if (ltree != null) ltree.updateTree(t); 
      if (rtree != null) rtree.updateTree(t);
    };
      
  }



  // Get parameter value
  public float getValue(String vname) {

    if (vname.equals("Cbr"))
      return Cbr;
    else if (vname.equals("Cbrp"))
      return Cbrp;
    else
      return 0f;

  }


}
