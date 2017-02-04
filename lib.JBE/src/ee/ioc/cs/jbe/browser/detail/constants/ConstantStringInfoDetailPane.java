/*
    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public
    License as published by the Free Software Foundation; either
    version 2 of the license, or (at your option) any later version.
*/

package ee.ioc.cs.jbe.browser.detail.constants;

import org.gjt.jclasslib.structures.InvalidByteCodeException;
import org.gjt.jclasslib.structures.constants.ConstantStringInfo;
import org.gjt.jclasslib.util.ExtendedJLabel;

import ee.ioc.cs.jbe.browser.BrowserServices;


import javax.swing.tree.TreePath;

/**
    Detail pane showing a <tt>CONSTANT_String</tt> constant pool entry.

    @author <a href="mailto:jclasslib@ej-technologies.com">Ingo Kegel</a>
    @version $Revision: 1.4 $ $Date: 2006/09/25 16:00:58 $
*/
public class ConstantStringInfoDetailPane extends AbstractConstantInfoDetailPane {

    // Visual components
    
    private ExtendedJLabel lblString;
    private ExtendedJLabel lblStringVerbose;
    
    /**
        Constructor.
        @param services the associated browser services.
     */
    public ConstantStringInfoDetailPane(BrowserServices services) {
        super(services);
    }
    
    protected void setupLabels() {
        
        addDetailPaneEntry(normalLabel("String:"),
                           lblString = linkLabel(),
                           lblStringVerbose = highlightLabel());
    }

    public void show(TreePath treePath) {
        
        int constantPoolIndex = constantPoolIndex(treePath);

        try {
            ConstantStringInfo entry = (ConstantStringInfo)services.getClassFile().getConstantPoolEntry(constantPoolIndex, ConstantStringInfo.class);

            constantPoolHyperlink(lblString,
                                  lblStringVerbose,
                                  entry.getStringIndex());
        
        } catch (InvalidByteCodeException ex) {
            lblStringVerbose.setText(MESSAGE_INVALID_CONSTANT_POOL_ENTRY);
        }
        
        super.show(treePath);
    }
    
}

