/* ADTree.java

Implements "diffusion + active transport" model of branching
  - branching due to concentration of branching chemical C in terminal tip (growth cone)
    - production in soma
    - decay in soma and growth cone
    - diffusion along growing neurite
    - active transport along neurite
  - section-level model
    - conc. measured in fixed volume at end of every neurite
      section (unbranched length of neurite)
      - volume specified by length dL
    - two new branches of length minL and containing no
      chemical are created on branching
      
Version 1.0 BPG 29-3-03
  - implements model as described in Graham and van Ooyen, 
    Journal of Theoretical Biology 230:421-432, 2004
  
*/

import java.lang.Math;
import java.util.Random;


// New tree class
public class ADTree extends Tree {

  // New class variables
  static float Kbr=1.0f;  // scale factor for branching probability
  static float dt=1.0f;  // time step
  static float dL=1.0f;  // length step
  static float minL=5.0f;  // min diffusion length
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
  static double Tbstop=1.0f;  // branching stop time
  static int ntot=1;	// total number of terminals in growing tree

  // data entry
  public static String DEparams[][] = {
    {"Branching scale factor (Kbr):", "0.01925", "0.01925"},
    {"Diffusion rate (D):", "0", "0"},
    {"Active transport rate (a):", "100", "100"},
    {"Soma production rate (I):", "10", "10"},
    {"Soma uptake rate (G0):", "9", "9"},
    {"Terminal uptake rate (Gn):", "100", "100"},
    {"Conc. calc. length (dL):", "1.0", "1.0"},
    {"Minimum length (minL):", "5.0", "5.0"},
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



  // Constructor for cell body
  public ADTree(int key, int order) {
    super(null, key, order, somaL, somadiam);  // basic tree constructor
    
    ranp = new Random();  // randomness for branching
    ranG = new Random();  // randomness for elongation rates
    
    updateParams();  // get parameter values from data entry
    Cbr = ICbr/(gsCbr+gtCbr);  // steady-state initially
    Cbrp = Cbr;
    crate = Vbr;  // branching phase mean elongation rate
    erate = (float)ranG.nextGaussian();  // new variation
    // must have positive growth
    while((rCV*crate*erate) + crate <= 0)
      erate = (float)ranG.nextGaussian();  // new variation
    
    // add first neurite segment as left branch
    this.ltree = new ADTree(this, key+1, Cbr);
    Tree.brkey = key+2;  // reset branch key
  }


  // Constructor for first neurite branch
  public ADTree(Tree parent, int key, float Cinit) {
    super(parent, key, 0, minL, termdiam);  // basic tree constructor
    Cbr = Cinit;  // no branching chemical initially
    Cbrp = Cbr;
    erate = (float)ranG.nextGaussian();  // new variation
    // must have positive growth
    while((rCV*crate*erate) + crate <= 0)
      erate = (float)ranG.nextGaussian();  // new variation
  }


  // Constructor for new branch
  public ADTree(Tree parent, int key, int order) {
    super(parent, key, order, minL, termdiam);  // basic tree constructor
    Cbr = 0;  // no branching chemical initially
    Cbrp = Cbr;
    erate = (float)ranG.nextGaussian();  // new variation
    // must have positive growth
    while((rCV*crate*erate) + crate <= 0)
      erate = (float)ranG.nextGaussian();  // new variation
  }


  // Constructor for single compartment (continuation of branch)
  public ADTree(Tree parent, int key, int order, float erate) {
    super(parent, key, order, minL, termdiam);  // basic tree constructor
    Cbr = 0;
    Cbrp = Cbr;
    this.erate = erate;  // parental variation
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
    minL = Float.valueOf(DEparams[7][2]).floatValue();
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
    ADTree.dt = dt;
  }



  // Randomly branch all terminal nodes
  public void branchTree(double t) {
    // new segments start with length minL
    // concentrations measured in volume of length dL at
    // end of each segment

    float Td=0, Ta=0;  // diffusive and active transfer

    // Diffusion and active transport
    
    // diffusion into segment
    if (parent == null) {  // soma
      if (ltree.length >= minL)
	     Td = ltree.diam*ltree.diam*(ltree.getValue("Cbrp") - Cbrp) / (((length-dL)/2)+ltree.length); 
    }
    else if (parent.parent == null && length >= minL) {  // parent is soma
      Td = diam*diam*(parent.getValue("Cbrp") - Cbrp) / (((parent.length-dL)/2)+length);
    }
    else if (length >= minL) {
      Td = diam*diam*(parent.getValue("Cbrp") - Cbrp) / length;
    };
    
    // diffusive transfer into child branches
    // (all compartments have length at least minL)
    if (parent != null) {  // not soma
      if (ltree != null)
        if (ltree.length >= minL)
	       Td = Td - (ltree.diam*ltree.diam*(Cbrp-ltree.getValue("Cbrp"))/ltree.length);
      if (rtree != null)
        if (rtree.length >= minL)
	       Td = Td - (rtree.diam*rtree.diam*(Cbrp-rtree.getValue("Cbrp"))/rtree.length);
    };
    
    // active transport to children
    if (parent == null)  // soma
      Ta = -Cbrp*ltree.diam*ltree.diam;
    else if (parent != null && ltree != null) // not soma or terminal
      Ta = -Cbrp*(ltree.diam*ltree.diam + rtree.diam*rtree.diam);

    // active transport from parent
    if (parent != null) // not soma
      Ta = Ta + parent.getValue("Cbrp")*diam*diam;
    
    // calculate new concentrations
    Td = Td / (diam*diam*dL);
    Ta = Ta / (diam*diam*dL);
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
      if (Cbr*Kbr*dt >= ranp.nextDouble()) {
        this.ltree = new ADTree(this, Tree.brkey++, order+1);
        this.rtree = new ADTree(this, Tree.brkey++, order+1);
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
    else
      return 0f;

  }


}
