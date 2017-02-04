/*
    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public
    License as published by the Free Software Foundation; either
    version 2 of the license, or (at your option) any later version.
*/

package ee.ioc.cs.jbe.browser.detail;

import ee.ioc.cs.jbe.browser.detail.attributes.code.ErrorReportWindow;
import ee.ioc.cs.jbe.browser.detail.attributes.code.MiscDetailPane;
import org.gjt.jclasslib.util.ExtendedJLabel;
import org.gjt.jclasslib.util.GUIHelper;
import org.gjt.jclasslib.util.ProgressDialog;

import ee.ioc.cs.jbe.browser.AbstractDetailPane;
import ee.ioc.cs.jbe.browser.BrowserInternalFrame;
import ee.ioc.cs.jbe.browser.BrowserServices;
import ee.ioc.cs.jbe.browser.BrowserTreeNode;
import ee.ioc.cs.jbe.browser.codeedit.ClassSaver;
import ee.ioc.cs.jbe.browser.detail.attributes.GenericAttributeDetailPane;
import ee.ioc.cs.jbe.browser.detail.attributes.SourceFileAttributeDetailPane;
import ee.ioc.cs.jbe.browser.detail.constants.AbstractConstantInfoDetailPane;


import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;

/**
    Base class for all detail panes with a structure of
    a fixed number of key-value pairs arranged in a list.
    
    @author <a href="mailto:jclasslib@ej-technologies.com">Ingo Kegel</a>
    @version $Revision: 1.9 $ $Date: 2006/09/25 16:00:58 $
*/
public abstract class FixedListDetailPane extends AbstractDetailPane implements ActionListener {
    
    // Visual components

    private java.util.List<DetailPaneEntry> detailPaneEntries;
    private JScrollPane scrollPane;
    private JButton deleteBtn;
    private BrowserInternalFrame internalFrame;
	private TreePath treePath;

    /**
        Constructor.
        @param services the associated browser services.
     */
    protected FixedListDetailPane(BrowserServices services) {
        super(services);
        internalFrame = (BrowserInternalFrame) services;
    }

    /**
        Add a detail entry consisting of a key value pair.
        @param key the key label
        @param value the value label
     */
    protected void addDetailPaneEntry(JComponent key, JComponent value) {
        addDetailPaneEntry(key, value, null);
    }


    /**
        Add a detail entry consisting of a key value pair with a associated comment
        which is made scrollable.
        @param key the key label
        @param value the value label
        @param comment the comment
     */
    protected void addDetailPaneEntry(JComponent key,
                                        JComponent  value,
                                      ExtendedJLabel comment) {
                                          
        if (detailPaneEntries == null) {
            detailPaneEntries = new ArrayList<DetailPaneEntry>();
        }
        
        detailPaneEntries.add(
                new DetailPaneEntry(key, value, comment)
            );
    }
    
    protected void setupComponent() {
        
        setupLabels();
        
        setLayout(new GridBagLayout());
        
        GridBagConstraints gKey = new GridBagConstraints();
        gKey.anchor = GridBagConstraints.NORTHWEST;
        gKey.insets = new Insets(1,10,0,10);
        
        GridBagConstraints gValue = new GridBagConstraints();
        gValue.gridx = 1;
        gValue.anchor = GridBagConstraints.NORTHEAST;
        gValue.insets = new Insets(1,0,0,5);

        GridBagConstraints gComment = new GridBagConstraints();
        gComment.gridx = 2;
        gComment.anchor = GridBagConstraints.NORTHWEST;
        gComment.insets = new Insets(1,0,0,5);
        gComment.fill = GridBagConstraints.HORIZONTAL;
        
        GridBagConstraints gCommentOnly = (GridBagConstraints)gComment.clone();
        gCommentOnly.gridx = 1;
        gCommentOnly.gridwidth = 2;
        
        GridBagConstraints gRemainder = new GridBagConstraints();
        gRemainder.gridx = 2;
        gRemainder.weightx = gRemainder.weighty = 1;
        gRemainder.fill = GridBagConstraints.BOTH;

        Iterator it = detailPaneEntries.iterator();
        while (it.hasNext()) {
            DetailPaneEntry entry = (DetailPaneEntry)it.next();
            if (entry == null) {
                continue;
            }

            gComment.gridy = gValue.gridy = ++gKey.gridy;
            if (entry.key != null) {
                add(entry.key, gKey);
            }
            if (entry.value != null) {
                add(entry.value, gValue);
            }
            if (entry.comment != null) {
                add(entry.comment, (entry.value == null) ? gCommentOnly : gComment);
                entry.comment.setAutoTooltip(true);
            }
     
        }
        if (!(this instanceof GeneralDetailPane || this instanceof GenericAttributeDetailPane || this instanceof MiscDetailPane || this instanceof SourceFileAttributeDetailPane)) {
        	addDeleteButton();
        }
        
        
        gRemainder.gridy = gKey.gridy + 1;
        gRemainder.gridy += addSpecial(gRemainder.gridy);

        add(new JPanel(), gRemainder);
        scrollPane = new JScrollPane(this);
        GUIHelper.setDefaultScrollbarUnits(scrollPane);
        scrollPane.setBorder(null);

    }

    private void addDeleteButton() {
    	String buttonText = "Delete this entry";
    	if (this instanceof AbstractConstantInfoDetailPane) {
    		buttonText = "Delete constant";
    	} else   if (this instanceof ClassMemberDetailPane) {
    		ClassMemberDetailPane cmdp = (ClassMemberDetailPane)this;
    		if (cmdp.getMode() == 2) {
    			buttonText = "Delete method";
    		} else  if (cmdp.getMode() == 1) {
    			buttonText = "Delete field";
    		}
    	}else   if (this instanceof InterfaceDetailPane) {
    		buttonText = "Delete interface";
    	}
    	deleteBtn = new JButton(buttonText);
    	deleteBtn.addActionListener(this);
    	GridBagConstraints gc = new GridBagConstraints();
        gc.weightx = 1;
        gc.anchor = GridBagConstraints.NORTHWEST;
        gc.insets = new Insets(5, 10, 0, 10);
        gc.gridy = GridBagConstraints.RELATIVE;//lastGridy + 2;
        gc.gridx = 0;
        gc.gridwidth = 3;
        add(deleteBtn, gc);
		
	}

	/**
        Get the scroll pane.
        @return the scroll pane.
     */
    public JScrollPane getScrollPane() {
        return scrollPane;
    }

    public void show(TreePath treePath) {
    	this.treePath = treePath;
        scrollPane.getViewport().setViewPosition(new Point(0, 0));

    }

    /**
        Setup all label and fill the <tt>detailPaneEntries</tt> list so that
        <tt>setupComponent</tt> can layout the pane.
     */
    protected abstract void setupLabels();

    /**
        Hook for derived classes to add additional visual elements.
        @param gridy the current <tt>gridy</tt> of the <tt>GridbagLayout</tt>.
        @return the number of added rows.
     */
    protected int addSpecial(int gridy) {
        return 0;
    }

    private static class DetailPaneEntry {
        public final JComponent key;
        public final JComponent value;
        public final ExtendedJLabel comment;
        
        private DetailPaneEntry(JComponent key,
                                JComponent value,
                                ExtendedJLabel comment) {
            this.key = key;
            this.value = value;
            this.comment = comment;
        }
    }

    
    public void actionPerformed(ActionEvent event) {
    	if (this instanceof AbstractConstantInfoDetailPane) {
            String fileName = internalFrame.getFileName();
            int constantPoolIndex = ((BrowserTreeNode)treePath.getLastPathComponent()).getIndex();
            ClassSaver cs = new ClassSaver(ClassSaver.REMOVE_CONSTANT, fileName, constantPoolIndex);
			ProgressDialog progressDialog = new ProgressDialog(internalFrame
					.getParentFrame(), null, "Removing constant...");

			progressDialog.setRunnable(cs);
			progressDialog.setVisible(true);
			if (cs.exceptionOccured()) {
				ErrorReportWindow er = new ErrorReportWindow(internalFrame
						.getParentFrame(), cs.getExceptionVerbose(), "removing constant failed");

				er.pack();
				GUIHelper.centerOnParentWindow(er, internalFrame
						.getParentFrame());
				er.setVisible(true);
			} else {
				internalFrame.getParentFrame().doReload();
			}
    	} else   if (this instanceof ClassMemberDetailPane) {
    		ClassMemberDetailPane cmdp = (ClassMemberDetailPane)this;
    		if (cmdp.getMode() == 2) {
                String fileName = internalFrame.getFileName();
                int methodIndex = ((BrowserTreeNode)treePath.getLastPathComponent()).getIndex();
                ClassSaver cs = new ClassSaver(ClassSaver.REMOVE_METHOD, fileName, methodIndex);
    			ProgressDialog progressDialog = new ProgressDialog(internalFrame
    					.getParentFrame(), null, "Removing method...");

    			progressDialog.setRunnable(cs);
    			progressDialog.setVisible(true);
    			if (cs.exceptionOccured()) {
    				ErrorReportWindow er = new ErrorReportWindow(internalFrame
    						.getParentFrame(), cs.getExceptionVerbose(),  "Removing method failed");

    				er.pack();
    				GUIHelper.centerOnParentWindow(er, internalFrame
    						.getParentFrame());
    				er.setVisible(true);
    			} else {
    				internalFrame.getParentFrame().doReload();
    			}
    		} else  if (cmdp.getMode() == 1) {
    			String fileName = internalFrame.getFileName();
                int fieldIndex = ((BrowserTreeNode)treePath.getLastPathComponent()).getIndex();
                ClassSaver cs = new ClassSaver(ClassSaver.REMOVE_FIELD, fileName, fieldIndex);
    			ProgressDialog progressDialog = new ProgressDialog(internalFrame
    					.getParentFrame(), null, "Removing field...");

    			progressDialog.setRunnable(cs);
    			progressDialog.setVisible(true);
    			if (cs.exceptionOccured()) {
    				ErrorReportWindow er = new ErrorReportWindow(internalFrame
    						.getParentFrame(), cs.getExceptionVerbose(), "Removing field failed");

    				er.pack();
    				GUIHelper.centerOnParentWindow(er, internalFrame
    						.getParentFrame());
    				er.setVisible(true);
    			} else {
    				internalFrame.getParentFrame().doReload();
    			}
    		}
    	}else   if (this instanceof InterfaceDetailPane) {
			String fileName = internalFrame.getFileName();
            int interfaceIndex = ((BrowserTreeNode)treePath.getLastPathComponent()).getIndex();
            ClassSaver cs = new ClassSaver(ClassSaver.REMOVE_INTERFACE, fileName, interfaceIndex);
			ProgressDialog progressDialog = new ProgressDialog(internalFrame
					.getParentFrame(), null, "Removing interface...");

			progressDialog.setRunnable(cs);
			progressDialog.setVisible(true);
			if (cs.exceptionOccured()) {
				ErrorReportWindow er = new ErrorReportWindow(internalFrame
						.getParentFrame(), cs.getExceptionVerbose(), "Removing interface failed");

				er.pack();
				GUIHelper.centerOnParentWindow(er, internalFrame
						.getParentFrame());
				er.setVisible(true);
			} else {
				internalFrame.getParentFrame().doReload();
			}
    	}

    }
}

