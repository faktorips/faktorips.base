/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.dialogs.PropertyDialogAction;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * Opens a properties dialog for the selected object.
 * 
 * @author Stefan Widmaier
 */
public class IpsPropertiesAction extends IpsAction {

    private IWorkbenchSite site;

    public IpsPropertiesAction(IWorkbenchSite site, ISelectionProvider selectionProvider) {
        super(selectionProvider);
        this.site = site;
        setText(Messages.IpsPropertiesAction_name);
    }

    /**
     * Opens the Properties dialog for the selected Project.
     */
    @Override
    public void run(IStructuredSelection selection) {
        Object selected = selection.getFirstElement();
        if (selected instanceof IIpsProject) {
            openPropertiesDialog(((IIpsProject)selected).getProject());
        } else if (selected != null) {
            openPropertiesDialog(selected);
        }
    }

    private void openPropertiesDialog(Object selectedObject) {
        IStructuredSelection sel = new StructuredSelection(selectedObject);
        SelProvider provider = new SelProvider(sel);
        PropertyDialogAction action = new PropertyDialogAction(site, provider);
        action.run();
    }

    /**
     * Returns <code>true</code> if the property action supports the given object.
     */
    public boolean isEnabledFor(Object selected) {
        if (selected instanceof IIpsObjectPart) {
            return false;
        }
        return true;
    }

    /**
     * Helper class to simulate a selection provider
     */
    private static final class SelProvider implements ISelectionProvider {

        private IStructuredSelection projectSelection = StructuredSelection.EMPTY;

        protected SelProvider(IStructuredSelection selection) {
            projectSelection = selection;
        }

        @Override
        public void addSelectionChangedListener(ISelectionChangedListener listener) {
            // do nothing
        }

        @Override
        public ISelection getSelection() {
            return projectSelection;
        }

        @Override
        public void removeSelectionChangedListener(ISelectionChangedListener listener) {
            // do nothing
        }

        @Override
        public void setSelection(ISelection selection) {
            // do nothing
        }
    }

}
