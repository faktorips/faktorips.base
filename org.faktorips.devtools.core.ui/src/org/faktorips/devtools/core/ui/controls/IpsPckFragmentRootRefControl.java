/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsPackageFragmentRootSelectionDialog;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;

/**
 * A control to edit a reference to a package fragment root containing source code.
 */
public class IpsPckFragmentRootRefControl extends TextButtonControl {

    /** true if only package fragment roots contains sourcecode are allowed. */
    private boolean onlySourceRoots;

    public IpsPckFragmentRootRefControl(Composite parent, boolean onlySourceRoots, UIToolkit toolkit) {
        super(parent, toolkit, Messages.IpsPckFragmentRootRefControl_title);
        this.onlySourceRoots = onlySourceRoots;
    }

    public void setIpsPackageFragmentRoot(IIpsPackageFragmentRoot root) {
        if (root == null) {
            setText(""); //$NON-NLS-1$
        } else {
            String newText = root.getCorrespondingResource().getWorkspaceRelativePath().toString().substring(1);
            setText(newText);
        }
    }

    public IIpsPackageFragmentRoot getIpsPackageFragmentRoot() {
        IWorkspaceRoot wpRoot = ResourcesPlugin.getWorkspace().getRoot();
        String pathString = IPath.SEPARATOR + getText();
        try {
            Path path = new Path(pathString);
            IFolder folder = wpRoot.getFolder(path);
            IIpsElement element = IIpsModel.get().getIpsElement(folder);
            if (element instanceof IIpsPackageFragmentRoot) {
                return (IIpsPackageFragmentRoot)element;
            }
        } catch (IllegalArgumentException e) {
            // string is not a valid path
        }
        return null;
    }

    @Override
    protected void buttonClicked() {
        try {
            IpsPackageFragmentRootSelectionDialog dialog = new IpsPackageFragmentRootSelectionDialog(getShell(),
                    onlySourceRoots);
            if (dialog.open() == Window.OK) {
                IIpsPackageFragmentRoot root = dialog.getSelectedRoot();
                setIpsPackageFragmentRoot(root);
            }
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

}
