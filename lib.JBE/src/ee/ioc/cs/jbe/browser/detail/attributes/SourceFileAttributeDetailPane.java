/*
    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public
    License as published by the Free Software Foundation; either
    version 2 of the license, or (at your option) any later version.
*/

package ee.ioc.cs.jbe.browser.detail.attributes;

import org.gjt.jclasslib.structures.attributes.SourceFileAttribute;
import org.gjt.jclasslib.util.ExtendedJLabel;

import ee.ioc.cs.jbe.browser.BrowserServices;
import ee.ioc.cs.jbe.browser.detail.FixedListDetailPane;


import javax.swing.tree.TreePath;

/**
    Detail pane showing a <tt>SourceFile</tt> attribute.

    @author <a href="mailto:jclasslib@ej-technologies.com">Ingo Kegel</a>
    @version $Revision: 1.3 $ $Date: 2006/01/02 17:57:03 $
*/
public class SourceFileAttributeDetailPane extends FixedListDetailPane {

    // Visual components
    
    private ExtendedJLabel lblSourceFile;
    private ExtendedJLabel lblSourceFileVerbose;
    
    /**
        Constructor.
        @param services the associated browser services.
     */
    public SourceFileAttributeDetailPane(BrowserServices services) {
        super(services);
    }
    
    protected void setupLabels() {
        
        addDetailPaneEntry(normalLabel("Source file name index:"),
                           lblSourceFile = linkLabel(),
                           lblSourceFileVerbose = highlightLabel());
    }

    public void show(TreePath treePath) {
        
        SourceFileAttribute attribute = (SourceFileAttribute)findAttribute(treePath);

        constantPoolHyperlink(lblSourceFile,
                              lblSourceFileVerbose,
                              attribute.getSourcefileIndex());
        
        super.show(treePath);
    }
    
}

