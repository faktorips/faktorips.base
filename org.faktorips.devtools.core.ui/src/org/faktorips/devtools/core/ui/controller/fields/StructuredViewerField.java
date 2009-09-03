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

package org.faktorips.devtools.core.ui.controller.fields;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;

/**
 * 
 * @author Cornelius.Dirmeier
 */
public class StructuredViewerField extends AbstractViewerField {

    private final StructuredViewer strucuredViewer;

    /**
     * 
     */
    public StructuredViewerField(StructuredViewer viewer) {
        super(viewer);
        strucuredViewer = viewer;
    }

    /**
     * {@inheritDoc}
     */
    public String getText() {
        return ((IStructuredSelection)strucuredViewer.getSelection()).getFirstElement().toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object parseContent() {
        Object o = ((IStructuredSelection)strucuredViewer.getSelection()).getFirstElement();
        return prepareObjectForGet(o);
    }

    /**
     * {@inheritDoc}
     */
    public void setValue(Object newValue) {
        ISelection selection = new StructuredSelection(prepareObjectForSet(newValue));
        strucuredViewer.setSelection(selection, true);
    }

    /**
     * {@inheritDoc}
     */
    public void setText(String newText) {
        strucuredViewer.setSelection(new StructuredSelection(newText));
    }

}
