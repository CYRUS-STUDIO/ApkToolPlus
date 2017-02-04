package org.apache.bcel.verifier;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache BCEL" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache BCEL", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import org.apache.bcel.*;
import org.apache.bcel.classfile.*;

/**
 * This class implements a machine-generated frame for use with
 * the GraphicalVerfifier.
 *
 * @version $Id: VerifierAppFrame.java,v 1.2 2006/09/04 15:43:18 andos Exp $
 * @author Enver Haase
 * @see GraphicalVerifier
 */
public class VerifierAppFrame extends JFrame {
  JPanel contentPane;
  JSplitPane jSplitPane1 = new JSplitPane();
  JPanel jPanel1 = new JPanel();
  JPanel jPanel2 = new JPanel();
  JSplitPane jSplitPane2 = new JSplitPane();
  JPanel jPanel3 = new JPanel();
  JList classNamesJList = new JList();
  GridLayout gridLayout1 = new GridLayout();
  JPanel messagesPanel = new JPanel();
  GridLayout gridLayout2 = new GridLayout();
  JMenuBar jMenuBar1 = new JMenuBar();
  JMenu jMenu1 = new JMenu();
  JScrollPane jScrollPane1 = new JScrollPane();
  JScrollPane messagesScrollPane = new JScrollPane();
  JScrollPane jScrollPane3 = new JScrollPane();
  GridLayout gridLayout4 = new GridLayout();
  JScrollPane jScrollPane4 = new JScrollPane();
  CardLayout cardLayout1 = new CardLayout();

  private String JUSTICE_VERSION = "JustIce by Enver Haase";
  private String current_class;
  GridLayout gridLayout3 = new GridLayout();
  JTextPane pass1TextPane = new JTextPane();
  JTextPane pass2TextPane = new JTextPane();
  JTextPane messagesTextPane = new JTextPane();
  JMenuItem newFileMenuItem = new JMenuItem();
  JSplitPane jSplitPane3 = new JSplitPane();
  JSplitPane jSplitPane4 = new JSplitPane();
  JScrollPane jScrollPane2 = new JScrollPane();
  JScrollPane jScrollPane5 = new JScrollPane();
  JScrollPane jScrollPane6 = new JScrollPane();
  JScrollPane jScrollPane7 = new JScrollPane();
  JList pass3aJList = new JList();
  JList pass3bJList = new JList();
  JTextPane pass3aTextPane = new JTextPane();
  JTextPane pass3bTextPane = new JTextPane();
  JMenu jMenu2 = new JMenu();
  JMenuItem whatisMenuItem = new JMenuItem();
  JMenuItem aboutMenuItem = new JMenuItem();

  /** Constructor. */
  public VerifierAppFrame() {
    enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }
  /** Initizalization of the components. */
  private void jbInit() throws Exception  {
    //setIconImage(Toolkit.getDefaultToolkit().createImage(Frame1.class.getResource("[Ihr Symbol]")));
    contentPane = (JPanel) this.getContentPane();
    contentPane.setLayout(cardLayout1);
    this.setJMenuBar(jMenuBar1);
    this.setSize(new Dimension(708, 451));
    this.setTitle("JustIce");
    jPanel1.setMinimumSize(new Dimension(100, 100));
    jPanel1.setPreferredSize(new Dimension(100, 100));
    jPanel1.setLayout(gridLayout1);
    jSplitPane2.setOrientation(JSplitPane.VERTICAL_SPLIT);
    jPanel2.setLayout(gridLayout2);
    jPanel3.setMinimumSize(new Dimension(200, 100));
    jPanel3.setPreferredSize(new Dimension(400, 400));
    jPanel3.setLayout(gridLayout4);
    messagesPanel.setMinimumSize(new Dimension(100, 100));
    messagesPanel.setLayout(gridLayout3);
    jPanel2.setMinimumSize(new Dimension(200, 100));
    jMenu1.setText("File");

    jScrollPane1.getViewport().setBackground(Color.red);
    messagesScrollPane.getViewport().setBackground(Color.red);
    messagesScrollPane.setPreferredSize(new Dimension(10, 10));
    classNamesJList.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        classNamesJList_valueChanged(e);
      }
    });
    classNamesJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    jScrollPane3.setBorder(BorderFactory.createLineBorder(Color.black));
    jScrollPane3.setPreferredSize(new Dimension(100, 100));
    gridLayout4.setRows(4);
    gridLayout4.setColumns(1);
    gridLayout4.setHgap(1);
    jScrollPane4.setBorder(BorderFactory.createLineBorder(Color.black));
    jScrollPane4.setPreferredSize(new Dimension(100, 100));
    pass1TextPane.setBorder(BorderFactory.createRaisedBevelBorder());
    pass1TextPane.setToolTipText("");
    pass1TextPane.setEditable(false);
    pass2TextPane.setBorder(BorderFactory.createRaisedBevelBorder());
    pass2TextPane.setEditable(false);
    messagesTextPane.setBorder(BorderFactory.createRaisedBevelBorder());
    messagesTextPane.setEditable(false);
    newFileMenuItem.setText("New...");
    newFileMenuItem.setAccelerator(KeyStroke.getKeyStroke(78, KeyEvent.CTRL_MASK, true));
    newFileMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        newFileMenuItem_actionPerformed(e);
      }
    });
    pass3aTextPane.setEditable(false);
    pass3bTextPane.setEditable(false);
    pass3aJList.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        pass3aJList_valueChanged(e);
      }
    });
    pass3bJList.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        pass3bJList_valueChanged(e);
      }
    });
    jMenu2.setText("Help");
    whatisMenuItem.setText("What is...");
    whatisMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        whatisMenuItem_actionPerformed(e);
      }
    });
    aboutMenuItem.setText("About");
    aboutMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        aboutMenuItem_actionPerformed(e);
      }
    });
    jSplitPane2.add(messagesPanel, JSplitPane.BOTTOM);
    messagesPanel.add(messagesScrollPane, null);
    messagesScrollPane.getViewport().add(messagesTextPane, null);
    jSplitPane2.add(jPanel3, JSplitPane.TOP);
    jPanel3.add(jScrollPane3, null);
    jScrollPane3.getViewport().add(pass1TextPane, null);
    jPanel3.add(jScrollPane4, null);
    jPanel3.add(jSplitPane3, null);
    jSplitPane3.add(jScrollPane2, JSplitPane.LEFT);
    jScrollPane2.getViewport().add(pass3aJList, null);
    jSplitPane3.add(jScrollPane5, JSplitPane.RIGHT);
    jScrollPane5.getViewport().add(pass3aTextPane, null);
    jPanel3.add(jSplitPane4, null);
    jSplitPane4.add(jScrollPane6, JSplitPane.LEFT);
    jScrollPane6.getViewport().add(pass3bJList, null);
    jSplitPane4.add(jScrollPane7, JSplitPane.RIGHT);
    jScrollPane7.getViewport().add(pass3bTextPane, null);
    jScrollPane4.getViewport().add(pass2TextPane, null);
    jSplitPane1.add(jPanel2, JSplitPane.TOP);
    jPanel2.add(jScrollPane1, null);
    jSplitPane1.add(jPanel1, JSplitPane.BOTTOM);
    jPanel1.add(jSplitPane2, null);
    jScrollPane1.getViewport().add(classNamesJList, null);
    jMenuBar1.add(jMenu1);
    jMenuBar1.add(jMenu2);
    contentPane.add(jSplitPane1, "jSplitPane1");
    jMenu1.add(newFileMenuItem);
    jMenu2.add(whatisMenuItem);
    jMenu2.add(aboutMenuItem);
    jSplitPane2.setDividerLocation(300);
    jSplitPane3.setDividerLocation(150);
    jSplitPane4.setDividerLocation(150);
  }

  /** Overridden to stop the application on a closing window. */
  protected void processWindowEvent(WindowEvent e) {
    super.processWindowEvent(e);
    if (e.getID() == WindowEvent.WINDOW_CLOSING) {
      System.exit(0);
    }
  }

  synchronized void classNamesJList_valueChanged(ListSelectionEvent e) {
    if (e.getValueIsAdjusting()) return;
    current_class = classNamesJList.getSelectedValue().toString();
    verify();
    classNamesJList.setSelectedValue(current_class, true);
  }

  private void verify(){
    setTitle("PLEASE WAIT");

    Verifier v = VerifierFactory.getVerifier(current_class);
    v.flush(); // Don't cache the verification result for this class.

    VerificationResult vr;

    vr =  v.doPass1();
    if (vr.getStatus() == VerificationResult.VERIFIED_REJECTED){
      pass1TextPane.setText(vr.getMessage());
      pass1TextPane.setBackground(Color.red);

      pass2TextPane.setText("");
      pass2TextPane.setBackground(Color.yellow);
      pass3aTextPane.setText("");
      pass3aJList.setListData(new Object[0]);
      pass3aTextPane.setBackground(Color.yellow);

      pass3bTextPane.setText("");
      pass3bJList.setListData(new Object[0]);
      pass3bTextPane.setBackground(Color.yellow);

    }
    else{ // Must be VERIFIED_OK, Pass 1 does not know VERIFIED_NOTYET
      pass1TextPane.setBackground(Color.green);
      pass1TextPane.setText(vr.getMessage());

      vr = v.doPass2();
      if (vr.getStatus() == VerificationResult.VERIFIED_REJECTED){
        pass2TextPane.setText(vr.getMessage());
        pass2TextPane.setBackground(Color.red);

        pass3aTextPane.setText("");
        pass3aTextPane.setBackground(Color.yellow);
        pass3aJList.setListData(new Object[0]);
        pass3bTextPane.setText("");
        pass3bTextPane.setBackground(Color.yellow);
        pass3bJList.setListData(new Object[0]);
      }
      else{ // must be Verified_OK, because Pass1 was OK (cannot be Verified_NOTYET).
          pass2TextPane.setText(vr.getMessage());
          pass2TextPane.setBackground(Color.green);

          JavaClass jc = Repository.lookupClass(current_class);
          /*
          boolean all3aok = true;
          boolean all3bok = true;
          String all3amsg = "";
          String all3bmsg = "";
          */

          String[] methodnames = new String[jc.getMethods().length];
          for (int i=0; i<jc.getMethods().length; i++){
            methodnames[i] = jc.getMethods()[i].toString().replace('\n',' ').replace('\t',' ');
          }
          pass3aJList.setListData(methodnames);
          pass3aJList.setSelectionInterval(0,jc.getMethods().length-1);
          pass3bJList.setListData(methodnames);
          pass3bJList.setSelectionInterval(0,jc.getMethods().length-1);
      }

    }
    String[] msgs = v.getMessages();
    messagesTextPane.setBackground(msgs.length == 0? Color.green : Color.yellow);
    String allmsgs = "";
    for (int i=0; i<msgs.length; i++){
      msgs[i] = msgs[i].replace('\n',' ');
      allmsgs += msgs[i] + "\n\n";
    }
    messagesTextPane.setText(allmsgs);

    setTitle(current_class + " - " + JUSTICE_VERSION);
  }

  void newFileMenuItem_actionPerformed(ActionEvent e) {
    String classname = JOptionPane.showInputDialog("Please enter the fully qualified name of a class or interface to verify:");
    if ((classname == null) || (classname.equals(""))) return;
    VerifierFactory.getVerifier(classname); // let observers do the rest.
    classNamesJList.setSelectedValue(classname, true);
  }

  synchronized void pass3aJList_valueChanged(ListSelectionEvent e) {

    if (e.getValueIsAdjusting()) return;
    Verifier v = VerifierFactory.getVerifier(current_class);

    String all3amsg = "";
    boolean all3aok = true;
    boolean rejected = false;
    for (int i=0; i<pass3aJList.getModel().getSize(); i++){

      if (pass3aJList.isSelectedIndex(i)){
        VerificationResult vr = v.doPass3a(i);

        if (vr.getStatus() == VerificationResult.VERIFIED_REJECTED){
          all3aok = false;
          rejected = true;
        }
        all3amsg += "Method '"+Repository.lookupClass(v.getClassName()).getMethods()[i]+"': "+vr.getMessage().replace('\n',' ')+"\n\n";
      }
    }
    pass3aTextPane.setText(all3amsg);
    pass3aTextPane.setBackground(all3aok? Color.green : (rejected? Color.red : Color.yellow));

  }

  synchronized void pass3bJList_valueChanged(ListSelectionEvent e) {
    if (e.getValueIsAdjusting()) return;

    Verifier v = VerifierFactory.getVerifier(current_class);

    String all3bmsg = "";
    boolean all3bok = true;
    boolean rejected = false;
    for (int i=0; i<pass3bJList.getModel().getSize(); i++){

      if (pass3bJList.isSelectedIndex(i)){
        VerificationResult vr = v.doPass3b(i);

        if (vr.getStatus() == VerificationResult.VERIFIED_REJECTED){
          all3bok = false;
          rejected = true;
        }
        all3bmsg += "Method '"+Repository.lookupClass(v.getClassName()).getMethods()[i]+"': "+vr.getMessage().replace('\n',' ')+"\n\n";
      }
    }
    pass3bTextPane.setText(all3bmsg);
    pass3bTextPane.setBackground(all3bok? Color.green : (rejected? Color.red : Color.yellow));

  }

  void aboutMenuItem_actionPerformed(ActionEvent e) {
    JOptionPane.showMessageDialog(this,
            "JustIce is a Java class file verifier.\nIt was implemented by Enver Haase in 2001, 2002.\n<http://jakarta.apache.org/bcel/index.html>",
             JUSTICE_VERSION, JOptionPane.INFORMATION_MESSAGE);
  }

  void whatisMenuItem_actionPerformed(ActionEvent e) {
    JOptionPane.showMessageDialog(this,
            "The upper four boxes to the right reflect verification passes according to The Java Virtual Machine Specification.\nThese are (in that order): Pass one, Pass two, Pass three (before data flow analysis), Pass three (data flow analysis).\nThe bottom box to the right shows (warning) messages; warnings do not cause a class to be rejected.",
             JUSTICE_VERSION, JOptionPane.INFORMATION_MESSAGE);
  }
}
