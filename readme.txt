*******************************************************
*** Simulator for Models of Neurite Outgrowth ***
*******************************************************

Help Contents:

1. Author Details
2. System Requirements
3. Quick User Guide
4. Model Descriptions
5. References


-----------------------------------------
1. Author Details:
-----------------------------------------
Author: Bruce P. Graham, Department of Computing Science and Mathematics, 
University of Stirling, Scotland, U.K.
Email: b.graham@cs.stir.ac.uk
Web: www.cs.stir.ac.uk/~bpg/


-----------------------------------------
2. System Requirements:
-----------------------------------------
Should run on any system supporting Java 2.
Code provided as executable jar file (e.g. java -jar Neurite.jar).
Example parameter files in "Params" subdirectory.


-----------------------------------------
3. Quick User Guide:
-----------------------------------------

Button		Function
------		--------
Quit 		quit the simulator
Load 		load a set of simulation parameters
Save 		save a set of simulation parameters
Model 		select required model from drop-down menu and set model parameters
Simulation 	set simulation parameters e.g. number of trees, 
		simulation duration and time step
Display		set 2D visualisation parameters
Off/On 		turn 2D visualisation off or on (visualisation only occurs
		during creation of a single tree)
Plot 		turn on model parameter plotting (each selection creates new graph)
Construct 	start simulator to create required number of trees (neurites)
Stop 		terminate current simulation
Draw 		draw 2D visualisation of one tree from currently created
		set of trees ("Display" must be on)
Display tree 	index of tree visualised by "Draw"
Help 		shows this file


-----------------------------------------
4. Model Descriptions:
-----------------------------------------

The simulator currently contains three models of neurite outgrowth.

1. BESTL
--------
This is an implementation of van Pelt's stochastic model of
dendritic development, based on the description given in 
van Pelt and Uylings (1999). 
Example parameter files:
BESTL_PC23.par - rat cortical layer 2/3 pyramidal cell basal 
		 dendrites (van Pelt et al, 2001)
BESTL_PC5.par - rat corical layer 5 pyramidal cell basal 
	        dendrites (van Pelt & Uylings, 1999)
BESTL_nonPC.par - rat cortical layer 4 non-pyramidal cell 
		  dendrites (van Pelt et al, 2003)
BESTL_Pur.par - guinea pig Purkinje cell dendrites (van Pelt et al, 2001)

2. AD
-----
Biophysical model of neurite outgrowth described in Graham and 
van Ooyen (2004). In the model, branching depends on the 
concentration of a branch-determining substance in each terminal 
segment. The substance is produced in the cell body and is 
transported by active transport and diffusion to the terminals. 
The model reveals that transport-limited effects may give rise 
to the same modulation of branching as indicated by the 
stochastic BESTL model. Different limitations arise if transport 
is dominated by active transport or by diffusion. Example 
parameter files for reproducing the same trees as for the BESTL 
model are provided (see Figure 4 and Table 2 of Graham & 
van Ooyen, 2004).
Example parameter files: AD_PC23.par, AD_PC5.par, AD_nonPC.par, AD_Pur.par


3. ADcm
-------
Implementation of the AD model in "compartmental" form, allowing
calculation of spatial concentration profiles along the lengths 
of unbranched neurite segments. Compartmentalization follows 
"growth cone" scheme of Graham and van Ooyen (2001) in which a 
compartment immediately preceding a terminal (or "growth cone") 
compartment elongates as the neurite grows. All other 
compartments have fixed length. Elongating compartments are 
split into two when their length reaches twice the length of 
other compartments. A branching event results in a growth cone 
compartment being replaced by four new compartments, consisting 
of a new growth cone and one preceding compartment for the two 
new daughter branches.
Concentration gradients are most obvious when transport is by 
slow diffusion.
Example parameter file: ADcm_D600.par


-----------------------------------------
5. References:
-----------------------------------------

Graham, B.P. & van Ooyen, A., Compartmental models of growing 
neurites, Neurocomputing 38-40:31-36, 2001

Graham, B.P. & van Ooyen, A., Transport limited effects in a 
model of dendritic branching, Journal of Theoretical 
Neurobiology 230:421-432, 2004

van Pelt, J. & Uylings, H.B.M., Natural variability in the 
geometry of dendritic branching patterns, Chapt. 4 in "Modeling
in the Neurosciences: From Ionic Channels to Neural Networks", 
Poznanski, R.R. (ed.), Harwood Academic, pp79-108, 1999

van Pelt, J., van Ooyen, A. & Uylings, H.B.M.,, Modeling 
dendritic geometry and the development of nerve connections, 
Chapt. 7 in "Computational Neuroscience: Realistic Modeling for 
Experimentalists", De Schutter, E. (ed.), CRC Press, pp179-208, 
2001

van Pelt, J., Graham, B.P. and Uylings, H.B.M., Formation of 
dendritic branching patterns, Chapt. 4 in "Modeling Neural 
Development", van Ooyen, A. (ed.), MIT Press, pp75-94, 2003.



