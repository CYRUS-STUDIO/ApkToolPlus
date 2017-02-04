/*
 *
 */
package ee.ioc.cs.jbe.browser.detail.attributes.code;

import java.awt.Dimension;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class ErrorReportWindow extends JDialog{
	
	public ErrorReportWindow(JFrame frame, String text, String label) {
		super(frame);
		//setModal(true);
        setTitle(label);
        JTextArea textArea = new JTextArea();
        textArea.setText(text);
        textArea.setLineWrap(true);
        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setPreferredSize(new Dimension(600, 300));
        this.add(scroll);
       
    }

}
