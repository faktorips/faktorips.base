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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.faktorips.util.ArgumentCheck;

/**
 * Base class for all <code>CellEditor</code>s for ColumnViewers. This cell editor is created with a
 * control that is displayed when the user edits a table cell.
 * <p>
 * This CellEditor can be configured to create new rows if needed and append them to the end of the
 * table.
 * 
 * @author Stefan Widmaier, Alexander Weickmann
 */
public abstract class IpsCellEditor extends CellEditor {

    /** The control to be displayed when the user edits a table cell. */
    private Control control;

    /**
     * Field for the {@link TraversalStrategy} this {@link IpsCellEditor} uses.
     */
    private TraversalStrategy traversalStrategy;

    /**
     * Constructs a <code>TableCellEditor</code> that is used in the given <code>TableViewer</code>.
     * The given control is displayed when a cell is edited. The given <code>columnIndex</code>
     * indicates in which column of the table this editor is used.
     * <p>
     * The created <code>TableCellEditor</code> does not create rows automatically and must be
     * configured to do so.
     * 
     * @param control The control to be displayed in a cell when editing.
     * 
     * @throws NullPointerException If <code>control</code> is <code>null</code>.
     */
    public IpsCellEditor(Control control) {
        // Do not call super-constructor.
        ArgumentCheck.notNull(control);
        deactivate();

        this.control = control;
    }

    @Override
    public LayoutData getLayoutData() {
        LayoutData layoutData = super.getLayoutData();
        layoutData.minimumWidth = Math.min(70, layoutData.minimumWidth);
        return layoutData;
    }

    /**
     * Registers the given {@link TraversalStrategy} with this {@link CellEditor}.
     */
    public void setTraversalStrategy(TraversalStrategy strategy) {
        removeStrategyAsListener(traversalStrategy);
        traversalStrategy = strategy;
        addStrategyAsListener(traversalStrategy);
    }

    private void removeStrategyAsListener(TraversalStrategy strategy) {
        if (strategy != null) {
            getControl().removeKeyListener(strategy);
            getControl().removeTraverseListener(strategy);
            getControl().removeFocusListener(strategy);
        }
    }

    protected void addStrategyAsListener(TraversalStrategy strategy) {
        if (strategy != null) {
            getControl().addKeyListener(strategy);
            getControl().addTraverseListener(strategy);
            getControl().addFocusListener(strategy);
        }
    }

    /**
     * Returns <code>true</code> if this <code>TableCellEditor</code> supports mapping between an id
     * and a text which will be displayed (e.g. a combo box internally stores an id but the user can
     * select the value by using names).
     * <p>
     * Returns <code>false</code> otherwise.
     */
    public abstract boolean isMappedValue();

    /**
     * {@inheritDoc}
     * <p>
     * This method is never called, since the super-constructor is not used to create this
     * <code>CellEditor</code>. Returns the control given at instantiation.
     */
    @Override
    protected Control createControl(Composite parent) {
        return control;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Copied from super class. We needed to do this because we do not call the super constructor
     * and so the <code>control</code> attribute of the super class is always <code>null</code>.
     * This method accesses the duplicated attribute in this class.
     */
    @Override
    public void deactivate() {
        if (control != null && !(control.isDisposed())) {
            control.setVisible(false);
            // FS#1607 wrong row changed after scrolling using vertical scroll bar,
            // Note that the value is stored by ColumnViewerEditor#applyEditorValue (Eclipse 3.4)
            // saveCurrentValue();
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Copied from super class. We needed to do this because we do not call the super constructor
     * and so the <code>control</code> attribute of the super class is always <code>null</code>.
     * This method accesses the duplicated attribute in this class.
     */
    @Override
    public void dispose() {
        if (control != null && !(control.isDisposed())) {
            control.dispose();
        }
        control = null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Copied from super class. We needed to do this because we do not call the super constructor
     * and so the <code>control</code> attribute of the super class is always <code>null</code>.
     * This method accesses the duplicated attribute in this class.
     */
    @Override
    public Control getControl() {
        return control;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Copied from super class. We needed to do this because we do not call the super constructor
     * and so the <code>control</code> attribute of the super class is always <code>null</code>.
     * This method accesses the duplicated attribute in this class.
     */
    @Override
    public boolean isActivated() {
        return control != null && control.isVisible();
    }

    /**
     * Overwritten to make method visible.
     */
    @Override
    protected void fireApplyEditorValue() {
        super.fireApplyEditorValue();
    }

    /**
     * Returns the {@link TraversalStrategy} used for this {@link IpsCellEditor}. Will return
     * <code>null</code> if no {@link TraversalStrategy} is set using
     * {@link #setTraversalStrategy(TraversalStrategy)}.
     */
    public TraversalStrategy getTraversalStrategy() {
        return traversalStrategy;
    }

    // override for getting visible in DelegateCellEditor
    @Override
    protected abstract Object doGetValue();

    // override for getting visible in DelegateCellEditor
    @Override
    protected abstract void doSetValue(Object value);

    // override for getting visible in DelegateCellEditor
    @Override
    protected abstract void doSetFocus();

}
