/*
    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public
    License as published by the Free Software Foundation; either
    version 2 of the license, or (at your option) any later version.
*/

package ee.ioc.cs.jbe.browser;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.Action;
import javax.swing.JOptionPane;

import ee.ioc.cs.jbe.browser.config.classpath.FindResult;
import ee.ioc.cs.jbe.browser.config.window.BrowserPath;
import ee.ioc.cs.jbe.browser.config.window.WindowState;
import org.gjt.jclasslib.io.ClassFileReader;
import org.gjt.jclasslib.mdi.BasicDesktopManager;
import org.gjt.jclasslib.mdi.BasicInternalFrame;
import org.gjt.jclasslib.structures.ClassFile;
import org.gjt.jclasslib.structures.InvalidByteCodeException;
import org.gjt.jclasslib.util.FileUtils;
import org.gjt.jclasslib.util.GUIHelper;

/**
 * A child window of the class file browser application.
 *
 * @author <a href="mailto:jclasslib@ej-technologies.com">Ingo Kegel</a>
 * @version $Revision: 1.1 $ $Date: 2006/09/25 16:00:58 $
 */
public class BrowserInternalFrame extends BasicInternalFrame
        implements BrowserServices {


    /**
     * Constructor for creating a derived <tt>BasicInternalFrame</tt> with
     * an initialization parameter.
     */
    public static final Class[] CONSTRUCTOR_ARGUMENTS =
            new Class[]{BasicDesktopManager.class, WindowState.class};

    private String fileName;
    private ClassFile classFile;
    private String backupFile;
    
    // Visual Components

    private BrowserComponent browserComponent;

	private boolean reloading = false;

    /**
     * Constructor.
     *
     * @param desktopManager the associated desktop manager
     * @param windowState    the window state object. The frame will load the class file from
     *                       information present within this object.
     */
    public BrowserInternalFrame(BasicDesktopManager desktopManager, WindowState windowState) {
        super(desktopManager, windowState.getFileName());
        fileName = windowState.getFileName();
        doBackup(fileName);
        setFrameIcon(BrowserMDIFrame.ICON_APPLICATION);
        readClassFile();
        setupInternalFrame(windowState.getBrowserPath());
    }

    private void doBackup(String fileName2) {
    	
    	try {
    		File source = new File (fileName);
			File temp = File.createTempFile("backup", ".class");
			FileUtils.copy(source, temp);
			backupFile = temp.getAbsolutePath();
			getParentFrame().addTempFile(backupFile);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	public Object getInitParam() {
        WindowState windowState = new WindowState(fileName, browserComponent.getBrowserPath(""));
        return windowState;
    }

    // Browser services

    public ClassFile getClassFile() {
        return classFile;
    }

    public void activate() {

        // force sync of toolbar state with this frame
        desktopManager.getDesktopPane().setSelectedFrame(this);
    }

    public BrowserComponent getBrowserComponent() {
        return browserComponent;
    }

    public Action getActionBackward() {
        return getParentFrame().getActionBackward();
    }

    public Action getActionForward() {
        return getParentFrame().getActionForward();
    }

    public void openClassFile(String className, BrowserPath browserPath) {

        FindResult findResult = getParentFrame().getConfig().findClass(className);
        while (findResult == null) {
            int result = GUIHelper.showOptionDialog(getParentFrame(),
                    "The class " + className + " could not be found.\n" +
                    "You can check your classpath configuration and try again.",
                    new String[]{"Setup classpath", "Cancel"},
                    JOptionPane.WARNING_MESSAGE);
            if (result == 0) {
                getParentFrame().getActionSetupClasspath().actionPerformed(new ActionEvent(this, 0, null));
                findResult = getParentFrame().getConfig().findClass(className);
            } else {
                return;
            }
        }

        BrowserInternalFrame frame = (BrowserInternalFrame)desktopManager.getOpenFrame(new WindowState(findResult.getFileName()));
        if (frame != null) {
            try {
                frame.setSelected(true);
                frame.browserComponent.setBrowserPath(browserPath);
                desktopManager.scrollToVisible(frame);
            } catch (PropertyVetoException e) {
            }
        } else {
            WindowState windowState = new WindowState(findResult.getFileName(), browserPath);
            frame = new BrowserInternalFrame(desktopManager, windowState);
            if (frame != null) {
                if (isMaximum()) {
                    try {
                        frame.setMaximum(true);
                    } catch (PropertyVetoException ex) {
                    }
                } else {
                    desktopManager.scrollToVisible(frame);
                }
            }
        }
    }

    public boolean canOpenClassFiles() {
        return true;
    }

    /**
     * Reload class file.
     * @param categoryName 
     */
    public void reload(String categoryName) {
    	reloading = true;
        readClassFile();
        browserComponent.rebuild(categoryName);
    }

    /**
     * Get the file name for the displayed class file.
     *
     * @return the file name
     */
    public String getFileName() {
        return fileName;
    }

    private void setupInternalFrame(BrowserPath browserPath) {

        setTitle(fileName);

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        browserComponent = new BrowserComponent(this);
        contentPane.add(browserComponent, BorderLayout.CENTER);

        setupInternalFrame();
        browserComponent.setBrowserPath(browserPath);

    }

    public BrowserMDIFrame getParentFrame() {
        return (BrowserMDIFrame)desktopManager.getParentFrame();
    }

    private void readClassFile() {
        try {
            int index = fileName.indexOf('!');
            if (index > -1) {
                String jarFileName = fileName.substring(0, index);
                String classFileName = fileName.substring(index + 1);
                JarFile jarFile = new JarFile(jarFileName);
                JarEntry jarEntry = jarFile.getJarEntry(classFileName);
                if (jarEntry != null) {
                    classFile = ClassFileReader.readFromInputStream(jarFile.getInputStream(jarEntry));
                }
            } else {
                classFile = ClassFileReader.readFromFile(new File(fileName));
            }
        } catch (InvalidByteCodeException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }



	public boolean isReloading() {
		return reloading;
	}

	public void setReloading(boolean b) {
		reloading = b;
		
	}

	public String getBackupFile() {
		return backupFile;
	}

}
