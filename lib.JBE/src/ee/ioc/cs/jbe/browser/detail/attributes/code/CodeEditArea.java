package ee.ioc.cs.jbe.browser.detail.attributes.code;

import java.awt.Font;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JEditorPane;
import javax.swing.KeyStroke;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import org.gjt.jclasslib.structures.ClassFile;

import ee.ioc.cs.jbe.browser.BrowserInternalFrame;
import ee.ioc.cs.jbe.browser.BrowserMDIFrame;
import ee.ioc.cs.jbe.browser.codeedit.CodeGenerator;


public class CodeEditArea extends JEditorPane {
	public UndoManager undo = new UndoManager();
	
	private static final long serialVersionUID = -5922040899795041012L;
	BrowserInternalFrame internalFrame;
	
	CodeEditArea(String methodIndex, byte[] code, ClassFile classFile, BrowserInternalFrame internalFrame)  {
		this.internalFrame = internalFrame;
		setFont(new Font("monospaced", Font.PLAIN, 14));
		setCaretPosition(0);
		setEditable(true);
		CodeGenerator cg = new CodeGenerator();
		setText(cg.makeMethod(code, classFile));
		getDocument().addUndoableEditListener(new UndoRedoListener());
		getActionMap().put("Undo", new AbstractAction("Undo") {
			public void actionPerformed(ActionEvent evt) {
				doUndo();
			}
		});

		// Bind the undo action to ctl-Z
		getInputMap().put(KeyStroke.getKeyStroke("control Z"), "Undo");

		// Create a redo action and add it to the text component
		getActionMap().put("Redo", new AbstractAction("Redo") {
			public void actionPerformed(ActionEvent evt) {
				doRedo();
			}

		});

		// Bind the redo action to ctl-Y
		getInputMap().put(KeyStroke.getKeyStroke("control Y"), "Redo");
		
	}
	public void doUndo() {
		try {
			if (undo.canUndo()) {
				undo.undo();
			}
			if (undo.canUndo()) {
				((BrowserMDIFrame)internalFrame.getParentFrame()).actionUndo.setEnabled(true);
			} else  {
				((BrowserMDIFrame)internalFrame.getParentFrame()).actionUndo.setEnabled(false);
			}
			if (undo.canRedo()) {
				((BrowserMDIFrame)internalFrame.getParentFrame()).actionRedo.setEnabled(true);
			} else  {
				((BrowserMDIFrame)internalFrame.getParentFrame()).actionRedo.setEnabled(false);
			}
		} catch (CannotUndoException e) {
		}
	}
		
	public void doRedo() {
		try {
			if (undo.canRedo()) {
				undo.redo();
			}
			if (undo.canUndo()) {
				((BrowserMDIFrame)internalFrame.getParentFrame()).actionUndo.setEnabled(true);
			} else  {
				((BrowserMDIFrame)internalFrame.getParentFrame()).actionUndo.setEnabled(false);
			}
			if (undo.canRedo()) {
				((BrowserMDIFrame)internalFrame.getParentFrame()).actionRedo.setEnabled(true);
			} else  {
				((BrowserMDIFrame)internalFrame.getParentFrame()).actionRedo.setEnabled(false);
			}
		} catch (CannotRedoException e) {
		}
	}
	class UndoRedoListener implements UndoableEditListener {
		public void undoableEditHappened(UndoableEditEvent e) {
			// Remember the edit and update the menus
			undo.addEdit(e.getEdit());
			if (undo.canUndo()) {
				((BrowserMDIFrame)internalFrame.getParentFrame()).actionUndo.setEnabled(true);
			} else  {
				((BrowserMDIFrame)internalFrame.getParentFrame()).actionUndo.setEnabled(false);
			}
			if (undo.canRedo()) {
				((BrowserMDIFrame)internalFrame.getParentFrame()).actionRedo.setEnabled(true);
			} else  {
				((BrowserMDIFrame)internalFrame.getParentFrame()).actionRedo.setEnabled(false);
			}
			/*
			 * internalFrame.getParentFrame(). undoAction.updateUndoState();
			 * redoAction.updateRedoState();
			 */

		}
	}
}
