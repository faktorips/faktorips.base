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

package org.faktorips.devtools.core.ui.table;

import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ICellEditorListener;

/**
 * {@link TraversalStrategy} that can be added to different {@link IpsCellEditor}s during its
 * lifetime (the {@link TableTraversalStrategy} in contrast is instantiated for every
 * {@link IpsCellEditor} separately). The {@link AbstractPermanentTraversalStrategy} is designed to
 * be used with a {@link EditingSupport} object. The {@link EditingSupport} should hold an instance
 * of the {@link AbstractPermanentTraversalStrategy} and register each {@link IpsCellEditor} with it
 * after it is created.
 * 
 * @author Stefan Widmaier
 */
public abstract class AbstractPermanentTraversalStrategy implements TraversalStrategy {

    private IpsCellEditor cellEditor;
    private Object viewItem;

    private EditingSupport editingSupport;

    public AbstractPermanentTraversalStrategy(EditingSupport editingSupport) {
        super();
        this.editingSupport = editingSupport;
    }

    /**
     * Method to be called by an {@link EditingSupport} holding an instance of this class after an
     * {@link IpsCellEditor} is created.
     * 
     * @param cEditor the newly created cell editor
     * @param viewItem the viewItem the cell editor will modify
     */
    public void setCurrentCellEditor(IpsCellEditor cEditor, Object viewItem) {
        if (getCurrentCellEditor() != null) {
            resetCellEditor();
        }
        cellEditor = cEditor;
        this.viewItem = viewItem;
        cellEditor.setTraversalStrategy(this);
        cellEditor.addListener(new CellEditorListener());
    }

    protected Object getCurrentViewItem() {
        return viewItem;
    }

    protected IpsCellEditor getCurrentCellEditor() {
        return cellEditor;
    }

    private void resetCellEditor() {
        cellEditor = null;
        viewItem = null;
    }

    private class CellEditorListener implements ICellEditorListener {

        public void editorValueChanged(boolean oldValidState, boolean newValidState) {
            // nothing to do
        }

        public void cancelEditor() {
            resetCellEditor();
        }

        public void applyEditorValue() {
            resetCellEditor();
        }
    }

    protected void fireApplyEditorValue() {
        if (getCurrentCellEditor() != null) {
            getCurrentCellEditor().fireApplyEditorValue();
        }
    }

    protected EditingSupport getEditingSupport() {
        return editingSupport;
    }

}
