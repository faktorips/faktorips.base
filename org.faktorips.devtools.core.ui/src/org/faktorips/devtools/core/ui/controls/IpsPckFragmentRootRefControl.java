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

package org.faktorips.devtools.core.ui.controls;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.ui.IpsPackageFragmentRootSelectionDialog;
import org.faktorips.devtools.core.ui.UIToolkit;

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
            String newText = root.getCorrespondingResource().getFullPath().toString().substring(1);
            setText(newText);
        }
    }

    public IIpsPackageFragmentRoot getIpsPackageFragmentRoot() {
        IWorkspaceRoot wpRoot = ResourcesPlugin.getWorkspace().getRoot();
        String pathString = IPath.SEPARATOR + getText();
        try {
            Path path = new Path(pathString);
            IFolder folder = wpRoot.getFolder(path);
            IIpsElement element = IpsPlugin.getDefault().getIpsModel().getIpsElement(folder);
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
