package ee.ioc.cs.jbe.browser.detail.attributes.code;

import java.awt.CardLayout;
import java.util.HashMap;

import javax.swing.JPanel;
import javax.swing.tree.TreePath;

import org.gjt.jclasslib.structures.ClassFile;
import org.gjt.jclasslib.structures.MethodInfo;
import org.gjt.jclasslib.structures.attributes.CodeAttribute;

import ee.ioc.cs.jbe.browser.BrowserInternalFrame;
import ee.ioc.cs.jbe.browser.BrowserServices;
import ee.ioc.cs.jbe.browser.BrowserTreeNode;


public class QuantDetailPane extends JPanel {

	private HashMap<String, MiscDetailPane> miscPanes = new HashMap<String, MiscDetailPane>();
	private BrowserInternalFrame internalFrame;
	private BrowserServices services;


	public QuantDetailPane(BrowserServices services) {
		internalFrame = (BrowserInternalFrame) services;
		this.services = services;
		ClassFile classFile = services.getClassFile();
		MethodInfo[] methods = classFile.getMethods();
		this.setLayout(new CardLayout());

		for (int i = 0; i < methods.length; i++) {
			String methodIndex = Integer.toString(i);
			int codeLength = 0;
			int maxStack = 0;
			int maxLocals = 0;
			for (int j = 0; j < methods[i].getAttributes().length; j++) {
				if (methods[i].getAttributes()[j] instanceof CodeAttribute) {
					CodeAttribute ca = ((CodeAttribute) methods[i].getAttributes()[j]);
					codeLength = ca.getCode().length;
					maxStack = ca.getMaxStack();
					maxLocals = ca.getMaxLocals();
					break;
				}
			}

			addMiscPane(methodIndex, maxStack, maxLocals, codeLength, classFile);
		}
	}

	private void addMiscPane(String methodName, int maxStack, int maxLocals, int codeLength,
			ClassFile classFile) {
		MiscDetailPane mdp = new MiscDetailPane(internalFrame);
		mdp.setMaxStack(maxStack);
		mdp.setMaxLocals(maxLocals);
		mdp.setCodeLength(codeLength);
		this.add(mdp, methodName);
		miscPanes.put(methodName, mdp);
	}

	public void show(TreePath treePath) {
		if (internalFrame.isReloading()) {
			updateEditPanes();
		}
		String methodIndex = Integer.toString(((BrowserTreeNode)treePath.getParentPath().getLastPathComponent()).getIndex());
		CardLayout cl = (CardLayout) this.getLayout();
		cl.show(this, methodIndex);

	}

	private void updateEditPanes() {
		internalFrame = (BrowserInternalFrame) services;
		ClassFile classFile = services.getClassFile();
		MethodInfo[] methods = classFile.getMethods();
		
		for (int i = 0; i < methods.length; i++) {
			String methodIndex = Integer.toString(i);
			int codeLength = 0;
			int maxStack = 0;
			int maxLocals = 0;
			for (int j = 0; j < methods[i].getAttributes().length; j++) {
				if (methods[i].getAttributes()[j] instanceof CodeAttribute) {
					CodeAttribute ca = ((CodeAttribute) methods[i].getAttributes()[j]);
					codeLength = ca.getCode().length;
					maxStack = ca.getMaxStack();
					maxLocals = ca.getMaxLocals();
					break;
				}
			}
			if (miscPanes.get(methodIndex) == null ) {
				addMiscPane(methodIndex, maxStack, maxLocals, codeLength, classFile);
			}
			MiscDetailPane mdp = miscPanes.get(methodIndex);
			mdp.setCodeLength(codeLength);
			mdp.setMaxStack(maxStack);
			mdp.setMaxLocals(maxLocals);
			
		}

	}


	public HashMap getMiscPanes() {
		return miscPanes;
	}
}
