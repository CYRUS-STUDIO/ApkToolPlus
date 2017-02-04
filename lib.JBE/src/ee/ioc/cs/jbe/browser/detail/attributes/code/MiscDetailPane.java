/*
    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public
    License as published by the Free Software Foundation; either
    version 2 of the license, or (at your option) any later version.
*/

package ee.ioc.cs.jbe.browser.detail.attributes.code;

import javax.swing.JTextField;
import javax.swing.tree.TreePath;

import org.gjt.jclasslib.util.ExtendedJLabel;

import ee.ioc.cs.jbe.browser.BrowserServices;
import ee.ioc.cs.jbe.browser.codeedit.InputFieldException;
import ee.ioc.cs.jbe.browser.detail.FixedListDetailPane;


/**
    Detail pane showing miscellaneous information of a <tt>Code</tt> attribute
    without substructure.
 
    @author <a href="mailto:jclasslib@ej-technologies.com">Ingo Kegel</a>
    @author Ando Saabas
    @version $Revision: 1.8 $ $Date: 2006/09/04 15:43:18 $

    Edited by Ando Saabas
*/
public class MiscDetailPane extends FixedListDetailPane {

    
    private ExtendedJLabel lblCodeLength;
	private JTextField maxLocalsField;
	private JTextField maxStackField;
    
    /**
        Constructor.
        @param services the associated browser services.
     */
    public MiscDetailPane(BrowserServices services) {
        super(services);
    }
    


	 protected void setupLabels() {
        maxLocalsField =  new JTextField(4);
        maxStackField  = new JTextField(4);
        addDetailPaneEntry(normalLabel("Maximum stack depth:"),
                           maxStackField);

        addDetailPaneEntry(normalLabel("Maximum local variables:"),
                           maxLocalsField);

        addDetailPaneEntry(normalLabel("Code length:"),
                           lblCodeLength = highlightLabel());
        
    }

    public void show(TreePath treePath) {
      
        super.show(treePath);
    }



	public void setMaxStack(int maxStack) {
		maxStackField.setText(Integer.toString(maxStack));
		
	}



	public void setMaxLocals(int maxLocals) {
		maxLocalsField.setText(Integer.toString(maxLocals));
		
	}



	public void setCodeLength(int codeLength) {
		lblCodeLength.setText(codeLength);
		
	}



	public int getMaxStack() throws InputFieldException{
        try {
            int i = Integer.parseInt(maxStackField.getText());
            return i;
        } catch (NumberFormatException nfe) {
            throw new InputFieldException(maxStackField.getText(), "Maximum stack");
        }
        
	}



	public int getMaxLocals()  throws InputFieldException{
        try {
            int i = Integer.parseInt(maxLocalsField.getText());
            return i;
        } catch (NumberFormatException nfe) {
            throw new InputFieldException(maxLocalsField.getText(), "Maximum locals");
        }
	}
    
    
}

