/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.ui.PdPackageSelectionDialog;
import org.faktorips.devtools.core.ui.UIToolkit;


/**
 * A control to edit a package fragment reference.
 */
public class IpsPckFragmentRefControl extends TextButtonControl {

    private IIpsPackageFragmentRoot root;
    
    public IpsPckFragmentRefControl(
            Composite parent, 
            UIToolkit toolkit) {
        super(parent, toolkit, Messages.IpsPckFragmentRefControl_titleBrowse);
    }
    
    public void setIpsPckFragmentRoot(IIpsPackageFragmentRoot root) {
        this.root = root;
        setButtonEnabled(root!=null && root.exists());
    }

    public IIpsPackageFragment getIpsPackageFragment() {
        if (root==null) {
            return null;
        }
        return root.getIpsPackageFragment(text.getText());
    }
    
    public void setIpsPackageFragment(IIpsPackageFragment newPack) {
        if (newPack==null) {
            text.setText(""); //$NON-NLS-1$
        } else {
            setText(newPack.getName());
        }
    }
    

    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.controls.TextButtonControl#buttonClicked()
     */ 
    protected void buttonClicked() {
        try {
            PdPackageSelectionDialog dialog = new PdPackageSelectionDialog(getShell());
            if (root!=null) {
                dialog.setElements(root.getIpsPackageFragments());    
            }
            if (dialog.open()==Window.OK) {
                if (dialog.getSelectedPackage()!=null) {
                    setText(dialog.getSelectedPackage().getName());
                } else {
                    setText(""); //$NON-NLS-1$
                }
            }
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }


}
