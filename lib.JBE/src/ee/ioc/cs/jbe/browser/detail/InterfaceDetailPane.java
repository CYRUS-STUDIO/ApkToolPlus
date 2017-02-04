/*
    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public
    License as published by the Free Software Foundation; either
    version 2 of the license, or (at your option) any later version.
*/

package ee.ioc.cs.jbe.browser.detail;

import org.gjt.jclasslib.util.ExtendedJLabel;

import ee.ioc.cs.jbe.browser.BrowserServices;


import javax.swing.tree.TreePath;

/**
    Detail pane showing interface entries.
 
    @author <a href="mailto:jclasslib@ej-technologies.com">Ingo Kegel</a>
    @version $Revision: 1.4 $ $Date: 2006/09/25 16:00:58 $
*/
public class InterfaceDetailPane extends FixedListDetailPane {

    // Visual components
    
    private ExtendedJLabel lblInterface;
    private ExtendedJLabel lblInterfaceVerbose;
    
    /**
        Constructor.
        @param services the associated browser services.
     */
    public InterfaceDetailPane(BrowserServices services) {
        super(services);
    }
    
    protected void setupLabels() {
        
        addDetailPaneEntry(normalLabel("Interface:"),
                           lblInterface = linkLabel(),
                           lblInterfaceVerbose = highlightLabel());

    }

    public void show(TreePath treePath) {
        
        int constantPoolIndex = services.getClassFile().getInterfaces()[getIndex(treePath)];
        
        constantPoolHyperlink(lblInterface,
                              lblInterfaceVerbose,
                              constantPoolIndex);
        
        super.show(treePath);
        
    }
    
}

