/*
 *
 */
package ee.ioc.cs.jbe.browser.detail;

import java.awt.GridLayout;
import java.awt.event.*;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.tree.TreePath;


import org.apache.bcel.classfile.Method;

import ee.ioc.cs.jbe.browser.detail.attributes.code.ErrorReportWindow;
import org.gjt.jclasslib.util.GUIHelper;
import org.gjt.jclasslib.util.ProgressDialog;

import ee.ioc.cs.jbe.browser.AbstractDetailPane;
import ee.ioc.cs.jbe.browser.BrowserInternalFrame;
import ee.ioc.cs.jbe.browser.BrowserServices;
import ee.ioc.cs.jbe.browser.codeedit.ClassSaver;


public class FieldsGeneralPane extends AbstractDetailPane implements ActionListener{


	private static final long serialVersionUID = 8932390908031077932L;

	private JButton addButton;

	private JComboBox dropdown;

	private JTextField name, descriptor;

	private JCheckBox staticCB;

	private JCheckBox finalCB;

	private JCheckBox volatileCB;

	private JCheckBox transientCB;


	private BrowserInternalFrame internalFrame;

	public FieldsGeneralPane(BrowserServices services) {
		super(services);
		internalFrame = (BrowserInternalFrame) services;
	}

	public void show(TreePath treePath) {

	}

	protected void setupComponent() {
		addButton = new JButton("Add field");
		dropdown = new JComboBox();
		name = new JTextField(15);
		descriptor = new JTextField(15);
		dropdown.addItem("");
		dropdown.addItem("public");
		dropdown.addItem("private");
		dropdown.addItem("protected");
		JLabel dropdownLabel = new JLabel("Access");
		JPanel dropdownPanel = new JPanel(new GridLayout(2,1));
		dropdownPanel.add(dropdownLabel);
		dropdownPanel.add(dropdown);
		add(dropdownPanel);
		JPanel checkBoxPanel = new JPanel();
		checkBoxPanel.setLayout(new GridLayout(2, 2));
		staticCB = new JCheckBox("Static");
		finalCB = new JCheckBox("Final");
		volatileCB = new JCheckBox("Volatile");
		transientCB = new JCheckBox("Transient");
		checkBoxPanel.add(staticCB);
		checkBoxPanel.add(finalCB);
		checkBoxPanel.add(volatileCB);
		checkBoxPanel.add(transientCB);
		add(checkBoxPanel);

		JPanel namePanel = new JPanel();
		namePanel.setLayout(new GridLayout(2, 1));
		namePanel.add(new JLabel("Name (e.g. myField)"));
		namePanel.add(name);
		add(namePanel);

		JPanel descriptionPanel = new JPanel();
		descriptionPanel.setLayout(new GridLayout(2, 1));
		descriptionPanel.add(new JLabel("Descriptor (e.g. Ljava/lang/String;)"));
		descriptionPanel.add(descriptor);
		add(descriptionPanel);
        Border simpleBorder = BorderFactory.createEtchedBorder();
        Border border = BorderFactory.createTitledBorder(simpleBorder,
                "Add field");
		this.setBorder(border);		
		JPanel buttonPanel = new JPanel(new GridLayout(2,1));
		buttonPanel.add(new JLabel(""));
		buttonPanel .add(addButton);
		add(buttonPanel);
		dropdown.addActionListener(this);
		dropdown.setActionCommand("select");
		addButton.addActionListener(this);
		addButton.setActionCommand("add");
	}

	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == addButton) {
			Method method = new Method();
			if (staticCB.isSelected()) {
				method.isStatic(true);
			}
			if (finalCB.isSelected()) {
				method.isFinal(true);
			}
			if (volatileCB.isSelected()) {
				method.isSynchronized(true);
			}
			if (transientCB.isSelected()) {
				method.isNative(true);
			}
			int selectedItem = dropdown.getSelectedIndex();

			switch (selectedItem) {
			case 1:
				method.isPublic(true);
				break;
			case 2:
				method.isPrivate(true);
				break;
			case 3:
				method.isProtected(true);
				break;
			}

			String fileName = internalFrame.getFileName();
			String fieldName = name.getText();
			String fieldDescriptor = descriptor.getText();
			int accessFlags = method.getAccessFlags();
			ClassSaver classSaver = new ClassSaver(ClassSaver.ADD_FIELD, fileName, fieldName, fieldDescriptor, accessFlags);
			ProgressDialog progressDialog = new ProgressDialog(
					internalFrame.getParentFrame(), null,
					"Adding interface...");
			progressDialog.setRunnable(classSaver);
			progressDialog.setVisible(true);
			if (classSaver.exceptionOccured()) {
				ErrorReportWindow er = new ErrorReportWindow(internalFrame
						.getParentFrame(), classSaver.getExceptionVerbose(), "Adding field failed");

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
