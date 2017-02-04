/*
 *
 */
package ee.ioc.cs.jbe.browser.detail.attributes.code;

import java.awt.CardLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.tree.TreePath;

import org.gjt.jclasslib.structures.ClassFile;
import org.gjt.jclasslib.structures.InvalidByteCodeException;
import org.gjt.jclasslib.structures.MethodInfo;
import org.gjt.jclasslib.structures.attributes.CodeAttribute;

import ee.ioc.cs.jbe.browser.AbstractDetailPane;
import ee.ioc.cs.jbe.browser.BrowserInternalFrame;
import ee.ioc.cs.jbe.browser.BrowserServices;
import ee.ioc.cs.jbe.browser.BrowserTreeNode;
import ee.ioc.cs.jbe.browser.codeedit.CodeGenerator;


// import javax.swing.undo.UndoManager;

import java.util.HashMap;

public class CodeEditPane extends AbstractDetailPane implements FocusListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3376865231556430458L;

	private HashMap<String, CodeEditArea> editPanes = new HashMap<String, CodeEditArea>();

	

	private BrowserInternalFrame internalFrame;

	public CodeEditPane(BrowserServices services) throws InvalidByteCodeException {
		super(services);
		internalFrame = (BrowserInternalFrame) services;
		ClassFile classFile = services.getClassFile();
		MethodInfo[] methods = classFile.getMethods();
		this.setLayout(new CardLayout());

		for (int i = 0; i < methods.length; i++) {
			String methodIndex = Integer.toString(i);
			//Integer methodIndex = new Integer(i);
			for (int j = 0; j < methods[i].getAttributes().length; j++) {
				if (methods[i].getAttributes()[j] instanceof CodeAttribute) {
					byte[] code = ((CodeAttribute) methods[i]
							.getAttributes()[j]).getCode();
					addEditPane(methodIndex, code, classFile);
					break;
				}
			}
		}

	}

	private void addEditPane(String methodIndex, byte[] code, ClassFile classFile) {
		CodeEditArea editArea = new CodeEditArea(methodIndex, code, classFile, internalFrame);

		JScrollPane scroll = new JScrollPane(editArea);
		scroll.setRowHeaderView(new LineNumberView(editArea));
		scroll.getVerticalScrollBar().setValue(10);
		// p.add(scroll);
		this.add(scroll, methodIndex);
		editPanes.put(methodIndex, editArea);
	}

	public void show(TreePath treePath) {
		if (internalFrame.isReloading()) {
			updateEditPanes();
		}
		//The panel's name is the index of the method in the panel 
		//It cannot be the name of the method since this isnt necessarily unique
		
		String methodIndex = Integer.toString(((BrowserTreeNode)treePath.getParentPath().getLastPathComponent()).getIndex());
		CardLayout cl = (CardLayout) this.getLayout();
		cl.show(this, methodIndex);

	}

	private void updateEditPanes() {
		internalFrame = (BrowserInternalFrame) services;
		ClassFile classFile = services.getClassFile();
		MethodInfo[] methods = classFile.getMethods();
		CodeGenerator cg = new CodeGenerator();
		for (int i = 0; i < methods.length; i++) {
			String methodIndex = Integer.toString(i);
			byte[] code = ((CodeAttribute) methods[i].getAttributes()[0])
					.getCode();
			if (editPanes.get(methodIndex) == null ) {
				addEditPane(methodIndex, code, classFile);
			}
			((JEditorPane) editPanes.get(methodIndex)).setText(cg.makeMethod(
					code, classFile));
		}


	}

	
	
	protected void setupComponent() {

	}

	public void focusGained(FocusEvent arg0) {
	}

	public void focusLost(FocusEvent arg0) {

	}

	public HashMap getEditPanes() {
		return editPanes;
	}

	


}
