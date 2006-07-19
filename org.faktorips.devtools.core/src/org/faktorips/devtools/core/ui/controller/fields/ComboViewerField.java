/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller.fields;

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Control;

/**
 * Field to map a ComboViewer
 */
public class ComboViewerField extends DefaultEditField {

    private ComboViewer viewer;
    
    public ComboViewerField(ComboViewer viewer) {
        this.viewer = viewer;
    }
    
    /**
     * {@inheritDoc}
     */ 
    public Control getControl() {
        return viewer.getControl();
    }

    /**
     * {@inheritDoc}
     */ 
    public Object getValue() {
        return viewer.getElementAt(viewer.getCombo().getSelectionIndex());
    }

    /**
     * {@inheritDoc}
     */ 
    public void setValue(Object newValue) {
        viewer.setSelection(new StructuredSelection(newValue));
    }

    /**
     * {@inheritDoc}
     */ 
    public String getText() {
        int i = viewer.getCombo().getSelectionIndex();
        if (i==-1) {
            return null;
        }
        return viewer.getElementAt(i).toString();
    }

    /**
     * {@inheritDoc}
     */ 
    public void setText(String newText) {
        viewer.getCombo().setText(newText);
    }

    /**
     * {@inheritDoc}
     */ 
    public void insertText(String text) {
        setText(text);
    }

    /**
     * {@inheritDoc}
     */ 
    public void selectAll() {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */ 
    protected void addListenerToControl() {
        viewer.getCombo().addSelectionListener(new SelectionListener() {

            public void widgetSelected(SelectionEvent e) {
                notifyChangeListeners(new FieldValueChangedEvent(ComboViewerField.this));
            }

            public void widgetDefaultSelected(SelectionEvent e) {
                // nothing to do
            }
        });

    }

}
