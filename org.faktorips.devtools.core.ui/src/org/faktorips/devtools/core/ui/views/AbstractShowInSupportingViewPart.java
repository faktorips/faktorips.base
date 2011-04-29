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

package org.faktorips.devtools.core.ui.views;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.IShowInTarget;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.part.ViewPart;

public abstract class AbstractShowInSupportingViewPart extends ViewPart implements IShowInSource, IShowInTarget {

    @Override
    public ShowInContext getShowInContext() {
        return new ShowInContext(this, getSelection());
    }

    protected abstract ISelection getSelection();

    @Override
    public boolean show(ShowInContext context) {
        if (context.getSelection() instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection)context.getSelection();
            Object firstElement = structuredSelection.getFirstElement();
            // Some crazy implementations like SearchResults return arrays as elements. In this case
            // we try to view the first element
            if (firstElement instanceof Object[]) {
                firstElement = ((Object[])firstElement)[0];
            }
            if (firstElement instanceof IAdaptable) {
                IAdaptable adaptable = (IAdaptable)firstElement;
                if (show(adaptable)) {
                    return true;
                    // If the view was not able to show the selection we keep on trying
                }
            }
        }
        if (context.getInput() instanceof IFileEditorInput) {
            IFile file = ((IFileEditorInput)context.getInput()).getFile();
            if (file == null) {
                return false;
            }
            show(file);
        }
        return false;
    }

    protected abstract boolean show(IAdaptable adaptable);
}
