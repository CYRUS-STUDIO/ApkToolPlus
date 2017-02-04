/*
    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public
    License as published by the Free Software Foundation; either
    version 2 of the license, or (at your option) any later version.
*/

package ee.ioc.cs.jbe.browser.detail.attributes;

import org.gjt.jclasslib.structures.attributes.ConstantValueAttribute;
import org.gjt.jclasslib.util.ExtendedJLabel;

import ee.ioc.cs.jbe.browser.BrowserServices;
import ee.ioc.cs.jbe.browser.detail.FixedListDetailPane;


import javax.swing.tree.TreePath;

/**
    Detail pane showing a <tt>ConstantValue</tt> attribute.
 
    @author <a href="mailto:jclasslib@ej-technologies.com">Ingo Kegel</a>
    @version $Revision: 1.3 $ $Date: 2006/01/02 17:57:03 $
*/
public class ConstantValueAttributeDetailPane extends FixedListDetailPane {

    // Visual components
    
    private ExtendedJLabel lblValue;
    private ExtendedJLabel lblVerbose;
    
    /**
        Constructor.
        @param services the associated browser services.
     */
    public ConstantValueAttributeDetailPane(BrowserServices services) {
        super(services);
    }
    
    protected void setupLabels() {
        
        addDetailPaneEntry(normalLabel("Constant value index:"),
                           lblValue = linkLabel(),
                           lblVerbose = highlightLabel());
    }

    public void show(TreePath treePath) {
        
        ConstantValueAttribute attribute = (ConstantValueAttribute)findAttribute(treePath);

        constantPoolHyperlink(lblValue,
                              lblVerbose,
                              attribute.getConstantvalueIndex());
        
        super.show(treePath);
    }
    
}

