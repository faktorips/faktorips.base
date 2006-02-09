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
        setWindowTitle("New " + ipsObjectType.getName());
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
            IIpsPackageFragment pack = objectPage.getPdPackageFragment();
            IIpsSrcFile file = pack.createIpsFile(ipsObjectType, objectPage.getPdObjectName(), true, null);
            finishPdObject(file.getIpsObject());
            file.save(true, null);
            IpsPlugin.getDefault().openEditor(file);
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
        return true;
    }
    
    protected abstract void finishPdObject(IIpsObject pdObject) throws CoreException;

    /** 
     * Overridden method.
     * 
     * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
     */
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.selection = selection;
    }

	
}
