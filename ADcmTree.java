/* ADcmTree.java

Implements "compartmental" version of AD model
  - uses "growth cone" scheme of Graham and van Ooyen, Neurocomputing 2001 

Version 1 BPG 4-3-03
*/

import java.lang.Math;
import java.util.Random;


// New tree class
public class ADcmTree extends Tree {

  // New class variables
  static float Kbr=1.0f;  // scale factor for branching probability
  static float dt=1.0f;  // time step
  static float dL=1.0f;  // length increment
  static float gcL=1.0f;  // growth cone length
  static float a=0f;	// active transport rate
  static float D=1.0f;  // diffusion constant
  static float ICbr=1.0f;  // production rate
  static float gsCbr=1.0f; // soma uptake rate
  static float gtCbr=1.0f; // terminal uptake rate
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
    {"Branching scale factor (Kbr):", "0.01925", "0.01925"},
    {"Diffusion rate (D):", "0", "0"},
    {"Active transport rate (a):", "100", "100"},
    {"Soma production rate (I):", "10", "10"},
    {"Soma uptake rate (G0):", "9", "9"},
    {"Terminal uptake rate (Gn):", "100", "100"},
    {"Compartment length (dL):", "1.0", "1.0"},
    {"Growth cone length (gcL):", "1.0", "1.0"},
    {"Branching phase rate (Vbr):", "0.22", "0.22"},
    {"Elongation phase rate (Vel):", "0.51", "0.51"},
    {"Rate coefficient of variation (rCV):", "0.28", "0.28"},
    {"Time branching stops (Tbstop):", "264", "264"},
    {"Soma length:", "10.0", "10.0"},
    {"Soma diameter:", "10.0", "10.0"},
    {"Terminal diameter:", "1.0", "1.0"},
    {"Branch power (e):", "8", "8"}};
  public static final int Nparams=16;

  // random number generators for branching and elongation
  static Random ranG=null;  // Gaussian random number
  static Random ranp=null;  // Uniform random number
 
  // Instance variables
  float Cbr;	// conc. of branch-producing chemical
  float Cbrp;	// value at previous time step
  float erate; // variation in elongation rate
  float deltL;	// change in length



  // Constructor for cell body
  public ADcmTree(int key, int order) {
    super(null, key, order, somaL, somadiam);  // basic tree constructor
    
    ranp = new Random();  // randomness for branching
    ranG = new Random();  // randomness for elongation rates
    
    updateParams();  // get parameter values from data entry
    Cbr = ICbr/(gsCbr+gtCbr);  // steady-state initially
    Cbrp = Cbr;
    crate = Vbr;  // branching phase mean elongation rate
    deltL = 0;
    pathlength = 0;
    erate = (float)ranG.nextGaussian();  // new variation
    // must have positive growth
    while((rCV*crate*erate) + crate <= 0)
      erate = (float)ranG.nextGaussian();  // new variation
    
    // add first neurite segment (compartment+GC) as left branch
    this.ltree = new ADcmTree(this, key+1, order, Cbr);
    Tree.brkey = key+2;  // reset branch key
  }


  // Constructor for new branch (GC plus one compartment)
  public ADcmTree(Tree parent, int key, int order, float C0) {
    super(parent, key, order, dL, termdiam);  // basic tree constructor
    Cbr = C0;  // initial branching chemical
    Cbrp = Cbr;
    erate = (float)ranG.nextGaussian();  // new variation
    // must have positive growth
    while((rCV*crate*erate) + crate <= 0)
      erate = (float)ranG.nextGaussian();  // new variation
    // add growth cone (actual terminal)
    ltree = new ADcmTree(this,key,order,diam,gcL,Cbr,erate);
  }


  // Constructor for single compartment (continuation of branch)
  public ADcmTree(Tree parent, int key, int order, float dm0, float L0, float C0, float er0) {
    super(parent, key, order, L0, dm0);  // basic tree constructor
    Cbr = C0;
    Cbrp = Cbr;
    erate = er0;
    deltL = 0;
    if (parent != null)
      pathlength = parent.pathlength+L0;
    else
      pathlength = 0;
  }



  // Get parameter values from data entry (class method)
  public static void updateParams() {

    Kbr = Float.valueOf(DEparams[0][2]).floatValue();
    D = Float.valueOf(DEparams[1][2]).floatValue();
    a = Float.valueOf(DEparams[2][2]).floatValue();
    ICbr = Float.valueOf(DEparams[3][2]).floatValue();
    gsCbr = Float.valueOf(DEparams[4][2]).floatValue();
    gtCbr = Float.valueOf(DEparams[5][2]).floatValue();
    dL = Float.valueOf(DEparams[6][2]).floatValue();
    gcL = Float.valueOf(DEparams[7][2]).floatValue();
    Vbr = Float.valueOf(DEparams[8][2]).floatValue();
    Vel = Float.valueOf(DEparams[9][2]).floatValue();
    rCV = Float.valueOf(DEparams[10][2]).floatValue();
    Tbstop = Double.valueOf(DEparams[11][2]).floatValue();
    somaL = Float.valueOf(DEparams[12][2]).floatValue();
    somadiam = Float.valueOf(DEparams[13][2]).floatValue();
    termdiam = Float.valueOf(DEparams[14][2]).floatValue();
    ebp = Float.valueOf(DEparams[15][2]).floatValue();

  }


  // Set time step (class method)
  public static void setTimeStep(float dt) {
    ADcmTree.dt = dt;
  }



  // Randomly branch all terminal nodes
  public void branchTree(double t) {
    // new segments start with compartment (dL) + GC (gcL)
    // concentrations measured at centre of each compartment

    float Td=0, Ta=0;  // diffusive and active transfer

    // Diffusive and active transport
    
    // Diffusive transfer
    Td = 0;
    if (parent != null)  // transfer from parent
      Td = diam*diam*(parent.getValue("Cbrp") - Cbrp) / (length+parent.length);
    // subtract transfer into child branches
    if (ltree != null)
      Td = Td - (ltree.diam*ltree.diam*(Cbrp-ltree.getValue("Cbrp"))/(length+ltree.length));
    if (rtree != null)
      Td = Td - (rtree.diam*rtree.diam*(Cbrp-rtree.getValue("Cbrp"))/(length+rtree.length)); 
    
    // Active transport
    Ta = 0;
    if (parent != null)  // transfer from parent
      Ta = Ta + parent.getValue("Cbrp")*diam*diam;
    // subtract transfer into child branches
    if (ltree != null)
      Ta = Ta - Cbrp*ltree.diam*ltree.diam;
    if (rtree != null)
      Ta = Ta - Cbrp*rtree.diam*rtree.diam; 
    
    // calculate new concentrations
    Td = (2*Td) / (diam*diam*length);
    Ta = Ta / (diam*diam*length);
    Cbr = Cbrp + (dt*((D*Td)+(a*Ta)));
    
    // Production
    if (parent == null)  // soma
      Cbr = Cbr + (dt*(ICbr-(gsCbr*Cbrp)));  // production - decay
      
    // Decay
    if (ltree == null && rtree == null)  // terminal
      Cbr = Cbr - (dt*gtCbr*Cbrp);  // decay
      
    if (Cbr < 0) Cbr = 0;  // cannot have negative concentrations
    
    // Randomly branch terminal node (during branch phase only)
    if (t <= Tbstop && ltree == null && rtree == null) {
      // randomly branch terminal node
      if (Cbr*Kbr*dt >= ranp.nextDouble()) {
	// this compartment is removed and its contents distributed
	// to its children
        // calculate relative volume and concentration
        float rvol = diam*diam*length/(termdiam*termdiam*(dL+gcL));
        float newC = Cbr*0.5f*rvol;
        // create new branches
        parent.ltree = new ADcmTree(parent, Tree.brkey++, order+1, newC);
        parent.rtree = new ADcmTree(parent, Tree.brkey++, order+1, newC);
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

    // Elongation (if child is growth cone)
    if (parent != null && ltree != null && ltree.ltree == null) {  
      float newL = length + ltree.getValue("deltL");
      // adjust concentration to new length
      Cbr = Cbr * length / newL;
      length = newL;
      pathlength = parent.pathlength + length;
      ltree.pathlength = pathlength + ltree.length;
      if (length >= 2*dL) {  // add new compartment
        pathlength = parent.pathlength + dL;
        Tree gcone = ltree;
        ltree = new ADcmTree(this,key,order,diam,length-dL,Cbr,erate);
        ltree.ltree = gcone;
        gcone.parent = ltree;
        length = dL;
      };
    };

    // Calculate elongation if growth cone (terminal node)
    if (parent != null && ltree == null && rtree == null) {
      deltL = ((rCV*crate*erate) + crate) * dt;
      }
    else {  // search for terminals to elongate
      if (ltree != null) ltree.elongateTree(t);
      if (rtree != null) rtree.elongateTree(t);
    };
    
  }



  // Set segment diameters
  public void diamTree(double t) {

    if ((ltree == null && rtree == null) || ebp == 0) 
      return;  // diameter already set
    else {
      if (ltree != null) ltree.diamTree(t);
      if (rtree != null) rtree.diamTree(t);
      if (parent != null) { // do not adjust soma!
        float diamp = diam;
        diam = termdiam*(float)Math.pow((double)countTerminals(),(double)(1/ebp));
        Cbr = Cbr * ((diamp*diamp) / (diam*diam));
        Cbrp = Cbrp * ((diamp*diamp) / (diam*diam));
      };
    };
      

  }



  // Update previous concentration values
  public void updateTree(double t) {

    Cbrp = Cbr;
    if (ltree != null) ltree.updateTree(t); 
    if (rtree != null) rtree.updateTree(t); 
      
  }



  // Get parameter value
  public float getValue(String vname) {

    if (vname.equals("Cbr"))
      return Cbr;
    else if (vname.equals("Cbrp"))
      return Cbrp;
    else if (vname.equals("pathlength"))
      return pathlength;
    else if (vname.equals("deltL"))
      return deltL;
    else
      return 0f;

  }


}
