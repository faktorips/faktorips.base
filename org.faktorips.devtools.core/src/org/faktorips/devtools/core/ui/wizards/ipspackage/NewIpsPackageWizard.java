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

package org.faktorips.devtools.core.ui.wizards.ipspackage;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsPackageFragment;


/**
 *
 */
public class NewIpsPackageWizard extends Wizard implements INewWizard {

    private IStructuredSelection selection;
    
    private IpsPackagePage packagePage;
    
    public NewIpsPackageWizard() {
        setWindowTitle(Messages.IpsPackagePage_title);
        this.setDefaultPageImageDescriptor(IpsPlugin.getDefault().getImageDescriptor("wizards/NewIpsPackageWizard.png"));
    }
    
    /** 
     * Overridden method.
     * @see org.eclipse.jface.wizard.IWizard#addPages()
     */
    public final void addPages() {
        try {
            packagePage = createFirstPage(selection); 
            addPage(packagePage);
            createAdditionalPages();
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }
    
    /** 
     * Overridden method.
     * @throws JavaModelException
     * @see org.faktorips.devtools.core.ui.wizards.NewIpsObjectWizard#createFirstPage()
     */
    protected IpsPackagePage createFirstPage(IStructuredSelection selection) throws JavaModelException {
        return new IpsPackagePage(selection);
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.wizards.NewIpsObjectWizard#createAdditionalPages()
     */
    protected void createAdditionalPages() {
    }

    /** 
     * Overridden method.
     * @see org.eclipse.jface.wizard.IWizard#performFinish()
     */
    public final boolean performFinish() {
        try {
            IIpsPackageFragment pack = packagePage.getIpsPackageFragment();
            pack.createSubPackage(packagePage.getIpsPackageName(), true, null);
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
        return true;
    }
    

    /** 
     * Overridden method.
     * 
     * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
     */
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.selection = selection;
    }
    
   

}
