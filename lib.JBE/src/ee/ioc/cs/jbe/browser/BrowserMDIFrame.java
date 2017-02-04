/*
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public
 License as published by the Free Software Foundation; either
 version 2 of the license, or (at your option) any later version.
 */

package ee.ioc.cs.jbe.browser;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyVetoException;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.tree.TreePath;

import ee.ioc.cs.jbe.browser.config.classpath.ClasspathArchiveEntry;
import ee.ioc.cs.jbe.browser.config.classpath.ClasspathBrowser;
import ee.ioc.cs.jbe.browser.config.classpath.ClasspathSetupDialog;
import ee.ioc.cs.jbe.browser.config.classpath.FindResult;
import ee.ioc.cs.jbe.browser.config.window.WindowState;

import org.gjt.jclasslib.mdi.BasicDesktopManager;
import org.gjt.jclasslib.mdi.BasicFileFilter;
import org.gjt.jclasslib.mdi.BasicMDIFrame;
import org.gjt.jclasslib.structures.ClassFile;
import org.gjt.jclasslib.structures.InvalidByteCodeException;
import org.gjt.jclasslib.util.FileUtils;
import org.gjt.jclasslib.util.GUIHelper;

import ee.ioc.cs.jbe.browser.codeedit.VerifierDisplay;
import ee.ioc.cs.jbe.browser.config.BrowserConfig;
import ee.ioc.cs.jbe.browser.detail.attributes.code.CodeEditArea;
import ee.ioc.cs.jbe.browser.detail.attributes.code.CodeEditPane;


/**
 * MDI Frame and entry point for the class file browser application.
 * 
 * @author <a href="mailto:jclasslib@ej-technologies.com">Ingo Kegel</a>
 *         Modified by Ando Saabas
 * @version $Revision: 1.1 $ $Date: 2006/09/25 16:00:58 $
 */
public class BrowserMDIFrame extends BasicMDIFrame{

	static final ImageIcon ICON_APPLICATION = loadIcon("jbe.png");

	private static final String SETTINGS_WORKSPACE_CHOOSER_PATH = "workspaceChooserPath";

	private static final String SETTINGS_CLASSES_CHOOSER_PATH = "classesChooserPath";

	private static final ImageIcon ICON_OPEN_CLASS_FILE = loadIcon("open_small.png");

	private static final ImageIcon ICON_OPEN_CLASS_FILE_LARGE = loadIcon("open_large.png");

	private static final ImageIcon ICON_BACKWARD = loadIcon("browser_backward_small.png");

	private static final ImageIcon ICON_BACKWARD_LARGE = loadIcon("browser_backward_large.png");

	private static final ImageIcon ICON_FORWARD = loadIcon("browser_forward_small.png");

	private static final ImageIcon ICON_FORWARD_LARGE = loadIcon("browser_forward_large.png");

	private static final ImageIcon ICON_RELOAD = loadIcon("reload_small.png");

	private static final ImageIcon ICON_RELOAD_LARGE = loadIcon("reload_large.png");

	private static final ImageIcon ICON_WEB = loadIcon("web_small.png");

//	private static final ImageIcon ICON_WEB_LARGE = loadIcon("web_large.png");

//	private static final ImageIcon ICON_BROWSE_CLASSPATH = loadIcon("tree_small.png");

//	private static final ImageIcon ICON_BROWSE_CLASSPATH_LARGE = loadIcon("tree_large.png");

	private static final ImageIcon ICON_HELP = loadIcon("help.png");

	private static final ImageIcon ICON_VERIFY_SMALL = loadIcon("verify_small.png");

	private static final ImageIcon ICON_VERIFY_LARGE = loadIcon("verify_large.png");

	private static final ImageIcon ICON_REVERT_SMALL = loadIcon("revert.png");

	private static final ImageIcon ICON_REVERT_LARGE = loadIcon("revert.png");
	
	private static final ImageIcon ICON_UNDO_SMALL = loadIcon("undo_small.png");

	private static final ImageIcon ICON_REDO_SMALL = loadIcon("redo_small.png");

	
	/**
	 * Load an icon from the <tt>images</tt> directory.
	 * 
	 * @param fileName
	 *            the file name for the icon
	 * @return the icon
	 */
	public static ImageIcon loadIcon(String fileName) {

		URL imageURL = BrowserMDIFrame.class.getResource("images/" + fileName);
		return new ImageIcon(imageURL);
	}

	private Action actionOpenClassFile;

	private Action actionBrowseClasspath;

	private Action actionSetupClasspath;

	private Action actionNewWorkspace;

	//private Action actionOpenWorkspace;

	//private Action actionSaveWorkspace;

	private Action actionSaveWorkspaceAs;

	private Action actionQuit;

	private Action actionShowHomepage;

	//private Action actionShowEJT;

	private Action actionBackward;

	private Action actionForward;

	private Action actionReload;

	private Action actionShowHelp;

	private Action actionAbout;

	private Action actionVerifyClass;
	
	private Action actionRevertClass;

	public UndoAction actionUndo;

	public RedoAction actionRedo;

	private File workspaceFile;

	private String workspaceChooserPath = "";

	private String classesChooserPath = "";

	private BrowserConfig config;

	// Visual Components

	//private JFileChooser workspaceFileChooser;

	private JFileChooser classesFileChooser;

	private RecentMenu recentMenu;

	private ClasspathSetupDialog classpathSetupDialog;

	private ClasspathBrowser classpathBrowser;

	private ClasspathBrowser jarBrowser;

	private HashSet<String> tempFiles = new HashSet<String>();

	/**
	 * Constructor.
	 */
	public BrowserMDIFrame() {

		doNewWorkspace();

		recentMenu = new RecentMenu(this);
		loadSettings();
		setupActions();
		setupMenu();
		setupFrame();

	}

	/**
	 * Get the current browser config.
	 * 
	 * @return the browser config
	 */
	public BrowserConfig getConfig() {
		return config;
	}

	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			desktopManager.showAll();
		}
	}

	/**
	 * Get the action for displaying the classpath setup dialog.
	 * 
	 * @return the action
	 */
	public Action getActionSetupClasspath() {
		return actionSetupClasspath;
	}

	/**
	 * Get the action for going backward in the navigation history.
	 * 
	 * @return the action
	 */
	public Action getActionBackward() {
		return actionBackward;
	}

	/**
	 * Get the action for going forward in the navigation history.
	 * 
	 * @return the action
	 */
	public Action getActionForward() {
		return actionForward;
	}

	/**
	 * Get the action for reloading the current frame.
	 * 
	 * @return the action
	 */
	public Action getActionReload() {
		return actionReload;
	}

	/**
	 * Get the last path for the classes file chooser.
	 * 
	 * @return the path
	 */
	public String getClassesChooserPath() {
		return classesChooserPath;
	}

	/**
	 * Set the last path for the classes file chooser.
	 * 
	 * @param classesChooserPath
	 *            the path
	 */
	public void setClassesChooserPath(String classesChooserPath) {
		this.classesChooserPath = classesChooserPath;
	}

	/**
	 * Open an internal frame for a given file.
	 * 
	 * @param file
	 *            the file
	 * @return the created internal frame
	 */
	public BrowserInternalFrame openClassFromFile(File file) {

		BrowserInternalFrame frame = new BrowserInternalFrame(desktopManager,
				new WindowState(file.getPath()));

		ClassFile classFile = frame.getClassFile();

		if (classFile != null) {
			try {
				String className = classFile.getThisClassName();
				String[] pathComponents = className.split("/");
				File currentDirectory = file.getParentFile();
				boolean validClasspathEntry = true;
				for (int i = pathComponents.length - 2; i >= 0; i--) {
					String pathComponent = pathComponents[i];
					if (!currentDirectory.getName().equals(pathComponent)) {
						validClasspathEntry = false;
						break;
					}
					currentDirectory = currentDirectory.getParentFile();
				}
				if (validClasspathEntry) {
					config.addClasspathDirectory(currentDirectory.getPath());
				}
			} catch (InvalidByteCodeException e) {
			}
		}
		actionVerifyClass.setEnabled(true);
		actionRevertClass.setEnabled(true);
		return frame;
	}

	protected void doQuit() {
		saveSettings();
		removeTempFiles();
		super.doQuit();
	}

	protected BasicDesktopManager createDesktopManager() {
		return new BrowserDesktopManager(this);
	}

	protected Class[] getFrameConstructorArguments(Class frameClass) {
		return BrowserInternalFrame.CONSTRUCTOR_ARGUMENTS;
	}

	private void setupActions() {

		actionOpenClassFile = new DefaultAction("Open class file",
				ICON_OPEN_CLASS_FILE);
		actionOpenClassFile.putValue(Action.SHORT_DESCRIPTION,
				"Open a class file");

		//actionBrowseClasspath = new DefaultAction("Browse classpath",
		//		ICON_BROWSE_CLASSPATH);
		//actionBrowseClasspath.putValue(Action.SHORT_DESCRIPTION,
		//		"Browse the current classpath to open a class file");

		//actionSetupClasspath = new DefaultAction("Setup classpath",
		//		GUIHelper.ICON_EMPTY);
		//actionSetupClasspath.putValue(Action.SHORT_DESCRIPTION,
		//		"Configure the classpath");

		// actionNewWorkspace = new DefaultAction("New workspace",
		// GUIHelper.ICON_EMPTY);
		// actionNewWorkspace.putValue(Action.SHORT_DESCRIPTION, "Close all
		// frames and open a new workspace");

		// actionOpenWorkspace = new DefaultAction("Open workspace",
		// ICON_OPEN_WORKSPACE);
		// actionOpenWorkspace.putValue(Action.SHORT_DESCRIPTION, "Open
		// workspace from disk");

		// actionSaveWorkspace = new DefaultAction("Save workspace",
		// ICON_SAVE_WORKSPACE);
		// actionSaveWorkspace.putValue(Action.SHORT_DESCRIPTION, "Save current
		// workspace to disk");

		// actionSaveWorkspaceAs = new DefaultAction("Save workspace as",
		// GUIHelper.ICON_EMPTY);
		// actionSaveWorkspaceAs.putValue(Action.SHORT_DESCRIPTION, "Save
		// current workspace to a different file");
		// actionSaveWorkspaceAs.setEnabled(false);

		actionQuit = new DefaultAction("Quit", GUIHelper.ICON_EMPTY);

		actionBackward = new DefaultAction("Backward", ICON_BACKWARD);
		actionBackward.putValue(Action.SHORT_DESCRIPTION,
				"Move backward in the navigation history");
		actionBackward.setEnabled(false);

		actionForward = new DefaultAction("Forward", ICON_FORWARD);
		actionForward.putValue(Action.SHORT_DESCRIPTION,
				"Move forward in the navigation history");
		actionForward.setEnabled(false);

		actionReload = new DefaultAction("Reload", ICON_RELOAD);
		actionReload.putValue(Action.SHORT_DESCRIPTION, "Reload class file");
		actionReload.setEnabled(false);

		actionVerifyClass = new DefaultAction("Verify class file",
				ICON_VERIFY_SMALL);
		actionVerifyClass.putValue(Action.SHORT_DESCRIPTION,
				"Verify class file");
		actionVerifyClass.setEnabled(false);

		
		actionRevertClass = new DefaultAction("Revert to original class file",
				ICON_REVERT_SMALL);
		actionRevertClass.putValue(Action.SHORT_DESCRIPTION,
				"Revert to original class file");
		actionRevertClass.setEnabled(false);
		
		
		actionShowHomepage = new DefaultAction("JBE webpage", ICON_WEB);
		actionShowHomepage.putValue(Action.SHORT_DESCRIPTION,
				"Visit jclasslib on the web");


		actionShowHelp = new DefaultAction("Show help", ICON_HELP);
		actionShowHelp.putValue(Action.SHORT_DESCRIPTION,
				"Show the JBE documentation");

		actionAbout = new DefaultAction("About Java Bytecode Editor",
				GUIHelper.ICON_EMPTY);
		actionAbout.putValue(Action.SHORT_DESCRIPTION,
				"About Java Bytecode Editor");

		actionUndo = new UndoAction("Undo typing", ICON_UNDO_SMALL);
		actionUndo.putValue(Action.SHORT_DESCRIPTION, "Undo text typing");

		actionRedo = new RedoAction("Redo typing", ICON_REDO_SMALL);
		actionRedo.putValue(Action.SHORT_DESCRIPTION, "Redo text typing");
	}

	private void setupMenu() {

		JMenuItem menuItem;
		JMenuBar menuBar = new JMenuBar();

		JMenu menuFile = new JMenu("File");
		menuFile.add(actionOpenClassFile);
		menuFile.add(actionVerifyClass);
		menuFile.addSeparator();
		// menuFile.add(actionNewWorkspace);
		// menuFile.add(actionOpenWorkspace);
		// menuFile.add(recentMenu);
		// menuFile.addSeparator();
		// menuFile.add(actionSaveWorkspace);
		// menuFile.add(actionSaveWorkspaceAs);
		// menuFile.addSeparator();
		menuFile.add(actionRevertClass);
		menuFile.addSeparator();
		menuFile.add(actionShowHomepage);
		menuFile.addSeparator();
		menuFile.add(actionQuit);

		JMenu menuEdit = new JMenu("Edit");
		menuItem = menuEdit.add(actionUndo);
		actionUndo.setEnabled(false);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
				Event.CTRL_MASK));
		menuItem = menuEdit.add(actionRedo);
		actionRedo.setEnabled(false);

		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y,
				Event.CTRL_MASK));

		//JMenu menuClasspath = new JMenu("Classpath");
		//menuClasspath.add(actionBrowseClasspath);
		//menuClasspath.add(actionSetupClasspath);

		JMenu menuBrowse = new JMenu("Browse");
		menuItem = menuBrowse.add(actionBackward);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,
				Event.ALT_MASK));
		menuItem = menuBrowse.add(actionForward);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,
				Event.ALT_MASK));

		menuBrowse.addSeparator();
		menuItem = menuBrowse.add(actionReload);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
				Event.CTRL_MASK));

		JMenu menuHelp = new JMenu("Help");
		menuItem = menuHelp.add(actionShowHelp);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		menuHelp.add(actionAbout);

		menuBar.add(menuFile);
		menuBar.add(menuEdit);
		//menuBar.add(menuClasspath);
		menuBar.add(menuBrowse);
		menuBar.add(menuWindow);
		menuBar.add(menuHelp);
		setJMenuBar(menuBar);

	}

	private void setupFrame() {

		Container contentPane = getContentPane();

		contentPane.add(buildToolbar(), BorderLayout.NORTH);
		setIconImage(ICON_APPLICATION.getImage());
	}

	private void updateTitle() {

		if (workspaceFile == null) {
			setTitle(BrowserApplication.APPLICATION_TITLE);
			if (actionSaveWorkspaceAs != null) {
				actionSaveWorkspaceAs.setEnabled(false);
			}
		} else {
			setTitle(BrowserApplication.APPLICATION_TITLE + " ["
					+ workspaceFile.getName() + "]");
		}
	}

	private JToolBar buildToolbar() {

		JToolBar toolBar = new JToolBar();
		toolBar.add(actionOpenClassFile).setIcon(ICON_OPEN_CLASS_FILE_LARGE);
		//toolBar.add(actionBrowseClasspath).setIcon(ICON_BROWSE_CLASSPATH_LARGE);
		toolBar.addSeparator();
		// toolBar.add(actionOpenWorkspace).setIcon(ICON_OPEN_WORKSPACE_LARGE);
		// toolBar.add(actionSaveWorkspace).setIcon(ICON_SAVE_WORKSPACE_LARGE);
		// toolBar.addSeparator();
		toolBar.add(actionBackward).setIcon(ICON_BACKWARD_LARGE);
		toolBar.add(actionForward).setIcon(ICON_FORWARD_LARGE);
		toolBar.addSeparator();
		toolBar.add(actionReload).setIcon(ICON_RELOAD_LARGE);
		toolBar.addSeparator();
		toolBar.add(actionVerifyClass).setIcon(ICON_VERIFY_LARGE);
		toolBar.addSeparator();
		toolBar.add(actionRevertClass).setIcon(ICON_REVERT_LARGE);
		//toolBar.addSeparator();
		//toolBar.add(actionShowHomepage).setIcon(ICON_WEB_LARGE);

		toolBar.setFloatable(false);

		return toolBar;
	}

	private void repaintNow() {

		JComponent contentPane = (JComponent) getContentPane();
		contentPane.paintImmediately(0, 0, contentPane.getWidth(), contentPane
				.getHeight());
		JMenuBar menuBar = getJMenuBar();
		menuBar.paintImmediately(0, 0, menuBar.getWidth(), menuBar.getHeight());
	}

	private void loadSettings() {

		Preferences preferences = Preferences.userNodeForPackage(getClass());

		workspaceChooserPath = preferences.get(SETTINGS_WORKSPACE_CHOOSER_PATH,
				workspaceChooserPath);
		classesChooserPath = preferences.get(SETTINGS_CLASSES_CHOOSER_PATH,
				classesChooserPath);
		recentMenu.read(preferences);
	}

	private void saveSettings() {

		Preferences preferences = Preferences.userNodeForPackage(getClass());
		preferences.put(SETTINGS_WORKSPACE_CHOOSER_PATH, workspaceChooserPath);
		preferences.put(SETTINGS_CLASSES_CHOOSER_PATH, classesChooserPath);
		recentMenu.save(preferences);
	}
/*
	private void doSaveWorkspace(boolean saveAs) {

		config.setMDIConfig(createMDIConfig());
		if (workspaceFile != null && !saveAs) {
			saveWorkspaceToFile(workspaceFile);
			return;
		}

		JFileChooser fileChooser = getWorkspaceFileChooser();
		int result = fileChooser.showSaveDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fileChooser.getSelectedFile();
			if (!selectedFile.getName().toLowerCase().endsWith(
					"." + BrowserApplication.WORKSPACE_FILE_SUFFIX)) {
				selectedFile = new File(selectedFile.getPath() + "."
						+ BrowserApplication.WORKSPACE_FILE_SUFFIX);
			}
			if (selectedFile.exists()
					&& GUIHelper.showOptionDialog(this, "The file "
							+ selectedFile.getPath()
							+ "\nexists. Do you want to overwrite this file?",
							GUIHelper.YES_NO_OPTIONS,
							JOptionPane.QUESTION_MESSAGE) != 0) {
				return;
			}
			saveWorkspaceToFile(selectedFile);
			workspaceFile = selectedFile;
			updateTitle();
			workspaceChooserPath = fileChooser.getCurrentDirectory()
					.getAbsolutePath();
		}
	}
*/
	private void saveWorkspaceToFile(File file) {

		try {
			FileOutputStream fos = new FileOutputStream(file);
			XMLEncoder encoder = new XMLEncoder(fos);
			encoder.writeObject(config);
			encoder.close();
			recentMenu.addRecentWorkspace(file);
		} catch (FileNotFoundException e) {
			GUIHelper.showMessage(this, "An error occured while saving to "
					+ file.getPath(), JOptionPane.ERROR_MESSAGE);
		}
		GUIHelper.showMessage(this, "Workspace saved to " + file.getPath(),
				JOptionPane.INFORMATION_MESSAGE);
		actionSaveWorkspaceAs.setEnabled(true);
	}

	private void doNewWorkspace() {

		closeAllFrames();
		workspaceFile = null;
		config = new BrowserConfig();
		config.addRuntimeLib();
		if (classpathBrowser != null) {
			classpathBrowser.setClasspathComponent(config);
		}
		updateTitle();
	}

	/**
	 * Open a workspace file.
	 * 
	 * @param file
	 *            the file.
	 */
	public void openWorkspace(File file) {

		repaintNow();
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		closeAllFrames();
		try {
			FileInputStream fos = new FileInputStream(file);
			XMLDecoder decoder = new XMLDecoder(fos);
			config = (BrowserConfig) decoder.readObject();
			readMDIConfig(config.getMDIConfig());
			decoder.close();
			recentMenu.addRecentWorkspace(file);
			if (classpathBrowser != null) {
				classpathBrowser.setClasspathComponent(config);
			}
		} catch (FileNotFoundException e) {
			GUIHelper.showMessage(this, "An error occured while reading "
					+ file.getPath(), JOptionPane.ERROR_MESSAGE);
		} finally {
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
		workspaceFile = file;
		updateTitle();
		actionSaveWorkspaceAs.setEnabled(true);
	}

	/*
	private void doOpenWorkspace() {

		JFileChooser fileChooser = getWorkspaceFileChooser();
		int result = fileChooser.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fileChooser.getSelectedFile();
			openWorkspace(selectedFile);
			workspaceChooserPath = fileChooser.getCurrentDirectory()
					.getAbsolutePath();
		}
	}
*/
	private void doOpenClassFile() {

		JFileChooser fileChooser = getClassesFileChooser();
		int result = fileChooser.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			repaintNow();
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			File file = fileChooser.getSelectedFile();
			classesChooserPath = fileChooser.getCurrentDirectory()
					.getAbsolutePath();

			BrowserInternalFrame frame;
			if (file.getPath().toLowerCase().endsWith(".class")) {
				frame = openClassFromFile(file);
			} else {
				frame = openClassFromJar(file);
			}

			if (frame != null) {
				try {
					frame.setMaximum(true);
				} catch (PropertyVetoException ex) {
				}
			}
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
	}

	private BrowserInternalFrame openClassFromJar(File file) {

		ClasspathArchiveEntry entry = new ClasspathArchiveEntry();
		entry.setFileName(file.getPath());
		if (jarBrowser == null) {
			jarBrowser = new ClasspathBrowser(this, null,
					"Classes in selected JAR file:", false);
		}
		jarBrowser.clear();
		jarBrowser.setClasspathComponent(entry);
		jarBrowser.setVisible(true);
		String selectedClassName = jarBrowser.getSelectedClassName();
		if (selectedClassName == null) {
			return null;
		}

		String fileName = file.getPath() + "!" + selectedClassName + ".class";

		BrowserInternalFrame frame = new BrowserInternalFrame(desktopManager,
				new WindowState(fileName));
		ClassFile classFile = frame.getClassFile();
		if (classFile != null) {
			config.addClasspathArchive(file.getPath());
		}

		return frame;
	}

	private void doBrowseClasspath() {

		if (classpathBrowser == null) {
			classpathBrowser = new ClasspathBrowser(this, config,
					"Configured classpath:", true);
		}
		classpathBrowser.setVisible(true);
		String selectedClassName = classpathBrowser.getSelectedClassName();
		if (selectedClassName == null) {
			return;
		}

		FindResult findResult = config.findClass(selectedClassName);
		if (findResult == null) {
			GUIHelper.showMessage(this, "Error loading " + selectedClassName,
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		repaintNow();
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		BrowserInternalFrame frame = new BrowserInternalFrame(desktopManager,
				new WindowState(findResult.getFileName()));
		try {
			frame.setMaximum(true);
		} catch (PropertyVetoException ex) {
		}
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

	}

	private void doSetupClasspath() {
		if (classpathSetupDialog == null) {
			classpathSetupDialog = new ClasspathSetupDialog(this);

		}
		classpathSetupDialog.setVisible(true);
	}

	private void doBackward() {
		BrowserInternalFrame frame = (BrowserInternalFrame) desktopPane
				.getSelectedFrame();
		if (frame != null) {
			frame.getBrowserComponent().getHistory().historyBackward();
		}
	}

	private void doForward() {
		BrowserInternalFrame frame = (BrowserInternalFrame) desktopPane
				.getSelectedFrame();
		if (frame != null) {
			frame.getBrowserComponent().getHistory().historyForward();
		}
	}

	/*
	 * Public, since it is called after modifying the calss file
	 */
	public void doReload() {
		BrowserInternalFrame frame = (BrowserInternalFrame) desktopPane
				.getSelectedFrame();
		if (frame != null) {
			TreePath selectionPath = frame.getBrowserComponent().getTreePane()
					.getTree().getSelectionPath();
			try {
				BrowserTreeNode categoryNode2 = (BrowserTreeNode) selectionPath
						.getPathComponent(2);
				String categoryName = categoryNode2.getUserObject().toString();
				frame.reload(categoryName);

			} catch (Exception e) {
				try {
					BrowserTreeNode categoryNode2 = (BrowserTreeNode) selectionPath
							.getPathComponent(1);
					String categoryName = categoryNode2.getUserObject()
							.toString();
					frame.reload(categoryName);
				} catch (Exception e2) {
					frame.reload("");
				}
			}

		}
	}

	private void doVerify() {
		BrowserInternalFrame frame = (BrowserInternalFrame) desktopPane
				.getSelectedFrame();

		String fileName = frame.getFileName();
		VerifierDisplay vd = new VerifierDisplay(this, fileName);

		vd.setVisible(true);
	}

	/*
	private JFileChooser getWorkspaceFileChooser() {

		if (workspaceFileChooser == null) {
			workspaceFileChooser = new JFileChooser(workspaceChooserPath);
			workspaceFileChooser.setDialogTitle("Choose workspace file");
			workspaceFileChooser.setFileFilter(new BasicFileFilter(
					BrowserApplication.WORKSPACE_FILE_SUFFIX,
					"jclasslib workspace files"));
		}

		return workspaceFileChooser;
	}*/

	private JFileChooser getClassesFileChooser() {

		if (classesFileChooser == null) {
			classesFileChooser = new JFileChooser(classesChooserPath);
			classesFileChooser.setDialogTitle("Choose class file or jar file");
			classesFileChooser.addChoosableFileFilter(new BasicFileFilter(
					"class", "class files"));
			classesFileChooser.addChoosableFileFilter(new BasicFileFilter(
					"jar", "jar files"));
			classesFileChooser.setFileFilter(new BasicFileFilter(new String[] {
					"class", "jar" }, "class files and jar files"));
		}

		return classesFileChooser;
	}

	private void doShowURL(String urlSpec) {

		String commandLine;
		if (System.getProperty("os.name").startsWith("Windows")) {
			commandLine = "rundll32.exe url.dll,FileProtocolHandler " + urlSpec;
		} else {
			commandLine = "netscape " + urlSpec;
		}
		try {
			Runtime.getRuntime().exec(commandLine);
		} catch (IOException ex) {
		}
	}
	
	void removeTempFiles() {
		for (String fileName: tempFiles) {
			new File(fileName).delete();
		}
		
	}

	private void doAbout() {
		new BrowserAboutDialog(this).setVisible(true);
	}

	private class DefaultAction extends AbstractAction {

		private DefaultAction(String name, Icon icon) {
			super(name, icon);
		}

		public void actionPerformed(ActionEvent ev) {

			if (this == actionOpenClassFile) {
				doOpenClassFile();
			} else if (this == actionBrowseClasspath) {
				doBrowseClasspath();
			} else if (this == actionSetupClasspath) {
				doSetupClasspath();
			} else if (this == actionNewWorkspace) {
				doNewWorkspace();
			} else if (this == actionQuit) {

				doQuit();
			} else if (this == actionBackward) {
				doBackward();
			} else if (this == actionForward) {
				doForward();
			} else if (this == actionReload) {
				doReload();
			} else if (this == actionShowHomepage) {
				doShowURL("http://www.cs.ioc.ee/~ando/jbe/");
			} else if (this == actionShowHelp) {
				try {
					doShowURL(new File("doc/help.html").getCanonicalFile()
							.toURL().toExternalForm());
				} catch (IOException e) {
				}
			} else if (this == actionAbout) {
				doAbout();
			} else if (this == actionVerifyClass) {
				doVerify();
			}else if (this == actionRevertClass) {
				BrowserInternalFrame frame = (BrowserInternalFrame) desktopPane
				.getSelectedFrame();
				try {
					FileUtils.copy(new File(frame.getBackupFile()), new File(frame.getFileName()));
					doReload();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
		}

	}

	public class UndoAction extends AbstractAction {
		UndoAction(String name, Icon icon) {
			super(name, icon);
		}

		public void actionPerformed(ActionEvent arg0) {
			BrowserInternalFrame frame = (BrowserInternalFrame) desktopPane
					.getSelectedFrame();
			CodeEditPane cep = frame.getBrowserComponent().getDetailPane()
					.getAttributeDetailPane().getCodeAttributeDetailPane()
					.getCodeEditPane();
			TreePath treePath = frame.getBrowserComponent().getTreePane()
					.getTree().getSelectionPath();
			String methodIndex = Integer.toString(((BrowserTreeNode) treePath
					.getParentPath().getLastPathComponent()).getIndex());
			((CodeEditArea) cep.getEditPanes().get(methodIndex)).doUndo();

		}

	}

	public class RedoAction extends AbstractAction {
		RedoAction(String name, Icon icon) {
			super(name, icon);
		}

		public void actionPerformed(ActionEvent arg0) {
			BrowserInternalFrame frame = (BrowserInternalFrame) desktopPane
					.getSelectedFrame();
			CodeEditPane cep = frame.getBrowserComponent().getDetailPane()
					.getAttributeDetailPane().getCodeAttributeDetailPane()
					.getCodeEditPane();
			TreePath treePath = frame.getBrowserComponent().getTreePane()
					.getTree().getSelectionPath();
			String methodIndex = Integer.toString(((BrowserTreeNode) treePath
					.getParentPath().getLastPathComponent()).getIndex());
			((CodeEditArea) cep.getEditPanes().get(methodIndex)).doRedo();

		}

	}

	public Action getActionVerify() {
		return actionVerifyClass;
	}

	public Action getActionRevert() {
		return actionRevertClass;
	}

	public void addTempFile(String backupFile) {
		tempFiles.add(backupFile);
		
	}


}
