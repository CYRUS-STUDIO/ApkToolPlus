/*
    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public
    License as published by the Free Software Foundation; either
    version 2 of the license, or (at your option) any later version.
*/
package ee.ioc.cs.jbe.browser.detail.attributes;

import org.gjt.jclasslib.structures.attributes.SignatureAttribute;
import org.gjt.jclasslib.util.ExtendedJLabel;

import ee.ioc.cs.jbe.browser.BrowserServices;
import ee.ioc.cs.jbe.browser.detail.FixedListDetailPane;


import javax.swing.tree.TreePath;

/**
 * Detail pane showing a <tt>Signature</tt> attribute.
 *
 * @author <a href="mailto:vitor.carreira@gmail.com">Vitor Carreira</a>
 * @version $Revision: 1.3 $ $Date: 2006/01/02 17:57:03 $
 */
public class SignatureAttributeDetailPane extends FixedListDetailPane {

    // Visual components
    private ExtendedJLabel lblSignature;
    private ExtendedJLabel lblSignatureVerbose;

    /**
     * Constructor.
     *
     * @param services the associated browser services.
     */
    public SignatureAttributeDetailPane(BrowserServices services) {
        super(services);
    }

    protected void setupLabels() {
        addDetailPaneEntry(normalLabel("Signature index:"),
                lblSignature = linkLabel(),
                lblSignatureVerbose = highlightLabel());
    }

    public void show(TreePath treePath) {
        SignatureAttribute attribute = (SignatureAttribute)findAttribute(treePath);
        constantPoolHyperlink(lblSignature,
                lblSignatureVerbose,
                attribute.getSignatureIndex());
        super.show(treePath);
    }
}
