/*
 *
 */
package ee.ioc.cs.jbe.browser.detail.attributes;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ee.ioc.cs.jbe.browser.detail.attributes.code.ErrorReportWindow;
import org.gjt.jclasslib.util.GUIHelper;
import org.gjt.jclasslib.util.ProgressDialog;

import ee.ioc.cs.jbe.browser.BrowserInternalFrame;
import ee.ioc.cs.jbe.browser.BrowserServices;
import ee.ioc.cs.jbe.browser.codeedit.ClassSaver;


public class AddExceptionPane extends JPanel implements ActionListener {
	private JButton addButton;

	private JTextField startPcField;

	private JTextField endPcField;

	private JTextField handlerPcField;

	private JTextField catchTypeField;

	private BrowserInternalFrame internalFrame;

	private int methodIndex;

	public AddExceptionPane(BrowserServices services) {
		internalFrame = (BrowserInternalFrame) services;
		JPanel addPanel = new JPanel(new BorderLayout());
		addButton = new JButton("Add Exception");
		addPanel.add(new JLabel(" "), BorderLayout.NORTH);
		addPanel.add(addButton);

		JPanel startPcPanel = new JPanel(new BorderLayout());
		startPcField = new JTextField(5);
		startPcPanel.add(new JLabel("Start PC"), BorderLayout.NORTH);
		startPcPanel.add(startPcField);

		JPanel endPcPanel = new JPanel(new BorderLayout());
		endPcField = new JTextField(5);
		endPcPanel.add(new JLabel("End PC"), BorderLayout.NORTH);
		endPcPanel.add(endPcField);

		JPanel handlerPcPanel = new JPanel(new BorderLayout());
		handlerPcField = new JTextField(5);
		handlerPcPanel.add(new JLabel("Handler PC"), BorderLayout.NORTH);
		handlerPcPanel.add(handlerPcField);

		JPanel catchTypePanel = new JPanel(new BorderLayout());
		catchTypeField = new JTextField(15);
		catchTypePanel.add(new JLabel("Catch type"), BorderLayout.NORTH);
		catchTypePanel.add(catchTypeField);

		add(startPcPanel);
		add(endPcPanel);
		add(handlerPcPanel);
		add(catchTypePanel);
		add(addPanel);
		addButton.addActionListener(this);
	}

	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == addButton) {
			String fileName = internalFrame.getFileName();
			try {
				int startPc = Integer.parseInt(startPcField.getText());
				int endPc = Integer.parseInt(endPcField.getText());
				int handlerPc = Integer.parseInt(handlerPcField.getText());
				String handlerClass = catchTypeField.getText();
				ClassSaver cs = new ClassSaver(
						ClassSaver.SAVE_EXCEPTION, fileName, methodIndex,
						startPc, endPc, handlerPc, handlerClass);
				

				ProgressDialog progressDialog = new ProgressDialog(internalFrame
						.getParentFrame(), null, "Adding exception...");

				progressDialog.setRunnable(cs);
				progressDialog.setVisible(true);
				
				if (cs.exceptionOccured()) {
					ErrorReportWindow er = new ErrorReportWindow(internalFrame
							.getParentFrame(), cs.getExceptionVerbose(), "Adding exception failed");

					er.pack();
					GUIHelper.centerOnParentWindow(er, internalFrame
							.getParentFrame());
					er.setVisible(true);
				} else {
					internalFrame.getParentFrame().doReload();
				}
				
			} catch (NumberFormatException nfe) {
				ErrorReportWindow erw = new ErrorReportWindow(internalFrame.getParentFrame(),
						"Illegal arguments for exception constructor", "Adding exception failed");
				GUIHelper.centerOnParentWindow(erw, internalFrame
						.getParentFrame());
				erw.setLocationRelativeTo(this);
				erw.pack();
				erw.setVisible(true);
			}

		}
	}

	public void setMethod(int methodIndex) {
		this.methodIndex = methodIndex;
	}
}
