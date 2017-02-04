/*
    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public
    License as published by the Free Software Foundation; either
    version 2 of the license, or (at your option) any later version.
*/

package ee.ioc.cs.jbe.browser;

import org.gjt.jclasslib.mdi.BasicDesktopManager;

import javax.swing.event.InternalFrameEvent;

/**
    The desktop manager for the class file browser application.
 
    @author <a href="mailto:jclasslib@ej-technologies.com">Ingo Kegel</a>
    @version $Revision: 1.1 $ $Date: 2006/09/25 16:00:58 $
*/
public class BrowserDesktopManager extends BasicDesktopManager {

    /**
        Constructor.
        @param parentFrame the parent frame
     */
    public BrowserDesktopManager(BrowserMDIFrame parentFrame) {
        super(parentFrame);
    }

    public void internalFrameActivated(InternalFrameEvent event) {
        BrowserInternalFrame internalFrame = (BrowserInternalFrame)event.getInternalFrame();
        actionStatus(internalFrame);
        internalFrame.getBrowserComponent().checkSelection();
    }

    public void internalFrameDeactivated(InternalFrameEvent event) {
        actionStatus(null);
    }


    private void actionStatus(BrowserInternalFrame internalFrame) {

        BrowserMDIFrame browserParentFrame = (BrowserMDIFrame)parentFrame;

        if (internalFrame != null) {
            internalFrame.getBrowserComponent().getHistory().updateActions();
        } else {
        	browserParentFrame.getActionVerify().setEnabled(false);
        	browserParentFrame.getActionRevert().setEnabled(false);
        	browserParentFrame.getActionReload().setEnabled(false);
            browserParentFrame.getActionBackward().setEnabled(false);
            browserParentFrame.getActionForward().setEnabled(false);
        }
        browserParentFrame.getActionReload().setEnabled(internalFrame != null);
        browserParentFrame.getActionVerify().setEnabled(internalFrame != null);
        browserParentFrame.getActionRevert().setEnabled(internalFrame != null);
    }
}
