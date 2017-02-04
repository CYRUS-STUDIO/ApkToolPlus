/*
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public
 License as published by the Free Software Foundation; either
 version 2 of the license, or (at your option) any later version.
 */

package ee.ioc.cs.jbe.browser.detail.attributes;

import ee.ioc.cs.jbe.browser.detail.attributes.code.*;
import org.gjt.jclasslib.structures.InvalidByteCodeException;
import org.gjt.jclasslib.util.GUIHelper;
import org.gjt.jclasslib.util.ProgressDialog;

import ee.ioc.cs.jbe.browser.AbstractDetailPane;
import ee.ioc.cs.jbe.browser.BrowserInternalFrame;
import ee.ioc.cs.jbe.browser.BrowserServices;
import ee.ioc.cs.jbe.browser.BrowserTreeNode;
import ee.ioc.cs.jbe.browser.codeedit.ClassSaver;
import ee.ioc.cs.jbe.browser.codeedit.CodeGenerator;
import ee.ioc.cs.jbe.browser.codeedit.InputFieldException;


import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Detail pane showing a <tt>Code</tt> attribute. Contains three other detail
 * panes in its tabbed pane.
 * 
 * @author <a href="mailto:jclasslib@ej-technologies.com">Ingo Kegel</a>
 * @author Ando Saabas
 * @version $Revision: 1.12 $ $Date: 2006/09/08 15:50:12 $
 */
public class CodeAttributeDetailPane extends AbstractDetailPane implements
		ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8073041883786738339L;

	private JTabbedPane tabbedPane;

	private ExceptionTableDetailPane exceptionTablePane;

	private ByteCodeDetailPane byteCodePane;

	private QuantDetailPane quantPane;

	private CodeEditPane codeEditPane;

	private AddExceptionPane addExceptionPane;

	private JPanel mainPane;

	private JButton codeSaveButton;

	private JButton miscSaveButton;

	private BrowserInternalFrame internalFrame;

	private TreePath treePath;

	/**
	 * Constructor.
	 * 
	 * @param services
	 *            the associated browser services.
	 */
	public CodeAttributeDetailPane(BrowserServices services) {

		super(services);
		internalFrame = (BrowserInternalFrame) services;
	}

	protected void setupComponent() {
		setLayout(new BorderLayout());

		add(buildTabbedPane(), BorderLayout.CENTER);
	}

	/**
	 * Get the <tt>ByteCodeDetailPane</tt> showing the code of this
	 * <tt>Code</tt> attribute.
	 * 
	 * @return the <tt>ByteCodeDetailPane</tt>
	 */
	public ByteCodeDetailPane getCodeAttributeByteCodeDetailPane() {
		return byteCodePane;
	}

	/**
	 * Select the <tt>ByteCodeDetailPane</tt> showing the code of this
	 * <tt>Code</tt> attribute.
	 */
	public void selectByteCodeDetailPane() {
		tabbedPane.setSelectedIndex(0);
	}

	private JTabbedPane buildTabbedPane() {

		tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Bytecode", buildByteCodePane());
		tabbedPane.addTab("Exception table", buildExceptionTablePane());
		tabbedPane.addTab("Misc", buildMiscPane());
		tabbedPane.addTab("Code Editor", buildCodeEditPane());
		return tabbedPane;
	}

	private JPanel buildByteCodePane() {
		byteCodePane = new ByteCodeDetailPane(services);
		return byteCodePane;
	}

	private JPanel buildExceptionTablePane() {
		JPanel exceptionsPane = new JPanel(new BorderLayout());
		addExceptionPane = new AddExceptionPane(services);
		exceptionTablePane = new ExceptionTableDetailPane(services);
		exceptionsPane.add(addExceptionPane, BorderLayout.NORTH);
		exceptionsPane.add(exceptionTablePane, BorderLayout.CENTER);
		return exceptionsPane;
	}

	private JPanel buildMiscPane() {

		mainPane = new JPanel();
		// mainPane.setLayout()
		mainPane.setLayout(new GridBagLayout());
		quantPane = new QuantDetailPane(services);

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 1;
		c.gridy = 1;

		mainPane.add(quantPane, c);
		miscSaveButton = new JButton("Save method");
		miscSaveButton.addActionListener(this);
		c = new GridBagConstraints();
		c.gridy = 0;

		mainPane.add(miscSaveButton, c);
		return mainPane;

	}

	private JPanel buildCodeEditPane() {
		mainPane = new JPanel();
		// mainPane.setLayout()
		mainPane.setLayout(new GridBagLayout());
		try {
			codeEditPane = new CodeEditPane(services);
		} catch (InvalidByteCodeException e) {
			e.printStackTrace();
		}

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 1;
		c.gridy = 1;

		mainPane.add(codeEditPane, c);
		codeSaveButton = new JButton("Save method");
		codeSaveButton.addActionListener(this);
		c = new GridBagConstraints();
		c.gridy = 0;

		mainPane.add(codeSaveButton, c);
		return mainPane;
	}

	public void show(TreePath treePath) {
		this.treePath = treePath;
		CodeGenerator cg = new CodeGenerator();
		addExceptionPane.setMethod(cg.getMethodIndex(treePath));

		exceptionTablePane.show(treePath);
		byteCodePane.show(treePath);
		quantPane.show(treePath);
		codeEditPane.show(treePath);
		internalFrame.setReloading(false);

	}

	public CodeEditPane getCodeEditPane() {
		return codeEditPane;
	}

	public QuantDetailPane getQuantEditPane() {
		return quantPane;
	}

	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == codeSaveButton) {

			int methodIndex = ((BrowserTreeNode) treePath.getParentPath()
					.getLastPathComponent()).getIndex();
			String methodBody = ((JEditorPane) getCodeEditPane().getEditPanes()
					.get(Integer.toString(methodIndex))).getText();
			String fileName = internalFrame.getFileName();
			ClassSaver cs = new ClassSaver(ClassSaver.SAVE_CODE, fileName,
					methodBody, methodIndex);
			ProgressDialog progressDialog = new ProgressDialog(internalFrame
					.getParentFrame(), null, "Saving changes...");

			progressDialog.setRunnable(cs);
			progressDialog.setVisible(true);

			if (cs.exceptionOccured()) {
				ErrorReportWindow er = new ErrorReportWindow(internalFrame
						.getParentFrame(), cs.getExceptionVerbose(), "Exporting code failed");

				er.pack();
				GUIHelper.centerOnParentWindow(er, internalFrame
						.getParentFrame());
				er.setVisible(true);
			} 	else {
				internalFrame.getParentFrame().doReload();
			}

		} else if (event.getSource() == miscSaveButton) {
			try {
				int methodIndex = ((BrowserTreeNode) treePath.getParentPath()
						.getLastPathComponent()).getIndex();
				String fileName = internalFrame.getFileName();
				MiscDetailPane mdp = (MiscDetailPane) getQuantEditPane()
						.getMiscPanes().get(Integer.toString(methodIndex));
				int maxStack = mdp.getMaxStack();
				int maxLocals = mdp.getMaxLocals();
				ClassSaver cs = new ClassSaver(ClassSaver.SAVE_MISC, fileName,
						maxStack, maxLocals, methodIndex);
				ProgressDialog progressDialog = new ProgressDialog(internalFrame
						.getParentFrame(), null, "Saving changes...");

				progressDialog.setRunnable(cs);
				progressDialog.setVisible(true);
				if (cs.exceptionOccured()) {
					ErrorReportWindow er = new ErrorReportWindow(internalFrame
							.getParentFrame(), cs.getExceptionVerbose(), "Setting values failed");

					er.pack();
					GUIHelper.centerOnParentWindow(er, internalFrame
							.getParentFrame());
					er.setVisible(true);
				}	else {
					internalFrame.getParentFrame().doReload();
				}
			} catch (InputFieldException e2) {
				ErrorReportWindow er = new ErrorReportWindow(internalFrame
						.getParentFrame(), e2.getErrorVerbose(), "Input field error");
				er.setLocationRelativeTo((BrowserInternalFrame) services);
				er.pack();
				er.setVisible(true);

			}

		}
	}
}
