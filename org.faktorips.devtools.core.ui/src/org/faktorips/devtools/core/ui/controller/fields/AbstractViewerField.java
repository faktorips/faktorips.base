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

package org.faktorips.devtools.core.ui.controller.fields;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.faktorips.devtools.core.ui.controller.EditField;

/**
 * A field that handles the selection of a {@link Viewer}
 * 
 * @see EditField for details about generic type T
 * 
 * @author Cornelius.Dirmeier
 */
public abstract class AbstractViewerField<T> extends DefaultEditField<T> {

    private final Viewer viewer;

    public AbstractViewerField(Viewer viewer) {
        this.viewer = viewer;
    }

    @Override
    public Control getControl() {
        return viewer.getControl();
    }

    @Override
    protected int getMessageDecorationPosition() {
        return SWT.LEFT | SWT.TOP;
    }

    @Override
    public void insertText(String text) {
        // viewer.setSelection(new StructuredSelection(text));
    }

    @Override
    public void selectAll() {
        // viewer.setSelection(new StructuredSelection(viewer.getInput()));
    }

    @Override
    protected void addListenerToControl() {
        viewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                notifyChangeListeners(new FieldValueChangedEvent(AbstractViewerField.this));
            }
        });
    }

    protected Viewer getViewer() {
        return viewer;
    }

}
