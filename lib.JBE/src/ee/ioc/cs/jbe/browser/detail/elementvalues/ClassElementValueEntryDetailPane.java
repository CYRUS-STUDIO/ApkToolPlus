/*
    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public
    License as published by the Free Software Foundation; either
    version 2 of the license, or (at your option) any later version.
*/
package ee.ioc.cs.jbe.browser.detail.elementvalues;

import org.gjt.jclasslib.structures.elementvalues.ClassElementValue;
import org.gjt.jclasslib.util.ExtendedJLabel;

import ee.ioc.cs.jbe.browser.BrowserServices;
import ee.ioc.cs.jbe.browser.BrowserTreeNode;
import ee.ioc.cs.jbe.browser.detail.FixedListDetailPane;


import javax.swing.tree.TreePath;

/**
 * Class for showing element value entry of type Class.
 *
 * @author <a href="mailto:vitor.carreira@gmail.com">Vitor Carreira</a>
 * @version $Revision: 1.4 $ $Date: 2006/09/25 16:00:58 $
 */
public class ClassElementValueEntryDetailPane extends FixedListDetailPane {

    private ExtendedJLabel lblClassInfoIndex;
    private ExtendedJLabel lblClassInfoIndexVerbose;

    public ClassElementValueEntryDetailPane(BrowserServices services) {
        super(services);
    }

    protected void setupLabels() {

        addDetailPaneEntry(normalLabel("Class info:"),
                lblClassInfoIndex = linkLabel(),
                lblClassInfoIndexVerbose = highlightLabel());
    }

    public void show(TreePath treePath) {
        ClassElementValue ceve = (ClassElementValue)
                ((BrowserTreeNode)treePath.getLastPathComponent()).getElement();

        constantPoolHyperlink(lblClassInfoIndex,
                lblClassInfoIndexVerbose,
                ceve.getClassInfoIndex());

        super.show(treePath);
    }

}
