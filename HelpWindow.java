/* HelpWindow.java

General purpose "help" text displayer

BPG 20-11-05
*/

import java.awt.*;
import java.awt.event.*;
import java.io.*;


public class HelpWindow extends Frame implements ActionListener {


	private Button closeB;
	
	
	public HelpWindow() {
		// Frame properties
        setTitle("Neurite User Guide");
        setSize(700, 550);
        setLocation(100, 100);
        setResizable(true);
        setLayout(new BorderLayout());

        // Read "help" text from file
        BufferedReader inFile;
        String text = "";
		try {

  	      inFile = new BufferedReader(new FileReader("readme.txt"));
  	      String line = inFile.readLine();
  	      text = line;
  	      
  	      while ((line = inFile.readLine()) != null) {
  	      	text = text.concat("\n");
  	      	text = text.concat(line);
  	      }
  	      // Close the file
  	      inFile.close();
  		}
  		catch(Exception ex) {
  			System.out.println("Error: Unable to open or read the help file.");
  		}
  		
	    // Create text pane to display "help"  
        TextArea pane = new TextArea(text, 500, 1, TextArea.SCROLLBARS_VERTICAL_ONLY);
        pane.setEditable(false);
        pane.setBounds(5, 5, 5, 5);
	    // Set invisible cursor back to first position in pane to ensure 
	    // that the first line is displayed first to user
  		add(pane, BorderLayout.CENTER);
  		
  		// "Close" button
  		closeB = new Button("Close Window");
  		closeB.addActionListener(this);
  		Panel buttonP = new Panel();
  		buttonP.add(closeB);
  		  		add(buttonP, BorderLayout.SOUTH);
  		
  		// display the frame window
		setVisible(true);
		

	}
	
	public void actionPerformed(ActionEvent e) {
		this.setVisible(false);
	}
	

}
