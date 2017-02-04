/*
    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public
    License as published by the Free Software Foundation; either
    version 2 of the license, or (at your option) any later version.
*/

package ee.ioc.cs.jbe.browser;

import org.gjt.jclasslib.util.GUIHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * About dialog.
 *
 * @author <a href="mailto:jclasslib@ej-technologies.com">Ingo Kegel</a>
 * @version $Revision: 1.1 $ $Date: 2006/09/25 16:00:58 $
 */
public class BrowserAboutDialog extends JDialog {

    private JButton btnOk;

    /**
     * Constructor.
     *
     * @param parent parent frame.
     */
    public BrowserAboutDialog(JFrame parent) {
        super(parent);
        setupControls();
        setupComponent();
    }

    private void setupComponent() {

        setModal(true);
        setTitle("About Java Bytecode Editor");
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(400, 450);
        GUIHelper.centerOnParentWindow(this, getOwner());

        JComponent contentPane = (JComponent)getContentPane();
        contentPane.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(10, 5, 0, 5);
        gc.gridx = 0;
        gc.gridy = GridBagConstraints.RELATIVE;

        gc.anchor = GridBagConstraints.CENTER;
        gc.weightx = 1;

        JLabel nameLabel = new JLabel("Java Bytecode Editor");
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 14));
        contentPane.add(nameLabel, gc);
        contentPane.add(new JLabel("by Ando Saabas"), gc);
        contentPane.add(new JLabel(""), gc);
        contentPane.add(new JLabel("based on"), gc);
        JLabel label = new JLabel("jclasslib bytecode viewer");
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        contentPane.add(label, gc);
        gc.insets.top = 5;
        contentPane.add(new JLabel("Version 3.0"), gc);
        contentPane.add(new JLabel("Copyright ej-technologies GmbH, 2001-2005"), gc);
        contentPane.add(new JLabel("Licensed under the General Public License"), gc);
        contentPane.add(new JLabel(""), gc);
        contentPane.add(new JLabel("This product includes software developed by the"), gc);
        contentPane.add(new JLabel("Apache Software Foundation (http://www.apache.org/)"), gc);

        
        gc.weighty = 0;
        gc.insets.top = 20;
        gc.insets.bottom = 5;
        gc.fill = GridBagConstraints.NONE;
        contentPane.add(btnOk, gc);

        Dimension size = contentPane.getPreferredSize();
        size.width += 100;
        contentPane.setPreferredSize(size);
        pack();
        setResizable(false);

    }

    private void setupControls() {

        btnOk = new JButton("Ok");
        btnOk.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                setVisible(false);
                dispose();
            }
        });
    }
}
