/*
    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public
    License as published by the Free Software Foundation; either
    version 2 of the license, or (at your option) any later version.
*/

package ee.ioc.cs.jbe.browser.detail.attributes;

import org.gjt.jclasslib.structures.AttributeInfo;
import org.gjt.jclasslib.util.ExtendedJLabel;

import ee.ioc.cs.jbe.browser.BrowserServices;
import ee.ioc.cs.jbe.browser.detail.FixedListDetailPane;


import javax.swing.tree.TreePath;

/**
    Detail pane showing the generic information which applies to all attribute.

    @author <a href="mailto:jclasslib@ej-technologies.com">Ingo Kegel</a>
    @version $Revision: 1.5 $ $Date: 2006/09/04 15:43:18 $
*/
public class GenericAttributeDetailPane extends FixedListDetailPane {

    // Visual components
    
    private ExtendedJLabel lblNameIndex;
    private ExtendedJLabel lblLength;
    
    /**
        Constructor.
        @param services the associated browser services.
     */
    public GenericAttributeDetailPane(BrowserServices services) {
        super(services);
    }
    
    protected void setupLabels() {
        
        addDetailPaneEntry(normalLabel("Attribute name index:"),
                           lblNameIndex = linkLabel(),
                           null);

        addDetailPaneEntry(normalLabel("Attribute length:"),
                           lblLength = highlightLabel());
    
    }

    public void show(TreePath treePath) {
        
        AttributeInfo attribute = findAttribute(treePath);

        constantPoolHyperlink(lblNameIndex,
                              null,
                              attribute.getAttributeNameIndex());
        
        lblLength.setText(attribute.getAttributeLength());
        
        super.show(treePath);
    }
    
}

