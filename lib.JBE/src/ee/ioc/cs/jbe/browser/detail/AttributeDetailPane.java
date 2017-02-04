/*
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public
 License as published by the Free Software Foundation; either
 version 2 of the license, or (at your option) any later version.
 */

package ee.ioc.cs.jbe.browser.detail;

import org.gjt.jclasslib.structures.AttributeInfo;
import org.gjt.jclasslib.structures.attributes.*;

import ee.ioc.cs.jbe.browser.AbstractDetailPane;
import ee.ioc.cs.jbe.browser.BrowserServices;
import ee.ioc.cs.jbe.browser.detail.attributes.*;


import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.HashMap;

/**
 * Detail pane for an attribute of class
 * <tt>org.gjt.jclasslib.structures.AttributeInfo</tt>. This class is a
 * container for the classes defined in the <tt>attributes</tt> subpackage and
 * switches between the contained panes as required.
 * 
 * @author <a href="mailto:jclasslib@ej-technologies.com">Ingo Kegel</a>
 * @version $Revision: 1.9 $ $Date: 2006/09/25 16:00:58 $
 */
public class AttributeDetailPane extends AbstractDetailPane  {

    private static final String SCREEN_UNKNOWN = "Unknown";

    private static final String SCREEN_CONSTANT_VALUE = "ConstantValue";

    private static final String SCREEN_CODE = "Code";

    private static final String SCREEN_EXCEPTIONS = "Exceptions";

    private static final String SCREEN_INNER_CLASSES = "InnerClasses";

    private static final String SCREEN_SOURCE_FILE = "SourceFile";

    private static final String SCREEN_LINE_NUMBER_TABLE = "LineNumberTable";

    private static final String SCREEN_LOCAL_VARIABLE_TABLE = "LocalVariableTable";

    private static final String SCREEN_ENCLOSING_METHOD = "EnclosingMethod";

    private static final String SCREEN_SIGNATURE = "Signature";

    private static final String SCREEN_LOCAL_VARIABLE_TYPE_TABLE = "LocalVariableTypeTable";

    private static final String SCREEN_RUNTIME_ANNOTATIONS = "RuntimeAnnotations";

    private static final String SCREEN_ANNOTATION_DEFAULT = "AnnotationDefault";

    private HashMap attributeTypeToDetailPane;

    // Visual components

    private JPanel specificInfoPane;

    private GenericAttributeDetailPane genericInfoPane;

    private CodeAttributeDetailPane codeAttributeDetailPane;

    /**
     * Constructor.
     * 
     * @param services
     *            the associated browser services.
     */
    public AttributeDetailPane(BrowserServices services) {
        super(services);

    }

    protected void setupComponent() {

        buildGenericInfoPane();
        buildSpecificInfoPane();

        setLayout(new BorderLayout());
        JPanel jp = new JPanel();
        jp.setLayout(new BorderLayout());
        jp.add(genericInfoPane, BorderLayout.NORTH);
        add(jp, BorderLayout.NORTH);
        add(specificInfoPane, BorderLayout.CENTER);

    }

    public void show(TreePath treePath) {

        AttributeInfo attribute = findAttribute(treePath);

        
        String paneName = null;
        if (attribute instanceof ConstantValueAttribute) {
            paneName = SCREEN_CONSTANT_VALUE;
        } else if (attribute instanceof CodeAttribute) {
            paneName = SCREEN_CODE;
        } else if (attribute instanceof ExceptionsAttribute) {
            paneName = SCREEN_EXCEPTIONS;
        } else if (attribute instanceof InnerClassesAttribute) {
            paneName = SCREEN_INNER_CLASSES;
        } else if (attribute instanceof SourceFileAttribute) {
            paneName = SCREEN_SOURCE_FILE;
        } else if (attribute instanceof LineNumberTableAttribute) {
            paneName = SCREEN_LINE_NUMBER_TABLE;
        } else if (attribute instanceof LocalVariableTableAttribute) {
            paneName = SCREEN_LOCAL_VARIABLE_TABLE;
        } else if (attribute instanceof EnclosingMethodAttribute) {
            paneName = SCREEN_ENCLOSING_METHOD;
        } else if (attribute instanceof SignatureAttribute) {
            paneName = SCREEN_SIGNATURE;
        } else if (attribute instanceof LocalVariableTypeTableAttribute) {
            paneName = SCREEN_LOCAL_VARIABLE_TYPE_TABLE;
        } else if (attribute instanceof RuntimeAnnotationsAttribute) {
            paneName = SCREEN_RUNTIME_ANNOTATIONS;
        } else if (attribute instanceof AnnotationDefaultAttribute) {
            paneName = SCREEN_ANNOTATION_DEFAULT;
        }

        CardLayout layout = (CardLayout) specificInfoPane.getLayout();
        if (paneName == null) {
            layout.show(specificInfoPane, SCREEN_UNKNOWN);
        } else {
            AbstractDetailPane pane = (AbstractDetailPane) attributeTypeToDetailPane
                    .get(paneName);
            pane.show(treePath);
            layout.show(specificInfoPane, paneName);
        }

        genericInfoPane.show(treePath);
    }

    /**
     * Get the <tt>CodeAttributeDetailPane</tt> showing the details of a
     * <tt>Code</tt> attribute.
     * 
     * @return the <tt>CodeAttributeDetailPane</tt>
     */
    public CodeAttributeDetailPane getCodeAttributeDetailPane() {
        return (CodeAttributeDetailPane) attributeTypeToDetailPane
                .get(SCREEN_CODE);
    }

    private void buildGenericInfoPane() {

        genericInfoPane = new GenericAttributeDetailPane(services);
        genericInfoPane.setBorder(createTitledBorder("Generic info:"));
    }

    private void buildSpecificInfoPane() {

        specificInfoPane = new JPanel();
        specificInfoPane.setBorder(createTitledBorder("Specific info:"));

        specificInfoPane.setLayout(new CardLayout());
        attributeTypeToDetailPane = new HashMap();
        JPanel pane;

        pane = new JPanel();
        specificInfoPane.add(pane, SCREEN_UNKNOWN);

        addScreen(new ConstantValueAttributeDetailPane(services),
                SCREEN_CONSTANT_VALUE);

        //we need this reference for when saving changed attributes
        codeAttributeDetailPane = new CodeAttributeDetailPane(services);
         
        addScreen(codeAttributeDetailPane, SCREEN_CODE);

        addScreen(new ExceptionsAttributeDetailPane(services),
                SCREEN_EXCEPTIONS);

        addScreen(new InnerClassesAttributeDetailPane(services),
                SCREEN_INNER_CLASSES);

        addScreen(new SourceFileAttributeDetailPane(services),
                SCREEN_SOURCE_FILE);

        addScreen(new LineNumberTableAttributeDetailPane(services),
                SCREEN_LINE_NUMBER_TABLE);

        addScreen(new LocalVariableTableAttributeDetailPane(services),
                SCREEN_LOCAL_VARIABLE_TABLE);

        addScreen(new EnclosingMethodAttributeDetailPane(services),
                SCREEN_ENCLOSING_METHOD);

        addScreen(new SignatureAttributeDetailPane(services), SCREEN_SIGNATURE);

        addScreen(new LocalVariableTypeTableAttributeDetailPane(services),
                SCREEN_LOCAL_VARIABLE_TYPE_TABLE);

        addScreen(new RuntimeAnnotationsAttributeDetailPane(services),
                SCREEN_RUNTIME_ANNOTATIONS);

        addScreen(new AnnotationDefaultAttributeDetailPane(services),
                SCREEN_ANNOTATION_DEFAULT);
    }

    private void addScreen(AbstractDetailPane detailPane, String name) {

        if (detailPane instanceof FixedListDetailPane) {
            specificInfoPane.add(((FixedListDetailPane) detailPane)
                    .getScrollPane(), name);
        } else {
            specificInfoPane.add(detailPane, name);
        }
        attributeTypeToDetailPane.put(name, detailPane);
    }

    private Border createTitledBorder(String title) {
        Border simpleBorder = BorderFactory.createEtchedBorder();
        Border titledBorder = BorderFactory.createTitledBorder(simpleBorder,
                title);

        return titledBorder;
    }


}
