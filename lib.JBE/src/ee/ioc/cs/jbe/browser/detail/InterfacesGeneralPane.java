package ee.ioc.cs.jbe.browser.detail;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.tree.TreePath;

import ee.ioc.cs.jbe.browser.detail.attributes.code.ErrorReportWindow;
import org.gjt.jclasslib.util.GUIHelper;
import org.gjt.jclasslib.util.ProgressDialog;

import ee.ioc.cs.jbe.browser.AbstractDetailPane;
import ee.ioc.cs.jbe.browser.BrowserInternalFrame;
import ee.ioc.cs.jbe.browser.BrowserServices;
import ee.ioc.cs.jbe.browser.codeedit.ClassSaver;


public class InterfacesGeneralPane extends AbstractDetailPane implements
		ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -336727149784220095L;

	private JButton addButton;

	private JTextField name;

	private BrowserInternalFrame internalFrame;

	public InterfacesGeneralPane(BrowserServices services) {
		super(services);
		internalFrame = (BrowserInternalFrame) services;
	}

	public void show(TreePath treePath) {

	}

	protected void setupComponent() {
		addButton = new JButton("Add interface");
		name = new JTextField(15);

		JPanel namePanel = new JPanel();
		namePanel.setLayout(new GridLayout(2, 1));
		namePanel.add(new JLabel("Interface name"));
		namePanel.add(name);
		add(namePanel);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(2, 1));
		
		buttonPanel.add(new JLabel(""));
		buttonPanel.add(addButton);
        Border simpleBorder = BorderFactory.createEtchedBorder();
        Border border = BorderFactory.createTitledBorder(simpleBorder,
                "Add interface");
		this.setBorder(border);		
		add(buttonPanel);
		addButton.addActionListener(this);
		addButton.setActionCommand("add");
	}

	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == addButton) {

			String fileName = internalFrame.getFileName();
			String interfaceName  = name.getText();
			ClassSaver classSaver = new ClassSaver(ClassSaver.ADD_INTERFACE, fileName, interfaceName);
			ProgressDialog progressDialog = new ProgressDialog(
					internalFrame.getParentFrame(), null,
					"Adding interface...");
			progressDialog.setRunnable(classSaver);
			progressDialog.setVisible(true);
			if (classSaver.exceptionOccured()) {
				ErrorReportWindow er = new ErrorReportWindow(internalFrame
						.getParentFrame(), classSaver.getExceptionVerbose(), "Adding interface failed");

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
