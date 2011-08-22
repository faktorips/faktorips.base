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

package org.faktorips.devtools.core.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

public class IpsPackageFragmentRootSelectionDialog extends ElementTreeSelectionDialog {

    public IpsPackageFragmentRootSelectionDialog(Shell parent, boolean onlySourceRoots) {
        super(parent, new DefaultLabelProvider(), new ContentProvider(onlySourceRoots));
        setTitle(Messages.PdSourceRootSelectionDialog_title);
        setMessage(Messages.PdSourceRootSelectionDialog_description);
        setAllowMultiple(false);
        setInput(IpsPlugin.getDefault().getIpsModel());
    }

    public IIpsPackageFragmentRoot getSelectedRoot() {
        if (getResult().length > 0) {
            Object result = getResult()[0];
            if (result instanceof IIpsPackageFragmentRoot) {
                return (IIpsPackageFragmentRoot)this.getResult()[0];
            } else if (result instanceof IIpsProject) {
                return ((IIpsProject)result).getIpsPackageFragmentRoots()[0];
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
                try {
                    if (onlySourceRoots) {
                        return ((IIpsProject)parentElement).getSourceIpsPackageFragmentRoots();
                    } else {
                        return ((IIpsProject)parentElement).getIpsPackageFragmentRoots();
                    }
                } catch (CoreException e) {
                    return new Object[0];
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
                return IpsPlugin.getDefault().getIpsModel().getIpsProjects();
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
