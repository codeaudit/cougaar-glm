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
 
package org.cougaar.domain.mlm.ui.views.policy;

import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

/**
 * .<pre>
 * Glass pane which swallows all mouse events.  
 * Taken from the GlassPaneDemo in the swing tutorial
 *
 * Disable input - 
 *    getRootPane().setGlassPane(new DisablingGlassPane(this))
 *    getRootPane().getGlassPane().setVisible(true)
 *
 * Reenable input -
 *    getRootPane().getGlassPane().setVisible(false)
 * </pre>
 */

public class DisablingGlassPane extends JComponent {
  
  /**
   * Constructor - 
   */
  public DisablingGlassPane(Container contentPane) {
    GlassPaneListener listener = new GlassPaneListener(this, contentPane);
    addMouseListener(listener);
    addMouseMotionListener(listener);
  }

  /** 
   * Listen for all events mouse events and swallow them.
   */
  private class GlassPaneListener extends MouseInputAdapter {
    DisablingGlassPane myGlassPane;
    Container myContentPane;
    
    public GlassPaneListener(DisablingGlassPane glassPane, 
                             Container contentPane) {
      myGlassPane = glassPane;
      myContentPane = contentPane;
    }
    
    public void mouseMoved(MouseEvent e) {
      redispatchMouseEvent(e, false);
    }
    
    public void mouseDragged(MouseEvent e) {
      redispatchMouseEvent(e, false);
    }
    
    public void mouseClicked(MouseEvent e) {
      redispatchMouseEvent(e, false);
    }
    
    public void mouseEntered(MouseEvent e) {
      redispatchMouseEvent(e, false);
    }
    
    public void mouseExited(MouseEvent e) {
      redispatchMouseEvent(e, false);
    }
    
    public void mousePressed(MouseEvent e) {
      redispatchMouseEvent(e, false);
    }
    
    public void mouseReleased(MouseEvent e) {
      redispatchMouseEvent(e, true);
    }
    
    private void redispatchMouseEvent(MouseEvent e,
                                      boolean repaint) {
      if (repaint) {
        Point glassPanePoint = e.getPoint();
        Component component = null;
        Container container = myContentPane;
        Point containerPoint = SwingUtilities.convertPoint(myGlassPane,
                                                           glassPanePoint, 
                                                           myContentPane);
        
        //XXX: If the event is from a component in a popped-up menu,
        //XXX: then the container should probably be the menu's
        //XXX: JPopupMenu, and containerPoint should be adjusted
        //XXX: accordingly.
        component = SwingUtilities.getDeepestComponentAt(container,
                                                         containerPoint.x,
                                                         containerPoint.y);
        
        if (component == null) {
          return;
        } else {
          myGlassPane.repaint();
        }
      }
    }
  }
}











