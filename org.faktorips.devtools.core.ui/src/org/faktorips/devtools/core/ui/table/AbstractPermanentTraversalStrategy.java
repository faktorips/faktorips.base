/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.table;

import org.faktorips.devtools.core.ui.controls.tableedit.FormattedCellEditingSupport;

/**
 * {@link TraversalStrategy} that can be added to different {@link IpsCellEditor}s during its
 * lifetime (the {@link TableTraversalStrategy} in contrast is instantiated for every
 * {@link IpsCellEditor} separately). The {@link AbstractPermanentTraversalStrategy} is designed to
 * be used with an {@link FormattedCellEditingSupport} object. The
 * {@link FormattedCellEditingSupport} should hold an instance of the
 * {@link AbstractPermanentTraversalStrategy} and register it with each {@link IpsCellEditor} after
 * it is created.
 * <p>
 * The generic Type T defines the type of the object displayed by the columns, provided by the
 * content provider
 * 
 * @author Stefan Widmaier
 */
public abstract class AbstractPermanentTraversalStrategy<T> implements TraversalStrategy {

    private CellTrackingEditingSupport<T> editingSupport;

    public AbstractPermanentTraversalStrategy(CellTrackingEditingSupport<T> editingSupport) {
        super();
        this.editingSupport = editingSupport;
    }

    protected T getCurrentViewItem() {
        return getEditingSupport().getCurrentViewItem();
    }

    protected IpsCellEditor getCurrentCellEditor() {
        return getEditingSupport().getCurrentCellEditor();
    }

    protected void fireApplyEditorValue() {
        if (getCurrentCellEditor() != null) {
            getCurrentCellEditor().fireApplyEditorValue();
        }
    }

    protected CellTrackingEditingSupport<T> getEditingSupport() {
        return editingSupport;
    }

}
