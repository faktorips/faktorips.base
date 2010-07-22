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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.faktorips.util.ArgumentCheck;

/**
 * Base class for all <tt>CellEditor</tt>s for ColumnViewers. This cell editor is created with a
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
     * Constructs a <tt>TableCellEditor</tt> that is used in the given <tt>TableViewer</tt>. The
     * given control is displayed when a cell is edited. The given <tt>columnIndex</tt> indicates in
     * which column of the table this editor is used.
     * <p>
     * The created <tt>TableCellEditor</tt> does not create rows automatically and must be
     * configured to do so.
     * 
     * @see #setRowCreating(boolean)
     * 
     * @param tableViewer The <tt>TableViewer</tt> this <tt>TableCellEditor</tt> is used in.
     * @param columnIndex The index of the column which cells this editor edits.
     * @param control The control to be displayed in a cell when editing.
     * 
     * @throws NullPointerException If <tt>control</tt> is <tt>null</tt>.
     */
    public IpsCellEditor(Control control) {
        // Do not call super-constructor.
        ArgumentCheck.notNull(control);
        deactivate();

        this.control = control;
    }

    /**
     * Registers the given {@link TraversalStrategy} with this {@link CellEditor}.
     * 
     * @param strategy
     */
    public void setTraversalStrategy(TraversalStrategy strategy) {
        if (strategy != null) {
            traversalStrategy = strategy;
            getControl().addKeyListener(strategy);
            getControl().addTraverseListener(strategy);
            getControl().addFocusListener(strategy);
        }
    }

    /**
     * Returns <tt>true</tt> if this <tt>TableCellEditor</tt> supports mapping between an id and a
     * text which will be displayed (e.g. a combo box internally stores an id but the user can
     * select the value by using names).
     * <p>
     * Returns <tt>false</tt> otherwise.
     */
    public abstract boolean isMappedValue();

    /**
     * {@inheritDoc}
     * <p>
     * This method is never called, since the super-constructor is not used to create this
     * <tt>CellEditor</tt>. Returns the control given at instantiation.
     */
    @Override
    protected Control createControl(Composite parent) {
        return control;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Copied from super class. We needed to do this because we do not call the super constructor
     * and so the <tt>control</tt> attribute of the super class is always <tt>null</tt>. This method
     * accesses the duplicated attribute in this class.
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
     * and so the <tt>control</tt> attribute of the super class is always <tt>null</tt>. This method
     * accesses the duplicated attribute in this class.
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
     * and so the <tt>control</tt> attribute of the super class is always <tt>null</tt>. This method
     * accesses the duplicated attribute in this class.
     */
    @Override
    public Control getControl() {
        return control;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Copied from super class. We needed to do this because we do not call the super constructor
     * and so the <tt>control</tt> attribute of the super class is always <tt>null</tt>. This method
     * accesses the duplicated attribute in this class.
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
     * 
     * @return
     */
    public TraversalStrategy getTraversalStrategy() {
        return traversalStrategy;
    }
}
