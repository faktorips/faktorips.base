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

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsPackageSelectionDialog;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.contentproposal.ContentProposals;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;

/**
 * A control to edit a package fragment reference.
 */
public class IpsPckFragmentRefControl extends TextButtonControl {

    private IIpsPackageFragmentRoot ipsPckFragmentRoot;

    public IpsPckFragmentRefControl(Composite parent, UIToolkit toolkit) {
        super(parent, toolkit, Messages.IpsPckFragmentRefControl_titleBrowse);
        ContentProposals.forText(getTextControl(), new IpsPckFragmenContentProposalProvider(this));
    }

    public void setIpsPckFragmentRoot(IIpsPackageFragmentRoot root) {
        this.ipsPckFragmentRoot = root;
        setButtonEnabled(root != null && root.exists());
    }

    public IIpsPackageFragmentRoot getIpsPckFragmentRoot() {
        return ipsPckFragmentRoot;
    }

    public IIpsPackageFragment getIpsPackageFragment() {
        if (ipsPckFragmentRoot == null) {
            return null;
        }
        return ipsPckFragmentRoot.getIpsPackageFragment(getTextControl().getText());
    }

    public void setIpsPackageFragment(IIpsPackageFragment newPack) {
        if (newPack == null) {
            getTextControl().setText(""); //$NON-NLS-1$
        } else {
            setText(newPack.getName());
        }
    }

    @Override
    protected void buttonClicked() {
        try {
            IpsPackageSelectionDialog dialog = new IpsPackageSelectionDialog(getShell());
            if (ipsPckFragmentRoot != null) {
                dialog.setElements(ipsPckFragmentRoot.getIpsPackageFragments());
            }
            if (dialog.open() == Window.OK) {
                if (dialog.getSelectedPackage() != null) {
                    setText(dialog.getSelectedPackage().getName());
                } else {
                    setText(""); //$NON-NLS-1$
                }
            }
            // CSOFF: IllegalCatch
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
        // CSON: IllegalCatch
    }

}
