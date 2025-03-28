/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

public class IpsPackageFragmentRootSelectionDialog extends ElementTreeSelectionDialog {

    public IpsPackageFragmentRootSelectionDialog(Shell parent, boolean onlySourceRoots) {
        super(parent, new DefaultLabelProvider(), new ContentProvider(onlySourceRoots));
        setTitle(Messages.PdSourceRootSelectionDialog_title);
        setMessage(Messages.PdSourceRootSelectionDialog_description);
        setAllowMultiple(false);
        setInput(IIpsModel.get());
    }

    public IIpsPackageFragmentRoot getSelectedRoot() {
        if (getResult().length > 0) {
            Object result = getResult()[0];
            if (result instanceof IIpsPackageFragmentRoot root) {
                return root;
            } else if (result instanceof IIpsProject ipsProject) {
                return ipsProject.getIpsPackageFragmentRoots()[0];
            }
        }
        return null;
    }

    private static class ContentProvider implements ITreeContentProvider {

        /** true if only package fragment roots contains sourcecode are allowed. */
        private boolean onlySourceRoots;

        ContentProvider(boolean onlySourceRoots) {
            this.onlySourceRoots = onlySourceRoots;
        }

        @Override
        public Object[] getChildren(Object parentElement) {
            if (parentElement instanceof IIpsProject) {
                if (onlySourceRoots) {
                    return ((IIpsProject)parentElement).getSourceIpsPackageFragmentRoots();
                } else {
                    return ((IIpsProject)parentElement).getIpsPackageFragmentRoots();
                }
            }
            return new Object[0];
        }

        @Override
        public Object getParent(Object element) {
            return ((IIpsElement)element).getParent();
        }

        @Override
        public boolean hasChildren(Object element) {
            return getChildren(element).length > 0;
        }

        @Override
        public Object[] getElements(Object inputElement) {
            try {
                return IIpsModel.get().getIpsProjects();
            } catch (Exception e) {
                IpsPlugin.logAndShowErrorDialog(e);
                return new Object[0];
            }
        }

        @Override
        public void dispose() {
            // Nothing to do
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            // Nothing to do
        }

    }

}
