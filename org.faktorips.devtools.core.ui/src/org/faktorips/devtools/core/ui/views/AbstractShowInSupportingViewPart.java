/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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
import org.faktorips.devtools.core.ui.editors.IpsArchiveEditorInput;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;

/**
 * Abstract ViewPart implementation for all classes that should be navigatable by show-in
 * functionality.
 * 
 * @author dirmeier
 */
public abstract class AbstractShowInSupportingViewPart extends ViewPart implements IShowInSource, IShowInTarget {

    @Override
    public ShowInContext getShowInContext() {
        return new ShowInContext(this, getSelection());
    }

    /**
     * Get the current selection for the show in context.#
     */
    protected abstract ISelection getSelection();

    @Override
    public boolean show(ShowInContext context) {
        if (context.getSelection() instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection)context.getSelection();
            return show(structuredSelection);
        }
        if (context.getInput() instanceof IFileEditorInput) {
            IFileEditorInput fileEditorInput = (IFileEditorInput)context.getInput();
            return show(fileEditorInput);
        }
        if (context.getInput() instanceof IpsArchiveEditorInput) {
            IpsArchiveEditorInput ipsArchiveEditorInput = (IpsArchiveEditorInput)context.getInput();
            return show(ipsArchiveEditorInput);
        }
        return false;
    }

    private boolean show(IpsArchiveEditorInput ipsArchiveEditorInput) {
        IIpsSrcFile srcFile = ipsArchiveEditorInput.getIpsSrcFile();
        if (srcFile == null) {
            return false;
        }
        return show(srcFile);
    }

    private boolean show(IFileEditorInput fileEditorInput) {
        IFile file = fileEditorInput.getFile();
        if (file == null) {
            return false;
        }
        return show(file);
    }

    private boolean show(IStructuredSelection structuredSelection) {
        Object firstElement = structuredSelection.getFirstElement();
        // Some crazy implementations like SearchResults return arrays as elements. In this case
        // we try to view the first element
        if (firstElement instanceof Object[]) {
            firstElement = ((Object[])firstElement)[0];
        }
        if (firstElement instanceof IAdaptable) {
            IAdaptable adaptable = (IAdaptable)firstElement;
            return show(adaptable);
        }
        return false;
    }

    /**
     * Open the adaptable object in the view and return true if it was successful.
     */
    protected abstract boolean show(IAdaptable adaptable);
}
