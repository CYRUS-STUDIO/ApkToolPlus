/*
    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public
    License as published by the Free Software Foundation; either
    version 2 of the license, or (at your option) any later version.
*/

package ee.ioc.cs.jbe.browser.config.classpath;

import javax.swing.*;
import java.awt.*;

/**
    Cell renderer for the list in the <tt>ClasspathSetupDialog</tt>.

    @author <a href="mailto:jclasslib@ej-technologies.com">Ingo Kegel</a>
    @version $Revision: 1.2 $ $Date: 2006/09/25 16:00:58 $
*/
public class ClasspathCellRenderer extends DefaultListCellRenderer {

    public Component getListCellRendererComponent(
            JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus)
    {
        ClasspathEntry entry = (ClasspathEntry)value;
        super.getListCellRendererComponent(list, entry.getFileName(), index, isSelected, cellHasFocus);

        Icon icon;
        if (entry instanceof ClasspathDirectoryEntry) {
            icon = UIManager.getIcon("FileView.directoryIcon");
        } else {
            icon = UIManager.getIcon("FileView.fileIcon");
        }
        setIcon(icon);

        return this;
    }
}
