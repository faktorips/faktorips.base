/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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
 * <p>
 * The generic Type T defines the type of the object displayed by the columns, provided by the
 * content provider
 * 
 * @author Stefan Widmaier
 */
public abstract class CellTrackingEditingSupport<T> extends EditingSupport {

    private IpsCellEditor currentCellEditor;
    private T currentViewItem;

    private AbstractPermanentTraversalStrategy<T> traversalStrategy;

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
        @SuppressWarnings("unchecked")
        // the object is passed from superclass, we try to get as much type safety as possible but
        // we cannot check this cast
        T castedElement = (T)element;
        currentViewItem = castedElement;
        IpsCellEditor cellEditor = getCellEditorInternal(castedElement);
        currentCellEditor = cellEditor;
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
    protected abstract IpsCellEditor getCellEditorInternal(T element);

    /**
     * Sets the {@link AbstractPermanentTraversalStrategy} instance to be used by this
     * {@link EditingSupport}.
     * 
     * @param traversalStrategy The new {@link TraversalStrategy} to be used.
     */
    public void setTraversalStrategy(AbstractPermanentTraversalStrategy<T> traversalStrategy) {
        this.traversalStrategy = traversalStrategy;
    }

    protected AbstractPermanentTraversalStrategy<T> getTraversalStrategy() {
        return traversalStrategy;
    }

    /**
     * 
     * @return the currently active cell editor, or <code>null</code> if no cell editor is active
     *             for the column (or editing support respectively).
     */
    protected IpsCellEditor getCurrentCellEditor() {
        return currentCellEditor;
    }

    protected T getCurrentViewItem() {
        return currentViewItem;
    }

    private void resetCellEditor() {
        currentCellEditor = null;
        currentViewItem = null;
    }

    @Override
    protected abstract boolean canEdit(Object element);

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
