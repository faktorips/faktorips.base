/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
        T o = selection.getFirstElement();

        if (supportsNull() && IpsPlugin.getDefault().getIpsPreferences().getNullPresentation().equals(o)) {
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
