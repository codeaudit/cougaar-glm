/*
 * <copyright>
 * Copyright 1997-2001 Defense Advanced Research Projects
 * Agency (DARPA) and ALPINE (a BBN Technologies (BBN) and
 * Raytheon Systems Company (RSC) Consortium).
 * This software to be used only in accordance with the
 * COUGAAR licence agreement.
 * </copyright>
 */
 
package org.cougaar.domain.mlm.ui.planviewer;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.tree.*;

abstract public class AbstractCellEditor 
                                implements TableCellEditor {
        protected EventListenerList listenerList = 
                                                                                new EventListenerList();
        protected Object value;
        protected ChangeEvent changeEvent = null;
        protected int clickCountToStart = 1;

        public Object getCellEditorValue() {
                return value;
        }
        public void setCellEditorValue(Object value) {
                this.value = value;
        }
        public void setClickCountToStart(int count) {
                clickCountToStart = count;
        }
        public int getClickCountToStart() {
                return clickCountToStart;
        }
        public boolean isCellEditable(EventObject anEvent) {
                if (anEvent instanceof MouseEvent) {
                        if (((MouseEvent)anEvent).getClickCount() < 
                                                                                                clickCountToStart)
                                return false;
                }
                return true;
        }
        public boolean shouldSelectCell(EventObject anEvent) {
                return true;
        }
        public boolean stopCellEditing() {
                fireEditingStopped();
                return true;
        }
        public void cancelCellEditing() {
                fireEditingCanceled();
        }
        public void addCellEditorListener(CellEditorListener l) {
                listenerList.add(CellEditorListener.class, l);
        }
        public void removeCellEditorListener(CellEditorListener l) {
                listenerList.remove(CellEditorListener.class, l);
        }
        protected void fireEditingStopped() {
                Object[] listeners = listenerList.getListenerList();
                for (int i = listeners.length-2; i>=0; i-=2) {
                        if (listeners[i] == CellEditorListener.class) {
                                if (changeEvent == null)
                                        changeEvent = new ChangeEvent(this);
                                ((CellEditorListener)
                                listeners[i+1]).editingStopped(changeEvent);
                        }              
                }
        }
        protected void fireEditingCanceled() {
                Object[] listeners = listenerList.getListenerList();
                for (int i = listeners.length-2; i>=0; i-=2) {
                        if (listeners[i]==CellEditorListener.class) {
                                if (changeEvent == null)
                                        changeEvent = new ChangeEvent(this);
                                ((CellEditorListener)
                                listeners[i+1]).editingCanceled(changeEvent);
                        }              
                }
        }
}
