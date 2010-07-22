/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ICellEditorListener;

/**
 * {@link EditingSupport} that keeps track of the currently open {@link CellEditor}. After
 * {@link #getCellEditor(Object)} has been called by the JFace framework, the currently open
 * {@link CellEditor} can be retrieved using the {@link #getCurrentCellEditor()} method. If the
 * {@link CellEditor} is closed (editing finished or was canceled) {@link #getCurrentCellEditor()}
 * returns <code>null</code>.
 * 
 * Moreover this {@link EditingSupport} can hold a {@link AbstractPermanentTraversalStrategy} which
 * can by set using {@link #setTraversalStrategy(AbstractPermanentTraversalStrategy)}. The
 * {@link AbstractPermanentTraversalStrategy} instance will then be added as traversal- and
 * key-listener to every {@link CellEditor} created by this {@link EditingSupport}.
 * 
 * @author Stefan Widmaier
 */
public abstract class CellTrackingEditingSupport extends EditingSupport {

    private IpsCellEditor currentCellEditor;
    private Object currentViewItem;

    private AbstractPermanentTraversalStrategy traversalStrategy;

    /**
     * Creates a {@link AbstractPermanentTraversalStrategy} for the given viewer. A
     * {@link AbstractPermanentTraversalStrategy} can be set using
     * {@link #setTraversalStrategy(AbstractPermanentTraversalStrategy)}.
     */
    public CellTrackingEditingSupport(ColumnViewer viewer) {
        super(viewer);
    }

    @Override
    protected CellEditor getCellEditor(Object element) {
        IpsCellEditor cellEditor = getCellEditorInternal(element);
        currentCellEditor = cellEditor;
        currentViewItem = element;
        if (currentCellEditor != null) {
            currentCellEditor.addListener(new CellEditorListener());
            if (traversalStrategy != null) {
                currentCellEditor.setTraversalStrategy(traversalStrategy);
            }
        }
        return cellEditor;
    }

    /**
     * Creates an {@link IpsCellEditor} for editing the given element.
     * 
     * @param element the element to edit
     * @return the newly created {@link IpsCellEditor}
     */
    protected abstract IpsCellEditor getCellEditorInternal(Object element);

    /**
     * Sets the {@link AbstractPermanentTraversalStrategy} instance to be used by this
     * {@link EditingSupport}.
     * 
     * @param traversalStrategy The new {@link TraversalStrategy} to be used.
     */
    public void setTraversalStrategy(AbstractPermanentTraversalStrategy traversalStrategy) {
        this.traversalStrategy = traversalStrategy;
    }

    protected AbstractPermanentTraversalStrategy getTraversalStrategy() {
        return traversalStrategy;
    }

    /**
     * 
     * @return the currently active cell editor, or <code>null</code> if no cell editor is active
     *         for the column (or editing support respectively).
     */
    protected IpsCellEditor getCurrentCellEditor() {
        return currentCellEditor;
    }

    protected Object getCurrentViewItem() {
        return currentViewItem;
    }

    private void resetCellEditor() {
        currentCellEditor = null;
        currentViewItem = null;
    }

    private class CellEditorListener implements ICellEditorListener {

        @Override
        public void editorValueChanged(boolean oldValidState, boolean newValidState) {
            // nothing to do
        }

        @Override
        public void cancelEditor() {
            resetCellEditor();
        }

        @Override
        public void applyEditorValue() {
            resetCellEditor();
        }
    }
}
