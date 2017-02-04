package ee.ioc.cs.jbe.browser.detail;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.tree.TreePath;

import ee.ioc.cs.jbe.browser.AbstractDetailPane;
import ee.ioc.cs.jbe.browser.BrowserInternalFrame;
import ee.ioc.cs.jbe.browser.BrowserServices;


public class AttributeGeneralPane extends AbstractDetailPane implements
		ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3907856125379924109L;
	private BrowserInternalFrame internalFrame;

	public AttributeGeneralPane(BrowserServices services) {
		super(services);
		internalFrame = (BrowserInternalFrame) services;
	}

	public void show(TreePath treePath) {

	}

	protected void setupComponent() {	}

	public void actionPerformed(ActionEvent event) {

		

	}

}
