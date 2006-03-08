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

package org.faktorips.devtools.core.ui.wizards;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;


/**
 * Base class for wizards to create a new ips object.
 */
public abstract class NewIpsObjectWizard extends Wizard implements INewWizard {
    
    private IStructuredSelection selection;
    
    // first page
    private IpsObjectPage objectPage;
    
    private IpsObjectType ipsObjectType;
    
    public NewIpsObjectWizard(IpsObjectType type) {
        ipsObjectType = type;
        setWindowTitle(Messages.NewIpsObjectWizard_title + ipsObjectType.getName());
    }
    
    /** 
     * Overridden method.
     * @see org.eclipse.jface.wizard.IWizard#addPages()
     */
    public final void addPages() {
        try {
            objectPage = createFirstPage(selection); 
            addPage(objectPage);
            createAdditionalPages();
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }
    
    protected abstract IpsObjectPage createFirstPage(IStructuredSelection selection) throws Exception;
    
    protected abstract void createAdditionalPages() throws Exception;
    
    protected IpsObjectType getIpsObjectType() {
        return ipsObjectType;
    }
    
    /** 
     * Overridden method.
     * @see org.eclipse.jface.wizard.IWizard#performFinish()
     */
    public final boolean performFinish() {
        try {
            IIpsPackageFragment pack = objectPage.getIpsPackageFragment();
            IIpsSrcFile file = pack.createIpsFile(ipsObjectType, objectPage.getIpsObjectName(), true, null);
            finishIpsObject(file.getIpsObject());
            file.save(true, null);
            IpsPlugin.getDefault().openEditor(file);
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
        return true;
    }
    
    protected abstract void finishIpsObject(IIpsObject ipsObject) throws CoreException;

    /** 
     * Overridden method.
     * 
     * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
     */
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.selection = selection;
    }

	
}
