/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller.fields;

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Control;

/**
 * 
 * @author Cornelius.Dirmeier
 */
public class ComboViewerField extends DefaultEditField {

    private final ComboViewer comboViewer;
    
    /**
     * 
     */
    public ComboViewerField(ComboViewer comboViewer) {
        this.comboViewer = comboViewer;
    }
    
    /**
     * {@inheritDoc}
     */
    public Control getControl() {
        return comboViewer.getControl();
    }

    /**
     * {@inheritDoc}
     */
    public String getText() {
        return comboViewer.getCombo().getText();
    }

    /**
     * {@inheritDoc}
     */
    public Object parseContent() {
        Object o = ((IStructuredSelection)comboViewer.getSelection()).getFirstElement();
        return prepareObjectForGet(o);
    }
    
    /**
     * {@inheritDoc}
     */
    public void setValue(Object newValue) {
        ISelection selection = new StructuredSelection(prepareObjectForSet(newValue));
        comboViewer.setSelection(selection, true);
    }

    /**
     * {@inheritDoc}
     */
    public void insertText(String text) {
        comboViewer.getCombo().setText(text);
    }

    /**
     * {@inheritDoc}
     */
    public void selectAll() {
        comboViewer.getCombo().select(0);
    }

    /**
     * {@inheritDoc}
     */
    public void setText(String newText) {
        comboViewer.getCombo().setText(newText);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addListenerToControl() {
        comboViewer.addSelectionChangedListener(new ISelectionChangedListener(){

            public void selectionChanged(SelectionChangedEvent event) {
                notifyChangeListeners(new FieldValueChangedEvent(ComboViewerField.this));
            }
            
        });
    }

}
