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

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.contentassist.ContentAssistHandler;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.ui.CompletionUtil;
import org.faktorips.devtools.core.ui.IpsPackageSelectionDialog;
import org.faktorips.devtools.core.ui.UIToolkit;

/**
 * A control to edit a package fragment reference.
 */
public class IpsPckFragmentRefControl extends TextButtonControl {

    public static final String PACKAGE_FRAGMENT_ROOT = "ipsPckFragmentRoot"; //$NON-NLS-1$

    private IIpsPackageFragmentRoot ipsPckFragmentRoot;

    private IpsPckFragmenCompletionProcessor completionProcessor;

    public IpsPckFragmentRefControl(Composite parent, UIToolkit toolkit) {
        super(parent, toolkit, Messages.IpsPckFragmentRefControl_titleBrowse);

        completionProcessor = new IpsPckFragmenCompletionProcessor(this);
        ContentAssistHandler.createHandlerForText(text, CompletionUtil.createContentAssistant(completionProcessor));
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
        return ipsPckFragmentRoot.getIpsPackageFragment(text.getText());
    }

    public void setIpsPackageFragment(IIpsPackageFragment newPack) {
        if (newPack == null) {
            text.setText(""); //$NON-NLS-1$
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
        } catch (Exception e) {
            // TODO catch Exception needs to be documented properly or specialized
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

}
