package ee.ioc.cs.jbe.browser.detail.elementvalues;
/*
    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public
    License as published by the Free Software Foundation; either
    version 2 of the license, or (at your option) any later version.
*/

import org.gjt.jclasslib.structures.elementvalues.ConstElementValue;
import org.gjt.jclasslib.util.ExtendedJLabel;

import ee.ioc.cs.jbe.browser.BrowserServices;
import ee.ioc.cs.jbe.browser.BrowserTreeNode;
import ee.ioc.cs.jbe.browser.detail.FixedListDetailPane;


import javax.swing.tree.TreePath;

/**
 * Class for showing element value entry of type Constant.
 *
 * @author <a href="mailto:vitor.carreira@gmail.com">Vitor Carreira</a>
 * @version $Revision: 1.4 $ $Date: 2006/09/25 16:00:58 $
 */
public class ConstElementValueEntryDetailPane extends FixedListDetailPane {

    private ExtendedJLabel lblIndex;
    private ExtendedJLabel lblIndexVerbose;

    public ConstElementValueEntryDetailPane(BrowserServices services) {
        super(services);
    }

    protected void setupLabels() {

        addDetailPaneEntry(normalLabel("Constant value:"),
                lblIndex = linkLabel(),
                lblIndexVerbose = highlightLabel());
    }

    public void show(TreePath treePath) {
        ConstElementValue ceve = (ConstElementValue)
                ((BrowserTreeNode)treePath.getLastPathComponent()).getElement();

        constantPoolHyperlink(lblIndex,
                lblIndexVerbose,
                ceve.getConstValueIndex());

        super.show(treePath);
    }
}
