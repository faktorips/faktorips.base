/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.search.model.scope;

import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

public class ModelSearchSelectionScope extends AbstractModelSearchScope {

    ISelection selection;

    public ModelSearchSelectionScope(ISelection selection) {
        this.selection = selection;
    }

    @Override
    protected String getScopeTypeLabel(boolean singular) {
        return ""; //$NON-NLS-1$
    }

    @Override
    protected List<?> getSelectedObjects() {
        return ((IStructuredSelection)selection).toList();
    }

}
