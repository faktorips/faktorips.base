/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
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
