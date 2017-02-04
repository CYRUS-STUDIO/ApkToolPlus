/*
    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public
    License as published by the Free Software Foundation; either
    version 2 of the license, or (at your option) any later version.
*/

package ee.ioc.cs.jbe.browser;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Tree node contained in the tree of the <tt>BrowserTreePane</tt> and
 * representing a structural element of the class file format.
 *
 * @author <a href="mailto:jclasslib@ej-technologies.com">Ingo Kegel</a>, <a href="mailto:vitor.carreira@gmail.com">Vitor Carreira</a>
 * @version $Revision: 1.1 $ $Date: 2006/09/25 16:00:58 $
 */
public class BrowserTreeNode extends DefaultMutableTreeNode {

    /**
	 * 
	 */
	private static final long serialVersionUID = -410528404280618258L;

	/**
     * Node that does not exhibit detail content.
     */
    public static final String NODE_NO_CONTENT = "noContent";
    
    /**
     * Node for general pane of constant pool
     */
    public static final String NODE_CONSTANT_POOL_GENERAL = "constantPoolGeneral";    

    /**
     * Node for general information on the class file structure.
     */
    public static final String NODE_GENERAL = "general";
    /**
     * Node for a constant pool entry (<tt>CPInfo</tt>).
     */
    public static final String NODE_CONSTANT_POOL = "constantPool";
    /**
     * Node for an interface entry.
     */
    public static final String NODE_INTERFACE = "interface";

    /**
     * Node for general fields page  (<tt>FieldInfo</tt>).
     */
    public static final String NODE_FIELDS_GENERAL = "fieldGeneral";    
    
    /**
     * Node for a field entry  (<tt>FieldInfo</tt>).
     */
    public static final String NODE_FIELD = "field";
    /**
     * Node for a method entry  (<tt>MethodInfo</tt>).
     */
    public static final String NODE_METHOD = "method";
    /**
     * Node for an attribute entry  (<tt>AttributeInfo</tt>).
     */
    public static final String NODE_ATTRIBUTE = "attribute";
    /**
     * Node for an attribute entry  (<tt>VisibleRuntimeAnnotation</tt>).
     */
    public static final String NODE_ANNOTATION = "annotation";
    /**
     * Node for an <tt>ElementValuePair</tt> entry.
     */
    public static final String NODE_ELEMENTVALUEPAIR = "elementvaluepair";
    /**
     * Node for an <tt>ElementValue</tt> entry.
     */
    public static final String NODE_ELEMENTVALUE = "elementvalue";
    /**
     * Node for an <tt>ArrayElementValue</tt> entry.
     */
    public static final String NODE_ARRAYELEMENTVALUE = "arrayelementvalue";

    public static final String NODE_METHOD_GENERAL = "methodGeneral";

	public static final String NODE_INTERFACES_GENERAL = "interfacesGeneral";

	public static final String NODE_ATTRIBUTES_GENERAL = "attributesGeneral";


    private String type;
    private int index;
    private Object element;

    /**
     * Constructor.
     *
     * @param text the display text.
     */
    public BrowserTreeNode(String text) {
        this(text, NODE_NO_CONTENT);
    }

    /**
     * Constructor.
     *
     * @param text the display text.
     * @param type the node type. One of the <tt>NODE_</tt> constants.
     */
    public BrowserTreeNode(String text, String type) {
        this(text, type, 0);
    }

    /**
     * Constructor.
     *
     * @param text  the display text.
     * @param type  the node type. One of the <tt>NODE_</tt> constants.
     * @param index the logical index of this node.
     */
    public BrowserTreeNode(String text, String type, int index) {
        this(text, type, index, null);
    }

    public BrowserTreeNode(String text, String type, int index, Object element) {
        super(text);
        this.type = type;
        this.index = index;
        this.element = element;
    }

    /**
     * Get the type of the node as defined by the <tt>NODE_</tt> constants.
     *
     * @return the type
     */
    public void setType(String s) {
        type = s;
    }    
    
    /**
     * Get the type of the node as defined by the <tt>NODE_</tt> constants.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Get the index of the node among its siblings. This information <i>could</i>
     * be retrieved from a tree but is important structural information and
     * should not be left to chance.
     *
     * @return the index
     */
    public int getIndex() {
        return index;
    }

    /**
     * Get the element associated with this node
     *
     * @return the element
     */
    public Object getElement() {
        return element;
    }
}
