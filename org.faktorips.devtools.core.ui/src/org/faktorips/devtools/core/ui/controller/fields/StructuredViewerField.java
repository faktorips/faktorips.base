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

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.util.TypedSelection;

/**
 * A field that handles the selection of a {@link StructuredViewer}
 * 
 * @see EditField for details about generic type T
 * @author Cornelius.Dirmeier
 */
public class StructuredViewerField<T> extends AbstractViewerField<T> {

    private final StructuredViewer strucuredViewer;
    private final Class<T> type;

    public StructuredViewerField(StructuredViewer viewer, Class<T> type) {
        super(viewer);
        strucuredViewer = viewer;
        this.type = type;
    }

    public static <O> StructuredViewerField<O> newInstance(StructuredViewer viewer, Class<O> type) {
        return new StructuredViewerField<O>(viewer, type);
    }

    @Override
    public String getText() {
        return ((IStructuredSelection)strucuredViewer.getSelection()).getFirstElement().toString();
    }

    @Override
    public T parseContent() {
        TypedSelection<T> selection = new TypedSelection<T>(type, strucuredViewer.getSelection());
        if (!selection.isValid()) {
            return null;
        }
        T o = selection.getFirstElement();

        if (supportsNullStringRepresentation() && IpsPlugin.getDefault().getIpsPreferences().getNullPresentation().equals(o)) {
            return null;
        }
        return o;
    }

    @Override
    public void setValue(T newValue) {
        ISelection selection = null;
        if (newValue == null) {
            selection = new StructuredSelection(IpsPlugin.getDefault().getIpsPreferences().getNullPresentation());
        } else {
            selection = new StructuredSelection(newValue);
        }
        strucuredViewer.setSelection(selection, true);
    }

    @Override
    public void setText(String newText) {
        strucuredViewer.setSelection(new StructuredSelection(newText));
    }

}
