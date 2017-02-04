/*
    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public
    License as published by the Free Software Foundation; either
    version 2 of the license, or (at your option) any later version.
*/
package ee.ioc.cs.jbe.browser.detail;

import org.gjt.jclasslib.structures.elementvalues.ElementValue;
import org.gjt.jclasslib.structures.elementvalues.ElementValuePair;
import org.gjt.jclasslib.util.ExtendedJLabel;

import ee.ioc.cs.jbe.browser.BrowserServices;
import ee.ioc.cs.jbe.browser.BrowserTreeNode;


import javax.swing.tree.TreePath;

/**
 * Class for showing an element value pair node.
 *
 * @author <a href="mailto:vitor.carreira@gmail.com">Vitor Carreira</a>
 * @version $Revision: 1.4 $ $Date: 2006/09/25 16:00:58 $
 */
public class ElementValuePairDetailPane extends FixedListDetailPane {

    private ExtendedJLabel lblElementName;
    private ExtendedJLabel lblElementNameVerbose;

    private ExtendedJLabel lblValueTag;
    private ExtendedJLabel lblValueTagVerbose;

    public ElementValuePairDetailPane(BrowserServices services) {
        super(services);
    }

    protected void setupLabels() {
        addDetailPaneEntry(normalLabel("Element name:"),
                lblElementName = linkLabel(),
                lblElementNameVerbose = highlightLabel());
        addDetailPaneEntry(normalLabel("Value tag:"),
                lblValueTag = highlightLabel(),
                lblValueTagVerbose = highlightLabel());

    }

    public void show(TreePath treePath) {
        ElementValuePair evp = (ElementValuePair)
                ((BrowserTreeNode)treePath.getLastPathComponent()).getElement();


        constantPoolHyperlink(lblElementName,
                lblElementNameVerbose,
                evp.getElementNameIndex());

        lblValueTag.setText(String.valueOf((char)evp.getElementValue().getTag()));
        lblValueTagVerbose.setText("<" + ElementValue.getTagDescription(evp.getElementValue().getTag()) + ">");

        super.show(treePath);
    }

}
