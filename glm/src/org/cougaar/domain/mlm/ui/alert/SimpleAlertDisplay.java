/*
 * <copyright>
 *  Copyright 1997-2001 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA).
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Cougaar Open Source License as published by
 *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 *  PERFORMANCE OF THE COUGAAR SOFTWARE.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.alert;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;

/**
 **/

public class SimpleAlertDisplay extends JPanel {
    protected static final int DEFAULT_TEXT_WIDTH = 30;

    String myUID;
    JLabel titleLabel = new JLabel();
    JTextArea textArea = new JTextArea();
    JPanel buttons = new JPanel();
    private Action disabledAction = new AbstractAction("Acknowledge") {
        public void actionPerformed(ActionEvent e) {
        }
    };

    /**
     * Constructor - 
     *
     * @param uid String - alert's unique identifier.
     * @param text String - explanation for the alert
     */
    public SimpleAlertDisplay() {
        super(new BorderLayout());
        Font titleFont = titleLabel.getFont();
        titleLabel.setFont(titleFont.deriveFont(1.5f * titleFont.getSize()));
        titleLabel.setForeground(Color.black);
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        setMinimumSize(new Dimension(100, 100));
        setPreferredSize(new Dimension(400, 400));
        disabledAction.setEnabled(false);
        clear();
        add(titleLabel, BorderLayout.NORTH);
        add(makeTextPanel(), BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
    }

    public void init(String uid, String title, String text, Action[] choices) {
        if (choices == null || choices.length == 0) {
            throw new IllegalArgumentException("Must have at least one choice");
        }
        myUID = uid;
        titleLabel.setText(title);
        textArea.setText(text);
        setButtons(choices);
    }

    public void clear() {
        myUID = null;
        titleLabel.setText("No Alert Selected");
        textArea.setText("");
        setButtons(new Action[] {disabledAction});
    }

    protected JPanel makeTitlePanel(String text) {
        JPanel result = new JPanel();
        result.add(new JLabel(text));
        return result;
    }

    private static Map monospacedAttributeMap = new HashMap();
    static {
        monospacedAttributeMap.put(TextAttribute.FAMILY, "monospaced");
        monospacedAttributeMap.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
    }

    /**
     * makeTextPanel - make a JPanel with alert text
     *
     * @return JPanel
     */
    protected JComponent makeTextPanel() {
        textArea.setColumns(DEFAULT_TEXT_WIDTH);
        textArea.setFont(UIManager.getFont("TextArea.font").deriveFont(monospacedAttributeMap));
        System.out.println(textArea.getFont());
        //      textArea.setForeground(UIManager.getColor("Label.foreground"));
        //      textArea.setBackground(UIManager.getColor("Label.background"));
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        
        JScrollPane textScrollPane = new JScrollPane(textArea);
        
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BorderLayout());
        textPanel.add(BorderLayout.CENTER, textScrollPane);
        
        return textPanel;
    }

    /**
     * getUID - return the uid for the associated alert
     * 
     * @return String - alert's uid
     **/
    protected String getUID() {
        return myUID;
    }

    protected void setButtons(Action[] choices) {
        buttons.removeAll();
        for (int i = 0; i < choices.length; i++) {
            Action a = choices[i];
            JButton b = new JButton((String) a.getValue(Action.NAME),
                                    (Icon) a.getValue(Action.SMALL_ICON));
            b.setHorizontalTextPosition(JButton.CENTER);
            b.setVerticalTextPosition(JButton.BOTTOM);
            b.setEnabled(a.isEnabled());
            b.addActionListener(a);
            buttons.add(b);
        }
        buttons.revalidate();
    }
}
