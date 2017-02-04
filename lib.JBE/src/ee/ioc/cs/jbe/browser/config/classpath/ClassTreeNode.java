/*
    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public
    License as published by the Free Software Foundation; either
    version 2 of the license, or (at your option) any later version.
*/

package ee.ioc.cs.jbe.browser.config.classpath;

import javax.swing.tree.DefaultMutableTreeNode;

/**
    Tree node for the tree in the <tt>ClasspathBrowser</tt> dialog.

    @author <a href="mailto:jclasslib@ej-technologies.com">Ingo Kegel</a>
    @version $Revision: 1.2 $ $Date: 2006/09/25 16:00:58 $
*/
public class ClassTreeNode extends DefaultMutableTreeNode {

    private boolean packageNode;

    /**
     * Constructor for the root node.
     */
    public ClassTreeNode() {
    }

    /**
     * Constructor for class and package nodes.
     * @param name the name of the entry.
     * @param packageNode whether the node is a package node or not.
     */
    public ClassTreeNode(String name, boolean packageNode) {
        super(name);
        this.packageNode = packageNode;
    }

    /**
     * Return whether the node is a package node or not.
     * @return the value.
     */
    public boolean isPackageNode() {
        return packageNode;
    }
}
