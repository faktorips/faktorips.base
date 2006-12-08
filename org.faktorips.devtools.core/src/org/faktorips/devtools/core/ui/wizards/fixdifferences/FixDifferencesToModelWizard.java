/***************************************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorips.org/legal/cl-v01.html eingesehen werden kann.
 * 
 * Mitwirkende:  Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de  
 **************************************************************************************************/

package org.faktorips.devtools.core.ui.wizards.fixdifferences;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.faktorips.devtools.core.IpsPlugin;

/**
 * 
 * @author Daniel Hohenberger
 */
public class FixDifferencesToModelWizard extends Wizard implements IWorkbenchWizard {
    private Set ipsElementsToFix;
    private ElementSelectionPage elementSelectionPage;

    public FixDifferencesToModelWizard(Set ipsElementsToFix) {
        this.ipsElementsToFix = ipsElementsToFix;
    }
    public FixDifferencesToModelWizard() {
        this.ipsElementsToFix = new HashSet();
        setWindowTitle(Messages.FixDifferencesToModelWizard_Title);
        this.setDefaultPageImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("wizards/NewIpsPackageWizard.png")); //$NON-NLS-1$
    }

    public boolean performFinish() {
        // TODO Auto-generated method stub
        return false;
    }

    public void init(IWorkbench workbench, IStructuredSelection selection) {
        // TODO Auto-generated method stub
        
    }
    
    public void addPages() {
        super.addPages();
        elementSelectionPage = new ElementSelectionPage(ipsElementsToFix);
        super.addPage(elementSelectionPage);
    }

}
