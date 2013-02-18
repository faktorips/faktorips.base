/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.controls.tableedit;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.faktorips.devtools.core.ui.dialogs.MultiValueTableModel;

/**
 * Content provider for a {@link EditTableControlViewer} that uses an {@link MultiValueTableModel} .
 * 
 * @author Stefan Widmaier
 */
public class ListTableModelContentProvider implements IStructuredContentProvider {

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // nothing to do
    }

    @Override
    public void dispose() {
        // nothing to do
    }

    @Override
    public Object[] getElements(Object inputElement) {
        IEditTableModel<?> model = (IEditTableModel<?>)inputElement;
        return model.getElements().toArray();
    }

}
