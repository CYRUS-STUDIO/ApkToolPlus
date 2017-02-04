/*
 *
 */
package ee.ioc.cs.jbe.browser.detail.constants;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.tree.TreePath;

import org.apache.bcel.Constants;
import ee.ioc.cs.jbe.browser.detail.attributes.code.ErrorReportWindow;
import org.gjt.jclasslib.util.GUIHelper;
import org.gjt.jclasslib.util.ProgressDialog;

import ee.ioc.cs.jbe.browser.AbstractDetailPane;
import ee.ioc.cs.jbe.browser.BrowserInternalFrame;
import ee.ioc.cs.jbe.browser.BrowserServices;
import ee.ioc.cs.jbe.browser.codeedit.ClassSaver;


public class ConstantAddPane extends AbstractDetailPane implements
		ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2709981225194399546L;

	private JButton addButton;

	private JComboBox dropdown;

	private JTextField mainText, sndText, thirdText;

	private JLabel mainTextLabel, sndTextLabel, thirdTextLabel, buttonLabel,
			dropdownLabel;

	BrowserInternalFrame internalFrame;

	public ConstantAddPane(BrowserServices services) {
		super(services);
		internalFrame = (BrowserInternalFrame) services;
	}

	public void show(TreePath treePath) {
	}

	protected void setupComponent() {
		addButton = new JButton("Add Constant");
		dropdown = new JComboBox();
		mainText = new JTextField(15);
		sndText = new JTextField(15);
		thirdText = new JTextField(15);
		mainTextLabel = new JLabel();
		sndTextLabel = new JLabel();
		thirdTextLabel = new JLabel();
		buttonLabel = new JLabel();
		dropdownLabel = new JLabel("Constant type");

		dropdown.addItem("Class");
		dropdown.addItem("Method");
		dropdown.addItem("Interface Method");
		dropdown.addItem("Field reference");
		dropdown.addItem("Float");
		dropdown.addItem("Double");
		dropdown.addItem("Integer");
		dropdown.addItem("Long");
		dropdown.addItem("String");
		dropdown.addItem("Name and type");
		dropdown.addItem("utf8");
		JPanel dropdownPanel = new JPanel();
		dropdownPanel.setLayout(new GridLayout(2, 1));
		dropdownPanel.add(dropdownLabel);
		dropdownPanel.add(dropdown);
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout(2, 1));
		mainPanel.add(mainTextLabel);
		mainPanel.add(mainText);
		JPanel sndPanel = new JPanel();
		sndPanel.setLayout(new GridLayout(2, 1));
		sndPanel.add(sndTextLabel);
		sndPanel.add(sndText);
		JPanel thirdPanel = new JPanel();
		thirdPanel.setLayout(new GridLayout(2, 1));
		thirdPanel.add(thirdTextLabel);
		thirdPanel.add(thirdText);
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(2, 1));
		buttonPanel.add(buttonLabel);
		buttonPanel.add(addButton);

		mainTextLabel.setText("Class name");
		add(dropdownPanel);
		add(mainPanel);
		add(sndPanel);
		add(thirdPanel);
		add(buttonPanel);
		sndText.setEditable(false);
		thirdText.setEditable(false);
		Border simpleBorder = BorderFactory.createEtchedBorder();
		Border border = BorderFactory.createTitledBorder(simpleBorder,
				"Add constant");
		this.setBorder(border);
		dropdown.addActionListener(this);
		dropdown.setActionCommand("select");
		addButton.addActionListener(this);
		addButton.setActionCommand("add");
	}

	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == dropdown) {
			int selected = dropdown.getSelectedIndex();

			switch (selected) {
			case 0:
				mainTextLabel.setText("Class name");
				sndTextLabel.setText("");
				thirdTextLabel.setText("");
				thirdText.setEditable(false);
				sndText.setEditable(false);
				break;
			case 1:
				mainTextLabel.setText("Class name");
				sndTextLabel.setText("Method name");
				thirdTextLabel.setText("Method signature");
				thirdText.setEditable(true);
				sndText.setEditable(true);
				break;
			case 2:
				mainTextLabel.setText("Class name");
				sndTextLabel.setText("Interface method name");
				thirdTextLabel.setText("Interface method signature");
				thirdText.setEditable(true);
				sndText.setEditable(true);
				break;
			case 3:
				mainTextLabel.setText("Class name");
				sndTextLabel.setText("Field name");
				thirdTextLabel.setText("Field signature");
				thirdText.setEditable(true);
				sndText.setEditable(true);
				break;
			case 4:
				mainTextLabel.setText("Float");
				sndTextLabel.setText("");
				thirdTextLabel.setText("");
				thirdText.setEditable(false);
				sndText.setEditable(false);
				break;
			case 5:
				mainTextLabel.setText("Double");
				sndTextLabel.setText("");
				thirdTextLabel.setText("");
				thirdText.setEditable(false);
				sndText.setEditable(false);
				break;
			case 6:
				mainTextLabel.setText("Integer");
				sndTextLabel.setText("");
				thirdTextLabel.setText("");
				thirdText.setEditable(false);
				sndText.setEditable(false);
				break;
			case 7:
				mainTextLabel.setText("Long");
				sndTextLabel.setText("");
				thirdTextLabel.setText("");
				thirdText.setEditable(false);
				sndText.setEditable(false);
				break;
			case 8:
				mainTextLabel.setText("String");
				sndTextLabel.setText("");
				thirdTextLabel.setText("");
				thirdText.setEditable(false);
				sndText.setEditable(false);
				break;
			case 9:
				mainTextLabel.setText("Name");
				sndTextLabel.setText("Signature");
				thirdTextLabel.setText("");
				thirdText.setEditable(false);
				sndText.setEditable(true);
				break;
			case 10:
				mainTextLabel.setText("String");
				sndTextLabel.setText("");
				thirdTextLabel.setText("");
				thirdText.setEditable(false);
				sndText.setEditable(false);
				break;

			default:
				thirdText.setEditable(false);
				sndText.setEditable(false);

			}

		} else if (event.getSource() == addButton) {
			int selectedItem = dropdown.getSelectedIndex();

			String fileName = internalFrame.getFileName();
			String constInfo[] = new String[3];
			constInfo[0] = mainText.getText();
			constInfo[1] = sndText.getText();
			constInfo[2] = thirdText.getText();
			ClassSaver classSaver = null;
			switch (selectedItem) {
			case 0:
				classSaver = new ClassSaver(ClassSaver.SAVE_CONSTANT, fileName,
						constInfo, Constants.CONSTANT_Class);
				break;
			case 1:
				classSaver = new ClassSaver(ClassSaver.SAVE_CONSTANT, fileName,
						constInfo, Constants.CONSTANT_Methodref);
				break;
			case 2:
				classSaver = new ClassSaver(ClassSaver.SAVE_CONSTANT, fileName,
						constInfo, Constants.CONSTANT_InterfaceMethodref);
				break;
			case 3:
				classSaver = new ClassSaver(ClassSaver.SAVE_CONSTANT, fileName,
						constInfo, Constants.CONSTANT_Fieldref);
				break;
			case 4:
				classSaver = new ClassSaver(ClassSaver.SAVE_CONSTANT, fileName,
						constInfo, Constants.CONSTANT_Float);
				break;
			case 5:
				classSaver = new ClassSaver(ClassSaver.SAVE_CONSTANT, fileName,
						constInfo, Constants.CONSTANT_Double);
				break;
			case 6:
				classSaver = new ClassSaver(ClassSaver.SAVE_CONSTANT, fileName,
						constInfo, Constants.CONSTANT_Integer);
				break;
			case 7:
				classSaver = new ClassSaver(ClassSaver.SAVE_CONSTANT, fileName,
						constInfo, Constants.CONSTANT_Long);
				break;
			case 8:
				classSaver = new ClassSaver(ClassSaver.SAVE_CONSTANT, fileName,
						constInfo, Constants.CONSTANT_String);
				break;
			case 9:
				classSaver = new ClassSaver(ClassSaver.SAVE_CONSTANT, fileName,
						constInfo, Constants.CONSTANT_NameAndType);
				break;
			case 10:
				classSaver = new ClassSaver(ClassSaver.SAVE_CONSTANT, fileName,
						constInfo, Constants.CONSTANT_Utf8);
				break;

			}
			if (classSaver != null) {
				ProgressDialog progressDialog = new ProgressDialog(
						internalFrame.getParentFrame(), null,
						"Adding constant...");
				progressDialog.setRunnable(classSaver);
				progressDialog.setVisible(true);
				if (classSaver.exceptionOccured()) {
					ErrorReportWindow er = new ErrorReportWindow(internalFrame
							.getParentFrame(), classSaver.getExceptionVerbose(), "Adding constant failed");

					er.pack();
					GUIHelper.centerOnParentWindow(er, internalFrame
							.getParentFrame());
					er.setVisible(true);
				} else {

					internalFrame.getParentFrame().doReload();
				}
			}
		}
		// dropdown.get
		// eci.addConstant();
	}

}
