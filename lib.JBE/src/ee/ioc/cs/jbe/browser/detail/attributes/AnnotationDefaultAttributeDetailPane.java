/*
    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public
    License as published by the Free Software Foundation; either
    version 2 of the license, or (at your option) any later version.
*/
package ee.ioc.cs.jbe.browser.detail.attributes;

import org.gjt.jclasslib.structures.attributes.AnnotationDefaultAttribute;
import org.gjt.jclasslib.structures.elementvalues.ElementValue;
import org.gjt.jclasslib.util.ExtendedJLabel;

import ee.ioc.cs.jbe.browser.BrowserServices;
import ee.ioc.cs.jbe.browser.detail.FixedListDetailPane;


import javax.swing.tree.TreePath;

/**
 * Detail pane showing <tt>AnnotationDefault</tt>  attribute.
 *
 * @author <a href="mailto:vitor.carreira@gmail.com">Vitor Carreira</a>
 * @version $Revision: 1.3 $ $Date: 2006/01/02 17:57:03 $
 */
public class AnnotationDefaultAttributeDetailPane extends FixedListDetailPane {

    private ExtendedJLabel lblTag;
    private ExtendedJLabel lblTagVerbose;

    public AnnotationDefaultAttributeDetailPane(BrowserServices services) {
        super(services);
    }

    protected void setupLabels() {
        addDetailPaneEntry(normalLabel("Default value:"),
                lblTag = highlightLabel(),
                lblTagVerbose = highlightLabel());
    }

    public void show(TreePath treePath) {
        AnnotationDefaultAttribute ada = (AnnotationDefaultAttribute)findAttribute(treePath);

        int tag = ada.getDefaultValue().getTag();
        String name = ada.getDefaultValue().getEntryName();
        lblTag.setText(name);
        lblTagVerbose.setText("<" + ElementValue.getTagDescription(tag) + ">");

        super.show(treePath);
    }
}
